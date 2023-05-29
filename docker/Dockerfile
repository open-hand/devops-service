# dockerfile: https://github.com/open-hand/dockerfile/tree/master/ruamel-yaml
FROM registry.cn-shanghai.aliyuncs.com/c7n/ruamel-yaml:0.7.12 as base
FROM registry.cn-shanghai.aliyuncs.com/c7n/javabase:jdk8u282-b08
COPY --from=base --chown=www-data:www-data /usr/lib/yaml /usr/lib/yaml
RUN mkdir /choerodon && chown -R www-data:www-data /choerodon
COPY --chown=www-data:www-data app.jar /choerodon/devops-service.jar
WORKDIR /choerodon
USER 33
CMD java $JAVA_OPTS $SKYWALKING_OPTS -jar /choerodon/devops-service.jar