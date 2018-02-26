#!/bin/sh

rootdir=$PWD

LIBS=$rootdir/libs
PUB_LIB=""
MAIN_CLASS=io.nuls.client.CommandHandle

for jar in `find $LIBS -name "*.jar"`

do
 PUB_LIB="$PUB_LIB:""$jar"
done

MAIN_LIB=$rootdir/dist_lib/nuls-node.jar

CONF_PATH=$rootdir/conf
CLASSPATH=$CLASSPATH:$CONF_PATH:$MAIN_LIB:.:$PUB_LIB
java -Xms128m -Xmx512m -XX:NewSize=256m -XX:MaxNewSize=256m -classpath $CLASSPATH $MAIN_CLASS