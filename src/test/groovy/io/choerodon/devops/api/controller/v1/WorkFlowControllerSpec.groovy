package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.PipelineService
import io.choerodon.devops.infra.dto.AppServiceVersionDTO
import io.choerodon.devops.infra.dto.PipelineRecordDTO
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO
import io.choerodon.devops.infra.dto.PipelineTaskRecordDTO
import io.choerodon.devops.infra.feign.WorkFlowServiceClient
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator
import io.choerodon.devops.infra.mapper.AppServiceVersionMapper
import io.choerodon.devops.infra.mapper.PipelineRecordMapper
import io.choerodon.devops.infra.mapper.PipelineStageRecordMapper
import io.choerodon.devops.infra.mapper.PipelineTaskRecordMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author zhaotianxin* @since 2019/8/30
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(WorkFlowController)
@Stepwise
class WorkFlowControllerSpec extends Specification {
    private static final  String MAPPING =  "/workflow"
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    PipelineTaskRecordMapper pipelineTaskRecordMapper
    @Autowired
    PipelineStageRecordMapper pipelineStageRecordMapper
    @Autowired
    AppServiceVersionMapper appServiceVersionMapper
    @Autowired
    PipelineService pipelineService
    @Autowired
    PipelineRecordMapper pipelineRecordMapper
    @Shared
    private PipelineTaskRecordDTO pipelineTaskRecordDTO = new PipelineTaskRecordDTO()
    @Shared
    private PipelineStageRecordDTO pipelineStageRecordDTO = new PipelineStageRecordDTO()
    @Shared
    private AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO()
    @Shared
    private PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO()

    WorkFlowServiceOperator workFlowServiceOperator = Mockito.mock(WorkFlowServiceOperator)
    def setup(){
        DependencyInjectUtil.setAttribute(pipelineService,"workFlowServiceOperator",workFlowServiceOperator)
        pipelineTaskRecordDTO.setProjectId(1L)
        pipelineTaskRecordDTO.setCreatedBy(1L)
        pipelineTaskRecordDTO.setName("aaa")
        pipelineTaskRecordDTO.setEnvId(1L)
        pipelineTaskRecordDTO.setInstanceId(1L)
        pipelineTaskRecordDTO.setInstanceName("asda")
        pipelineTaskRecordDTO.setValueId(1L)

        pipelineStageRecordDTO.setPipelineRecordId(1L)
        pipelineStageRecordDTO.setCreatedBy(1L)
        pipelineStageRecordDTO.setStageId(1L)
        pipelineStageRecordDTO.setIsParallel(1)

        appServiceVersionDTO.setAppServiceId(1L)
        appServiceVersionDTO.setVersion("sdadsadaf")

        pipelineRecordDTO.setProjectId(1L)
        pipelineRecordDTO.setProjectId(1L)
        pipelineRecordDTO.setPipelineId(1L)

    }
    def "AutoDeploy"() {
        given: '初始化数据'
        pipelineRecordMapper.insertSelective(pipelineRecordDTO)
        pipelineTaskRecordMapper.insertSelective(pipelineTaskRecordDTO)
        pipelineStageRecordMapper.insertSelective(pipelineStageRecordDTO)
        appServiceVersionMapper.insertSelective(appServiceVersionDTO)
        when: '触发自动部署'
        def entity = restTemplate.getForEntity(MAPPING +"/auto_deploy?stage_record_id=1&task_record_id=1",null)
        then:
        entity.getStatusCode().is2xxSuccessful()
    }
    def "SetAppDeployStatusTask"() {
        given: '初始化参数'

        Map<String,Long> map = new HashMap<>()
        map.put("pipeline_record_id",1L)
        map.put("stage_record_id",1L)
        map.put("task_record_id",1L)
        HttpEntity<Map> httpEntity = new HttpEntity<>()
        when: '接收任务状态'
        def entity = restTemplate.exchange(
                MAPPING + "/auto_deploy/status?status=false&pipeline_record_id=1&stage_record_id=1&task_record_id=1"
                , HttpMethod.PUT,httpEntity,Object.class)
        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "GetAppDeployStatusTask"() {
        when: "检测部署任务生成实例状态"
        def entity = restTemplate.getForEntity(MAPPING + "/auto_deploy/status?stage_record_id=1&task_record_id=1",String.class)
        then:
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
    }
}
