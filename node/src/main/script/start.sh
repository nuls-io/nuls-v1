#!/bin/sh

rootdir=$PWD
export logdir="$rootdir/logs"

LIBS=$rootdir/libs
PUB_LIB=""
MAIN_CLASS=io.nuls.Bootstrap

for jar in `find $LIBS -name "*.jar"`
do
     PUB_LIB="$PUB_LIB:""$jar"
done

CONF_PATH=$rootdir/conf

#project class path
export CLASSPATH=$CLASSPATH:$CONF_PATH:.:$PUB_LIB
if [ ! -d "$logdir" ]; then
  mkdir "$logdir"
fi
1>${rootdir}/logs/stdout.log 0>${rootdir}/logs/stderr.log &

nohup java -Xms1024m -Xmx4096m  -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=256M -XX:MaxPermSize=256M -classpath $CLASSPATH $MAIN_CLASS 1>${rootdir}/logs/stdout.log 0>${rootdir}/logs/stderr.log 2>&1 &
#nohup java -Xms1024m -Xmx4096m  -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=256M -XX:MaxPermSize=256M -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -classpath $CLASSPATH $MAIN_CLASS 1>${rootdir}/logs/stdout.log 0>${rootdir}/logs/stderr.log 2>&1 &

[ ! -d "${rootdir}/pid" ] && { mkdir "${rootdir}/pid";}
touch ${rootdir}/pid/pid
echo $! > ${rootdir}/pid/pid
