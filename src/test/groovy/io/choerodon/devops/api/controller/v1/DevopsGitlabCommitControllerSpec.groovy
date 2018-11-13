package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsGitlabCommitDTO
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.DevopsGitlabCommitDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.dataobject.iam.UserDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper
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
@Subject(DevopsGitlabCommitController)
@Stepwise
class DevopsGitlabCommitControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper

    @Autowired
    private ApplicationMapper applicationMapper

    @Shared
    ApplicationDO applicationDO = new ApplicationDO()
    @Shared
    DevopsGitlabCommitDO devopsGitlabCommitDO = new DevopsGitlabCommitDO()

    @Autowired
    private IamRepository iamRepository

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    def setupSpec() {
        applicationDO.setId(1L)
        applicationDO.setProjectId(1L)
        applicationDO.setActive(true)
        applicationDO.setCode("test")
        applicationDO.setName("test")
        applicationDO.setGitlabProjectId(1)

        devopsGitlabCommitDO.setAppId(1L)
        devopsGitlabCommitDO.setUserId(1L)
        devopsGitlabCommitDO.setCommitSha("test")
        devopsGitlabCommitDO.setCommitContent("test")
        devopsGitlabCommitDO.setRef("test")
        devopsGitlabCommitDO.setCommitDate(new Date())
    }

    def setup() {
        iamRepository.initMockIamService(iamServiceClient)

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

    def "GetCommits"() {
        given: '初始化参数'
        applicationMapper.insert(applicationDO)
        devopsGitlabCommitMapper.insert(devopsGitlabCommitDO)

        when: '获取应用下的代码提交'
        def devopsGitlabCommit = restTemplate.postForObject("/v1/projects/1/apps/commits?start_date=2015/10/12&end_date=3018/10/18", [1], DevopsGitlabCommitDTO.class)

        then: '校验返回值'
        devopsGitlabCommit != null
    }

    def "GetRecordCommits"() {
        when: '获取应用下的代码提交历史记录'
        def pages = restTemplate.postForObject("/v1/projects/1/apps/commits/record?page=0&size=5&start_date=2015/10/12&end_date=3018/10/18", [1], Page.class)

        then: '校验返回值'
        pages.size() == 1

        and: '清理数据'
        // 删除app
        List<ApplicationDO> list = applicationMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (ApplicationDO e : list) {
                applicationMapper.delete(e)
            }
        }
        // 删除gitlabCommit
        List<DevopsGitlabCommitDO> list1 = devopsGitlabCommitMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsGitlabCommitDO e : list1) {
                devopsGitlabCommitMapper.delete(e)
            }
        }
    }
}
