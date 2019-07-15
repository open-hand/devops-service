package io.choerodon.devops.app.service.impl;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.app.service.DevopsNotificationService;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.dto.DevopsNotificationDTO;
import io.choerodon.devops.infra.mapper.DevopsNotificationMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.TriggerObject;
import io.choerodon.devops.infra.enums.TriggerType;
import io.choerodon.devops.infra.dto.DevopsIngressDTO;
import io.choerodon.devops.infra.feign.NotifyClient;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
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
    public static final Gson gson = new Gson();



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
    @Autowired
    private DevopsNotificationMapper devopsNotificationMapper;


    @Override
    public DevopsNotificationVO create(Long projectId, DevopsNotificationVO notificationDTO) {
        DevopsNotificationE notificationE = ConvertHelper.convert(notificationDTO, DevopsNotificationE.class);
        Set<String> resources = check(projectId, notificationDTO.getEnvId());
        notificationDTO.getNotifyTriggerEvent().forEach(event -> {
            if (resources.contains(event)) {
                throw new CommonException("error.trigger.event.exist");
            }
        });
        notificationE.setProjectId(projectId);
        notificationE = notificationRepository.baseCreateOrUpdate(notificationE);
        List<Long> userRelIds = notificationDTO.getUserRelIds();
        if (userRelIds != null && !userRelIds.isEmpty()) {
            Long notificationId = notificationE.getId();
            userRelIds.forEach(t -> {
                notificationUserRelRepository.baseCreate(notificationId, t);
            });
        }
        return notificationDTO;
    }

    @Override
    public DevopsNotificationVO update(Long projectId, DevopsNotificationVO notificationDTO) {
        DevopsNotificationE notificationE = ConvertHelper.convert(notificationDTO, DevopsNotificationE.class);
        DevopsNotificationE oldNotificationE = notificationRepository.baseQuery(notificationDTO.getId());
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
        notificationRepository.baseCreateOrUpdate(notificationE);
        updateUserRel(notificationDTO);
        return notificationDTO;
    }

    @Override
    public void delete(Long notificationId) {
        notificationRepository.baseDelete(notificationId);
        notificationUserRelRepository.delete(notificationId, null);
    }

    @Override
    public DevopsNotificationVO queryById(Long notificationId) {
        DevopsNotificationVO notificationDTO = ConvertHelper.convert(notificationRepository.baseQuery(notificationId), DevopsNotificationVO.class);
        List<Long> userRelIds = notificationUserRelRepository.baseListByNotificationId(notificationId).stream().map(DevopsNotificationUserRelE::getUserId).collect(Collectors.toList());
        notificationDTO.setUserRelIds(userRelIds);
        return notificationDTO;
    }

    @Override
    public PageInfo<DevopsNotificationVO> listByOptions(Long projectId, Long envId, String params, PageRequest pageRequest) {
        PageInfo<DevopsNotificationVO> page = ConvertPageHelper.convertPageInfo(notificationRepository.basePageByOptions(projectId, envId, params, pageRequest), DevopsNotificationVO.class);
        List<DevopsNotificationVO> list = new ArrayList<>();
        page.getList().forEach(t -> {
            if ("specifier".equals(t.getNotifyObject())) {
                List<DevopsNotificationUserRelVO> userRelDTOS = ConvertHelper.convertList(notificationUserRelRepository.baseListByNotificationId(t.getId()), DevopsNotificationUserRelVO.class);
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
        PageInfo<DevopsNotificationVO> dtoPage = new PageInfo<>();
        BeanUtils.copyProperties(page, dtoPage);
        dtoPage.setList(list);
        return dtoPage;
    }

    @Override
    public Set<String> check(Long projectId, Long envId) {
        Set<String> hashSet = new HashSet<>();
        notificationRepository.baseListByEnvId(projectId, envId).forEach(t -> Collections.addAll(hashSet, t.getNotifyTriggerEvent().split(",")));
        return hashSet;
    }

    @Override
    public ResourceCheckDTO checkResourceDelete(Long envId, String objectType) {
        ResourceCheckDTO resourceCheckDTO = new ResourceCheckDTO();
        List<DevopsNotificationE> devopsNotificationES = notificationRepository.baseListByEnvId(envId);
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
                        List<Long> userIds = notificationUserRelRepository.baseListByNotificationId(devopsNotificationE.getId()).stream().map(DevopsNotificationUserRelE::getUserId).collect(Collectors.toList());
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
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(envId);
        String objectCode = getObjectCode(objectId, objectType);

        //生成验证码，存放在redis
        String resendKey = String.format("choerodon:devops:env:%s:%s:%s", devopsEnvironmentE.getCode(), objectType, objectCode);
        String Captcha = String.valueOf(new Random().nextInt(899999) + 100000);
        redisTemplate.opsForValue().set(resendKey, Captcha, timeout, TimeUnit.SECONDS);


        //生成发送消息需要的模板对象
        DevopsNotificationE devopsNotificationE = notificationRepository.baseQuery(notificationId);
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
            PageInfo<UserVO> allOwnerUsersPage = iamRepository

                    .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), new RoleAssignmentSearchDTO(),

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
            List<Long> userIds = notificationUserRelRepository.baseListByNotificationId(devopsNotificationE.getId()).stream().map(DevopsNotificationUserRelE::getUserId).collect(Collectors.toList());
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
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(envId);
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
                DevopsServiceE devopsServiceE = devopsServiceRepository.baseQuery(objectId);
                code = devopsServiceE.getName();
                break;
            case INGRESS:
                DevopsIngressDTO devopsIngressDTO = devopsIngressRepository.basePageByOptions(objectId);
                code = devopsIngressDTO.getName();
                break;
            case CERTIFICATE:
                CertificationE certificationE = certificationRepository.baseQueryById(objectId);
                code = certificationE.getName();
                break;
            case CONFIGMAP:
                DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.baseQueryById(objectId);
                code = devopsConfigMapE.getName();
                break;
            case SECRET:
                DevopsSecretE devopsSecretE = devopsSecretRepository.baseQuery(objectId);
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

    private void updateUserRel(DevopsNotificationVO notificationDTO) {
        List<Long> addUserIds = new ArrayList<>();
        List<Long> oldUserIds = notificationUserRelRepository.baseListByNotificationId(notificationDTO.getId())
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
            addUserIds.forEach(t -> notificationUserRelRepository.baseCreate(notificationDTO.getId(), t));
        }
        if (!oldUserIds.isEmpty()) {
            oldUserIds.forEach(t -> notificationUserRelRepository.delete(notificationDTO.getId(), t));
        }
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
        PageInfo<DevopsNotificationDTO> notificationDOPage = PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsNotificationMapper.listByOptions(projectId, envId, searchParamMap, paramMap));
        return notificationDOPage;
    }

    public List<DevopsNotificationDTO> baseListByEnvId(Long projectId, Long envId) {
        DevopsNotificationDTO devopsNotificationDTO = new DevopsNotificationDTO();
        devopsNotificationDTO.setProjectId(projectId);
        devopsNotificationDTO.setEnvId(envId);
        return devopsNotificationMapper.select(devopsNotificationDTO);
    }
}
