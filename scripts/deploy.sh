#!/bin/bash
set -e

APP_DIR=/home/ec2-user/app
cd "$APP_DIR"
JAR_PATH=$(ls $APP_DIR/*.jar | grep -v plain | head -1)
echo "> JAR 파일: $JAR_PATH"

echo "> 실행 중인 애플리케이션 종료"
CURRENT_PID=$(pgrep -f '\.jar' || true)
if [ -n "$CURRENT_PID" ]; then
  echo "> 종료: $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
else
  echo "> 실행 중인 애플리케이션 없음"
fi

echo "> 애플리케이션 시작"
OTEL_AGENT_JAR=$APP_DIR/grafana-opentelemetry-java.jar
OTEL_ENV_FILE=$APP_DIR/otel.env
JAVA_AGENT_OPTS=""
if [ -f "$OTEL_AGENT_JAR" ] && [ -f "$OTEL_ENV_FILE" ]; then
  echo "> Grafana OTel agent 감지, 모니터링 활성화"
  set -a
  source "$OTEL_ENV_FILE"
  set +a
  JAVA_AGENT_OPTS="-javaagent:$OTEL_AGENT_JAR"
else
  echo "> Grafana OTel agent 미설정, 모니터링 없이 실행"
fi

LOG_FILE=/home/ec2-user/app/nohup.out
START_LINE=$(wc -l < "$LOG_FILE" 2>/dev/null || echo 0)

nohup java $JAVA_AGENT_OPTS -jar \
  -Duser.timezone=Asia/Seoul \
  $JAR_PATH \
  >> "$LOG_FILE" 2>&1 &
APP_PID=$!
echo "> 프로세스 시작됨 (PID: $APP_PID)"

echo "> 앱 준비 신호 대기 중 (최대 300초, 프로세스 생존 여부로 실패 판단)"
MAX_WAIT=300
ELAPSED=0
READY=false

while [ $ELAPSED -lt $MAX_WAIT ]; do
  if ! kill -0 $APP_PID 2>/dev/null; then
    echo "> 프로세스가 예기치 않게 종료됨 (크래시)"
    echo "> 최근 로그:"
    tail -n 40 "$LOG_FILE"
    echo "> 배포 실패"
    exit 1
  fi

  if tail -n +"$((START_LINE + 1))" "$LOG_FILE" | grep -q "Started ServerApplication"; then
    echo "> 앱 준비 완료 신호 감지 (${ELAPSED}초 소요)"
    READY=true
    break
  fi

  sleep 2
  ELAPSED=$((ELAPSED + 2))
done

if [ "$READY" != "true" ]; then
  echo "> ${MAX_WAIT}초 내에 준비 신호 없음 — 안전장치 발동"
  echo "> 최근 로그:"
  tail -n 40 "$LOG_FILE"
  echo "> 배포 실패"
  exit 1
fi

RESPONSE=$(curl -s http://localhost:8080/actuator/health || true)
echo "> 헬스체크 확인: $RESPONSE"

echo "> Nginx 시작"
sudo systemctl start nginx || true
sudo systemctl enable nginx || true

echo "> 배포 완료"
