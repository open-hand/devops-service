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
    private String mktAppVersion;
    @Encrypt
    private Long envId;

    private List<DevopsHzeroDeployDetailsVO> deployDetailsVOList;


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

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public List<DevopsHzeroDeployDetailsVO> getDeployDetailsVOList() {
        return deployDetailsVOList;
    }

    public void setDeployDetailsVOList(List<DevopsHzeroDeployDetailsVO> deployDetailsVOList) {
        this.deployDetailsVOList = deployDetailsVOList;
    }
}
