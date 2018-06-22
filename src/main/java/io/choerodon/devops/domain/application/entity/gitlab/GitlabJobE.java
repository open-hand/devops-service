package io.choerodon.devops.domain.application.entity.gitlab;

import java.util.Date;

import io.choerodon.devops.infra.common.util.enums.JobStatus;

/**
 * Created by zzy on 2018/1/10.
 */
public class GitlabJobE {

    private Integer id;
    private Date createdAt;
    private Date finishedAt;
    private String name;
    private Date startedAt;
    private Boolean tag;
    private String stage;
    private JobStatus status;

    /**
     * getJobTime from GitlabJobE
     * @param o GitlabJobE
     * @return time diff
     */
    public static Long getJobTime(GitlabJobE o) {
        long diff = 0L;
        if (o.getStartedAt() != null && o.getFinishedAt() != null) {
            long time1 = o.getStartedAt().getTime();
            long time2 = o.getFinishedAt().getTime();
            if (time1 < time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
        }
        return diff;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Boolean getTag() {
        return tag;
    }

    public void setTag(Boolean tag) {
        this.tag = tag;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}
