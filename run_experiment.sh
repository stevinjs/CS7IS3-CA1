#!/bin/bash

# Set these to your actual data file paths
CRAN_DOCS="./resources/cran.all.1400"
CRAN_QRY="./resources/cran.qry"
CRAN_QREL="./resources/cranqrel_clean"
TREC_EVAL="./trec_eval-9.0.7/trec_eval" # Adjust if trec_eval binary elsewhere

# List analyzers (must match your Java switch statement)
ANALYZERS=(standard english simple whitespace)

for ANALYZER in "${ANALYZERS[@]}"; do
    echo "==== Testing Analyzer: $ANALYZER ===="
    INDEX_DIR="/home/azureuser/ca1/CS7IS3-CA1/index_$ANALYZER"
    RESULTS="/home/azureuser/ca1/CS7IS3-CA1/results/results_$ANALYZER.txt"
    METRICS="metrics_$ANALYZER.txt"

    # (Re-)Index documents using selected analyzer
    mvn exec:java -Dexec.mainClass="com.example.Indexer" -Dexec.args="$ANALYZER $INDEX_DIR $CRAN_DOCS"
    if [[ $? -ne 0 ]]; then
        echo "Indexing failed for $ANALYZER"; exit 1
    fi

    # Run queries using same analyzer (search step)
    mvn exec:java -Dexec.mainClass="com.example.QueryCranfield" -Dexec.args="$ANALYZER $INDEX_DIR $RESULTS $CRAN_QRY"
    if [[ $? -ne 0 ]]; then
        echo "Query failed for $ANALYZER"; exit 1
    fi

    # Evaluate using trec_eval
    $TREC_EVAL $CRAN_QREL "$RESULTS" > "$METRICS"
    if [[ $? -ne 0 ]]; then
        echo "trec_eval failed for $ANALYZER"; exit 1
    fi

    # Print summary MAP and P@10 scores
    echo "MAP and P@10 summary for $ANALYZER:"
    grep -E '^map|^P_10' "$METRICS"
    echo "--------------------------"
done

echo "All analyzers tested."
