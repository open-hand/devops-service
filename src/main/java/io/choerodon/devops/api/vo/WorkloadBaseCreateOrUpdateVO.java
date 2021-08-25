package io.choerodon.devops.api.vo;

import java.util.Map;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.annotate.JsonIgnore;

public class WorkloadBaseCreateOrUpdateVO {
    @ApiModelProperty("环境id/必填")
    @NotNull(message = "error.env.id.null")
    private String envId;
    @ApiModelProperty("资源")
    private String content;
    @ApiModelProperty("操作类型，新增/更新")
    private String operateType;
    @ApiModelProperty("资源id")
    private String resourceId;
    @ApiModelProperty("额外信息，比如会用来保存部署组的应用配置、容器配置信息")
    private Map<String, Object> extraInfo;

    @JsonIgnore
    @ApiModelProperty("是否需要执行解密逻辑，工作负载调用时为true，部署组调用时为false")
    private boolean toDecrypt = true;

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Map<String, Object> extraInfo) {
        this.extraInfo = extraInfo;
    }

    public boolean getToDecrypt() {
        return toDecrypt;
    }

    public void setToDecrypt(boolean toDecrypt) {
        this.toDecrypt = toDecrypt;
    }
}
