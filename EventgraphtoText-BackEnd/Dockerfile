# 使用官方 OpenJDK 17 运行时镜像
FROM eclipse-temurin:17-jre

# 设置工作目录
WORKDIR /app

# 复制本地 jar 文件到容器中
COPY myapp-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口（Spring Boot 默认 8080）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
