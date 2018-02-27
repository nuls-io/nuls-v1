#!/bin/sh

rootdir=$(dirname "$PWD")

LIBS=$rootdir/libs
PUB_LIB=""
MAIN_CLASS=io.nuls.client.CommandHandler

for jar in `find $LIBS -name "*.jar"`
do
     PUB_LIB="$PUB_LIB:""$jar"
done

MAIN_LIB=$rootdir/dist_lib/nuls-node.jar

CONF_PATH=$rootdir/conf

#project class path
export CLASSPATH=$CLASSPATH:$CONF_PATH:$MAIN_LIB:.:$PUB_LIB

1>${rootdir}/logs/script.log 0>${rootdir}/logs/scripterr.log &
nohup java -Xms128m -Xmx512m  -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=256M -XX:MaxPermSize=256M  -classpath $CLASSPATH $MAIN_CLASS 1>${rootdir}/logs/script.log 0>${rootdir}/logs/scripterr.log 2>&1 &

