package io.choerodon.devops.api.vo.deploy.hzero;

import java.util.List;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 9:30
 */
public class HzeroDeployVO {

    @Encrypt
    private Long mktAppId;
    @Encrypt
    private Long mktAppVersionId;
    private String mktAppVersion;
    @Encrypt
    private Long envId;

    private List<HzeroInstanceVO> instanceList;


    public String getMktAppVersion() {
        return mktAppVersion;
    }

    public void setMktAppVersion(String mktAppVersion) {
        this.mktAppVersion = mktAppVersion;
    }

    public Long getMktAppId() {
        return mktAppId;
    }

    public void setMktAppId(Long mktAppId) {
        this.mktAppId = mktAppId;
    }

    public Long getMktAppVersionId() {
        return mktAppVersionId;
    }

    public void setMktAppVersionId(Long mktAppVersionId) {
        this.mktAppVersionId = mktAppVersionId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public List<HzeroInstanceVO> getInstanceList() {
        return instanceList;
    }

    public void setInstanceList(List<HzeroInstanceVO> instanceList) {
        this.instanceList = instanceList;
    }
}
