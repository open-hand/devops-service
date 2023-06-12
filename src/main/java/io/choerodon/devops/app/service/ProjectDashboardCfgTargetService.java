package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.ProjectDashboardCfgTargetDTO;

/**
 * 项目质量评分配置对象表(ProjectDashboardCfgTarget)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-12 14:38:58
 */
public interface ProjectDashboardCfgTargetService {

    List<ProjectDashboardCfgTargetDTO> listByCfgId(Long cfgId);

    List<Long> listProjectIdsByCfgId(Long cfgId);

    void deleteByCfgId(Long cfgId);

    void batchSave(Long cfgId, List<Long> projectIds);
}

