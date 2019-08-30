package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.SonarInfoVO
import io.choerodon.devops.app.service.SonarService
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author zhaotianxin* @since 2019/8/30
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(SonarController)
@Stepwise
class SonarControllerSpec extends Specification {
    @Autowired
    private TestRestTemplate restTemplate

    def "getSonarInfo"(){
        when:
        URI uri = new URI("/sonar/info")
        HttpEntity<String> httpEntity = new HttpEntity<>()
        def entity = restTemplate.exchange(uri, HttpMethod.GET,httpEntity,SonarInfoVO.class)
        then:
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getPassword() != null
    }

}
