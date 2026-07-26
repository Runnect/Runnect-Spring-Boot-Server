# --- Build stage ---
FROM gradle:8.2.1-jdk11 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

COPY src ./src

# 로컬/모니터링 검증용 application.yml은 build context 루트에 두고 복사한다.
# (CI/CD에서는 GitHub Secret으로 별도 생성 — 이 Dockerfile은 로컬 검증 전용)
COPY application.yml ./application.yml

RUN ./gradlew bootJar -x test --no-daemon

# --- Run stage ---
FROM eclipse-temurin:11-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/application.yml application.yml

# Grafana Cloud 모니터링용 OTel Java agent (없어도 앱 실행에는 영향 없음)
RUN curl -sL -o grafana-opentelemetry-java.jar \
    https://github.com/grafana/grafana-opentelemetry-java/releases/latest/download/grafana-opentelemetry-java.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Xmx192m", "-XX:MaxMetaspaceSize=128m", "-XX:ReservedCodeCacheSize=48m", "-javaagent:grafana-opentelemetry-java.jar", "-jar", "app.jar", "--spring.config.location=file:./application.yml"]
