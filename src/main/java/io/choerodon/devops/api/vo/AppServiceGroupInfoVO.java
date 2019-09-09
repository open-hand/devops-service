package io.choerodon.devops.api.vo;

import java.util.Objects;

/**
 * @author zhaotianxin
 * @since 2019/8/13
 */
public class AppServiceGroupInfoVO {
    private Long id;
    private String name;
    private String code;
    private Long appId;
    private String type;
    private Long versionId;
    private String version;
    private Boolean share;
    private String  appName;

    public Boolean getShare() {
        return share;
    }

    public void setShare(Boolean share) {
        this.share = share;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AppServiceGroupInfoVO appServiceGroupInfoVO = (AppServiceGroupInfoVO) obj;
        return Objects.equals(id, appServiceGroupInfoVO.id);
    }
}
