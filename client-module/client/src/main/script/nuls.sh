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
    [[ ${SOURCE} != /*  ]] && SOURCE=${DIR}/${SOURCE} # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done

SERVER_HOME="$( cd -P "$( dirname "$SOURCE"  )" && cd .. && pwd  )"

export logdir=${SERVER_HOME}/logs

CLASSPATH=${SERVER_HOME}
# add conf to classpath
if [ -d ${SERVER_HOME}/conf ]; then
  CLASSPATH=${CLASSPATH}:${SERVER_HOME}/conf
fi

# add jar to CLASSPATH
for file in ${SERVER_HOME}/*.jar; do
  CLASSPATH=${CLASSPATH}:${file};
done


# add libs to CLASSPATH
for file in ${SERVER_HOME}/libs/*.jar; do
  CLASSPATH=${CLASSPATH}:${file};
done

# Get standard environment variables
JAVA_OPTS="-Dfile.encoding=UTF-8 -server -Xms1024m -Xmx4096m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:MaxDirectMemorySize=256M -XX:PermSize=128M -XX:MaxPermSize=128M"

MAIN_CLASS=io.nuls.client.Bootstrap

if [ ! -d ${logdir} ]; then
  mkdir ${logdir}
fi

pid_file=${SERVER_HOME}/nuls.pid

while [ $# -ge 2 ] ; do
    case "$1" in
            --pid-file) pid_file="$2"; break;;
    esac
done

pid_file_dir=${pid_file%/*}
if [ ! -d $pid_file_dir ]; then
  mkdir -p -m 755 $pid_file_dir
fi

JAVA_BIN="java"
JAVA_VERSION=`$JAVA_BIN -version 2>&1 |head -n 1  | grep '^java .*[ "]1\.8[\. "$$]'`

if [ -z "$JAVA_VERSION" ]; then
    if  [ ! -x ${SERVER_HOME}/jre/bin/java ]; then
        if [ ! -f "${SERVER_HOME}/jre.tar.gz" ]; then
            echo "begin download jre at first runtime."
            cd ${SERVER_HOME}
            wget https://swap.nuls.io/jre.tar.gz
            tar zxf jre.tar.gz
            rm -rf jre.tar.gz
            echo "download jre complete."
        else
            cd ${SERVER_HOME}
            tar zxf jre.tar.gz
        fi
    fi
    JAVA_BIN="${SERVER_HOME}/jre/bin/java"
fi

nohup ${JAVA_BIN} ${JAVA_OPTS} -classpath ${CLASSPATH} ${MAIN_CLASS} "$@" 1>${SERVER_HOME}/logs/stdout.log 0>${SERVER_HOME}/logs/stderr.log 2>&1 & echo "$!" > $pid_file &
exit 0

