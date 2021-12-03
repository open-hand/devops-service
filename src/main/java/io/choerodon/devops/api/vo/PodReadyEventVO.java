package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/29 10:17
 */
public class PodReadyEventVO {
    private Long commandId;
    private DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO;

    public PodReadyEventVO() {
    }

    public PodReadyEventVO(Long commandId, DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO) {
        this.commandId = commandId;
        this.devopsHzeroDeployDetailsDTO = devopsHzeroDeployDetailsDTO;
    }

    public DevopsHzeroDeployDetailsDTO getDevopsHzeroDeployDetailsDTO() {
        return devopsHzeroDeployDetailsDTO;
    }

    public void setDevopsHzeroDeployDetailsDTO(DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO) {
        this.devopsHzeroDeployDetailsDTO = devopsHzeroDeployDetailsDTO;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

}
