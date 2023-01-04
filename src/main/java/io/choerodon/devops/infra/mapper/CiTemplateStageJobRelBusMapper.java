package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiTemplateStageJobRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by wangxiang on 2021/12/20
 */
public interface CiTemplateStageJobRelBusMapper extends BaseMapper<CiTemplateStageJobRelDTO> {
    void deleteByIds(@Param("stageJobRelIds") Set<Long> stageJobRelIds);

    List<CiTemplateStageJobRelDTO> listByStageId(@Param("stageId") Long stageId);
}
