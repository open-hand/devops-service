package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsAutoDeployDTO;
import io.choerodon.devops.api.dto.DevopsAutoDeployRecordDTO;
import io.choerodon.devops.app.service.DevopsAutoDeployService;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployE;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployValueE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:33 2019/2/25
 * Description:
 */
@Service
public class DevopsAutoDeployServiceImpl implements DevopsAutoDeployService {
    private static final String STATUS_DEL = "deleted";

    @Autowired
    private DevopsAutoDeployRepository devopsAutoDeployRepository;
    @Autowired
    private DevopsAutoDeployRecordRepository devopsAutoDeployRecordRepository;
    @Autowired
    private DevopsAutoDeployValueRepository devopsAutoDeployValueRepository;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private DevopsEnvironmentRepository devopsEnviromentRepository;
    @Autowired
    private IamRepository iamRepository;

    @Override
    public DevopsAutoDeployDTO createOrUpdate(Long projectId, DevopsAutoDeployDTO devopsAutoDeployDTO) {
        //校验taskName
        devopsAutoDeployRepository.checkTaskName(devopsAutoDeployDTO.getId(), projectId, devopsAutoDeployDTO.getTaskName());
        DevopsAutoDeployE devopsAutoDeployE = ConvertHelper.convert(devopsAutoDeployDTO, DevopsAutoDeployE.class);
        devopsAutoDeployE.setProjectId(projectId);
        //保存value信息
        DevopsAutoDeployValueE devopsAutoDeployValueE = new DevopsAutoDeployValueE(devopsAutoDeployE.getValue());
        if (devopsAutoDeployE.getId() != null && devopsAutoDeployE.getValueId() != null) {
            devopsAutoDeployValueE.setId(devopsAutoDeployE.getValueId());
        }
        devopsAutoDeployE.setValueId(devopsAutoDeployValueRepository.createOrUpdate(devopsAutoDeployValueE));
        //保存autoDeploy信息
        return ConvertHelper.convert(devopsAutoDeployRepository.createOrUpdate(devopsAutoDeployE), DevopsAutoDeployDTO.class);
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long autoDeployId) {
        devopsAutoDeployRecordRepository.updateStatus(autoDeployId, STATUS_DEL);
        devopsAutoDeployRepository.delete(autoDeployId);
    }

    @Override
    public Page<DevopsAutoDeployDTO> listByOptions(Long projectId, Long userId, Long appId, Long envId, Boolean doPage, PageRequest pageRequest, String params) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        //判断当前用户是否是项目所有者
        userId = iamRepository.isProjectOwner(userId, projectE) ? null : userId;
        Page<DevopsAutoDeployDTO> devopsAutoDeployDTOS = ConvertPageHelper.convertPage(devopsAutoDeployRepository.listByOptions(projectId, userId, appId, envId, doPage, pageRequest, params), DevopsAutoDeployDTO.class);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList();
        devopsAutoDeployDTOS.forEach(autoDeployE -> {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(autoDeployE.getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                autoDeployE.setEnvStatus(true);
            }
        });
        return devopsAutoDeployDTOS;
    }

    @Override
    public List<DevopsAutoDeployDTO> queryByProjectId(Long projectId, Long userId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        userId = iamRepository.isProjectOwner(userId, projectE) ? null : userId;
        return ConvertHelper.convertList(devopsAutoDeployRepository.queryByProjectId(projectId, userId), DevopsAutoDeployDTO.class);
    }

    @Override
    public Page<DevopsAutoDeployRecordDTO> queryRecords(Long projectId, Long userId, Long appId, Long envId, String taskName, Boolean doPage, PageRequest pageRequest, String params) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        userId = iamRepository.isProjectOwner(userId, projectE) ? null : userId;
        Page<DevopsAutoDeployRecordDTO> devopsAutoDeployRecordDTOS = ConvertPageHelper.convertPage(devopsAutoDeployRecordRepository.listByOptions(projectId, userId, appId, envId, taskName, doPage, pageRequest, params), DevopsAutoDeployRecordDTO.class);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList();
        devopsAutoDeployRecordDTOS.forEach(autoDeployE -> {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(autoDeployE.getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                autoDeployE.setEnvStatus(true);
            }
        });
        return devopsAutoDeployRecordDTOS;
    }

    @Override
    public DevopsAutoDeployDTO queryById(Long projectId, Long autoDeployId) {
        return ConvertHelper.convert(devopsAutoDeployRepository.queryById(autoDeployId), DevopsAutoDeployDTO.class);
    }

    @Override
    public void checkName(Long id, Long projectId, String name) {
        devopsAutoDeployRepository.checkTaskName(id, projectId, name);
    }

    @Override
    public DevopsAutoDeployDTO updateIsEnabled(Long autoDeployId, Integer isEnabled) {
        return ConvertHelper.convert(devopsAutoDeployRepository.updateIsEnabled(autoDeployId, isEnabled), DevopsAutoDeployDTO.class);
    }
}
