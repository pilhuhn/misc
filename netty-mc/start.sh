#!/bin/sh

if [ ! -e target/misc.netty-mc-0.1.jar ]
then
	mvn install
fi

java -Djava.net.preferIPv4Stack=true -cp target/misc.netty-mc-0.1.jar\
:$HOME/.m2/repository/io/netty/netty-all/4.0.19.Final/netty-all-4.0.19.Final.jar\
:$HOME/.m2/repository/org/slf4j/slf4j-log4j12/1.7.7/slf4j-log4j12-1.7.7.jar\
:$HOME/.m2/repository/org/slf4j/slf4j-api/1.7.7/slf4j-api-1.7.7.jar\
:$HOME/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar\
 de.bsd.nettymc.Main
