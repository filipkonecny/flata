#!/bin/bash

# Script for running all the benchmarks

# Paths to benchmark folders
declare -A BENCHMARKS_DIRS=(
    ["reach"]="../flataProvidedBenchmarks/benchmarks-reach"
    ["term"]="../flataProvidedBenchmarks/benchmarks-term"
    ["sil"]="../flataProvidedBenchmarks/benchmarks-sil"
    ["recur"]="../flataProvidedBenchmarks/benchmarks-recur"
)

# Paths to scripts
declare -A SCRIPTS=(
    ["reach"]="./flata-reachability.sh"
    ["term"]="./flata-termination.sh"
    ["sil"]="./flata-sil.sh"
    ["recur"]="./flata-reachability.sh" # Using the reachability script for recursivity benchmarks
)

# Output files
declare -A OUTPUT_FILES=(
    ["reach"]="benchmark_reach_results.txt"
    ["term"]="benchmark_term_results.txt"
    ["sil"]="benchmark_sil_results.txt"
    ["recur"]="benchmark_recur_results.txt"
)

# Define the timeout duration in seconds
TIMEOUT_DURATION=120

# Redirect standard input from the terminal
exec 3<&0

# Process each benchmark category
for key in "${!BENCHMARKS_DIRS[@]}"; do
    echo "Processing category: $key"
    BENCHMARKS_DIR="${BENCHMARKS_DIRS[$key]}"
    FLATA_SCRIPT="${SCRIPTS[$key]}"
    OUTPUT_FILE="${OUTPUT_FILES[$key]}"
    
    # Count total benchmarks
    total_files=$(find "$BENCHMARKS_DIR" -type f -name "*.nts" | wc -l)
    current_file=0

    # Process each .nts file in the directory
    find "$BENCHMARKS_DIR" -type f -name "*.nts" | while read file; do
        current_file=$((current_file + 1))
        echo "Processing $file... ($current_file of $total_files)" >&2

        # Run the corresponding script with a timeout and redirect its output to the designated output file
        echo "Processing $file... ($current_file of $total_files)" >> "$OUTPUT_FILE"
        timeout $TIMEOUT_DURATION "$FLATA_SCRIPT" "$file" >> "$OUTPUT_FILE" 2>&1
        
        if [ $? -eq 124 ]; then
            echo "Timeout: Processing of $file was terminated after $TIMEOUT_DURATION seconds." >> "$OUTPUT_FILE"
            echo "Timeout: Processing of $file was terminated after $TIMEOUT_DURATION seconds." >&2
        fi
    done
    echo "All $key files have been processed. Results are saved in $OUTPUT_FILE."
done

# Restore standard input
exec 3<&-

echo "All categories have been processed."
