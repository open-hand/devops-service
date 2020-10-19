package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestResultVO;
import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestVO;
import io.choerodon.devops.app.service.DevopsClusterNodeService;
import io.choerodon.devops.infra.enums.DevopsHostStatus;
import io.choerodon.devops.infra.util.SshUtil;

public class DevopsClusterNodeServiceImpl implements DevopsClusterNodeService {

    @Autowired
    private SshUtil sshUtil;

    @Override
    public DevopsClusterNodeConnectionTestResultVO testConnection(Long projectId, DevopsClusterNodeConnectionTestVO devopsClusterNodeConnectionTestVO) {
        DevopsClusterNodeConnectionTestResultVO result = new DevopsClusterNodeConnectionTestResultVO();
        boolean sshConnected = sshUtil.sshConnect(devopsClusterNodeConnectionTestVO.getHostIp(), devopsClusterNodeConnectionTestVO.getSshPort(), devopsClusterNodeConnectionTestVO.getAuthType(), devopsClusterNodeConnectionTestVO.getUsername(), devopsClusterNodeConnectionTestVO.getPassword());
        result.setHostStatus(sshConnected ? DevopsHostStatus.SUCCESS.getValue() : DevopsHostStatus.FAILED.getValue());
        if (!sshConnected) {
            result.setHostCheckError("failed to check ssh, please ensure network and authentication is valid");
        }
        return result;
    }
}
