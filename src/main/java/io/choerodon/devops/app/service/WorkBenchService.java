package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.ApprovalVO;
import io.choerodon.devops.api.vo.CommitFormRecordVO;
import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.api.vo.dashboard.ProjectMeasureVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author lihao
 */
public interface WorkBenchService {
    /**
     * 列出代办事项
     */
    List<ApprovalVO> listApproval(Long organizationId, Long projectId);

    List<LatestAppServiceVO> listLatestAppService(Long organizationId, Long projectId);

    Page<CommitFormRecordVO> listLatestCommits(Long organizationId, Long projectId, PageRequest pageRequest);

    Page<ProjectMeasureVO> listProjectMeasure(Long organizationId, PageRequest pageRequest);
}
