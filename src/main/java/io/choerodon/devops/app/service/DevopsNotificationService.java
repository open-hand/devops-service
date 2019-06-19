package io.choerodon.devops.app.service;

import java.util.Set;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.dto.DevopsNotificationDTO;
import io.choerodon.devops.api.dto.ResourceCheckDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:08 2019/5/13
 * Description:
 */
public interface DevopsNotificationService {
    DevopsNotificationDTO create(Long projectId, DevopsNotificationDTO notificationDTO);

    DevopsNotificationDTO update(Long projectId, DevopsNotificationDTO notificationDTO);

    void delete(Long notificationId);

    DevopsNotificationDTO queryById(Long notificationId);

    PageInfo<DevopsNotificationDTO> listByOptions(Long projectId, Long envId, String params, PageRequest pageRequest);


    Set<String> check(Long projectId, Long envId);


    /**
     * 校验删除对象是否需要发送验证码
     *
     * @param envId  环境id
     * @param objectType  资源对象类型
     * @return
     */
    ResourceCheckDTO checkResourceDelete(Long envId, String objectType);

    /**
     * 发送验证码
     *
     * @param envId   环境Id
     * @param objectId  对象Id
     * @param notificationId  通知Id
     * @param objectType  对象类型
     */
    void sendMessage(Long envId,Long notificationId,Long objectId, String objectType);

    /**
     * 校验验证码
     *
     * @param envId   环境Id
     * @param objectId  对象Id
     * @param captcha  验证码
     * @param objectType  对象类型
     * @return
     */
    void validateCaptcha(Long envId, Long objectId, String objectType, String captcha);

}
