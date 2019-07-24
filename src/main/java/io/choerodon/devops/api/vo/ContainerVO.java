package io.choerodon.devops.api.vo;

/**
 * @author zmf
 */
public class ContainerVO {
    private String name;
    private Boolean isReady;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }
}
