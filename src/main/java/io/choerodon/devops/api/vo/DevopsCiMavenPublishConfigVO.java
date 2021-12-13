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
    private Long id;

    @Encrypt
    @ApiModelProperty("项目下已有的maven仓库id列表 json")
    private String nexusMavenRepoIdStr;

    @ApiModelProperty("表单填写的Maven的依赖仓库 json格式")
    private String repoStr;

    @ApiModelProperty("直接粘贴的maven的settings内容")
    private String mavenSettings;

    @Encrypt
    @ApiModelProperty("nexus的maven仓库在制品库的主键id")
    private Long nexusRepoIds;

    @Encrypt
    @ApiModelProperty("项目下已有的maven仓库id列表")
    private Set<Long> nexusMavenRepoIds;

    @ApiModelProperty("表单填写的Maven的依赖仓库")
    private List<MavenRepoVO> repos;

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

    public Long getNexusRepoIds() {
        return nexusRepoIds;
    }

    public void setNexusRepoIds(Long nexusRepoIds) {
        this.nexusRepoIds = nexusRepoIds;
    }
}
