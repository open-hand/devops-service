package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationTemplateDTO
import io.choerodon.devops.api.dto.ApplicationTemplateRepDTO
import io.choerodon.devops.app.service.ApplicationTemplateService
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.event.producer.execute.EventProducerTemplate
import io.choerodon.event.producer.execute.EventRecord
import io.choerodon.event.producer.execute.EventStoreClient
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
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
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    @Autowired
    @Qualifier("mockGitlabRepository")
    private GitlabRepository gitlabRepository




//    @Autowired
//    @Qualifier("mockEventProducerTemplate")
//    private EventProducerTemplate eventProducerTemplate

//    @Autowired
//    private EventStoreClient eventStoreClient




    @Autowired
    private ApplicationTemplateRepository applicationTemplateRepository



    def "Create"() {
        given:
        EventStoreClient eventStoreClient1 = detachedMockFactory.Mock(EventStoreClient.class)
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO()
        applicationTemplateDTO.setCode("test")
        applicationTemplateDTO.setName("test")
        applicationTemplateDTO.setDescription("test")
        applicationTemplateDTO.setOrganizationId(1L)
        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("test")
        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setId(1)
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setGitlabUserId(1L)

        when:
        def entity = restTemplate.postForEntity('/v1/organizations/{organization_id}/app_templates', applicationTemplateDTO, ApplicationTemplateRepDTO, 1L)

        then:
        1 * iamRepository.queryOrganizationById(_) >> organization
        1 * gitlabRepository.queryGroupByName(_,_) >> null
        1 * gitlabRepository.createGroup(_,_) >> gitlabGroupE
        1 * eventProducerTemplate.execute(_,_,_,_)
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
    }

    def "Delete"() {
    }

    def "QueryByAppTemplateId"() {
    }

    def "ListByOptions"() {
    }

    def "ListByOrgId"() {
    }

    def "CheckName"() {
    }

    def "CheckCode"() {
    }
}
