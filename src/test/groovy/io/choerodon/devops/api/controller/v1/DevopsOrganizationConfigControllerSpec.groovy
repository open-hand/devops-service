package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ConfigVO
import io.choerodon.devops.api.vo.DefaultConfigVO
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
        List<DevopsConfigVO> devopsConfigVOS = new ArrayList<>()
        DevopsConfigVO devopsConfigVO = new DevopsConfigVO()
//        devopsConfigVO.setId(1L)
        devopsConfigVO.setName("test")
        devopsConfigVO.setProjectId(1L)
        devopsConfigVO.setAppServiceId(1L)
        devopsConfigVO.setCustom(true)
        devopsConfigVO.setType("harbor")

        ConfigVO configVO = new ConfigVO()
        configVO.setEmail("zhuang.chang@hand-china.com")
        configVO.setPassword("Handhand1357")
        configVO.setUserName("admin")
        configVO.setUrl("https://registry.saas.hand-china.com")
        configVO.setPrivate(true)
        devopsConfigVO.setConfig(configVO)


        devopsConfigVOS.add(devopsConfigVO)
        DevopsProjectDTO projectDTO = new DevopsProjectDTO()
        projectDTO.setAppId(1L)
        projectDTO.setDevopsAppGroupId(1L)
        projectDTO.setDevopsEnvGroupId(1L)
        projectDTO.setHarborProjectIsPrivate(true)
        projectDTO.setIamProjectId(1L)
        devopsProjectMapper.insert(projectDTO)

        when:
        def entity = restTemplate.postForEntity(BASE_URL, devopsConfigVOS, null, organization_id)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "Query"() {
        given:
        def url = BASE_URL + "?type={type}"
        when:
        def entity = restTemplate.getForEntity(url, List.class, 1L, "harbor")
        then:
        entity.body.size() == 1
    }

    def "QueryOrganizationDefaultConfig"() {
        given:
        def url = BASE_URL + "/default_config"
        when:
        def entity = restTemplate.getForEntity(url, DefaultConfigVO.class, 1L)
        then:
        entity.body.getHarborConfigUrl() != null
    }
}
