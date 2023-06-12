package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.dashboard.ProjectDashboardCfgVO;
import io.choerodon.devops.infra.dto.ProjectDashboardCfgDTO;

/**
 * 项目质量评分配置表(ProjectDashboardCfg)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-12 14:38:58
 */
public interface ProjectDashboardCfgService {

    ProjectDashboardCfgVO queryByOrganizationId(Long organizationId);

    ProjectDashboardCfgVO queryDefaultConfig(Long organizationId);

    ProjectDashboardCfgDTO baseQueryByOrgId(Long organizationId);

    ProjectDashboardCfgDTO saveOrUpdateCfg(Long organizationId, ProjectDashboardCfgVO projectDashboardCfgVO);
}

