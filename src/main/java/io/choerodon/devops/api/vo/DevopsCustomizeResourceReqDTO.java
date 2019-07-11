package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/7/3.
 */
public class DevopsCustomizeResourceReqDTO {


    private Long envId;
    private Long resourceId;
    private String type;
    private String content;


    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
