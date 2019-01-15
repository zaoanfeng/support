# MySQL主从备份


## 原理

MySQL数据库数据的备份使用二进制日志文件（binary log file）。主数据库（master）开启二进制文件，则所有操作都会以“事件”的方式记录在二进制日志中，从数据库（slave）通过一个I/O线程与主服务器保持通信，并监控master的二进制日志文件的变化，依照主数据库日志的变化将相应的事件同步到自己的数据库中，来实现从数据库和主数据库的一致性。。


## 准备

1. 两台数据库服务器，两台数据库服务器上的MySQL版本一致

2. 主、从服务器IP地址获取

       主数据库：192.168.1.1
       从数据库：192.168.1.2

## 主数据库操作

1. 找到主数据库的备份文件my.cnf(或者my.ini),Linux系统文件地址为：/etc/my.cnf, Windows系统下C:\\ProgramData\\MySQL\\MySQL Server 5.7\\my.cnf。

2. 在my.cnf的[mysqld]部分插入如下两行

       [mysqld]
       server-id=1  #设置server-id,每个数据库的server-id必须唯一
       log_bin=/var/lib/mysql/mysql-bin.log  #开启二进制日志，MySQL通过些日志文件进行同步

3. 重启MySQL服务，登录MySQL, 创建同步数据使用账号。
     用户：rep, 密码：123456，并指令从库的ip地址为192.168.1.2（可使用%占位，如:192.168.%.%表示192.168段的任何机器上的数据库均可访问）

       CREATE USER 'rep'@'192.168.1.2' IDENTIFIED BY '123456';  

4. 账号赋slave权限

       GRANT REPLICATION SLAVE ON *.* TO 'rep'@'192.168.1.2';

5. 刷新权限

       flush privileges;

6. 查看master状态（执行sql： show master status;），记录二进制文件名(mysql-bin.000005)和位置(1402)

       mysql> show master status;
       +------------------+----------+--------------+------------------+-------------------+
       | File             | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
       +------------------+----------+--------------+------------------+-------------------+
       | mysql-bin.000005 |     1402 |              |                  |                   |
       +------------------+----------+--------------+------------------+-------------------+
       1 row in set (0.00 sec)


## 从数据库操作

1. 找到主数据库的备份文件my.cnf(或者my.ini),Linux系统文件地址为：/etc/my.cnf, Windows系统下C:\\ProgramData\\MySQL\\MySQL Server 5.7\\my.cnf。

2. 在my.cnf的[mysqld]部分插入如下两行

       [mysqld]
       server-id=2  设置server-id,每个数据库的server-id必须唯一

3. 重启MySQL服务，执行同步SQL语句

       CHANGE MASTER TO MASTER_HOST='192.168.1.1', MASTER_USER='rep', MASTER_PASSWORD='123456', MASTER_LOG_FILE='mysql-bin.000005', MASTER_LOG_POS=1402;
   _# MASTER_HOST=主服务器的ip地址_
   _# MASTER_USER=主服务器中创建的同步账号_
   _# MASTER_PASSWORD=主服务器中创建的同步账号的密码_
   _# MASTER_LOG_FILE=主服务器中二进制文件名，show master status;命令中获取得值_
   _# MASTER_LOG_POS=主服务器中二进制文件的读写位置，show master status;命令中获取的值_

4. 开启从库同步服务

       start slave;

5. 查看同步状态，当Slave_IO_Running和Slave_SQL_Running都为YES的时候就表示主从同步设置成功了

       mysql> show slave status\G;
       *************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 192.168.1.1
                  Master_User: rep
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000005
          Read_Master_Log_Pos: 1402
               Relay_Log_File: ubuntu-relay-bin.000002
                Relay_Log_Pos: 632
        Relay_Master_Log_File: mysql-bin.000005
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes

6. 主从同步完成


## 扩展

master开启二进制日志后默认记录所有库所有表的操作，可以通过配置来指定只记录指定的数据库甚至指定的表的操作，具体在mysql配置文件的[mysqld]可添加修改如下选项

    # 不同步哪些数据库  
    binlog-ignore-db = mysql  
    binlog-ignore-db = test  
    binlog-ignore-db = information_schema  
    
    # 只同步哪些数据库，除此之外，其他不同步  
    binlog-do-db = shopweb