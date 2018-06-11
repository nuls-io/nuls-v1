#!/bin/sh
cd ..
rootdir=$PWD
cd bin

export logdir="$rootdir/logs"

LIBS=$rootdir/libs
PUB_LIB=""
MAIN_CLASS=io.nuls.client.Bootstrap

for jar in `find $LIBS -name "*.jar"`
do
     PUB_LIB="$PUB_LIB:""$jar"
done

CONF_PATH=$rootdir/conf

#project class path
export CLASSPATH=$rootdir:$CLASSPATH:$CONF_PATH:$PUB_LIB
if [ ! -d "$logdir" ]; then
  mkdir "$logdir"
fi
1>${rootdir}/logs/stdout.log 0>${rootdir}/logs/stderr.log &

nohup $rootdir/jre/bin/java -Xms1024m -Xmx8192m  -XX:NewSize=256m -XX:MaxNewSize=256m -classpath $CLASSPATH $MAIN_CLASS 1>${rootdir}/logs/stdout.log 0>${rootdir}/logs/stderr.log 2>&1 &
