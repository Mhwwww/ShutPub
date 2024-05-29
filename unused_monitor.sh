#!/bin/bash

# 指定Broker.jar的进程名称
PROCESS_NAME="Broker.jar"

# 指定输出CSV文件名称
OUTPUT_DIR="./log"
OUTPUT_FILE="$OUTPUT_DIR/cpu_ram_utilization.csv"

# 检查是否存在"log"文件夹，如果不存在则创建它
if [ ! -d "$OUTPUT_DIR" ]; then
    mkdir -p "$OUTPUT_DIR"
fi

# 检查是否存在输出文件，如果不存在则创建并写入标题行
if [ ! -e "$OUTPUT_FILE" ]; then
    echo "Time,Unix Time,CPU Utilization (%),RAM Utilization (MB)" > "$OUTPUT_FILE"
fi

# 运行监测循环，持续60秒
for ((i=0; i<600; i++)); do
    # 获取当前时间和Unix时间戳
    CURRENT_TIME=$(date +"%Y-%m-%d %H:%M:%S")
    UNIX_TIME=$(date +"%s")

    # 使用ps命令获取Broker.jar进程的CPU和RAM利用率
    PROCESS_INFO=$(ps aux | grep "$PROCESS_NAME" | grep -v "grep")

    if [ -n "$PROCESS_INFO" ]; then
        CPU_UTILIZATION=$(echo $PROCESS_INFO | awk '{print $3}')
        RAM_UTILIZATION=$(echo $PROCESS_INFO | awk '{print $6/1024}') # 将RAM利用率转换为MB
    else
        CPU_UTILIZATION="N/A"
        RAM_UTILIZATION="N/A"
    fi

    # 将数据写入CSV文件
    echo "$CURRENT_TIME,$UNIX_TIME,$CPU_UTILIZATION,$RAM_UTILIZATION" >> "$OUTPUT_FILE"

    # 每秒等待1秒
    sleep 1
done

echo "Finished! $OUTPUT_FILE"
