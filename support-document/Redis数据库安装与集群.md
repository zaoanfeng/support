# Redis数据库安装与集群

## Linux下Redis的下载、安装

进入Redis官网，下载tar.gz的软件包

```bash
$ wget http://download.redis.io/releases/redis-5.0.3.tar.gz     //在线下载
$ tar xzf redis-5.0.3.tar.gz      //解压包
$ cd redis-5.0.3     //进入解压后的目录
$ make     //编辑源码
```
## 启动Redis

进入到Redis项目的src目录下，执行以下命令启动服务

```bash
$ ./redis-server
```

## 安装系统服务

进入到Redis项目的utils目录下，执行./install_server.sh，按照提示进行操作
```bash
$ ./install_service.sh

Welcome to the redis service installer
This script will help you easily set up a running redis server
Please select the redis port for this instance: [6379]       //不输入，默认端口为6379
Selecting default: 6379
Please select the redis config file name [/etc/redis/6379.conf]    //服务读取的配置文件及路径
Selected default - /etc/redis/6379.conf
Please select the redis log file name [/var/log/redis_6379.log]    //日志存放文件及路径
Selected default - /var/log/redis_6379.log
Please select the data directory for this instance [/var/lib/redis/6379]    //实例化数据路径
Selected default - /var/lib/redis/6379
Please select the redis executable path []    //redis根目录src下redis-server文件的绝对路径
Selected config:     //以下为确认信息
Port           : 6379
Config file    : /etc/redis/6379.conf
Log file       : /var/log/redis_6379.log
Data dir       : /var/lib/redis/6379
Executable     : /home/an/Downloads/redis-5.0.3/src/redis-server
Cli Executable : /home/an/Downloads/redis-5.0.3/src/redis-cli
Is this ok? Then press ENTER to go on or Ctrl-C to abort.    //回车确认
Copied /tmp/6379.conf => /etc/init.d/redis_6379     //redis_6379为服务名
Installing service...
Successfully added to chkconfig!
Successfully added to runlevels 345!
Starting Redis server...
Installation successful!
```
服务会安装到 /etc/init.d/目录下

## 服务的开启、关闭

```bash
$ sudo service redis_6379 start|stop|restart
```


## Redis集群（Redis-cluster）

本机描述6台Redis服务器(redis集群数量至少要求6台)，端口分别为7001~7006，在Redis目录创建redis-cluster目录，在redis-cluster目录下建6个实例文件夹（最好以端口名文件名，本文以在redis-cluster下创建三个文件夹名字为7001~7006），将Redis根目录的redis.conf文件拷贝到6个实例文件夹中，并修改以下项

```ini
port 7001      //端口7001,7002,7003....
bind 本机ip     //默认ip为127.0.0.1 需要改为其他节点机器可访问的ip 否则创建集群时无法访问对应的端口，无法创建集群
daemonize yes       //redis后台运行
pidfile /var/run/redis_7001.pid          //pidfile文件对应7001,7002,7003....
cluster-enabled yes                           //开启集群  把注释#去掉
cluster-config-file nodes_7001.conf   //集群的配置  配置文件首次启动自动生成 7001,7002,7003....
cluster-node-timeout 15000                //请求超时  默认15秒，可自行设置
appendonly yes                           //aof日志开启  有需要就开启，它会每次写操作都记录一条日志
```

开启每个服务实例，将每个实例文件夹中的redis.conf做参数传入，命令如下

```bash
$ cd src
./redis-server ../redis-cluster/7001/redis.conf
./redis-server ../redis-cluster/7002/redis.conf
./redis-server ../redis-cluster/7003/redis.conf
./redis-server ../redis-cluster/7004/redis.conf
./redis-server ../redis-cluster/7005/redis.conf
./redis-server ../redis-cluster/7006/redis.conf
```

查看服务状态

```bash
$ ps -ef | grep redis
an     4219     1  0 04:35 ?     00:00:00 ./redis-server 192.168.9.86:7001 [cluster]
an     4221     1  0 04:35 ?     00:00:00 ./redis-server 192.168.9.86:7002 [cluster]
an     4223     1  0 04:35 ?     00:00:00 ./redis-server 192.168.9.86:7003 [cluster]
an     4225     1  0 04:35 ?     00:00:00 ./redis-server 192.168.9.86:7004 [cluster]
an     4227     1  0 04:35 ?     00:00:00 ./redis-server 192.168.9.86:7005 [cluster]
an     4229     1  0 04:35 ?     00:00:00 ./redis-server 192.168.9.86:7006 [cluster]
```

进入到Redis下的src目录下，使用redis-cli进行创建集群，--cluster-replicas参数后面跟创建几个slave节点（值为1时，一个master对应一个slave）

```bash
$ ./redis-cli --cluster create 192.168.9.86:7001 192.168.9.86:7002 192.168.9.86:7003 192.168.9.86:7004 192.168.9.86:7005 192.168.9.86:7006 --cluster-replicas 1

>>> Performing hash slots allocation on 6 nodes...
Master[0] -> Slots 0 - 5460
Master[1] -> Slots 5461 - 10922
Master[2] -> Slots 10923 - 16383
Adding replica 192.168.9.86:7004 to 192.168.9.86:7001
Adding replica 192.168.9.86:7005 to 192.168.9.86:7002
Adding replica 192.168.9.86:7006 to 192.168.9.86:7003
>>> Trying to optimize slaves allocation for anti-affinity
[WARNING] Some slaves are in the same host as their master
M: e5d8a249b87eb8f70391febc04f66e5c3ee2e757 192.168.9.86:7001
   slots:[0-5460] (5461 slots) master
M: c6818b5def50815ac7909e8b306c7ba1c42cf2c1 192.168.9.86:7002
   slots:[5461-10922] (5462 slots) master
M: d3ec157de13dbf95b8db691c955a80c50065a7a2 192.168.9.86:7003
   slots:[10923-16383] (5461 slots) master
S: d82c3e690a176b53c997d14d05d66ccb071d4c51 192.168.9.86:7004
   replicates d3ec157de13dbf95b8db691c955a80c50065a7a2
S: 1ab73757fadb8dd02d0e119180e8ada37cc2218c 192.168.9.86:7005
   replicates e5d8a249b87eb8f70391febc04f66e5c3ee2e757
S: ddb379174c9f3ac629fd4ab2728a3c445f509665 192.168.9.86:7006
   replicates c6818b5def50815ac7909e8b306c7ba1c42cf2c1
Can I set the above configuration? (type 'yes' to accept): yes    //输入yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join
....
>>> Performing Cluster Check (using node 192.168.9.86:7001)
M: e5d8a249b87eb8f70391febc04f66e5c3ee2e757 192.168.9.86:7001
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
S: ddb379174c9f3ac629fd4ab2728a3c445f509665 192.168.9.86:7006
   slots: (0 slots) slave
   replicates c6818b5def50815ac7909e8b306c7ba1c42cf2c1
M: d3ec157de13dbf95b8db691c955a80c50065a7a2 192.168.9.86:7003
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
S: 1ab73757fadb8dd02d0e119180e8ada37cc2218c 192.168.9.86:7005
   slots: (0 slots) slave
   replicates e5d8a249b87eb8f70391febc04f66e5c3ee2e757
S: d82c3e690a176b53c997d14d05d66ccb071d4c51 192.168.9.86:7004
   slots: (0 slots) slave
   replicates d3ec157de13dbf95b8db691c955a80c50065a7a2
M: c6818b5def50815ac7909e8b306c7ba1c42cf2c1 192.168.9.86:7002
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
```

至此集群完成。

## FAQ

1. 编译源码示提示以下错误，为系统没有安装gcc、gcc-c++,安装完gcc等重新 make

    /bin/sh: cc: command not found

安装gcc、gcc-c++命令如下

```bash
$ sudo yum -y install gcc gcc-c++
```

2. 编译源码时报以下错误

    cc: error: ../deps/hiredis/libhiredis.a: No such file or directory
    cc: error: ../deps/lua/src/liblua.a: No such file or directory

解决方法：进入到deps目录下，先编译hiredis、jemalloc、linenoise、lua，编译完成后，再退到根目录，重新 make

```bash
$ cd deps
$ make hiredis jemalloc linenoise lua
```
