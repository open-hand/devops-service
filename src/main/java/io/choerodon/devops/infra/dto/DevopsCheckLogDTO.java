package io.choerodon.devops.infra.dto;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_check_log")
public class DevopsCheckLogDTO extends AuditDomain {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Date beginCheckDate;
    private Date endCheckDate;
    private String log;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getBeginCheckDate() {
        return beginCheckDate;
    }

    public void setBeginCheckDate(Date beginCheckDate) {
        this.beginCheckDate = beginCheckDate;
    }

    public Date getEndCheckDate() {
        return endCheckDate;
    }

    public void setEndCheckDate(Date endCheckDate) {
        this.endCheckDate = endCheckDate;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
