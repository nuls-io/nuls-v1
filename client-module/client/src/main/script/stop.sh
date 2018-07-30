#!/bin/sh
ps -ef|grep io.nuls.client.Bootstrap |awk '{print $2}'|while read pid
do
   kill -9 $pid
   echo "NULS has stopped."
   break;
done