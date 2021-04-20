package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 9:57
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CiConfigTemplateVO {
    @ApiModelProperty("步骤名称")
    @NotEmpty(message = "error.step.name.cannot.be.null")
    private String name;

    /**
     * {@link io.choerodon.devops.infra.enums.CiJobScriptTypeEnum}
     */
    @NotEmpty(message = "error.step.type.cannot.be.empty")
    @ApiModelProperty("步骤类型")
    private String type;

    @ApiModelProperty("步骤顺序")
    @NotNull(message = "error.step.sequence.cannot.be.null")
    private Long sequence;

    @ApiModelProperty("执行脚本/Base64加密过, 解决特殊符号问题")
    private String script;

    @Encrypt
    @ApiModelProperty("项目下已有的maven仓库id列表/用于maven构建步骤")
    private Set<Long> nexusMavenRepoIds;

    @ApiModelProperty("表单填写的Maven的依赖仓库")
    private List<MavenRepoVO> repos;

    @ApiModelProperty("直接粘贴的maven的settings内容 / 是base64加密过的字符串, 处理特殊字符")
    private String mavenSettings;

    @ApiModelProperty("Maven发布jar到maven仓库的配置对象 / 上传软件包到制品库步骤需要")
    private MavenDeployRepoSettings mavenDeployRepoSettings;

    @ApiModelProperty("Docker步骤的构建上下文")
    private String dockerContextDir;

    @ApiModelProperty("Dockerfile文件路径")
    private String dockerFilePath;

    @ApiModelProperty("是否跳过harbor的证书校验 / true表示跳过")
    private Boolean skipDockerTlsVerify;

    @ApiModelProperty("是否开启镜像扫描/true表示开启")
    private Boolean imageScan;

    @ApiModelProperty("是否开启安全门禁")
    private Boolean securityControl;


    @ApiModelProperty("门禁条件")
    private SecurityConditionConfigVO securityCondition;


    @ApiModelProperty("是否自定义镜像名")
    private Boolean customDockerTagName;

    @ApiModelProperty("自定义镜像名")
    private String dockerTagName;

    @ApiModelProperty("是否自定义chart版本名")
    private Boolean customChartVersionName;

    @ApiModelProperty("自定义chart版本名")
    private String chartVersionName;

    @ApiModelProperty("chart关联docker任务名")
    private String dockerJobName;

    @Encrypt
    @ApiModelProperty("chart关联docker任务 Id")
    private Long dockerJobId;

    public Boolean getSecurityControl() {
        return securityControl;
    }

    public void setSecurityControl(Boolean securityControl) {
        this.securityControl = securityControl;
    }

    public SecurityConditionConfigVO getSecurityCondition() {
        return securityCondition;
    }

    public void setSecurityCondition(SecurityConditionConfigVO securityCondition) {
        this.securityCondition = securityCondition;
    }

    public Boolean getImageScan() {
        return imageScan;
    }

    public void setImageScan(Boolean imageScan) {
        this.imageScan = imageScan;
    }

    public Boolean getCustomChartVersionName() {
        return customChartVersionName;
    }

    public void setCustomChartVersionName(Boolean customChartVersionName) {
        this.customChartVersionName = customChartVersionName;
    }

    public String getDockerJobName() {
        return dockerJobName;
    }

    public void setDockerJobName(String dockerJobName) {
        this.dockerJobName = dockerJobName;
    }

    public Long getDockerJobId() {
        return dockerJobId;
    }

    public void setDockerJobId(Long dockerJobId) {
        this.dockerJobId = dockerJobId;
    }

    public String getChartVersionName() {
        return chartVersionName;
    }

    public void setChartVersionName(String chartVersionName) {
        this.chartVersionName = chartVersionName;
    }

    public String getDockerTagName() {
        return dockerTagName;
    }

    public void setDockerTagName(String dockerTagName) {
        this.dockerTagName = dockerTagName;
    }

    public Boolean getCustomDockerTagName() {
        return customDockerTagName;
    }

    public void setCustomDockerTagName(Boolean customDockerTagName) {
        this.customDockerTagName = customDockerTagName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public List<MavenRepoVO> getRepos() {
        return repos;
    }

    public void setRepos(List<MavenRepoVO> repos) {
        this.repos = repos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDockerContextDir() {
        return dockerContextDir;
    }

    public void setDockerContextDir(String dockerContextDir) {
        this.dockerContextDir = dockerContextDir;
    }

    public String getDockerFilePath() {
        return dockerFilePath;
    }

    public void setDockerFilePath(String dockerFilePath) {
        this.dockerFilePath = dockerFilePath;
    }

    public String getMavenSettings() {
        return mavenSettings;
    }

    public void setMavenSettings(String mavenSettings) {
        this.mavenSettings = mavenSettings;
    }

    public Boolean getSkipDockerTlsVerify() {
        return skipDockerTlsVerify;
    }

    public void setSkipDockerTlsVerify(Boolean skipDockerTlsVerify) {
        this.skipDockerTlsVerify = skipDockerTlsVerify;
    }

    public MavenDeployRepoSettings getMavenDeployRepoSettings() {
        return mavenDeployRepoSettings;
    }

    public void setMavenDeployRepoSettings(MavenDeployRepoSettings mavenDeployRepoSettings) {
        this.mavenDeployRepoSettings = mavenDeployRepoSettings;
    }

    public Set<Long> getNexusMavenRepoIds() {
        return nexusMavenRepoIds;
    }

    public void setNexusMavenRepoIds(Set<Long> nexusMavenRepoIds) {
        this.nexusMavenRepoIds = nexusMavenRepoIds;
    }
}
