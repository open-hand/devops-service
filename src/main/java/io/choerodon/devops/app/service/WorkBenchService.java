package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.ApprovalVO;
import io.choerodon.devops.api.vo.LatestAppServiceVO;

import java.util.List;

/**
 * @author lihao
 */
public interface WorkBenchService {
    /**
     * 列出代办事项
     */
    List<ApprovalVO> listApproval(Long organizationId, Long projectId);

    List<LatestAppServiceVO> listLatestAppService(Long organizationId, Long projectId);
}
