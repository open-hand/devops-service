package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.CiDockerAuthConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineFunctionDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineVariableDTO;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CiCdPipelineVO {
    @Encrypt(ignoreUserConflict = true)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("流水线名称")
    @NotEmpty(message = "error.pipeline.name.cannot.be.null")
    private String name;

    @ApiModelProperty("项目id")
    private Long projectId;

    @Encrypt
    @ApiModelProperty("流水线关联应用服务id")
    @NotNull(message = "error.pipeline.appSvc.id.cannot.be.null")
    private Long appServiceId;

    @ApiModelProperty("流水线关联应用服务名称/nullable")
    private String appServiceName;
    private String appServiceType;
    private String appServiceCode;
    @ApiModelProperty("gitlab项目id/nullable")

    private Long gitlabProjectId;
    @ApiModelProperty("runner镜像地址")
    private String image;

    @ApiModelProperty("自定义版本名称")
    private String versionName;
    @ApiModelProperty("是否启用/nullable")
    private Boolean enabled;
    @ApiModelProperty("最近执行时间/nullable")
    private Date latestExecuteDate;
    @ApiModelProperty("最近执行记录状态/nullable")
    private String latestExecuteStatus;
    @ApiModelProperty(name = "ci阶段信息")
    @Valid
    private List<DevopsCiStageVO> devopsCiStageVOS;

    @ApiModelProperty(name = "cd阶段信息")
    @Valid
    private List<DevopsCdStageVO> devopsCdStageVOS;

    @ApiModelProperty(name = "流水线变量")
    private List<DevopsCiPipelineVariableDTO> devopsCiPipelineVariableDTOList;

    private Boolean hasRecords = false;
    //cicd 流水线下的执行记录
    private List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS;

    private Set<String> relatedBranches;

    private List<DevopsCiPipelineFunctionDTO> devopsCiPipelineFunctionDTOList;
    @ApiModelProperty(name = "Docker认证配置")
    private List<CiDockerAuthConfigDTO> ciDockerAuthConfigDTOList;

    private Long objectVersionNumber;

    private Boolean edit;
    private List<Long> pipelineUserRels;
    private Date lastUpdateDate;
    private Date creationDate;
    private String createUserUrl;
    private String createUserName;
    private String createUserRealName;
    private Long createdBy;
    private Boolean execute;
    private String envName;
    //流程耗时
    private Long time;


    public List<CiDockerAuthConfigDTO> getCiDockerAuthConfigDTOList() {
        return ciDockerAuthConfigDTOList;
    }

    public void setCiDockerAuthConfigDTOList(List<CiDockerAuthConfigDTO> ciDockerAuthConfigDTOList) {
        this.ciDockerAuthConfigDTOList = ciDockerAuthConfigDTOList;
    }

    public List<DevopsCiPipelineVariableDTO> getDevopsCiPipelineVariableDTOList() {
        return devopsCiPipelineVariableDTOList;
    }

    public void setDevopsCiPipelineVariableDTOList(List<DevopsCiPipelineVariableDTO> devopsCiPipelineVariableDTOList) {
        this.devopsCiPipelineVariableDTOList = devopsCiPipelineVariableDTOList;
    }

    public List<DevopsCiPipelineFunctionDTO> getDevopsCiPipelineFunctionDTOList() {
        return devopsCiPipelineFunctionDTOList;
    }

    public void setDevopsCiPipelineFunctionDTOList(List<DevopsCiPipelineFunctionDTO> devopsCiPipelineFunctionDTOList) {
        this.devopsCiPipelineFunctionDTOList = devopsCiPipelineFunctionDTOList;
    }

    public Set<String> getRelatedBranches() {
        return relatedBranches;
    }

    public void setRelatedBranches(Set<String> relatedBranches) {
        this.relatedBranches = relatedBranches;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getAppServiceType() {
        return appServiceType;
    }

    public void setAppServiceType(String appServiceType) {
        this.appServiceType = appServiceType;
    }

    public List<CiCdPipelineRecordVO> getCiCdPipelineRecordVOS() {
        return ciCdPipelineRecordVOS;
    }

    public void setCiCdPipelineRecordVOS(List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS) {
        this.ciCdPipelineRecordVOS = ciCdPipelineRecordVOS;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLatestExecuteDate() {
        return latestExecuteDate;
    }

    public void setLatestExecuteDate(Date latestExecuteDate) {
        this.latestExecuteDate = latestExecuteDate;
    }

    public String getLatestExecuteStatus() {
        return latestExecuteStatus;
    }

    public void setLatestExecuteStatus(String latestExecuteStatus) {
        this.latestExecuteStatus = latestExecuteStatus;
    }

    public List<DevopsCiStageVO> getDevopsCiStageVOS() {
        return devopsCiStageVOS;
    }

    public void setDevopsCiStageVOS(List<DevopsCiStageVO> devopsCiStageVOS) {
        this.devopsCiStageVOS = devopsCiStageVOS;
    }

    public List<DevopsCdStageVO> getDevopsCdStageVOS() {
        return devopsCdStageVOS;
    }

    public void setDevopsCdStageVOS(List<DevopsCdStageVO> devopsCdStageVOS) {
        this.devopsCdStageVOS = devopsCdStageVOS;
    }

    public Boolean getHasRecords() {
        return hasRecords;
    }

    public void setHasRecords(Boolean hasRecords) {
        this.hasRecords = hasRecords;
    }


    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public List<Long> getPipelineUserRels() {
        return pipelineUserRels;
    }

    public void setPipelineUserRels(List<Long> pipelineUserRels) {
        this.pipelineUserRels = pipelineUserRels;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getCreateUserUrl() {
        return createUserUrl;
    }

    public void setCreateUserUrl(String createUserUrl) {
        this.createUserUrl = createUserUrl;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getCreateUserRealName() {
        return createUserRealName;
    }

    public void setCreateUserRealName(String createUserRealName) {
        this.createUserRealName = createUserRealName;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

}
