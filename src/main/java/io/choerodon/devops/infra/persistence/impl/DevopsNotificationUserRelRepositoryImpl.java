package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsNotificationUserRelE;
import io.choerodon.devops.domain.application.repository.DevopsNotificationUserRelRepository;
import io.choerodon.devops.infra.dataobject.DevopsNotificationUserRelDO;
import io.choerodon.devops.infra.mapper.DevopsNotificationUserRelMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:45 2019/5/13
 * Description:
 */
@Component
public class DevopsNotificationUserRelRepositoryImpl implements DevopsNotificationUserRelRepository {
    @Autowired
    private DevopsNotificationUserRelMapper notificationUserRelMapper;

    @Override
    public DevopsNotificationUserRelE create(Long notificationId, Long userId) {
        DevopsNotificationUserRelDO userRelDO = new DevopsNotificationUserRelDO(userId, notificationId);
        if (notificationUserRelMapper.insert(userRelDO) != 1) {
            throw new CommonException("error.notification.user.create");
        }
        return ConvertHelper.convert(userRelDO, DevopsNotificationUserRelE.class);
    }

    @Override
    public void delete(Long notificationId, Long userId) {
        DevopsNotificationUserRelDO userRelDO = new DevopsNotificationUserRelDO(userId, notificationId);
        notificationUserRelMapper.delete(userRelDO);
    }

    @Override
    public List<DevopsNotificationUserRelE> queryByNoticaionId(Long notificationId) {
        DevopsNotificationUserRelDO userRelDO = new DevopsNotificationUserRelDO();
        userRelDO.setNotificationId(notificationId);
        return ConvertHelper.convertList(notificationUserRelMapper.select(userRelDO), DevopsNotificationUserRelE.class);
    }
}
