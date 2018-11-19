#!/bin/sh
scriptdir=$(cd `dirname $0`; pwd)
homedir=`dirname $scriptdir`
pid=`ps -ef| grep $homedir |grep -v 'grep' |awk '{print $2}'`
if [ ! -z "$pid" ]; then
    kill -9 $pid
fi