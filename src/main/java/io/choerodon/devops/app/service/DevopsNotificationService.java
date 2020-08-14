package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.ResourceCheckVO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:08 2019/5/13
 * Description:
 */
public interface DevopsNotificationService {
    /**
     * 校验删除对象是否需要发送验证码
     *
     * @param projectId  项目id
     * @param envId      环境id
     * @param objectType 资源对象类型
     * @return
     */
    ResourceCheckVO checkResourceDelete(Long projectId, Long envId, String objectType);

    /**
     * 发送验证码
     *
     * @param projectId      项目id
     * @param envId          环境Id
     * @param objectId       对象Id
     * @param notificationId 通知Id
     * @param objectType     对象类型
     */
    void sendMessage(Long projectId, Long envId, Long notificationId, Long objectId, String objectType);

    /**
     * 校验验证码
     *
     * @param projectId  项目id
     * @param envId      环境Id
     * @param objectId   对象Id
     * @param captcha    验证码
     * @param objectType 对象类型
     * @return
     */
    void validateCaptcha(Long projectId, Long envId, Long objectId, String objectType, String captcha);
}
