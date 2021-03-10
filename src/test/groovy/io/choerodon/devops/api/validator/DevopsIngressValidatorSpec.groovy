package io.choerodon.devops.api.validator


import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 *
 * @author zmf* @since 2021/3/10
 *
 */
@Subject(DevopsIngressValidator)
class DevopsIngressValidatorSpec extends Specification {
    @Unroll
    def "CheckHost"() {
        given: '准备数据'
        Exception ex = null

        when:
        try {
            DevopsIngressValidator.checkHost(host)
        } catch (Exception e) {
            ex = e
        }

        then:
        (ex == null) == noEx

        where:
        host         | noEx
        "a.com"      | true
        "a"          | true
        "*.com"      | true
        "abc.com"    | true
        "aa.abc.com" | true
        "*.abc.com"  | true
        "*..com"     | false
        "中文.com"     | false
        "*abc.com"   | false
        "abc.*.com"  | false
        "*com"       | false
        "*"          | false
    }
}
