#!/usr/bin/env bash
mkdir -p choerodon_temp
if [ ! -f choerodon_temp/choerodon-tool-liquibase.jar ]
then
    curl http://nexus.choerodon.com.cn/repository/choerodon-release/io/choerodon/choerodon-tool-liquibase/0.5.4.RELEASE/choerodon-tool-liquibase-0.5.4.RELEASE.jar -L  -o choerodon_temp/choerodon-tool-liquibase.jar
fi
java -Ddata.drop=true -Ddata.init=true \
 -Dspring.h2.console.enabled=true \
 -Dspring.main.web-environment=true \
 -Ddata.dir=src/main/resources \
 -jar choerodon_temp/choerodon-tool-liquibase.jar
