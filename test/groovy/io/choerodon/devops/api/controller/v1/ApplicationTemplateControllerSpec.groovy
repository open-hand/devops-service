package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationTemplateDTO
import io.choerodon.devops.api.dto.ApplicationTemplateRepDTO
import io.choerodon.devops.app.service.*
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.repository.ApplicationTemplateRepository
import io.choerodon.devops.domain.application.repository.GitlabRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.repository.UserAttrRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.mock.DetachedMockFactory

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class ApplicationTemplateControllerSpec extends Specification {

    private final detachedMockFactory = new DetachedMockFactory()

    @Autowired
    TestRestTemplate restTemplate

    @Shared
    private static final String TEMPLATE = "template"
    @Shared
    private static final String MASTER = "master"
    @Shared
    private String applicationName
    @Shared
    private String gitlabUrl

    @Autowired
    private DevopsGitService devopsGitService

    @Autowired
    private UserAttrRepository userAttrRepository

    @Autowired
    private ApplicationTemplateService applicationTemplateService

    SagaClient sagaClient = Mockito.mock(SagaClient.class)
    ApplicationInstanceService applicationInstanceService = Mockito.mock(ApplicationInstanceService.class)
    DevopsServiceService devopsServiceService = Mockito.mock(DevopsServiceService.class)
    DevopsIngressService devopsIngressService = Mockito.mock(DevopsIngressService.class)

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    @Autowired
    @Qualifier("mockGitlabRepository")
    private GitlabRepository gitlabRepository

    @Autowired
    @Qualifier("mockApplicationTemplateRepository")
    private ApplicationTemplateRepository applicationTemplateRepository

//    @Autowired
//    @Qualifier("mockEventProducerTemplate")
//    private EventProducerTemplate eventProducerTemplate

//    @Autowired
//    private EventStoreClient eventStoreClient

    def "Create"() {
        given:
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO()
        applicationTemplateDTO.setCode("test")
        applicationTemplateDTO.setName("test")
        applicationTemplateDTO.setDescription("test")
        applicationTemplateDTO.setOrganizationId(1L)

        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("test")

        GitlabGroupE gitlabGroupE = new GitlabGroupE()

        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setGitlabUserId(1L)
        userAttrRepository.insert(userAttrE)

        devopsGitService.initMockService(sagaClient, applicationInstanceService, devopsServiceService, devopsIngressService)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(null, null)

        ApplicationTemplateE applicationTemplateE = new ApplicationTemplateE()

        when:
        def entity = restTemplate.postForEntity('/v1/organizations/{organization_id}/app_templates', applicationTemplateDTO, ApplicationTemplateRepDTO, 1L)

        then:
        1 * userAttrRepository.queryById(_) >> userAttrE
        1 * iamRepository.queryOrganizationById(_) >> organization
        1 * gitlabRepository.queryGroupByName(_, _) >> null
        1 * gitlabRepository.createGroup(_, _) >> gitlabGroupE
        1 * applicationTemplateRepository.create(_) >> applicationTemplateE
//        entity.statusCode.is2xxSuccessful()
//        entity.body.getCode().equals("test")
//        applicationTemplateRepository.delete(entity.getBody().getId())

//        when:
//        def entity1 = restTemplate.postForEntity('/v1/organizations/{organization_id}/app_templates', applicationTemplateDTO, ApplicationTemplateRepDTO, 1L)
//        then:
//        iamRepository.queryOrganizationById(_) >> organization
//        gitlabRepository.queryGroupByName(_) >> gitlabGroupE
//        gitlabRepository.createGroup(_) >> 1L
//        1 * eventProducerTemplate.execute(_)
//        entity1.statusCode.is2xxSuccessful()
//        entity1.body.getCode().equals("test")
//
//        when:
//        applicationTemplateDTO.setCode(".....")
//        restTemplate.postForEntity('/v1/organizations/{organization_id}/app_templates', applicationTemplateDTO, ApplicationTemplateRepDTO, 1L)
//
//        then:
//        thrown("error.template.code.notMatch")
//
//        when:
//        restTemplate.postForEntity('/v1/organizations/{organization_id}/app_templates', applicationTemplateDTO, ApplicationTemplateRepDTO, 1L)
//
//        then:
//        thrown("error.code.exist")
    }

    def "Update"() {
        given:
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO()
        applicationTemplateDTO.setCode("test")
        applicationTemplateDTO.setName("test")
        applicationTemplateDTO.setDescription("test")
        applicationTemplateDTO.setOrganizationId(1L)

        ApplicationTemplateE applicationTemplateE = new ApplicationTemplateE();
        applicationTemplateE.setName("test")
        applicationTemplateE.setCode("test")
        applicationTemplateE.setDescription("test")

        Organization organization = new Organization();
        organization.setId(1L)
        applicationTemplateE.setOrganization(organization)
        applicationTemplateRepository.create(applicationTemplateE)

        when:
        def entity = restTemplate.put("/v1/organizations/1/app_templates", applicationTemplateDTO, ApplicationTemplateDTO, 1)

        then:
        1 * applicationTemplateRepository.update(_) >> applicationTemplateE
    }

    def "Delete"() {
        given:
//        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO()
//        applicationTemplateDTO.setCode("test")
//        applicationTemplateDTO.setName("test")
//        applicationTemplateDTO.setDescription("test")
//        applicationTemplateDTO.setOrganizationId(1L)

        ApplicationTemplateE applicationTemplateE = new ApplicationTemplateE();
        applicationTemplateE.setName("test")
        applicationTemplateE.setCode("test")
        applicationTemplateE.setDescription("test")
        applicationTemplateE.initGitlabProjectE(1)

        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setGitlabUserId(1L)
        userAttrE.setId(1L)

        when:
        def entity = restTemplate.delete("/v1/organizations/1/app_templates/1")

        then:
        1 * gitlabRepository.deleteProject(_, _) >> null
        1 * applicationTemplateRepository.delete(_) >> null
        1 * applicationTemplateRepository.query(_) >> applicationTemplateE
        1 * userAttrRepository.queryById(_) >> userAttrE
    }

    def "QueryByAppTemplateId"() {
        given:
        Organization organization = new Organization();
        organization.setId(1L)

        ApplicationTemplateE applicationTemplateE = new ApplicationTemplateE();
        applicationTemplateE.setName("test")
        applicationTemplateE.setCode("test")
        applicationTemplateE.setDescription("test")
        applicationTemplateE.initGitlabProjectE(1)
        applicationTemplateE.setRepoUrl("/test/test")
        applicationTemplateE.setOrganization(organization)

        when:
        def entity = restTemplate.getForObject("/v1/organizations/1/app_templates/1", Object.class)

        then:
        1 * applicationTemplateRepository.query(_) >> applicationTemplateE
    }

    def "ListByOptions"() {
        given:
        String infra = "{\"name\":\"testlist\"}";
        PageRequest pageRequest = new PageRequest(0, 20)

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/json;UTF-8"));
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers);

        Organization organization = new Organization();
        organization.setId(1L)

        ApplicationTemplateE applicationTemplateE = new ApplicationTemplateE()
        applicationTemplateE.setId(1L)
        applicationTemplateE.setName("testlist")
        applicationTemplateE.setRepoUrl("/test/test")
        applicationTemplateE.setOrganization(organization)
        ApplicationTemplateE applicationTemplateE1 = new ApplicationTemplateE()
        applicationTemplateE1.setId(2L)
        applicationTemplateE1.setName("testlist")
        applicationTemplateE1.setRepoUrl("/test/test")
        applicationTemplateE1.setOrganization(organization)
        applicationTemplateRepository.create(applicationTemplateE)
        applicationTemplateRepository.create(applicationTemplateE1)

        List<ApplicationTemplateE> applicationTemplateEList = new ArrayList<>()
        applicationTemplateEList.add(applicationTemplateE)
        applicationTemplateEList.add(applicationTemplateE1)

        Page<ApplicationTemplateE> applicationTemplateEPage = new Page<>()
        applicationTemplateEPage.setContent(applicationTemplateEList)
        when:
        def entity = restTemplate.postForObject("/v1/organizations/1/app_templates/list_by_options", strEntity, String.class)
        then:
        1 * applicationTemplateRepository.listByOptions(_, _, _) >> applicationTemplateEPage
        applicationTemplateRepository.create(_) >> applicationTemplateE
        applicationTemplateRepository.create(_) >> applicationTemplateE1
    }

    def "ListByOrgId"() {

        Organization organization = new Organization()
        organization.setId(1L)

        ApplicationTemplateE applicationTemplateE = new ApplicationTemplateE()
        applicationTemplateE.setId(1L)
        applicationTemplateE.setName("test")
        applicationTemplateE.setRepoUrl("/test/test")
        applicationTemplateE.setOrganization(organization)
        ApplicationTemplateE applicationTemplateE1 = new ApplicationTemplateE()
        applicationTemplateE1.setId(2L)
        applicationTemplateE1.setName("test")
        applicationTemplateE1.setRepoUrl("/test/test")
        applicationTemplateE1.setOrganization(organization)

        List<ApplicationTemplateE> applicationTemplateEList = new ArrayList<>()
        applicationTemplateEList.add(applicationTemplateE)
        applicationTemplateEList.add(applicationTemplateE1)

        when:
        def entity = restTemplate.getForObject("/v1/organizations/1/app_templates", Object.class)

        then:
        1 * applicationTemplateRepository.list(_) >> applicationTemplateEList
    }

    def "CheckName"() {
        when:
        def entity = restTemplate.getForObject("/v1/organizations/1/app_templates/checkName?name=test", Object.class)
        then:
        applicationTemplateService.checkName(_, _) >> null
    }

    def "CheckCode"() {
        when:
        def entity = restTemplate.getForObject("/v1/organizations/1/app_templates/checkCode?code=test", Object.class)
        then:
        applicationTemplateService.checkCode(_, _) >> null
    }
}
