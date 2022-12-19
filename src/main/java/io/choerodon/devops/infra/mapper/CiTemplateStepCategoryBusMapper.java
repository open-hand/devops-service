package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.infra.dto.CiTemplateStepCategoryDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
public interface CiTemplateStepCategoryBusMapper extends BaseMapper<CiTemplateStepCategoryDTO> {

    List<CiTemplateStepCategoryVO> queryTemplateStepCategoryByParams(@Param("sourceId") Long sourceId, @Param("searchParam") String searchParam);

    Integer checkTemplateStepCategoryName(@Param("sourceId") Long sourceId,
                                          @Param("name") String name,
                                          @Param("ciTemplateCategoryId") Long ciTemplateCategoryId);

}

