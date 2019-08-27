package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.UserAttrVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author: 25499* @date: 2019/8/26 15:33
 * @description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(GitlabWebHookController)
@Stepwise
class DevopsUserControllerSpec extends Specification {
    @Autowired
    private TestRestTemplate restTemplate

    private static final base_url = "/v1/projects/{project_id}/users/{user_id}"
    def "QueryByUserId"() {
        when: "调用方法"
        def entity = restTemplate.getForEntity(base_url, UserAttrVO.class,490,1)
        then: "校验结果"
        entity.getBody().getGitlabUserId()
        entity.statusCode.is2xxSuccessful()
    }
}
