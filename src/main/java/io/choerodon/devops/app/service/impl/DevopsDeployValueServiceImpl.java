package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.dto.DevopsDeployValueDTO;
import io.choerodon.devops.app.service.DevopsDeployValueService;
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE;
import io.choerodon.devops.domain.application.entity.DevopsDeployValueE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository;
import io.choerodon.devops.domain.application.repository.DevopsDeployValueRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvUserPermissionRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployRepository;
import io.choerodon.devops.infra.common.util.EnvUtil;

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
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;

    @Override
    public DevopsDeployValueDTO createOrUpdate(Long projectId, DevopsDeployValueDTO pipelineValueDTO) {
        DevopsDeployValueE pipelineValueE = ConvertHelper.convert(pipelineValueDTO, DevopsDeployValueE.class);
        pipelineValueE.setProjectId(projectId);
        pipelineValueE = valueRepository.createOrUpdate(pipelineValueE);
        return ConvertHelper.convert(pipelineValueE, DevopsDeployValueDTO.class);
    }

    @Override
    public void delete(Long projectId, Long valueId) {
        valueRepository.delete(valueId);
    }

    @Override
    public PageInfo<DevopsDeployValueDTO> listByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params) {
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList();

        PageInfo<DevopsDeployValueDTO> valueDTOS = ConvertPageHelper.convertPageInfo(valueRepository.listByOptions(projectId, appId, envId, DetailsHelper.getUserDetails().getUserId(), pageRequest, params), DevopsDeployValueDTO.class);
        PageInfo<DevopsDeployValueDTO> page = new PageInfo<>();
        BeanUtils.copyProperties(valueDTOS, page);
        page.setList(valueDTOS.getList().stream().peek(t -> {
            UserE userE = iamRepository.queryUserByUserId(t.getCreateBy());
            t.setCreateUserName(userE.getLoginName());
            t.setCreateUserUrl(userE.getImageUrl());
            t.setCreateUserRealName(userE.getRealName());
            DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(t.getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                t.setEnvStatus(true);
            }
            t.setIndex(checkDelete(projectId,t.getId()));
        }).collect(Collectors.toList()));
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
        List<ApplicationInstanceE> instanceEList = applicationInstanceRepository.listByValueId(valueId);
        return appDeployEList == null || appDeployEList.isEmpty() || instanceEList == null || instanceEList.isEmpty();
    }
}
