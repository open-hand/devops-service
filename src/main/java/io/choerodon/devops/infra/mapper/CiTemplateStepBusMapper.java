package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.infra.dto.CiTemplateStepDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
public interface CiTemplateStepBusMapper extends BaseMapper<CiTemplateStepDTO> {

    List<CiTemplateStepVO> queryTemplateStepByParams(@Param("sourceId") Long sourceId,
                                                     @Param("sourceType") String sourceType,
                                                     @Param("organizationId") Long organizationId,
                                                     @Param("name") String name,
                                                     @Param("categoryName") String categoryName,
                                                     @Param("categoryId") Long categoryId,
                                                     @Param("builtIn") Boolean builtIn,
                                                     @Param("params") String params);

    List<CiTemplateStepDTO> queryStepTemplateByJobIdAndSourceId(@Param("sourceId") Long sourceId, @Param("templateJobId") Long templateJobId);

    List<CiTemplateStepDTO> queryStepTemplateByJobId(@Param("templateJobId") Long templateJobId);

    void deleteByIds(@Param("stepIds") Set<Long> stepIds);

    List<CiTemplateStepDTO> selectByParams(@Param("sourceId") Long sourceId,
                                           @Param("sourceType") String sourceType,
                                           @Param("organizationId") Long organizationId,
                                           @Param("name") String name);

    Integer checkTemplateStepName(
            @Param("sourceId") Long sourceId,
            @Param("name") String name,
            @Param("templateStepId") Long templateStepId);

}

