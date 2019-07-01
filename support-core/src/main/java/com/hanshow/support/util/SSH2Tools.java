package com.hanshow.support.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SSH2Tools implements AutoCloseable{
	
	private SSHClient ssh;
	private Session session;
	private String host;
	private int port;
	private String username;
	private String password;
	
	public SSH2Tools() {
		
	}

	/**
	 * 创建SSH链接
	 * @param host 远程ip地址
	 * @param port 远程端口
	 * @param username 远程账号
	 * @param password 远程密码
	 * @return
	 * @throws IOException
	 */
	public SSH2Tools connect(String host, int port, String username, String password) throws IOException {
		
        try {
        	this.host = host;
        	this.port = port;
        	this.username = username;
        	this.password = password;
        	ssh = new SSHClient();
        	ssh.addHostKeyVerifier(new PromiscuousVerifier());
            //ssh.loadKnownHosts();
			ssh.connect(host, port);
			ssh.authPassword(username, password);
		} catch (IOException e) {
			e.printStackTrace();
			if (ssh != null) {
				ssh.close();
			}
			throw e;
		}  
        return this;
	}
	
	public boolean reconnect() throws Exception {
		int retryTime = 5;
		for(int i=0; i<retryTime; i++) {
	        try {
	        	Thread.sleep(60 * 1000);
        		ssh = new SSHClient();
            	ssh.addHostKeyVerifier(new PromiscuousVerifier());
                //ssh.loadKnownHosts();
    			ssh.connect(host, port);
    			ssh.authPassword(username, password);
    			return true;	
			} catch (IOException | InterruptedException e) {
				if (i >= retryTime - 1) {
					e.printStackTrace();
					throw e;
				} 
				if (ssh != null) {
					ssh.close();
				}		
			}  
        
		}
		return false;
	}
	
	public boolean reconnect(String username, String password) throws Exception {
		int retryTime = 5;
		for(int i=0; i<retryTime; i++) {
	        try {
	        	Thread.sleep(60 * 1000);
        		ssh = new SSHClient();
            	ssh.addHostKeyVerifier(new PromiscuousVerifier());
                //ssh.loadKnownHosts();
    			ssh.connect(host, port);
    			ssh.authPassword(username, password);
    			return true;	
			} catch (IOException | InterruptedException e) {
				if (i >= retryTime - 1) {
					e.printStackTrace();
					throw e;
				} 
				if (ssh != null) {
					ssh.close();
				}		
			}  
        
		}
		return false;
	}
	
	public void disconnect() throws IOException {
		if (ssh != null) {
			ssh.disconnect();
			ssh.close();
		}
	}
	
	public String exec(String command) throws IOException {
		try {
            session = ssh.startSession();
            Command cmd = session.exec(command);
            String content = IOUtils.readFully(cmd.getInputStream()).toString();
            cmd.join(5, TimeUnit.SECONDS);
            return content;
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (IOException e) {
                throw e; 
            }
            
        }
	}
	
	public void shell(String command) throws IOException {
		System.out.println(exec(command));
	}
	
	public void scpFileToRemote(String filePath, String remotePath) throws IOException {
		try{
			ssh.newSCPFileTransfer().upload(new FileSystemFile(filePath), remotePath); 
		} catch (IOException e) {
        	e.printStackTrace();
        	throw e;
        }
	}
	
	public void scpFileFromRemote(String filePath, String localPath) throws IOException {
		try{
			ssh.newSCPFileTransfer().download(filePath, localPath);
		} catch (IOException e) {
        	e.printStackTrace();
        	throw e;
        }
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		disconnect();
	}
}
