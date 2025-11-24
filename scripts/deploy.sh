#!/bin/bash

# Bibly 배포 스크립트
# 사용법: ./scripts/deploy.sh

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 환경 변수 확인
if [ -z "$EC2_HOST" ]; then
    echo -e "${RED}ERROR: EC2_HOST 환경 변수가 설정되지 않았습니다.${NC}"
    exit 1
fi

if [ -z "$EC2_USERNAME" ]; then
    echo -e "${YELLOW}WARNING: EC2_USERNAME이 설정되지 않았습니다. 기본값 'ubuntu' 사용${NC}"
    EC2_USERNAME="ubuntu"
fi

if [ -z "$EC2_SSH_KEY_PATH" ]; then
    echo -e "${RED}ERROR: EC2_SSH_KEY_PATH 환경 변수가 설정되지 않았습니다.${NC}"
    echo "예시: export EC2_SSH_KEY_PATH=~/.ssh/bibly-keypair.pem"
    exit 1
fi

# SSH 키 파일 존재 확인
if [ ! -f "$EC2_SSH_KEY_PATH" ]; then
    echo -e "${RED}ERROR: SSH 키 파일을 찾을 수 없습니다: $EC2_SSH_KEY_PATH${NC}"
    exit 1
fi

# SSH 키 권한 확인
KEY_PERMS=$(stat -c "%a" "$EC2_SSH_KEY_PATH" 2>/dev/null || stat -f "%A" "$EC2_SSH_KEY_PATH" 2>/dev/null)
if [ "$KEY_PERMS" != "400" ] && [ "$KEY_PERMS" != "600" ]; then
    echo -e "${YELLOW}WARNING: SSH 키 권한이 안전하지 않습니다. 권한 변경 중...${NC}"
    chmod 600 "$EC2_SSH_KEY_PATH"
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Bibly 배포 시작${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "배포 대상: $EC2_USERNAME@$EC2_HOST"
echo ""

# 1. 프로젝트 빌드
echo -e "${GREEN}[1/6] 프로젝트 빌드 중...${NC}"
./gradlew clean build -x test
echo -e "${GREEN}✓ 빌드 완료${NC}"
echo ""

# 2. JAR 파일 찾기
echo -e "${GREEN}[2/6] JAR 파일 확인 중...${NC}"
JAR_FILE=$(ls build/libs/*.jar | grep -v plain.jar)
if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}ERROR: JAR 파일을 찾을 수 없습니다.${NC}"
    exit 1
fi
echo "JAR 파일: $JAR_FILE"
echo -e "${GREEN}✓ JAR 파일 확인 완료${NC}"
echo ""

# 3. EC2 연결 테스트
echo -e "${GREEN}[3/6] EC2 연결 테스트 중...${NC}"
if ! ssh -i "$EC2_SSH_KEY_PATH" -o ConnectTimeout=10 "$EC2_USERNAME@$EC2_HOST" "echo 'Connected successfully'" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: EC2 서버에 연결할 수 없습니다.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ EC2 연결 성공${NC}"
echo ""

# 4. 기존 애플리케이션 중지
echo -e "${GREEN}[4/6] 기존 애플리케이션 중지 중...${NC}"
ssh -i "$EC2_SSH_KEY_PATH" "$EC2_USERNAME@$EC2_HOST" '
    if sudo systemctl is-active --quiet bibly.service; then
        sudo systemctl stop bibly.service
        echo "애플리케이션 중지됨"
    else
        echo "실행 중인 애플리케이션이 없습니다"
    fi
' || true
echo -e "${GREEN}✓ 중지 완료${NC}"
echo ""

# 5. JAR 파일 배포
echo -e "${GREEN}[5/6] JAR 파일 배포 중...${NC}"
scp -i "$EC2_SSH_KEY_PATH" "$JAR_FILE" "$EC2_USERNAME@$EC2_HOST:/opt/bibly/bibly.jar"
echo -e "${GREEN}✓ 파일 전송 완료${NC}"
echo ""

# 6. 애플리케이션 시작
echo -e "${GREEN}[6/6] 애플리케이션 시작 중...${NC}"
ssh -i "$EC2_SSH_KEY_PATH" "$EC2_USERNAME@$EC2_HOST" '
    sudo systemctl daemon-reload
    sudo systemctl start bibly.service
    sudo systemctl enable bibly.service
'

echo "애플리케이션 시작 대기 중 (30초)..."
sleep 30

# 헬스 체크
echo -e "${GREEN}헬스 체크 수행 중...${NC}"
HEALTH_CHECK_RESULT=$(ssh -i "$EC2_SSH_KEY_PATH" "$EC2_USERNAME@$EC2_HOST" '
    for i in {1..10}; do
        if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
            echo "SUCCESS"
            exit 0
        fi
        echo "대기 중... ($i/10)"
        sleep 10
    done
    echo "FAILED"
    exit 1
' || echo "FAILED")

echo ""
echo -e "${GREEN}========================================${NC}"
if [ "$HEALTH_CHECK_RESULT" == "FAILED" ]; then
    echo -e "${RED}  배포 실패!${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo "최근 로그:"
    ssh -i "$EC2_SSH_KEY_PATH" "$EC2_USERNAME@$EC2_HOST" 'sudo journalctl -u bibly.service -n 20 --no-pager'
    exit 1
else
    echo -e "${GREEN}  배포 성공!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "애플리케이션 URL: http://$EC2_HOST:8080"
    echo "Swagger UI: http://$EC2_HOST:8080/swagger-ui.html"
    echo ""
    echo "로그 확인 명령어:"
    echo "  ssh -i $EC2_SSH_KEY_PATH $EC2_USERNAME@$EC2_HOST 'sudo journalctl -u bibly.service -f'"
fi
