package io.choerodon.devops.api.vo.deploy.hzero;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 9:30
 */
public class HzeroDeployRecordVO {

    private DevopsEnvironmentDTO environmentDTO;

    private String mktApplication;

    private String mktAppVersion;


    private List<DevopsHzeroDeployDetailsVO> deployDetailsVOList;


    public String getMktApplication() {
        return mktApplication;
    }

    public void setMktApplication(String mktApplication) {
        this.mktApplication = mktApplication;
    }

    public DevopsEnvironmentDTO getEnvironmentDTO() {
        return environmentDTO;
    }

    public void setEnvironmentDTO(DevopsEnvironmentDTO environmentDTO) {
        this.environmentDTO = environmentDTO;
    }

    public String getMktAppVersion() {
        return mktAppVersion;
    }

    public void setMktAppVersion(String mktAppVersion) {
        this.mktAppVersion = mktAppVersion;
    }

    public List<DevopsHzeroDeployDetailsVO> getDeployDetailsVOList() {
        return deployDetailsVOList;
    }

    public void setDeployDetailsVOList(List<DevopsHzeroDeployDetailsVO> deployDetailsVOList) {
        this.deployDetailsVOList = deployDetailsVOList;
    }
}
