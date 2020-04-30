package io.choerodon.devops.infra.util

import spock.lang.Specification

import io.choerodon.devops.infra.dto.maven.Repository
import io.choerodon.devops.infra.dto.maven.RepositoryPolicy
import io.choerodon.devops.infra.dto.maven.Server

/**
 *
 * @author zmf
 * @since 20-4-15
 *
 */
class MavenSettingsUtilSpec extends Specification {
    def "GenerateMavenSettings"() {
        given: "准备数据"
        Repository privateSnapshot = new Repository("zmf-snapshot", "zmf-snapshot", "http://localhost:8081/repository/zmf-snapshot/", new RepositoryPolicy(false), new RepositoryPolicy(true, "always"))
        Repository privateRelease = new Repository("zmf-release", "zmf-release", "http://localhost:8081/repository/zmf-release/", new RepositoryPolicy(true, "daily"), new RepositoryPolicy(false))
        List<Repository> repositories = [privateSnapshot, privateRelease]
        Server snapshotServer = new Server("zmf-snapshot", "username", "username")
        Server releaseServer = new Server("zmf-release", "username", "username")
        List<Server> servers = [snapshotServer, releaseServer]

        when: "调用方法"
        String result = MavenSettingsUtil.generateMavenSettings(servers, repositories)
        println(result)

        then: "校验结果"
        noExceptionThrown()
    }
}
