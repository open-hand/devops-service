package io.choerodon.devops.infra.dto.iam;


/**
 * Created by younger on 2018/4/3.
 */
public class OrganizationDO {
    private Long id;
    private String name;
    private String code;
    private Long passwordPolicyId;
    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPasswordPolicyId() {
        return passwordPolicyId;
    }

    public void setPasswordPolicyId(Long passwordPolicyId) {
        this.passwordPolicyId = passwordPolicyId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
