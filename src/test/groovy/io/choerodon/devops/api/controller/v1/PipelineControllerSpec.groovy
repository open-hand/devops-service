package io.choerodon.devops.api.controller.v1

import static org.mockito.ArgumentMatchers.eq
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import com.github.pagehelper.PageInfo
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.*
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.*
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.app.service.PipelineService
import io.choerodon.devops.infra.dto.PipelineRecordDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.PipelineRecordMapper

/**
 * @author zhaotianxin* @since 2019/8/29
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(PipelineController)
@Stepwise
class PipelineControllerSpec extends Specification {
    private final static MAPPING = "/v1/projects/{project_id}/pipeline"
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    PipelineService pipelineService
    @Autowired
    PipelineRecordMapper pipelineRecordMapper

    @Qualifier("mockBaseServiceClientOperator")
    @Autowired
    private BaseServiceClientOperator mockBaseServiceClientOperator

    @Shared
    private PipelineReqVO pipelineReqVO = new PipelineReqVO()
    @Shared
    def project_id = 1L

    def setup() {
        List<PipelineStageVO> stageVOList = new ArrayList<>()
        PipelineStageVO pipelineStageVO = new PipelineStageVO()
        pipelineStageVO.setStageName("aa")
        stageVOList.add(pipelineStageVO)

        List<Long> pipelineUserRels = new ArrayList<>()
        pipelineUserRels.add(1L)
        pipelineReqVO.setId(1L)
        pipelineReqVO.setName("流水线")
        pipelineReqVO.setProjectId(project_id)
        pipelineReqVO.setPipelineStageVOs(stageVOList)
        pipelineReqVO.setPipelineUserRels(pipelineUserRels)

        ProjectDTO projectDTO = new ProjectDTO()
        projectDTO.setId(1L)
        projectDTO.setName("aa")
        projectDTO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> projectDO = new ResponseEntity<>(projectDTO, HttpStatus.OK)
        Mockito.doReturn(projectDO).when(mockBaseServiceClientOperator).queryIamProjectById(1L)

        List<ProjectWithRoleVO> list = new ArrayList<>()
        Mockito.when(mockBaseServiceClientOperator.listProjectWithRoleDTO(eq(1))).thenReturn(list)
    }

    def "create"() {
        when:
        def entity = restTemplate.postForEntity(MAPPING, pipelineReqVO, null, project_id)
        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "update"() {
        given:
        pipelineReqVO.setId(1L)
        pipelineReqVO.setName("流水2")
        pipelineReqVO.setObjectVersionNumber(1L)
        HttpEntity<PipelineReqVO> requestEntity = new HttpEntity<PipelineReqVO>(pipelineReqVO)
        when:
        def entity = restTemplate.exchange(MAPPING, HttpMethod.PUT, requestEntity, PipelineReqVO.class, project_id)
        then:
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getName() == "流水2"
    }

    def "updateIsEnabled"() {
        given: '初始化数据'

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8)

        HttpEntity<Integer> requestEntity = new HttpEntity<Integer>(headers)

        when: '启用流水线'
        def entity = restTemplate.exchange(MAPPING + "/{pipeline_id}?isEnabled=1", HttpMethod.PUT, requestEntity, PipelineVO.class, project_id, 1L)

        then:
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null

        when: '停用流水线'
        HttpEntity<String> requestEntity1 = new HttpEntity<String>(headers)
        def entity1 = restTemplate.exchange(MAPPING + "/{pipeline_id}?isEnabled=0", HttpMethod.PUT, requestEntity1, PipelineVO.class, project_id, 1L)

        then:
        entity1.getStatusCode().is2xxSuccessful()
        entity1.getBody() != null
    }

    def "queryById"() {
        when: '查询流水线详情'
        def entity = restTemplate.getForEntity(MAPPING + "/{pipeline_id}", PipelineReqVO.class, project_id, 1L)

        then:
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
    }

    def "pageByOptions"() {
        given:
        PipelineSearchVO pipelineReqVO = new PipelineSearchVO()
        pipelineReqVO.setEnvId(1L)

        when: '查询流水线详情'
        def entity = restTemplate.postForEntity(MAPPING + "/page_by_options?page=1&size=10", pipelineReqVO, PageInfo.class, project_id)

        then:
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
    }

    def "pageRecords"() {
        given:
        String infra = "{\"params\":[],\"searchParam\":{}}"
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        when: '项目下获取流水线记录'
        def entity = restTemplate.postForEntity(MAPPING + "/page_record?page=1&size=10&pipeline_id=1", strEntity, PageInfo.class, project_id)

        then:
        entity.getStatusCode().is2xxSuccessful()
    }

//    def "audit"(){
//        given: '初始化参数'
//        PipelineUserRecordRelationshipVO pipelineUserRecordRelationshipVO = new PipelineUserRecordRelationshipVO()
//        pipelineUserRecordRelationshipVO.setUserId(1L)
//        pipelineUserRecordRelationshipVO.setPipelineRecordId(1L)
//        pipelineUserRecordRelationshipVO.setStageRecordId(1L)
//        pipelineUserRecordRelationshipVO.setTaskRecordId(1L)
//        when: '人工审核'
//        def entity = restTemplate.postForEntity(MAPPING + "/audit",pipelineUserRecordRelationshipVO,List.class,1L)
//        then:
//        entity.getStatusCode().is2xxSuccessful()
//    }

//    def "check_audit"(){
//        given: '初始化参数'
//        PipelineUserRecordRelationshipVO pipelineUserRecordRelationshipVO = new PipelineUserRecordRelationshipVO()
//        pipelineUserRecordRelationshipVO.setUserId(1L)
//        pipelineUserRecordRelationshipVO.setPipelineRecordId(1L)
//        pipelineUserRecordRelationshipVO.setStageRecordId(1L)
//        pipelineUserRecordRelationshipVO.setTaskRecordId(1L)
//        when: '校验人工审核'
//        def entity = restTemplate.postForEntity(MAPPING + "/check_audit",pipelineUserRecordRelationshipVO, CheckAuditVO.class,1L)
//        then:
//        entity.getStatusCode().is2xxSuccessful()
//    }

    def "check_deploy"() {
        when: '条件校验'
        def entity = restTemplate.getForEntity(MAPPING + "/check_deploy?pipeline_id=1", PipelineCheckDeployVO.class, 1L)

        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "getRecordById"() {
        given: "初始化参数"
        PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO()
        pipelineRecordDTO.setProjectId(1L)
        pipelineRecordDTO.setPipelineId(1L)
        pipelineRecordDTO.setStatus("success")
        pipelineRecordDTO.setTriggerType("manual")
        pipelineRecordMapper.insertSelective(pipelineRecordDTO)

        when: '查询流水线记录详情'
        def entity = restTemplate.getForEntity(MAPPING + "/{pipeline_record_id}/record_detail", PipelineRecordReqVO.class, 1L, 1L)

        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "queryByPipelineId"() {
        when: '流水线所有记录'
        def entity = restTemplate.getForEntity(MAPPING + "/{pipeline_id}/list_record", List.class, 1L, 1L)

        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "checkName"() {
        when: '名称校验'
        def entity = restTemplate.getForEntity(MAPPING + "/check_name?name=流水线", null, 1L)

        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "listPipelineDTO"() {
        when: '获取所有流水线'
        def entity = restTemplate.getForEntity(MAPPING + "/list_all", List.class, 1L)

        then:
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().size() > 0
    }

    def "failed"() {
        when: '停止流水线'
        def entity = restTemplate.getForEntity(MAPPING + "/failed?pipeline_record_id=1", null, 1L)

        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "delete"() {
        given: '初始化参数'
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8)
        HttpEntity<Integer> requestEntity = new HttpEntity<Integer>(headers)

        when: '项目下删除流水线'
        def entity = restTemplate.exchange(MAPPING + "/1", HttpMethod.DELETE, requestEntity, Object.class, 1L)

        then:
        entity.getStatusCode().is2xxSuccessful()
    }
}
