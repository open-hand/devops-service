package io.choerodon.devops.infra.dataobject;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:00 2019/4/8
 * Description:
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_pipeline_app_deploy_value")
public class PipelineAppDeployValueDO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;
    private String value;
    private Long valueId;

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
