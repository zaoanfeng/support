package com.hanshow.support.server.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class LuceneDaoImpl<T> implements LuceneDao<T> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private T t;
	
	
	@Override
	public void insert(Document doc) throws IOException {
		try(IndexWriter writer = LuceneUtils.getIndexWriter()) {
			writer.addDocument(doc);
			writer.commit();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} 
	}

	@Override
	public void delete(String fieldName, String fieldValue) throws IOException {
		try(IndexWriter writer = LuceneUtils.getIndexWriter()) {
			writer.deleteDocuments(new Term(fieldName, fieldValue));
			writer.commit();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void update(Document doc, String fieldName, String fieldValue) throws IOException {
		try(IndexWriter writer = LuceneUtils.getIndexWriter()) {
			writer.updateDocument(new Term(fieldName, fieldValue), doc);
			writer.commit();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<Document> queryAll(String keyword) throws NotImplementedException {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public List<T> queryForPage(String keywords, String[] fieldNames, int offset, int limit) throws Exception{
		IndexSearcher searcher = LuceneUtils.getIndexSearcher();
		//这里是第二种query方式，不是termQuery
       // QueryParser queryParser = new MultiFieldQueryParser(fieldNames, LuceneUtils.getAnalyzer());
        Query query = new TermQuery(new Term("content", keywords));
		try {
			//Query query = queryParser.parse(keywords);
	        TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
	        logger.debug("total records：" + topDocs.totalHits);
	        //表示返回的结果集
	        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
	        int min = Math.min(scoreDocs.length, (offset * limit) + limit);
	        List<T> list = new ArrayList<>();
	        for (int i = offset; i < min; i++) {
	            logger.debug("matcher rate："+scoreDocs[i].score);
	            //获取查询结果的文档的惟一编号，只有获取惟一编号，才能获取该编号对应的数据
	            int doc = scoreDocs[i].doc;
	            //使用编号，获取真正的数据
	            Document document = searcher.doc(doc);
	            list.add(DocumentUtils.documentToEntity(document, t));
	        }
	        return list;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}     
	}

	@Override
	public int count(String keywords, String[] fieldNames) throws ParseException, IOException {
		IndexSearcher searcher = LuceneUtils.getIndexSearcher();
		//这里是第二种query方式，不是termQuery
        QueryParser queryParser = new MultiFieldQueryParser(fieldNames, LuceneUtils.getAnalyzer());
        try {
        	Query query = queryParser.parse(keywords);
        	return searcher.count(query);
        }catch (ParseException | IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	
}
