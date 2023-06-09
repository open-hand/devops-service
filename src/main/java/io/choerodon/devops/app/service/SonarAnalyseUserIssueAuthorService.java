package io.choerodon.devops.app.service;

import java.util.Collection;

import io.choerodon.devops.infra.dto.SonarAnalyseUserIssueAuthorDTO;

/**
 * 代码扫描记录表(SonarAnalyseUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
public interface SonarAnalyseUserIssueAuthorService {

    void baseCreate(SonarAnalyseUserIssueAuthorDTO sonarAnalyseUserIssueAuthorDTO);

    void batchSave(Long recordId, Collection<SonarAnalyseUserIssueAuthorDTO> sonarAnalyseUserIssueAuthorDTOList);
}

