#!/usr/bin/env bash
mkdir -p choerodon_temp
if [ ! -f choerodon_temp/choerodon-tool-liquibase.jar ]
then
    curl http://nexus.choerodon.com.cn/repository/choerodon-release/io/choerodon/choerodon-tool-liquibase/0.7.0.RELEASE/choerodon-tool-liquibase-0.7.0.RELEASE.jar -L  -o choerodon_temp/choerodon-tool-liquibase.jar
fi
java -Dspring.datasource.url="jdbc:mysql://localhost/devops_service?useUnicode=true&characterEncoding=utf-8&useSSL=false" \
 -Dspring.datasource.username=choerodon \
 -Dspring.datasource.password=choerodon \
 -Ddata.drop=false -Ddata.init=true \
 -Ddata.dir=src/main/resources \
 -jar choerodon_temp/choerodon-tool-liquibase.jar