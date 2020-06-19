package io.choerodon.devops.infra.util

import java.util.regex.Pattern

import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject
import spock.lang.Unroll

import io.choerodon.devops.infra.enums.EnvironmentType

/**
 *
 * @author zmf* @since 12/3/19
 *
 */
@Subject(GitUtil)
@Stepwise
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
        url                          | expectResult
        "git.test.ssh"               | "git@git.test.ssh:org-pro/choerodon-cluster-agent.git"
        "git.test.ssh:2209"          | "ssh://git@git.test.ssh:2209/org-pro/choerodon-cluster-agent.git"
        "git@git.test.ssh"           | "git@git.test.ssh:org-pro/choerodon-cluster-agent.git"
        "git@git.test.ssh:2209"      | "ssh://git@git.test.ssh:2209/org-pro/choerodon-cluster-agent.git"
        "ssh://git@192.168.1.2:2209" | "ssh://git@192.168.1.2:2209/org-pro/choerodon-cluster-agent.git"
    }

    @Unroll
    def "TestGetEnvSshUrl"() {
        given: "准备数据"
        def orgCode = "org"
        def proCode = "pro"
        def envCode = "choerodon-cluster-agent"
        def clusterCode = "cluster-code";
        def pattern = Pattern.compile('^[-+]?[\\d]*$');

        when: "调用"
        def result = GitUtil.getGitlabSshUrl(pattern, url, orgCode, proCode, envCode, EnvironmentType.USER, clusterCode)

        then: "校验结果"
        result == expectResult

        where:
        url                          | expectResult
        "git.test.ssh"               | "git@git.test.ssh:org-pro-gitops/choerodon-cluster-agent.git"
        "git.test.ssh:2209"          | "ssh://git@git.test.ssh:2209/org-pro-gitops/choerodon-cluster-agent.git"
        "git@git.test.ssh"           | "git@git.test.ssh:org-pro-gitops/choerodon-cluster-agent.git"
        "git@git.test.ssh:2209"      | "ssh://git@git.test.ssh:2209/org-pro-gitops/choerodon-cluster-agent.git"
        "ssh://git@192.168.1.2:2209" | "ssh://git@192.168.1.2:2209/org-pro-gitops/choerodon-cluster-agent.git"
    }
}
