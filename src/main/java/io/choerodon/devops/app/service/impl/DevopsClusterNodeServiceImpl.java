package io.choerodon.devops.app.service.impl;

import java.util.List;

import net.schmizz.sshj.SSHClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestVO;
import io.choerodon.devops.api.vo.DevopsClusterNodeVO;
import io.choerodon.devops.app.service.DevopsClusterNodeService;
import io.choerodon.devops.infra.constant.ClusterCheckConstant;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.devops.infra.enums.ClusterNodeRole;
import io.choerodon.devops.infra.mapper.DevopsClusterNodeMapper;
import io.choerodon.devops.infra.util.SshUtil;

@Service
public class DevopsClusterNodeServiceImpl implements DevopsClusterNodeService {

    @Autowired
    private SshUtil sshUtil;
    @Autowired
    private DevopsClusterNodeMapper devopsClusterNodeMapper;

    @Override
    public boolean testConnection(Long projectId, DevopsClusterNodeConnectionTestVO devopsClusterNodeConnectionTestVO) {
        return sshUtil.sshConnect(devopsClusterNodeConnectionTestVO.getHostIp(),
                devopsClusterNodeConnectionTestVO.getSshPort(),
                devopsClusterNodeConnectionTestVO.getAuthType(),
                devopsClusterNodeConnectionTestVO.getUsername(),
                devopsClusterNodeConnectionTestVO.getPassword());
    }

    @Override
    public void batchInsert(List<DevopsClusterNodeDTO> devopsClusterNodeDTOList) {
        int size = devopsClusterNodeDTOList.size();
        if (devopsClusterNodeMapper.batchInsert(devopsClusterNodeDTOList) != size) {
            throw new CommonException("error.batch.insert.node");
        }
    }

    @Override
    public Boolean checkEnableDelete(Long projectId, Long nodeId) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);

        // 统计

        // 查询节点类型
        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);
        ClusterNodeRole.isMaster(devopsClusterNodeDTO.getRole());
        return null;
    }

    @Override
    public void execCommand(SSHClient ssh, String command) {
    }

    @Async
    @Override
    public void checkNode(SSHClient ssh) {
    }

    @Override
    public void uploadNodeConfiguration(SSHClient ssh, List<DevopsClusterNodeVO> devopsClusterNodeVOList) {

    }
}
