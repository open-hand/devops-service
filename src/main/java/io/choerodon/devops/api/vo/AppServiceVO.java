package io.choerodon.devops.api.vo;

import java.util.List;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2019/9/12
 */
public class AppServiceVO {
    @Encrypt
    private Long id;
    private String name;
    private String code;
    private String type;
    private List<AppServiceVersionVO> allAppServiceVersions;
    private String status;
    private Integer gitlabProjectId;

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
}
