package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsHarborUserService;
import io.choerodon.devops.infra.dto.HarborUserDTO;
import io.choerodon.devops.infra.mapper.HarborUserMapper;

/**
 * @author: 25499
 * @date: 2019/10/23 11:55
 * @description:
 */
@Service
public class DevopsHarborUserServiceImpl implements DevopsHarborUserService {
    @Autowired
    private HarborUserMapper harborUserMapper;

    @Override
    public void baseCreateOrUpdate(HarborUserDTO harborUser) {
        HarborUserDTO oldHarborUserDTO = harborUserMapper.selectOne(harborUser);
        if (oldHarborUserDTO == null) {
            if (harborUserMapper.insertSelective(harborUser) != 1) {
                throw new CommonException("error.insert.harbor.user");
            }
        } else {
            harborUser.setId(oldHarborUserDTO.getId());
            harborUser.setObjectVersionNumber(oldHarborUserDTO.getObjectVersionNumber());
            if (harborUserMapper.updateByPrimaryKeySelective(harborUser) != 1) {
                throw new CommonException("error.update.harbor.user");
            }
        }
    }

    @Override
    public void baseCreate(HarborUserDTO harborUser) {
        if (harborUserMapper.insertSelective(harborUser) != 1) {
            throw new CommonException("error.insert.harbor.user");
        }
    }

    @Override
    public HarborUserDTO queryHarborUserById(Long id) {
        return harborUserMapper.selectByPrimaryKey(id);
    }

    @Override
    public void baseDelete(Long harborUserId) {
        if (harborUserMapper.deleteByPrimaryKey(harborUserId) != 1) {
            throw new CommonException("erroe.delete.harbor.user");
        }
    }
}
