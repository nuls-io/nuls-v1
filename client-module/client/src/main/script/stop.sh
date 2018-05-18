#!/bin/sh
kill -9 `ps -ef| grep io.nuls.client.Bootstrap |awk '{print $2}'`
