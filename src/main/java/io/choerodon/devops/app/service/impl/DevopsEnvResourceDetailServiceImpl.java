package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsEnvResourceDetailService;
import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvResourceDetailMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:15 2019/7/15
 * Description:
 */
@Service
public class DevopsEnvResourceDetailServiceImpl implements DevopsEnvResourceDetailService {
    @Autowired
    private DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper;

    @Override
    public DevopsEnvResourceDetailDTO baseCreate(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO) {
        if (devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDTO) != 1) {
            throw new CommonException("error.message.insert");
        }
        return devopsEnvResourceDetailDTO;
    }

    @Override
    public DevopsEnvResourceDetailDTO baesQueryByMessageId(Long messageId) {
        return devopsEnvResourceDetailMapper.selectByPrimaryKey(messageId);
    }

    @Override
    public void baseUpdate(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO) {
        devopsEnvResourceDetailDTO.setObjectVersionNumber(
                devopsEnvResourceDetailMapper.selectByPrimaryKey(
                        devopsEnvResourceDetailDTO.getId()).getObjectVersionNumber());
        if (devopsEnvResourceDetailMapper.updateByPrimaryKeySelective(devopsEnvResourceDetailDTO) != 1) {
            throw new CommonException("error.message.update");
        }
    }
}
