package com.hanshow.support.server.lucene;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentUtils {

	private static Logger logger = LoggerFactory.getLogger(DocumentUtils.class);

	public static Document entityToDocument(Object entity) throws IllegalAccessException {
		Document doc = new Document();
		Field[] fields = entity.getClass().getDeclaredFields();
		try {
			for (Field field : fields) {
				field.setAccessible(true);
				if (field.getName().equals("serialVersionUID")) {
					continue;
				}
				if (field.getGenericType().toString().equals("class java.lang.Long") 
						|| field.getGenericType().toString().equals("long")) {
					doc.add(new StringField(field.getName(), Long.toString((long) field.get(entity)), Store.YES));
				}
				if (field.getGenericType().toString().equals("class java.lang.Integer") 
						|| field.getGenericType().toString().equals("int")) {
					doc.add(new StringField(field.getName(), Integer.toString((int) field.get(entity)), Store.YES));
				}
				if (field.getGenericType().toString().equals("class java.lang.String")) {
					if (((String) field.get(entity)).length() < 100) {
						doc.add(new StringField(field.getName(), (String) field.get(entity), Store.YES));
					} else {
						doc.add(new TextField(field.getName(), (String) field.get(entity), Store.YES));
					}
				}
			}
			return doc;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}

	}

	public static <T> T documentToEntity(Document document, T t) throws Exception {
		List<IndexableField> list = document.getFields();
		Class<? extends Object> clazz = t.getClass();
		try {
			for (IndexableField indexableField : list) {
				Field field = clazz.getField(indexableField.name());
				if (field != null) {
					field.setAccessible(true);
					if (field.get(t) instanceof String) {
						field.set(t, document.getField(field.getName()).stringValue());
					}
					if (field.get(t) instanceof Long) {
						field.set(t, Long.parseLong(document.getField(field.getName()).stringValue()));
					}
					if (field.get(t) instanceof Integer) {
						field.set(t, Integer.parseInt(document.getField(field.getName()).stringValue()));
					}
				}	
			}
			return t;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
}