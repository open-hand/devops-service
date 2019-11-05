package io.choerodon.devops.app.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsPvPermissionUpateVO;
import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.app.service.DevopsPvProPermissionService;
import io.choerodon.devops.app.service.DevopsPvServcie;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.devops.infra.mapper.DevopsPvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevopsPvServiceImpl implements DevopsPvServcie {

    @Autowired
    DevopsPvMapper devopsPvMapper;

    @Autowired
    DevopsPvProPermissionService devopsPvProPermissionService;
    
    @Override
    public PageInfo<DevopsPvVO> queryAll(PageRequest pageRequest) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize())
                .doSelectPageInfo(() -> devopsPvMapper.queryAll());
    }


    @Override
    public void createPv(DevopsPvDTO devopsPvDTO) {
        if (devopsPvMapper.insert(devopsPvDTO) != 1){
            throw new CommonException("error.pv.add.error");
        }
    }

    @Transactional
    @Override
    public void assignPermission(DevopsPvPermissionUpateVO update) {
        DevopsPvDTO devopsPvDTO = devopsPvMapper.selectByPrimaryKey(update.getPvId());

        if (devopsPvDTO.getSkipCheckProjectPermission()){
            // 原来对组织下所有项目公开,更新之后依然公开，则不做任何处理
            // 更新之后对特定项目公开则忽略之前的更新权限表
            if (!update.getSkipCheckProjectPermission()){
                // 更新相关字段
                updatePvInfo(update);

                //批量插入
                devopsPvProPermissionService.batchInsertIgnore(update.getPvId(), update.getProjectIds());
            }
        }else{
            // 原来不公开,现在设置公开，更新版本号，直接删除原来的权限表中的数据
            if(update.getSkipCheckProjectPermission()){
                // 先更新相关字段
                updatePvInfo(update);

                //批量删除
                devopsPvProPermissionService.baseListByPvId(update.getPvId());
            }else{
                //原来不公开，现在也不公开,则根据ids批量插入
                devopsPvProPermissionService.batchInsertIgnore(update.getPvId(), update.getProjectIds());
            }

        }
    }


    public void updatePvInfo(DevopsPvPermissionUpateVO update){
        DevopsPvDTO devopsPvDTO = new DevopsPvDTO();
        devopsPvDTO.setId(update.getPvId());
        devopsPvDTO.setSkipCheckProjectPermission(update.getSkipCheckProjectPermission());
        devopsPvDTO.setObjectVersionNumber(update.getObjectVersionNumber());
        devopsPvMapper.updateByPrimaryKeySelective(devopsPvDTO);
    }
}
