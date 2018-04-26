#!/bin/sh
kill -9 `ps -ef| grep io.nuls.Bootstrap | grep java | awk '{print $2}'`
