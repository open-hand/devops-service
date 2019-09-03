package io.choerodon.devops.api.controller.v1

import static org.mockito.Matchers.any
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import com.github.pagehelper.PageInfo
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
import io.choerodon.devops.api.vo.DevopsGitlabCommitVO
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.AppServiceMapper
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper

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
    private AppServiceMapper applicationMapper

    @Shared
    AppServiceDTO applicationDO = new AppServiceDTO()
    @Shared
    DevopsGitlabCommitDTO devopsGitlabCommitDO = new DevopsGitlabCommitDTO()

    @Qualifier("mockBaseServiceClientOperator")
    @Autowired
    private BaseServiceClientOperator mockBaseServiceClientOperator

    def setupSpec() {
        applicationDO.setId(1L)
        applicationDO.setAppId(1L)
        applicationDO.setActive(true)
        applicationDO.setCode("test")
        applicationDO.setName("test")
        applicationDO.setGitlabProjectId(1)

        devopsGitlabCommitDO.setAppServiceId(1L)
        devopsGitlabCommitDO.setUserId(1L)
        devopsGitlabCommitDO.setCommitSha("test")
        devopsGitlabCommitDO.setCommitContent("test")
        devopsGitlabCommitDO.setRef("test")
        devopsGitlabCommitDO.setCommitDate(new Date())
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
        Mockito.when(mockBaseServiceClientOperator.listUsersByIds(any(List))).thenReturn(userDOList)
    }

    def "GetCommits"() {
        given: '初始化参数'
        applicationMapper.insert(applicationDO)
        devopsGitlabCommitMapper.insert(devopsGitlabCommitDO)
        and: '构造mock返回值'
        IamUserDTO iamUserDTO = new IamUserDTO()
        iamUserDTO.setId(1L)
        iamUserDTO.setRealName("aa")
        List<IamUserDTO> iamUserDTOList = new ArrayList<>()
        iamUserDTOList.add(iamUserDTO)
        Mockito.doReturn(iamUserDTOList).when(mockBaseServiceClientOperator).listUsersByIds(any(List))
        when: '获取应用下的代码提交'
        def devopsGitlabCommit = restTemplate.postForObject("/v1/projects/1/commits?start_date=2015/10/12&end_date=3018/10/18", [1], DevopsGitlabCommitVO.class)

        then: '校验返回值'
        devopsGitlabCommit != null
        !devopsGitlabCommit.getCommitFormUserVOList().isEmpty()
    }

    def "GetRecordCommits"() {
        when: '获取应用下的代码提交历史记录'
        def pages = restTemplate.postForObject("/v1/projects/1/commits/record?page=0&size=5&start_date=2015/10/12&end_date=3018/10/18", [1], PageInfo.class)

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
        // 删除gitlabCommit
        List<DevopsGitlabCommitDTO> list1 = devopsGitlabCommitMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsGitlabCommitDTO e : list1) {
                devopsGitlabCommitMapper.delete(e)
            }
        }
    }
}
