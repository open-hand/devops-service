package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.dashboard.ProjectDashboardCfgVO;
import io.choerodon.devops.app.service.ProjectDashboardCfgService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.ProjectDashboardCfgDTO;
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


    @Override
    public ProjectDashboardCfgVO queryByOrganizationId(Long organizationId) {
        ProjectDashboardCfgDTO projectDashboardCfgDTO = baseQueryByOrgId(organizationId);
        if (projectDashboardCfgDTO == null) {
            return queryDefaultConfig(organizationId);
        } else {
            return ConvertUtils.convertObject(projectDashboardCfgDTO, ProjectDashboardCfgVO.class);
        }
    }

    @Override
    public ProjectDashboardCfgVO queryDefaultConfig(Long organizationId) {
        ProjectDashboardCfgVO projectDashboardCfgVO = new ProjectDashboardCfgVO();
        projectDashboardCfgVO.setPassScore(80.0);
        projectDashboardCfgVO.setCodeWeight(45L);
        projectDashboardCfgVO.setVulnWeight(45L);
        projectDashboardCfgVO.setK8sWeight(10L);
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
            projectDashboardCfgDTO.setVulnWeight(projectDashboardCfgVO.getVulnWeight());
            projectDashboardCfgDTO.setK8sWeight(projectDashboardCfgVO.getK8sWeight());
            projectDashboardCfgDTO.setCodeWeight(projectDashboardCfgVO.getCodeWeight());
            projectDashboardCfgMapper.updateByPrimaryKeySelective(projectDashboardCfgDTO);
        }
        return projectDashboardCfgDTO;
    }

    private void checkProp(ProjectDashboardCfgVO projectDashboardCfgVO) {
        if (projectDashboardCfgVO.getVulnWeight() + projectDashboardCfgVO.getCodeWeight() + projectDashboardCfgVO.getK8sWeight() != 100) {
            throw new CommonException("devops.weight.invalid");
        }
    }
}

