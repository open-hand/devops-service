package io.choerodon.devops.infra.constant;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/25 15:34
 */
public class DevopsHostConstants {

    // ws
    public static final String GROUP = "host:";
    public static final String DOCKER_INSTANCE = "host:%s:docker:instances:%s";
    public static final String JAVA_INSTANCE = "host:%s:java:instances:%s";
    public static final String PIPELINE_CUSTOM_DEPLOY = "pipeline:custom:deploy:hosts:%s:jobs:%s";
    public static final String MIDDLEWARE_INSTANCE = "host:%s:middleware:instances:%s";

    public static String HOST_RESOURCE_INFO_KEY = "devops:host:%s:resourceInfo";
    public static String HOST_JAVA_PROCESS_INFO_KEY = "devops:host:%s:javaProcess";
    public static String HOST_DOCKER_PROCESS_INFO_KEY = "devops:host:%s:dockerProcess";


    public static String ERROR_SAVE_APP_HOST_REL_FAILED = "error.save.app.host.rel.failed";
    public static String ERROR_SAVE_JAVA_INSTANCE_FAILED = "error.save.java.instance.failed";

    public static String ERROR_SAVE_DOCKER_INSTANCE_FAILED = "error.save.docker.instance.failed";


}
