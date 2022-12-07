package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:04:47
 */
public class CiPipelineScheduleVO extends CommonScheduleVO {


    @Encrypt
    private Long id;
    @ApiModelProperty(value = "应用服务id", required = true)
    @Encrypt
    private Long appServiceId;
    @ApiModelProperty(value = "定时任务名称", required = true)
    private String name;
    @ApiModelProperty(value = "gitlab pipeline_schedule_id", required = true)
    private Long pipelineScheduleId;
    @ApiModelProperty(value = "触发分支", required = true)
    private String ref;
    @Valid
    @ApiModelProperty(value = "变量列表")
    List<CiScheduleVariableVO> variableVOList;
    @ApiModelProperty(value = "更新者信息")
    private IamUserDTO userDTO;
    @ApiModelProperty(value = "下次执行时间")
    private Date nextRunAt;
    @ApiModelProperty(value = "是否启用")
    private Boolean active;

    @ApiModelProperty(hidden = true)
    private Date creationDate;
    @ApiModelProperty(hidden = true)
    private Long createdBy;
    @ApiModelProperty(hidden = true)
    private Date lastUpdateDate;
    @ApiModelProperty(hidden = true)
    private Long lastUpdatedBy;
    @ApiModelProperty(hidden = true)
    private Long objectVersionNumber;

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(Date nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public IamUserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(IamUserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public List<CiScheduleVariableVO> getVariableVOList() {
        return variableVOList;
    }

    public void setVariableVOList(List<CiScheduleVariableVO> variableVOList) {
        this.variableVOList = variableVOList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPipelineScheduleId() {
        return pipelineScheduleId;
    }

    public void setPipelineScheduleId(Long pipelineScheduleId) {
        this.pipelineScheduleId = pipelineScheduleId;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }


}
