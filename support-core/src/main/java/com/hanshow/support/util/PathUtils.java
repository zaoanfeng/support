package com.hanshow.support.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathUtils {

	private static final char MULTIPLE_CHAR = '*';
	//private static final char SINGLE_CHAR = '?';
	private static Logger logger = LoggerFactory.getLogger(PathUtils.class);
	
	/**
	 * 正常
	 * @param path, 路径中包含 *和？ *代表多个字符， ？代表单个字符
	 * @return
	 */
	public static String[] getFilePaths(String path) {
		List<String> paths = new ArrayList<>();
		//判断路径中是否包含*, 包含返回匹配上的列表，不包含直接返回url
		if (path.indexOf(MULTIPLE_CHAR) != -1) {
			File file = new File(path.substring(0, path.indexOf(MULTIPLE_CHAR)));
			File rootFile = file.getParentFile();
			String tempPath = new File(path).getPath().replaceAll("\\\\", "/");
			
			//获取*前面的路径做为根路径，往下查找，最大查找DEEP级，结束
			filter(path, rootFile, paths, /*getMultistage(file)*/ tempPath.split("/").length);	
			//setSuffix(file, suffix);
		} else {
			paths.add(path);
		}
		return paths.toArray(new String[] {});
	}
	
	/**
	 * 迭代匹配路径是否满足路径中带*的路径
	 * @param matchPath
	 * @param file
	 * @param paths
	 * @param deep
	 */
	private static void filter(String matchPath, File file, List<String> paths, int stage) {
		String path = file.getPath().replaceAll("\\\\", "/");
		if(path.split("/").length >= stage) {
			return;
		}
		/*if (getMultistage(file) >= stage) {
			return;
		}*/
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				if (subFile.isDirectory()) {
					//System.out.println(subFile.getPath());
					if(matching(matchPath, subFile.getPath())) {
						paths.add(subFile.getPath());
					} 
					filter(matchPath, subFile, paths, stage);
				}	
			}
		}
	}
	
	/**
	 * 正则校验路径地址是否匹配
	 * @param matchStr
	 * @param targerStr
	 * @return
	 */
	private static boolean matching(String matchStr,String targerStr){
        matchStr = matchStr.replaceAll("\\*", ".*").replaceAll("\\?", "\\.").replaceAll("\\\\", "/");
        targerStr = targerStr.replaceAll("\\\\", "/");
        try {
            if(Pattern.compile(matchStr).matcher(targerStr).matches()) {
                return true;
            }
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
            return false;
        }
        return false;
    }
	
	@SuppressWarnings("unused")
	private static int getMultistage(File file) {
		PathUtils utils = new PathUtils();
		Stage stage = utils.new Stage();
		stage.setValue(1);
		if (file.getParent() != null && !file.getParent().isEmpty()) {	
			getMultistage(file.getParentFile(), stage);
		}
		return stage.getValue();
	}
	
	private static void getMultistage(File file, Stage stage) {
		//System.out.println(file.getParent());
		if (file.getParent() != null && !file.getParent().isEmpty()) {
			stage.setValue(stage.getValue() + 1);
			getMultistage(file.getParentFile(), stage);
		}
	}
	
	/*private static void setSuffix(File file, String suffix) {
		String temp = file.getPath().substring(file.getPath().indexOf(MULTIPLE_CHAR), file.getPath().length());
		if (temp.indexOf(File.separator) != -1) {
			temp = file.getName();
			setSuffix(file.getParentFile(),temp + suffix);
		} else {
			suffix = "";
		}
	}*/
	
	private class Stage {
		private int value;

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}
	
	public static void main(String[] args) {
		String[] paths = getFilePaths("E:\\Documents\\eslworking_test_upgrade\\eslworking*\\config");
		for (String path : paths) {
			System.out.println(path);
		}
	}
}
