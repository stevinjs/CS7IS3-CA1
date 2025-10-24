#!/bin/bash

# Set these to your actual data file paths
CRAN_DOCS="./resources/cran.all.1400"
CRAN_QRY="./resources/cran.qry"
CRAN_QREL="./resources/qrel"
TREC_EVAL="./trec_eval-9.0.7/trec_eval" # Adjust if trec_eval binary elsewhere

# List analyzers (must match your Java switch statement)
ANALYZERS=(custom)
MODES=(bm25 classic boolean)

for ANALYZER in "${ANALYZERS[@]}"; do
    for MODE in "${MODES[@]}"; do
        echo "==== Testing Analyzer: $ANALYZER with Similarity: $MODE ===="
        INDEX_DIR="./indexes/index_${ANALYZER}_$MODE"
        RESULTS="./results/results_${ANALYZER}_$MODE.txt"
        METRICS="./metrics/metrics_${ANALYZER}_$MODE.txt"

        # (Re-)Index documents using selected analyzer
        mvn exec:java -Dexec.mainClass="com.example.Indexer" -Dexec.args="$ANALYZER $INDEX_DIR $CRAN_DOCS $MODE"
        if [[ $? -ne 0 ]]; then
            echo "Indexing failed for $ANALYZER with $MODE"; exit 1
        fi

        # Run queries using same analyzer (search step)
        mvn exec:java -Dexec.mainClass="com.example.QueryCranfield" -Dexec.args="$ANALYZER $INDEX_DIR $RESULTS $CRAN_QRY $MODE"
        if [[ $? -ne 0 ]]; then
            echo "Query failed for $ANALYZER with $MODE"; exit 1
        fi

        # Evaluate using trec_eval
        $TREC_EVAL $CRAN_QREL "$RESULTS" > "$METRICS"
        if [[ $? -ne 0 ]]; then
            echo "trec_eval failed for $ANALYZER with $MODE"; exit 1
        fi

        # Print summary MAP and P@10 scores
        echo "MAP and P@5 summary for $ANALYZER with $MODE:"
        grep -E '^map|^P_5' "$METRICS"
        echo "--------------------------"
    done
done

echo "All analyzers tested."
