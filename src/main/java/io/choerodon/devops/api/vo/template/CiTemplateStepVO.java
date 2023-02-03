package io.choerodon.devops.api.vo.template;

import java.util.Date;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.pipeline.CiTemplateAuditConfigVO;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * @author hao.wang08@hand-china.com
 * @since 2021-12-03 10:56:27
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CiTemplateStepVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "任务名称", required = true)
    @NotNull
    private String name;
    @ApiModelProperty(value = "层级", required = true)
    private String sourceType;
    @ApiModelProperty(value = "层级Id", required = true)
    private Long sourceId;
    @ApiModelProperty(value = "流水线步骤分类id", required = true)
    @Encrypt
    private Long categoryId;
    @ApiModelProperty(value = "分类名称")
    private String categoryName;
    @NotNull
    @ApiModelProperty(value = "步骤类型", required = true)
    private String type;
    @ApiModelProperty(value = "自定义步骤的脚本", required = true)
    private String script;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    private Boolean builtIn;


    @ApiModelProperty(value = "所属任务id", required = true)
    private Long ciTemplateJobId;
    @ApiModelProperty(value = "任务中的顺序", required = true)
    private Long sequence;

    @ApiModelProperty("创建者")
    private IamUserDTO creator;
    private Long createdBy;

    @ApiModelProperty("步骤为代码扫描时需要，保存代码扫描相关信息")
    private CiTemplateSonarDTO sonarConfig;

    @ApiModelProperty("步骤为Docker构建时需要，保存docker构建相关信息")
    private CiTemplateDockerDTO dockerBuildConfig;
    @ApiModelProperty("步骤为chart 发布时需要，保存chart 发布相关信息")
    private CiTplChartPublishConfigDTO chartPublishConfig;

    @ApiModelProperty("步骤为npm 发布时需要，保存npm发布相关信息")
    private CiNpmPublishConfigDTO npmPublishConfig;

    @ApiModelProperty("步骤为npm 构建时需要，保存npm构建相关信息")
    private CiNpmBuildConfigDTO npmBuildConfig;
    @ApiModelProperty("步骤为maven发布时需要，保存maven发布相关信息")
    private CiTemplateMavenPublishDTO mavenPublishConfig;
    @ApiModelProperty("步骤为maven构建时需要，保存maven构建相关信息")
    private CiTemplateMavenBuildDTO mavenBuildConfig;
    @ApiModelProperty("步骤为人工卡点时需要，保存人工卡点相关信息")
    private CiTemplateAuditConfigVO ciAuditConfig;
    @ApiModelProperty(value = "创建时间")
    private Date creationDate;
    @ApiModelProperty("任务模板是否可见")
    private Boolean visibility;

    public CiNpmBuildConfigDTO getNpmBuildConfig() {
        return npmBuildConfig;
    }

    public void setNpmBuildConfig(CiNpmBuildConfigDTO npmBuildConfig) {
        this.npmBuildConfig = npmBuildConfig;
    }

    public CiNpmPublishConfigDTO getNpmPublishConfig() {
        return npmPublishConfig;
    }

    public void setNpmPublishConfig(CiNpmPublishConfigDTO npmPublishConfig) {
        this.npmPublishConfig = npmPublishConfig;
    }

    public CiTplChartPublishConfigDTO getChartPublishConfig() {
        return chartPublishConfig;
    }

    public void setChartPublishConfig(CiTplChartPublishConfigDTO chartPublishConfig) {
        this.chartPublishConfig = chartPublishConfig;
    }

    public CiTemplateAuditConfigVO getCiAuditConfig() {
        return ciAuditConfig;
    }

    public void setCiAuditConfig(CiTemplateAuditConfigVO ciAuditConfig) {
        this.ciAuditConfig = ciAuditConfig;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    private CiTemplateStepCategoryVO ciTemplateStepCategoryVO;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public IamUserDTO getCreator() {
        return creator;
    }

    public void setCreator(IamUserDTO creator) {
        this.creator = creator;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getCiTemplateJobId() {
        return ciTemplateJobId;
    }

    public void setCiTemplateJobId(Long ciTemplateJobId) {
        this.ciTemplateJobId = ciTemplateJobId;
    }

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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public CiTemplateStepCategoryVO getCiTemplateStepCategoryVO() {
        return ciTemplateStepCategoryVO;
    }

    public void setCiTemplateStepCategoryVO(CiTemplateStepCategoryVO ciTemplateStepCategoryVO) {
        this.ciTemplateStepCategoryVO = ciTemplateStepCategoryVO;
    }

    public CiTemplateSonarDTO getSonarConfig() {
        return sonarConfig;
    }

    public void setSonarConfig(CiTemplateSonarDTO sonarConfig) {
        this.sonarConfig = sonarConfig;
    }

    public CiTemplateDockerDTO getDockerBuildConfig() {
        return dockerBuildConfig;
    }

    public void setDockerBuildConfig(CiTemplateDockerDTO dockerBuildConfig) {
        this.dockerBuildConfig = dockerBuildConfig;
    }

    public CiTemplateMavenPublishDTO getMavenPublishConfig() {
        return mavenPublishConfig;
    }

    public void setMavenPublishConfig(CiTemplateMavenPublishDTO mavenPublishConfig) {
        this.mavenPublishConfig = mavenPublishConfig;
    }

    public CiTemplateMavenBuildDTO getMavenBuildConfig() {
        return mavenBuildConfig;
    }

    public void setMavenBuildConfig(CiTemplateMavenBuildDTO mavenBuildConfig) {
        this.mavenBuildConfig = mavenBuildConfig;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }
}
