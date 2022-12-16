package io.choerodon.devops.api.vo.cd;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * @author
 * @since 2022-11-24 16:56:04
 */
public class PipelineAuditCfgVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "流水线id", required = true)
    private Long pipelineId;
    @ApiModelProperty(value = "是否会签 1是会签,0 是或签", required = true)
    private Boolean countersigned;
    @Encrypt
    private List<Long> auditUserIds;

    private List<IamUserDTO> iamUserDTOS;

    public List<IamUserDTO> getIamUserDTOS() {
        return iamUserDTOS;
    }

    public void setIamUserDTOS(List<IamUserDTO> iamUserDTOS) {
        this.iamUserDTOS = iamUserDTOS;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }

    public List<Long> getAuditUserIds() {
        return auditUserIds;
    }

    public void setAuditUserIds(List<Long> auditUserIds) {
        this.auditUserIds = auditUserIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

}
