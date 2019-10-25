package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsHarborUserService;
import io.choerodon.devops.infra.dto.HarborUserDTO;
import io.choerodon.devops.infra.dto.harbor.User;
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
    public long create(HarborUserDTO harborUser) {
        return harborUserMapper.insertSelective(harborUser);
    }


    @Override
    public HarborUserDTO queryHarborUserById(Long id) {
        return harborUserMapper.selectByPrimaryKey(id);
    }
}
