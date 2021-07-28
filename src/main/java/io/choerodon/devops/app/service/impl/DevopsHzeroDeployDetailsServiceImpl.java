package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.DevopsHzeroDeployDetailsService;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
import io.choerodon.devops.infra.enums.HzeroDeployDetailsStatusEnum;
import io.choerodon.devops.infra.mapper.DevopsHzeroDeployDetailsMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 10:04
 */
@Service
public class DevopsHzeroDeployDetailsServiceImpl implements DevopsHzeroDeployDetailsService {

    private static final String ERROR_SAVE_DEPLOY_DETAILS_FAILED = "error.save.deploy.details.failed";
    private static final String ERROR_UPDATE_DEPLOY_DETAILS_FAILED = "error.update.deploy.details.failed";

    @Autowired
    private DevopsHzeroDeployDetailsMapper devopsHzeroDeployDetailsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevopsHzeroDeployDetailsDTO baseSave(DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO) {
        MapperUtil.resultJudgedInsert(devopsHzeroDeployDetailsMapper, devopsHzeroDeployDetailsDTO, ERROR_SAVE_DEPLOY_DETAILS_FAILED);
        return devopsHzeroDeployDetailsMapper.selectByPrimaryKey(devopsHzeroDeployDetailsDTO.getId());
    }

    @Override
    public DevopsHzeroDeployDetailsDTO baseQueryById(Long detailsRecordId) {
        return devopsHzeroDeployDetailsMapper.selectByPrimaryKey(detailsRecordId);
    }

    @Override
    @Transactional
    public void updateStatusById(Long id, HzeroDeployDetailsStatusEnum status) {
        DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = devopsHzeroDeployDetailsMapper.selectByPrimaryKey(id);
        devopsHzeroDeployDetailsDTO.setStatus(status.value());
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHzeroDeployDetailsMapper, devopsHzeroDeployDetailsDTO, ERROR_UPDATE_DEPLOY_DETAILS_FAILED);
    }
}
