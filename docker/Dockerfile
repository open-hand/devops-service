FROM registry.cn-shanghai.aliyuncs.com/c7n/devops-base:0.7.6 as base
FROM registry.cn-shanghai.aliyuncs.com/c7n/javabase:0.9.0
# Install kaniko
COPY --from=base /usr/lib/yaml /usr/lib/yaml
COPY --from=base /usr/bin/kaniko /usr/bin/kaniko
WORKDIR /choerodon
COPY app.jar devops-service.jar
RUN chown -R www-data:www-data /usr/lib/yaml /usr/bin/kaniko /choerodon
USER 33
CMD java $JAVA_OPTS $SKYWALKING_OPTS -jar /choerodon/devops-service.jar