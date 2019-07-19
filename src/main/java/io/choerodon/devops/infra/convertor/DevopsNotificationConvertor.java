package io.choerodon.devops.infra.convertor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsNotificationVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsNotificationE;
import io.choerodon.devops.infra.dto.DevopsNotificationDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsNotificationConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsNotificationDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsNotificationE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsNotificationConvertor.java
>>>>>>> [IMP]重构后端断码
import io.choerodon.devops.infra.dataobject.DevopsNotificationDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsNotificationConvertor.java

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:23 2019/5/13
 * Description:
 */
@Component
public class DevopsNotificationConvertor implements ConvertorI<DevopsNotificationE, DevopsNotificationDTO, DevopsNotificationVO> {




    @Override
    public DevopsNotificationDTO entityToDo(DevopsNotificationE entity) {
        DevopsNotificationDTO notificationDO = new DevopsNotificationDTO();
        BeanUtils.copyProperties(entity, notificationDO);
        return notificationDO;
    }

    @Override
    public DevopsNotificationE doToEntity(DevopsNotificationDTO notificationDO) {
        DevopsNotificationE notificationE = new DevopsNotificationE();
        BeanUtils.copyProperties(notificationDO, notificationE);
        return notificationE;
    }


}
