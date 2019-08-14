package io.choerodon.devops.infra.enums;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:33 2019/8/14
 * Description:
 */
public enum UploadErrorEnum {
    /**
     * chart包为空
     */
    CHART_EMPTY("error.chart.empty"),

    /**
     * 下载chart失败
     */
    CHART_DOWNLOAD("error.download.chart"),

    /**
     * 替换镜像地址失败
     */
    PARAM_REPLACE("error.param.replace"),

    /**
     *zip打包失败
     */
    ZIP_REPOSITORY("error.zip.repository"),

    /**
     * push镜像失败
     */
    PUSH_IMAGE("error.exec.push.image"),

    /**
     * 仓库clone失败
     */
    GIT_CLONE("error.git.clone");

    private final String value;

    UploadErrorEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
