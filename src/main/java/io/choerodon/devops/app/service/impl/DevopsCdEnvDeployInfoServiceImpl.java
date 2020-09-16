package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdEnvDeployInfoService;
import io.choerodon.devops.infra.dto.DevopsCdEnvDeployInfoDTO;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.mapper.DevopsCdEnvDeployInfoMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/8 15:31
 */
@Service
public class DevopsCdEnvDeployInfoServiceImpl implements DevopsCdEnvDeployInfoService {

    private static final String ERROR_APP_SVC_ID_IS_NULL = "error.app.svc.id.is.null";
    private static final String ERROR_ENV_ID_IS_NULL = "error.env.id.is.null";
    private static final String ERROR_VALUE_ID_IS_NULL = "error.value.id.is.null";
    private static final String ERROR_INSTANCE_NAME_IS_NULL = "error.instance.name.id.is.null";
    private static final String ERROR_PARAM_IS_INVALID = "error.param.is.invalid";

    private static final String ERROR_DEPLOY_INFO_ID_IS_NULL = "error.deploy.info.id.is.null";
    private static final String ERROR_SAVE_DEPLOY_INFO = "error.save.deploy.info";
    private static final String ERROR_UPDATE_DEPLOY_INFO = "error.update.deploy.info";

    @Autowired
    private DevopsCdEnvDeployInfoMapper devopsCdEnvDeployInfoMapper;

    @Override
    @Transactional
    public DevopsCdEnvDeployInfoDTO save(DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO) {
        // 参数校验
        Assert.notNull(devopsCdEnvDeployInfoDTO.getAppServiceId(), ERROR_APP_SVC_ID_IS_NULL);
        Assert.notNull(devopsCdEnvDeployInfoDTO.getEnvId(), ERROR_ENV_ID_IS_NULL);
        Assert.notNull(devopsCdEnvDeployInfoDTO.getValueId(), ERROR_VALUE_ID_IS_NULL);
        Assert.notNull(devopsCdEnvDeployInfoDTO.getInstanceName(), ERROR_INSTANCE_NAME_IS_NULL);

        if (CommandType.CREATE.getType().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
            if (devopsCdEnvDeployInfoDTO.getInstanceName() == null || devopsCdEnvDeployInfoDTO.getInstanceId() != null) {
                throw new CommonException(ERROR_PARAM_IS_INVALID);
            }
        } else if (CommandType.UPDATE.getType().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
            if (devopsCdEnvDeployInfoDTO.getInstanceId() == null) {
                throw new CommonException(ERROR_PARAM_IS_INVALID);
            }
        } else {
            throw new CommonException(ERROR_PARAM_IS_INVALID);
        }
        // 保存记录
        if (devopsCdEnvDeployInfoMapper.insertSelective(devopsCdEnvDeployInfoDTO) != 1) {
            throw new CommonException(ERROR_SAVE_DEPLOY_INFO);
        }
        return devopsCdEnvDeployInfoMapper.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getId());
    }

    @Override
    public DevopsCdEnvDeployInfoDTO queryById(Long deployInfoId) {
        Assert.notNull(deployInfoId, ERROR_DEPLOY_INFO_ID_IS_NULL);
        return devopsCdEnvDeployInfoMapper.selectByPrimaryKey(deployInfoId);
    }

    @Override
    @Transactional
    public void update(DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO) {
        if (devopsCdEnvDeployInfoMapper.updateByPrimaryKeySelective(devopsCdEnvDeployInfoDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_DEPLOY_INFO);
        }
    }

    @Override
    public void updateOrUpdateByCdJob(Long cdJobId, String jarName) {
        DevopsCdEnvDeployInfoDTO queryDTO = new DevopsCdEnvDeployInfoDTO();
        queryDTO.setCdJobId(cdJobId);
        DevopsCdEnvDeployInfoDTO envDeployInfoDTO = devopsCdEnvDeployInfoMapper.selectOne(queryDTO);
        if (envDeployInfoDTO == null) {
            queryDTO.setJarName(jarName);
            if (devopsCdEnvDeployInfoMapper.insert(queryDTO) != 1) {
                throw new CommonException(ERROR_SAVE_DEPLOY_INFO);
            }
        } else {
            envDeployInfoDTO.setJarName(jarName);
            if (devopsCdEnvDeployInfoMapper.updateByPrimaryKeySelective(envDeployInfoDTO) != 1) {
                throw new CommonException(ERROR_UPDATE_DEPLOY_INFO);
            }
        }
    }

    @Override
    public DevopsCdEnvDeployInfoDTO queryByCdJobId(Long cdJobId) {
        DevopsCdEnvDeployInfoDTO queryDTO = new DevopsCdEnvDeployInfoDTO();
        queryDTO.setCdJobId(cdJobId);
        return devopsCdEnvDeployInfoMapper.selectOne(queryDTO);
    }
}
