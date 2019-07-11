package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.PipelineFrequencyDTO
import io.choerodon.devops.api.vo.PipelineTimeDTO
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.infra.dataobject.ApplicationDTO
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.dataobject.iam.UserDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.ApplicationMapper
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

import static org.mockito.Matchers.any
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
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
    private ApplicationMapper applicationMapper

    @Autowired
    private IamRepository iamRepository

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    @Shared
    ApplicationDTO applicationDO = new ApplicationDTO()
    @Shared
    DevopsGitlabPipelineDO devopsGitlabPipelineDO = new DevopsGitlabPipelineDO()

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
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        UserDO userDO = new UserDO()
        userDO.setLoginName("test")
        userDO.setId(1L)
        List<UserDO> userDOList = new ArrayList<>()
        userDOList.add(userDO)
        ResponseEntity<List<UserDO>> responseEntity3 = new ResponseEntity<>(userDOList, HttpStatus.OK)
        Mockito.when(iamServiceClient.listUsersByIds(any(Long[].class))).thenReturn(responseEntity3)
    }

    def "ListPipelineTime"() {
        given: '初始化数据'
        applicationMapper.insert(applicationDO)
        devopsGitlabPipelineMapper.insert(devopsGitlabPipelineDO);

        when: '获取pipeline时长报表'
        def pipelineTimeDTO = restTemplate.getForObject("/v1/projects/1/pipeline/time?appId=1&startTime=2015/10/12&endTime=3018/10/18", PipelineTimeDTO.class)

        then: '校验返回值'
        pipelineTimeDTO.getRefs().size() != 0
    }

    def "ListPipelineFrequency"() {
        when: '获取pipeline次数报表'
        def pipelineFrequencyDTO = restTemplate.getForObject("/v1/projects/1/pipeline/frequency?appId=1&startTime=2015/10/12&endTime=3018/10/18", PipelineFrequencyDTO.class)

        then: '校验返回值'
        pipelineFrequencyDTO.getPipelineFrequencys().size() != 0
    }

    def "PagePipeline"() {
        when: '分页获取pipeline'
        def pages = restTemplate.getForObject("/v1/projects/384/pipeline/page?appId=1&startTime=2015/10/13&endTime=3018/10/19&page=0&size=10", Page.class)

        then: '校验返回值'
        pages.size() == 1

        and: '清理数据'
        // 删除app
        List<ApplicationDTO> list = applicationMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (ApplicationDTO e : list) {
                applicationMapper.delete(e)
            }
        }
        // 删除gitlabPipeline
        List<DevopsGitlabPipelineDO> list1 = devopsGitlabPipelineMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsGitlabPipelineDO e : list1) {
                devopsGitlabPipelineMapper.delete(e)
            }
        }
    }
}
