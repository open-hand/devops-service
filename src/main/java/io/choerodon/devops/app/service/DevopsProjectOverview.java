package io.choerodon.devops.app.service;

import java.util.Map;
import java.util.Set;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.CountVO;
import io.choerodon.devops.api.vo.MergeRequestVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsProjectOverview {
    Map<String, Long> getEnvStatusCount(Long projectId);

    Map<String, Long> getAppServiceStatusCount(Long projectId);

    CountVO getCommitCount(Long projectId);

    CountVO getDeployCount(Long projectId);

    CountVO getCiCount(Long projectId);

    /**
     * 获取项目下待审核的合并请求
     *
     * @param projectId
     * @return
     */
    Page<MergeRequestVO> getMergeRequestToBeChecked(Long projectId, Set<Long> appServiceIdsToSearch, String param, PageRequest pageRequest);
}
