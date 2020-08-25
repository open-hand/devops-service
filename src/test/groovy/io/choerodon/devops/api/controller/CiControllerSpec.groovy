package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.AppServiceService
import io.choerodon.devops.app.service.HarborService
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsConfigDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.dto.iam.Tenant
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.RdupmClient
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.AppServiceMapper
import io.choerodon.devops.infra.mapper.DevopsConfigMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Subject(CiController)
@Import(IntegrationTestConfiguration)
class CiControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    CiController ciController

    @Autowired
    AppServiceService applicationService

    @Autowired
    AppServiceMapper applicationMapper
    @Autowired
    BaseServiceClientOperator baseServiceClientOperator
    @Autowired
    DevopsConfigMapper devopsConfigMapper

    @Shared
    ProjectDTO projectE = new ProjectDTO()
    @Shared
    AppServiceDTO appServiceDTO
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    String token = "token"

    BaseServiceClient baseServiceClient = Mock()
    RdupmClient rdupmClient = Mock()
    HarborService harborService = Mock()

    def setupSpec() {
        projectE.setId(init_id)
        projectE.setCode("pro")
        projectE.setOrganizationId(init_id)
        projectE.setOrganizationId(init_id)

        appServiceDTO = new AppServiceDTO()
        appServiceDTO.setId(init_id)
        appServiceDTO.setProjectId(project_id)
        appServiceDTO.setToken("token")
        appServiceDTO.setCode("app")

    }

    def setup() {

        applicationMapper.insert(appServiceDTO)

        ReflectionTestUtils.setField(baseServiceClientOperator, "baseServiceClient", baseServiceClient)
        ReflectionTestUtils.setField(applicationService, "harborService", harborService)

        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectE, HttpStatus.OK)
        baseServiceClient.queryIamProject(init_id, _ as Boolean, _ as Boolean, _ as Boolean) >> responseEntity

        Tenant tenant = new Tenant()
        tenant.setTenantId(1L)
        tenant.setTenantNum("org")
        ResponseEntity<Tenant> responseEntity1 = new ResponseEntity<>(tenant, HttpStatus.OK)
        baseServiceClient.queryOrganizationById(init_id, _ as Boolean) >> responseEntity1


    }

    def "QueryFile"() {
        given:"构造参数"
        def errorToken = "abc"
        def devopsConfigDTO = new DevopsConfigDTO()
        devopsConfigDTO.setName("sonar_default")
        devopsConfigDTO.setType("sonar")
        devopsConfigDTO.setConfig("7742d3fabdb3e2a76a65c547e48e6bc15b69a698")
        devopsConfigMapper.insert(devopsConfigDTO)

        def harborConfigDTO = new DevopsConfigDTO()
        harborConfigDTO.setId(init_id)
        harborConfigDTO.setType("custom")
        harborConfigDTO.setConfig("{\"url\":\"harbor.example.com\",\"userName\":\"123456\",\"password\":\"123456\"}")
        harborService.queryRepoConfigToDevopsConfig(init_id, init_id, "push") >> harborConfigDTO

        when: '应用查询ci脚本文件 - 应用服务不存在'
        def entity = restTemplate.getForEntity("/ci?token={token}", String.class, errorToken)
        then: '校验返回结果'
        // 校验响应码
        entity.statusCode.is4xxClientError()
        // 校验响应体
        entity.body == null

        when: '应用查询ci脚本文件 - 应用服务存在'
        def entity1 = restTemplate.getForEntity("/ci?token={token}", String.class, token)
        then: '校验返回结果'
        // 校验响应码
        entity1.statusCode.is2xxSuccessful()

    }

//    def "getSonarDefault"() {
//        when: '应用查询ci脚本文件'
//        def entity = restTemplate.getForEntity("/ci/sonar_default", SonarInfoVO.class)
//
//        then: '校验返回结果'
//        entity.statusCode.is2xxSuccessful()
//    }

}
