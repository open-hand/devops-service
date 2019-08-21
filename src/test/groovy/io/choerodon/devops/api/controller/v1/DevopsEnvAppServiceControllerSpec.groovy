package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsEnvAppServiceVO
import org.junit.runners.Parameterized
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author: trump* @date: 2019/8/20 19:50
 * @description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvFileErrorController)
@Stepwise
class DevopsEnvAppServiceControllerSpec extends Specification {
    @Autowired
    private TestRestTemplate restTemplate
    private static final base_url = "/v1/projects/{project_id}/env/app_services"

    def "BatchCreate"() {

    }

    def "BatchDelete"() {
    }

    def "ListAppByEnvId"() {
        when:
        def entity = restTemplate.getForEntity(base_url + "list_by_env?env_id=1L", List, 1L)
        then:
        entity.body.size() !=0
    }
    def "ListLabelByAppAndEnvId"() {
    }

    def "ListPortByAppAndEnvId"() {
    }

    def "ListNonRelatedAppService"() {
    }
}
