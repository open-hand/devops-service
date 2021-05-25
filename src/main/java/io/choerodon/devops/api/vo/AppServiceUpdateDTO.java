package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;

/**
 * Created by younger on 2018/4/10.
 */
public class AppServiceUpdateDTO {

    @ApiModelProperty("id")
    @NotNull(message = "error.id.null")
    @Encrypt
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("图片链接")
    private String imgUrl;
    @ApiModelProperty("harbor配置")
    private DevopsConfigVO harbor;
    @ApiModelProperty("char配置")
    private DevopsConfigVO chart;

    @Length(max = 512, min = 1)
    @Pattern(regexp = "[A-Za-z0-9_\\-.]+")
    @ApiModelProperty("应用服务附加的pom信息：groupId（敏捷使用）")
    private String groupId;

    @Length(max = 512, min = 1)
    @Pattern(regexp = "[A-Za-z0-9_\\-.]+")
    @ApiModelProperty("应用服务附加的pom信息：artifactId（敏捷使用）")
    private String artifactId;

    @ApiModelProperty("制品库docker仓库配置")
    private HarborRepoConfigDTO harborRepoConfigDTO;

    @ApiModelProperty("自定义仓库id")
    private Long customRepoId;

    public Long getCustomRepoId() {
        return customRepoId;
    }

    public void setCustomRepoId(Long customRepoId) {
        this.customRepoId = customRepoId;
    }

    public DevopsConfigVO getHarbor() {
        return harbor;
    }

    public void setHarbor(DevopsConfigVO harbor) {
        this.harbor = harbor;
    }

    public DevopsConfigVO getChart() {
        return chart;
    }

    public void setChart(DevopsConfigVO chart) {
        this.chart = chart;
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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public HarborRepoConfigDTO getHarborRepoConfigDTO() {
        return harborRepoConfigDTO;
    }

    public void setHarborRepoConfigDTO(HarborRepoConfigDTO harborRepoConfigDTO) {
        this.harborRepoConfigDTO = harborRepoConfigDTO;
    }

}
