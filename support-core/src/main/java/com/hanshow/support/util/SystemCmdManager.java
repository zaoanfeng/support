package com.hanshow.support.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class SystemCmdManager {

	private static short SECOND = 1000;

	/**
	 * 查看服务状态
	 * 
	 * @param serviceName
	 * @return
	 * @throws IOException
	 */
	public boolean status(String serviceName) throws IOException {
		if (System.getProperty("os.name").toLowerCase().startsWith("window")) {
			return statusOfWindows(serviceName);
		} else {
			return statusOfLinux(serviceName);
		}
	}

	/**
	 * 开启服务
	 * 
	 * @param serviceName 服务名
	 * @param timeout     等待时间（秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public boolean start(String serviceName, int timeout) throws IOException, InterruptedException, TimeoutException {
		if (System.getProperty("os.name").toLowerCase().startsWith("window")) {
			return startOfWindows(serviceName, timeout);
		} else {
			return startOfLinux(serviceName, timeout);
		}
	}

	/**
	 * 关闭服务
	 * 
	 * @param serviceName 服务名
	 * @param timeout     等待时间(秒)
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public boolean stop(String serviceName, int timeout) throws IOException, InterruptedException, TimeoutException {
		if (System.getProperty("os.name").toLowerCase().startsWith("window")) {
			return stopOfWindows(serviceName, timeout);
		} else {
			return stopOfLinux(serviceName, timeout);
		}
	}

	/**
	 * 安装服务
	 * 
	 * @param windowsFile windows下安装服务用到的文件
	 * @param linuxFile   linux下安装服务用到的文件
	 * @param serviceName 服务名
	 * @param timeout     服务安装等待最大时间（秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public boolean install(String windowsFile, String linuxFile, String serviceName, int timeout)
			throws IOException, InterruptedException, TimeoutException {
		return install(windowsFile, linuxFile, serviceName, "", timeout);
	}
	
	/**
	 * 安装服务
	 * 
	 * @param windowsFile windows下安装服务用到的文件
	 * @param linuxFile   linux下安装服务用到的文件
	 * @param serviceName 服务名
	 * @param timeout     服务安装等待最大时间（秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public boolean install(String windowsFile, String linuxFile, String serviceName, String displayName, int timeout)
			throws IOException, InterruptedException, TimeoutException {
		if (System.getProperty("os.name").toLowerCase().startsWith("window")) {
			return installOfWindows(windowsFile, serviceName, displayName, timeout);
		} else {
			return installOfLinux(linuxFile, serviceName, timeout);
		}
	}

	/**
	 * 卸载服务
	 * 
	 * @param windowsFile windows下安装服务用到的文件
	 * @param linuxFile   linux下安装服务用到的文件
	 * @param serviceName 服务名
	 * @param timeout     服务安装等待最大时间（秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public boolean uninstall(String windowsFile, String linuxFile, String serviceName, int timeout)
			throws IOException, InterruptedException, TimeoutException {
		if (System.getProperty("os.name").toLowerCase().startsWith("window")) {
			return uninstallOfWindows(windowsFile, serviceName, timeout);
		} else {
			return uninstallOfLinux(linuxFile, serviceName, timeout);
		}
	}

	/**
	 * linux系统下查看服务状态
	 * 
	 * @param serviceName 服务名
	 * @return
	 * @throws IOException
	 */
	private boolean statusOfLinux(String serviceName) throws IOException {
		Process process = Runtime.getRuntime().exec("su");
		String cmd[] = new String[] { "service", "-c", "/sbin/service" + serviceName.trim() + " status" };
		boolean running = true;
		try (DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			int length = cmd.length;
			for (int i = 0; i < length; i++) {
				dataOutputStream.writeBytes(cmd[i] + "\n");
			}
			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush();

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("STATE") != -1) {
					if (line.indexOf("RUNNING") != -1) {
						break;
					} else {
						running = false;
					}
				}
			}
		}
		return running;
	}

	/**
	 * windows下查看服务状态
	 * 
	 * @param serviceName 服务名
	 * @return
	 * @throws IOException
	 */
	private boolean statusOfWindows(String serviceName) throws IOException {
		Process process = Runtime.getRuntime().exec("sc query " + serviceName.trim());
		boolean running = true;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("STATE") != -1) {
					if (line.indexOf("RUNNING") != -1) {
						break;
					} else {
						running = false;
					}
				}
			}
		} catch (IOException e) {
			throw e;
		}
		return running;
	}

	/**
	 * linux下开启服务
	 * 
	 * @param serviceName 服务名
	 * @param timeout     安装服务最大等待时长（秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private boolean startOfLinux(String serviceName, int timeout)
			throws IOException, InterruptedException, TimeoutException {
		return shellOfLinux(new String[] { "service", "-c", "/sbin/service " + serviceName.trim() + " start" },
				timeout);
	}

	/**
	 * windows下启动服务
	 * 
	 * @param serviceName 服务名
	 * @param timeout     启动服务最大等待时间 （秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private boolean startOfWindows(String serviceName, int timeout) throws IOException, InterruptedException, TimeoutException {
		if (!statusOfWindows(serviceName)) {
			Process process = Runtime.getRuntime().exec("net start " + serviceName.trim());
			Worker worker = new Worker(process);
			worker.start();
			try {
				worker.join(timeout * SECOND);
				if (worker.exit != null) {
					int value = worker.exit;
					if (value == 0) {
						return true;
					} else {
						return false;
					}
				} else {
					throw new TimeoutException();
				}
			} catch (InterruptedException ex) {
				worker.interrupt();
				Thread.currentThread().interrupt();
				throw ex;
			} finally {
				process.destroy();
			}
		} else {
			return true;
		}
	}

	/**
	 * linux下关闭服务
	 * 
	 * @param serviceName 服务名
	 * @param timeout     关闭服务最大等待时间（秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private boolean stopOfLinux(String serviceName, int timeout) throws IOException, TimeoutException, InterruptedException {
		return shellOfLinux(new String[] { "service", "-c", "/sbin/service " + serviceName.trim() + " stop" }, timeout);
	}

	/**
	 * windows下关闭服务
	 * 
	 * @param serviceName 服务名
	 * @param timeout     关闭服务最大等待时间 （秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private boolean stopOfWindows(String serviceName, int timeout)
			throws IOException, TimeoutException, InterruptedException {
		Process process = Runtime.getRuntime().exec("net stop " + serviceName.trim());
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout * SECOND);
			if (worker.exit != null) {
				int value = worker.exit;
				if (value == 0) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new TimeoutException();
			}
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	/**
	 * windows下安装服务
	 * 
	 * @param exec        安装服务的命令脚本
	 * @param serviceName 服务名
	 * @param timeout     安装服务最大等待时间 （秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private boolean installOfWindows(String exec, String serviceName, String displayName, int timeout)
			throws IOException, InterruptedException, TimeoutException {
		String path = exec.substring(0, exec.lastIndexOf(File.separator));
		String cmd = exec.substring(exec.lastIndexOf(File.separator) + 1);
		Process process = Runtime.getRuntime().exec("cmd /c " + cmd + " install " + serviceName.trim() + " " + displayName, null, new File(path));
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout * SECOND);
			if (worker.exit != null) {
				int value = worker.exit;
				if (value == 0) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new TimeoutException();
			}
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	/**
	 * linux下安装服务
	 * 
	 * @param exec        安装服务的命令脚本
	 * @param serviceName 服务名
	 * @param timeout     安装服务最大等待时间 （秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private boolean installOfLinux(String exec, String serviceName, int timeout)
			throws IOException, InterruptedException, TimeoutException {
		// linux下拷贝 "sh", "-c", "/bin/cp /data1 /data2"
		String cmd[] = { "sh", "-c", "/bin/cp -f " + exec + " /etc/init.d/" + serviceName.trim(),
				"chkconfig --add " + serviceName.trim(), "chkconfig --level 2345 " + serviceName.trim() + " on" };
		Process process = Runtime.getRuntime().exec(cmd);
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout * SECOND);
			if (worker.exit != null) {
				int value = worker.exit;
				if (value == 0) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new TimeoutException();
			}
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	/**
	 * windows下卸载服务
	 * 
	 * @param exec        卸载服务的命令脚本
	 * @param serviceName 服务名
	 * @param timeout     卸载服务最大等待时间（秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private boolean uninstallOfWindows(String exec, String serviceName, int timeout)
			throws IOException, InterruptedException, TimeoutException {
		String path = exec.substring(0, exec.lastIndexOf(File.separator));
		String cmd = exec.substring(exec.lastIndexOf(File.separator) + 1);
		Process process = Runtime.getRuntime().exec("cmd /c " + cmd + " remove " + serviceName.trim(), null,
				new File(path));
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout * SECOND);
			if (worker.exit != null) {
				int value = worker.exit;
				if (value == 0) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new TimeoutException();
			}
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	/**
	 * linux下卸载服务
	 * 
	 * @param exec        卸载服务的命令脚本
	 * @param serviceName 服务名
	 * @param timeout     卸载服务最大等待时间 （秒）
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private boolean uninstallOfLinux(String exec, String serviceName, int timeout)
			throws IOException, InterruptedException, TimeoutException {
		return shellOfLinux(new String[] { "sh", "-c", "/bin/rm -f /etc/init.d/" + serviceName.trim() }, timeout);
	}

	public boolean shell(String cmd, int timeout) throws IOException, InterruptedException, TimeoutException {
		Process process = null;
		if (System.getProperty("os.name").toLowerCase().startsWith("window")) {
			process = Runtime.getRuntime().exec("cmd /c start " + cmd);
		} else {
			process = Runtime.getRuntime().exec(cmd);
		}
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout * SECOND);
			if (worker.exit != null) {
				int value = worker.exit;
				if (value == 0) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new TimeoutException();
			}
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	/**
	 * 执行shell命令
	 * 
	 * @param cmd
	 * @param timeout
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public boolean shell(String cmd[], int timeout) throws IOException, InterruptedException, TimeoutException {
		Process process = Runtime.getRuntime().exec(cmd);
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout * SECOND);
			if (worker.exit != null) {
				int value = worker.exit;
				if (value == 0) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new TimeoutException();
			}
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	/**
	 * 执行shell命令
	 * 
	 * @param cmd
	 * @param timeout
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public boolean shellOfLinux(String cmd[], int timeout) throws IOException, InterruptedException, TimeoutException {
		Process process = Runtime.getRuntime().exec("su");
		try (DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream())) {
			int length = cmd.length;
			for (int i = 0; i < length; i++) {
				dataOutputStream.writeBytes(cmd[i] + "\n");
			}
			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush();
		}
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout * SECOND);
			if (worker.exit != null) {
				int value = worker.exit;
				if (value == 0) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new TimeoutException();
			}
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	/**
	 * 启用服务超时处理线程
	 * 
	 * @author Administrator
	 *
	 */
	private static class Worker extends Thread {
		private final Process process;
		private Integer exit;

		private Worker(Process process) {
			this.process = process;
		}

		public void run() {
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}
	}
}
