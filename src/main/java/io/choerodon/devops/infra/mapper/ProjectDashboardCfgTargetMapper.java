package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.ProjectDashboardCfgTargetDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 项目质量评分配置对象表(ProjectDashboardCfgTarget)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-12 14:38:58
 */
public interface ProjectDashboardCfgTargetMapper extends BaseMapper<ProjectDashboardCfgTargetDTO> {
    void batchSave(@Param("cfgId") Long cfgId,
                   @Param("projectIds") List<Long> projectIds);
}

