package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsNotificationUserRelDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsNotificationUserRelE;
import io.choerodon.devops.infra.dto.DevopsNotificationUserRelDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsNotificationUserRelConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsNotificationUserRelDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsNotificationUserRelE;
import io.choerodon.devops.infra.dataobject.DevopsNotificationUserRelDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsNotificationUserRelConvertor.java

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:51 2019/5/13
 * Description:
 */
@Component
public class DevopsNotificationUserRelConvertor implements ConvertorI<DevopsNotificationUserRelE, DevopsNotificationUserRelDO, DevopsNotificationUserRelDTO> {

    @Override
    public DevopsNotificationUserRelE dtoToEntity(DevopsNotificationUserRelDTO notificationDTO) {
        DevopsNotificationUserRelE notificationUserRelE = new DevopsNotificationUserRelE();
        BeanUtils.copyProperties(notificationDTO, notificationUserRelE);
        return notificationUserRelE;
    }

    @Override
    public DevopsNotificationUserRelDO entityToDo(DevopsNotificationUserRelE entity) {
        DevopsNotificationUserRelDO notificationUserRelDO = new DevopsNotificationUserRelDO();
        BeanUtils.copyProperties(entity, notificationUserRelDO);
        return notificationUserRelDO;
    }

    @Override
    public DevopsNotificationUserRelE doToEntity(DevopsNotificationUserRelDO notificationUserRelDO) {
        DevopsNotificationUserRelE notificationUserRelE = new DevopsNotificationUserRelE();
        BeanUtils.copyProperties(notificationUserRelDO, notificationUserRelE);
        return notificationUserRelE;
    }

    @Override
    public DevopsNotificationUserRelDTO entityToDto(DevopsNotificationUserRelE entity) {
        DevopsNotificationUserRelDTO notificationUserRelDTO = new DevopsNotificationUserRelDTO();
        BeanUtils.copyProperties(entity, notificationUserRelDTO);
        return notificationUserRelDTO;
    }
}
