package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.PipelineFrequencyVO
import io.choerodon.devops.api.vo.PipelineTimeVO
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.mapper.AppServiceMapper
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper
import io.choerodon.devops.infra.mapper.DevopsGitlabPipelineMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

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

    @Autowired
    private IamService iamRepository

    BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient)

    @Shared
    AppServiceDTO applicationDO = new AppServiceDTO()
    @Shared
    DevopsGitlabPipelineDTO devopsGitlabPipelineDO = new DevopsGitlabPipelineDTO()

    def setupSpec() {
        applicationDO.setId(1L)
        applicationDO.setProjectId(1L)
        applicationDO.setActive(true)
        applicationDO.setCode("test")
        applicationDO.setName("test")
        applicationDO.setGitlabProjectId(1)

        devopsGitlabPipelineDO.setAppId(1L)
        devopsGitlabPipelineDO.setPipelineId(1L)
        devopsGitlabPipelineDO.setPipelineCreateUserId(1L)
        devopsGitlabPipelineDO.setCommitId(1L)
        devopsGitlabPipelineDO.setPipelineCreationDate(new Date())
        devopsGitlabPipelineDO.setStatus("passed")
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", baseServiceClient)

        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(baseServiceClient).queryIamProject(1L)

        OrganizationDTO organizationDO = new OrganizationDTO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(1L)

        IamUserDTO userDO = new IamUserDTO()
        userDO.setLoginName("test")
        userDO.setId(1L)
        List<IamUserDTO> userDOList = new ArrayList<>()
        userDOList.add(userDO)
        ResponseEntity<List<IamUserDTO>> responseEntity3 = new ResponseEntity<>(userDOList, HttpStatus.OK)
        Mockito.when(baseServiceClient.listUsersByIds()).thenReturn(responseEntity3)
    }

    def "ListPipelineTime"() {
        given: '初始化数据'
        applicationMapper.insert(applicationDO)
        devopsGitlabPipelineMapper.insert(devopsGitlabPipelineDO)

        when: '获取pipeline时长报表'
        def pipelineTimeDTO = restTemplate.getForObject("/v1/projects/1/pipeline/time?appId=1&startTime=2015/10/12&endTime=3018/10/18", PipelineTimeVO.class)

        then: '校验返回值'
        pipelineTimeDTO.getRefs().size() != 0
    }

    def "ListPipelineFrequency"() {
        when: '获取pipeline次数报表'
        def pipelineFrequencyDTO = restTemplate.getForObject("/v1/projects/1/pipeline/frequency?appId=1&startTime=2015/10/12&endTime=3018/10/18", PipelineFrequencyVO.class)

        then: '校验返回值'
        pipelineFrequencyDTO.getPipelineFrequencys().size() != 0
    }

    def "PagePipeline"() {
        when: '分页获取pipeline'
        def pages = restTemplate.getForObject("/v1/projects/384/pipeline/page?appId=1&startTime=2015/10/13&endTime=3018/10/19&page=0&size=10", PageInfo.class)

        then: '校验返回值'
        pages.getTotal() == 1

        and: '清理数据'
        // 删除app
        List<AppServiceDTO> list = applicationMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (AppServiceDTO e : list) {
                applicationMapper.delete(e)
            }
        }
        // 删除gitlabPipeline
        List<DevopsGitlabPipelineDTO> list1 = devopsGitlabPipelineMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsGitlabPipelineDTO e : list1) {
                devopsGitlabPipelineMapper.delete(e)
            }
        }
    }
}
