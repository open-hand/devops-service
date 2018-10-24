package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationTemplateDTO
import io.choerodon.devops.api.dto.ApplicationTemplateRepDTO
import io.choerodon.devops.api.dto.ApplicationTemplateUpdateDTO
import io.choerodon.devops.app.service.ApplicationTemplateService
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.repository.GitlabRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.repository.UserAttrRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.enums.Visibility
import io.choerodon.devops.infra.dataobject.ApplicationTemplateDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.ApplicationTemplateMapper
import io.choerodon.devops.infra.persistence.impl.ApplicationTemplateRepositoryImpl
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.*
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static org.mockito.Matchers.anyLong
import static org.mockito.Matchers.anyObject
import static org.mockito.Matchers.anyString
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/11
 * Time: 10:30
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class ApplicationTemplateControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private ApplicationTemplateMapper applicationTemplateMapper
    @Autowired
    private ApplicationTemplateService applicationTemplateService
    @Autowired
    private ApplicationTemplateRepositoryImpl applicationTemplateRepository

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository
    @Autowired
    @Qualifier("mockGitlabRepository")
    private GitlabRepository gitlabRepository
    @Autowired
    @Qualifier("mockUserAttrRepository")
    private UserAttrRepository userAttrRepository

    SagaClient sagaClient = Mockito.mock(SagaClient.class)
    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

//    private IamServiceClient iamServiceClient = Mock(IamServiceClient)
//    private SagaClient sagaClient = Mock(SagaClient)

    @Shared
    Organization organization = new Organization()
    @Shared
    OrganizationDO organizationDO = new OrganizationDO()
    @Shared
    UserAttrE userAttrE = new UserAttrE()
    @Shared
    GitlabGroupE gitlabGroupE = new GitlabGroupE()
    @Shared
    Map<String, Object> searchParam = new HashMap<>();

    @Shared
    Long org_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    Long template_id = 4L

    //初始化部分对象
    def setupSpec() {
        given:
        organization.setId(init_id)
        organization.setCode("org")

        organizationDO.setCode("orgDO")

        gitlabGroupE.setName("org_template")
        gitlabGroupE.setPath("org_template")
        gitlabGroupE.setVisibility(Visibility.PUBLIC)

        userAttrE.setIamUserId(init_id)
        userAttrE.setGitlabUserId(init_id)

        Map<String, Object> params = new HashMap<>();
        params.put("name", [])
        params.put("code", ["code"])
        searchParam.put("searchParam", params)
        searchParam.put("param", "")
    }

    //组织下创建应用模板
    def "createTemplate"() {
        given: "初始化数据"
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO()
        applicationTemplateDTO.setId(4L)
        applicationTemplateDTO.setCode("code")
        applicationTemplateDTO.setName("app")
        applicationTemplateDTO.setDescription("des")
        applicationTemplateDTO.setOrganizationId(1L)

        and: 'mock'
        applicationTemplateService.initMockService(sagaClient)
        applicationTemplateRepository.initMockService(iamServiceClient)
        Mockito.doReturn(new ResponseEntity<OrganizationDO>(organizationDO, HttpStatus.OK)).when(iamServiceClient).queryOrganizationById(anyLong())
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(anyString(), anyObject())
        userAttrRepository.queryById(_) >> userAttrE
        iamRepository.queryOrganizationById(_) >> organization
        gitlabRepository.queryGroupByName(_, _) >> null
        gitlabRepository.createGroup(_, _) >> gitlabGroupE

        when:
        def entity = restTemplate.postForEntity("/v1/organizations/{org_id}/app_templates", applicationTemplateDTO, ApplicationTemplateRepDTO.class, org_id)

        then:
        entity.statusCode.is2xxSuccessful()

        expect:
        ApplicationTemplateDO applicationTemplateDO = applicationTemplateMapper.selectByPrimaryKey(4L)
        applicationTemplateDO.name == "app"
    }

    //组织下更新应用模板
    def "updateTemplate"() {
        given:
        ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO = new ApplicationTemplateUpdateDTO()
        applicationTemplateUpdateDTO.setId(4L)
        applicationTemplateUpdateDTO.setName("updateName")
        applicationTemplateUpdateDTO.setDescription("des")

        when:
        restTemplate.put("/v1/organizations/{org_id}/app_templates", applicationTemplateUpdateDTO, org_id)

        then:
        ApplicationTemplateDO applicationTemplateDO = applicationTemplateMapper.selectByPrimaryKey(4L)

        expect:
        applicationTemplateDO.name == "updateName"
    }

    //组织下查询单个应用模板
    def "queryByAppTemplateId"() {
        when:
        def object = restTemplate.getForObject("/v1/organizations/{org_id}/app_templates/{template_id}", ApplicationTemplateRepDTO.class, org_id, template_id)

        then:
        object.code == "code"
    }

    //组织下分页查询应用模板
    def "listByOptions"() {
        when:
        def page = restTemplate.postForObject("/v1/organizations/{org_id}/app_templates/list_by_options", searchParam, Page.class, org_id)

        then:
        page.size() == 1

        expect:
        page.get(0).code == "code"
    }

    //组织下分页查询应用模板
    def "listByOrgId"() {
        when:
        def list = restTemplate.getForObject("/v1/organizations/{org_id}/app_templates", List.class, org_id)

        then:
        list.size() == 4

        expect:
        list.get(3).code == "code"
    }
    //创建模板校验名称是否存在
    def "checkName"() {
        when:
        def entity = restTemplate.getForEntity("/v1/organizations/{org_id}/app_templates/checkName?name={name}", Object.class, org_id, "name")

        then:
        entity.statusCode.is2xxSuccessful()
        entity.body == null

        when:
        def entity2 = restTemplate.getForEntity("/v1/organizations/{org_id}/app_templates/checkName?name={name}", Object.class, org_id, "updateName")

        then:
        entity2.statusCode.is2xxSuccessful()
        entity2.body.failed == true
    }
    //创建模板校验编码是否存在
    def "checkCode"() {
        when:
        def entity = restTemplate.getForEntity("/v1/organizations/{org_id}/app_templates/checkCode?code={code}", Object.class, org_id, "testCode")

        then:
        entity.statusCode.is2xxSuccessful()
        entity.body == null

        when:
        def entity2 = restTemplate.getForEntity("/v1/organizations/{org_id}/app_templates/checkCode?code={code}", Object.class, org_id, "code")

        then:
        entity2.statusCode.is2xxSuccessful()
        entity2.body.failed == true
    }

    //组织下删除应用模板
    def "deleteTemplate"() {
        given:
        userAttrRepository.queryById(_ as Long) >> userAttrE
        gitlabRepository.deleteProject(_ as Integer, _ as Integer) >> null

        when:
        restTemplate.delete("/v1/organizations/{project_id}/app_templates/{template_id}", org_id, 4L)

        then:
        ApplicationTemplateDO applicationTemplateDO = applicationTemplateMapper.selectByPrimaryKey(4L)

        expect:
        applicationTemplateDO == null
    }
}
