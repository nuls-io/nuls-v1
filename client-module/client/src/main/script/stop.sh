#!/bin/sh
ps -ef|grep io.nuls.client.Bootstrap |grep -v grep |awk '{print $2}'|while read pid
do
   kill -9 $pid
   echo "NULS has stopped."
   break;
done