package io.choerodon.devops.api.vo;

import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author wanghao
 * @Date 2020/4/2 17:00
 */
public class DevopsCiPipelineVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ApiModelProperty("流水线名称")
    @NotEmpty(message = "error.pipeline.name.cannot.be.null")
    private String name;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("流水线关联应用服务id")
    @NotNull(message = "error.pipeline.appSvc.id.cannot.be.null")
    private Long appServiceId;

    private String appServiceName;

    @ApiModelProperty("流水线触发方式")
    @NotEmpty(message = "error.pipeline.triggerType.cannot.be.null")
    private String triggerType;
    @ApiModelProperty("阶段信息")
    @Valid
    private List<DevopsCiStageVO> stageList;

    private Long objectVersionNumber;

    public Long getId() {
        return id;
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

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<DevopsCiStageVO> getStageList() {
        return stageList;
    }

    public void setStageList(List<DevopsCiStageVO> stageList) {
        this.stageList = stageList;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }
}
