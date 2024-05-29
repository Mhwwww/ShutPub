#!/bin/bash

# Output CSV file name
output_file="cpu_ram_network_utilization.csv"

# Main loop to measure CPU, RAM, and network utilization
measure_utilization() {
    # Get current Unix timestamp
    unix_timestamp=$(date +%s)

    # Measure CPU utilization using mpstat
    cpu_utilization=$(mpstat 1 1 | awk 'END{print 100-$NF""}')

    # Measure RAM utilization using free
    ram_utilization=$(free | awk 'NR==2{printf "%.2f", $3*100/$2}')

    # Measure network throughput using sar
    network_info=$(sar -n DEV 1 1 | tail -n 1 | awk '{print $5","$6}')

    # Append data to the CSV file
    echo "$(date +"%Y-%m-%d %H:%M:%S"),$unix_timestamp,$cpu_utilization,$ram_utilization,$network_info" >> "$output_file"
}

# Check if the output file already exists; if not, create it with headers
if [ ! -e "$output_file" ]; then
    echo "Time,Unix Time,CPU Utilization (%),RAM Utilization (%),rxKBs,txKBs" > "$output_file"
fi

end_time=$((SECONDS + 600))  # 600 seconds (10 minutes)

# Run the main loop for 5 minutes
while [ $SECONDS -lt $end_time ]; do
    measure_utilization
done

echo "Finished! $output_file"
