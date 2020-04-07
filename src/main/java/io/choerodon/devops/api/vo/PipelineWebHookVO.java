package io.choerodon.devops.api.vo;

import java.util.List;

public class PipelineWebHookVO {
    private PipelineWebHookAttributesVO objectAttributes;
    private PipelineWebHookUserVO user;
    private List<CiJobWebHookVO> builds;
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

    public List<CiJobWebHookVO> getBuilds() {
        return builds;
    }

    public void setBuilds(List<CiJobWebHookVO> builds) {
        this.builds = builds;
    }
}
