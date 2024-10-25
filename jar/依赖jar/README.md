
# 目录说明

> 该jar包为 联通接口调用 需使用 (未封装成自然月联通结算周期为27至次月26号23:59:59) 目前

>在`yunze-apiCommon` 打包遇到缺少 com.cu.api 【联通jar包依赖，开源版本中目前暂未使用到，删除依赖即可】 时 可删除 jar依赖

>或者 添加 `IoTGateway-Sdk_2.7_fat.jar` 至你的maven仓库

>PS: -Dfile= ;后面是跟的你的jar地址 

## 例：

> mvn install:install-file -Dfile="C:\iotlink\jar\依赖jar\IoTGateway-Sdk_2.7_fat.jar" -DgroupId=com.cu -DartifactId=api -Dversion=2 -Dpackaging=jar