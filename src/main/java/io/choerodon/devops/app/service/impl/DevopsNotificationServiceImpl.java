package io.choerodon.devops.app.service.impl;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.DevopsNotificationTransferDataVO;
import io.choerodon.devops.api.vo.NotifyVO;
import io.choerodon.devops.api.vo.ResourceCheckVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.notify.MessageSettingVO;
import io.choerodon.devops.api.vo.notify.TargetUserDTO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.LabelType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.TriggerObject;
import io.choerodon.devops.infra.feign.NotifyClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsNotificationMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:09 2019/5/13
 * Description:
 */
@Service
public class DevopsNotificationServiceImpl implements DevopsNotificationService {

    public static final String RESOURCE_DELETE_CONFIRMATION = "resourceDeleteConfirmation";
    private static final String CODE = "resourceDeleteConfirmation";
    private static final String NOTIFY_TYPE = "resourceDelete";
    public static final Gson gson = new Gson();
    private static final Long TIMEOUT = 600L;
    private static final String MOBILE = "mobile";

    @Autowired
    private DevopsNotificationMapper devopsNotificationMapper;
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

    @Override
    public ResourceCheckVO checkResourceDelete(Long envId, String objectType) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        ResourceCheckVO resourceCheckVO = new ResourceCheckVO();
        MessageSettingVO messageSettingVO = notifyClient.queryByEnvIdAndEventNameAndProjectIdAndCode(NOTIFY_TYPE, devopsEnvironmentDTO.getProjectId(), CODE, envId, objectType);
        if (Objects.isNull(messageSettingVO)) {
            return resourceCheckVO;
        }
        //返回删除对象时,获取验证码方式和所通知的目标人群
        List<String> method = new ArrayList<>();
        fillMetod(method, messageSettingVO);
        if (CollectionUtils.isEmpty(method)) {
            return new ResourceCheckVO();
        }
        resourceCheckVO.setMethod(method.stream().collect(Collectors.joining(",")));
        resourceCheckVO.setNotificationId(messageSettingVO.getId());
        List<TargetUserDTO> targetUserDTOS = messageSettingVO.getTargetUserDTOS();
        if (CollectionUtils.isEmpty(targetUserDTOS)) {
            return new ResourceCheckVO();
        }
        List<String> userList = new ArrayList<>();
        fillTargetUser(userList, targetUserDTOS);
        resourceCheckVO.setUser(userList.stream().collect(Collectors.joining(",")));
        return resourceCheckVO;
    }

    private void fillTargetUser(List<String> userList, List<TargetUserDTO> targetUserDTOS) {
        Set<Long> userIdsSet = new HashSet<>();
        for (TargetUserDTO targetUserDTO : targetUserDTOS) {
            if (TriggerObject.PROJECT_OWNER.getObject().equals(targetUserDTO.getType())) {
                userList.add("项目所有者");
            }

            if (TriggerObject.HANDLER.getObject().equals(targetUserDTO.getType())) {
                userIdsSet.add(GitUserNameUtil.getUserId().longValue());
            }
            if (TriggerObject.SPECIFIER.getObject().equals(targetUserDTO.getType())) {
                userIdsSet.add(targetUserDTO.getUserId());
            }
        }
        List<Long> userIds = new ArrayList<>(userIdsSet);
        userList.add(StringUtils.join(baseServiceClientOperator.listUsersByIds(userIds).stream().map(userDTO -> {
            if (userDTO.getRealName() != null) {
                return userDTO.getRealName();
            } else {
                return userDTO.getLoginName();
            }
        }).toArray(), ","));
    }

    private void fillMetod(List<String> method, MessageSettingVO messageSettingVO) {
        if (messageSettingVO.getPmEnable()) {
            method.add("站内信");
        }
        if (messageSettingVO.getEmailEnable()) {
            method.add("邮件");
        }
        if (messageSettingVO.getSmsEnable()) {
            method.add("短消息");
        }
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
        //由于短信模板内容的问题，暂时需要传入此instance,后续统一改成object和objectType
        params.put("instance", objectCode);
        notifyVO.setEnvId(envId);
        notifyVO.setEventName(objectType);
        notifyVO.setNotifyType(NOTIFY_TYPE);
        List<TargetUserDTO> targetUserDTOS = messageSettingVO.getTargetUserDTOS();
        //通知对象为null，则不发消息
        if (CollectionUtils.isEmpty(targetUserDTOS)) {
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
            if (TriggerObject.PROJECT_OWNER.getObject().equals(e.getType())) {
                List<IamUserDTO> iamUserDTOS = baseServiceClientOperator
                        .listProjectOwnerByProjectId(devopsEnvironmentDTO.getProjectId());
                if (!iamUserDTOS.isEmpty()) {
                    iamUserDTOS.forEach(v -> {
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
                baseServiceClientOperator.listUsersByIds(userIds).stream().forEach(k -> {
                    NoticeSendDTO.User user = new NoticeSendDTO.User();
                    user.setEmail(k.getEmail());
                    user.setId(k.getId());
                    users.add(user);
                    phones.add(k.getPhone());
                });
            }
        });
        params.put(MOBILE, StringUtils.join(phones, ","));
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
    public List<DevopsNotificationTransferDataVO> transferDate() {
        return devopsNotificationMapper.transferData();
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
}


