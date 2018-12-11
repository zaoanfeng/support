#!/bin/sh
#touch test.txt
file_path="/home/elinker/upgrade_file"

if [ -d ${file_path} ];then
	rm -rf ${file_path}/*
else
	mkdir ${file_path}
fi

ret=`tar -xvf $1 -C ${file_path}`
if [ "$?" != "0" ];then
	rm -rf ${file_path}
	exit 1
fi

chmod +x ${file_path}/check.sh
ret=`$file_path/check.sh $2`
if [ "$?" != "0" ];then
	rm -rf ${file_path}
	exit 2
fi

killall elinker
chmod +x ${file_path}/upgrade.sh
ret=`$file_path/upgrade.sh`
if [ "$?" != "0" ];then
	rm -rf ${file_path}
	exit 3
fi

rm -rf ${file_path}
exit 0
