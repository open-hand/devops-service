package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsClusterNodeVO;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/30 10:15
 */
public interface DevopsClusterNodeOperatorService {
    void addNode(Long projectId, Long clusterId, String operatingId, DevopsClusterNodeVO nodeVO);

    void deleteNode(Long projectId, DevopsClusterNodeDTO devopsClusterNodeDTO, Long operationRecordId);

    void deleteNodeRole(Long projectId, DevopsClusterNodeDTO devopsClusterNodeDTO, Integer role, Long operationRecordId);
}
