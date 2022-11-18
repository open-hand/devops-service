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
        List<DevopsCiHostDeployInfoDTO> DevopsCiHostDeployInfoDTOList = devopsCiHostDeployInfoMapper.selectByHostAppId(dockerDeployVO.getHostAppId());
        if (CollectionUtils.isEmpty(DevopsCiHostDeployInfoDTOList)) {
            return;
        }

        DevopsCiHostDeployInfoDTOList.forEach(DevopsCiHostDeployInfoDTO -> {
            DevopsCiHostDeployInfoDTO.setDockerCommand(dockerDeployVO.getValue());

            DevopsCiHostDeployInfoVO.ImageDeploy imageDeploy = JsonHelper.unmarshalByJackson(DevopsCiHostDeployInfoDTO.getDeployJson(), DevopsCiHostDeployInfoVO.ImageDeploy.class);
            imageDeploy.setContainerName(dockerDeployVO.getContainerName());

            DevopsCiHostDeployInfoDTO.setDeployJson(JsonHelper.marshalByJackson(imageDeploy));
            DevopsCiHostDeployInfoDTO.setAppName(dockerDeployVO.getAppName());

            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiHostDeployInfoMapper, DevopsCiHostDeployInfoDTO, DEVOPS_UPDATE_PIPELINE_DOCKER_DEPLOY_INFO);
        });

    }

    @Override
    public void updateJarDeployInfoFromAppCenter(JarDeployVO jarDeployVO) {
        List<DevopsCiHostDeployInfoDTO> DevopsCiHostDeployInfoDTOList = devopsCiHostDeployInfoMapper.selectByHostAppId(jarDeployVO.getAppId());
        if (CollectionUtils.isEmpty(DevopsCiHostDeployInfoDTOList)) {
            return;
        }
        DevopsCiHostDeployInfoDTOList.forEach(DevopsCiHostDeployInfoDTO -> {
            DevopsCiHostDeployInfoDTO.setAppName(jarDeployVO.getAppName());
            DevopsCiHostDeployInfoDTO.setPreCommand(jarDeployVO.getPreCommand());
            DevopsCiHostDeployInfoDTO.setRunCommand(jarDeployVO.getRunCommand());
            DevopsCiHostDeployInfoDTO.setPostCommand(jarDeployVO.getPostCommand());
            DevopsCiHostDeployInfoDTO.setKillCommand(jarDeployVO.getKillCommand());
            DevopsCiHostDeployInfoDTO.setHealthProb(jarDeployVO.getHealthProb());

            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiHostDeployInfoMapper, DevopsCiHostDeployInfoDTO, DEVOPS_UPDATE_PIPELINE_JAR_DEPLOY_INFO);

        });
    }

    @Override
    public void updateCustomDeployInfoFromAppCenter(CustomDeployVO customDeployVO) {
        List<DevopsCiHostDeployInfoDTO> DevopsCiHostDeployInfoDTOList = devopsCiHostDeployInfoMapper.selectByHostAppId(customDeployVO.getAppId());
        if (CollectionUtils.isEmpty(DevopsCiHostDeployInfoDTOList)) {
            return;
        }
        DevopsCiHostDeployInfoDTOList.forEach(DevopsCiHostDeployInfoDTO -> {
            DevopsCiHostDeployInfoDTO.setAppName(customDeployVO.getAppName());
            DevopsCiHostDeployInfoDTO.setPreCommand(customDeployVO.getPreCommand());
            DevopsCiHostDeployInfoDTO.setRunCommand(customDeployVO.getRunCommand());
            DevopsCiHostDeployInfoDTO.setPostCommand(customDeployVO.getPostCommand());
            DevopsCiHostDeployInfoDTO.setKillCommand(customDeployVO.getKillCommand());
            DevopsCiHostDeployInfoDTO.setHealthProb(customDeployVO.getHealthProb());

            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiHostDeployInfoMapper, DevopsCiHostDeployInfoDTO, DEVOPS_UPDATE_PIPELINE_CUSTOM_DEPLOY_INFO);
        });
    }
}
