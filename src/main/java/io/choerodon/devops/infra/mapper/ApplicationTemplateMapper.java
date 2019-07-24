package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.ApplicationTemplateDTO;

/**
 * Created by younger on 2018/3/27.
 */
public interface ApplicationTemplateMapper extends Mapper<ApplicationTemplateDTO> {
    List<ApplicationTemplateDTO> listByOrganizationId(@Param("organizationId") Long organizationId,
                                      @Param("searchParam") Map<String, Object> searchParam,
                                      @Param("param") String param);

    ApplicationTemplateDTO queryByCode(@Param("organizationId") Long organizationId,
                                       @Param("code") String code);
}
