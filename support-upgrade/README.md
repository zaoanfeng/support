# Support-Upgrade


## 项目介绍

集服务状态监控、ap批量升级、批量修改ap访问ESL-Working的ip及端口、ESL-Working批量升级等功 能


## 注意事项
windows系统下使用管理员执行脚本命令，linux系统下使用root账号执行命令（仅支持CentOS和Redhat），linux下执行 脚本命令，先查看脚本是否有可执行权限，赋可执行权限命令为:

    chmod +x 文件名.sh 


## ESL-Working快速升级

* ### 配置文件说明
      eslworking.package.path=ESL-Working新版安装包的位置 
      eslworking.old.path=ESL-Working老版本的安装目录 
      eslworking.new.path=ESL-Working新版本的安装目录 
      eslworking.service.name=ESL-Working服务名 
      eslworking.linux.user=linux系统下安装服务使用的用户 
      eslworking.linux.user.group=linux系统下ESL-Working服务使用的用户组 
      shopweb.exclude.folder=升级过程中data文件夹下不需要拷贝到新系统中的文件夹,多个以英文“,”隔 
      shopweb.exclude.config=升级过程中不需要修改的配置项（仅config.properties） 
      
## AP快速升级、配置
* ### 配置文件说明
      ap.upgrade.package=ap升级包的存放目录 
      ap.eslworking.aps.url=通过ESL-Working接口获取所有ap的ip列表（优先） 
      ap.ip.list=代理下所有ap的ip一致时，可通过文本文件读取ip信息 
      ap.eslworking.ip=ap访问eslworking的ip地址（修改eslworking ip地址时使用，升级可不填） 
      ap.eslworking.port=ap访问eslworking的端口（修改eslworking 端口时使用，升级可不填） 