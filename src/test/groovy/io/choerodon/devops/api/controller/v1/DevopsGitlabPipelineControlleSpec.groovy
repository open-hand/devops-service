package io.choerodon.devops.api.controller.v1

import static org.mockito.ArgumentMatchers.any

import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.PipelineFrequencyVO
import io.choerodon.devops.api.vo.PipelineTimeVO
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.AppServiceMapper
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper
import io.choerodon.devops.infra.mapper.DevopsGitlabPipelineMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsGitlabPipelineController)
@Stepwise
class DevopsGitlabPipelineControlleSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper
    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper
    @Autowired
    private AppServiceMapper applicationMapper

    @Qualifier("mockBaseServiceClientOperator")
    @Autowired
    private BaseServiceClientOperator mockBaseServiceClientOperator


    @Shared
    AppServiceDTO applicationDO = new AppServiceDTO()
    @Shared
    DevopsGitlabCommitDTO devopsGitlabCommitDTO = new DevopsGitlabCommitDTO()
    @Shared
    DevopsGitlabPipelineDTO devopsGitlabPipelineDO = new DevopsGitlabPipelineDTO()

    def setupSpec() {
        applicationDO.setId(1L)
        applicationDO.setProjectId(1L)
        applicationDO.setActive(true)
        applicationDO.setCode("test")
        applicationDO.setName("test")
        applicationDO.setGitlabProjectId(1)

        devopsGitlabPipelineDO.setAppServiceId(1L)
        devopsGitlabPipelineDO.setPipelineId(1L)
        devopsGitlabPipelineDO.setPipelineCreateUserId(1L)
        devopsGitlabPipelineDO.setCommitId(1L)
        devopsGitlabPipelineDO.setCommitUserId(1L)
        devopsGitlabPipelineDO.setPipelineCreationDate(new Date())
        devopsGitlabPipelineDO.setStatus("passed")
        devopsGitlabCommitDTO.setId(1L)
        devopsGitlabCommitDTO.setAppServiceId(1L)
        devopsGitlabCommitDTO.setUserId(1L)
    }

    def setup() {
        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        Mockito.doReturn(projectDO).when(mockBaseServiceClientOperator).queryIamProjectById(1L)

        OrganizationDTO organizationDO = new OrganizationDTO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        Mockito.doReturn(organizationDO).when(mockBaseServiceClientOperator).queryOrganizationById(1L)

        IamUserDTO userDO = new IamUserDTO()
        userDO.setLoginName("test")
        userDO.setId(1L)
        List<IamUserDTO> userDOList = new ArrayList<>()
        userDOList.add(userDO)
        Mockito.doReturn(userDOList).when(mockBaseServiceClientOperator).listUsersByIds(any(List))
    }

    def "ListPipelineTime"() {
        given: '初始化数据'
        applicationMapper.insert(applicationDO)
        devopsGitlabPipelineMapper.insert(devopsGitlabPipelineDO)

        when: '获取pipeline时长报表'
        def pipelineTimeDTO = restTemplate.getForEntity("/v1/projects/1/pipeline/time?app_service_id=1&start_time=2015/10/12&end_time=3018/10/18", PipelineTimeVO.class).getBody()

        then: '校验返回值'
        pipelineTimeDTO.getRefs().size() != 0
    }

    def "ListPipelineFrequency"() {


        when: '获取pipeline次数报表'
        def pipelineFrequencyDTO = restTemplate.getForObject("/v1/projects/1/pipeline/frequency?app_service_id=1&start_time=2015/10/12&end_time=3018/10/18", PipelineFrequencyVO.class)

        then: '校验返回值'
        pipelineFrequencyDTO.getPipelineFrequencys().size() != 0
    }

//    def "PagePipeline"() {
//        given: "初始化数据"
//        devopsGitlabCommitMapper.insert(devopsGitlabCommitDTO)
//        when: '分页获取pipeline'
//        def pages = restTemplate.getForObject("/v1/projects/1/pipeline/page_by_options?app_service_id=1&start_time=2015/10/13&end_time=3018/10/19&page=0&size=10", PageInfo.class)
//
//        then: '校验返回值'
//        pages.getTotal() == 1
//
//        and: '清理数据'
//        // 删除app
//        List<AppServiceDTO> list = applicationMapper.selectAll()
//        if (list != null && !list.isEmpty()) {
//            for (AppServiceDTO e : list) {
//                applicationMapper.delete(e)
//            }
//        }
//        // 删除gitlabPipeline
//        List<DevopsGitlabPipelineDTO> list1 = devopsGitlabPipelineMapper.selectAll()
//        if (list1 != null && !list1.isEmpty()) {
//            for (DevopsGitlabPipelineDTO e : list1) {
//                devopsGitlabPipelineMapper.delete(e)
//            }
//        }
//    }
}
