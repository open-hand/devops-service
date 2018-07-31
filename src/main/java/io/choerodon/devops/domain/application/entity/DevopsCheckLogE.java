package io.choerodon.devops.domain.application.entity;

import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DevopsCheckLogE {
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
