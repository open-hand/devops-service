package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.DevopsCdHostDeployInfoService;
import io.choerodon.devops.infra.dto.DevopsCdHostDeployInfoDTO;
import io.choerodon.devops.infra.mapper.DevopsCdHostDeployInfoMapper;
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
}
