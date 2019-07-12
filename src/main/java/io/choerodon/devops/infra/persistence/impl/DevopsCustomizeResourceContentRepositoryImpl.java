package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceContentVO;
import io.choerodon.devops.domain.application.repository.DevopsCustomizeResourceContentRepository;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
import io.choerodon.devops.infra.mapper.DevopsCustomizeResourceContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Sheep on 2019/6/26.
 */
@Component
public class DevopsCustomizeResourceContentRepositoryImpl implements DevopsCustomizeResourceContentRepository {

    @Autowired
    DevopsCustomizeResourceContentMapper devopsCustomizeResourceContentMapper;

    @Override
    public DevopsCustomizeResourceContentVO baseCreate(DevopsCustomizeResourceContentVO devopsCustomizeResourceContentE) {
        DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDO = ConvertHelper.convert(devopsCustomizeResourceContentE, DevopsCustomizeResourceContentDTO.
                class);
        if (devopsCustomizeResourceContentMapper.insert(devopsCustomizeResourceContentDO) != 1) {
            throw new CommonException("error.customize.resource.content.insert.error");
        }
        return ConvertHelper.convert(devopsCustomizeResourceContentDO, DevopsCustomizeResourceContentVO.class);
    }

    @Override
    public DevopsCustomizeResourceContentVO baseQuery(Long contentId) {
        return ConvertHelper.convert(devopsCustomizeResourceContentMapper.selectByPrimaryKey(contentId), DevopsCustomizeResourceContentVO.class);
    }

    @Override
    public void baseUpdate(DevopsCustomizeResourceContentVO devopsCustomizeResourceContentE) {
        DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDO = ConvertHelper.convert(devopsCustomizeResourceContentE, DevopsCustomizeResourceContentDTO.class);
        if (devopsCustomizeResourceContentMapper.updateByPrimaryKey(devopsCustomizeResourceContentDO) != 1) {
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
