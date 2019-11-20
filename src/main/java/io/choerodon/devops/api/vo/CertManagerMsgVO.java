package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * User: Mr.Wang
 * Date: 2019/11/19
 */
public class CertManagerMsgVO {
    @ApiModelProperty("集群下的环境中是否存在使用CertManager申请或上传的证书")
    private Boolean checkCert;

    public CertManagerMsgVO(){}

    public CertManagerMsgVO(Boolean checkCert) {
        this.checkCert = checkCert;
    }

    public Boolean getCheckCert() {
        return checkCert;
    }

    public void setCheckCert(Boolean checkCert) {
        this.checkCert = checkCert;
    }
}
