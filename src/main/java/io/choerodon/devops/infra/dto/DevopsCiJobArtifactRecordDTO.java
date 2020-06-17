package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author zmf
 * @since 20-4-20
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_job_artifact_record")
@Deprecated
public class DevopsCiJobArtifactRecordDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("gitlab流水线记录id")
    private Long gitlabPipelineId;

    @ApiModelProperty("gitlab内置的ci过程的job id")
    private Long gitlabJobId;

    @ApiModelProperty("软件包名称，和流水线中定义的元数据一致")
    private String name;

    @ApiModelProperty("上传到文件服务后的文件地址")
    private String fileUrl;

    public DevopsCiJobArtifactRecordDTO() {
    }

    public DevopsCiJobArtifactRecordDTO(Long gitlabPipelineId, Long gitlabJobId, String name, String fileUrl) {
        this.gitlabPipelineId = gitlabPipelineId;
        this.gitlabJobId = gitlabJobId;
        this.name = name;
        this.fileUrl = fileUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getGitlabJobId() {
        return gitlabJobId;
    }

    public void setGitlabJobId(Long gitlabJobId) {
        this.gitlabJobId = gitlabJobId;
    }
}
