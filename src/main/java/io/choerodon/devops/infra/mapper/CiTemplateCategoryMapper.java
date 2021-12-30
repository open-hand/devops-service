package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线模板适用语言表(CiTemplateLanguage)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:18
 */
public interface CiTemplateCategoryMapper extends BaseMapper<CiTemplateCategoryDTO> {

    List<CiTemplateCategoryDTO> listByIds(@Param("ids") Set<Long> ids);
}

