package com.example;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;

import java.util.Arrays;
import java.util.Set;
import org.apache.lucene.analysis.CharArraySet;

public class CustomAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream filter = new LowerCaseFilter(source);
        filter = new EnglishPossessiveFilter(filter);

        // Extend standard stopwords with science-specific noise words
        Set<?> extraStopwords = new CharArraySet(
            Arrays.asList("et", "al", "study", "results", "based", "shown"), true);

        CharArraySet stopSet = CharArraySet.unmodifiableSet(
            CharArraySet.copy(EnglishAnalyzer.getDefaultStopSet()));
        stopSet.addAll(extraStopwords);

        filter = new StopFilter(filter, stopSet);

        filter = new PorterStemFilter(filter);
        filter = new ASCIIFoldingFilter(filter);
        filter = new LengthFilter(filter, 3, Integer.MAX_VALUE); // Only remove <3 chars

        return new TokenStreamComponents(source, filter);
    }
}
