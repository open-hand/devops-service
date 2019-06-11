package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.ApplicationTemplateDO;

/**
 * Created by younger on 2018/3/27.
 */
public interface ApplicationTemplateMapper extends Mapper<ApplicationTemplateDO> {
    List<ApplicationTemplateDO> list(@Param("organizationId") Long organizationId,
                                     @Param("searchParam") Map<String, Object> searchParam,
                                     @Param("param") String param);

    ApplicationTemplateDO  queryByCode(@Param("organizationId") Long organizationId,
                                       @Param("code") String code);
}
