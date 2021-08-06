package io.choerodon.devops.infra.constant;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/25 15:34
 */
public class DevopsHostConstants {

    private DevopsHostConstants() {
    }

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final int THREE_MINUTE_MILLISECONDS = 3 * 60 * 1000;
    // ws
    public static final String HOST_SESSION = "host-sessions-cache";
    public static final String GROUP = "host:";
    public static final String DOCKER_INSTANCE = "host:%s:docker:instances:%s";
    public static final String JAVA_INSTANCE = "host:%s:java:instances:%s";
    public static final String PIPELINE_CUSTOM_DEPLOY = "pipeline:custom:deploy:hosts:%s:jobs:%s";
    public static final String MIDDLEWARE_INSTANCE = "host:%s:middleware:instances:%s";
    public static final String HOST_COMMANDS = "host:%s:commands";

    public static final String HOST_RESOURCE_INFO_KEY = "devops:host:%s:resourceInfo";
    public static final String HOST_JAVA_PROCESS_INFO_KEY = "devops:host:%s:javaProcess";
    public static final String HOST_DOCKER_PROCESS_INFO_KEY = "devops:host:%s:dockerProcess";


    public static final String ERROR_SAVE_APP_HOST_REL_FAILED = "error.save.app.host.rel.failed";
    public static final String ERROR_SAVE_JAVA_INSTANCE_FAILED = "error.save.java.instance.failed";

    public static final String ERROR_SAVE_DOCKER_INSTANCE_FAILED = "error.save.docker.instance.failed";


}
