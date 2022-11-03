package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
            throw new CommonException("devops.message.insert");
        }
        return devopsEnvResourceDetailDTO;
    }

    @Override
    public DevopsEnvResourceDetailDTO baseQueryByResourceDetailId(Long resourceDetailId) {
        return devopsEnvResourceDetailMapper.selectByPrimaryKey(resourceDetailId);
    }

    @Override
    public void baseUpdate(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO) {
        devopsEnvResourceDetailDTO.setObjectVersionNumber(
                devopsEnvResourceDetailMapper.selectByPrimaryKey(
                        devopsEnvResourceDetailDTO.getId()).getObjectVersionNumber());
        if (devopsEnvResourceDetailMapper.updateByPrimaryKeySelective(devopsEnvResourceDetailDTO) != 1) {
            throw new CommonException("devops.message.update");
        }
    }

    @Override
    public List<DevopsEnvResourceDetailDTO> listByResourceDetailsIds(Set<Long> resourceDetailIds) {
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = new ArrayList<>();
       if(!CollectionUtils.isEmpty(resourceDetailIds)){
           devopsEnvResourceDetailDTOS = devopsEnvResourceDetailMapper.listByResourceDetailIds(resourceDetailIds);

       }
       return  devopsEnvResourceDetailDTOS;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int batchDeleteByIdInNewTrans() {
        List<Long> ids = devopsEnvResourceDetailMapper.selectDirtyDataIdWithLimit();
        if (!CollectionUtils.isEmpty(ids)) {
            devopsEnvResourceDetailMapper.batchDeleteByIdInNewTrans(ids);
        }
        return ids.size();
    }
}
