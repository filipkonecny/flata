#!/bin/bash

# Script to run old and new program versions on benchmarks and measure execution time

# Directories for old and new program versions
OLD_VERSION_DIR="./flataArchived"
NEW_VERSION_DIR="./dist"

SOLVERS=("MATHSAT5" "SMTINTERPOL" "Z3" "PRINCESS" "CVC4" "YICES2")

# Paths to benchmark folders and their corresponding scripts
declare -A BENCHMARKS_DIRS=(
    ["reach"]="./flataProvidedBenchmarks/benchmarks-reach"
    # ["term"]="./flataProvidedBenchmarks/benchmarks-term"
    ["sil"]="./flataProvidedBenchmarks/benchmarks-sil"
    ["recur"]="./flataProvidedBenchmarks/benchmarks-recur"
)

# Mapping of scripts for different benchmark categories
declare -A SCRIPTS=(
    ["reach"]="flata-reachability.sh"
    # ["term"]="./flata-termination.sh"
    ["sil"]="flata-sil.sh"
    ["recur"]="flata-reachability.sh" # Using the reachability script for recursivity benchmarks
)

# Output directory for timings
TIMINGS_OUTPUT_DIR="./output_timings/"
mkdir -p "$TIMINGS_OUTPUT_DIR"

# Function to parse the running time from the output file
parse_running_time() {
    local output_file="$1"
    local running_time=$(grep -Eo 'running time(\s\(total\))?: [0-9]+\.[0-9]+s' "$output_file" | tail -1 | grep -Eo '[0-9]+\.[0-9]+')
    if [ -z "$running_time" ]; then
        echo "\"T/O\""
    else
        LC_NUMERIC=C printf "%.2f" "$running_time"
    fi
}

# Make sure the chosen solver is actually used in the output
# looking for: Using solver: YICES2
parse_chosen_solver() {
    local output_file="$1"
    local solver="$2"
    local chosen_solver=$(grep -Eo "Using solver: $solver" "$output_file")
    if [ -z "$chosen_solver" ]; then
        echo "Warning: The chosen solver $solver was not used in the output."
        #print out the output file for debugging
        cat "$output_file"
    fi
}

# Function to execute a version of the program with timeout and timing
run_program() {
    local program_dir="$1"
    local script_name="$2"
    local benchmark="$3"
    local output_file="$4"
    local timeout_duration="$5"
    local time_file="$6"
    local solver="$7"
    local absolute_path=$(realpath "$benchmark")  # Convert to absolute path

    # print the command that will be executed, for debugging
    # echo "timeout $timeout_duration $program_dir/$script_name $absolute_path -solver $solver > $output_file"

    # Temporarily move to program directory to respect relative classpath settings
    pushd "$program_dir" > /dev/null

    # Time and execute the script with the absolute path to the benchmark using timeout
    /usr/bin/time -f "%e" -o "$time_file" timeout "$timeout_duration" "./$script_name" "$absolute_path" "$solver" > "$output_file"
    local status=$?

    # Return to the original directory
    popd > /dev/null

    # Check if the chosen solver is actually used in the output
    parse_chosen_solver "$new_output" "$solver"

    # Check if the process was killed by the timeout
    if [ $status -eq 124 ]; then
        # if solver is not provided, it means the script is the old version, set solver to yices1
        if [ -z "$solver" ]; then
            solver="yices1"
        fi
        echo "Timeout: Processing of $benchmark with solver $solver exceeded $timeout_duration seconds."
        echo "\"T/O\"" > "$time_file"  # Indicating a timeout as maximum time
        echo "\"T/O\"" > "$output_file"  # Indicating a timeout as maximum time
    fi
}

# Process each category and benchmark
for category in "${!BENCHMARKS_DIRS[@]}"; do
    echo "Processing category: $category"
    BENCHMARKS_DIR="${BENCHMARKS_DIRS[$category]}"
    SCRIPT_NAME="${SCRIPTS[$category]}"
    TIMEOUT_DURATION=60

    # Get the total number of benchmark files
    total_files=$(find "$BENCHMARKS_DIR" -type f -name "*.nts" | wc -l)
    current_file=0

    # JSON file to store timings
    json_output_file="${TIMINGS_OUTPUT_DIR}${category}_timings.json"
    echo "[" > "$json_output_file"

    # Process each benchmark file
    find "$BENCHMARKS_DIR" -type f -name "*.nts" | while read benchmark; do
        current_file=$((current_file + 1))
        echo "Running $category benchmark $(basename "$benchmark") ($current_file of $total_files)..."

        # Prepare temporary files for output and timing
        old_output=$(mktemp)
        old_time=$(mktemp)

        # Execute old version of the script with timeout and timing
        run_program "$OLD_VERSION_DIR" "$SCRIPT_NAME" "$benchmark" "$old_output" "$TIMEOUT_DURATION" "$old_time" ""

        # Read timing for old version (total time and own measured time)
        old_total_duration=$(cat "$old_time")
        old_measured_duration=$(parse_running_time "$old_output")
        if [ "$old_total_duration" != "\"T/O\"" ]; then
            old_total_duration=$(LC_NUMERIC=C printf "%.2f" "$old_total_duration")
        fi

        # Initialize an array to hold new durations for each solver
        new_durations=()

        # Execute new versions of the script with each solver
        for solver in "${SOLVERS[@]}"; do
            new_output=$(mktemp)
            new_time=$(mktemp)
            run_program "$NEW_VERSION_DIR" "$SCRIPT_NAME" "$benchmark" "$new_output" "$TIMEOUT_DURATION" "$new_time" "$solver"

            # Read timing for new version (total time and own measured time)
            new_total_duration=$(cat "$new_time")
            new_measured_duration=$(parse_running_time "$new_output")
            if [ "$new_total_duration" != "\"T/O\"" ]; then
                new_total_duration=$(LC_NUMERIC=C printf "%.2f" "$new_total_duration")
            fi
            new_durations+=(",\"${solver}_total\": $new_total_duration, \"${solver}_measured\": $new_measured_duration")

            # Clean up temporary files
            rm "$new_output" "$new_time"
        done

        # Append results to JSON
        echo "{\"benchmark\": \"$(basename "$benchmark")\", \"old_total_duration\": $old_total_duration, \"old_measured_duration\": $old_measured_duration ${new_durations[@]}}," >> "$json_output_file"

        # Clean up temporary files
        rm "$old_output" "$old_time"
    done

    # Properly close the JSON array
    sed -i '$ s/,$//' "$json_output_file"  # Remove the last comma
    echo "]" >> "$json_output_file"

    echo "All $category benchmarks processed ($total_files files)."
done

echo "All categories have been processed. Check the '$TIMINGS_OUTPUT_DIR' directory for the timing results."
