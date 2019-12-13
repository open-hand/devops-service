package io.choerodon.devops.app.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.notify.MessageSettingVO;
import io.choerodon.devops.api.vo.notify.SendSettingDTO;
import io.choerodon.devops.api.vo.notify.TargetUserDTO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.NotifyClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsNotificationMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.autoconfigure.CustomPageRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:09 2019/5/13
 * Description:
 */
@Service
public class DevopsNotificationServiceImpl implements DevopsNotificationService {

    public static final String RESOURCE_DELETE_CONFIRMATION = "resourceDeleteConfirmation";
    public static final String DEVOPS_DELETE_INSTANCE_4_SMS = "devopsDeleteInstance4Sms";
    public static final String NOTIFY_RESOURCE_DELETE_CONFIRMATION = "resourceDeleteConfirmation";
    private static final String PROJECT_OWNER = "role/project/default/project-owner";
    private static final String CODE = "resourceDeleteConfirmation";
    private static final String NOTIFY_TYPE = "resourceDelete";
    public static final Gson gson = new Gson();
    private static final Long TIMEOUT = 600L;
    private static final String MOBILE = "mobile";

    @Autowired
    private DevopsNotificationMapper devopsNotificationMapper;
    @Autowired
    private DevopsNotificationUserRelService devopsNotificationUserRelService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private NotifyClient notifyClient;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private CertificationService certificationService;
    @Autowired
    private DevopsConfigMapService devopsConfigMapService;
    @Autowired
    private DevopsSecretService devopsSecretService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;


    @Override
    public DevopsNotificationVO create(Long projectId, DevopsNotificationVO devopsNotificationVO) {
        Set<String> resources = check(projectId, devopsNotificationVO.getEnvId());
        devopsNotificationVO.getNotifyTriggerEvent().forEach(event -> {
            if (resources.contains(event)) {
                throw new CommonException("error.trigger.event.exist");
            }
        });
        devopsNotificationVO.setProjectId(projectId);
        DevopsNotificationDTO devopsNotificationDTO = baseCreateOrUpdate(voToDto(devopsNotificationVO));

        //存储通知设置的目标人
        List<Long> userRelIds = devopsNotificationVO.getUserRelIds();
        if (userRelIds != null && !userRelIds.isEmpty()) {
            Long notificationId = devopsNotificationDTO.getId();
            userRelIds.forEach(t -> devopsNotificationUserRelService.baseCreate(notificationId, t));
        }
        return dtoToVo(devopsNotificationDTO);
    }


    @Override
    public DevopsNotificationVO update(Long projectId, DevopsNotificationVO devopsNotificationVO) {
        DevopsNotificationDTO oldNotificationDTO = baseQuery(devopsNotificationVO.getId());
        Set<String> resources = check(projectId, devopsNotificationVO.getEnvId());

        //判断通知设置事件是否已存在
        List<String> events = new ArrayList<>();
        events.addAll(devopsNotificationVO.getNotifyTriggerEvent());
        events.removeAll(Arrays.asList(oldNotificationDTO.getNotifyTriggerEvent().split(",")));
        if (!events.isEmpty()) {
            events.forEach(event -> {
                if (resources.contains(event)) {
                    throw new CommonException("error.trigger.event.exist");
                }
            });
        }
        devopsNotificationVO.setProjectId(projectId);
        DevopsNotificationDTO devopsNotificationDTO = baseCreateOrUpdate(voToDto(devopsNotificationVO));
        //更新通知设置的目标人
        updateUserRel(devopsNotificationVO);
        return dtoToVo(devopsNotificationDTO);
    }

    @Override
    public void delete(Long notificationId) {
        baseDelete(notificationId);
        devopsNotificationUserRelService.baseDelete(notificationId, null);
    }

    @Override
    public DevopsNotificationVO queryById(Long notificationId) {
        DevopsNotificationVO devopsNotificationVO = dtoToVo(baseQuery(notificationId));
        List<Long> userRelIds = devopsNotificationUserRelService.baseListByNotificationId(notificationId).stream().map(DevopsNotificationUserRelDTO::getUserId).collect(Collectors.toList());
        devopsNotificationVO.setUserRelIds(userRelIds);
        return devopsNotificationVO;
    }

    @Override
    public PageInfo<DevopsNotificationVO> pageByOptions(Long projectId, Long envId, String params, Pageable pageable) {
        PageInfo<DevopsNotificationVO> devopsNotificationVOPageInfo = ConvertUtils.convertPage(basePageByOptions(projectId, envId, params, pageable), this::dtoToVo);
        devopsNotificationVOPageInfo.getList().forEach(t -> {
            if ("specifier".equals(t.getNotifyObject())) {
                List<DevopsNotificationUserRelVO> userRelVOS = ConvertUtils.convertList(devopsNotificationUserRelService.baseListByNotificationId(t.getId()), DevopsNotificationUserRelVO.class);
                userRelVOS = userRelVOS.stream().peek(u -> {
                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(u.getUserId());
                    if (iamUserDTO != null) {
                        u.setImageUrl(iamUserDTO.getImageUrl());
                        u.setRealName(iamUserDTO.getRealName());
                        u.setLoginName(iamUserDTO.getLdap() ? iamUserDTO.getLoginName() : iamUserDTO.getEmail());
                    }
                }).collect(Collectors.toList());
                t.setUserRelDTOS(userRelVOS);
            }
        });
        return devopsNotificationVOPageInfo;
    }

    @Override
    public Set<String> check(Long projectId, Long envId) {
        Set<String> hashSet = new HashSet<>();
        baseListByEnvId(projectId, envId).forEach(t -> Collections.addAll(hashSet, t.getNotifyTriggerEvent().split(",")));
        return hashSet;
    }

    @Override
    public ResourceCheckVO checkResourceDelete(Long envId, String objectType) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        ResourceCheckVO resourceCheckVO = new ResourceCheckVO();
        MessageSettingVO messageSettingVO = notifyClient.queryByEnvIdAndEventNameAndProjectIdAndCode(NOTIFY_TYPE, devopsEnvironmentDTO.getProjectId(), CODE, envId, objectType);
        if (Objects.isNull(messageSettingVO)) {
            return resourceCheckVO;
        }
        //返回删除对象时,获取验证码方式和所通知的目标人群
        StringBuilder stringBuilder = new StringBuilder();
        if (messageSettingVO.getPmEnable()) {
            stringBuilder.append("站内信");
        }
        if (messageSettingVO.getEmailEnable()) {
            stringBuilder.append(",邮件");
        }
        if (messageSettingVO.getSmsEnable()) {
            stringBuilder.append(",短消息");
        }
        resourceCheckVO.setMethod(stringBuilder.toString());
        resourceCheckVO.setNotificationId(messageSettingVO.getId());
        List<TargetUserDTO> targetUserDTOS = messageSettingVO.getTargetUserDTOS();
        if (targetUserDTOS == null || targetUserDTOS.size() == 0) {
            return new ResourceCheckVO();
        }
        StringBuilder notifyTargetUser = new StringBuilder();
        targetUserDTOS.stream().forEach(e -> {
            if (TriggerObject.OWNER.getObject().equals(e.getType())) {
                notifyTargetUser.append("项目所有者");
            }
            if (TriggerObject.HANDLER.getObject().equals(e.getType())) {
                List<IamUserDTO> users = baseServiceClientOperator.listUsersByIds(Arrays.asList(GitUserNameUtil.getUserId().longValue()));
                if (!users.isEmpty()) {
                    if (users.get(0).getRealName() != null) {
                        notifyTargetUser.append("," + users.get(0).getRealName());
                    } else {
                        notifyTargetUser.append("," + users.get(0).getLoginName());
                    }
                }
            }
            if (TriggerObject.SPECIFIER.getObject().equals(e.getType())) {
                notifyTargetUser.append(",");
            }
            if (TriggerObject.SPECIFIER.getObject().equals(e.getType())) {
                List<Long> userIds = Stream.of(e.getUserId()).collect(Collectors.toList());
                baseServiceClientOperator.listUsersByIds(userIds).stream().map(IamUserDTO::getRealName).collect(Collectors.toList());
                notifyTargetUser.append(StringUtils.join(baseServiceClientOperator.listUsersByIds(userIds).stream().map(userDTO -> {
                    if (userDTO.getRealName() != null) {
                        return userDTO.getRealName();
                    } else {
                        return userDTO.getLoginName();
                    }
                }).toArray(), ","));
            }
        });
        resourceCheckVO.setUser(notifyTargetUser.toString());
        return resourceCheckVO;
    }


    @Override
    public void sendMessage(Long envId, Long notificationId, Long objectId, String objectType) {
        //notificationId为messmageSettingVO 的ID
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        String objectCode = getObjectCode(objectId, objectType);

        //生成验证码，存放在redis
        String resendKey = String.format("choerodon:devops:env:%s:%s:%s", devopsEnvironmentDTO.getCode(), objectType, objectCode);
        String captcha = String.valueOf(new Random().nextInt(899999) + 100000);
        redisTemplate.opsForValue().set(resendKey, captcha, TIMEOUT, TimeUnit.SECONDS);


        //生成发送消息需要的模板对象
        MessageSettingVO messageSettingVO = notifyClient.queryByEnvIdAndEventNameAndProjectIdAndCode(NOTIFY_TYPE, devopsEnvironmentDTO.getProjectId(), CODE, envId, objectType);
        List<String> triggerTypes = new ArrayList<>();
        if (messageSettingVO.getSmsEnable()) {
            triggerTypes.add("sms");
        }
        if (messageSettingVO.getEmailEnable()) {
            triggerTypes.add("email");
        }
        if (messageSettingVO.getPmEnable()) {
            triggerTypes.add("pm");
        }
        NotifyVO notifyVO = new NotifyVO();
        Map<String, Object> params = new HashMap<>();
        List<IamUserDTO> userES = baseServiceClientOperator.listUsersByIds(Arrays.asList(GitUserNameUtil.getUserId().longValue()));
        if (!userES.isEmpty()) {
            if (userES.get(0).getRealName() != null) {
                params.put("user", userES.get(0).getRealName());
            } else {
                params.put("user", userES.get(0).getLoginName());
            }
        }
        params.put("env", devopsEnvironmentDTO.getName());
        params.put("object", getObjectType(objectType));
        params.put("objectName", objectCode);
        params.put("captcha", captcha);
        params.put("timeout", "10");
        params.put("notifyType",NOTIFY_TYPE);
        //由于短信模板内容的问题，暂时需要传入此instance,后续统一改成object和objectType
        params.put("instance", objectCode);
        List<TargetUserDTO> targetUserDTOS = messageSettingVO.getTargetUserDTOS();
        //通知对象为null，则不发消息
        if (targetUserDTOS == null || targetUserDTOS.size() == 0) {
            return;
        }
        List<NoticeSendDTO.User> users = new ArrayList<>();
        List<String> phones = new ArrayList<>();
        targetUserDTOS.stream().forEach(e -> {
            if (TriggerObject.HANDLER.getObject().equals(e.getType())) {
                NoticeSendDTO.User user = new NoticeSendDTO.User();
                phones.add(userES.get(0).getPhone());
                user.setEmail(GitUserNameUtil.getEmail());
                user.setId(GitUserNameUtil.getUserId().longValue());
                users.add(user);
            }
            if (TriggerObject.OWNER.getObject().equals(e.getType())) {
                Long ownerId = baseServiceClientOperator.queryRoleIdByCode(PROJECT_OWNER);
                PageInfo<IamUserDTO> allOwnerUsersPage = baseServiceClientOperator
                        .pagingQueryUsersByRoleIdOnProjectLevel(CustomPageRequest.of(0, 0), new RoleAssignmentSearchVO(),
                                ownerId, devopsEnvironmentDTO.getProjectId(), false);
                if (!allOwnerUsersPage.getList().isEmpty()) {
                    allOwnerUsersPage.getList().stream().forEach(v->{
                        NoticeSendDTO.User user = new NoticeSendDTO.User();
                        user.setEmail(v.getEmail());
                        user.setId(v.getId());
                        users.add(user);
                        phones.add(v.getPhone());
                    });
                }
            }
            if (TriggerObject.SPECIFIER.getObject().equals(e.getType())) {
                List<Long> userIds = new ArrayList<>();
                userIds.add(e.getUserId());
                baseServiceClientOperator.listUsersByIds(userIds).stream().forEach(k->{
                    NoticeSendDTO.User user = new NoticeSendDTO.User();
                    user.setEmail(k.getEmail());
                    user.setId(k.getId());
                    users.add(user);
                    phones.add(k.getPhone());
                });
            }
        });
        params.put(MOBILE,StringUtils.join(phones, ","));
        notifyVO.setTargetUsers(users);
        notifyVO.setParams(params);
        try {
            //根据不同的通知方式发送验证码
            notifyVO.setSourceId(devopsEnvironmentDTO.getProjectId());
            notifyVO.setCode(RESOURCE_DELETE_CONFIRMATION);
            notifyClient.sendMessage(notifyVO);
        } catch (Exception e) {
            redisTemplate.delete(resendKey);
            throw new CommonException("error.msg.send.failed");
        }
    }


    @Override
    public void validateCaptcha(Long envId, Long objectId, String objectType, String captcha) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        String objectCode = getObjectCode(objectId, objectType);
        String resendKey = String.format("choerodon:devops:env:%s:%s:%s", devopsEnvironmentDTO.getCode(), objectType, objectCode);
        if (!captcha.equals(redisTemplate.opsForValue().get(resendKey))) {
            throw new CommonException("error.captcha");
        }
        redisTemplate.delete(resendKey);
    }

    @Override
    public NotifyEventVO queryNotifyEventGroupByEnv(Long projectId, String envName) {
        NotifyEventVO notifyEventVO = new NotifyEventVO();
        SendSettingDTO sendSettingDTO = notifyClient.queryByCode(NOTIFY_RESOURCE_DELETE_CONFIRMATION).getBody();
        if (Boolean.FALSE.equals(sendSettingDTO.getEnabled())) {
            return notifyEventVO;
        }
        // 项目下环境信息
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOList = devopsEnvironmentService.listByProjectIdAndName(projectId, envName);
        if (CollectionUtils.isEmpty(devopsEnvironmentDTOList)) {
            return notifyEventVO;
        }
//        devopsEnvironmentDTOList = devopsEnvironmentDTOList.stream().filter(v -> Boolean.TRUE.equals(v.getActive())
//                && clusterConnectionHandler.getEnvConnectionStatus(v.getClusterId())).collect(Collectors.toList());
        List<DevopsEnvironmentVO> devopsEnvironmentVOList = ConvertUtils.convertList(devopsEnvironmentDTOList, DevopsEnvironmentVO.class);
        Set<Long> envIds = devopsEnvironmentDTOList.stream().map(DevopsEnvironmentDTO::getId).collect(Collectors.toSet());
        notifyEventVO.setDevopsEnvironmentList(devopsEnvironmentVOList);
        // 系统预置配置
        List<NotificationEventVO> defaultNotifyEventList = devopsNotificationMapper.listDefaultNotifyEvent();
        // 用户配置的通知设置
        envIds.forEach(envId -> {
            List<NotificationEventVO> devopsNotificationVOList = devopsNotificationMapper.queryByProjectIdAndEnvId(projectId, envId);
            Map<String, NotificationEventVO> devopsNotificationDTOMap = devopsNotificationVOList.stream().collect(Collectors.toMap(NotificationEventVO::getNotifyTriggerEvent, v -> v));
            // 对于没有配置的通知设置，使用系统预置的配置
            defaultNotifyEventList.forEach(v -> {
                if (devopsNotificationDTOMap.get(v.getNotifyTriggerEvent()) == null) {
                    // 为默认设置添加环境id
                    NotificationEventVO notificationEventVO = ConvertUtils.convertObject(v, NotificationEventVO.class);
                    notificationEventVO.setEnvId(envId);
                    notificationEventVO.setProjectId(projectId);
                    devopsNotificationVOList.add(notificationEventVO);
                }
            });
            notifyEventVO.getDevopsNotificationList().addAll(devopsNotificationVOList);
        });

        // 计算是否发送给操作者，项目所有者，指定用户的值
        calculateSendUser(notifyEventVO.getDevopsNotificationList());
        // 删除用户列表里的操作者，项目所有者
        delOwnerAndHandler(notifyEventVO.getDevopsNotificationList());
        // 添加用户信息（登录名，真实姓名）
        addUserInfo(notifyEventVO.getDevopsNotificationList());
        // 计算事件名称
        calEventName(notifyEventVO.getDevopsNotificationList());
        return notifyEventVO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateNotifyEvent(Long projectId, List<NotificationEventVO> notificationEventList) {
        // 系统默认配置
//        List<NotificationEventVO> notificationEventVOS = devopsNotificationMapper.listDefaultNotifyEvent();
//        Set<Long> defaultEventIds = notificationEventVOS.stream().map(NotificationEventVO::getId).collect(Collectors.toSet());
//
//        List<NotificationEventVO> defaultEventList = notificationEventList.stream().filter(v -> defaultEventIds.contains(v.getId())).collect(Collectors.toList());
//        List<NotificationEventVO> customerEventList = notificationEventList.stream().filter(v -> !defaultEventIds.contains(v.getId())).collect(Collectors.toList());
//        // 默认配置的修改
//        defaultEventList.forEach(defaultEvent -> {
//            defaultEvent.setId(null);
//            DevopsNotificationDTO devopsNotificationDTO = ConvertUtils.convertObject(defaultEvent, DevopsNotificationDTO.class);
//            devopsNotificationDTO.setDefaultSetting(false);
//            devopsNotificationMapper.insertSelective(devopsNotificationDTO);
//            saveUserRel(devopsNotificationDTO.getId(), defaultEvent);
//        });
//        // 自定义配置的修改
//        customerEventList.forEach(customerEvent -> {
//            DevopsNotificationDTO devopsNotificationDTO = ConvertUtils.convertObject(customerEvent, DevopsNotificationDTO.class);
//            devopsNotificationMapper.updateByPrimaryKeySelective(devopsNotificationDTO);
//            if(!CollectionUtils.isEmpty(devopsNotificationUserRelService.baseListByNotificationId(devopsNotificationDTO.getId()))) {
//                devopsNotificationUserRelService.baseDeleteByNotificationId(devopsNotificationDTO.getId());
//            }
//            saveUserRel(devopsNotificationDTO.getId(), customerEvent);
//        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotifyEventByProjectIdAndEnvId(Long projectId, Long envId) {
        if (projectId == null) {
            throw new CommonException("error.project.id.null");
        }
        if (envId == null) {
            throw new CommonException("error.env.id.null");
        }
        DevopsNotificationDTO record = new DevopsNotificationDTO();
        record.setProjectId(projectId);
        record.setEnvId(envId);
        List<DevopsNotificationDTO> devopsNotificationDTOList = devopsNotificationMapper.select(record);
        devopsNotificationDTOList.forEach(devopsNotificationDTO -> {
            // 删除通知对象
            if (!CollectionUtils.isEmpty(devopsNotificationUserRelService.baseListByNotificationId(devopsNotificationDTO.getId()))) {
                devopsNotificationUserRelService.baseDeleteByNotificationId(devopsNotificationDTO.getId());
            }
            // 删除通知设置
            if (queryById(devopsNotificationDTO.getId()) != null) {
                baseDelete(devopsNotificationDTO.getId());
            }

        });
    }

    @Override
    public List<DevopsNotificationTransferDataVO> transferDate() {
        return devopsNotificationMapper.transferData();
    }

    public DevopsNotificationDTO baseCreateOrUpdate(DevopsNotificationDTO devopsNotificationDTO) {
        if (devopsNotificationDTO.getId() == null) {
            if (devopsNotificationMapper.insert(devopsNotificationDTO) != 1) {
                throw new CommonException("error.notification.create");
            }
        } else {
            if (devopsNotificationMapper.updateByPrimaryKeySelective(devopsNotificationDTO) != 1) {
                throw new CommonException("error.notification.update");
            }
        }
        return devopsNotificationDTO;
    }

    public void baseDelete(Long notificationId) {
        if (devopsNotificationMapper.deleteByPrimaryKey(notificationId) != 1) {
            throw new CommonException("error.delete.notify.failed");
        }
    }

    public DevopsNotificationDTO baseQuery(Long notificationId) {
        return devopsNotificationMapper.selectByPrimaryKey(notificationId);
    }

    public PageInfo<DevopsNotificationDTO> basePageByOptions(Long projectId, Long envId, String params, Pageable pageable) {
        Map<String, Object> map = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(map.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(map.get(TypeUtil.PARAMS));
        return PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize(), PageRequestUtil.getOrderBy(pageable)).doSelectPageInfo(() -> devopsNotificationMapper.listByOptions(projectId, envId, searchParamMap, paramList));
    }

    public List<DevopsNotificationDTO> baseListByEnvId(Long projectId, Long envId) {
        DevopsNotificationDTO devopsNotificationDTO = new DevopsNotificationDTO();
        devopsNotificationDTO.setProjectId(projectId);
        devopsNotificationDTO.setEnvId(envId);
        return devopsNotificationMapper.select(devopsNotificationDTO);
    }

    private void calEventName(List<NotificationEventVO> devopsNotificationList) {
        devopsNotificationList.forEach(notificationEventVO -> {
            if (NotifyEventEnum.INSTANCE.value().equals(notificationEventVO.getNotifyTriggerEvent())) {
                notificationEventVO.setName("删除实例");
            }
            if (NotifyEventEnum.INGRESS.value().equals(notificationEventVO.getNotifyTriggerEvent())) {
                notificationEventVO.setName("删除域名");
            }
            if (NotifyEventEnum.SERVICE.value().equals(notificationEventVO.getNotifyTriggerEvent())) {
                notificationEventVO.setName("删除网络");
            }
            if (NotifyEventEnum.CONFIGMAP.value().equals(notificationEventVO.getNotifyTriggerEvent())) {
                notificationEventVO.setName("删除配置映射");
            }
            if (NotifyEventEnum.CERTIFICATE.value().equals(notificationEventVO.getNotifyTriggerEvent())) {
                notificationEventVO.setName("删除证书");
            }
            if (NotifyEventEnum.SECRET.value().equals(notificationEventVO.getNotifyTriggerEvent())) {
                notificationEventVO.setName("删除密文");
            }
        });

    }

    private void delOwnerAndHandler(List<NotificationEventVO> devopsNotificationList) {
        devopsNotificationList.forEach(v -> {
            if (!CollectionUtils.isEmpty(v.getUserList())) {
                v.setUserList(v.getUserList().stream().filter(user -> TriggerObject.SPECIFIER.getObject().equals(user.getUserType())).collect(Collectors.toList()));
            }
        });
    }

    private void saveUserRel(Long notificationId, NotificationEventVO defaultEvent) {
//        List<DevopsNotificationUserRelDTO> devopsNotificationUserRelDTOS = new ArrayList<>();
//        // 添加操作者和项目管理者
//        if (defaultEvent.getSendOwner()) {
//            DevopsNotificationUserRelDTO devopsNotificationUserRelDTO = new DevopsNotificationUserRelDTO();
//            devopsNotificationUserRelDTO.setUserType(TriggerObject.OWNER.getObject());
//            devopsNotificationUserRelDTOS.add(devopsNotificationUserRelDTO);
//        }
//        if (defaultEvent.getSendHandler()) {
//            DevopsNotificationUserRelDTO devopsNotificationUserRelDTO = new DevopsNotificationUserRelDTO();
//            devopsNotificationUserRelDTO.setUserType(TriggerObject.HANDLER.getObject());
//            devopsNotificationUserRelDTOS.add(devopsNotificationUserRelDTO);
//        }
//        // 添加指定用户
//        List<DevopsNotificationUserRelVO> userList = defaultEvent.getUserList();
//        if (!CollectionUtils.isEmpty(userList)) {
//            devopsNotificationUserRelDTOS.addAll(ConvertUtils.convertList(userList, DevopsNotificationUserRelDTO.class));
//        }
//        devopsNotificationUserRelDTOS.forEach(user -> user.setNotificationId(notificationId));
//        devopsNotificationUserRelService.batchInsert(devopsNotificationUserRelDTOS);
    }

    private void addUserInfo(List<NotificationEventVO> devopsNotificationList) {
        devopsNotificationList.stream().flatMap(v -> v.getUserList().stream())
                .filter(user -> TriggerObject.SPECIFIER.getObject().equals(user.getUserType()))
                .forEach(u -> {
                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(u.getUserId());
                    if (iamUserDTO != null) {
                        u.setImageUrl(iamUserDTO.getImageUrl());
                        u.setRealName(iamUserDTO.getRealName());
                        u.setLoginName(iamUserDTO.getLdap() ? iamUserDTO.getLoginName() : iamUserDTO.getEmail());
                    }
                });

    }

    private DevopsNotificationDTO queryNotifyEventByProjectIdAndEnvIdAndType(Long projectId, Long envId, String objectType) {
        DevopsNotificationDTO devopsNotificationDTO = new DevopsNotificationDTO();
        devopsNotificationDTO.setEnvId(envId);
        devopsNotificationDTO.setProjectId(projectId);
        devopsNotificationDTO.setNotifyTriggerEvent(objectType);
        return devopsNotificationMapper.selectOne(devopsNotificationDTO);
    }

    private void calculateSendUser(List<NotificationEventVO> devopsNotificationList) {
        devopsNotificationList.forEach(v -> {
            List<DevopsNotificationUserRelVO> userList = v.getUserList();

            if (!CollectionUtils.isEmpty(userList)
                    && userList.stream().anyMatch(user -> TriggerObject.HANDLER.getObject().equals(user.getUserType()))) {
                v.setSendHandler(true);
            }
            if (!CollectionUtils.isEmpty(userList)
                    && userList.stream().anyMatch(user -> TriggerObject.OWNER.getObject().equals(user.getUserType()))) {
                v.setSendOwner(true);

            }
            if (!CollectionUtils.isEmpty(userList)
                    && userList.stream().anyMatch(user -> TriggerObject.SPECIFIER.getObject().equals(user.getUserType()))) {
                v.setSendSpecifier(true);
            }

        });

    }


    private void updateUserRel(DevopsNotificationVO devopsNotificationVO) {
        List<Long> addUserIds = new ArrayList<>();
        List<Long> oldUserIds = devopsNotificationUserRelService.baseListByNotificationId(devopsNotificationVO.getId())
                .stream().map(DevopsNotificationUserRelDTO::getUserId).collect(Collectors.toList());
        if (devopsNotificationVO.getUserRelIds() != null) {
            List<Long> newUserIds = devopsNotificationVO.getUserRelIds();
            newUserIds.forEach(t -> {
                if (oldUserIds.contains(t)) {
                    oldUserIds.remove(t);
                } else {
                    addUserIds.add(t);
                }
            });
        }
        if (!addUserIds.isEmpty()) {
            addUserIds.forEach(t -> devopsNotificationUserRelService.baseCreate(devopsNotificationVO.getId(), t));
        }
        if (!oldUserIds.isEmpty()) {
            oldUserIds.forEach(t -> devopsNotificationUserRelService.baseDelete(devopsNotificationVO.getId(), t));
        }
    }


    private String getObjectCode(Long objectId, String type) {
        String code = "";
        ObjectType objectType = ObjectType.forValue(type);
        switch (objectType) {
            case INSTANCE:
                AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(objectId);
                code = appServiceInstanceDTO.getCode();
                break;
            case SERVICE:
                DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQuery(objectId);
                code = devopsServiceDTO.getName();
                break;
            case INGRESS:
                DevopsIngressDTO devopsIngressDTO = devopsIngressService.baseQuery(objectId);
                code = devopsIngressDTO.getName();
                break;
            case CERTIFICATE:
                CertificationDTO certificationDTO = certificationService.baseQueryById(objectId);
                code = certificationDTO.getName();
                break;
            case CONFIGMAP:
                DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService.baseQueryById(objectId);
                code = devopsConfigMapDTO.getName();
                break;
            case SECRET:
                DevopsSecretDTO devopsSecretDTO = devopsSecretService.baseQuery(objectId);
                code = devopsSecretDTO.getName();
                break;
            default:
                break;
        }
        return code;
    }


    private String getObjectType(String type) {
        String result = "";
        ObjectType objectType = ObjectType.forValue(type);
        switch (objectType) {
            case INSTANCE:
                result = "实例";
                break;
            case SERVICE:
                result = "网络";
                break;
            case INGRESS:
                result = "域名";
                break;
            case CERTIFICATE:
                result = "证书";
                break;
            case CONFIGMAP:
                result = "配置映射";
                break;
            case SECRET:
                result = "密文";
                break;
            default:
                break;
        }
        return result;
    }

    private DevopsNotificationDTO voToDto(DevopsNotificationVO devopsNotificationVO) {
        DevopsNotificationDTO notificationDTO = new DevopsNotificationDTO();
        BeanUtils.copyProperties(devopsNotificationVO, notificationDTO);
        notificationDTO.setNotifyType(devopsNotificationVO.getNotifyType().stream().collect(Collectors.joining(",")));
        notificationDTO.setNotifyTriggerEvent(devopsNotificationVO.getNotifyTriggerEvent().stream().collect(Collectors.joining(",")));
        return notificationDTO;
    }


    private DevopsNotificationVO dtoToVo(DevopsNotificationDTO devopsNotificationDTO) {
        DevopsNotificationVO devopsNotificationVO = new DevopsNotificationVO();
        BeanUtils.copyProperties(devopsNotificationDTO, devopsNotificationVO);
        if (devopsNotificationDTO.getNotifyType() != null && !devopsNotificationDTO.getNotifyType().isEmpty()) {
            devopsNotificationVO.setNotifyType(Arrays.asList(devopsNotificationDTO.getNotifyType().split(",")));
        } else {
            devopsNotificationVO.setNotifyType(new ArrayList<>());
        }

        if (devopsNotificationDTO.getNotifyTriggerEvent() != null && !devopsNotificationDTO.getNotifyTriggerEvent().isEmpty()) {
            devopsNotificationVO.setNotifyTriggerEvent(Arrays.asList(devopsNotificationDTO.getNotifyTriggerEvent().split(",")));
        } else {
            devopsNotificationVO.setNotifyTriggerEvent(new ArrayList<>());
        }
        return devopsNotificationVO;
    }
}
