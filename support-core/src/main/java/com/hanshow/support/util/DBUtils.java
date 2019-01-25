package com.hanshow.support.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DBUtils {

	private static final int BATCH_SIZE = 500;
	private static String url;
	private static String username;
	private static String password;
	public static final String MYSQL = "mysql";
	
	
	public DBUtils() {}
	
	public DBUtils(String url, String username, String password) {
		DBUtils.url = url;
		DBUtils.username = username;
		DBUtils.password = password;
	}

	public static void backup(String dbCategory, String url, String username, String password, String backupPath) throws Exception {
		File file = new File(backupPath);
		if (backupPath.endsWith(".sql")) {
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdir();
				}
				file.createNewFile();
			}
		} else {
			file = new File(file, "shopweb.sql");
			file.createNewFile();
		}
		backupMysql(url, username, password, file);
	}
	
	public void backupMysql(String tables[], File backupFile) throws Exception {
		Connection conn = getConnection();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(backupFile))){
			for (String table : tables) {
				ResultSet query = conn.prepareStatement(String.format("SELECT COUNT(*) FROM `%s`;", table)).executeQuery();
				int count = 0;
				if (query.next()) {
					count = query.getInt(1);
				} else {
					return;
				}
				if (count == 0) {
					System.out.println("Table " + table + " is empty!");
					return;
				}
				// 删除表数据
				bw.write(String.format("DELETE FROM `%s`;\n", table));
				for (int fromIndex = 0; fromIndex < count; fromIndex += BATCH_SIZE) {
					ResultSet result = conn.prepareStatement(String.format("SELECT * FROM `%s` LIMIT %s, %s;", table, fromIndex, BATCH_SIZE)).executeQuery();
					int columnCount = result.getMetaData().getColumnCount();
		
					while (result.next()) {
						StringBuffer insertSql = new StringBuffer();
						// 组装 insert into table(col1,col2.....);
						insertSql.append("INSERT INTO ").append("`").append(table).append("`(");
						for (int i = 1; i <= columnCount; i++) {
							insertSql.append('`').append(result.getMetaData().getColumnName(i)).append('`');
							if (i < columnCount) {
								insertSql.append(",");
							}
						}
						insertSql.append(") VALUES (");
						// 组装数据
						for (int i = 1; i <= columnCount; i++) {
							if (result.getObject(i) != null) {
								insertSql.append('\'').append(result.getObject(i).toString()).append('\'').append(",");
							} else {
								insertSql.append("NULL").append(",");
							}
							
						}
						// 写文件
						String out = insertSql.substring(0, insertSql.length() - 1);
						out = out + ");";	
						bw.write(out + "\n");						
					}
				}
				System.out.println("Backup " + table + " data finished!");
			}		
		}catch (IOException | SQLException e) {
			throw e;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	private static void backupMysql(String url, String username, String password, File backupFile) throws Exception  {
		DBUtils.url = url;
		DBUtils.username = username;
		DBUtils.password = password;
		Connection conn = getConnection();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(backupFile))) {
			ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), conn.getSchema(), "%", null);
			while (rs.next()) {
				// 获取表
				if (rs.getString(4).equalsIgnoreCase("TABLE")) {
					ResultSet query = conn.prepareStatement(String.format("SELECT COUNT(*) FROM `%s`;", rs.getString(3))).executeQuery();
					int count = 0;
					if (query.next()) {
						count = query.getInt(1);
					} else {
						continue;
					}
					if (count == 0) {
						System.out.println("Table " + rs.getString(3) + " is empty!");
						continue;
					}
					// 删除表数据
					bw.write(String.format("DELETE FROM `%s`;\n", rs.getString(3)));
					for (int fromIndex = 0; fromIndex < count; fromIndex += BATCH_SIZE) {
						int toIndex = fromIndex + BATCH_SIZE > count ? count : fromIndex + BATCH_SIZE;
						ResultSet result = conn.prepareStatement(String.format("SELECT * FROM `%s` LIMIT %s, %s;", rs.getString(3), fromIndex, toIndex)).executeQuery();
						int columnCount = result.getMetaData().getColumnCount();

						while (result.next()) {
							StringBuffer insertSql = new StringBuffer();
							// 组装 insert into table(col1,col2.....);
							insertSql.append("INSERT INTO ").append("`").append(rs.getString(3)).append("`(");
							for (int i = 1; i <= columnCount; i++) {
								insertSql.append('`').append(result.getMetaData().getColumnName(i)).append('`');
								if (i < columnCount) {
									insertSql.append(",");
								}
							}
							insertSql.append(") VALUES (");
							// 组装数据
							for (int i = 1; i <= columnCount; i++) {
								if (result.getObject(i) != null) {
									insertSql.append('\'').append(result.getObject(i).toString()).append('\'').append(",");
								} else {
									insertSql.append("NULL").append(",");
								}
								
							}
							// 写文件
							String out = insertSql.substring(0, insertSql.length() - 1);
							out = out + ");";	
							bw.write(out + "\n");						
						}
					}
					System.out.println("Backup " + rs.getString(3) + " data finished!");
				}
			}
		} catch (IOException | SQLException e) {
			throw e;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	public Map<String, Object> select(String sql) throws Exception {
		Map<String, Object> result = null;
		Connection conn = getConnection();
		try {
			conn.prepareStatement(sql).executeQuery();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return result;
	}
	
	/**
	 *  更新、删除或插入
	 * @param url
	 * @param username
	 * @param password
	 * @param sqls
	 * @throws Exception
	 */
	public void execute(String[] sqls) throws Exception {
		Connection conn = getConnection();
		try {
			int count = 0;
			conn.setAutoCommit(false);
			for (String sql : sqls) {
				count ++;
				conn.prepareStatement(sql).executeUpdate();
				if (count % 1000 == 0) {
					conn.commit();
				}	
			}
			conn.commit();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	private static Connection getConnection() throws Exception {
		Connection conn = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(url + "&serverTimezone=UTC", username, password);
		} catch (SQLException | ClassNotFoundException e) {
			throw e;
		}
		return conn;
	}

}