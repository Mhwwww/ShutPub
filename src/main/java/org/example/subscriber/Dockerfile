# 使用适当的基础镜像，例如 openjdk:8-jre-alpine
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 将 SimpleSubscriber.java 复制到容器中
COPY /src/main/java/org/example/subscriber /app/subscriber


# 编译 Java 程序
RUN apk --no-cache add maven && \
    mvn clean install -DskipTests \

# 运行 Subscriber 程序
CMD ["java", "-cp", "target/classes", "org.example.subscriber.SimpleSubscriber"]
