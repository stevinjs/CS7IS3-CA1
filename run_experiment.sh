#!/bin/bash
# List analyzers to test
ANALYZERS=(standard english simple whitespace)

for ANALYZER in "${ANALYZERS[@]}"; do
    echo "==== Testing $ANALYZER ===="
    INDEX_DIR="index_$ANALYZER"
    RESULTS="results_$ANALYZER.txt"
    METRICS="metrics_$ANALYZER.txt"
    
    # 1. Indexing
    mvn exec:java -Dexec.mainClass="com.example.Indexer" -Dexec.args="$ANALYZER $INDEX_DIR"

    # 2. Querying
    mvn exec:java -Dexec.mainClass="com.example.QueryCranfield" -Dexec.args="$ANALYZER $INDEX_DIR $RESULTS"

    # 3. Evaluation
    ./trec_eval cranqrel_clean "$RESULTS" > "$METRICS"

    # 4. Display summary
    grep "map\|P@5" "$METRICS"
    echo "=============================="
done
