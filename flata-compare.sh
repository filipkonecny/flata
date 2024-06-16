#!/bin/bash

# Script to run old and new program versions on benchmarks and compare outputs

# Directories for old and new program versions
OLD_VERSION_DIR="./flataArchived"
NEW_VERSION_DIR="./dist"

# Paths to benchmark folders and their corresponding scripts
declare -A BENCHMARKS_DIRS=(
    ["reach"]="./flataProvidedBenchmarks/benchmarks-reach"
    ["sil"]="./flataProvidedBenchmarks/benchmarks-sil"
    ["recur"]="./flataProvidedBenchmarks/benchmarks-recur"
)

declare -A SCRIPTS=(
    ["reach"]="./flata-reachability.sh"
    # ["term"]="./flata-termination.sh"
    ["sil"]="./flata-sil.sh"
    ["recur"]="./flata-reachability.sh" # Using the reachability script for recursivity benchmarks
)

# Output directory for differences
DIFF_OUTPUT_DIR="./output_differences/"
mkdir -p "$DIFF_OUTPUT_DIR"

# Helper function to compare output and save if different
compare_and_save() {
    local category="$1"
    local benchmark="$2"
    local old_output="$3"
    local new_output="$4"
    
    if ! diff -q "$old_output" "$new_output" > /dev/null; then
        # Differences found, save them
        local diff_file="${DIFF_OUTPUT_DIR}${category}_$(basename "$benchmark").diff"
        echo "Differences found for $(basename "$benchmark"), saving to $diff_file"
        diff "$old_output" "$new_output" > "$diff_file"
    fi
}

# Function to execute a version of the program with timeout
run_program() {
    local program_dir="$1"
    local script_name="$2"
    local benchmark="$3"
    local output_file="$4"
    local timeout_duration="$5"
    local absolute_path=$(realpath "$benchmark")  # Convert to absolute path

    # Temporarily move to program directory to respect relative classpath settings
    pushd "$program_dir" > /dev/null

    # Execute the script with the absolute path to the benchmark using timeout
    timeout "$timeout_duration" "./$script_name" "$absolute_path" > "$output_file"
    local status=$?

    # Return to the original directory
    popd > /dev/null

    # Check if the process was killed by the timeout
    if [ $status -eq 124 ]; then
        echo "Timeout: Processing of $benchmark exceeded $timeout_duration seconds."
    fi
}

# Process each category and benchmark
for category in "${!BENCHMARKS_DIRS[@]}"; do
    echo "Processing category: $category"
    BENCHMARKS_DIR="${BENCHMARKS_DIRS[$category]}"
    SCRIPT_NAME="${SCRIPTS[$category]}"  # Assuming the same script name for simplicity
    TIMEOUT_DURATION=120

    echo "Running benchmarks in $BENCHMARKS_DIR with $SCRIPT_NAME..."

    # Get the total number of benchmark files
    total_files=$(find "$BENCHMARKS_DIR" -type f -name "*.nts" | wc -l)
    current_file=0

    # Process each benchmark file
    find "$BENCHMARKS_DIR" -type f -name "*.nts" | while read benchmark; do
        current_file=$((current_file + 1))
        echo "Running $category benchmark $(basename "$benchmark") ($current_file of $total_files)..."

        # Prepare temporary files for output
        old_output=$(mktemp)
        new_output=$(mktemp)

        # Execute old and new versions of the script with timeout
        run_program "$OLD_VERSION_DIR" "$SCRIPT_NAME" "$benchmark" "$old_output" "$TIMEOUT_DURATION"
        run_program "$NEW_VERSION_DIR" "$SCRIPT_NAME" "$benchmark" "$new_output" "$TIMEOUT_DURATION"

        # Compare outputs
        compare_and_save "$category" "$benchmark" "$old_output" "$new_output"

        # Clean up temporary files
        rm "$old_output" "$new_output"
    done

    echo "All $category benchmarks processed ($total_files files)."
done

echo "All categories have been processed. Check the '$DIFF_OUTPUT_DIR' directory for any differences."
