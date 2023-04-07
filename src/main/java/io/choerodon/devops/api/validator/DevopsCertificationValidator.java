package io.choerodon.devops.api.validator;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.infra.util.K8sUtil;

/**
 * Creator: Runge
 * Date: 2018/8/21
 * Time: 14:51
 * Description:
 */
@Component
public class DevopsCertificationValidator {

    private final CertificationService certificationService;

    public DevopsCertificationValidator(@Lazy CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    /**
     * check certification
     *
     * @param name certification's name
     */
    public void checkCertification(Long envId, String name) {
        checkCertificationName(name);
        checkCertificationExists(envId, name);
    }

    private void checkCertificationName(String name) {
        if (!K8sUtil.NAME_PATTERN.matcher(name).matches()) {
            throw new CommonException("devops.certification.name.illegal");
        }
    }

    private void checkCertificationExists(Long envId, String name) {
        if (!certificationService.checkCertNameUniqueInEnv(envId, name, null)) {
            throw new CommonException("devops.certNameInEnv.notUnique");
        }
    }

}
