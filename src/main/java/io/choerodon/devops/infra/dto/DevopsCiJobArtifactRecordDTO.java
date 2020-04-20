package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * @author zmf
 * @since 20-4-20
 */
@Table(name = "devops_ci_job_artifact_record")
public class DevopsCiJobArtifactRecordDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("gitlab流水线记录id")
    private Long gitlabPipelineId;

    @ApiModelProperty("软件包名称，和流水线中定义的元数据一致")
    private String name;

    @ApiModelProperty("上传到文件服务后的文件地址")
    private String fileUrl;

    public DevopsCiJobArtifactRecordDTO() {
    }

    public DevopsCiJobArtifactRecordDTO(Long gitlabPipelineId, String name, String fileUrl) {
        this.gitlabPipelineId = gitlabPipelineId;
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
}
