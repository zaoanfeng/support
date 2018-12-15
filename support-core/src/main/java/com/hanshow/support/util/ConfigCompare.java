package com.hanshow.support.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public class ConfigCompare {

	private static final String SUFFIX_PROPERTIES = ".properties";
	private static final String SUFFIX_CONF = ".conf";
	private static final String[] WINDOWS_SERVICE_SCRIPT_KEY = {"set DEFAULT_JVM_OPTS"};
	private static final String[] LINUX_SERVICE_SCRIPT_KEY = { "JAVA_HOME", "APP_HOME", "APP_USER", "JAVA_OPTS"};
	
	/**
	 * 仅支持.properties和.conf文件 
	 * @param srcFile 源文件
	 * @param compareFile 参考对比文件， 与源文件的文件格式一样
	 * @return 返回修改后的结果数据
	 * @throws IOException
	 */
	public static String compare(File srcFile, File compareFile, String[] excludeConfig) throws IOException {
		if (srcFile.getName().lastIndexOf(SUFFIX_CONF) > -1 && compareFile.getName().lastIndexOf(SUFFIX_CONF) > -1) {
			return compareConf(srcFile, compareFile, excludeConfig);
		} else if (srcFile.getName().lastIndexOf(SUFFIX_PROPERTIES) > -1 && compareFile.getName().lastIndexOf(SUFFIX_PROPERTIES) > -1)  {
			return compareProperties(srcFile, compareFile, excludeConfig);
		} else {
			throw new IOException("only support " + SUFFIX_CONF + " and " + SUFFIX_PROPERTIES + " format file");
		}
	}
	
	/**
	 * 比对合并properties文件
	 * @param srcFile 源文件
	 * @param compareFile 参考对比文件
	 * @return
	 * @throws IOException
	 */
	private static String compareProperties(File srcFile, File compareFile, String[] excludeConfig) throws IOException {
		if (!srcFile.exists()) {
			throw new FileNotFoundException(srcFile + " not found!");
		}
		if (!compareFile.exists()) {
			throw new FileNotFoundException(compareFile + " not found!");
		}
		//读配置文件
		Properties newConfig = new Properties();
		Properties oldConfig = new Properties();
		Map<Object, Object> diff = new HashMap<>();
		try (InputStream newConfigIn = new FileInputStream(srcFile);
				InputStream oldConfigIn = new FileInputStream(compareFile);
				BufferedReader reader = new BufferedReader(new FileReader(srcFile));){
			newConfig.load(newConfigIn);
			oldConfig.load(oldConfigIn);
			//取出差异值数据，进行
			for(Object key : newConfig.keySet()) {
				if (!Arrays.asList(excludeConfig).contains(key) && oldConfig.get(key) != null && !newConfig.get(key).equals(oldConfig.get(key))) {
					diff.put(key, oldConfig.get(key));
				}
			}
			//将差异数据写到新的配置文件中
			String content = "";
			String line = "";
			while((line = reader.readLine()) != null) {
				for(Object key : diff.keySet()) {
					if (line.indexOf(key.toString()) != -1) {
						line = (key.toString() + "=" + diff.get(key));
					}
				}
				content += (line + "\n");
			}
			return content;
		} catch (IOException e) {
			throw e;
		}	
	}
	
	/**
	 * 比对合并conf文件
	 * @param srcFile 源文件
	 * @param compareFile 参考对比文件
	 * @return 修改后的字符内容
	 * @throws IOException
	 */
	private static String compareConf(File srcFile, File compareFile, String[] excludeConfig) throws IOException {
		if (!srcFile.exists()) {
			throw new FileNotFoundException(srcFile + " not found!");
		}
		if (!compareFile.exists()) {
			throw new FileNotFoundException(compareFile + " not found!");
		}
		// 读取配置文件，加载到内存
		try (BufferedReader reader = new BufferedReader(new FileReader(srcFile))) {
			Config newServerConf = ConfigFactory.parseFile(srcFile);
			Config oldServerConf = ConfigFactory.parseFile(compareFile);
			Iterator<Entry<String, ConfigValue>> newIter = newServerConf.entrySet().iterator();
			Iterator<Entry<String, ConfigValue>> oldIter = oldServerConf.entrySet().iterator();
			Map<String, ConfigValue> oldMap = new HashMap<>();
			Map<String, ConfigValue> newMap = new HashMap<>();
			//对比两个文件内容，找出不一样的数据
			Map<String, ConfigValue> diff = new HashMap<>();
			while (oldIter.hasNext()) {
				Entry<String, ConfigValue> entry = oldIter.next();
				oldMap.put(entry.getKey(), entry.getValue());
			}
			while (newIter.hasNext()) {
				Entry<String, ConfigValue> entry = newIter.next();
				if (!Arrays.asList(excludeConfig).contains(entry.getKey()) && oldMap.get(entry.getKey()) != null && !entry.getValue().equals(oldMap.get(entry.getKey()))) {
					diff.put(entry.getKey(), oldMap.get(entry.getKey()));
					newMap.put(entry.getKey(), entry.getValue());
				}
			}
			//将diff的key进行树型排序
			List<Tree> treeList = new ArrayList<>();
			for (String key : diff.keySet()) {
				treeList.add(tree(key, diff.get(key)));
			}	
			
			//将差异数据写到新的配置文件中
			String content = "";
			String line = "";
			Tree tempTree = null;
			//读文件，遍例每行
			while((line = reader.readLine()) != null) {
				if (tempTree == null) {
					for(Tree tree : treeList) {
						//当最高节点有子项时，获取次高节点，赋值给临时变量tempTree
						if (tree == null) break;
						if (line.trim().indexOf(tree.getIndex()) != -1) {
							tempTree = tree.getChildTree();
							break;
						}
					}
				} else {
					//匹配判断临时变量，匹配上内容判断tempTree是否使有子项，有则继续循环查找，没有赋值并将tempTree置空值?
					if (line.indexOf(tempTree.getIndex()) != -1) {
						if (tempTree.getChildTree() == null) {
							String first = line.substring(0, line.indexOf("=") + 1);
							//String middle = line.substring(line.indexOf("="), line.indexOf("#"));
							String last = line.substring(line.indexOf("#"));

							line = first + " " + tempTree.getValue().unwrapped() + " " + last;
							tempTree = null;
						} else {
							tempTree = tempTree.getChildTree();
						}	
					}				
				}			
				content += (line + "\n");
			}
			return content;
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * netlink.start_time.hour 拆分成树
	 * @param key
	 * @param value
	 * @return
	 */
	private static Tree tree(String key, ConfigValue value) {
		String strs[] = key.split("\\.");
		Tree tree = null;
		for (int i = strs.length; i > 0; i--) {
			tree = new Tree(strs[i -1], tree);
			if(i - 1 == strs.length - 1) {
				tree.setValue(value);
			}
		}
		return tree;
	}
	
	/**
	 *  修改启动服务脚本的配置， window下修改启动脚本的内存配置, linux下修改eslworking.sh的内存中的JAVA_HOME、APP_HOME、APP_USER
	 * @param srcWindowsFile 新版本的window系统下服务文件
	 * @param compareWindowsFile 老版本的window系统下的服务文件
	 * @param srcLinuxFile 新版本的linux系统下的服务文件
	 * @param compareLinuxFile 老版本的linux系统下的服务文件
	 * @return 修改后的内容字符数据
	 * @throws IOException
	 */
	public static String compareServiceScript(File srcWindowsFile, File compareWindowsFile, File srcLinuxFile, File compareLinuxFile, String oldVersionPath, String newVersionPath) throws IOException {
		if (System.getProperty("os.name").toLowerCase().startsWith("window")) {
			return compareServiceScript(srcWindowsFile, compareWindowsFile, WINDOWS_SERVICE_SCRIPT_KEY, "\n", oldVersionPath, newVersionPath);
		} else {
			return compareServiceScript(srcLinuxFile, compareLinuxFile, LINUX_SERVICE_SCRIPT_KEY, "\n", oldVersionPath, newVersionPath);
		}
	}
	
	/**
	 *  修改启动服务脚本的配置， window下修改启动脚本的内存配置
	 * @param srcWindowsFile 新版本的window系统下服务文件
	 * @param compareWindowsFile 老版本的window系统下的服务文件
	 * @return 修改后的内容字符数据
	 * @throws IOException
	 */
	public static String compareWindowsServiceScript(File srcWindowsFile, File compareWindowsFile, String oldVersionPath, String newVersionPath) throws IOException {
		return compareServiceScript(srcWindowsFile, compareWindowsFile, WINDOWS_SERVICE_SCRIPT_KEY, "\n", oldVersionPath, newVersionPath);
	}
	
	/**
	 *  修改启动服务脚本的配置， linux下修改eslworking.sh的内存中的JAVA_HOME、APP_HOME、APP_USER
	 * @param srcLinuxFile 新版本的linux系统下的服务文件
	 * @param compareLinuxFile 老版本的linux系统下的服务文件
	 * @return 修改后的内容字符数据
	 * @throws IOException
	 */
	public static String compareLinuxServiceScript(File srcLinuxFile, File compareLinuxFile, String oldVersionPath, String newVersionPath) throws IOException {
		return compareServiceScript(srcLinuxFile, compareLinuxFile, LINUX_SERVICE_SCRIPT_KEY, "\n", oldVersionPath, newVersionPath);
	}
	
	/**
	 * windows下修改服务脚本
	 * @param srcFile 新版本的系统下服务文件
	 * @param compareFile 老版本的系统下的服务文件
	 * @param keys 修改项的关键字
	 * @param warp 分隔符 windows(\n\r) linux(\n)
	 * @return 修改后的内容字符数据
	 * @throws IOException
	 */
	private static String compareServiceScript(File srcFile, File compareFile, String[] keys, String warp, String oldVersionPath, String newVersionPath) throws IOException {
		// 接收并修改完成的内容
		String content = "";
		Map<String, String> searchMap = new HashMap<>();
		try(BufferedReader srcReader = new BufferedReader(new FileReader(srcFile));
				BufferedReader compareReader = new BufferedReader(new FileReader(compareFile))) {
			String line = "";
			// 逐行去读，根据要求修改的key进行修改，将查出来的数据放到searchMap
			while((line = compareReader.readLine()) != null) {
				for (String key : keys) {
					if (line.trim().startsWith(key) && searchMap.get(key) == null) {
						searchMap.put(key, line);
					}
				}
			}
			// 对比两个文件，根据查找的key找到两条数据进行对比，不一样用compare的替换srcFile中的数据
			List<String> alreadyKeys = new ArrayList<String>();
			while((line = srcReader.readLine()) != null) {
				for (String key : keys) {
					if (line.trim().startsWith(key) && !alreadyKeys.contains(key)) {
						if (key.equals("set DEFAULT_JVM_OPTS")) {
							String old = searchMap.get(key);
							old = old.substring(old.indexOf("-Xms"), old.length());
							String oldXms = old.substring(0, old.indexOf(";"));
							old = old.substring(old.indexOf("-Xmx"), old.length());
							String oldXmx = old.substring(0, old.indexOf(";"));
							
							String newLine = line;
							newLine = newLine.substring(newLine.indexOf("-Xms"), newLine.length());
							String newXms = newLine.substring(0, newLine.indexOf(";"));
							newLine = newLine.substring(newLine.indexOf("-Xmx"), newLine.length());
							String newXmx = newLine.substring(0, newLine.indexOf(";"));
							
							line = line.replace(newXms, oldXms);
							line = line.replace(newXmx, oldXmx);
						} else if (key.equals("APP_HOME")) {
							line = "APP_HOME=" + newVersionPath;
						} else if (!line.equals(searchMap.get(key))) {
							line = searchMap.get(key);
						}
						alreadyKeys.add(key);
					}
				}
				content += (line + warp);
			}
			return content;
		}catch (IOException e) {
			throw e;
		}
	}

}