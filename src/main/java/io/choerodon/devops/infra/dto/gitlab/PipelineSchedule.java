//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.choerodon.devops.infra.dto.gitlab;

import java.util.Date;
import java.util.List;

public class PipelineSchedule {
    private Integer id;
    private String description;
    private String ref;
    private String cron;
    private String cronTimezone;
    private Date nextRunAt;
    private Date createdAt;
    private Date updatedAt;
    private Boolean active;
    private Pipeline lastPipeline;
    private List<Variable> variables;

    public PipelineSchedule() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRef() {
        return this.ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getCron() {
        return this.cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getCronTimezone() {
        return this.cronTimezone;
    }

    public void setCronTimezone(String cronTimezone) {
        this.cronTimezone = cronTimezone;
    }

    public Date getNextRunAt() {
        return this.nextRunAt;
    }

    public void setNextRunAt(Date nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getActive() {
        return this.active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Pipeline getLastPipeline() {
        return this.lastPipeline;
    }

    public void setLastPipeline(Pipeline lastPipeline) {
        this.lastPipeline = lastPipeline;
    }

    public List<Variable> getVariables() {
        return this.variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

}
