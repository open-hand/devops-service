package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.ApplicationTemplateDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/3/27.
 */
public interface ApplicationTemplateMapper extends BaseMapper<ApplicationTemplateDO> {
    List<ApplicationTemplateDO> list(@Param("organizationId") Long organizationId,
                                     @Param("searchParam") Map<String, Object> searchParam,
                                     @Param("param") String param);
}
