package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCustomizeResourceContentService;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
import io.choerodon.devops.infra.mapper.DevopsCustomizeResourceContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:11 2019/7/12
 * Description:
 */
@Service
public class DevopsCustomizeResourceContentServiceImpl implements DevopsCustomizeResourceContentService {

    @Autowired
    DevopsCustomizeResourceContentMapper devopsCustomizeResourceContentMapper;

    @Override
    public DevopsCustomizeResourceContentDTO baseCreate(DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDTO) {
        if (devopsCustomizeResourceContentMapper.insert(devopsCustomizeResourceContentDTO) != 1) {
            throw new CommonException("error.customize.resource.content.insert.error");
        }
        return devopsCustomizeResourceContentDTO;
    }

    @Override
    public DevopsCustomizeResourceContentDTO baseQuery(Long contentId) {
        return devopsCustomizeResourceContentMapper.selectByPrimaryKey(contentId);
    }

    @Override
    public void baseUpdate(DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDTO) {
        if (devopsCustomizeResourceContentMapper.updateByPrimaryKey(devopsCustomizeResourceContentDTO) != 1) {
            throw new CommonException("error.customize.resource.content.update.error");
        }
    }

    @Override
    public void baseDelete(Long contentId) {
        if (devopsCustomizeResourceContentMapper.deleteByPrimaryKey(contentId) != 1) {
            throw new CommonException("error.customize.resource.content.delete.error");
        }
    }
}
