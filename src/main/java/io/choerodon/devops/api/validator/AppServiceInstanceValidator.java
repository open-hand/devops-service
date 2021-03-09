package io.choerodon.devops.api.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.infra.util.CommonExAssertUtil;

/**
 * Created by n!Ck
 * Date: 2018/11/20
 * Time: 9:51
 * Description:
 */
@Component
public class AppServiceInstanceValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServiceInstanceValidator.class);
    //appServiceInstance name
    private static final String NAME_PATTERN = "[a-z]([-a-z0-9]*[a-z0-9])?";
    @Autowired
    private Validator validator;
    /**
     * 允许的最大部署条数
     */
    @Value("${devops.batch.deployment.maxSize:20}")
    private int batchDeploymentMaxSize;

    private AppServiceInstanceValidator() {
    }


    public static void checkName(String name) {
        if (!isNameValid(name)) {
            throw new CommonException("error.app.instance.name.notMatch");
        }
    }

    public static boolean isNameValid(String name) {
        return Pattern.matches(NAME_PATTERN, name);
    }

    /**
     * 校验批量部署的请求参数
     *
     * @param appServiceDeployVOS 批量部署的信息
     */
    public void validateBatchDeployment(List<AppServiceDeployVO> appServiceDeployVOS) {
        if (appServiceDeployVOS.isEmpty()) {
            throw new CommonException("error.request.instance.empty");
        }
        int size = appServiceDeployVOS.size();
        if (size > batchDeploymentMaxSize) {
            throw new CommonException("error.batch.deployment.size", size);
        }

        Long envId = appServiceDeployVOS.get(0).getEnvironmentId();
        CommonExAssertUtil.assertTrue(envId != null, "error.env.id.null");
        appServiceDeployVOS.forEach(ins -> ins.setEnvironmentId(envId));

        List<String> instanceCodes = new ArrayList<>(size);
        List<String> serviceNames = new ArrayList<>(size);
        List<String> ingressNames = new ArrayList<>(size);

        // 校验实例
        for (AppServiceDeployVO appServiceDeployVO : appServiceDeployVOS) {
            Set<ConstraintViolation<AppServiceDeployVO>> set = validator.validate(appServiceDeployVO);
            if (!CollectionUtils.isEmpty(set)) {
                set.stream().findFirst().ifPresent(cv -> {
                    LOGGER.info("App-service-validator: invalid instance. the message is {}", cv.getMessageTemplate());
                    throw new CommonException(cv.getMessageTemplate());
                });
            }
            if (instanceCodes.contains(appServiceDeployVO.getInstanceName())) {
                throw new CommonException("error.app.service.name.duplicated.in.list", appServiceDeployVO.getInstanceName());
            }
            // 实例名称最大53，限制于helm release的名称长度，参考issue: https://github.com/helm/helm/issues/6006
            CommonExAssertUtil.assertTrue(appServiceDeployVO.getInstanceName().length() <= 53, "error.app.instance.name.length");
            instanceCodes.add(appServiceDeployVO.getInstanceName());

            // 校验网络
            if (appServiceDeployVO.getDevopsServiceReqVO() != null) {
                Set<ConstraintViolation<DevopsServiceReqVO>> serviceSet = validator.validate(appServiceDeployVO.getDevopsServiceReqVO());
                if (!CollectionUtils.isEmpty(serviceSet)) {
                    serviceSet.stream().findFirst().ifPresent(cv -> {
                        LOGGER.info("App-service-validator: invalid service. the message is {}", cv.getMessageTemplate());
                        throw new CommonException(cv.getMessageTemplate());
                    });
                }
                if (serviceNames.contains(appServiceDeployVO.getDevopsServiceReqVO().getName())) {
                    throw new CommonException("error.service.name.duplicated.in.list", appServiceDeployVO.getDevopsServiceReqVO().getName());
                }
                serviceNames.add(appServiceDeployVO.getDevopsServiceReqVO().getName());
            }

            // 校验Ingress
            if (appServiceDeployVO.getDevopsIngressVO() != null) {
                if (ingressNames.contains(appServiceDeployVO.getDevopsIngressVO().getName())) {
                    throw new CommonException("error.ingress.name.duplicated.in.list", appServiceDeployVO.getDevopsIngressVO().getName());
                }
                ingressNames.add(appServiceDeployVO.getDevopsIngressVO().getName());
                DevopsIngressValidator.checkVOForBatchDeployment(appServiceDeployVO.getDevopsIngressVO());
            }
        }
    }
}
