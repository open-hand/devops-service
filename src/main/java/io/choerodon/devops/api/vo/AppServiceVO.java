package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2019/9/12
 */
public class AppServiceVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("应用服务名称")
    private String name;
    @ApiModelProperty("应用服务编码")
    private String code;
    @ApiModelProperty("应用服务类型")
    private String type;
    @ApiModelProperty("应用服务版本")
    private List<AppServiceVersionVO> allAppServiceVersions;
    @ApiModelProperty("应用服务状态")
    private String status;
    @ApiModelProperty("gitlabProjectId")
    private Integer gitlabProjectId;
    @Encrypt
    @ApiModelProperty("外部仓库配置id")
    private Long externalConfigId;

    public List<AppServiceVersionVO> getAllAppServiceVersions() {
        return allAppServiceVersions;
    }

    public void setAllAppServiceVersions(List<AppServiceVersionVO> allAppServiceVersions) {
        this.allAppServiceVersions = allAppServiceVersions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Integer gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public Long getExternalConfigId() {
        return externalConfigId;
    }

    public void setExternalConfigId(Long externalConfigId) {
        this.externalConfigId = externalConfigId;
    }
}
