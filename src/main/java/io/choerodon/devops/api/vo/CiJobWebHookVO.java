package io.choerodon.devops.api.vo;

import java.util.Date;

import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 15:15
 */
public class CiJobWebHookVO {
    private Long id;
    private String stage;
    private String name;
    private String status;
    private String type;
    private String groupType;
    private Date createdAt;
    private Date startedAt;
    private Date finishedAt;

    private Long duration;
    private GitlabWebHookUserVO user;
    private String metadata;

    private CiAuditConfigVO ciAuditConfigVO;


    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public CiAuditConfigVO getCiAuditConfigVO() {
        return ciAuditConfigVO;
    }

    public void setCiAuditConfigVO(CiAuditConfigVO ciAuditConfigVO) {
        this.ciAuditConfigVO = ciAuditConfigVO;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public GitlabWebHookUserVO getUser() {
        return user;
    }

    public void setUser(GitlabWebHookUserVO user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
