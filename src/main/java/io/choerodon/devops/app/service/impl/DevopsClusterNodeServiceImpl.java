package io.choerodon.devops.app.service.impl;

import java.util.List;

import net.schmizz.sshj.SSHClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestVO;
import io.choerodon.devops.api.vo.DevopsClusterNodeVO;
import io.choerodon.devops.app.service.DevopsClusterNodeService;
import io.choerodon.devops.infra.constant.ClusterCheckConstant;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.devops.infra.enums.ClusterNodeRole;
import io.choerodon.devops.infra.mapper.DevopsClusterNodeMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
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

        // 查询节点类型
        return checkNodeNumByRole(projectId, nodeId);
    }

    private Boolean checkNodeNumByRole(Long projectId, Long nodeId) {
        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);
        if (ClusterNodeRole.isMaster(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRole.listMasterRoleSet()) < 2) {
                return false;
            }
        }
        if (ClusterNodeRole.isWorker(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRole.listWorkerRoleSet()) < 2) {
                return false;
            }
        }
        if (ClusterNodeRole.isEtcd(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRole.listWorkerRoleSet()) < 2) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long nodeId) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterNodeDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        if (Boolean.FALSE.equals(checkNodeNumByRole(projectId, nodeId))) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_FAILED);
        }
        // todo 删除集群中的node

        // 删除数据库中数据
        if (devopsClusterNodeMapper.deleteByPrimaryKey(nodeId) != 1) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_FAILED);
        }
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
