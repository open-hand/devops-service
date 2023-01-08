package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsEnvCommandValueService;
import io.choerodon.devops.infra.dto.DevopsEnvCommandValueDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandValueMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:48 2019/7/12
 * Description:
 */
@Service
public class DevopsEnvCommandValueServiceImpl implements DevopsEnvCommandValueService {
    @Autowired
    private DevopsEnvCommandValueMapper devopsEnvCommandValueMapper;

    @Override
    public DevopsEnvCommandValueDTO baseCreate(DevopsEnvCommandValueDTO devopsEnvCommandValueDTO) {
        if (devopsEnvCommandValueMapper.insert(devopsEnvCommandValueDTO) != 1) {
            throw new CommonException("devops.env.command.value.insert");
        }
        return devopsEnvCommandValueDTO;
    }

    @Override
    public DevopsEnvCommandValueDTO baseQueryById(Long id) {
        return devopsEnvCommandValueMapper.selectByPrimaryKey(id);
    }

    @Override
    public void baseDeleteById(Long valueId) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDTO.setId(valueId);
        devopsEnvCommandValueMapper.deleteByPrimaryKey(devopsEnvCommandValueDTO);
    }

    @Override
    public void baseUpdateById(Long valueId, String value) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDTO.setId(valueId);
        devopsEnvCommandValueDTO.setValue(value);
        devopsEnvCommandValueMapper.updateByPrimaryKeySelective(devopsEnvCommandValueDTO);
    }

    @Override
    @Transactional
    public void batchDeleteByIds(Set<Long> ids) {
        devopsEnvCommandValueMapper.batchDeleteByIds(ids);
    }
}
