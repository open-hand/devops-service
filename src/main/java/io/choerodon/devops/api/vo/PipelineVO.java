package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.cd.PipelineScheduleVO;
import io.choerodon.devops.api.vo.cd.PipelineStageVO;

/**
 * @author
 * @since 2022-11-24 16:08:35
 */
public class PipelineVO {

    private Long id;
    @ApiModelProperty(value = "项目id", required = true)
    private Long projectId;
    @ApiModelProperty(value = "流水线名称", required = true)
    @NotEmpty(message = "{devops.pipeline.name.cannot.be.null}")
    @Size(min = 1, max = 30, message = "{devops.pipeline.name.max.size.is.30}")
    private String name;
    @ApiModelProperty(value = "当前生效的版本，devops_pipeline_version.id", hidden = true)
    @Encrypt
    private Long effectVersionId;
    @ApiModelProperty(value = "令牌")
    private String token;
    @ApiModelProperty(value = "是否启用")
    private Boolean enable;
    @ApiModelProperty(value = "是否开启应用服务版本生成触发")
    private Boolean appVersionTriggerEnable;
    @Valid
    @ApiModelProperty(value = "流水线阶段信息", required = true)
    private List<PipelineStageVO> stageList;

    @ApiModelProperty(value = "流水线定时执行配置", required = true)
    private List<PipelineScheduleVO> pipelineScheduleList;

    public Boolean getAppVersionTriggerEnable() {
        return appVersionTriggerEnable;
    }

    public void setAppVersionTriggerEnable(Boolean appVersionTriggerEnable) {
        this.appVersionTriggerEnable = appVersionTriggerEnable;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public List<PipelineScheduleVO> getPipelineScheduleList() {
        return pipelineScheduleList;
    }

    public void setPipelineScheduleList(List<PipelineScheduleVO> pipelineScheduleList) {
        this.pipelineScheduleList = pipelineScheduleList;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<PipelineStageVO> getStageList() {
        return stageList;
    }

    public void setStageList(List<PipelineStageVO> stageList) {
        this.stageList = stageList;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getEffectVersionId() {
        return effectVersionId;
    }

    public void setEffectVersionId(Long effectVersionId) {
        this.effectVersionId = effectVersionId;
    }
}
