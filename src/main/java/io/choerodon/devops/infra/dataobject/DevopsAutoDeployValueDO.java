package io.choerodon.devops.infra.dataobject;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:54 2019/2/26
 * Description:
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_auto_deploy_value")
public class DevopsAutoDeployValueDO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;
    private String value;

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
