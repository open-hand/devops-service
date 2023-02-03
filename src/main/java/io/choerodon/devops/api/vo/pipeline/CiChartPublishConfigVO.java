package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author hao.wang@zknow.com
 * @since 2023-01-09 14:46:13
 */
public class CiChartPublishConfigVO {

    @Encrypt
    private Long id;
    @Encrypt
    @ApiModelProperty(value = "所属步骤id", required = true)
    private Long stepId;
    @Encrypt
    @ApiModelProperty(value = "helm仓库id")
    private Long repoId;
    @ApiModelProperty(value = "是否使用默认仓库")
    private Boolean useDefaultRepo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

    public Boolean getUseDefaultRepo() {
        return useDefaultRepo;
    }

    public void setUseDefaultRepo(Boolean useDefaultRepo) {
        this.useDefaultRepo = useDefaultRepo;
    }
}
