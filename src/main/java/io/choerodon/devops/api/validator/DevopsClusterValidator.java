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

@Component
public class DevopsClusterValidator {

    public void check(DevopsClusterReqVO devopsClusterReqVO) {
        List<DevopsClusterNodeVO> devopsClusterNodeVOList = devopsClusterReqVO.getDevopsClusterNodeVOList();
        if (CollectionUtils.isEmpty(devopsClusterNodeVOList)) {
            throw new CommonException("error.node.size.is.zero");
        }
        if (!checkMemberUnique(devopsClusterNodeVOList.stream().map(DevopsClusterNodeVO::getName).collect(Collectors.toList()))) {
            throw new CommonException("error.node.name.not.unique");
        }
        if (!checkMemberUnique(devopsClusterNodeVOList.stream().map(DevopsClusterNodeVO::getHostIp).collect(Collectors.toList()))) {
            throw new CommonException("error.node.ip.not.unique");
        }
    }

    private boolean checkMemberUnique(List<?> stringList) {
        Set<Object> stringAfterFiltered = new HashSet<>(stringList);
        return stringAfterFiltered.size() == stringList.size();
    }
}
