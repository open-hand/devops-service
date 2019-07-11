package io.choerodon.devops.api.vo;

public class PipelineWebHookDTO {
    private PipelineWebHookAttributesDTO objectAttributes;
    private PipelineWebHookUserDTO user;
    private String token;

    public PipelineWebHookAttributesDTO getObjectAttributes() {
        return objectAttributes;
    }

    public void setObjectAttributes(PipelineWebHookAttributesDTO objectAttributes) {
        this.objectAttributes = objectAttributes;
    }

    public PipelineWebHookUserDTO getUser() {
        return user;
    }

    public void setUser(PipelineWebHookUserDTO user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
