package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 17:52
 */
public class DevopsCiMavenPublishConfigVO {
    @Encrypt
    private Long id;

    @ApiModelProperty("项目下已有的maven仓库id列表 json")
    private String nexusMavenRepoIdStr;

    @ApiModelProperty("表单填写的Maven的依赖仓库 json格式")
    private String repoStr;

    @ApiModelProperty("发包的目的仓库信息 json格式")
    private String targetRepoStr;

    @ApiModelProperty("发包的目的仓库信息")
    private MavenRepoVO targetRepo;

    @ApiModelProperty("直接粘贴的maven的settings内容")
    private String mavenSettings;

    @Encrypt
    @ApiModelProperty("nexus的maven仓库在制品库的主键id")
    private Long nexusRepoId;

    @Encrypt
    @ApiModelProperty("项目下已有的maven仓库id列表")
    private Set<Long> nexusMavenRepoIds;

    @ApiModelProperty("表单填写的Maven的依赖仓库")
    private List<MavenRepoVO> repos;

    @ApiModelProperty("坐标来源类型")
    private String gavSourceType;

    private String pomLocation;

    private String groupId;

    private String artifactId;

    private String version;
    private String packaging;

    public String getGavSourceType() {
        return gavSourceType;
    }

    public void setGavSourceType(String gavSourceType) {
        this.gavSourceType = gavSourceType;
    }

    public String getPomLocation() {
        return pomLocation;
    }

    public void setPomLocation(String pomLocation) {
        this.pomLocation = pomLocation;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getTargetRepoStr() {
        return targetRepoStr;
    }

    public void setTargetRepoStr(String targetRepoStr) {
        this.targetRepoStr = targetRepoStr;
    }

    public MavenRepoVO getTargetRepo() {
        return targetRepo;
    }

    public void setTargetRepo(MavenRepoVO targetRepo) {
        this.targetRepo = targetRepo;
    }

    public Set<Long> getNexusMavenRepoIds() {
        return nexusMavenRepoIds;
    }

    public void setNexusMavenRepoIds(Set<Long> nexusMavenRepoIds) {
        this.nexusMavenRepoIds = nexusMavenRepoIds;
    }

    public List<MavenRepoVO> getRepos() {
        return repos;
    }

    public void setRepos(List<MavenRepoVO> repos) {
        this.repos = repos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNexusMavenRepoIdStr() {
        return nexusMavenRepoIdStr;
    }

    public void setNexusMavenRepoIdStr(String nexusMavenRepoIdStr) {
        this.nexusMavenRepoIdStr = nexusMavenRepoIdStr;
    }

    public String getRepoStr() {
        return repoStr;
    }

    public void setRepoStr(String repoStr) {
        this.repoStr = repoStr;
    }

    public String getMavenSettings() {
        return mavenSettings;
    }

    public void setMavenSettings(String mavenSettings) {
        this.mavenSettings = mavenSettings;
    }

    public Long getNexusRepoId() {
        return nexusRepoId;
    }

    public void setNexusRepoId(Long nexusRepoId) {
        this.nexusRepoId = nexusRepoId;
    }
}
