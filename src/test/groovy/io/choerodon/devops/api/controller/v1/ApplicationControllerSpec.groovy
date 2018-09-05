package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationDTO
import io.choerodon.devops.api.dto.ApplicationUpdateDTO
import io.choerodon.devops.api.validator.ApplicationValidator
import io.choerodon.devops.app.service.ApplicationInstanceService
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.app.service.DevopsIngressService
import io.choerodon.devops.app.service.DevopsServiceService
import io.choerodon.devops.domain.application.entity.ApplicationE
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE
import io.choerodon.devops.domain.application.entity.ApplicationVersionE
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabProjectE
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository
import io.choerodon.devops.domain.application.repository.ApplicationRepository
import io.choerodon.devops.domain.application.repository.ApplicationTemplateRepository
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.repository.UserAttrRepository
import io.choerodon.devops.domain.application.valueobject.ApplicationVersionReadmeV
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import javafx.application.Application
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/3
 * Time: 20:27
 * Description: 
 */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(ApplicationValidator.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class ApplicationControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private UserAttrRepository userAttrRepository
    @Autowired
    private DevopsProjectRepository devopsProjectRepository
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository


    @Autowired
    private ApplicationRepository applicationRepository
//    @Autowired
//    @Qualifier("mockApplicationRepository")
//    private ApplicationRepository applicationRepository

    SagaClient sagaClient = Mockito.mock(SagaClient.class)
    ApplicationInstanceService applicationInstanceService = Mockito.mock(ApplicationInstanceService.class)
    DevopsServiceService devopsServiceService = Mockito.mock(DevopsServiceService.class)
    DevopsIngressService devopsIngressService = Mockito.mock(DevopsIngressService.class)

//    @Test
//    def "Create"() {
//        given:
//        Organization organization = new Organization();
//        organization.setId(1L)
//
//        ProjectE projectE = new ProjectE()
//        projectE.setId(1L)
//        projectE.setOrganization(organization)
//
//        ApplicationDTO applicationDTO = new ApplicationDTO()
//        applicationDTO.setId(1L)
//
//        ApplicationE applicationE = new ApplicationE()
//        applicationE.setId(1L)
//
//        UserAttrE userAttreE = new UserAttrE()
//        userAttreE.setId(1L)
//        userAttreE.setGitlabUserId(1L)
//
//        GitlabGroupE gitlabGroupE = new GitlabGroupE()
//        gitlabGroupE.setProjectE(projectE)
//        gitlabGroupE.setGitlabGroupId(1)
//
//        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE()
//        groupMemberE.setId(1)
//        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue())
//
//
//        SagaClient mock = PowerMockito.mock(SagaClient.class)
//        PowerMockito.doReturn(null).when(mock).startSaga(null, null)
//
//        PowerMockito.mockStatic(ApplicationValidator.class)
//        PowerMockito.doNothing().when(ApplicationValidator.class)
//
//        when:
//        // def entity = restTemplate.postForObject("/v1/projects/1/apps", applicationDTO, ApplicationDTO.class)
//
//        ApplicationController applicationController = PowerMockito.mock(ApplicationController.class)
//        PowerMockito.doReturn(ResponseEntity).when(applicationController).create(1L, applicationDTO)
//        then:
//        iamRepository.queryIamProject(_) >> projectE
//        devopsProjectRepository.queryDevopsProject(_) >> gitlabGroupE
//        applicationRepository.create(_) >> applicationE
//    }

//    def "QueryByAppId"() {
//        given:
//        Organization organization = new Organization()
//        organization.setId(1L)
//
//        ProjectE projectE = new ProjectE()
//        projectE.setId(1L)
//        projectE.setOrganization(organization)
//
//        ApplicationE applicationE = new ApplicationE()
//        applicationE.setProjectE(projectE)
//        applicationRepository.create(applicationE)
//
//        when:
//        def entity = restTemplate.getForObject("/v1/projects/1/apps/1/detail", Object.class)
//
//        then:
//        iamRepository.queryIamProject(_) >> projectE
//        iamRepository.queryOrganizationById(_) >> organization
//    }

//    def "Update"() {
//        given:
//        ApplicationUpdateDTO applicationUpdateDTO = new ApplicationUpdateDTO()
//        applicationUpdateDTO.setId(1L)
//        applicationUpdateDTO.setName("test")
//
//        ProjectE projectE = new ProjectE()
//        projectE.setId(1L)
//
//        ApplicationE applicationE = new ApplicationE()
//        applicationE.setProjectE(projectE)
//        applicationRepository.create(applicationE)
//        when:
//        def entity = restTemplate.put("/v1/projects/1/apps", applicationUpdateDTO, ApplicationUpdateDTO.class)
//        then:
//        applicationRepository.update(_) >> 1
//    }

//    def "QueryByAppIdAndActive"() {
//        given:
//        ProjectE projectE = new ProjectE()
//        projectE.setId(1L)
//
//        ApplicationE applicationE = new ApplicationE()
//        applicationE.setProjectE(projectE)
//        applicationE.setActive(true)
//        applicationRepository.create(applicationE)
//
//        ApplicationVersionE applicationVersionE = new ApplicationVersionE()
//        applicationVersionE.setId(1L)
//        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE()
//        devopsEnvironmentE.setId(1L)
//
//        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE()
//        applicationInstanceE.setId(1L)
//        applicationInstanceE.setStatus("deleted")
//        applicationInstanceE.setApplicationE(applicationE)
//        applicationInstanceE.setApplicationVersionE(applicationVersionE)
//        applicationInstanceE.setDevopsEnvironmentE(devopsEnvironmentE)
//        applicationInstanceRepository.create(applicationInstanceE)
//
//        when:
//        def entity = restTemplate.put("/v1/projects/1/apps/1?active=false", Object.class)
//        then:
//        applicationRepository.update(_) >> 1
//    }

//    def "PageByOptions"() {
//        given:
//        String infra = "{\"searchParam\":\"testlist\"}";
//        PageRequest pageRequest = new PageRequest(0, 20)
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.valueOf("application/json;UTF-8"));
//        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers);
//
//        Organization organization = new Organization()
//        organization.setId(1L)
//        organization.setCode("test")
//
//        ProjectE projectE = new ProjectE()
//        projectE.setId(1L)
//        projectE.setCode("test")
//        projectE.setOrganization(organization)
//
//        GitlabProjectE gitlabProjectE = new GitlabProjectE(1)
//
//
//        ApplicationE applicationE = new ApplicationE()
//        ApplicationE applicationE1 = new ApplicationE()
//        applicationE.setProjectE(projectE)
//        applicationE1.setProjectE(projectE)
//        applicationE.setActive(true)
//        applicationE1.setActive(true)
//        applicationE.setCode("test")
//        applicationE1.setCode("test1")
//        applicationE.setGitlabProjectE(gitlabProjectE)
//        applicationE1.setGitlabProjectE(gitlabProjectE)
//
//        ApplicationVersionReadmeV applicationVersionReadmeV = new ApplicationVersionReadmeV()
//        applicationVersionReadmeV.setReadme("testReadme")
//        ApplicationVersionE applicationVersionE = new ApplicationVersionE()
//        applicationVersionE.setVersion("testVersion")
//        applicationVersionE.setApplicationE(applicationE)
//        applicationVersionE.setApplicationVersionReadmeV(applicationVersionReadmeV)
//        applicationVersionRepository.create(applicationVersionE)
//
//        applicationRepository.create(applicationE)
//        applicationRepository.create(applicationE1)
//
//        List<ApplicationE> applicationES = new ArrayList<>()
//        applicationES.add(applicationE)
//        applicationES.add(applicationE1)
//        Page<ApplicationE> applicationPage = new Page<>()
//        applicationPage.setContent(applicationES)
//
//        when:
//        def entity = restTemplate.postForObject("/v1/projects/1/apps/list_by_options?active=true&has_version=true", strEntity, String.class)
//
//        then:
//        iamRepository.queryIamProject(_) >> projectE
//        iamRepository.queryOrganizationById(_) >> organization
//        applicationRepository.listByOptions(_, _, _, _, _,) >> applicationPage
//    }

    def "PageByEnvIdAndStatus"() {
        given:
        String infra = "{\"searchParam\":\"testlist\"}";
        PageRequest pageRequest = new PageRequest(1, 20)

        GitlabProjectE gitlabProjectE = new GitlabProjectE(1)

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("test")

        ApplicationE applicationE = new ApplicationE()
        ApplicationE applicationE1 = new ApplicationE()
        applicationE.setProjectE(projectE)
        applicationE1.setProjectE(projectE)
        applicationE.setGitlabProjectE(gitlabProjectE)
        applicationE1.setGitlabProjectE(gitlabProjectE)

        List<ApplicationE> applicationES = new ArrayList<>()
        applicationES.add(applicationE)
        applicationES.add(applicationE1)
        Page<ApplicationE> applicationPage = new Page<>()
        applicationPage.setContent(applicationES)
        when:
        def entity = restTemplate.getForObject("/v1/projects/1/apps/pages?env_id=1", PageRequest.class)
        then:
        applicationRepository.listByEnvId(_, _, _) >> applicationPage
    }

//    def "ListByEnvIdAndStatus"() {
//    }
//
//    def "ListByActive"() {
//    }
//
//    def "ListAll"() {
//    }
//
//    def "CheckName"() {
//    }
//
//    def "CheckCode"() {
//    }
//
//    def "ListTemplate"() {
//    }
//
//    def "ListByActiveAndPubAndVersion"() {
//    }
//
//    def "ListCodeRepository"() {
//    }
}
