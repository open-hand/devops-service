package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.gitlab.PipelineDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:26 2019/4/4
 * Description:
 */
public interface PipelineMapper extends BaseMapper<PipelineDO> {

    List<PipelineDO> listByOptions(@Param("projectId") Long projectId,
                                   @Param("searchParam") Map<String, Object> searchParam,
                                   @Param("param") String param,
                                   @Param("index") String index);
}
