package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceContentVO;
import io.choerodon.devops.app.service.DevopsCustomizeResourceContentService;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
import io.choerodon.devops.infra.mapper.DevopsCustomizeResourceContentMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:11 2019/7/12
 * Description:
 */
public class DevopsCustomizeResourceContentServiceImpl implements DevopsCustomizeResourceContentService {

    @Autowired
    DevopsCustomizeResourceContentMapper devopsCustomizeResourceContentMapper;

    @Override
    public DevopsCustomizeResourceContentVO baseCreate(DevopsCustomizeResourceContentVO devopsCustomizeResourceContentVO) {
        DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDTO = ConvertHelper.convert(devopsCustomizeResourceContentVO, DevopsCustomizeResourceContentDTO.
                class);
        if (devopsCustomizeResourceContentMapper.insert(devopsCustomizeResourceContentDTO) != 1) {
            throw new CommonException("error.customize.resource.content.insert.error");
        }
        return ConvertHelper.convert(devopsCustomizeResourceContentDTO, DevopsCustomizeResourceContentVO.class);
    }

    @Override
    public DevopsCustomizeResourceContentVO baseQuery(Long contentId) {
        return ConvertHelper.convert(devopsCustomizeResourceContentMapper.selectByPrimaryKey(contentId), DevopsCustomizeResourceContentVO.class);
    }

    @Override
    public void baseUpdate(DevopsCustomizeResourceContentVO devopsCustomizeResourceContentVO) {
        DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDTO = ConvertHelper.convert(devopsCustomizeResourceContentVO, DevopsCustomizeResourceContentDTO.class);
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
