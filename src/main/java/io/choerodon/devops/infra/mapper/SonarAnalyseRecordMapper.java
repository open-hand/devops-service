package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.SonarAnalyseRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 代码扫描记录表(SonarAnalyseRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
public interface SonarAnalyseRecordMapper extends BaseMapper<SonarAnalyseRecordDTO> {
    List<SonarAnalyseRecordDTO> listProjectLatestRecord(@Param("pids") List<Long> pids);

}

