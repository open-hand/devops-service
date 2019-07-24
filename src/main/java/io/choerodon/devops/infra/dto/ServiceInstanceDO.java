package io.choerodon.devops.infra.dto;

/**
 * Created by Zenger on 2018/4/14.
 */
public class ServiceInstanceDO {

    private String id;
    private String code;
    private String instanceStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }
}
