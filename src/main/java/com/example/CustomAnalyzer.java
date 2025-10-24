package com.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.core.StopFilter; // use the core version
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
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
        filter = new NGramTokenFilter(filter, 2, 2, false);
        // Optional: add stemming at the end
        filter = new PorterStemFilter(filter);

        return new TokenStreamComponents(source, filter);
    }
}
