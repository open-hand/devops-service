package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.app.service.DevopsHzeroDeployConfigService;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployConfigDTO;
import io.choerodon.devops.infra.mapper.DevopsHzeroDeployConfigMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 10:02
 */
@Service
public class DevopsHzeroDeployConfigServiceImpl implements DevopsHzeroDeployConfigService {

    private static final String ERROR_SAVE_DEPLOY_VALUE_FAILED = "error.save.deploy.value.failed";
    private static final String ERROR_UPDATE_DEPLOY_VALUE_FAILED = "error.update.deploy.value.failed";

    @Autowired
    private DevopsHzeroDeployConfigMapper devopsHzeroDeployConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevopsHzeroDeployConfigDTO baseSave(DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsHzeroDeployConfigMapper, devopsHzeroDeployConfigDTO, ERROR_SAVE_DEPLOY_VALUE_FAILED);
        return devopsHzeroDeployConfigMapper.selectByPrimaryKey(devopsHzeroDeployConfigDTO.getId());
    }

    @Override
    public DevopsHzeroDeployConfigDTO baseQueryById(Long valueId) {
        return devopsHzeroDeployConfigMapper.selectByPrimaryKey(valueId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHzeroDeployConfigMapper, devopsHzeroDeployConfigDTO, ERROR_UPDATE_DEPLOY_VALUE_FAILED);
    }

    @Override
    @Transactional
    public void updateById(Long id, String value, DevopsServiceReqVO devopsServiceReqVO, DevopsIngressVO devopsIngressVO) {
        DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO = baseQueryById(id);
        devopsHzeroDeployConfigDTO.setValue(value);
        if (devopsServiceReqVO != null) {
            devopsHzeroDeployConfigDTO.setService(JsonHelper.marshalByJackson(devopsServiceReqVO));
        } else {
            devopsHzeroDeployConfigDTO.setService(null);
        }
        if (devopsIngressVO != null) {
            devopsHzeroDeployConfigDTO.setIngress(JsonHelper.marshalByJackson(devopsIngressVO));
        } else {
            devopsHzeroDeployConfigDTO.setIngress(null);
        }
        update(devopsHzeroDeployConfigDTO);
    }
}
