package io.choerodon.devops.app.service;

import java.util.Collection;

import io.choerodon.devops.infra.dto.SonarAnalyseUserRecordDTO;

/**
 * 代码扫描记录表(SonarAnalyseUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
public interface SonarAnalyseUserRecordService {

    void baseCreate(SonarAnalyseUserRecordDTO sonarAnalyseUserRecordDTO);

    void batchSave(Long recordId, Collection<SonarAnalyseUserRecordDTO> sonarAnalyseUserRecordDTOList);
}

