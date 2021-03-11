package io.choerodon.devops.infra.enums;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/10
 * @Modified By:
 */
public enum DevopsAppTemplateStatusEnum {
    SUCCESS("S"),
    FAILED("F"),
    CREATING("C");

    private String type;

    DevopsAppTemplateStatusEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
