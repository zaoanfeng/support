package com.hanshow.support.server.lucene;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * 
 * @author anfeng.zhao
 *
 */
public interface LuceneDao<T> {

	/**
	 * 创建
	 * @param doc
	 */
	void insert(Document doc) throws IOException;
	
	/**
	 * 删除基于id
	 * @param fieldName 字段
	 * @param fieldValue 值
	 */
	void delete(String fieldName, String fieldValue) throws IOException;
	
	/**
	 * 更新 fieldName、fieldValue为更新条件
	 * * @param doc
	 * @param fieldName 字段
	 * @param fieldValue 值
	 */
	void update(Document doc, String fieldName, String fieldValue) throws IOException;
	
	/**
	 * 查出所有匹配关键字的内容
	 * @param keyword
	 */
	@Deprecated
	List<Document> queryAll(String keyword);
	
	/**
	 * 查出匹配关键字的内容进行分页
	 * @param keyword 关键字
	 * @param fieldNames 查询的字段列
	 * @param offset 从0开始
	 * @param limit 查几条
	 */
	List<T> queryForPage(String keyword, String[] fieldNames, int offset, int limit) throws Exception;
	
	/**
	 * 满足条件的数量
	 * @param keyword 关键字
	 * @param fieldNames 查询的字段列
	 * @return
	 */
	int count(String keyword, String[] fieldNames) throws ParseException, IOException;
}
