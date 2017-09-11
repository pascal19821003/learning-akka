#!/bin/bash
basepath=$(cd `dirname $0`; pwd)
echo $basepath
#JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5555"
#JPRO_PARAS="-Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.managent.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

export CLASSPATH=$CLASSPATH:.:$basepath/conf:$basepath/lib/*

java ${JAVA_OPTS} ${JPRO_PARAS} -Xms512m -Xmx1024m com.akkademy.Main
