#!/bin/bash

if [ "$profile_env" == "$PROFILE_ENV_OFFICE" ]; then
    JVM_ARGS="-server"
    JVM_HEAP="-XX:NewRatio=3 -XX:SurvivorRatio=3"
    JVM_SIZE="-Xmx1g -Xms1g -Xmn256m -Dfile.encoding=UTF-8"
else
    JVM_ARGS="-server"
    JVM_HEAP="-XX:NewRatio=4 -XX:SurvivorRatio=4"
    JVM_SIZE="-Xmx1g -Xms1g -Xmn256m -Dfile.encoding=UTF-8"
fi

CUSTOM_JVM_ARGS="$JVM_ARGS $JVM_HEAP $JVM_SIZE -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError -verbose:gc -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=256M"