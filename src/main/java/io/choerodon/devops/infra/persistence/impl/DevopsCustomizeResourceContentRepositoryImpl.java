package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsCustomizeResourceContentE;
import io.choerodon.devops.domain.application.repository.DevopsCustomizeResourceContentRepository;
import io.choerodon.devops.infra.dataobject.DevopsCustomizeResourceContentDO;
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
    public DevopsCustomizeResourceContentE create(DevopsCustomizeResourceContentE devopsCustomizeResourceContentE) {
        DevopsCustomizeResourceContentDO devopsCustomizeResourceContentDO = ConvertHelper.convert(devopsCustomizeResourceContentE, DevopsCustomizeResourceContentDO.
                class);
        if (devopsCustomizeResourceContentMapper.insert(devopsCustomizeResourceContentDO) != 1) {
            throw new CommonException("error.customize.resource.content.insert.error");
        }
        return ConvertHelper.convert(devopsCustomizeResourceContentDO, DevopsCustomizeResourceContentE.class);
    }

    @Override
    public DevopsCustomizeResourceContentE query(Long contentId) {
        return ConvertHelper.convert(devopsCustomizeResourceContentMapper.selectByPrimaryKey(contentId), DevopsCustomizeResourceContentE.class);
    }

    @Override
    public void update(DevopsCustomizeResourceContentE devopsCustomizeResourceContentE) {
        DevopsCustomizeResourceContentDO devopsCustomizeResourceContentDO = ConvertHelper.convert(devopsCustomizeResourceContentE, DevopsCustomizeResourceContentDO.class);
        if (devopsCustomizeResourceContentMapper.updateByPrimaryKey(devopsCustomizeResourceContentDO) != 1) {
            throw new CommonException("error.customize.resource.content.update.error");
        }
    }

    @Override
    public void delete(Long contentId) {
        if (devopsCustomizeResourceContentMapper.deleteByPrimaryKey(contentId) != 1) {
            throw new CommonException("error.customize.resource.content.delete.error");
        }
    }


}
