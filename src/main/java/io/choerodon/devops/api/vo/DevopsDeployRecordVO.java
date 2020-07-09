package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Sheep on 2019/7/29.
 */
public class DevopsDeployRecordVO {
    @Encrypt
    private Long id;
    @Encrypt
    private Long projectId;
    private String deployType;
    @Encrypt
    private Long deployId;
    private String env;
    private String deployStatus;
    private String pipelineName;
    private String pipelineTriggerType;
    @Encrypt
    private Long deployCreatedBy;
    private String userName;
    private String userLoginName;
    private String userImage;
    private Date deployTime;
    private List<PipelineStageRecordVO> stageDTOList;
    private PipelineDetailVO pipelineDetailVO;
    private String errorInfo;

    @ApiModelProperty("手动部署生成的实例id, 对于自动部署的纪录此值为空")
    private Long instanceId;

    @ApiModelProperty("手动部署生成的实例的环境id, 对于自动部署的纪录此值为空")
    private Long envId;

    @ApiModelProperty("手动部署的实例的应用服务id, 对于自动部署的纪录此值为空")
    private Long appServiceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Long getDeployId() {
        return deployId;
    }

    public void setDeployId(Long deployId) {
        this.deployId = deployId;
    }

    public String getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(String deployStatus) {
        this.deployStatus = deployStatus;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getPipelineTriggerType() {
        return pipelineTriggerType;
    }

    public void setPipelineTriggerType(String pipelineTriggerType) {
        this.pipelineTriggerType = pipelineTriggerType;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }


    public Date getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(Date deployTime) {
        this.deployTime = deployTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public Long getDeployCreatedBy() {
        return deployCreatedBy;
    }

    public void setDeployCreatedBy(Long deployCreatedBy) {
        this.deployCreatedBy = deployCreatedBy;
    }

    public PipelineDetailVO getPipelineDetailVO() {
        return pipelineDetailVO;
    }

    public void setPipelineDetailVO(PipelineDetailVO pipelineDetailVO) {
        this.pipelineDetailVO = pipelineDetailVO;
    }

    public List<PipelineStageRecordVO> getStageDTOList() {
        return stageDTOList;
    }

    public void setStageDTOList(List<PipelineStageRecordVO> stageDTOList) {
        this.stageDTOList = stageDTOList;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getUserLoginName() {
        return userLoginName;
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName = userLoginName;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}
