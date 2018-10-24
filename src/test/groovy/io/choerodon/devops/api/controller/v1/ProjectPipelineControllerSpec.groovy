package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository
import io.choerodon.devops.domain.application.repository.UserAttrRepository
import io.choerodon.devops.infra.feign.GitlabServiceClient
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class ProjectPipelineControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private GitlabProjectRepository gitlabProjectRepository

    @Autowired
    private UserAttrRepository userAttrRepository
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)


    def "Retry"() {
        given:

        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(true, HttpStatus.OK)
        gitlabProjectRepository.initMockService(gitlabServiceClient)
        Mockito.doReturn(responseEntity).when(gitlabServiceClient).retry(1, 1, 1)

        when:
        def result = restTemplate.postForObject("/v1/projects/1/gitlab_projects/1/pipelines/1/retry", null, Boolean.class)

        then:
        userAttrRepository.queryById(_ as Long) >> userAttrE
        result == true

    }

    def "Cancel"() {

        given:

        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(true, HttpStatus.OK)
        gitlabProjectRepository.initMockService(gitlabServiceClient)
        Mockito.doReturn(responseEntity).when(gitlabServiceClient).cancel(1, 1, 1)

        when:
        def result = restTemplate.postForObject("/v1/projects/1/gitlab_projects/1/pipelines/1/cancel", null, Boolean.class)

        then:
        userAttrRepository.queryById(_ as Long) >> userAttrE
        Thread.sleep(6000000)
        result == true


    }

}
