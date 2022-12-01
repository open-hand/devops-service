package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CiHostDeployCode.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.deploy.CustomDeployVO;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiHostDeployInfoVO;
import io.choerodon.devops.app.service.DevopsCiHostDeployInfoService;
import io.choerodon.devops.infra.dto.DevopsCiHostDeployInfoDTO;
import io.choerodon.devops.infra.mapper.DevopsCiHostDeployInfoMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCiHostDeployInfoServiceImpl implements DevopsCiHostDeployInfoService {
    @Autowired
    private DevopsCiHostDeployInfoMapper devopsCiHostDeployInfoMapper;


    @Override
    public void baseUpdate(DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO) {
        devopsCiHostDeployInfoMapper.updateByPrimaryKeySelective(devopsCiHostDeployInfoDTO);
    }

    @Override
    public DevopsCiHostDeployInfoDTO selectByPrimaryKey(Long configId) {
        return devopsCiHostDeployInfoMapper.selectByPrimaryKey(configId);
    }

    @Override
    public void updateDockerDeployInfoFromAppCenter(DockerDeployVO dockerDeployVO) {
        List<DevopsCiHostDeployInfoDTO> devopsCiHostDeployInfoDTOList = devopsCiHostDeployInfoMapper.selectByHostAppId(dockerDeployVO.getHostAppId());
        if (CollectionUtils.isEmpty(devopsCiHostDeployInfoDTOList)) {
            return;
        }

        devopsCiHostDeployInfoDTOList.forEach(devopsCiHostDeployInfoDTO -> {
            devopsCiHostDeployInfoDTO.setDockerCommand(dockerDeployVO.getValue());

            DevopsCiHostDeployInfoVO.ImageDeploy imageDeploy = JsonHelper.unmarshalByJackson(devopsCiHostDeployInfoDTO.getDeployJson(), DevopsCiHostDeployInfoVO.ImageDeploy.class);
            imageDeploy.setContainerName(dockerDeployVO.getContainerName());

            devopsCiHostDeployInfoDTO.setDeployJson(JsonHelper.marshalByJackson(imageDeploy));
            devopsCiHostDeployInfoDTO.setAppName(dockerDeployVO.getAppName());

            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiHostDeployInfoMapper, devopsCiHostDeployInfoDTO, DEVOPS_UPDATE_PIPELINE_DOCKER_DEPLOY_INFO);
        });

    }

    @Override
    public void updateJarDeployInfoFromAppCenter(JarDeployVO jarDeployVO) {
        List<DevopsCiHostDeployInfoDTO> devopsCiHostDeployInfoDTOList = devopsCiHostDeployInfoMapper.selectByHostAppId(jarDeployVO.getAppId());
        if (CollectionUtils.isEmpty(devopsCiHostDeployInfoDTOList)) {
            return;
        }
        devopsCiHostDeployInfoDTOList.forEach(devopsCiHostDeployInfoDTO -> {
            devopsCiHostDeployInfoDTO.setAppName(jarDeployVO.getAppName());
            devopsCiHostDeployInfoDTO.setPreCommand(jarDeployVO.getPreCommand());
            devopsCiHostDeployInfoDTO.setRunCommand(jarDeployVO.getRunCommand());
            devopsCiHostDeployInfoDTO.setPostCommand(jarDeployVO.getPostCommand());
            devopsCiHostDeployInfoDTO.setKillCommand(jarDeployVO.getKillCommand());
            devopsCiHostDeployInfoDTO.setHealthProb(jarDeployVO.getHealthProb());

            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiHostDeployInfoMapper, devopsCiHostDeployInfoDTO, DEVOPS_UPDATE_PIPELINE_JAR_DEPLOY_INFO);

        });
    }

    @Override
    public void updateCustomDeployInfoFromAppCenter(CustomDeployVO customDeployVO) {
        List<DevopsCiHostDeployInfoDTO> devopsCiHostDeployInfoDTOList = devopsCiHostDeployInfoMapper.selectByHostAppId(customDeployVO.getAppId());
        if (CollectionUtils.isEmpty(devopsCiHostDeployInfoDTOList)) {
            return;
        }
        devopsCiHostDeployInfoDTOList.forEach(devopsCiHostDeployInfoDTO -> {
            devopsCiHostDeployInfoDTO.setAppName(customDeployVO.getAppName());
            devopsCiHostDeployInfoDTO.setPreCommand(customDeployVO.getPreCommand());
            devopsCiHostDeployInfoDTO.setRunCommand(customDeployVO.getRunCommand());
            devopsCiHostDeployInfoDTO.setPostCommand(customDeployVO.getPostCommand());
            devopsCiHostDeployInfoDTO.setKillCommand(customDeployVO.getKillCommand());
            devopsCiHostDeployInfoDTO.setHealthProb(customDeployVO.getHealthProb());

            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiHostDeployInfoMapper, devopsCiHostDeployInfoDTO, DEVOPS_UPDATE_PIPELINE_CUSTOM_DEPLOY_INFO);
        });
    }
}
