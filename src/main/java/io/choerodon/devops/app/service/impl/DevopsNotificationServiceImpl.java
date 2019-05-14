package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsNotificationDTO;
import io.choerodon.devops.api.dto.DevopsNotificationUserRelDTO;
import io.choerodon.devops.app.service.DevopsNotificationService;
import io.choerodon.devops.domain.application.entity.DevopsNotificationE;
import io.choerodon.devops.domain.application.entity.DevopsNotificationUserRelE;
import io.choerodon.devops.domain.application.repository.DevopsNotificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsNotificationUserRelRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:09 2019/5/13
 * Description:
 */
@Service
public class DevopsNotificationServiceImpl implements DevopsNotificationService {
    @Autowired
    private DevopsNotificationRepository notificationRepository;
    @Autowired
    private DevopsNotificationUserRelRepository notificationUserRelRepository;
    @Autowired
    private IamRepository iamRepository;

    @Override
    public DevopsNotificationDTO create(Long projectId, DevopsNotificationDTO notificationDTO) {
        DevopsNotificationE notificationE = ConvertHelper.convert(notificationDTO, DevopsNotificationE.class);
        notificationE.setProjectId(projectId);
        notificationE = notificationRepository.createOrUpdate(notificationE);
        if (notificationDTO.getUserRelDTOS() != null && !notificationDTO.getUserRelDTOS().isEmpty()) {
            List<DevopsNotificationUserRelE> userRelES = ConvertHelper.convertList(notificationDTO.getUserRelDTOS(), DevopsNotificationUserRelE.class);
            Long notificationId = notificationE.getId();
            userRelES.forEach(t -> {
                notificationUserRelRepository.create(notificationId, t.getUserId());
            });
        }
        return notificationDTO;
    }

    @Override
    public DevopsNotificationDTO update(Long projectId, DevopsNotificationDTO notificationDTO) {
        DevopsNotificationE notificationE = ConvertHelper.convert(notificationDTO, DevopsNotificationE.class);
        notificationE.setProjectId(projectId);
        notificationRepository.createOrUpdate(notificationE);
        updateUserRel(notificationDTO);
        return notificationDTO;
    }

    @Override
    public void delete(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        notificationUserRelRepository.queryByNoticaionId(notificationId).forEach(t -> {
            notificationUserRelRepository.deleteById(t.getId());
        });
    }

    @Override
    public DevopsNotificationDTO queryById(Long notificationId) {
        DevopsNotificationDTO notificationDTO = ConvertHelper.convert(notificationRepository.queryById(notificationId), DevopsNotificationDTO.class);
        List<DevopsNotificationUserRelDTO> userRelDTOS = ConvertHelper.convertList(notificationUserRelRepository.queryByNoticaionId(notificationId), DevopsNotificationUserRelDTO.class);
        userRelDTOS.forEach(t ->
                t.setImageUrl(iamRepository.queryUserByUserId(t.getUserId()).getImageUrl()));
        notificationDTO.setUserRelDTOS(userRelDTOS);
        return notificationDTO;
    }

    @Override
    public Page<DevopsNotificationDTO> listByOptions(Long projectId, Long envId, String params, PageRequest pageRequest) {
        Page<DevopsNotificationE> page = notificationRepository.listByOptions(projectId, envId, params, pageRequest);
        return null;
    }

    private void updateUserRel(DevopsNotificationDTO notificationDTO) {
        List<Long> addUserIds = new ArrayList<>();
        List<Long> newUserIds = notificationDTO.getUserRelDTOS().stream().map(DevopsNotificationUserRelDTO::getUserId).collect(Collectors.toList());
        List<Long> oldUserIds = notificationUserRelRepository.queryByNoticaionId(notificationDTO.getId())
                .stream().map(DevopsNotificationUserRelE::getUserId).collect(Collectors.toList());
        newUserIds.forEach(t -> {
            if (oldUserIds.contains(t)) {
                oldUserIds.remove(t);
            } else {
                addUserIds.add(t);
            }
        });

        if (!addUserIds.isEmpty()) {
            addUserIds.forEach(t -> notificationUserRelRepository.create(notificationDTO.getId(), t));
        }

        if (!oldUserIds.isEmpty()) {
            addUserIds.forEach(t -> notificationUserRelRepository.delete(notificationDTO.getId(), t));
        }
    }
}
