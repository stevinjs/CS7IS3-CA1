package com.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;

public class QueryCranfield {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: java QueryCranfield <analyzer> <indexDir> <resultsFile> <cran.qry>");
            System.exit(1);
        }
        String analyzerType = args[0];
        String indexDir = args[1];
        String outputPath = args[2];
        String queriesPath = args[3];
        Analyzer analyzer = getAnalyzer(analyzerType);

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new QueryParser("content", analyzer);

        BufferedReader br = new BufferedReader(new FileReader(queriesPath));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
        String line, queryText = "";
        int queryId = 1;

        while ((line = br.readLine()) != null) {
            if (line.startsWith(".I")) {
                if (!queryText.equals("")) {
                    runQueryAndWriteResults(parser, searcher, queryId, queryText.trim(), writer);
                    queryId++;
                    queryText = "";
                }
            } else if (line.startsWith(".W")) {
                // Start of query text
            } else {
                queryText += line + " ";
            }
        }
        if (!queryText.trim().isEmpty()) {
            runQueryAndWriteResults(parser, searcher, queryId, queryText.trim(), writer);
        }
        br.close();
        writer.close();
        reader.close();
        System.out.println("Finished querying for " + analyzerType);
    }

    private static Analyzer getAnalyzer(String type) {
        switch (type.toLowerCase()) {
            case "english": return new EnglishAnalyzer();
            case "standard": return new StandardAnalyzer();
            case "simple": return new SimpleAnalyzer();
            case "whitespace": return new WhitespaceAnalyzer();
            default: return new StandardAnalyzer();
        }
    }

    private static void runQueryAndWriteResults(QueryParser parser, IndexSearcher searcher, int queryId, String queryText, BufferedWriter writer) throws Exception {
        Query query = parser.parse(QueryParser.escape(queryText));
        TopDocs topDocs = searcher.search(query, 50);
        ScoreDoc[] hits = topDocs.scoreDocs;
        for (int rank = 0; rank < hits.length; rank++) {
            int luceneDocId = hits[rank].doc;
            Document doc = searcher.doc(luceneDocId);
            String origDocId = doc.get("docId");
            float score = hits[rank].score;
            writer.write(String.format("%d Q0 %s %d %.4f STANDARD\n", queryId, origDocId, rank + 1, score));
        }
    }
}
