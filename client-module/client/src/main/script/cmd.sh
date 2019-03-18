#!/bin/sh
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

SOURCE="$0"
while [ -h "$SOURCE"  ]; do # resolve $SOURCE until the file is no longer a symlink
    DIR="$( cd -P "$( dirname "$SOURCE"  )" && pwd  )"
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /*  ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
SERVER_HOME="$( cd -P "$( dirname "$SOURCE"  )" && cd .. && pwd  )"

export logdir=${SERVER_HOME}/logs
if [ ! -d ${logdir} ]; then
  mkdir ${logdir}
fi

LIBS=$SERVER_HOME/libs
PUB_LIB=""
MAIN_CLASS=io.nuls.client.cmd.CommandHandler

for jar in `find $LIBS -name "*.jar"`

do
 PUB_LIB="$PUB_LIB:""$jar"
done

# Get standard environment variables
JAVA_OPTS="-Xms128m -Xmx128m"

CONF_PATH=$SERVER_HOME/conf
CLASSPATH=$CLASSPATH:$CONF_PATH:$PUB_LIB:.

if  [ -x ${SERVER_HOME}/jre/bin/java ]; then
  ${SERVER_HOME}/jre/bin/java $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS
  exit 0
fi

JAVA_BIN=`which java`
# try to use JAVA_HOME jre
if [ -x ${JAVA_BIN} ]; then
  ${JAVA_BIN} $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS "$@"
  exit 0
fi

 $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS

echo "The JAVA_HOME environment variable is not defined"
echo "This environment variable is needed to run this program"
exit 1