FROM registry.cn-hangzhou.aliyuncs.com/choerodon-tools/devops-base:0.7.1 as pyinstaller
WORKDIR /home/yaml
COPY values_yaml.py requirements.txt  ./
RUN pyinstaller --noconfirm --clean values_yaml.py
#RUN ls -R /src/
#RUN cat /src/warn*.txt

FROM registry.cn-hangzhou.aliyuncs.com/choerodon-tools/javabase:0.8.0
COPY --from=pyinstaller /home/yaml/dist/values_yaml /usr/lib/yaml
COPY app.jar /devops-service.jar
ENTRYPOINT exec java $JAVA_OPTS $SKYWALKING_OPTS  -jar /devops-service.jar