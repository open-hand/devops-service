package io.choerodon.devops.app.service.impl;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.api.dto.iam.UserDTO;
import io.choerodon.devops.app.service.DevopsNotificationService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.common.util.enums.TriggerObject;
import io.choerodon.devops.infra.common.util.enums.TriggerType;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.devops.infra.feign.NotifyClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:09 2019/5/13
 * Description:
 */
@Service
public class DevopsNotificationServiceImpl implements DevopsNotificationService {

    public static final String RESOURCE_DELETE_CONFIRMATION = "resourceDeleteConfirmation";
    private static final String PROJECT_OWNER = "role/project/default/project-owner";
    private static final Long timeout = 600L;
    public static final String DEVOPS_DELETE_INSTANCE_4_SMS = "devopsDeleteInstance4Sms";


    @Autowired
    private DevopsNotificationRepository notificationRepository;
    @Autowired
    private DevopsNotificationUserRelRepository notificationUserRelRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private NotifyClient notifyClient;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private DevopsConfigMapRepository devopsConfigMapRepository;
    @Autowired
    private DevopsSecretRepository devopsSecretRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;

    @Override
    public DevopsNotificationDTO create(Long projectId, DevopsNotificationDTO notificationDTO) {
        DevopsNotificationE notificationE = ConvertHelper.convert(notificationDTO, DevopsNotificationE.class);
        Set<String> resources = check(projectId, notificationDTO.getEnvId());
        notificationDTO.getNotifyTriggerEvent().forEach(event -> {
            if (resources.contains(event)) {
                throw new CommonException("error.trigger.event.exist");
            }
        });
        notificationE.setProjectId(projectId);
        notificationE = notificationRepository.createOrUpdate(notificationE);
        List<Long> userRelIds = notificationDTO.getUserRelIds();
        if (userRelIds != null && !userRelIds.isEmpty()) {
            Long notificationId = notificationE.getId();
            userRelIds.forEach(t -> {
                notificationUserRelRepository.create(notificationId, t);
            });
        }
        return notificationDTO;
    }

    @Override
    public DevopsNotificationDTO update(Long projectId, DevopsNotificationDTO notificationDTO) {
        DevopsNotificationE notificationE = ConvertHelper.convert(notificationDTO, DevopsNotificationE.class);
        DevopsNotificationE oldNotificationE = notificationRepository.queryById(notificationDTO.getId());
        Set<String> resources = check(projectId, notificationDTO.getEnvId());
        List<String> events = notificationDTO.getNotifyTriggerEvent();
        events.removeAll(Arrays.asList(oldNotificationE.getNotifyTriggerEvent().split(",")));
        if (!events.isEmpty()) {
            events.forEach(event -> {
                if (resources.contains(event)) {
                    throw new CommonException("error.trigger.event.exist");
                }
            });
        }
        notificationE.setProjectId(projectId);
        notificationRepository.createOrUpdate(notificationE);
        updateUserRel(notificationDTO);
        return notificationDTO;
    }

    @Override
    public void delete(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        notificationUserRelRepository.delete(notificationId, null);
    }

    @Override
    public DevopsNotificationDTO queryById(Long notificationId) {
        DevopsNotificationDTO notificationDTO = ConvertHelper.convert(notificationRepository.queryById(notificationId), DevopsNotificationDTO.class);
        List<Long> userRelIds = notificationUserRelRepository.queryByNoticaionId(notificationId).stream().map(DevopsNotificationUserRelE::getUserId).collect(Collectors.toList());
        notificationDTO.setUserRelIds(userRelIds);
        return notificationDTO;
    }

    @Override
    public PageInfo<DevopsNotificationDTO> listByOptions(Long projectId, Long envId, String params, PageRequest pageRequest) {
        PageInfo<DevopsNotificationDTO> page = ConvertPageHelper.convertPageInfo(notificationRepository.listByOptions(projectId, envId, params, pageRequest), DevopsNotificationDTO.class);
        List<DevopsNotificationDTO> list = new ArrayList<>();
        page.getList().forEach(t -> {
            if ("specifier".equals(t.getNotifyObject())) {
                List<DevopsNotificationUserRelDTO> userRelDTOS = ConvertHelper.convertList(notificationUserRelRepository.queryByNoticaionId(t.getId()), DevopsNotificationUserRelDTO.class);
                userRelDTOS = userRelDTOS.stream().peek(u -> {
                    UserE userE = iamRepository.queryUserByUserId(u.getUserId());
                    if (userE != null) {
                        u.setImageUrl(userE.getImageUrl());
                        u.setRealName(userE.getRealName());
                        u.setLoginName(userE.getLoginName());
                    }
                }).collect(Collectors.toList());
                t.setUserRelDTOS(userRelDTOS);
            }
            list.add(t);
        });
        PageInfo<DevopsNotificationDTO> dtoPage = new PageInfo<>();
        BeanUtils.copyProperties(page, dtoPage);
        dtoPage.setList(list);
        return dtoPage;
    }

    @Override
    public Set<String> check(Long projectId, Long envId) {
        Set<String> hashSet = new HashSet<>();
        notificationRepository.queryByEnvId(projectId, envId).forEach(t -> Collections.addAll(hashSet, t.getNotifyTriggerEvent().split(",")));
        return hashSet;
    }

    @Override
    public ResourceCheckDTO checkResourceDelete(Long envId, String objectType) {
        ResourceCheckDTO resourceCheckDTO = new ResourceCheckDTO();
        List<DevopsNotificationE> devopsNotificationES = notificationRepository.ListByEnvId(envId);
        if (devopsNotificationES.isEmpty()) {
            return resourceCheckDTO;
        }
        //返回删除对象时,获取验证码方式和所通知的目标人群
        for (DevopsNotificationE devopsNotificationE : devopsNotificationES) {
            List<String> triggerEvents = Arrays.asList(devopsNotificationE.getNotifyTriggerEvent().split(","));
            for (String triggerEvent : triggerEvents) {
                if (triggerEvent.equals(objectType)) {
                    resourceCheckDTO.setNotificationId(devopsNotificationE.getId());
                    List<String> triggerTypes = Arrays.asList(devopsNotificationE.getNotifyType().split(","));
                    resourceCheckDTO.setMethod(StringUtils.join(triggerTypes.stream().map(trigger -> {
                        if (trigger.equals(TriggerType.EMAIL.getType())) {
                            return "邮件";
                        } else if (trigger.equals(TriggerType.PM.getType())) {
                            return "站内信";
                        } else {
                            return "短消息";
                        }
                    }).collect(Collectors.toList()).toArray(), ","));
                    if (devopsNotificationE.getNotifyObject().equals(TriggerObject.HANDLER.getObject())) {
                        List<UserE> users = iamRepository.listUsersByIds(Arrays.asList(GitUserNameUtil.getUserId().longValue()));
                        if (!users.isEmpty()) {
                            if (users.get(0).getRealName() != null) {
                                resourceCheckDTO.setUser(users.get(0).getRealName());
                            } else {
                                resourceCheckDTO.setUser(users.get(0).getLoginName());
                            }
                        }
                    } else if (devopsNotificationE.getNotifyObject().equals(TriggerObject.OWNER.getObject())) {
                        resourceCheckDTO.setUser("项目所有者");
                    } else {
                        List<Long> userIds = notificationUserRelRepository.queryByNoticaionId(devopsNotificationE.getId()).stream().map(DevopsNotificationUserRelE::getUserId).collect(Collectors.toList());
                        iamRepository.listUsersByIds(userIds).stream().map(UserE::getRealName).collect(Collectors.toList());
                        resourceCheckDTO.setUser(StringUtils.join(iamRepository.listUsersByIds(userIds).stream().map(userE -> {
                            if (userE.getRealName() != null) {
                                return userE.getRealName();
                            } else {
                                return userE.getLoginName();
                            }
                        }).collect(Collectors.toList()).toArray(), ","));
                    }
                    return resourceCheckDTO;
                }
            }
        }
        return resourceCheckDTO;
    }

    @Override
    public void sendMessage(Long envId, Long notificationId, Long objectId, String objectType) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        String objectCode = getObjectCode(objectId, objectType);

        //生成验证码，存放在redis
        String resendKey = String.format("choerodon:devops:env:%s:%s:%s", devopsEnvironmentE.getCode(), objectType, objectCode);
        String Captcha = String.valueOf(new Random().nextInt(899999) + 100000);
        redisTemplate.opsForValue().set(resendKey, Captcha, timeout, TimeUnit.SECONDS);


        //生成发送消息需要的模板对象
        DevopsNotificationE devopsNotificationE = notificationRepository.queryById(notificationId);
        List<String> triggerTypes = Arrays.asList(devopsNotificationE.getNotifyType().split(","));
        NotifyDTO notifyDTO = new NotifyDTO();
        Map<String, Object> params = new HashMap<>();
        List<UserE> userES = iamRepository.listUsersByIds(Arrays.asList(GitUserNameUtil.getUserId().longValue()));
        if (!userES.isEmpty()) {
            if (userES.get(0).getRealName() != null) {
                params.put("user", userES.get(0).getRealName());
            } else {
                params.put("user", userES.get(0).getLoginName());
            }
        }

        params.put("env", devopsEnvironmentE.getName());
        params.put("object", getObjectType(objectType));
        params.put("objectName", objectCode);
        params.put("captcha", Captcha);
        params.put("timeout", "10");
        //由于短信模板内容的问题，暂时需要传入此instance,后续统一改成object和objectType
        params.put("instance", objectCode);
        if (devopsNotificationE.getNotifyObject().equals(TriggerObject.HANDLER.getObject())) {
            NoticeSendDTO.User user = new NoticeSendDTO.User();
            params.put("mobile", userES.get(0).getPhone());
            user.setEmail(GitUserNameUtil.getEmail());
            user.setId(GitUserNameUtil.getUserId().longValue());
            notifyDTO.setTargetUsers(Arrays.asList(user));
        } else if (devopsNotificationE.getNotifyObject().equals(TriggerObject.OWNER.getObject())) {
            Long ownerId = iamRepository.queryRoleIdByCode(PROJECT_OWNER);
            PageInfo<UserDTO> allOwnerUsersPage = iamRepository
                    .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(1,0), new RoleAssignmentSearchDTO(),
                            ownerId, devopsEnvironmentE.getProjectE().getId(), false);
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
            notifyDTO.setTargetUsers(users);
        } else {
            List<Long> userIds = notificationUserRelRepository.queryByNoticaionId(devopsNotificationE.getId()).stream().map(DevopsNotificationUserRelE::getUserId).collect(Collectors.toList());
            List<NoticeSendDTO.User> users = new ArrayList<>();
            params.put("mobile", StringUtils.join(iamRepository.listUsersByIds(userIds).stream().map(userE -> {
                NoticeSendDTO.User user = new NoticeSendDTO.User();
                user.setEmail(userE.getEmail());
                user.setId(userE.getId());
                users.add(user);
                return userE.getPhone();
            }).collect(Collectors.toList()), ","));
            notifyDTO.setTargetUsers(users);
        }
        notifyDTO.setParams(params);
        try {
            //根据不同的通知方式发送验证码
            triggerTypes.stream().forEach(triggerType -> {
                if (triggerType.equals(TriggerType.EMAIL.getType())) {
                    notifyDTO.setSourceId(devopsEnvironmentE.getProjectE().getId());
                    notifyDTO.setCode(RESOURCE_DELETE_CONFIRMATION);
                    notifyDTO.setCustomizedSendingTypes(Arrays.asList("email"));
                    notifyClient.sendMessage(notifyDTO);
                } else if (triggerType.equals(TriggerType.PM.getType())) {
                    notifyDTO.setSourceId(devopsEnvironmentE.getProjectE().getId());
                    notifyDTO.setCode(RESOURCE_DELETE_CONFIRMATION);
                    notifyDTO.setCustomizedSendingTypes(Arrays.asList("siteMessage"));
                    notifyClient.sendMessage(notifyDTO);
                } else {
                    notifyDTO.setSourceId(0L);
                    notifyDTO.setCode(DEVOPS_DELETE_INSTANCE_4_SMS);
                    notifyDTO.setCustomizedSendingTypes(Arrays.asList("sms"));
                    notifyClient.sendMessage(notifyDTO);
                }
            });
        } catch (Exception e) {
            redisTemplate.delete(resendKey);
            throw new CommonException("error.msg.send.failed");
        }
    }


    @Override
    public void validateCaptcha(Long envId, Long objectId, String objectType, String captcha) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        String objectCode = getObjectCode(objectId, objectType);
        String resendKey = String.format("choerodon:devops:env:%s:%s:%s", devopsEnvironmentE.getCode(), objectType, objectCode);
        if (!captcha.equals(redisTemplate.opsForValue().get(resendKey))) {
            throw new CommonException("error.captcha");
        }
        redisTemplate.delete(resendKey);
    }

    private String getObjectCode(Long objectId, String type) {
        String code = "";
        ObjectType objectType = ObjectType.forValue(type);
        switch (objectType) {
            case INSTANCE:
                ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(objectId);
                code = applicationInstanceE.getCode();
                break;
            case SERVICE:
                DevopsServiceE devopsServiceE = devopsServiceRepository.query(objectId);
                code = devopsServiceE.getName();
                break;
            case INGRESS:
                DevopsIngressDO devopsIngressDO = devopsIngressRepository.getIngress(objectId);
                code = devopsIngressDO.getName();
                break;
            case CERTIFICATE:
                CertificationE certificationE = certificationRepository.queryById(objectId);
                code = certificationE.getName();
                break;
            case CONFIGMAP:
                DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryById(objectId);
                code = devopsConfigMapE.getName();
                break;
            case SECRET:
                DevopsSecretE devopsSecretE = devopsSecretRepository.queryBySecretId(objectId);
                code = devopsSecretE.getName();
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

    private void updateUserRel(DevopsNotificationDTO notificationDTO) {
        List<Long> addUserIds = new ArrayList<>();
        List<Long> oldUserIds = notificationUserRelRepository.queryByNoticaionId(notificationDTO.getId())
                .stream().map(DevopsNotificationUserRelE::getUserId).collect(Collectors.toList());
        if (notificationDTO.getUserRelIds() != null) {
            List<Long> newUserIds = notificationDTO.getUserRelIds();
            newUserIds.forEach(t -> {
                if (oldUserIds.contains(t)) {
                    oldUserIds.remove(t);
                } else {
                    addUserIds.add(t);
                }
            });
        }
        if (!addUserIds.isEmpty()) {
            addUserIds.forEach(t -> notificationUserRelRepository.create(notificationDTO.getId(), t));
        }
        if (!oldUserIds.isEmpty()) {
            oldUserIds.forEach(t -> notificationUserRelRepository.delete(notificationDTO.getId(), t));
        }
    }
}
