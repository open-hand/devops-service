package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.ApprovalVO;

import java.util.List;

/**
 * @author lihao
 */
public interface WorkDesktopService {
    /**
     * 列出代办事项
     */
    List<ApprovalVO> listApproval(Long organizationId,Long projectId);
}
