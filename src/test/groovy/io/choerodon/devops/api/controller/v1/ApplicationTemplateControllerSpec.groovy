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
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE
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
import spock.lang.Specification
import spock.lang.Stepwise

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

    def "Create"() {
        given:
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO()
        applicationTemplateDTO.setCode("code")
        applicationTemplateDTO.setName("app")
        applicationTemplateDTO.setDescription("des")
        applicationTemplateDTO.setOrganizationId(1L)

        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setCode("orgDO")

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setName("org_template")
        gitlabGroupE.setPath("org_template")
        gitlabGroupE.setVisibility(Visibility.PUBLIC)

        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)

        // mock SagaClient
        applicationTemplateService.initMockService(sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(null, null)

        // mock FeignClient
        applicationTemplateRepository.initMockService(iamServiceClient)
        Mockito.doReturn(new ResponseEntity<OrganizationDO>(organizationDO, HttpStatus.OK)).when(iamServiceClient).queryOrganizationById(1)

        ApplicationTemplateE applicationTemplateE = new ApplicationTemplateE()
        applicationTemplateE.setOrganization(organization)

        when:
        def dto = restTemplate.postForEntity("/v1/organizations/1/app_templates", applicationTemplateDTO, ApplicationTemplateRepDTO.class)

        then:
        userAttrRepository.queryById(_ as Long) >> userAttrE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        gitlabRepository.queryGroupByName(_ as String, _ as Integer) >> null
        gitlabRepository.createGroup(_ as GitlabGroupE, _ as Integer) >> gitlabGroupE
        dto != null
    }

    def "Update"() {
        given:
        ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO = new ApplicationTemplateUpdateDTO()
        applicationTemplateUpdateDTO.setId(1L)

        ApplicationTemplateDO applicationTemplateDO = new ApplicationTemplateDO()
        applicationTemplateDO.setObjectVersionNumber(1L)
        applicationTemplateMapper.insert(applicationTemplateDO)

        when:
        restTemplate.put("/v1/organizations/1/app_templates", applicationTemplateUpdateDTO, ApplicationTemplateRepDTO.class)

        then:
        true
    }

    def "Delete"() {
        given:
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setGitlabUserId(1L)
        userAttrE.setId(1L)

        when:
        restTemplate.delete("/v1/organizations/1/app_templates/1")

        then:
        userAttrRepository.queryById(_ as Long) >> userAttrE
        gitlabRepository.deleteProject(_ as Integer, _ as Integer) >> null
    }

    def "QueryByAppTemplateId"() {
        given:
        ApplicationTemplateDO applicationTemplateDO = new ApplicationTemplateDO()
        applicationTemplateDO.setOrganizationId(1L)
        applicationTemplateDO.setRepoUrl("/test/repoUrl/")
        applicationTemplateMapper.insert(applicationTemplateDO)

        when:
        def dto = restTemplate.getForObject("/v1/organizations/1/app_templates/3", ApplicationTemplateRepDTO.class)

        then:
        dto != null
    }

    def "ListByOptions"() {
        given:
        String infra = null
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        when:
        def page = restTemplate.postForObject("/v1/organizations/1/app_templates/list_by_options", strEntity, Page.class)

        then:
        !page.isEmpty()
    }

    def "ListByOrgId"() {
        when:
        def list = restTemplate.getForObject("/v1/organizations/1/app_templates", List.class)

        then:
        !list.isEmpty()
    }

    def "CheckName"() {
        when:
        restTemplate.getForObject("/v1/organizations/1/app_templates/checkName?name=test", Object.class)

        then:
        true
    }

    def "CheckCode"() {
        when:
        restTemplate.getForObject("/v1/organizations/1/app_templates/checkCode?code=test", Object.class)

        then:
        true
    }
}
