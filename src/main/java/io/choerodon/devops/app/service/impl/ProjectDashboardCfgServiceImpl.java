package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.dashboard.ProjectDashboardCfgVO;
import io.choerodon.devops.app.service.ProjectDashboardCfgService;
import io.choerodon.devops.app.service.ProjectDashboardCfgTargetService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.ProjectDashboardCfgDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.ProjectDashboardCfgMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 项目质量评分配置表(ProjectDashboardCfg)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-12 14:38:58
 */
@Service
public class ProjectDashboardCfgServiceImpl implements ProjectDashboardCfgService {

    private static final String DEVOPS_SAVE_DASHBOARD_CFG_FAILED = "devops.save.dashboard.cfg.failed";

    @Autowired
    private ProjectDashboardCfgMapper projectDashboardCfgMapper;
    @Autowired
    private ProjectDashboardCfgTargetService projectDashboardCfgTargetService;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public ProjectDashboardCfgVO queryByOrganizationIdId(Long organizationId) {
        ProjectDashboardCfgDTO projectDashboardCfgDTO = baseQueryByOrgId(organizationId);
        if (projectDashboardCfgDTO == null) {
            return queryDefaultConfig(organizationId);
        } else {
            ProjectDashboardCfgVO projectDashboardCfgVO = ConvertUtils.convertObject(projectDashboardCfgDTO, ProjectDashboardCfgVO.class);
            projectDashboardCfgVO.setProjectIds(projectDashboardCfgTargetService.listProjectIdsByCfgId(projectDashboardCfgDTO.getId()));
            return projectDashboardCfgVO;
        }
    }

    @Override
    public ProjectDashboardCfgVO queryDefaultConfig(Long organizationId) {
        ProjectDashboardCfgVO projectDashboardCfgVO = new ProjectDashboardCfgVO();
        projectDashboardCfgVO.setPassScore(80.0);
        projectDashboardCfgVO.setBugWeight(45L);
        projectDashboardCfgVO.setVulnerabilityWeight(45L);
        projectDashboardCfgVO.setCodeSmellWeight(10L);
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.listManagedProjects(organizationId);
        if (!CollectionUtils.isEmpty(projectDTOS)) {
            List<Long> projectIds = projectDTOS.stream().map(ProjectDTO::getId).collect(Collectors.toList());
            projectDashboardCfgVO.setProjectIds(projectIds);
        } else {
            projectDashboardCfgVO.setProjectIds(new ArrayList<>());
        }
        return projectDashboardCfgVO;
    }

    @Override
    public ProjectDashboardCfgDTO baseQueryByOrgId(Long organizationId) {
        Assert.notNull(organizationId, ResourceCheckConstant.DEVOPS_ORGANIZATION_ID_IS_NULL);
        ProjectDashboardCfgDTO record = new ProjectDashboardCfgDTO();
        record.setTenantId(organizationId);
        return projectDashboardCfgMapper.selectOne(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDashboardCfgDTO saveOrUpdateCfg(Long organizationId, ProjectDashboardCfgVO projectDashboardCfgVO) {
        ProjectDashboardCfgDTO projectDashboardCfgDTO = baseQueryByOrgId(organizationId);
        checkProp(projectDashboardCfgVO);
        if (projectDashboardCfgDTO == null) {
            projectDashboardCfgDTO = ConvertUtils.convertObject(projectDashboardCfgVO, ProjectDashboardCfgDTO.class);
            projectDashboardCfgDTO.setTenantId(organizationId);
            MapperUtil.resultJudgedInsertSelective(projectDashboardCfgMapper, projectDashboardCfgDTO, DEVOPS_SAVE_DASHBOARD_CFG_FAILED);
        } else {
            projectDashboardCfgDTO.setPassScore(projectDashboardCfgVO.getPassScore());
            projectDashboardCfgDTO.setVulnerabilityWeight(projectDashboardCfgVO.getVulnerabilityWeight());
            projectDashboardCfgDTO.setBugWeight(projectDashboardCfgVO.getBugWeight());
            projectDashboardCfgDTO.setCodeSmellWeight(projectDashboardCfgVO.getCodeSmellWeight());
            projectDashboardCfgMapper.updateByPrimaryKeySelective(projectDashboardCfgDTO);
        }
        // 更新项目范围
        projectDashboardCfgTargetService.deleteByCfgId(projectDashboardCfgDTO.getId());
        projectDashboardCfgTargetService.batchSave(projectDashboardCfgDTO.getId(), projectDashboardCfgVO.getProjectIds());
        return projectDashboardCfgDTO;
    }

    private void checkProp(ProjectDashboardCfgVO projectDashboardCfgVO) {
        if (projectDashboardCfgVO.getBugWeight() + projectDashboardCfgVO.getVulnerabilityWeight() + projectDashboardCfgVO.getCodeSmellWeight() != 100) {
            throw new CommonException("devops.weight.invalid");
        }
    }
}

