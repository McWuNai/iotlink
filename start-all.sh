#!/bin/sh
cd /

# 启动各个服务
nohup java -Xms512m -Xmx512m -jar /mnt/RunJar/shiyan-admin.jar
nohup java -Xms512m -Xmx1024m -jar /mnt/RunJar/cAdmin.jar
nohup java -Xms512m -Xmx1024m -jar /mnt/RunJar/cardActivateDate.jar
nohup java -Xms512m -Xmx1024m -jar /mnt/RunJar/cardDisconnected.jar
nohup java -Xms512m -Xmx1024m -jar /mnt/RunJar/cardFlow.jar
nohup java -Xms512m -Xmx1024m -jar /mnt/RunJar/cardStatus.jar
nohup java -Xms512m -Xmx1024m -jar /mnt/RunJar/cardStop.jar
nohup java -Xms512m -Xmx1024m -jar /mnt/RunJar/cOrder.jar
nohup java -Xms512m -Xmx1024m -jar /mnt/RunJar/cUpdate.jar

# 防止脚本结束导致容器退出
wait