package io.choerodon.devops.infra.dataobject.gitlab;

public class IdentityDO {

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