#!/bin/bash
BUILD_PATH=$(ls /home/ubuntu/app/server-0.0.1-SNAPSHOT.jar)
JAR_NAME=$(basename $BUILD_PATH)
echo "> build 파일명: $JAR_NAME"

echo "> build 파일 복사"
DEPLOY_PATH=/home/ubuntu/app/nonstop/jar/
cp $BUILD_PATH $DEPLOY_PATH

echo "> 현재 구동중인 Set 확인"
CURRENT_PROFILE=$(curl -s http://localhost/profile)
echo "> $CURRENT_PROFILE"

# 쉬고 있는 set 찾기: set1이 사용중이면 set2가 쉬고 있고, 반대면 set1이 쉬고 있음
if [ $CURRENT_PROFILE == set1 ]
then
  IDLE_PROFILE=set2
  IDLE_PORT=8082
elif [ $CURRENT_PROFILE == set2 ]
then
  IDLE_PROFILE=set1
  IDLE_PORT=8081
else
  echo "> 일치하는 Profile이 없습니다. Profile: $CURRENT_PROFILE"
  echo "> set1을 할당합니다. IDLE_PROFILE: set1"
  IDLE_PROFILE=set1
  IDLE_PORT=8081
fi

echo "> application.jar 교체"
IDLE_APPLICATION=$IDLE_PROFILE-Runnect-Spring-Boot-Server.jar
IDLE_APPLICATION_PATH=$DEPLOY_PATH$IDLE_APPLICATION

ln -Tfs $DEPLOY_PATH$JAR_NAME $IDLE_APPLICATION_PATH

echo "> $IDLE_PROFILE 에서 구동중인 애플리케이션 pid 확인"
IDLE_PID=$(pgrep -f $IDLE_APPLICATION)

if [ -z $IDLE_PID ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "> kill -15 $IDLE_PID"
  kill -15 $IDLE_PID
  sleep 20
fi

echo "> $IDLE_PROFILE 배포"
nohup java -jar -Duser.timezone=Asia/Seoul -Dspring.profiles.active=$IDLE_PROFILE $IDLE_APPLICATION_PATH >> /home/ubuntu/app/nohup.out 2>&1 &

echo "> $IDLE_PROFILE 10초 후 Health check 시작"
echo "> curl -s http://localhost:$IDLE_PORT/health "
sleep 10

for retry_count in {1..10}
do
  response=$(curl -s http://localhost:$IDLE_PORT/actuator/health)
  up_count=$(echo $response | grep 'UP' | wc -l)

  if [ $up_count -ge 1 ]
  then # $up_count >= 1 ("UP" 문자열이 있는지 검증)
      echo "> Health check 성공"
      break
  else
      echo "> Health check의 응답을 알 수 없거나 혹은 status가 UP이 아닙니다."
      echo "> Health check: ${response}"
  fi

  if [ $retry_count -eq 10 ]
  then
    echo "> Health check 실패. "
    echo "> Nginx에 연결하지 않고 배포를 종료합니다."
    exit 1
  fi

  echo "> Health check 연결 실패. 재시도..."
  sleep 10
done

echo "> Nginx 상태 확인"
if ! sudo systemctl is-active --quiet nginx; then
  echo "> Nginx가 중지되어 있습니다. 재시작합니다."
  sudo systemctl start nginx
  sleep 2
  if sudo systemctl is-active --quiet nginx; then
    echo "> Nginx 재시작 성공"
  else
    echo "> Nginx 재시작 실패. 상태:"
    sudo systemctl status nginx
  fi
else
  echo "> Nginx 정상 구동 중"
fi

echo "> 스위칭"
sleep 10
/home/ubuntu/app/nonstop/switch.sh

echo "> 배포 완료. 진단 정보 수집 중..."

DIAG_FILE="/tmp/server-diagnostic-$(date +%Y%m%d-%H%M%S).txt"
{
  echo "========== SERVER DIAGNOSTIC =========="
  echo "Date: $(date)"
  echo ""

  echo "=== Public IP (EC2 metadata) ==="
  curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "메타데이터 접근 불가"
  echo ""

  echo "=== Network Interfaces ==="
  ip addr show 2>/dev/null || ifconfig 2>/dev/null
  echo ""

  echo "=== Nginx Status ==="
  sudo systemctl status nginx 2>&1
  echo ""

  echo "=== Nginx Config ==="
  sudo nginx -T 2>&1
  echo ""

  echo "=== Listening Ports ==="
  sudo ss -tlnp 2>/dev/null || sudo netstat -tlnp 2>/dev/null
  echo ""

  echo "=== Java Processes ==="
  pgrep -a java 2>/dev/null || echo "Java 프로세스 없음"
  echo ""

  echo "=== iptables Rules ==="
  sudo iptables -L -n 2>/dev/null || echo "iptables 조회 실패"
  echo ""

  echo "=== Localhost Health Check ==="
  curl -s http://localhost:8081/actuator/health 2>/dev/null
  echo ""
  curl -s http://localhost:8082/actuator/health 2>/dev/null
  echo ""
  curl -s http://localhost/actuator/health 2>/dev/null
  echo ""
  curl -s http://localhost/profile 2>/dev/null
  echo ""

  echo "=== Disk Usage ==="
  df -h 2>/dev/null
  echo ""

  echo "=== Memory Usage ==="
  free -h 2>/dev/null
  echo ""

  echo "=== nohup.out (last 50 lines) ==="
  tail -50 /home/ubuntu/app/nohup.out 2>/dev/null || echo "nohup.out 없음"
  echo ""

  echo "========== END DIAGNOSTIC =========="
} > "$DIAG_FILE" 2>&1

echo "> 진단 결과를 S3에 업로드..."
aws s3 cp "$DIAG_FILE" s3://runnect-prod-bucket/diagnostics/$(basename "$DIAG_FILE") 2>&1 || echo "> S3 업로드 실패"

echo "> 진단 완료"
