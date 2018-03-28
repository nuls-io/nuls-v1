#!/bin/sh
kill -9 `ps -ef| grep io.nuls.Bootstrap |awk '{print $2}'`
