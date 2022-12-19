package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by wangxiang on 2021/12/2
 */
public interface CiTemplateCategoryBusMapper extends BaseMapper<CiTemplateCategoryDTO> {
    List<CiTemplateCategoryDTO> pageTemplateCategory(@Param("searchParam") String searchParam);

    Integer checkTemplateCategoryName(@Param("sourceId") Long sourceId,
                                      @Param("name") String name,
                                      @Param("ciTemplateCategoryId") Long ciTemplateCategoryId);

}
