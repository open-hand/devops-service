package io.choerodon.devops.api.vo.pipeline;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * @author hao.wang@zknow.com
 * @since 2022-11-02 13:49:55
 */
public class CiAuditConfigVO {


    @Encrypt
    private Long id;
    @ApiModelProperty(value = "是否会签 1是会签,0 是或签", required = true)
    private Boolean countersigned;

    @ApiModelProperty(value = "审核人员的集合")
    private List<IamUserDTO> iamUserDTOS;
    @Encrypt
    private List<Long> cdAuditUserIds;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }

    public List<IamUserDTO> getIamUserDTOS() {
        return iamUserDTOS;
    }

    public void setIamUserDTOS(List<IamUserDTO> iamUserDTOS) {
        this.iamUserDTOS = iamUserDTOS;
    }

    public List<Long> getCdAuditUserIds() {
        return cdAuditUserIds;
    }

    public void setCdAuditUserIds(List<Long> cdAuditUserIds) {
        this.cdAuditUserIds = cdAuditUserIds;
    }
}
