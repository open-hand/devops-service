package io.choerodon.devops.api.vo;

import java.util.List;

public class PipelineWebHookVO {
    private PipelineWebHookAttributesVO objectAttributes;
    private PipelineWebHookUserVO user;
    private List<CiJobWebHookVO> jobList;
    private String token;

    public PipelineWebHookAttributesVO getObjectAttributes() {
        return objectAttributes;
    }

    public void setObjectAttributes(PipelineWebHookAttributesVO objectAttributes) {
        this.objectAttributes = objectAttributes;
    }

    public PipelineWebHookUserVO getUser() {
        return user;
    }

    public void setUser(PipelineWebHookUserVO user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<CiJobWebHookVO> getJobList() {
        return jobList;
    }

    public void setJobList(List<CiJobWebHookVO> jobList) {
        this.jobList = jobList;
    }
}
