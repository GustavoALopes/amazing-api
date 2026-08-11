#!/usr/bin/env bash

set -uo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/outputs"

mkdir -p "$OUTPUT_DIR"
rm -f -- "$OUTPUT_DIR"/*.log

if command -v mvn >/dev/null 2>&1; then
    MAVEN=(mvn)
elif [[ -f "$PROJECT_DIR/api/mvnw" ]]; then
    MAVEN=(bash "$PROJECT_DIR/api/mvnw")
else
    printf 'Pipeline failed: Maven and api/mvnw were not found.\n' >&2
    exit 127
fi

write_error_artifacts() {
    local step="$1"
    local log_file="$2"
    local artifact_prefix="$OUTPUT_DIR/"
    local clean_log="${log_file}.clean"

    # Remove terminal colour sequences so Maven's [ERROR] marker is stable in
    # artifacts produced by Linux, WSL, Git Bash, and CI runners.
    sed $'s/\033\\[[0-9;]*[[:alpha:]]//g' "$log_file" >"$clean_log"

    # Maven separates its major error blocks with an empty [ERROR] line. Each
    # block becomes an independently numbered artifact and retains stack traces.
    awk -v prefix="$artifact_prefix" -v step="$step" '
        function artifact_name() {
            return sprintf("%s%02d-%s.log", prefix, ++artifact_number, step)
        }

        /^\[ERROR\][[:space:]]*$/ {
            if (capturing && block_has_content) {
                close(output_file)
                capturing = 0
                block_has_content = 0
            }
            next
        }

        /^\[ERROR\]/ {
            if (!capturing) {
                output_file = artifact_name()
                capturing = 1
            }
            print > output_file
            block_has_content = 1
            next
        }

        capturing {
            print > output_file
            block_has_content = 1
        }

        END {
            if (capturing) {
                close(output_file)
            }
        }
    ' "$clean_log"

    if ! compgen -G "$OUTPUT_DIR/??-$step.log" >/dev/null; then
        {
            printf '[ERROR] Maven terminated before producing a structured error block.\n'
            awk '
                /^(Exception in thread|Caused by:|[[:alnum:]_.]+(Exception|Error):)/ {
                    capturing = 1
                }
                capturing
            ' "$clean_log"
        } >"$OUTPUT_DIR/01-$step.log"
    fi

    rm -f -- "$clean_log"
}

run_stage() {
    local step="$1"
    local label="$2"
    shift 2

    local stage_log
    stage_log="$(mktemp "${TMPDIR:-/tmp}/amazing-api-${step}.XXXXXX.log")"

    printf 'Pipeline: %s...\n' "$label"
    if "${MAVEN[@]}" -B -e -Dstyle.color=never -f "$PROJECT_DIR/pom.xml" "$@" >"$stage_log" 2>&1; then
        rm -f -- "$stage_log"
        return 0
    fi

    write_error_artifacts "$step" "$stage_log"
    rm -f -- "$stage_log"
    printf 'Pipeline failed: %s. Error artifacts: %s/??-%s.log\n' \
        "$label" "$OUTPUT_DIR" "$step" >&2
    return 1
}

run_stage UNIT 'unit tests' test || exit $?
run_stage MUT 'mutation tests' test-compile org.pitest:pitest-maven:mutationCoverage || exit $?
run_stage INT 'integration tests' test-compile failsafe:integration-test failsafe:verify || exit $?
run_stage PKG 'module packaging' -Dmaven.test.skip=true package || exit $?

printf 'Pipeline completed successfully.\n'
