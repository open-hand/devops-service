package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 通知对象类
 */
public class CertificationNotifyObject {
    @ApiModelProperty("通知对象类型 user/role")
    private String type;
    @ApiModelProperty("id")
    @Encrypt
    private Long id;
    @Encrypt
    private Long certificationId;
    private String realName;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public CertificationNotifyObject() {

    }

    public CertificationNotifyObject(String type, Long id, Long certificationId) {
        this.type = type;
        this.id = id;
        this.certificationId = certificationId;
    }

    public Long getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(Long certificationId) {
        this.certificationId = certificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
