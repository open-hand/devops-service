package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsImageScanResultDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by wangxiang on 2021/3/25
 */
public interface DevopsImageScanResultMapper extends BaseMapper<DevopsImageScanResultDTO> {
    List<DevopsImageScanResultDTO> pageByOptions(@Param("gitlabPipelineId") Long gitlabPipelineId, @Param("options") String options);

    void deleteByGitlabPipelineIds(@Param("gitlabPipelineIds") List<Long> gitlabPipelineIds);

    int insertScanResultBatch(@Param("devopsImageScanResultDTOS") List<DevopsImageScanResultDTO> devopsImageScanResultDTOS);

    void updateScanDate(@Param("startDate") Date startDate,
                        @Param("endDate") Date endDate,
                        @Param("devopsPipelineId") Long devopsPipelineId,
                        @Param("gitlabPipelineId") Long gitlabPipelineId,
                        @Param("jobName") String jobName);
}
