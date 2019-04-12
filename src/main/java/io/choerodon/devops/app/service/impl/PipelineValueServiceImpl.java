package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.PipelineValueDTO;
import io.choerodon.devops.app.service.PipelineValueService;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.PipelineValueE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployValueRepository;
import io.choerodon.devops.domain.application.repository.PipelineValueRepository;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:01 2019/4/10
 * Description:
 */
@Service
public class PipelineValueServiceImpl implements PipelineValueService {
    @Autowired
    private PipelineValueRepository valueRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private DevopsEnvironmentRepository devopsEnviromentRepository;
    @Autowired
    private PipelineAppDeployValueRepository appDeployValueRepository;

    @Override
    public PipelineValueDTO createOrUpdate(PipelineValueDTO pipelineValueDTO) {
        PipelineValueE pipelineValueE = ConvertHelper.convert(pipelineValueDTO, PipelineValueE.class);
        pipelineValueE = valueRepository.createOrUpdate(pipelineValueE);
        return ConvertHelper.convert(pipelineValueE, PipelineValueDTO.class);
    }

    @Override
    public Boolean delete(Long projectId, Long valueId) {
        if (appDeployValueRepository.queryByValueId(valueId).size() == 0) {
            valueRepository.delete(valueId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Page<PipelineValueDTO> listByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params) {
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList();

        Page<PipelineValueDTO> valueDTOS = ConvertPageHelper.convertPage(valueRepository.listByOptions(projectId, appId, envId, pageRequest, params), PipelineValueDTO.class);
        Page<PipelineValueDTO> page = new Page<>();
        page.setContent(valueDTOS.getContent().stream().peek(t -> {
            UserE userE = iamRepository.queryUserByUserId(t.getCreateBy());
            t.setCreateUserName(userE.getLoginName());
            t.setCreateUserUrl(userE.getImageUrl());
            t.setCreateUserRealName(userE.getRealName());
            DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(t.getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                t.setEnvStatus(true);
            }
        }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public PipelineValueDTO queryById(Long valueId) {
        PipelineValueDTO valueDTO = ConvertHelper.convert(valueRepository.queryById(valueId), PipelineValueDTO.class);
        valueDTO.setIndex(appDeployValueRepository.queryByValueId(valueId).size() == 0);
        return valueDTO;
    }

    @Override
    public void checkName(Long projectId, String name) {
        valueRepository.checkName(projectId, name);
    }
}
