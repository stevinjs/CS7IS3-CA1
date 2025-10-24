package com.example;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.*;
import org.apache.lucene.analysis.en.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.ngram.*;
import org.apache.lucene.analysis.miscellaneous.*;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.analysis.pattern.*;
import java.util.Arrays;

public class CustomAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();

        TokenStream filter = new LowerCaseFilter(source);
        // Remove numbers using a regex filter (tokens matching digits only)
        filter = new PatternReplaceFilter(filter, java.util.regex.Pattern.compile("\\d+"), "", true);
        // Remove tokens shorter than 3 chars
        filter = new LengthFilter(filter, 3, Integer.MAX_VALUE);
        // Remove stopwords (shorter list: just the very common English ones)
        filter = new StopFilter(filter, StopFilter.makeStopSet(Arrays.asList("the", "and", "of", "in", "on", "for", "to", "with")));
        // Add a bigram filter (generates two-word tokens for phrase matching)
        filter = new NGramTokenFilter(filter, 2, 2);
        // Optional: add stemming at the end
        filter = new PorterStemFilter(filter);

        return new TokenStreamComponents(source, filter);
    }
}
