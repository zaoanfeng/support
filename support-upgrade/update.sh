#!/bin/sh
cd /tmp
tar zxf update.zip
cp check_upgrade_bin.sh /etc/
chmod 700 /etc/check_upgrade_bin.sh
cp openssl /home/elinker/upgrade/
chmod a+x /home/elinker/upgrade/openssl
cp root.war /home/elinker/jetty/webapps/
cp rsa_public.key /home/elinker/etc/
