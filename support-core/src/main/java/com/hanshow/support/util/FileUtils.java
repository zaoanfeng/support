package com.hanshow.support.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.alibaba.fastjson.JSON;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

public class FileUtils {

	private static final String ZIP_SUFFIX = ".zip";

	/**
	 * 解压文件
	 * 
	 * @param srcFile zip文件
	 * @param path    解压目录
	 * @throws IOException
	 */
	public static String unzip(File srcFile, String path) throws IOException {
		String folderName = null;
		if (!srcFile.exists()) {
			throw new FileNotFoundException(srcFile.getPath() + "isn't found");
		}
		if (!srcFile.getName().toLowerCase().endsWith(ZIP_SUFFIX)) {
			throw new IOException("only support '.zip' file");
		}
		try (ZipFile zipFile = new ZipFile(srcFile, getFileEncode(srcFile))) {
			Enumeration<?> entries = zipFile.entries();
			folderName = ((ZipEntry) entries.nextElement()).getName();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				// 如果是文件夹，就创建文件夹
				if (entry.isDirectory()) {

					String dirPath = path + File.separator + entry.getName();
					File dir = new File(dirPath);
					dir.mkdirs();
				} else {
					// 如果是文件，就先创建这个文件，然后用io流把内容copy过去
					File targetFile = new File(path + File.separator + entry.getName());
					// 保证这个文件的父文件夹必须要存在
					if (!targetFile.getParentFile().exists()) {
						targetFile.getParentFile().mkdirs();
					}
					targetFile.createNewFile();
					// 将压缩文件内容写入到这个文件
					try (InputStream is = zipFile.getInputStream(entry);
							FileOutputStream fos = new FileOutputStream(targetFile);) {
						int len;
						byte[] buf = new byte[1024];
						while ((len = is.read(buf)) != -1) {
							fos.write(buf, 0, len);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("unzip error from ZipUtils", e);
		}
		return folderName;
	}
	
	/**
	 * 拷贝文件夹
	 * 
	 * @param srcPath
	 * @param desPath
	 * @throws IOException
	 */
	public static void copyFolder(String srcPath, String desPath) throws IOException {
		File srcDir = new File(srcPath);
		File desDir = new File(desPath);
		copyFile(srcDir, desDir, new String[] {});
	}

	/**
	 * 拷贝文件夹
	 * 
	 * @param srcPath
	 * @param desPath
	 * @throws IOException
	 */
	public static void copyFolder(String srcPath, String desPath, String[] excludeFolder) throws IOException {
		File srcDir = new File(srcPath);
		File desDir = new File(desPath);
		copyFile(srcDir, desDir, excludeFolder);
	}
	
	/**
	 * 递归拷贝文件
	 * 
	 * @param srcFile
	 * @param desFile
	 * @throws IOException
	 */
	public static void copyFile(File srcFile, File desFile) throws IOException {
		copyFile(srcFile, desFile, new String[] {});
	}

	/**
	 * 递归拷贝文件
	 * 
	 * @param srcFile
	 * @param desFile
	 * @throws IOException
	 */
	public static void copyFile(File srcFile, File desFile, String[] excludeFolder) throws IOException {
		if (!srcFile.exists()) {
			if (!srcFile.exists()) {
				throw new FileNotFoundException(srcFile.getPath() + "isn't found");
			}
		}
		if (Arrays.asList(excludeFolder).contains(srcFile.getName())){
		    System.out.println("ingore copy folder/file : " + srcFile.getAbsolutePath()); 
		    return;
		}
		// 如果源文件是目录
		if (srcFile.isDirectory()) {
			// 目录不存在，创建
			if (!desFile.exists()) {
				desFile.mkdir();
			}
			for (File file : srcFile.listFiles()) {
				File newFile = new File(desFile, file.getName());
				System.out.println("copy === " + file.getAbsolutePath() + " --> " + newFile.getAbsolutePath());
				copyFile(file, newFile, excludeFolder);
			}
		} else {
			// 文件不存在，创建
			if (!desFile.exists()) {
				desFile.createNewFile();
			}

			// 将压缩文件内容写入到这个文件中
			try (InputStream is = new FileInputStream(srcFile); FileOutputStream fos = new FileOutputStream(desFile);) {
				int len;
				byte[] buf = new byte[1024];
				while ((len = is.read(buf)) != -1) {
					fos.write(buf, 0, len);
				}
			}
		}
	}

	/**
	 * 删除目录
	 * @param dir
	 */
	public static void deleteFolder(File dir) {
		if (dir.exists()) {
			if (dir.isDirectory()) {
				for (File f : dir.listFiles()) {
					deleteFolder(f);
				}
				dir.delete();
			} else {
				dir.delete();
			}
		}
	}

	/**
	 * properties文件转成json
	 * @param propertiesFile
	 * @param jsonFile
	 * @throws IOException
	 */
	public static void propertiesToJson(File propertiesFile, File jsonFile) throws IOException {
		Properties properties = new Properties();
		List<Map<String, Object>> list = new ArrayList<>();
		try (InputStream inputStream = new FileInputStream(propertiesFile);
				BufferedWriter bw = new BufferedWriter(new FileWriter(jsonFile));) {
				properties.load(inputStream);
				for (Object key : properties.keySet()) {
					Map<String, Object> map = new HashMap<>();
					map.put("field", key.toString());
					if (properties.getProperty(key.toString()).equals("2")) {
						map.put("delay", Boolean.valueOf(true));
					} else {
						map.put("delay", Boolean.valueOf(false));
					}
					list.add(map);
				}
				if (!jsonFile.exists()) {
					jsonFile.createNewFile();
				}
				bw.write(JSONFormat.formatJson(JSON.toJSONString(list)));
		} catch (IOException e) {
			throw e;
		}
	}

	public static Charset getFileEncode(File file) throws MalformedURLException, IOException {
		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();

		detector.add(new ParsingDetector(false));
		detector.add(UnicodeDetector.getInstance());
		detector.add(JChardetFacade.getInstance());
		detector.add(ASCIIDetector.getInstance());

		Charset charset = detector.detectCodepage(file.toURI().toURL());
		if (charset != null) {
			return charset;
		}
		return Charset.forName("UTF-8");
	}
}
