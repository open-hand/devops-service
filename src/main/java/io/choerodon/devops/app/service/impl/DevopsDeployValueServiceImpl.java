package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsDeployValueDTO;
import io.choerodon.devops.app.service.DevopsDeployValueService;
import io.choerodon.devops.domain.application.entity.DevopsDeployValueE;
import io.choerodon.devops.domain.application.entity.DevopsEnvUserPermissionE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.DevopsDeployValueRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvUserPermissionRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployRepository;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:01 2019/4/10
 * Description:
 */
@Service
public class DevopsDeployValueServiceImpl implements DevopsDeployValueService {
    @Autowired
    private DevopsDeployValueRepository valueRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private DevopsEnvironmentRepository devopsEnviromentRepository;
    @Autowired
    private PipelineAppDeployRepository appDeployRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;

    @Override
    public DevopsDeployValueDTO createOrUpdate(Long projectId, DevopsDeployValueDTO pipelineValueDTO) {
        DevopsDeployValueE pipelineValueE = ConvertHelper.convert(pipelineValueDTO, DevopsDeployValueE.class);
        pipelineValueE.setProjectId(projectId);
        if (pipelineValueE.getId() == null) {
            valueRepository.checkName(projectId, pipelineValueE.getName());
        }
        pipelineValueE = valueRepository.createOrUpdate(pipelineValueE);
        return ConvertHelper.convert(pipelineValueE, DevopsDeployValueDTO.class);
    }

    @Override
    public void delete(Long projectId, Long valueId) {
        valueRepository.delete(valueId);
    }

    @Override
    public Page<DevopsDeployValueDTO> listByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList();

        Page<DevopsDeployValueDTO> valueDTOS = ConvertPageHelper.convertPage(valueRepository.listByOptions(projectId, appId, envId, pageRequest, params), DevopsDeployValueDTO.class);
        Page<DevopsDeployValueDTO> page = new Page<>();
        BeanUtils.copyProperties(valueDTOS, page);
        List<DevopsDeployValueDTO> deployValueDTOS = new ArrayList<>();
        valueDTOS.getContent().forEach(t -> {
            UserE userE = iamRepository.queryUserByUserId(t.getCreateBy());
            t.setCreateUserName(userE.getLoginName());
            t.setCreateUserUrl(userE.getImageUrl());
            t.setCreateUserRealName(userE.getRealName());
            DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(t.getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                t.setEnvStatus(true);
            }
            if (!iamRepository.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE)) {
                List<Long> envIds = devopsEnvUserPermissionRepository
                        .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                        .filter(DevopsEnvUserPermissionE::getPermitted)
                        .map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
                if (envIds.contains(GitUserNameUtil.getUserId().longValue())) {
                    deployValueDTOS.add(t);
                }
            } else {
                deployValueDTOS.add(t);
            }
        });
        page.setContent(deployValueDTOS);
        return page;
    }

    @Override
    public DevopsDeployValueDTO queryById(Long projectId, Long valueId) {
        DevopsDeployValueDTO valueDTO = ConvertHelper.convert(valueRepository.queryById(valueId), DevopsDeployValueDTO.class);
        valueDTO.setIndex(checkDelete(projectId, valueId));
        return valueDTO;
    }

    @Override
    public void checkName(Long projectId, String name) {
        valueRepository.checkName(projectId, name);
    }

    @Override
    public List<DevopsDeployValueDTO> queryByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(valueRepository.queryByAppIdAndEnvId(projectId, appId, envId), DevopsDeployValueDTO.class);
    }

    @Override
    public Boolean checkDelete(Long projectId, Long valueId) {
        List<PipelineAppDeployE> appDeployEList = appDeployRepository.queryByValueId(valueId);
        return appDeployEList == null || appDeployEList.isEmpty();
    }
}
