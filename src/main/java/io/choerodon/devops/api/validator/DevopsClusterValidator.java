package io.choerodon.devops.api.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsClusterNodeVO;
import io.choerodon.devops.api.vo.DevopsClusterReqVO;
import io.choerodon.devops.infra.enums.ClusterNodeRoleEnum;

@Component
public class DevopsClusterValidator {

    public void check(DevopsClusterReqVO devopsClusterReqVO) {
        List<DevopsClusterNodeVO> devopsClusterNodeVOList = devopsClusterReqVO.getDevopsClusterInnerNodeVOList();
        // 检查节点不为空
        if (CollectionUtils.isEmpty(devopsClusterNodeVOList)) {
            throw new CommonException("error.node.size.is.zero");
        }
        // 检查节点名称唯一
        if (!checkMemberUnique(devopsClusterNodeVOList.stream().map(DevopsClusterNodeVO::getName).collect(Collectors.toList()))) {
            throw new CommonException("error.node.name.not.unique");
        }
        // 检查节点ip唯一
        if (!checkMemberUnique(devopsClusterNodeVOList.stream().map(DevopsClusterNodeVO::getHostIp).collect(Collectors.toList()))) {
            throw new CommonException("error.node.ip.not.unique");
        }

        // 检查每个节点角色数量必须大于0
        long masterCount;
        long workerCount;
        long etcdCount;
        masterCount = devopsClusterNodeVOList.stream().filter(n -> ClusterNodeRoleEnum.isMaster(n.getRole())).count();
        workerCount = devopsClusterNodeVOList.stream().filter(n -> ClusterNodeRoleEnum.isWorker(n.getRole())).count();
        etcdCount = devopsClusterNodeVOList.stream().filter(n -> ClusterNodeRoleEnum.isEtcd(n.getRole())).count();
        if (masterCount == 0 || workerCount == 0 || etcdCount == 0) {
            throw new CommonException("error.node.role.number.equal.zero");
        }
    }

    private boolean checkMemberUnique(List<?> stringList) {
        Set<Object> stringAfterFiltered = new HashSet<>(stringList);
        return stringAfterFiltered.size() == stringList.size();
    }
}
