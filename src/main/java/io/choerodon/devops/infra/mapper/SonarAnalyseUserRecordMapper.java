package io.choerodon.devops.infra.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.SonarAnalyseIssueAuthorVO;
import io.choerodon.devops.infra.dto.SonarAnalyseIssueAuthorDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 代码扫描记录表(SonarAnalyseUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
public interface SonarAnalyseUserRecordMapper extends BaseMapper<SonarAnalyseIssueAuthorDTO> {
    void batchSave(@Param("recordId") Long recordId,
                   @Param("sonarAnalyseUserIssueAuthorDTOList") Collection<SonarAnalyseIssueAuthorDTO> sonarAnalyseIssueAuthorDTOList);

    List<SonarAnalyseIssueAuthorVO> listMemberIssue(@Param("appServiceId") Long appServiceId);
}

