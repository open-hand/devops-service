FROM registry.cn-shanghai.aliyuncs.com/c7n/devops-base:0.7.8 as base
FROM registry.cn-shanghai.aliyuncs.com/c7n/javabase:0.10.0
COPY --from=base --chown=www-data:www-data /usr/lib/yaml /usr/lib/yaml
COPY --chown=www-data:www-data app.jar /choerodon/devops-service.jar
WORKDIR /choerodon
USER 33
CMD java $JAVA_OPTS $SKYWALKING_OPTS -jar /choerodon/devops-service.jar