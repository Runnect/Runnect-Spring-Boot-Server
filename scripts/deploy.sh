#!/bin/bash
set -e

APP_DIR=/home/ec2-user/app
JAR_PATH=$(ls $APP_DIR/*.jar | head -1)
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
nohup java -jar \
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
