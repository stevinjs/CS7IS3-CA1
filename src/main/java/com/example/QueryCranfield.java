package com.example;

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
        String queriesPath = "./cran.qry";        // Path to your cran.qry file
        String indexDir = "./index";              // Path to your Lucene index
        String outputPath = "results.txt";      // Output file for TREC eval

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
        IndexSearcher searcher = new IndexSearcher(reader);
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        QueryParser parser = new QueryParser("content", analyzer);

        // Read queries
        BufferedReader br = new BufferedReader(new FileReader(queriesPath));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
        String line, queryText = "";
        int queryId = 1;

        while ((line = br.readLine()) != null) {
            if (line.startsWith(".I")) {
                // If we just finished reading a query, process it
                if (!queryText.equals("")) {
                    runQueryAndWriteResults(parser, searcher, queryId, queryText.trim(), writer);
                    queryId++;
                    queryText = "";
                }
            } else if (line.startsWith(".W")) {
                // Start of query content, do nothing
            } else {
                queryText += line + " ";
            }
        }
        // Last query
        if (!queryText.trim().isEmpty()) {
            runQueryAndWriteResults(parser, searcher, queryId, queryText.trim(), writer);
        }

        br.close();
        writer.close();
        reader.close();

        System.out.println("Done searching all queries!");
    }

    private static void runQueryAndWriteResults(QueryParser parser, IndexSearcher searcher,
                                                int queryId, String queryText,
                                                BufferedWriter writer) throws Exception {
        Query query = parser.parse(QueryParser.escape(queryText));
        TopDocs topDocs = searcher.search(query, 50);
        ScoreDoc[] hits = topDocs.scoreDocs;

        for (int rank = 0; rank < hits.length; rank++) {
            int luceneDocId = hits[rank].doc;
            Document doc = searcher.doc(luceneDocId);
            String origDocId = doc.get("docId"); // ID as indexed
            float score = hits[rank].score;
            // TREC format: querynum Q0 docnum rank score STANDARD
            writer.write(String.format("%d Q0 %s %d %.4f STANDARD\n",
                    queryId, origDocId, rank+1, score));
        }
    }
}
