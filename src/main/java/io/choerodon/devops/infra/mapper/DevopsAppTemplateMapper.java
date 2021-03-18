package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsAppTemplateDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/9
 * @Modified By:
 */
public interface DevopsAppTemplateMapper extends BaseMapper<DevopsAppTemplateDTO> {
    List<DevopsAppTemplateDTO> pageAppTemplate(@Param("sourceId") Long sourceId,
                                               @Param("sourceType") String sourceType,
                                               @Param("userId") Long userId,
                                               @Param("params") List<String> params,
                                               @Param("searchParam") Map<String, Object> searchParam);

    List<DevopsAppTemplateDTO> listAppTemplate(@Param("sourceId") Long sourceId,
                                               @Param("sourceType") String sourceType,
                                               @Param("param") String param);
}
