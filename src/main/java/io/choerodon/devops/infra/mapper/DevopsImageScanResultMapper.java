package io.choerodon.devops.infra.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsImageScanResultDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by wangxiang on 2021/3/25
 */
public interface DevopsImageScanResultMapper extends BaseMapper<DevopsImageScanResultDTO> {
    List<DevopsImageScanResultDTO> pageByOptions(@Param("gitlabPipelineId") Long gitlabPipelineId, @Param("jobId") Long jobId, @Param("options") String options);
}
