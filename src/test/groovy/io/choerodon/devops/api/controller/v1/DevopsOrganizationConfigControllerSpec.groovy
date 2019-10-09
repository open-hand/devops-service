package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ConfigVO
import io.choerodon.devops.api.vo.DefaultConfigVO
import io.choerodon.devops.api.vo.DevopsConfigRepVO
import io.choerodon.devops.api.vo.DevopsConfigVO
import io.choerodon.devops.api.vo.DevopsNotificationVO
import io.choerodon.devops.infra.dto.DevopsProjectDTO
import io.choerodon.devops.infra.mapper.DevopsProjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author: 25499* @date: 2019/8/30 9:06
 * @description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsIngressController)
@Stepwise
class DevopsOrganizationConfigControllerSpec extends Specification {
    private static final BASE_URL = "/v1/organizations/{organization_id}/organization_config"
    def organization_id = 1L
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsProjectMapper devopsProjectMapper

    def "Create"() {
        given:
        ConfigVO configVO = new ConfigVO()
        configVO.setEmail("zhuang.chang@hand-china.com")
        configVO.setPassword("Handhand1357")
        configVO.setUserName("admin")
        configVO.setUrl("https://registry.saas.hand-china.com")
        configVO.setPrivate(true)


        DevopsConfigVO devopsConfigVO = new DevopsConfigVO()
        devopsConfigVO.setHarborPrivate(false)
        devopsConfigVO.setType("harbor")
        devopsConfigVO.setName("app")
        devopsConfigVO.setId(1L)
        devopsConfigVO.setOrganizationId(1L)
        devopsConfigVO.setProjectId(1L)
        devopsConfigVO.setAppServiceId(1L)
        devopsConfigVO.setCustom(false)
        devopsConfigVO.setConfig(configVO)

        DevopsConfigRepVO devopsConfigRepVO = new DevopsConfigRepVO()
        devopsConfigRepVO.setHarborPrivate(false)
        devopsConfigRepVO.setHarbor(devopsConfigVO)
        when:
        def entity = restTemplate.postForEntity(BASE_URL, devopsConfigRepVO, null, organization_id)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "Query"() {
        given:
        def url = BASE_URL
        when:
        def entity = restTemplate.getForEntity(url, DevopsConfigRepVO.class, 1L)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "QueryOrganizationDefaultConfig"() {
        given:
        def url = BASE_URL + "/default_config"
        when:
        def entity = restTemplate.getForEntity(url, DefaultConfigVO.class, 1L)
        then:
        entity.body!= null
    }
    def "checkHarbor"(){
        given:
        def url = BASE_URL+"/check_harbor?url=https://registry.saas.hand-china.com&" +
                "userName=admin&password=Handhand1357&email=zhuang.chang@hand-china.com"
        Map<String, Object> map = new HashMap<>()
        when:
        def entity = restTemplate.getForEntity(url, Boolean.class, 1L)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "checkChart"() {
        given:
        def url = BASE_URL + "/check_chart?url=https://registry.saas.hand-china.com"
        when:
        def entity = restTemplate.getForEntity(url, Boolean.class, 1L)
        then:
        entity.getStatusCode().is2xxSuccessful()
    }
}
