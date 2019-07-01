package com.hanshow.support.server.lucene;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.highlight.Highlighter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentUtils {

	private static Logger logger = LoggerFactory.getLogger(DocumentUtils.class);
	//private static String CLASS_NAME = "className";

	/**
	 * 实体类转成document
	 * @param entity
	 * @return
	 * @throws IllegalAccessException
	 */
	public static Document entityToDocument(Object entity) throws IllegalAccessException {
		Document doc = new Document();
		Class<? extends Object> clazz = entity.getClass();
		Field[] fields = clazz.getDeclaredFields();
		try {
			String result = "";
			for (Field field : fields) {
				field.setAccessible(true);
				if (field.getName().equals("serialVersionUID")) {
					continue;
				}
				if (field.getGenericType().toString().equals("class java.lang.Long") 
						|| field.getGenericType().toString().equals("long")) {
					doc.add(new StringField(field.getName(), Long.toString((long) field.get(entity)), Store.YES));
					continue;
				}
				if (field.getGenericType().toString().equals("class java.lang.Integer") 
						|| field.getGenericType().toString().equals("int")) {
					doc.add(new StringField(field.getName(), Integer.toString((int) field.get(entity)), Store.YES));
					continue;
				}
				
				if (field.getGenericType().toString().equals("class java.lang.String")) {
					/*if (field.get(entity) != null) {
						if (((String) field.get(entity)).length() < 2) {
							doc.add(new StringField(clazz.getSimpleName() + "." + field.getName(), (String) field.get(entity), Store.YES));
						} else {
							doc.add(new TextField(clazz.getSimpleName() + "." + field.getName(), (String) field.get(entity), Store.YES));
						}
					}*/
					result += (field.get(entity) + "\n");
					continue;
				}
				
			}
			if (!result.equals("")) {
				doc.add(new TextField("content" , result, Store.YES));
			}
			doc.add(new StringField("type", clazz.getSimpleName(), Store.YES));
			return doc;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * document转成实体类
	 * @param document
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public static <T> T documentToEntity(Document document, T t) throws Exception {
		return documentToEntity(document, t, null, null);
	}
	
	/**
	 * document转实体类，并增加关键字的高亮显示
	 * @param document
	 * @param t
	 * @param highlighter
	 * @param keywords
	 * @return
	 * @throws Exception
	 */
	public static <T> T documentToEntity(Document document, T t, Highlighter highlighter, String keywords) throws Exception {
		List<IndexableField> list = document.getFields();
		Class<? extends Object> clazz = t.getClass();
		try {
			for (IndexableField indexableField : list) {
				String fieldName = (indexableField.name());
				Field field = null;
				try {
					field = clazz.getDeclaredField(fieldName);
				} catch (NoSuchFieldException e) {
					logger.debug(fieldName + " field not found!");
				}
				
				if (field != null) {
					field.setAccessible(true);
					String result = document.getField(fieldName).stringValue();
					if (field.getGenericType().toString().equals("class java.lang.String")) {
						if (highlighter != null && keywords != null && !fieldName.equals("type")) {			
							field.set(t, highlighter.getBestFragment(LuceneUtils.getAnalyzer(), fieldName, result));
						} else {
							field.set(t, result);
						}
						continue;
					}
					if (field.getGenericType().toString().equals("class java.lang.Long") 
							|| field.getGenericType().toString().equals("long")) {
						field.set(t, Long.parseLong(result));
						continue;
					}
					if (field.getGenericType().toString().equals("class java.lang.Integer") 
							|| field.getGenericType().toString().equals("int")) {
						field.set(t, Integer.parseInt(result));
						continue;
					}
				}	
			}
			return t;
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
}