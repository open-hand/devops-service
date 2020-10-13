package io.choerodon.devops.api.validator

import io.choerodon.devops.api.validator.annotation.EnumCheck
import io.choerodon.devops.infra.enums.DevopsHostType
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 *
 * @author zmf* @since 2020/9/15
 *
 */
@Subject(EnumCheckValidator)
class EnumCheckValidatorTest extends Specification {
    @EnumCheck(skipNull = false, enumClass = DevopsHostType)
    private String fieldToTest

    @Unroll
    def "IsValid"() {
        given:
        EnumCheckValidator enumCheckValidator = new EnumCheckValidator()
        enumCheckValidator.initialize(this.class.getDeclaredField("fieldToTest").getAnnotation(EnumCheck))

        when:
        def valid = enumCheckValidator.isValid(input, null)

        then:
        valid == result

        where:
        input             | result
        "a"               | false
        "deploy"          | true
        "Deploy"          | true
        "distribute_Test" | true
    }
}
