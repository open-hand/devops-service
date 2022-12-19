package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiTemplateStageDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by wangxiang on 2021/12/16
 */
public interface CiTemplateStageBusMapper extends BaseMapper<CiTemplateStageDTO> {

    void deleteByIds(@Param("stageIds") Set<Long> stageIds);

}
