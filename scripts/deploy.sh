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

nohup java $JAVA_AGENT_OPTS -jar \
  -Duser.timezone=Asia/Seoul \
  $JAR_PATH \
  >> /home/ec2-user/app/nohup.out 2>&1 &

echo "> 15초 후 헬스체크 시작"
sleep 15

for i in {1..10}; do
  RESPONSE=$(curl -s http://localhost:8080/actuator/health || true)
  if echo "$RESPONSE" | grep -q '"status":"UP"'; then
    echo "> 헬스체크 성공"
    break
  fi
  echo "> 헬스체크 실패($i/10): $RESPONSE"
  if [ $i -eq 10 ]; then
    echo "> 배포 실패"
    exit 1
  fi
  sleep 10
done

echo "> Nginx 시작"
sudo systemctl start nginx || true
sudo systemctl enable nginx || true

echo "> 배포 완료"
