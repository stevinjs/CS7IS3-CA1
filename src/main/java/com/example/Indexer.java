import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

import java.io.*;
import java.nio.file.Paths;

public class Indexer {
    public static void main(String[] args) throws Exception {
        String cranFilePath = "./cran.all.1400"; // path to dataset
        String indexDir = "index";             // output index directory

        Directory dir = FSDirectory.open(Paths.get(indexDir));
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir, iwc);

        BufferedReader br = new BufferedReader(new FileReader(cranFilePath));
        String line, docId = "", title = "", text = "";
        boolean inTitle = false, inText = false;

        while ((line = br.readLine()) != null) {
            if (line.startsWith(".I")) {
                if (!docId.equals("")) {
                    // Index previous document
                    Document doc = new Document();
                    doc.add(new StringField("docId", docId, org.apache.lucene.document.Field.Store.YES));
                    doc.add(new TextField("content", title + " " + text, org.apache.lucene.document.Field.Store.YES));
                    writer.addDocument(doc);
                    title = "";
                    text = "";
                }
                docId = line.substring(3).trim();
                inTitle = false;
                inText = false;
            } else if (line.startsWith(".T")) {
                inTitle = true;
                inText = false;
            } else if (line.startsWith(".A") || line.startsWith(".B")) {
                inTitle = false;
            } else if (line.startsWith(".W")) {
                inTitle = false;
                inText = true;
            } else {
                if (inTitle) {
                    title += line + " ";
                }
                if (inText) {
                    text += line + " ";
                }
            }
        }
        // Last doc
        Document doc = new Document();
        doc.add(new StringField("docId", docId, org.apache.lucene.document.Field.Store.YES));
        doc.add(new TextField("content", title + " " + text, org.apache.lucene.document.Field.Store.YES));
        writer.addDocument(doc);

        br.close();
        writer.close();
        System.out.println("Indexing complete.");
    }
}
