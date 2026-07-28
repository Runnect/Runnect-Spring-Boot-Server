#!/bin/bash
set -e

APP_DIR=/home/ec2-user/app
cd "$APP_DIR"
JAR_PATH=$(ls $APP_DIR/*.jar | grep -v plain | grep -v opentelemetry | head -1)
echo "> JAR 파일: $JAR_PATH"

OTEL_AGENT_JAR=$APP_DIR/grafana-opentelemetry-java.jar
OTEL_ENV_FILE=$APP_DIR/otel.env

# 포트 하나씩 순서대로 종료→재기동한다 (롤링 재시작).
# nginx가 두 포트를 upstream으로 물고 있어서, 한쪽이 재시작되는 동안에도
# 다른 한쪽이 계속 트래픽을 받아 무중단으로 배포된다.
deploy_process() {
  local PORT=$1
  local USE_OTEL=$2
  local LOG_FILE=$APP_DIR/app-$PORT.log

  echo "=== [$PORT] 배포 시작 ==="

  echo "> [$PORT] 실행 중인 프로세스 종료"
  OLD_PID=$(pgrep -f "server.port=$PORT" || true)
  if [ -n "$OLD_PID" ]; then
    echo "> [$PORT] 종료: $OLD_PID"
    kill -15 $OLD_PID
    sleep 5
  else
    echo "> [$PORT] 실행 중인 프로세스 없음"
  fi

  JAVA_AGENT_OPTS=""
  if [ "$USE_OTEL" = "true" ] && [ -f "$OTEL_AGENT_JAR" ] && [ -f "$OTEL_ENV_FILE" ]; then
    echo "> [$PORT] Grafana OTel agent 활성화"
    set -a
    source "$OTEL_ENV_FILE"
    set +a
    JAVA_AGENT_OPTS="-javaagent:$OTEL_AGENT_JAR"
  else
    echo "> [$PORT] Grafana OTel agent 미적용"
  fi

  START_LINE=$(wc -l < "$LOG_FILE" 2>/dev/null || echo 0)

  nohup java $JAVA_AGENT_OPTS -jar \
    -Duser.timezone=Asia/Seoul \
    $JAR_PATH \
    --server.port=$PORT \
    >> "$LOG_FILE" 2>&1 &
  APP_PID=$!
  echo "> [$PORT] 프로세스 시작됨 (PID: $APP_PID)"

  local MAX_WAIT=240
  local ELAPSED=0
  local READY=false

  while [ $ELAPSED -lt $MAX_WAIT ]; do
    if ! kill -0 $APP_PID 2>/dev/null; then
      echo "> [$PORT] 프로세스가 예기치 않게 종료됨 (크래시)"
      echo "> [$PORT] 최근 로그:"
      tail -n 40 "$LOG_FILE"
      return 1
    fi

    if tail -n +"$((START_LINE + 1))" "$LOG_FILE" | grep -q "Started ServerApplication"; then
      echo "> [$PORT] 준비 완료 신호 감지 (${ELAPSED}초 소요)"
      READY=true
      break
    fi

    sleep 2
    ELAPSED=$((ELAPSED + 2))
  done

  if [ "$READY" != "true" ]; then
    echo "> [$PORT] ${MAX_WAIT}초 내에 준비 신호 없음 — 안전장치 발동"
    echo "> [$PORT] 최근 로그:"
    tail -n 40 "$LOG_FILE"
    return 1
  fi

  RESPONSE=$(curl -s --max-time 10 http://localhost:$PORT/actuator/health || true)
  echo "> [$PORT] 헬스체크 확인: $RESPONSE"

  if ! echo "$RESPONSE" | grep -q '"status":"UP"'; then
    echo "> [$PORT] 헬스체크 실패"
    return 1
  fi

  return 0
}

if ! deploy_process 8080 true; then
  echo "> 8080 배포 실패 — 8081은 계속 서비스 중이므로 전체 다운은 아니지만 배포는 실패 처리"
  echo "> 배포 실패"
  exit 1
fi

if ! deploy_process 8081 false; then
  echo "> 8081 배포 실패 — 8080은 정상 서비스 중이므로 전체 다운은 아니지만 배포는 실패 처리"
  echo "> 배포 실패"
  exit 1
fi

echo "> Nginx 시작"
sudo systemctl start nginx || true
sudo systemctl enable nginx || true

echo "> 배포 완료 (8080, 8081 모두 정상)"
