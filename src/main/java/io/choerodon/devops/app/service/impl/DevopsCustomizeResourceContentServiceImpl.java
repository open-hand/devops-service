package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCustomizeResourceContentService;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
import io.choerodon.devops.infra.mapper.DevopsCustomizeResourceContentMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:11 2019/7/12
 * Description:
 */
@Service
public class DevopsCustomizeResourceContentServiceImpl implements DevopsCustomizeResourceContentService {

    @Autowired
    private DevopsCustomizeResourceContentMapper devopsCustomizeResourceContentMapper;

    @Override
    public DevopsCustomizeResourceContentDTO baseCreate(DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDTO) {
        if (devopsCustomizeResourceContentMapper.insert(devopsCustomizeResourceContentDTO) != 1) {
            throw new CommonException("devops.customize.resource.content.insert.error");
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
            throw new CommonException("devops.customize.resource.content.update.error");
        }
    }

    @Override
    public void baseDelete(Long contentId) {
        if (devopsCustomizeResourceContentMapper.deleteByPrimaryKey(contentId) != 1) {
            throw new CommonException("devops.customize.resource.content.delete.error");
        }
    }

    @Override
    public void baseDeleteByContentIds(List<Long> contentIds) {
        devopsCustomizeResourceContentMapper.deleteByContentIds(contentIds);
    }
}
