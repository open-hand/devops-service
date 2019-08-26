package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.HarborMarketVO
import io.choerodon.devops.infra.dto.harbor.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author: trump*
 * @date: 2019/8/19 19:37
 * @description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(AppServiceVersionController)
@Stepwise
class OrgAppMarketControllerSpec extends Specification {
    @Autowired
    private TestRestTemplate restTemplate

    private static final base_url = "/v1/organizations/app_market"

    def "ListAllAppServices"() {
        when: "调用方法"
        def entity = restTemplate.getForEntity(base_url + "/list_app_services", List.class)
        then: "校验结果"
        entity.statusCode.is2xxSuccessful()
    }

    def "PageByAppId"() {
        given:
        def app_id = 1L
        when:
        def entity = restTemplate.postForEntity(base_url + "/page_app_services?app_id={app_id}", null, com.github.pagehelper.PageInfo, app_id)
        then:
        entity.statusCode.is2xxSuccessful()

    }

//    def "Upload"() {
//    }
//
//    def "ListVersionsByAppServiceId"() {
//        given:
//        def app_service_id = 1L
//        when:
//        def entity = restTemplate.getForEntity(base_url + "/list_versions/{app_service_id}", List.class, app_service_id)
//        then:
//        entity.body.size() != 0
//    }

    def "CreateHarborRepository"() {
        given:
        HarborMarketVO harborMarketVO = new HarborMarketVO()
        harborMarketVO.setProjectCode("test_scp")
        User user = new User()
        user.setEmail("test001@qq.com")
        user.setPassword("Handhand")
        user.setUsername("test001")
        user.setRealname("test001")
        harborMarketVO.setUser(user)
        when:
        def entity = restTemplate.postForEntity(base_url + "/harbor_repo", harborMarketVO, String.class)
        then:
        entity.body.size() != 0
    }

    def "DownLoadApp"() {
    }
}
