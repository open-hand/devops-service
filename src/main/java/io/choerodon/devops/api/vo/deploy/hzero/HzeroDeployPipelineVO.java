package io.choerodon.devops.api.vo.deploy.hzero;

import java.util.List;

import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 11:23
 */
public class HzeroDeployPipelineVO {
    private String businessKey;
    private List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsDTOList;


    public HzeroDeployPipelineVO() {
    }

    public HzeroDeployPipelineVO(String businessKey, List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsDTOList) {
        this.businessKey = businessKey;
        this.devopsHzeroDeployDetailsDTOList = devopsHzeroDeployDetailsDTOList;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public List<DevopsHzeroDeployDetailsDTO> getDevopsHzeroDeployDetailsDTOList() {
        return devopsHzeroDeployDetailsDTOList;
    }

    public void setDevopsHzeroDeployDetailsDTOList(List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsDTOList) {
        this.devopsHzeroDeployDetailsDTOList = devopsHzeroDeployDetailsDTOList;
    }
}
