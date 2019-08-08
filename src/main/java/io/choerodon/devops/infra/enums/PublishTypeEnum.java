package io.choerodon.devops.infra.enums;

/**
 * 应用发布的类型
 *
 * @author Eugen
 */
public enum PublishTypeEnum {
    /**
     * 仅可部署（默认）
     */
    DEPLOY_ONLY("deploy_only"),
    /**
     * 仅可下载
     */
    DOWNLOAD_ONLY("download_only"),
    /**
     * 可部署，可下载
     */
    ALL("all");

    private  final String value;

    PublishTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }


}
