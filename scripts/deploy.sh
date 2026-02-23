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

echo "> Nginx 상태 확인 및 복구"
sudo systemctl stop nginx 2>/dev/null || true
sleep 1
sudo systemctl start nginx
sleep 2
if sudo systemctl is-active --quiet nginx; then
  echo "> Nginx 시작 성공"
else
  echo "> Nginx 시작 실패. 강제 재시작 시도..."
  sudo killall nginx 2>/dev/null || true
  sleep 1
  sudo nginx
  sleep 2
fi

echo "> 방화벽 규칙 초기화 (인바운드 트래픽 허용)"
# UFW 비활성화
sudo ufw disable 2>/dev/null || true

# iptables 초기화 - 모든 트래픽 허용
sudo iptables -P INPUT ACCEPT 2>/dev/null || true
sudo iptables -P FORWARD ACCEPT 2>/dev/null || true
sudo iptables -P OUTPUT ACCEPT 2>/dev/null || true
sudo iptables -F 2>/dev/null || true
sudo iptables -X 2>/dev/null || true

echo "> 스위칭"
sleep 10
/home/ubuntu/app/nonstop/switch.sh

echo "> 배포 완료. 진단 정보 출력 (logTail 캡처용)..."
echo "=========================================="
echo "=== [DIAG] Public IP ==="
curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "메타데이터 접근 불가"
echo ""
echo "=== [DIAG] Instance ID ==="
curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/instance-id 2>/dev/null || echo "메타데이터 접근 불가"
echo ""
echo "=== [DIAG] Security Groups ==="
curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/security-groups 2>/dev/null || echo "메타데이터 접근 불가"
echo ""
echo "=== [DIAG] Listening Ports ==="
sudo ss -tlnp 2>/dev/null | head -20
echo ""
echo "=== [DIAG] Nginx Status ==="
sudo systemctl is-active nginx 2>&1
echo ""
echo "=== [DIAG] Java Processes ==="
pgrep -a java 2>/dev/null | head -5
echo ""
echo "=== [DIAG] iptables ==="
sudo iptables -L -n 2>/dev/null | head -20
echo ""
echo "=== [DIAG] UFW Status ==="
sudo ufw status 2>/dev/null
echo ""
echo "=== [DIAG] Network Interfaces ==="
ip addr show 2>/dev/null | grep -E "inet |state" | head -10
echo ""
echo "=== [DIAG] Localhost Tests ==="
echo "8081: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/actuator/health 2>/dev/null)"
echo "8082: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8082/actuator/health 2>/dev/null)"
echo "80: $(curl -s -o /dev/null -w '%{http_code}' http://localhost/actuator/health 2>/dev/null)"
echo "profile: $(curl -s http://localhost/profile 2>/dev/null)"
echo ""
echo "=== [DIAG] Memory ==="
free -h 2>/dev/null | head -3
echo ""
echo "=== [DIAG] nohup.out (last 20 lines) ==="
tail -20 /home/ubuntu/app/nohup.out 2>/dev/null
echo "=========================================="
echo "> 진단 출력 완료. 의도적 실패 (logTail 캡처)..."
exit 1
