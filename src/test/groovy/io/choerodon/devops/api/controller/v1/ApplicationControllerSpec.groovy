package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationDTO
import io.choerodon.devops.api.dto.ApplicationRepDTO
import io.choerodon.devops.api.dto.ApplicationUpdateDTO
import io.choerodon.devops.app.service.ApplicationService
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.repository.UserAttrRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.mapper.*
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
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/3
 * Time: 20:27
 * Description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class ApplicationControllerSpec extends Specification {

    private static flag = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private ApplicationService applicationService
    @Autowired
    private UserAttrRepository userAttrRepository
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private ApplicationMarketMapper applicationMarketMapper
    @Autowired
    private DevopsProjectRepository devopsProjectRepository
    @Autowired
    protected DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private ApplicationTemplateMapper applicationTemplateMapper

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository
    @Autowired
    @Qualifier("mockGitlabGroupMemberRepository")
    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    SagaClient sagaClient = Mockito.mock(SagaClient.class)

//    def setup() {
//        if (flag == 0) {
////            DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
////            devopsEnvironmentDO.setId(1L)
////            devopsEnvironmentDO.setCode("env")
////            devopsEnvironmentDO.setProjectId(1L)
////            devopsEnvironmentDO.setName("testName")
////            devopsEnvironmentDO.setEnvIdRsa("EnvIdRsa")
////            devopsEnvironmentDO.setGitlabEnvProjectId(1L)
////            devopsEnvironmentMapper.insert(devopsEnvironmentDO)
//
////            ApplicationDO applicationDO = new ApplicationDO()
////            ApplicationDO applicationDO1 = new ApplicationDO()
////            applicationDO.setActive(true)
////            applicationDO1.setActive(true)
////            applicationDO.setSynchro(true)
////            applicationDO1.setSynchro(true)
////            applicationDO.setCode("app")
////            applicationDO1.setCode("app1")
////            applicationDO.setProjectId(1L)
////            applicationDO1.setProjectId(1L)
////            applicationDO.setName("appname")
////            applicationDO1.setName("appname1")
////            applicationDO.setGitlabProjectId(1)
////            applicationDO1.setGitlabProjectId(1)
////            applicationDO.setAppTemplateId(1L)
////            applicationDO1.setAppTemplateId(1L)
////            applicationDO.setObjectVersionNumber(1L)
////            applicationDO1.setObjectVersionNumber(1L)
////            applicationMapper.insert(applicationDO)
////            applicationMapper.insert(applicationDO1)
////
////            ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()
////            applicationInstanceDO.setAppId(2L)
////            applicationInstanceDO.setEnvId(1L)
////            applicationInstanceDO.setAppVersionId(1L)
////            applicationInstanceDO.setStatus("deleted")
////            ApplicationInstanceDO applicationInstanceDO1 = new ApplicationInstanceDO()
////            applicationInstanceDO1.setAppId(2L)
////            applicationInstanceDO1.setEnvId(1)
////            applicationInstanceDO1.setAppVersionId(1L)
////            applicationInstanceDO1.setStatus("running")
////            applicationInstanceMapper.insert(applicationInstanceDO)
////            applicationInstanceMapper.insert(applicationInstanceDO1)
//
//
//
////            ApplicationVersionDO applicationVersionDO1 = new ApplicationVersionDO()
////            applicationVersionDO1.setAppId(2L)
////            applicationVersionMapper.insert(applicationVersionDO1)
//
//            flag = 1
//        }
//    }

    def "Create"() {
        given:
        ApplicationDTO applicationDTO = new ApplicationDTO()
        applicationDTO.setName("dtoname")
        applicationDTO.setCode("ddtoapp")
        applicationDTO.setProjectId(1L)
        applicationDTO.setApplictionTemplateId(1L)

        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setId(1L)
        userAttrE.setGitlabUserId(1L)

        Organization organization = new Organization()
        organization.setId(1L)

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setOrganization(organization)

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setProjectE(projectE)
        gitlabGroupE.setGitlabGroupId(1)

        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE()
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue())

        // mock SagaClient
        applicationService.initMockService(sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(null, null)

        when:
        restTemplate.postForObject("/v1/projects/1/apps", applicationDTO, ApplicationRepDTO.class)

        then:
        userAttrRepository.queryById(_ as Long) >> userAttrE
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> groupMemberE
    }

    def "QueryByAppId"() {
        given:
        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        when:
        restTemplate.getForObject("/v1/projects/1/apps/1/detail", ApplicationRepDTO.class)

        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
    }

    def "Update"() {
        given:
        ApplicationUpdateDTO applicationUpdateDTO = new ApplicationUpdateDTO()
        applicationUpdateDTO.setId(1L)
        applicationUpdateDTO.setName("updatename")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)

        when:
        restTemplate.put("/v1/projects/1/apps", applicationUpdateDTO, Boolean.class)

        then:
        true
    }

    def "QueryByAppIdAndActive"() {
        when:
        restTemplate.put("/v1/projects/1/apps/2?active=false", Boolean.class)
        then:
        true
    }

    def "PageByOptions"() {
        given:
        String infra = "{\"searchParam\":{\"code\":[\"app\"]}}"
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/json;UTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        ApplicationVersionReadmeDO applicationVersionReadmeDO = new ApplicationVersionReadmeDO()
        applicationVersionReadmeDO.setReadme("readme")

        when:
        restTemplate.postForObject("/v1/projects/1/apps/list_by_options?active=true&has_version=true", strEntity, String.class)

        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
    }

    def "PageByEnvIdAndStatus"() {
        given:
        PageRequest pageRequest = new PageRequest(1, 20)

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pro")

        when:
        def page = restTemplate.getForObject("/v1/projects/1/apps/pages?env_id=1", PageRequest.class)
        then:
        page != null
    }

    def "ListByEnvIdAndStatus"() {
        when:
        restTemplate.getForObject("/v1/projects/1/apps/options?envId=1&status=running", List.class)

        then:
        true
    }

    def "ListByActive"() {
        when:
        def list = restTemplate.getForObject("/v1/projects/1/apps", List.class)

        then:
        !list.isEmpty()
    }

    def "ListAll"() {
        when:
        def list = restTemplate.getForObject("/v1/projects/1/apps/list_all", List.class)

        then:
        !list.isEmpty()
    }

    def "CheckName"() {
        when:
        restTemplate.getForObject("/v1/projects/1/apps/checkName?name=test", Object.class)

        then:
        true
    }

    def "CheckCode"() {
        when:
        restTemplate.getForObject("/v1/projects/1/apps/checkCode?code=test", Object.class)

        then:
        true
    }

    def "ListTemplate"() {
        given:
        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setOrganization(organization)

        ApplicationTemplateDO applicationTemplateDO = new ApplicationTemplateDO()
        applicationTemplateDO.setId(1L)
        applicationTemplateDO.setName("tempname")
        applicationTemplateDO.setCode("tempcode")
        applicationTemplateDO.setOrganizationId(1L)
        applicationTemplateDO.setDescription("tempdes")
        applicationTemplateDO.setCopyFrom(1L)
        applicationTemplateDO.setRepoUrl("tempurl")
        applicationTemplateDO.setType(null)
        applicationTemplateDO.setUuid("tempuuid")
        applicationTemplateDO.setGitlabProjectId(1L)

        when:
        def list = restTemplate.getForObject("/v1/projects/1/apps/template", List.class)

        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        !list.isEmpty()
    }

    def "ListByActiveAndPubAndVersion"() {
        given:
        String infra = "{}"
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        when:
        restTemplate.postForObject("/v1/projects/1/apps/list_unpublish", strEntity, Object.class)

        then:
        true
    }

    def "ListCodeRepository"() {
        given:
        String infra = "{\"searchParam\":{}}"
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        when:
        restTemplate.postForObject("/v1/projects/1/apps/list_code_repository", strEntity, Object.class)

        then:
        true
    }
}
