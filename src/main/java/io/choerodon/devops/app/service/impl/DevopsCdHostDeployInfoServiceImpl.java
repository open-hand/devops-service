package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.CdHostDeployConfigVO;
import io.choerodon.devops.api.vo.deploy.CustomDeployVO;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.app.service.DevopsCdHostDeployInfoService;
import io.choerodon.devops.infra.dto.DevopsCdHostDeployInfoDTO;
import io.choerodon.devops.infra.mapper.DevopsCdHostDeployInfoMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/14 10:02
 */
@Service
public class DevopsCdHostDeployInfoServiceImpl implements DevopsCdHostDeployInfoService {
    private static final String DEVOPS_SAVE_CD_HOST_DEPLOY_INFO_FAILED = "devops.save.cd.host.deploy.info.failed";
    private static final String DEVOPS_UPDATE_PIPELINE_DOCKER_DEPLOY_INFO = "devops.update.pipeline.docker.deploy.info";
    private static final String DEVOPS_UPDATE_PIPELINE_JAR_DEPLOY_INFO = "devops.update.pipeline.jar.deploy.info";
    private static final String DEVOPS_UPDATE_PIPELINE_CUSTOM_DEPLOY_INFO = "devops.update.pipeline.custom.deploy.info";

    @Autowired
    private DevopsCdHostDeployInfoMapper devopsCdHostDeployInfoMapper;

    @Override
    @Transactional
    public DevopsCdHostDeployInfoDTO baseCreate(DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCdHostDeployInfoMapper, devopsCdHostDeployInfoDTO, DEVOPS_SAVE_CD_HOST_DEPLOY_INFO_FAILED);
        return devopsCdHostDeployInfoMapper.selectByPrimaryKey(devopsCdHostDeployInfoDTO.getId());
    }

    @Override
    public DevopsCdHostDeployInfoDTO queryById(Long id) {
        return devopsCdHostDeployInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO) {
        devopsCdHostDeployInfoMapper.updateByPrimaryKeySelective(devopsCdHostDeployInfoDTO);
    }

    @Override
    public void updateDockerDeployInfoFromAppCenter(DockerDeployVO dockerDeployVO) {
        List<DevopsCdHostDeployInfoDTO> devopsCdHostDeployInfoDTOList = devopsCdHostDeployInfoMapper.selectByHostAppId(dockerDeployVO.getHostAppId());
        if (CollectionUtils.isEmpty(devopsCdHostDeployInfoDTOList)) {
            return;
        }

        devopsCdHostDeployInfoDTOList.forEach(devopsCdHostDeployInfoDTO -> {
            devopsCdHostDeployInfoDTO.setDockerCommand(dockerDeployVO.getValue());

            CdHostDeployConfigVO.ImageDeploy imageDeploy = JsonHelper.unmarshalByJackson(devopsCdHostDeployInfoDTO.getDeployJson(), CdHostDeployConfigVO.ImageDeploy.class);
            imageDeploy.setContainerName(dockerDeployVO.getContainerName());

            devopsCdHostDeployInfoDTO.setDeployJson(JsonHelper.marshalByJackson(imageDeploy));
            devopsCdHostDeployInfoDTO.setAppName(dockerDeployVO.getAppName());

            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCdHostDeployInfoMapper, devopsCdHostDeployInfoDTO, DEVOPS_UPDATE_PIPELINE_DOCKER_DEPLOY_INFO);
        });

    }

    @Override
    public void updateJarDeployInfoFromAppCenter(JarDeployVO jarDeployVO) {
        List<DevopsCdHostDeployInfoDTO> devopsCdHostDeployInfoDTOList = devopsCdHostDeployInfoMapper.selectByHostAppId(jarDeployVO.getAppId());
        if (CollectionUtils.isEmpty(devopsCdHostDeployInfoDTOList)) {
            return;
        }
        devopsCdHostDeployInfoDTOList.forEach(devopsCdHostDeployInfoDTO -> {
            devopsCdHostDeployInfoDTO.setAppName(jarDeployVO.getAppName());
            devopsCdHostDeployInfoDTO.setPreCommand(jarDeployVO.getPreCommand());
            devopsCdHostDeployInfoDTO.setRunCommand(jarDeployVO.getRunCommand());
            devopsCdHostDeployInfoDTO.setPostCommand(jarDeployVO.getPostCommand());
            devopsCdHostDeployInfoDTO.setKillCommand(jarDeployVO.getKillCommand());
            devopsCdHostDeployInfoDTO.setHealthProb(jarDeployVO.getHealthProb());

            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCdHostDeployInfoMapper, devopsCdHostDeployInfoDTO, DEVOPS_UPDATE_PIPELINE_JAR_DEPLOY_INFO);

        });
    }

    @Override
    public void updateCustomDeployInfoFromAppCenter(CustomDeployVO customDeployVO) {
        List<DevopsCdHostDeployInfoDTO> devopsCdHostDeployInfoDTOList = devopsCdHostDeployInfoMapper.selectByHostAppId(customDeployVO.getAppId());
        if (CollectionUtils.isEmpty(devopsCdHostDeployInfoDTOList)) {
            return;
        }
        devopsCdHostDeployInfoDTOList.forEach(devopsCdHostDeployInfoDTO -> {
            devopsCdHostDeployInfoDTO.setAppName(customDeployVO.getAppName());
            devopsCdHostDeployInfoDTO.setPreCommand(customDeployVO.getPreCommand());
            devopsCdHostDeployInfoDTO.setRunCommand(customDeployVO.getRunCommand());
            devopsCdHostDeployInfoDTO.setPostCommand(customDeployVO.getPostCommand());
            devopsCdHostDeployInfoDTO.setKillCommand(customDeployVO.getKillCommand());
            devopsCdHostDeployInfoDTO.setHealthProb(customDeployVO.getHealthProb());

            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCdHostDeployInfoMapper, devopsCdHostDeployInfoDTO, DEVOPS_UPDATE_PIPELINE_CUSTOM_DEPLOY_INFO);
        });
    }
}
