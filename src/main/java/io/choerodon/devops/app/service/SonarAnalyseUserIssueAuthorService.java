package io.choerodon.devops.app.service;

import java.util.Collection;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.SonarAnalyseIssueAuthorRankVO;
import io.choerodon.devops.api.vo.SonarAnalyseIssueAuthorVO;
import io.choerodon.devops.infra.dto.SonarAnalyseIssueAuthorDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 代码扫描记录表(SonarAnalyseUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
public interface SonarAnalyseUserIssueAuthorService {

    void baseCreate(SonarAnalyseIssueAuthorDTO sonarAnalyseIssueAuthorDTO);

    void batchSave(Long recordId, Collection<SonarAnalyseIssueAuthorDTO> sonarAnalyseIssueAuthorDTOList);

    List<SonarAnalyseIssueAuthorVO> listMemberIssue(Long appServiceId);

    List<SonarAnalyseIssueAuthorRankVO> listMemberBugRank(Long projectId, Long appServiceId);

    Page<SonarAnalyseIssueAuthorVO> listMemberIssue(Long projectId, Long appServiceId, PageRequest pageRequest);

    List<SonarAnalyseIssueAuthorRankVO> listMemberVulnRank(Long projectId, Long appServiceId);

    List<SonarAnalyseIssueAuthorRankVO> listMemberCodeSmellRank(Long projectId, Long appServiceId);
}

