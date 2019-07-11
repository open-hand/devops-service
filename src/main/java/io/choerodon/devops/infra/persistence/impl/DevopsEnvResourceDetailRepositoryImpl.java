package io.choerodon.devops.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvResourceDetailE;
import io.choerodon.devops.domain.application.repository.DevopsEnvResourceDetailRepository;
import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDO;
import io.choerodon.devops.infra.mapper.DevopsEnvResourceDetailMapper;

/**
 * Created by younger on 2018/4/24.
 */
@Service
public class DevopsEnvResourceDetailRepositoryImpl implements DevopsEnvResourceDetailRepository {

    private DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper;

    public DevopsEnvResourceDetailRepositoryImpl(DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper) {
        this.devopsEnvResourceDetailMapper = devopsEnvResourceDetailMapper;
    }


    @Override
    public DevopsEnvResourceDetailE create(DevopsEnvResourceDetailE devopsEnvResourceDetailE) {
        DevopsEnvResourceDetailDO devopsEnvResourceDetailDO =
                ConvertHelper.convert(devopsEnvResourceDetailE, DevopsEnvResourceDetailDO.class);
        if (devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO) != 1) {
            throw new CommonException("error.message.insert");
        }
        return ConvertHelper.convert(devopsEnvResourceDetailDO, DevopsEnvResourceDetailE.class);
    }

    @Override
    public DevopsEnvResourceDetailE query(Long messageId) {
        return ConvertHelper.convert(
                devopsEnvResourceDetailMapper.selectByPrimaryKey(messageId),
                DevopsEnvResourceDetailE.class);
    }

    @Override
    public void update(DevopsEnvResourceDetailE devopsEnvResourceDetailE) {
        DevopsEnvResourceDetailDO devopsEnvResourceDetailDO = ConvertHelper.convert(
                devopsEnvResourceDetailE, DevopsEnvResourceDetailDO.class);
        devopsEnvResourceDetailDO.setObjectVersionNumber(
                devopsEnvResourceDetailMapper.selectByPrimaryKey(
                        devopsEnvResourceDetailDO.getId()).getObjectVersionNumber());
        if (devopsEnvResourceDetailMapper.updateByPrimaryKeySelective(devopsEnvResourceDetailDO) != 1) {
            throw new CommonException("error.message.update");
        }
    }
}
