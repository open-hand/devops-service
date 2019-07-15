package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsNotificationUserRelService;
import io.choerodon.devops.infra.dto.DevopsNotificationUserRelDTO;
import io.choerodon.devops.infra.mapper.DevopsNotificationUserRelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/15.
 */

@Service
public class DevopsNotificationUserRelServiceImpl implements DevopsNotificationUserRelService {

    @Autowired
    private DevopsNotificationUserRelMapper devopsNotificationUserRelMapper;


    public DevopsNotificationUserRelDTO baseCreate(Long notificationId, Long userId) {
        DevopsNotificationUserRelDTO devopsNotificationUserRelDTO = new DevopsNotificationUserRelDTO(userId, notificationId);
        if (devopsNotificationUserRelMapper.insert(devopsNotificationUserRelDTO) != 1) {
            throw new CommonException("error.notification.user.create");
        }
        return devopsNotificationUserRelDTO;
    }

    public void baseDelete(Long notificationId, Long userId) {
        DevopsNotificationUserRelDTO devopsNotificationUserRelDTO = new DevopsNotificationUserRelDTO(userId, notificationId);
        devopsNotificationUserRelMapper.delete(devopsNotificationUserRelDTO);
    }

    public List<DevopsNotificationUserRelDTO> baseListByNotificationId(Long notificationId) {
        DevopsNotificationUserRelDTO devopsNotificationUserRelDTO = new DevopsNotificationUserRelDTO();
        devopsNotificationUserRelDTO.setNotificationId(notificationId);
        return devopsNotificationUserRelMapper.select(devopsNotificationUserRelDTO);
    }






}
