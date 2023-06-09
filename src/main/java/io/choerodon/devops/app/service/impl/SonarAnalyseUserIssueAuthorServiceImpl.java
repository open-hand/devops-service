package io.choerodon.devops.app.service.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.SonarAnalyseUserIssueAuthorService;
import io.choerodon.devops.infra.dto.SonarAnalyseUserIssueAuthorDTO;
import io.choerodon.devops.infra.mapper.SonarAnalyseUserRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 代码扫描记录表(SonarAnalyseUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
@Service
public class SonarAnalyseUserIssueAuthorServiceImpl implements SonarAnalyseUserIssueAuthorService {

    private static final String DEVOPS_SAVE_SONAR_ANALYSE_USER_RECORD_FAILED = "devops.save.sonar.analyse.user.record.failed";
    @Autowired
    private SonarAnalyseUserRecordMapper sonarAnalyseUserRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(SonarAnalyseUserIssueAuthorDTO sonarAnalyseUserIssueAuthorDTO) {
        MapperUtil.resultJudgedInsertSelective(sonarAnalyseUserRecordMapper, sonarAnalyseUserIssueAuthorDTO, DEVOPS_SAVE_SONAR_ANALYSE_USER_RECORD_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long recordId, Collection<SonarAnalyseUserIssueAuthorDTO> sonarAnalyseUserIssueAuthorDTOList) {
        if (!CollectionUtils.isEmpty(sonarAnalyseUserIssueAuthorDTOList)) {
            sonarAnalyseUserRecordMapper.batchSave(recordId, sonarAnalyseUserIssueAuthorDTOList);
        }
    }
}

