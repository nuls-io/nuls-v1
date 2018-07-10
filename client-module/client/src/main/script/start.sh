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

export logdir="$SERVER_HOME/logs"

# add conf to classpath
if [ -d "$SERVER_HOME/conf" ]; then
  CLASSPATH=${CLASSPATH}:$SERVER_HOME/conf
fi

# add jar to CLASSPATH
for f in $SERVER_HOME/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done


# add libs to CLASSPATH
for f in $SERVER_HOME/libs/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=$SERVER_HOME/jre
fi
if [ -z "$JAVA_HOME" ]; then
  echo "The JAVA_HOME environment variable is not defined"
  echo "This environment variable is needed to run this program"
  exit 1
fi

# Get standard environment variables
JAVA_OPTS="-server -Xms4096m -Xmx4096m"

MAIN_CLASS=io.nuls.client.Bootstrap

if [ ! -d "$logdir" ]; then
  mkdir "$logdir"
fi
1>${SERVER_HOME}/logs/stdout.log 0>${SERVER_HOME}/logs/stderr.log &

nohup $JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS 1>${SERVER_HOME}/logs/stdout.log 0>${SERVER_HOME}/logs/stderr.log 2>&1 &
