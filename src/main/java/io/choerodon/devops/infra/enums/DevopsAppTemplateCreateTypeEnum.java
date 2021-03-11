package io.choerodon.devops.infra.enums;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/11
 * @Modified By:
 */
public enum DevopsAppTemplateCreateTypeEnum {
    TEMPLATE("template"),
    GITLAB("gitlab"),
    GITHUB("github");

    private String type;

    DevopsAppTemplateCreateTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
