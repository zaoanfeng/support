package com.hanshow.support.server.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneUtils {

	private static Analyzer analyzer = new StandardAnalyzer();
	private static final String INDEX_DIR = System.getProperty("user.dir") + File.separator + "data" + File.separator + "index";
	private static Logger logger = LoggerFactory.getLogger(LuceneUtils.class);

	/**
	 * 不设值使用StandarAnalyzer分词器
	 * 
	 * @return
	 */
	public static void setAnalyzer(Analyzer analyzer) {
		if (analyzer != null) {
			LuceneUtils.analyzer = analyzer;
		}
	}

	public static Analyzer getAnalyzer() {
		return LuceneUtils.analyzer;
	}

	/**
	 * 默认使用StandarAnalyzer分词器
	 * 
	 * @return
	 */
	public static IndexWriter getIndexWriter() {
		IndexWriter writer = null;
		try {
			Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			writer = new IndexWriter(dir, iwc);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return writer;
	}

	/**
	 * 默认使用StandarAnalyzer分词器
	 * 
	 * @return
	 */
	public static IndexSearcher getIndexSearcher() {
		IndexSearcher searcher = null;
		try {
			Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
			IndexReader r = DirectoryReader.open(dir);
			searcher = new IndexSearcher(r);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return searcher;
	}
	
	public static void tokenStream(String fieldName, String fieldValues) {		
		try(TokenStream tokenStream = analyzer.tokenStream(fieldName, fieldValues)) {
			tokenStream.reset();
		}catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static List<String> getTokenStream(String fieldName, String fieldValues) {
		List<String> keywords = new ArrayList<>();
		try {
			TokenStream tokenStream = analyzer.tokenStream(fieldName, fieldValues);
			CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
			tokenStream.reset();
			// 遍历分词数据
			while (tokenStream.incrementToken()) {
				keywords.add(term.toString());
			}
			tokenStream.clearAttributes();
			tokenStream.close();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return keywords;
	}
}
