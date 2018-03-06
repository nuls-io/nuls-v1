#!/bin/sh
file=$PWD/pid/pid
kill -9 `cat ${file}`
