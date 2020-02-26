FROM registry.cn-shanghai.aliyuncs.com/c7n/devops-base:0.7.5 as base
FROM registry.cn-shanghai.aliyuncs.com/c7n/javabase:0.9.0
# Install kaniko
COPY --from=base /usr/lib/yaml /usr/lib/yaml
COPY --from=base /usr/bin/kaniko /usr/bin/kaniko
COPY app.jar /devops-service.jar
RUN chown -R www-data:www-data /usr/lib/yaml /usr/bin/kaniko /devops-service.jar
USER www-data
CMD java $JAVA_OPTS $SKYWALKING_OPTS -jar /devops-service.jar