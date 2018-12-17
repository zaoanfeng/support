# Support-Client

## 项目介绍

监控磁盘性能、网络丢包统计及服务的运行状态，当磁盘性能慢，网络丢包数据过多及服务停止时（尝试去启动无论 成功与否）会发送邮件进行通知。


## 服务安装、卸载

* ### Windows

      Hanshow-Support.exe install        //安装服务 
      Hanshow-Support.exe uninstall      //卸载服务 
      startup.bat             //启动服务 
      
     _注：windows下安装服务需系统已安装 Microso .NET Framework 4及以上版本_

* ### Linux

  将hanshow-support-client.1.0.0.jar拷贝到/etc/init.d/下并重命令为hanshow-support(服务名自已命名)，命令如下

       sudo cp hanshow-support-client.1.0.0.jar /etc/init.d/hanshow-support 
       sudo chkconfig --add hanshow-support 
       sudo chkconfig --level 2345 hanshow-support on 


## 服务监控

* ### 配置文件说明

       user.store.code=门店编号 
       user.store.name=门店名 
       mail.enable=启用邮件功能(true ? false) 
       mailbox.address=发件人（管理员）邮件 
       mailbox.password=发件人（管理员）密码 
       mail.smtp.host=邮件服务器 
       mail.smtp.port=邮件服务器端口（一般默认为：开启SSL为465，不开启为25）
       mail.smtp.starttls.enable=是否开启SSL（true ? false）
       mail.recipients=收件人，多个以，号分割 
       mail.language=zh_CN（邮件内容的语言） 
       monitor.delay=监控周期,每隔多长时间检测一下服务是否正常，单位秒
       disk.check.enable=是否启用磁盘检查功能(true ? false) 
       disk.check.time=每天监控磁盘及网络的时间点，格式（15:59:00） 
       monitor.service.name=需要监控的服务的名字，多个以英文“,”分隔 
       monitor.eslworking.path=监控磁盘需分析ESL-Working，这里需配置eslworking的安装目录 
       package.size.small=这个值以下认为价签打包数量较小 
       package.size.large=这个值以上认为价签打包数量较在 
       package.size.small.rate=价签包数较小时一个小时的理论打包数量 
       package.size.middle.rate=价于上下两个之间时第一个小时的理论打包数量 
       package.size.large.rate=价签包数较大时一个小时的理论打包数量 
       ap.ack.timeout.rate=ack 超时的价签在一天中的占比，值为整数（2：代表百分之二），超过这个值 预警