#!/usr/bin/env bash
MAVEN_LOCAL_REPO=$(cd / && mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout)
TOOL_GROUP_ID=io.choerodon
TOOL_ARTIFACT_ID=choerodon-tool-liquibase
TOOL_VERSION=0.17.2.RELEASE
TOOL_JAR_PATH=${MAVEN_LOCAL_REPO}/${TOOL_GROUP_ID/\./\/}/${TOOL_ARTIFACT_ID}/${TOOL_VERSION}/${TOOL_ARTIFACT_ID}-${TOOL_VERSION}.jar
mvn org.apache.maven.plugins:maven-dependency-plugin:get \
 -Dartifact=${TOOL_GROUP_ID}:${TOOL_ARTIFACT_ID}:${TOOL_VERSION} \
 -Dtransitive=false

java -Dspring.datasource.url="jdbc:mysql://localhost:3306/?serverTimezone=CTT&useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true" \
 -Dspring.datasource.username=root \
 -Dspring.datasource.password=123456 \
 -Dspring.datasource.driver-class-name=com.mysql.jdbc.Driver \
 -Dinstaller.datasources.platform.url="jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true" \
 -Dinstaller.datasources.platform.username=root \
 -Dinstaller.datasources.platform.password=root \
 -Ddata.init=true \
 -Dlogging.level.root=info \
 -Dinstaller.jarPath=target/app.jar \
 -Dinstaller.jarPath.init=true \
 -jar ${TOOL_JAR_PATH}
