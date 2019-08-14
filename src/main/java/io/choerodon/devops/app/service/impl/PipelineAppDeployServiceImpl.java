package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.PipelineAppServiceDeployDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.PipelineAppDeployService;
import io.choerodon.devops.infra.mapper.PipelineAppServiceDeployMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:05 2019/7/15
 * Description:
 */
@Service
public class PipelineAppDeployServiceImpl implements PipelineAppDeployService {

    @Autowired
    private PipelineAppServiceDeployMapper appDeployMapper;
    @Autowired
    DevopsEnvironmentService devopsEnvironmentService;

    @Override
    public PipelineAppServiceDeployDTO baseCreate(PipelineAppServiceDeployDTO pipelineAppServiceDeployDTO) {
        if (appDeployMapper.insert(pipelineAppServiceDeployDTO) != 1) {
            throw new CommonException("error.insert.pipeline.app.deploy");
        }
        return pipelineAppServiceDeployDTO;
    }

    @Override
    public PipelineAppServiceDeployDTO baseUpdate(PipelineAppServiceDeployDTO pipelineAppServiceDeployDTO) {
        if (appDeployMapper.updateByPrimaryKeySelective(pipelineAppServiceDeployDTO) != 1) {
            throw new CommonException("error.update.pipeline.app.deploy");
        }
        return pipelineAppServiceDeployDTO;
    }

    @Override
    public void baseDeleteById(Long appDelpoyId) {
        appDeployMapper.deleteByPrimaryKey(appDelpoyId);
    }

    @Override
    public PipelineAppServiceDeployDTO baseQueryById(Long appDelpoyId) {
        return appDeployMapper.queryById(appDelpoyId);
    }

    @Override
    public List<PipelineAppServiceDeployDTO> baseQueryByAppId(Long appServiceId) {
        PipelineAppServiceDeployDTO pipelineAppServiceDeployDTO = new PipelineAppServiceDeployDTO();
        pipelineAppServiceDeployDTO.setAppServiceId(appServiceId);
        return appDeployMapper.select(pipelineAppServiceDeployDTO);
    }

    @Override
    public void baseCheckName(String name, Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        List<Long> envIds = devopsEnvironmentService.baseListByClusterId(devopsEnvironmentDTO.getClusterId()).stream().map(DevopsEnvironmentDTO::getId).collect(Collectors.toList());
        if(appDeployMapper.checkNameExist(name,envIds)) {
            throw new CommonException("error.app.instance.name.already.exist");
        }
    }

    @Override
    public List<PipelineAppServiceDeployDTO> baseQueryByValueId(Long valueId) {
        PipelineAppServiceDeployDTO appDeployDO = new PipelineAppServiceDeployDTO();
        appDeployDO.setValueId(valueId);
        return appDeployMapper.select(appDeployDO);
    }

    @Override
    public List<PipelineAppServiceDeployDTO> baseQueryByEnvId(Long envId) {
        PipelineAppServiceDeployDTO appDeployDO = new PipelineAppServiceDeployDTO();
        appDeployDO.setEnvId(envId);
        return appDeployMapper.select(appDeployDO);
    }

    @Override
    public void baseUpdateWithInstanceId(Long instanceId) {
        appDeployMapper.updateInstanceId(instanceId);
    }
}
