package io.choerodon.devops.domain.application.entity;

import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/4/24.
 */
@Component
@Scope("prototype")
public class DevopsEnvResourceE {
    private Long id;
    private ApplicationInstanceE applicationInstanceE;
    private DevopsEnvResourceDetailE devopsEnvResourceDetailE;
    private String kind;
    private String name;
    private Long weight;
    private Long reversion;
    private Date creationDate;
    private Date lastUpdateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Long getReversion() {
        return reversion;
    }

    public void setReversion(Long reversion) {
        this.reversion = reversion;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public ApplicationInstanceE getApplicationInstanceE() {
        return applicationInstanceE;
    }

    public DevopsEnvResourceDetailE getDevopsEnvResourceDetailE() {
        return devopsEnvResourceDetailE;
    }

    public void initApplicationInstanceE(Long id) {
        this.applicationInstanceE = new ApplicationInstanceE(id);
    }

    public void initDevopsInstanceResourceMessageE(Long id) {
        this.devopsEnvResourceDetailE = new DevopsEnvResourceDetailE(id);
    }
}
