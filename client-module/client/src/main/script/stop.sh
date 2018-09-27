#!/bin/sh

pid=`ps -ef| grep io.nuls.client.Bootstrap |grep -v 'grep' |awk '{print $2}'`

if [ ! -z "$pid" ]; then
    kill -9 $pid
fi