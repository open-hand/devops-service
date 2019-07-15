package io.choerodon.devops.app.service;

import java.util.Set;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsNotificationVO;
import io.choerodon.devops.api.vo.ResourceCheckDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:08 2019/5/13
 * Description:
 */
public interface DevopsNotificationService {
    DevopsNotificationVO create(Long projectId, DevopsNotificationVO notificationDTO);

    DevopsNotificationVO update(Long projectId, DevopsNotificationVO notificationDTO);

    void delete(Long notificationId);

    DevopsNotificationVO queryById(Long notificationId);

    PageInfo<DevopsNotificationVO> listByOptions(Long projectId, Long envId, String params, PageRequest pageRequest);


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
