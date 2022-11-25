package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.cd.PipelineStageVO;

/**
 * @author
 * @since 2022-11-24 16:08:35
 */
public class PipelineVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "项目id", required = true)
    private Long projectId;
    @ApiModelProperty(value = "流水线名称", required = true)
    private String name;
    @ApiModelProperty(value = "当前生效的版本，devops_pipeline_version.id", required = true)
    @Encrypt
    private Long effectVersionId;
    @ApiModelProperty(value = "令牌", required = false)
    private String token;
    @Valid
    @ApiModelProperty(value = "流水线阶段信息", required = true)
    private List<PipelineStageVO> stageList;

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
