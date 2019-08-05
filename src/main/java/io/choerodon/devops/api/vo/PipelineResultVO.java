package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;


public class PipelineResultVO {

    private Long id;
    private String status;
    private String createUser;
    private String commit;
    private String ref;
    private String commitUser;
    private List<GitlabJobVO> jobs;
    private Date createdAt;
    private Long[] time;
    private String title;
    private String appServiceName;
    private String appServiceCode;

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getCommitUser() {
        return commitUser;
    }

    public void setCommitUser(String commitUser) {
        this.commitUser = commitUser;
    }

    public List<GitlabJobVO> getJobs() {
        return jobs;
    }

    public void setJobs(List<GitlabJobVO> jobs) {
        this.jobs = jobs;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Long[] getTime() {
        return time;
    }

    public void setTime(Long[] time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}

