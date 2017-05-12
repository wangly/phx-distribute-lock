#!/bin/bash
set -e

source ./boot.ini

RUNNABLE_FOLDER_PATH=`pwd`

gclog_path="/opt/logs/gclogs"
mkdir -p $gclog_path

runnable_jar="$IA_APP.jar"
daemontools_log="/opt/logs/daemontools-$IA_APP.log"

java_path="$java_home/bin/java"

echo "using java_home:$java_home"

touch $daemontools_log
ip="$(ifconfig | grep -A 1 'eth0' | tail -1 | cut -d ':' -f 2 | cut -d ' ' -f 1)"

exec $java_path -jar -Dapp.ip=$ip -Dapp.host=$HOSTNAME -Dapp.env.profile=$profile_env $CUSTOM_JVM_ARGS -Xloggc:$gclog_path/$IA_APP.gc.log $RUNNABLE_FOLDER_PATH/$runnable_jar > $daemontools_log 2>&1