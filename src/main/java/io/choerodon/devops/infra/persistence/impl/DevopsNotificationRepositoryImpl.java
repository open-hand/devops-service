package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsNotificationE;
import io.choerodon.devops.domain.application.repository.DevopsNotificationRepository;
import io.choerodon.devops.infra.dataobject.DevopsNotificationDO;
import io.choerodon.devops.infra.mapper.DevopsNotificationMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:22 2019/5/13
 * Description:
 */
@Component
public class DevopsNotificationRepositoryImpl implements DevopsNotificationRepository {
    @Autowired
    private DevopsNotificationMapper notificationMapper;

    @Override
    public DevopsNotificationE createOrUpdate(DevopsNotificationE notificationE) {
        DevopsNotificationDO notificationDO = ConvertHelper.convert(notificationE, DevopsNotificationDO.class);
        if (notificationDO.getId() == null) {
            if (notificationMapper.insert(notificationDO) != 1) {
                throw new CommonException("error.notification.create");
            }
        } else {
            if (notificationMapper.updateByPrimaryKeySelective(notificationDO) != 1) {
                throw new CommonException("error.notification.create");
            }
        }
        return ConvertHelper.convert(notificationDO, DevopsNotificationE.class);
    }

    @Override
    public void deleteById(Long notificationId) {
        notificationMapper.deleteByPrimaryKey(notificationId);
    }

    @Override
    public List<DevopsNotificationE> listByOptions() {
        return null;
    }

    @Override
    public DevopsNotificationE queryById(Long notificationId) {
        return ConvertHelper.convert(notificationMapper.selectByPrimaryKey(notificationId), DevopsNotificationE.class);
    }
}
