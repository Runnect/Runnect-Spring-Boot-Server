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
sleep 10

for retry_count in {1..10}
do
  response=$(curl -s http://localhost:$IDLE_PORT/actuator/health)
  up_count=$(echo $response | grep 'UP' | wc -l)

  if [ $up_count -ge 1 ]
  then
      echo "> Health check 성공"
      break
  else
      echo "> Health check의 응답을 알 수 없거나 혹은 status가 UP이 아닙니다."
  fi

  if [ $retry_count -eq 10 ]
  then
    echo "> Health check 실패."
    exit 1
  fi

  sleep 10
done

echo "> Nginx 복구"
sudo systemctl stop nginx 2>/dev/null || true
sleep 1
sudo systemctl start nginx
sleep 2

echo "> 방화벽 초기화"
sudo ufw disable 2>/dev/null || true
sudo iptables -P INPUT ACCEPT 2>/dev/null || true
sudo iptables -P FORWARD ACCEPT 2>/dev/null || true
sudo iptables -P OUTPUT ACCEPT 2>/dev/null || true
sudo iptables -F 2>/dev/null || true

echo "> 스위칭"
sleep 10
/home/ubuntu/app/nonstop/switch.sh

echo "======= DIAG START ======="
echo "[IP] $(curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo NONE)"
echo "[INST] $(curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/instance-id 2>/dev/null || echo NONE)"
echo "[SG] $(curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/security-groups 2>/dev/null || echo NONE)"
echo "[MAC] $(curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/mac 2>/dev/null || echo NONE)"
MAC=$(curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/mac 2>/dev/null)
if [ -n "$MAC" ]; then
  echo "[VPC] $(curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/network/interfaces/macs/$MAC/vpc-id 2>/dev/null || echo NONE)"
  echo "[SUBNET] $(curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/network/interfaces/macs/$MAC/subnet-id 2>/dev/null || echo NONE)"
  echo "[SG-IDS] $(curl -s --connect-timeout 3 http://169.254.169.254/latest/meta-data/network/interfaces/macs/$MAC/security-group-ids 2>/dev/null || echo NONE)"
fi
echo "[NGINX] $(sudo systemctl is-active nginx 2>&1)"
echo "[PORTS] $(sudo ss -tlnp 2>/dev/null | grep -E ':(80|8081|8082) ' || echo NONE)"
echo "[JAVA] $(pgrep -a java 2>/dev/null | head -2 || echo NONE)"
echo "[UFW] $(sudo ufw status 2>/dev/null | head -1)"
echo "[IPT] $(sudo iptables -S 2>/dev/null | head -5)"
echo "[LOCAL80] $(curl -s -o /dev/null -w '%{http_code}' http://localhost/actuator/health 2>/dev/null)"
echo "[LOCAL8081] $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/actuator/health 2>/dev/null)"
echo "[LOCAL8082] $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8082/actuator/health 2>/dev/null)"
echo "[PROFILE] $(curl -s http://localhost/profile 2>/dev/null)"
echo "[MEM] $(free -m 2>/dev/null | grep Mem | awk '{print $2"M total, "$3"M used, "$4"M free"}')"
echo "======= DIAG END ======="
exit 1
