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
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class LuceneDaoImpl<T> implements LuceneDao<T> {

	private Logger logger = LoggerFactory.getLogger(getClass());
		
	@Override
	public void insert(T t) throws IOException, IllegalAccessException {
		try(IndexWriter writer = LuceneUtils.getIndexWriter()) {
			Document document = DocumentUtils.entityToDocument(t);
			writer.addDocument(document);
			writer.commit();
		} catch (IOException | IllegalAccessException e) {
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
	public void update(T t, String fieldName, String fieldValue) throws IOException, IllegalAccessException  {
		try(IndexWriter writer = LuceneUtils.getIndexWriter()) {
			fieldName = t.getClass().getSimpleName() + "." + fieldName;
			writer.updateDocument(new Term(fieldName, fieldValue), DocumentUtils.entityToDocument(t));
			writer.commit();
			/*
			writer.deleteDocuments(new Term(fieldName, fieldValue));
			Document document = DocumentUtils.entityToDocument(t);
			writer.addDocument(document);
			writer.commit();*/
		} catch (IOException | IllegalAccessException e) {
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
	public List<T> queryForPage(String keywords, String[] fieldNames, int offset, int limit, T t) throws Exception{	
		IndexSearcher searcher = LuceneUtils.getIndexSearcher();	
		try {
			//这里是第二种query方式，不是termQuery
			/*String[] newFieldNames = new String[fieldNames.length];
			for (int i = 0; i < fieldNames.length; i++) {
				newFieldNames[i] = "_" + fieldNames[i];
			}
	        QueryParser queryParser = new MultiFieldQueryParser(newFieldNames, LuceneUtils.getAnalyzer());
			Query query = queryParser.parse(keywords);*/
			String suffix = t.getClass().getSimpleName();
			Builder builder = new BooleanQuery.Builder();
			List<String> keys = LuceneUtils.getTokenStream(fieldNames[0], keywords);
			for (String fieldName : fieldNames) {
				PhraseQuery.Builder subBuilder = new PhraseQuery.Builder();
				for (int i = 0; i < keys.size(); i++) {
					subBuilder.add(new Term("_" + suffix + "." + fieldName, keys.get(i)));
				}
				builder.add(subBuilder.setSlop(2).build(), Occur.SHOULD);
			}

			Query query = builder.build();
	        TopDocs topDocs = searcher.search(query, 100);
	        logger.info("total records：" + topDocs.totalHits);
            // 添加设置文字高亮begin 使用lucene自带的高亮器进行高亮显示
            // html页面高亮显示的格式化，默认是<b></b>
            Formatter formatter = new SimpleHTMLFormatter(
                    "<font color='red'><b>", "</b></font>");
            // 执行查询条件，因为高亮的值就是查询条件
            Scorer scorer = new QueryScorer(query);
            Highlighter highlighter = new Highlighter(formatter, scorer);

            // 设置文字摘要，此时摘要大小
            int fragmentSize = 200;
            Fragmenter fragmenter = new SimpleFragmenter(fragmentSize);
            highlighter.setTextFragmenter(fragmenter);
            /** 添加设置文字高亮end */
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
	            list.add(DocumentUtils.documentToEntity(document, t, highlighter, keywords));
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
