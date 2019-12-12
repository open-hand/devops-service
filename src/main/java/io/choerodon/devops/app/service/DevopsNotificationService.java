package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.devops.api.vo.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:08 2019/5/13
 * Description:
 */
public interface DevopsNotificationService {

    /**
     * 项目下创建通知
     *
     * @param projectId
     * @param notificationDTO
     * @return
     */
    DevopsNotificationVO create(Long projectId, DevopsNotificationVO notificationDTO);

    /**
     * 项目下更新通知
     *
     * @param projectId
     * @param notificationDTO
     * @return
     */
    DevopsNotificationVO update(Long projectId, DevopsNotificationVO notificationDTO);

    /**
     * 项目下删除通知
     *
     * @param notificationId
     */
    void delete(Long notificationId);

    /**
     * 项目下获取通知详情
     *
     * @param notificationId
     * @return
     */
    DevopsNotificationVO queryById(Long notificationId);

    /**
     * 分页查询通知列表
     *
     * @param projectId
     * @param envId
     * @param params
     * @param pageable
     * @return
     */
    PageInfo<DevopsNotificationVO> pageByOptions(Long projectId, Long envId, String params, Pageable pageable);


    /**
     * 项目下校验通知
     * 环境下每个触发事件只能有一个通知
     *
     * @param projectId
     * @param envId
     * @return
     */
    Set<String> check(Long projectId, Long envId);


    /**
     * 校验删除对象是否需要发送验证码
     *
     * @param envId      环境id
     * @param objectType 资源对象类型
     * @return
     */
    ResourceCheckVO checkResourceDelete(Long envId, String objectType);

    /**
     * 发送验证码
     *
     * @param envId          环境Id
     * @param objectId       对象Id
     * @param notificationId 通知Id
     * @param objectType     对象类型
     */
    void sendMessage(Long envId, Long notificationId, Long objectId, String objectType);

    /**
     * 校验验证码
     *
     * @param envId      环境Id
     * @param objectId   对象Id
     * @param captcha    验证码
     * @param objectType 对象类型
     * @return
     */
    void validateCaptcha(Long envId, Long objectId, String objectType, String captcha);

    NotifyEventVO queryNotifyEventGroupByEnv(Long projectId, String envName);

    void batchUpdateNotifyEvent(Long projectId, List<NotificationEventVO> notificationEventList);

    void deleteNotifyEventByProjectIdAndEnvId(Long projectId, Long envId);

    List<DevopsNotificationTransferDataVO> transferDate();
}
