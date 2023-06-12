package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.ProjectDashboardCfgTargetService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.ProjectDashboardCfgTargetDTO;
import io.choerodon.devops.infra.mapper.ProjectDashboardCfgTargetMapper;

/**
 * 项目质量评分配置对象表(ProjectDashboardCfgTarget)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-12 14:38:59
 */
@Service
public class ProjectDashboardCfgTargetServiceImpl implements ProjectDashboardCfgTargetService {
    @Autowired
    private ProjectDashboardCfgTargetMapper projectDashboardCfgTargetMapper;

    @Override
    public List<ProjectDashboardCfgTargetDTO> listByCfgId(Long cfgId) {
        Assert.notNull(cfgId, ResourceCheckConstant.DEVOPS_ID_IS_NULL);

        ProjectDashboardCfgTargetDTO record = new ProjectDashboardCfgTargetDTO();
        record.setCfgId(cfgId);
        return projectDashboardCfgTargetMapper.select(record);
    }

    @Override
    public List<Long> listProjectIdsByCfgId(Long cfgId) {
        List<ProjectDashboardCfgTargetDTO> projectDashboardCfgTargetDTOS = listByCfgId(cfgId);
        if (CollectionUtils.isEmpty(projectDashboardCfgTargetDTOS)) {
            return new ArrayList<>();
        }
        return projectDashboardCfgTargetDTOS.stream().map(ProjectDashboardCfgTargetDTO::getProjectId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCfgId(Long cfgId) {
        Assert.notNull(cfgId, ResourceCheckConstant.DEVOPS_ID_IS_NULL);
        ProjectDashboardCfgTargetDTO record = new ProjectDashboardCfgTargetDTO();
        record.setCfgId(cfgId);
        projectDashboardCfgTargetMapper.delete(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long cfgId, List<Long> projectIds) {
        projectDashboardCfgTargetMapper.batchSave(cfgId, projectIds);
    }
}

