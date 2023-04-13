package io.choerodon.devops.app.service.impl;


import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.MessageSender;
import org.hzero.boot.message.entity.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.enums.MessageAdditionalType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.ResourceCheckVO;
import io.choerodon.devops.api.vo.notify.MessageSettingVO;
import io.choerodon.devops.api.vo.notify.TargetUserDTO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.TriggerObject;
import io.choerodon.devops.infra.feign.HzeroMessageClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.StringMapBuilder;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:09 2019/5/13
 * Description:
 */
@Service
public class DevopsNotificationServiceImpl implements DevopsNotificationService {

    private static final String NOTIFY_TYPE = "resourceDelete";
    public static final Gson gson = new Gson();
    private static final Long TIMEOUT = 600L;
    private static final String MOBILE = "mobile";

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
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
    private HzeroMessageClient hzeroMessageClient;
    @Autowired
    private MessageClient messageClient;
    @Autowired
    private PermissionHelper permissionHelper;


    @Override
    public ResourceCheckVO checkResourceDelete(Long projectId, Long envId, String objectType) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);
        ResourceCheckVO resourceCheckVO = new ResourceCheckVO();
        MessageSettingVO messageSettingVO = hzeroMessageClient.queryByEnvIdAndEventNameAndProjectIdAndCode(NOTIFY_TYPE, devopsEnvironmentDTO.getProjectId(), MessageCodeConstants.RESOURCE_DELETE_CONFIRMATION, envId, objectType);
        if (Objects.isNull(messageSettingVO)) {
            return resourceCheckVO;
        }
        // 返回删除对象时,获取验证码方式和所通知的目标人群
        List<String> method = new ArrayList<>();
        fillMethod(method, messageSettingVO);
        if (CollectionUtils.isEmpty(method)) {
            return new ResourceCheckVO();
        }
        resourceCheckVO.setMethod(String.join(",", method));
        resourceCheckVO.setNotificationId(messageSettingVO.getId());
        List<TargetUserDTO> targetUserDTOS = messageSettingVO.getTargetUserDTOS();
        if (CollectionUtils.isEmpty(targetUserDTOS)) {
            return new ResourceCheckVO();
        }
        List<String> userList = new ArrayList<>();
        fillTargetUser(userList, targetUserDTOS);
        resourceCheckVO.setUser(String.join(",", userList));
        return resourceCheckVO;
    }

    private void fillTargetUser(List<String> userList, List<TargetUserDTO> targetUserDTOS) {
        Set<Long> userIdsSet = new HashSet<>();
        for (TargetUserDTO targetUserDTO : targetUserDTOS) {
            if (TriggerObject.PROJECT_OWNER.getObject().equals(targetUserDTO.getType())) {
                userList.add("项目所有者");
            }

            if (TriggerObject.HANDLER.getObject().equals(targetUserDTO.getType())) {
                userIdsSet.add(GitUserNameUtil.getUserId());
            }
            if (TriggerObject.SPECIFIER.getObject().equals(targetUserDTO.getType())) {
                userIdsSet.add(targetUserDTO.getUserId());
            }
        }
        List<Long> userIds = new ArrayList<>(userIdsSet);
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        userList.add(StringUtils.join(baseServiceClientOperator.listUsersByIds(userIds).stream().map(userDTO -> {
            if (userDTO.getRealName() != null) {
                return userDTO.getRealName();
            } else {
                return userDTO.getLoginName();
            }
        }).toArray(), ","));
    }

    private void fillMethod(List<String> method, MessageSettingVO messageSettingVO) {
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
    public void sendMessage(Long projectId, Long envId, Long notificationId, Long objectId, String objectType) {
        // notificationId为messageSettingVO 的ID
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);
        String objectCode = getObjectCode(objectId, objectType);

        // 生成验证码，存放在redis
        String resendKey = String.format("choerodon:devops:env:%s:%s:%s", devopsEnvironmentDTO.getCode(), objectType, objectCode);
        String captcha = String.valueOf(new Random().nextInt(899999) + 100000);
        redisTemplate.opsForValue().set(resendKey, captcha, TIMEOUT, TimeUnit.SECONDS);


        //生成发送消息需要的模板对象
        MessageSettingVO messageSettingVO = hzeroMessageClient.queryByEnvIdAndEventNameAndProjectIdAndCode(NOTIFY_TYPE, devopsEnvironmentDTO.getProjectId(), MessageCodeConstants.RESOURCE_DELETE_CONFIRMATION, envId, objectType);

        StringMapBuilder params = StringMapBuilder.newBuilder();
        List<IamUserDTO> userES = baseServiceClientOperator.listUsersByIds(ArrayUtil.singleAsList(GitUserNameUtil.getUserId()));
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

        List<TargetUserDTO> targetUserDTOS = messageSettingVO.getTargetUserDTOS();
        //通知对象为null，则不发消息
        if (CollectionUtils.isEmpty(targetUserDTOS)) {
            return;
        }

        List<Receiver> users = new ArrayList<>();

        List<String> phones = new ArrayList<>();
        targetUserDTOS.forEach(e -> {
            if (TriggerObject.HANDLER.getObject().equals(e.getType())) {
                Receiver user = new Receiver();
                phones.add(userES.get(0).getPhone());
                user.setEmail(GitUserNameUtil.getEmail());
                user.setUserId(GitUserNameUtil.getUserId());
                user.setTargetUserTenantId(DetailsHelper.getUserDetails().getTenantId());
                user.setPhone(userES.get(0).getPhone());
                users.add(user);
            }
            if (TriggerObject.PROJECT_OWNER.getObject().equals(e.getType())) {
                List<IamUserDTO> iamUserDTOS = baseServiceClientOperator
                        .listProjectOwnerByProjectId(devopsEnvironmentDTO.getProjectId());
                if (!iamUserDTOS.isEmpty()) {
                    iamUserDTOS.forEach(v -> processReceiver(v, users, phones));
                }
            }
            if (TriggerObject.SPECIFIER.getObject().equals(e.getType())) {
                List<Long> userIds = new ArrayList<>();
                userIds.add(e.getUserId());
                baseServiceClientOperator.listUsersByIds(userIds).forEach(k -> processReceiver(k, users, phones));
            }
        });
        params.put(MOBILE, StringUtils.join(phones, ","));

        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put(MessageAdditionalType.PARAM_PROJECT_ID.getTypeName(), devopsEnvironmentDTO.getProjectId());
        additionalParams.put(MessageAdditionalType.PARAM_ENV_ID.getTypeName(), devopsEnvironmentDTO.getId());
        additionalParams.put(MessageAdditionalType.PARAM_EVENT_NAME.getTypeName(), objectType);

        MessageSender messageSender = new MessageSender();
        messageSender.setTenantId(0L);
        messageSender.setMessageCode(MessageCodeConstants.RESOURCE_DELETE_CONFIRMATION);
        messageSender.setArgs(params.build());
        messageSender.setAdditionalInformation(additionalParams);
        messageSender.setReceiverAddressList(users);

        try {
            //根据不同的通知方式发送验证码
            messageClient.async().sendMessage(messageSender);
        } catch (Exception e) {
            redisTemplate.delete(resendKey);
            throw new CommonException("devops.msg.send.failed");
        }
    }

    private void processReceiver(IamUserDTO user, List<Receiver> receivers, List<String> phones) {
        Receiver receiver = new Receiver();
        receiver.setEmail(user.getEmail());
        receiver.setUserId(user.getId());
        receiver.setPhone(user.getPhone());
        receiver.setTargetUserTenantId(user.getOrganizationId());
        receivers.add(receiver);
        phones.add(user.getPhone());
    }


    @Override
    public void validateCaptcha(Long projectId, Long envId, Long objectId, String objectType, String captcha) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);
        String objectCode = getObjectCode(objectId, objectType);
        String resendKey = String.format("choerodon:devops:env:%s:%s:%s", devopsEnvironmentDTO.getCode(), objectType, objectCode);
        if (!captcha.equals(redisTemplate.opsForValue().get(resendKey))) {
            throw new CommonException("devops.captcha.error");
        }
        redisTemplate.delete(resendKey);
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


