# 使用适当的基础镜像，例如 openjdk:8-jre-alpine
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

COPY . .

# 编译 Java 程序
RUN javac BrokerPSF.java

# 运行 Broker 程序
CMD ["java", "BrokerPSF"]
