package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsNotificationUserRelService;
import io.choerodon.devops.infra.dto.DevopsNotificationUserRelDTO;
import io.choerodon.devops.infra.mapper.DevopsNotificationUserRelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by Sheep on 2019/7/15.
 */

@Service
public class DevopsNotificationUserRelServiceImpl implements DevopsNotificationUserRelService {

    @Autowired
    private DevopsNotificationUserRelMapper devopsNotificationUserRelMapper;

    @Override
    public DevopsNotificationUserRelDTO baseCreate(Long notificationId, Long userId) {
        DevopsNotificationUserRelDTO devopsNotificationUserRelDTO = new DevopsNotificationUserRelDTO(userId, notificationId);
        if (devopsNotificationUserRelMapper.insert(devopsNotificationUserRelDTO) != 1) {
            throw new CommonException("error.notification.user.create");
        }
        return devopsNotificationUserRelDTO;
    }

    @Override
    public void baseDelete(Long notificationId, Long userId) {
        DevopsNotificationUserRelDTO devopsNotificationUserRelDTO = new DevopsNotificationUserRelDTO(userId, notificationId);
        devopsNotificationUserRelMapper.delete(devopsNotificationUserRelDTO);
    }

    @Override
    public List<DevopsNotificationUserRelDTO> baseListByNotificationId(Long notificationId) {
        DevopsNotificationUserRelDTO devopsNotificationUserRelDTO = new DevopsNotificationUserRelDTO();
        devopsNotificationUserRelDTO.setNotificationId(notificationId);
        return devopsNotificationUserRelMapper.select(devopsNotificationUserRelDTO);
    }

    @Override
    public void batchInsert(List<DevopsNotificationUserRelDTO> devopsNotificationUserRelDTOS) {
        if (CollectionUtils.isEmpty(devopsNotificationUserRelDTOS)) {
            throw new CommonException("error.users.size.empty");
        }
        if (devopsNotificationUserRelMapper.batchInsert(devopsNotificationUserRelDTOS) < 1) {
            throw new CommonException("save.user.rel.failed");
        }
    }

    @Override
    public void baseDeleteByNotificationId(Long id) {
        if (id == null) {
            throw new CommonException("error.id.null");
        }
        DevopsNotificationUserRelDTO devopsNotificationUserRelDTO = new DevopsNotificationUserRelDTO();
        devopsNotificationUserRelDTO.setNotificationId(id);
        if (devopsNotificationUserRelMapper.delete(devopsNotificationUserRelDTO) < 1) {
            throw new CommonException("delete.user.rel.failed");
        }
    }


}
