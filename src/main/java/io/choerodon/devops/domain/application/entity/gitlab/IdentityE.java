package io.choerodon.devops.domain.application.entity.gitlab;

/**
 * Created by qs on 2017/11/14.
 */
public class IdentityE {
    private String provider;
    private String externUid;


    public String getProvider() {
        return this.provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getExternUid() {
        return this.externUid;
    }

    public void setExternUid(String externUid) {
        this.externUid = externUid;
    }
}