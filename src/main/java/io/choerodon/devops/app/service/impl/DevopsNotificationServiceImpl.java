package io.choerodon.devops.app.service.impl;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.TriggerObject;
import io.choerodon.devops.infra.enums.TriggerType;
import io.choerodon.devops.infra.feign.NotifyClient;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsNotificationMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:09 2019/5/13
 * Description:
 */
@Service
public class DevopsNotificationServiceImpl implements DevopsNotificationService {

    public static final String RESOURCE_DELETE_CONFIRMATION = "resourceDeleteConfirmation";
    public static final String DEVOPS_DELETE_INSTANCE_4_SMS = "devopsDeleteInstance4Sms";
    public static final Gson gson = new Gson();
    private static final String PROJECT_OWNER = "role/project/default/project-owner";
    private static final Long timeout = 600L;
    @Autowired
    private DevopsNotificationMapper devopsNotificationMapper;
    @Autowired
    private DevopsNotificationUserRelService devopsNotificationUserRelService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private NotifyClient notifyClient;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
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
        List<String> events = devopsNotificationVO.getNotifyTriggerEvent();
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
    public PageInfo<DevopsNotificationVO> pageByOptions(Long projectId, Long envId, String params, PageRequest pageRequest) {
        PageInfo<DevopsNotificationVO> devopsNotificationVOPageInfo = ConvertUtils.convertPage(basePageByOptions(projectId, envId, params, pageRequest), this::dtoToVo);
        devopsNotificationVOPageInfo.getList().forEach(t -> {
            if ("specifier".equals(t.getNotifyObject())) {
                List<DevopsNotificationUserRelVO> userRelVOS = ConvertUtils.convertList(devopsNotificationUserRelService.baseListByNotificationId(t.getId()), DevopsNotificationUserRelVO.class);
                userRelVOS = userRelVOS.stream().peek(u -> {
                    IamUserDTO iamUserDTO = iamServiceClientOperator.queryUserByUserId(u.getUserId());
                    if (iamUserDTO != null) {
                        u.setImageUrl(iamUserDTO.getImageUrl());
                        u.setRealName(iamUserDTO.getRealName());
                        u.setLoginName(iamUserDTO.getLoginName());
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
        List<DevopsNotificationDTO> devopsNotificationDTOS = baseListByEnvId(devopsEnvironmentDTO.getProjectId(), envId);
        if (devopsNotificationDTOS.isEmpty()) {
            return resourceCheckVO;
        }
        //返回删除对象时,获取验证码方式和所通知的目标人群
        for (DevopsNotificationDTO devopsNotificationDTO : devopsNotificationDTOS) {
            List<String> triggerEvents = Arrays.asList(devopsNotificationDTO.getNotifyTriggerEvent().split(","));
            for (String triggerEvent : triggerEvents) {
                if (triggerEvent.equals(objectType)) {
                    resourceCheckVO.setNotificationId(devopsNotificationDTO.getId());
                    List<String> triggerTypes = Arrays.asList(devopsNotificationDTO.getNotifyType().split(","));
                    resourceCheckVO.setMethod(StringUtils.join(triggerTypes.stream().map(trigger -> {
                        if (trigger.equals(TriggerType.EMAIL.getType())) {
                            return "邮件";
                        } else if (trigger.equals(TriggerType.PM.getType())) {
                            return "站内信";
                        } else {
                            return "短消息";
                        }
                    }).collect(Collectors.toList()).toArray(), ","));
                    if (devopsNotificationDTO.getNotifyObject().equals(TriggerObject.HANDLER.getObject())) {
                        List<IamUserDTO> users = iamServiceClientOperator.listUsersByIds(Arrays.asList(GitUserNameUtil.getUserId().longValue()));
                        if (!users.isEmpty()) {
                            if (users.get(0).getRealName() != null) {
                                resourceCheckVO.setUser(users.get(0).getRealName());
                            } else {
                                resourceCheckVO.setUser(users.get(0).getLoginName());
                            }
                        }
                    } else if (devopsNotificationDTO.getNotifyObject().equals(TriggerObject.OWNER.getObject())) {
                        resourceCheckVO.setUser("项目所有者");
                    } else {
                        List<Long> userIds = devopsNotificationUserRelService.baseListByNotificationId(devopsNotificationDTO.getId()).stream().map(DevopsNotificationUserRelDTO::getUserId).collect(Collectors.toList());
                        iamServiceClientOperator.listUsersByIds(userIds).stream().map(IamUserDTO::getRealName).collect(Collectors.toList());
                        resourceCheckVO.setUser(StringUtils.join(iamServiceClientOperator.listUsersByIds(userIds).stream().map(userDTO -> {
                            if (userDTO.getRealName() != null) {
                                return userDTO.getRealName();
                            } else {
                                return userDTO.getLoginName();
                            }
                        }).collect(Collectors.toList()).toArray(), ","));
                    }
                    return resourceCheckVO;
                }
            }
        }
        return resourceCheckVO;
    }

    @Override
    public void sendMessage(Long envId, Long notificationId, Long objectId, String objectType) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        String objectCode = getObjectCode(objectId, objectType);

        //生成验证码，存放在redis
        String resendKey = String.format("choerodon:devops:env:%s:%s:%s", devopsEnvironmentDTO.getCode(), objectType, objectCode);
        String captcha = String.valueOf(new Random().nextInt(899999) + 100000);
        redisTemplate.opsForValue().set(resendKey, captcha, timeout, TimeUnit.SECONDS);


        //生成发送消息需要的模板对象
        DevopsNotificationDTO devopsNotificationDTO = baseQuery(notificationId);
        List<String> triggerTypes = Arrays.asList(devopsNotificationDTO.getNotifyType().split(","));
        NotifyVO notifyVO = new NotifyVO();
        Map<String, Object> params = new HashMap<>();
        List<IamUserDTO> userES = iamServiceClientOperator.listUsersByIds(Arrays.asList(GitUserNameUtil.getUserId().longValue()));
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
        //由于短信模板内容的问题，暂时需要传入此instance,后续统一改成object和objectType
        params.put("instance", objectCode);
        if (devopsNotificationDTO.getNotifyObject().equals(TriggerObject.HANDLER.getObject())) {
            NoticeSendDTO.User user = new NoticeSendDTO.User();
            params.put("mobile", userES.get(0).getPhone());
            user.setEmail(GitUserNameUtil.getEmail());
            user.setId(GitUserNameUtil.getUserId().longValue());
            notifyVO.setTargetUsers(Arrays.asList(user));
        } else if (devopsNotificationDTO.getNotifyObject().equals(TriggerObject.OWNER.getObject())) {
            Long ownerId = iamServiceClientOperator.queryRoleIdByCode(PROJECT_OWNER);
            PageInfo<IamUserDTO> allOwnerUsersPage = iamServiceClientOperator

                    .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), new RoleAssignmentSearchVO(),

                            ownerId, devopsEnvironmentDTO.getProjectId(), false);
            List<NoticeSendDTO.User> users = new ArrayList<>();
            if (!allOwnerUsersPage.getList().isEmpty()) {
                params.put("mobile", StringUtils.join(allOwnerUsersPage.getList().stream().map(userDTO -> {
                    NoticeSendDTO.User user = new NoticeSendDTO.User();
                    user.setEmail(userDTO.getEmail());
                    user.setId(userDTO.getId());
                    users.add(user);
                    return userDTO.getPhone();
                }).collect(Collectors.toList()), ","));
            }
            notifyVO.setTargetUsers(users);
        } else {
            List<Long> userIds = devopsNotificationUserRelService.baseListByNotificationId(devopsNotificationDTO.getId()).stream().map(DevopsNotificationUserRelDTO::getUserId).collect(Collectors.toList());
            List<NoticeSendDTO.User> users = new ArrayList<>();
            params.put("mobile", StringUtils.join(iamServiceClientOperator.listUsersByIds(userIds).stream().map(userDTO -> {
                NoticeSendDTO.User user = new NoticeSendDTO.User();
                user.setEmail(userDTO.getEmail());
                user.setId(userDTO.getId());
                users.add(user);
                return userDTO.getPhone();
            }).collect(Collectors.toList()), ","));
            notifyVO.setTargetUsers(users);
        }
        notifyVO.setParams(params);
        try {
            //根据不同的通知方式发送验证码
            triggerTypes.stream().forEach(triggerType -> {
                if (triggerType.equals(TriggerType.EMAIL.getType())) {
                    notifyVO.setSourceId(devopsEnvironmentDTO.getProjectId());
                    notifyVO.setCode(RESOURCE_DELETE_CONFIRMATION);
                    notifyVO.setCustomizedSendingTypes(Arrays.asList("email"));
                    notifyClient.sendMessage(notifyVO);
                } else if (triggerType.equals(TriggerType.PM.getType())) {
                    notifyVO.setSourceId(devopsEnvironmentDTO.getProjectId());
                    notifyVO.setCode(RESOURCE_DELETE_CONFIRMATION);
                    notifyVO.setCustomizedSendingTypes(Arrays.asList("siteMessage"));
                    notifyClient.sendMessage(notifyVO);
                } else {
                    notifyVO.setSourceId(0L);
                    notifyVO.setCode(DEVOPS_DELETE_INSTANCE_4_SMS);
                    notifyVO.setCustomizedSendingTypes(Arrays.asList("sms"));
                    notifyClient.sendMessage(notifyVO);
                }
            });
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
        devopsNotificationMapper.deleteByPrimaryKey(notificationId);
    }

    public DevopsNotificationDTO baseQuery(Long notificationId) {
        return devopsNotificationMapper.selectByPrimaryKey(notificationId);
    }

    public PageInfo<DevopsNotificationDTO> basePageByOptions(Long projectId, Long envId, String params, PageRequest pageRequest) {
        Map<String, Object> map = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(map.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(map.get(TypeUtil.PARAM));
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsNotificationMapper.listByOptions(projectId, envId, searchParamMap, paramMap));
    }

    public List<DevopsNotificationDTO> baseListByEnvId(Long projectId, Long envId) {
        DevopsNotificationDTO devopsNotificationDTO = new DevopsNotificationDTO();
        devopsNotificationDTO.setProjectId(projectId);
        devopsNotificationDTO.setEnvId(envId);
        return devopsNotificationMapper.select(devopsNotificationDTO);
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
                ApplicationInstanceDTO applicationInstanceDTO = applicationInstanceService.baseQuery(objectId);
                code = applicationInstanceDTO.getCode();
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
