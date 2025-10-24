package com.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;

public class Indexer {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: java Indexer <analyzer> <indexDir> <cran.all.1400> <similarityMode>");
            System.exit(1);
        }
        String analyzerType = args[0];
        String indexDir = args[1];
        String cranPath = args[2];
        String similarityMode = args[3];
        Analyzer analyzer = getAnalyzer(analyzerType);

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // Set similarity based on mode
        switch (similarityMode.toLowerCase()) {
            case "bm25":
                config.setSimilarity(new BM25Similarity());
                break;
            case "classic":
                config.setSimilarity(new ClassicSimilarity());
                break;
            case "boolean":
                config.setSimilarity(new BooleanSimilarity());
                break;
            default:
                config.setSimilarity(new BM25Similarity());
                break;
        }
        
        IndexWriter writer = new IndexWriter(FSDirectory.open(Paths.get(indexDir)), config);

        BufferedReader br = new BufferedReader(new FileReader(cranPath));
        String line, content = "", docId = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith(".I")) {
                if (docId != null) {
                    addDoc(writer, docId, content.trim());
                }
                docId = line.split(" ")[1];
                content = "";
            } else if (line.startsWith(".W") || line.startsWith(".T") || line.startsWith(".A") || line.startsWith(".B"))
                ; // skip tags
            else {
                content += " " + line;
            }
        }
        if (docId != null && !content.isBlank()) {
            addDoc(writer, docId, content.trim());
        }

        br.close();
        writer.close();
        System.out.println("Finished indexing for " + analyzerType);
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

    private static void addDoc(IndexWriter writer, String docId, String content) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("docId", docId, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        writer.addDocument(doc);
    }
}
