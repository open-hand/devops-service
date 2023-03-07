package io.choerodon.devops.infra.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangxiang on 2020/12/15
 */
public enum AppSourceType {
    /**
     * 平台预置
     */
    PLATFORM_PRESET("platformPreset"),
    /**
     * 未知部署来源
     */
    UNKNOWN("unknown"),
    /**
     * 项目制品库(应用中心部署组部署会用上)
     */
    CURRENT_PROJECT("currentProject"),
    /**
     * 应用服务来自本项目 （应用中心chart包部署）
     */
    NORMAL("normal"),
    /**
     * 应用服务来来自市场（应用中心chart包部署、部署组部署会用上）
     */
    MARKET("market"),
    /**
     * 应用服务来自共享（应用中心chart包部署、部署组部署会用上）
     */
    SHARE("share"),
    /**
     * hzero应用（应用中心chart包部署、部署组部署会用上）
     */
    HZERO("hzero"),
    /**
     * 上传（应用中心部署组部署会用上）
     */
    UPLOAD("upload"),

    /**
     * 流水线构建产生的制品
     */
    PIPELINE("pipeline"),

    /**
     * 自定义jar下载地址
     */
    CUSTOM_JAR("custom_jar"),

    /**
     * 自定义来源 （应用中心部署组部署会用上）
     */
    CUSTOM("custom"),

    /**
     * 自定义来源 （应用中心部署组部署会用上）
     */
    DOCKER_COMPOSE("docker_compose"),
    /**
     * 中间件,中间件在devops中的逻辑和市场应用大多都类似 （应用中心chart包部署会用上）
     */
    MIDDLEWARE("middleware"),
    /**
     * 部署组
     */
    DEPLOYMENT("deployment");

    private String value;

    AppSourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static Map<String, AppSourceType> appSourceTypeMap = new HashMap<>();

    static {
        for (AppSourceType sourceType : AppSourceType.values()) {
            appSourceTypeMap.put(sourceType.getValue(), sourceType);
        }
    }

    public static AppSourceType find(String value) {
        return appSourceTypeMap.get(value);
    }
}
