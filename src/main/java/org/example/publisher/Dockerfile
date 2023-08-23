# 使用适当的基础镜像，例如 openjdk:8-jre-alpine
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 将 SimplePSFPublisher.java 复制到容器中
COPY src/main/java/org/example//publisher /app/publisher

RUN apk --no-cache add maven && \
    mvn clean install -DskipTests

# 运行 Publisher 程序
CMD ["java", "-cp", "target/classes", "org.example.publisher.SimplePSFPublisher"]
