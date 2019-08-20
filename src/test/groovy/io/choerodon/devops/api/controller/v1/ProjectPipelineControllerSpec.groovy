package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ProjectPipelineController)
@Stepwise
class ProjectPipelineControllerSpec extends Specification {
    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private GitlabServiceClientOperator gitlabProjectRepository

    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    def setup() {
        DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)

        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(true, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(gitlabServiceClient).retry(1, 1, 1)
    }

    def "Retry"() {
        when: 'Retry jobs in a pipeline'
        def result = restTemplate.postForObject("/v1/projects/1/gitlab_projects/1/pipelines/1/retry", null, Boolean.class)

        then: '校验返回值'
        result
    }

    def "Cancel"() {
        when: 'Cancel jobs in a pipeline'
        def result = restTemplate.postForObject("/v1/projects/1/gitlab_projects/1/pipelines/1/cancelPipeline", null, Boolean.class)

        then: '校验返回值'
        result
    }
}
