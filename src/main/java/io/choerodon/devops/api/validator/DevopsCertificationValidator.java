package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.domain.application.repository.CertificationRepository;

/**
 * Creator: Runge
 * Date: 2018/8/21
 * Time: 14:51
 * Description:
 */
@Component
public class DevopsCertificationValidator {
    private static final String NAME_PATTERN = "[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*";

    private CertificationService certificationService;

    @Autowired
    public DevopsCertificationValidator(CertificationService certificationService) {
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
        if (!Pattern.matches(NAME_PATTERN, name)) {
            throw new CommonException("error.certification.name.illegal");
        }
    }

    private void checkCertificationExists(Long envId, String name) {
        if (!certificationService.baseCheckCertNameUniqueInEnv(envId, name)) {
            throw new CommonException("error.certNameInEnv.notUnique");
        }
    }

}
