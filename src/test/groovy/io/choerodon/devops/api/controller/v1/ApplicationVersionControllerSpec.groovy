package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository
import io.choerodon.devops.infra.dataobject.ApplicationVersionDO
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import io.choerodon.websocket.helper.EnvSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/17
 * Time: 13:43
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class ApplicationVersionControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository

    def "PageByOptions"() {
        given:
        String infra = "{\"searchParam\":{\"version\":[\"version\"]}}"
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/json;UTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        when:
        restTemplate.postForObject("/v1/projects/1/app_version/list_by_options?appId=1", strEntity, Page.class)

        then:
        true
    }

    def "QueryByAppId"() {
        when:
        def list = restTemplate.getForObject("/v1/projects/1/apps/1/version/list?is_publish=true", List.class)

        then:
        !list.isEmpty()
    }

    def "QueryDeployedByAppId"() {
        when:
        def list = restTemplate.getForObject("/v1/projects/1/apps/1/version/list_deployed", List.class)

        then:
        !list.isEmpty()
    }

    def "QueryByAppIdAndEnvId"() {
        when:
        def list = restTemplate.getForObject("/v1/projects/1/apps/1/version?envId=1", List.class)

        then:
        !list.isEmpty()
    }

    def "PageByApp"() {
        given:
        String infra = "{\"searchParam\":{\"version\":[\"version\"]}}"
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/json;UTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        Map<String, EnvSession> envs = new HashMap<>()
        EnvSession envSession = new EnvSession()
        envSession.setEnvId(1L)
        envSession.setVersion("")
        envs.put("testenv", envSession)

        when:
        restTemplate.postForObject("/v1/projects/1/apps/1/version/list_by_options", strEntity, Page.class)

        then:
        true
    }

    def "GetUpgradeAppVersion"() {
        given:
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
        applicationVersionDO.setAppId(1L)
        applicationVersionDO.setValueId(1L)
        applicationVersionDO.setIsPublish(1L)
        applicationVersionDO.setCommit("commit1")
        applicationVersionDO.setVersion("version1")
        applicationVersionDO.setCreationDate(new Date(2018, 9, 17, 15, 50, 0))
        applicationVersionMapper.insert(applicationVersionDO)

        when:
        def list = restTemplate.getForObject("/v1/projects/1/version/1/upgrade_version", List.class)

        then:
        !list.isEmpty()
    }
}
