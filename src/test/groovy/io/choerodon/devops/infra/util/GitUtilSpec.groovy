package io.choerodon.devops.infra.util

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 *
 * @author zmf
 * @since 12/3/19
 *
 */
@Subject(GitUtil)
class GitUtilSpec extends Specification {
    @Unroll
    def "TestGetAppServiceSshUrl"() {
        given: "准备数据"
        def orgCode = "org"
        def proCode = "pro"
        def appServiceCode = "choerodon-cluster-agent"

        when: "调用"
        def result = GitUtil.getAppServiceSshUrl(url, orgCode, proCode, appServiceCode)

        then: "校验结果"
        result == expectResult

        where:
        url                     | expectResult
        "git.test.ssh"          | "git@git.test.ssh:org-pro/choerodon-cluster-agent.git"
        "git.test.ssh:2209"     | "ssh://git@git.test.ssh:2209/org-pro/choerodon-cluster-agent.git"
        "git@git.test.ssh"      | "git@git.test.ssh:org-pro/choerodon-cluster-agent.git"
        "git@git.test.ssh:2209" | "ssh://git@git.test.ssh:2209/org-pro/choerodon-cluster-agent.git"
    }
}
