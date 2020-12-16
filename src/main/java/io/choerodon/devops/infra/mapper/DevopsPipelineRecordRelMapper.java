package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.pipeline.PipelineCompositeRecordVO;
import io.choerodon.devops.infra.dto.DevopsPipelineRecordRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/14 20:50
 */
public interface DevopsPipelineRecordRelMapper extends BaseMapper<DevopsPipelineRecordRelDTO> {
    List<DevopsPipelineRecordRelDTO> selectBySprint(@Param("pipeline_id") Long pipelineId,
                                                    @Param("startDate") Date startDate,
                                                    @Param("endDate") Date endDate);

    PipelineCompositeRecordVO queryLatestedPipelineRecord(@Param("id") Long id);
}
