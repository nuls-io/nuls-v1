#!/usr/bin/env sh
# set -e

cd /nuls/bin/

if [ "$1" = 'start' ]
then

    ./start.sh
    tail -f /nuls/logs/stdout.log

elif [ "$1" = 'command' ]
then

    ./cmd.sh

else

    exec "$@"

fi
