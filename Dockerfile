FROM openjdk:18-jdk-alpine


RUN cd /mnt && mkdir RunJar && cd /mnt/RunJar


# 复制所有 JAR 文件到容器中
COPY shiyan-admin/target/shiyan-admin.jar /mnt/RunJar/shiyan-admin.jar
COPY shiyan-consumption-admin/target/cAdmin.jar /mnt/RunJar/cAdmin.jar
COPY shiyan-consumption-car-activatedate/target/cardActivateDate.jar /mnt/RunJar/cardActivateDate.jar
COPY shiyan-consumption-car-disconnected/target/cardDisconnected.jar /mnt/RunJar/cardDisconnected.jar
COPY shiyan-consumption-car-flow/target/cardFlow.jar /mnt/RunJar/cardFlow.jar
COPY shiyan-consumption-car-status/target/cardStatus.jar /mnt/RunJar/cardStatus.jar
COPY shiyan-consumption-car-stop/target/cardStop.jar /mnt/RunJar/cardStop.jar
COPY shiyan-consumption-order/target/cOrder.jar /mnt/RunJar/cOrder.jar
COPY shiyan-consumption-update/target/cUpdate.jar /mnt/RunJar/cUpdate.jar

# 复制启动脚本到容器中
COPY start-all.sh /mnt/RunJar/start-all.sh

# 赋予脚本可执行权限
RUN chmod +x /mnt/RunJar/start-all.sh

# 使用启动脚本作为 ENTRYPOINT
ENTRYPOINT ["/mnt/RunJar/start-all.sh"]