package io.choerodon.devops.api.validator

import io.choerodon.core.exception.CommonException
import io.choerodon.devops.domain.application.repository.CertificationRepository
import org.mockito.Mockito
import spock.lang.Specification

/**
 *
 * @author zmf
 *
 */
class DevopsCertificationValidatorSpec extends Specification {
    CertificationRepository mockCertificationRepository = Mockito.mock(CertificationRepository)
    DevopsCertificationValidator devopsCertificationValidator = new DevopsCertificationValidator(mockCertificationRepository)

    def "CheckCertification For Valid Input"() {
        given: "准备数据"
        String name = "z-z"
        Long envId = 1L

        Mockito.when(mockCertificationRepository.baseCheckCertNameUniqueInEnv(Mockito.anyLong(), Mockito.anyString())).thenReturn(true)

        when: "调用方法"
        devopsCertificationValidator.checkCertification(envId, name)

        then: "校验结果"
        noExceptionThrown()
    }

    def "CheckCertification For Invalid name and envId"() {
        given: "准备数据"
        Long envId = 1L

        Mockito.when(mockCertificationRepository.baseCheckCertNameUniqueInEnv(Mockito.anyLong(), Mockito.anyString())).thenReturn(false)

        when: "调用方法"
        devopsCertificationValidator.checkCertification(envId, name)

        then: "校验结果"
        def e = thrown(CommonException)
        e.getCode() == code

        where: "多次数据集"
        name   | code
        "z-z&" | "error.certification.name.illegal"
        "z-z"  | "error.certNameInEnv.notUnique"
    }
}
