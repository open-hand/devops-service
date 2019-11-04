package io.choerodon.devops.app.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.app.service.DevopsPvServcie;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;
import io.choerodon.devops.infra.mapper.DevopsPvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevopsPvServiceImpl implements DevopsPvServcie {

    @Autowired
    DevopsPvMapper devopsPvMapper;
    
    @Override
    public PageInfo<DevopsPvVO> queryAll(PageRequest pageRequest) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize())
                .doSelectPageInfo(() -> devopsPvMapper.queryAll());
    }


    @Override
    public void createPv(DevopsPvDTO devopsPvDTO) {
        devopsPvMapper.insert(devopsPvDTO);
    }

    @Override
    public void assignPermission(DevopsPvProPermissionDTO devopsPvProPermissionDTO) {

    }
}
