package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.DevopsCdPipelineService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Subject(DevopsCdPipelineController)
@Import(IntegrationTestConfiguration)
class DevopsCdPipelineControllerSpec extends Specification {

    def BASE_URL = "/v1/cd_pipeline"

    @Autowired
    DevopsCdPipelineController devopsCdPipelineController

    @Autowired
    TestRestTemplate testRestTemplate

    DevopsCdPipelineService devopsCdPipelineService = Mock()

    void setup() {
        ReflectionTestUtils.setField(devopsCdPipelineController, "devopsCdPipelineService", devopsCdPipelineService)
    }

    def "ExecuteApiTestTask"() {
        given:
        def pipelineRecordId = 1L
        def stageRecordId = 1L
        def jobRecordId = 1L

        when:
        testRestTemplate.postForEntity(BASE_URL + "/execute_api_test_task?pipeline_record_id={pipelineRecordId}&stage_record_id={stageRecordId}&job_record_id={jobRecordId}", null, Void.class, pipelineRecordId, stageRecordId, jobRecordId)

        then:
        1 * devopsCdPipelineService.executeApiTestTask(_, _, _)
    }
}
