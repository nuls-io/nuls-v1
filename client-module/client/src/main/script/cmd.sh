#!/bin/sh

rootdir=$PWD

LIBS=$rootdir/libs
PUB_LIB=""
MAIN_CLASS=io.nuls.client.cmd.CommandHandler

for jar in `find $LIBS -name "*.jar"`

do
 PUB_LIB="$PUB_LIB:""$jar"
done

CONF_PATH=$rootdir/conf
CLASSPATH=$CLASSPATH:$CONF_PATH:$PUB_LIB:.
java -Xms128m -Xmx512m -XX:NewSize=256m -XX:MaxNewSize=256m -classpath $CLASSPATH $MAIN_CLASS