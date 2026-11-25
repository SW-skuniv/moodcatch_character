FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# OS 레벨 타임존을 KST(Asia/Seoul)로 고정 (기본 UTC라 스케줄러/로그 시각이 9시간 어긋났음)
RUN apt-get update && apt-get install -y tzdata && \
    ln -fs /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV TZ=Asia/Seoul

COPY build/libs/*.jar app.jar

EXPOSE 8081

# JVM 레벨에서도 KST를 명시 (OS 설정과 이중 보장)
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "app.jar"]