package io.choerodon.devops.app.service.impl

import io.choerodon.core.exception.CommonException
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.DevopsCdJobRecordService
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO
import io.choerodon.devops.infra.dto.test.ApiTestTaskRecordDTO
import io.choerodon.devops.infra.enums.JobTypeEnum
import io.choerodon.devops.infra.feign.operator.TestServiceClientOperator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Subject(DevopsCdPipelineServiceImpl)
@Import(IntegrationTestConfiguration)
class DevopsCdPipelineServiceImplSpec extends Specification {

    @Autowired
    DevopsCdPipelineServiceImpl devopsCdPipelineServiceImpl

    DevopsCdJobRecordService devopsCdJobRecordService = Mock()

    TestServiceClientOperator testServiceClientOperator = Mock()

    def setup() {
        ReflectionTestUtils.setField(devopsCdPipelineServiceImpl, "devopsCdJobRecordService", devopsCdJobRecordService)
        ReflectionTestUtils.setField(devopsCdPipelineServiceImpl, "testServiceClientoperator", testServiceClientOperator)
    }

    def "ExecuteApiTestTask"() {
        given:
        def pipelineRecordId = 1L
        def stageRecordId = 1L
        def jobRecordId = 1L

        def cdDeployJob = new DevopsCdJobRecordDTO()
        cdDeployJob.setType(JobTypeEnum.CD_DEPLOY.value())

        def cdApiTestJob = new DevopsCdJobRecordDTO()
        cdApiTestJob.setType(JobTypeEnum.CD_API_TEST.value())
        cdApiTestJob.setMetadata("{\"apiTestTaskId\":\"1\",\"apiTestTaskName\":\"test\",\"blockAfterJob\":\"true\"}")

        def taskRecordDTO = new ApiTestTaskRecordDTO()
        taskRecordDTO.setId(1L)

        when: "任务类型错误"
        devopsCdPipelineServiceImpl.executeApiTestTask(pipelineRecordId, stageRecordId, jobRecordId)
        then:
        1 * devopsCdJobRecordService.queryById(jobRecordId) >> cdDeployJob
        def e = thrown(CommonException)
        e.code == "error.invalid.job.type"

        when: "调用test-service失败"
        devopsCdPipelineServiceImpl.executeApiTestTask(pipelineRecordId, stageRecordId, jobRecordId)
        then:
        1 * devopsCdJobRecordService.queryById(jobRecordId) >> cdApiTestJob
        2 * devopsCdJobRecordService.updateStatusById(_, _)
        1 * testServiceClientOperator.executeTask(_, _, devopsCdJobRecordDTO.getCreatedBy()) >> {throw new CommonException("error.execute.api.test.task")}
        0 * devopsCdJobRecordService.update(_)

        when: "执行成功"
        devopsCdPipelineServiceImpl.executeApiTestTask(pipelineRecordId, stageRecordId, jobRecordId)
        then:
        2 * devopsCdJobRecordService.queryById(jobRecordId) >> cdApiTestJob
        1 * devopsCdJobRecordService.updateStatusById(_, _)
        1 * testServiceClientOperator.executeTask(_, _, devopsCdJobRecordDTO.getCreatedBy()) >> taskRecordDTO
        1 * devopsCdJobRecordService.update(_)
    }
}
