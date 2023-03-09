package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * CI配置文件关联表(CiJobConfigFileRel)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-16 15:50:03
 */

@ApiModel("CI配置文件关联表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_job_config_file_rel")
public class CiJobConfigFileRelDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_CI_JOB_ID = "ciJobId";
    public static final String FIELD_CONFIG_FILE_ID = "configFileId";
    public static final String FIELD_CONFIG_FILE_PATH = "configFilePath";
    private static final long serialVersionUID = 877944788281580208L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_ci_job.id", required = true)
    @NotNull
    private Long ciJobId;

    @ApiModelProperty(value = "devops_config_file.id", required = true)
    @NotNull
    private Long configFileId;

    @ApiModelProperty(value = "配置文件下载路径", required = true)
    @NotBlank
    private String configFilePath;


    public CiJobConfigFileRelDTO() {
    }

    public CiJobConfigFileRelDTO(Long ciJobId, Long configFileId, String configFilePath) {
        this.ciJobId = ciJobId;
        this.configFileId = configFileId;
        this.configFilePath = configFilePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiJobId() {
        return ciJobId;
    }

    public void setCiJobId(Long ciJobId) {
        this.ciJobId = ciJobId;
    }

    public Long getConfigFileId() {
        return configFileId;
    }

    public void setConfigFileId(Long configFileId) {
        this.configFileId = configFileId;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

}

