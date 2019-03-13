package io.choerodon.devops.api.dto.gitlab;

import javax.validation.constraints.NotNull;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:51 2019/3/12
 * Description:
 */
public class VariableDTO {
    @NotNull
    private String key;
    @NotNull
    private String value;
    @NotNull
    private Boolean protecteds;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getProtecteds() {
        return protecteds;
    }

    public void setProtecteds(Boolean protecteds) {
        this.protecteds = protecteds;
    }
}
