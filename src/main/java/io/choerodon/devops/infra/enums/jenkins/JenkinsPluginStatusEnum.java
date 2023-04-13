package io.choerodon.devops.infra.enums.jenkins;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/15 16:04
 */
public enum JenkinsPluginStatusEnum {

    /**
     * 未安装
     */
    UNINSTALL("uninstall"),
    /**
     * 已安装
     */
    INSTALLED("installed"),
    /**
     * 可升级
     */
    UPGRADEABLE("upgradeable"),
    /**
     * 已停用
     */
    DISABLED("disabled");


    private final String value;

    JenkinsPluginStatusEnum(String value) {
        this.value = value;
    }


    public String value() {
        return value;
    }


}
