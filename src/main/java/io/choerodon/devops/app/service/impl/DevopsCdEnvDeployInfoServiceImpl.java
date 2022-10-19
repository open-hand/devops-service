package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_ENV_ID_IS_NULL;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdEnvDeployInfoService;
import io.choerodon.devops.infra.dto.DevopsCdEnvDeployInfoDTO;
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

    private static final String ERROR_VALUE_ID_IS_NULL = "error.value.id.is.null";

    private static final String ERROR_DEPLOY_INFO_ID_IS_NULL = "error.deploy.info.id.is.null";
    private static final String ERROR_SAVE_DEPLOY_INFO = "error.save.deploy.info";
    private static final String ERROR_UPDATE_DEPLOY_INFO = "error.update.deploy.info";

    @Autowired
    private DevopsCdEnvDeployInfoMapper devopsCdEnvDeployInfoMapper;

    @Override
    @Transactional
    public DevopsCdEnvDeployInfoDTO save(DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO) {
        // 参数校验
        Assert.notNull(devopsCdEnvDeployInfoDTO.getEnvId(), DEVOPS_ENV_ID_IS_NULL);

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
    public List<DevopsCdEnvDeployInfoDTO> queryCurrentByValueId(Long valueId) {
        Assert.notNull(valueId, ERROR_VALUE_ID_IS_NULL);
        return devopsCdEnvDeployInfoMapper.queryCurrentByValueId(valueId);
    }

    @Override
    public List<DevopsCdEnvDeployInfoDTO> queryCurrentByEnvId(Long environmentId) {
        Assert.notNull(environmentId, ERROR_VALUE_ID_IS_NULL);
        return devopsCdEnvDeployInfoMapper.queryCurrentByEnvId(environmentId);
    }

    @Override
    public List<DevopsCdEnvDeployInfoDTO> listAll() {
        return devopsCdEnvDeployInfoMapper.selectAll();
    }
}
