package io.choerodon.devops.app.service;

import java.util.Collection;
import java.util.List;

import io.choerodon.devops.api.vo.SonarAnalyseIssueAuthorVO;
import io.choerodon.devops.infra.dto.SonarAnalyseIssueAuthorDTO;

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
}

