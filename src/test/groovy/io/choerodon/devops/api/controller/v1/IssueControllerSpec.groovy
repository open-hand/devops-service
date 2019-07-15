package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.IssueDTO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.vo.iam.RoleDTO
import io.choerodon.devops.app.service.IssueService
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.ApplicationDTO
import io.choerodon.devops.infra.dataobject.DevopsBranchDO
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO
<<<<<<< HEAD
<<<<<<< HEAD
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO
=======
import io.choerodon.devops.infra.dataobject.gitlab.CommitDTO
>>>>>>> [IMP] 修改AppControler重构
=======
import io.choerodon.devops.infra.dataobject.gitlab.CommitDTO
=======
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a
>>>>>>> [IMP]修改后端结构
import io.choerodon.devops.infra.dataobject.gitlab.MemberDTO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.dataobject.iam.UserDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.DevopsBranchMapper
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper
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

import static org.mockito.Matchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/6
 * Time: 20:51
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(IssueController)
@Stepwise
class IssueControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private IssueService issueService
    @Autowired
    private DevopsBranchMapper devopsBranchMapper
    @Autowired
    private ApplicationRepository applicationRepository
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper
    @Autowired
    private DevopsMergeRequestRepository devopsMergeRequestRepository
    @Autowired
    private DevopsGitRepository devopsGitRepository
    @Autowired
    private ApplicationMapper applicationMapper

    @Autowired
    private IamRepository iamRepository
    @Autowired
    private GitlabRepository gitlabRepository
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    @Shared
    Date date = new Date(2018, 9, 7, 9, 18, 0)
    @Shared
    Date date1 = new Date(2018, 9, 7, 9, 18, 0)
    @Shared
    ApplicationDTO applicationDO = new ApplicationDTO()
    @Shared
    DevopsBranchDO devopsBranchDO = new DevopsBranchDO()
    @Shared
    DevopsBranchDO devopsBranchDO1 = new DevopsBranchDO()
    @Shared
    DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO()
    @Shared
    DevopsMergeRequestDO devopsMergeRequestDO1 = new DevopsMergeRequestDO()
    @Shared
    List<CommitDTO> commitDOS = new ArrayList<>()
    @Shared
    CommitDTO commitDO = new CommitDTO()
    @Shared
    CommitDTO commitDO1 = new CommitDTO()

    def setupSpec() {
        applicationDO.setId(1L)
        applicationDO.setName("name")
        applicationDO.setProjectId(1L)
        applicationDO.setGitlabProjectId(1)

        devopsBranchDO.setId(1L)
        devopsBranchDO.setIssueId(1L)
        devopsBranchDO.setDeleted(false)
        devopsBranchDO.setCheckoutDate(date)
        devopsBranchDO.setBranchName("branch")
        devopsBranchDO.setAppId(1L)

        devopsBranchDO1.setId(2L)
        devopsBranchDO1.setIssueId(1L)
        devopsBranchDO1.setDeleted(false)
        devopsBranchDO1.setCheckoutDate(date1)
        devopsBranchDO1.setBranchName("branch1")
        devopsBranchDO1.setAppId(1L)

        devopsMergeRequestDO.setId(1L)
        devopsMergeRequestDO.setProjectId(1L)
        devopsMergeRequestDO.setAuthorId(1L)
        devopsMergeRequestDO.setAssigneeId(1L)
        devopsMergeRequestDO.setSourceBranch("branch")
        devopsMergeRequestDO.setTargetBranch("branch111")
        devopsMergeRequestDO.setUpdatedAt(date)
        devopsMergeRequestDO.setState("opened")

        devopsMergeRequestDO1.setId(2L)
        devopsMergeRequestDO1.setProjectId(1L)
        devopsMergeRequestDO1.setAuthorId(1L)
        devopsMergeRequestDO1.setAssigneeId(1L)
        devopsMergeRequestDO1.setSourceBranch("branch1")
        devopsMergeRequestDO1.setTargetBranch("branch111")
        devopsMergeRequestDO1.setUpdatedAt(date1)
        devopsMergeRequestDO1.setState("opened")

        commitDO.setId("commitNot")
        commitDO.setCreatedAt(date)

        commitDO1.setId("commitNot1")
        commitDO1.setCreatedAt(date1)
        commitDOS.add(commitDO)
        commitDOS.add(commitDO1)
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(devopsGitRepository, "gitlabServiceClient", gitlabServiceClient)

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

        List<UserDO> addIamUserList = new ArrayList<>()
        UserDO userDO = new UserDO()
        userDO.setId(1L)
        userDO.setLoginName("test")
        userDO.setRealName("realTest")
        userDO.setImageUrl("imageURL")
        addIamUserList.add(userDO)
        ResponseEntity<List<UserDO>> responseEntity2 = new ResponseEntity<>(addIamUserList, HttpStatus.OK)
        Mockito.when(iamServiceClient.listUsersByIds(any(Long[].class))).thenReturn(responseEntity2)

        List<CommitDTO> commitDOS1 = new ArrayList<>();
        CommitDTO commitDO = new CommitDTO();
        commitDO.setId("test")
        commitDOS1.add(commitDO)
        ResponseEntity<List<CommitDTO>> responseEntity3 = new ResponseEntity<>(commitDOS1, HttpStatus.OK)
        Mockito.doReturn(responseEntity3).when(gitlabServiceClient).getCommits(anyInt(), anyString(), anyString())


        MemberDTO memberDO = new MemberDTO()
        memberDO.setAccessLevel(AccessLevel.OWNER)
        ResponseEntity<MemberDTO> responseEntity4 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(responseEntity4)

        List<RoleDTO> roleDTOList = new ArrayList<>()
        RoleDTO roleDTO = new RoleDTO()
        roleDTO.setCode("role/project/default/project-owner")
        roleDTOList.add(roleDTO)
        List<ProjectWithRoleDTO> projectWithRoleDTOList = new ArrayList<>()
        ProjectWithRoleDTO projectWithRoleDTO = new ProjectWithRoleDTO()
        projectWithRoleDTO.setName("pro")
        projectWithRoleDTO.setRoles(roleDTOList)
        projectWithRoleDTOList.add(projectWithRoleDTO)
        PageInfo<ProjectWithRoleDTO> projectWithRoleDTOPage = new PageInfo<>(projectWithRoleDTOList)
        ResponseEntity<PageInfo<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        ResponseEntity<List<CommitDTO>> responseEntity5 = new ResponseEntity<>(commitDOS, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getCommits(anyInt(), anyString(), anyString())).thenReturn(responseEntity5)
    }

    def "GetCommitsByIssueId"() {
        given: '初始化数据'
        devopsBranchMapper.insert(devopsBranchDO)
        devopsBranchMapper.insert(devopsBranchDO1)
        applicationMapper.insert(applicationDO)
        devopsMergeRequestMapper.insert(devopsMergeRequestDO)
        devopsMergeRequestMapper.insert(devopsMergeRequestDO1)

        when: '根据issueId获取issue关联的commit列表'
        def list = restTemplate.getForObject("/v1/project/1/issue/1/commit/baseList", List.class)

        then: '校验返回值'
        list.get(0)["branchName"] == "branch"
        list.get(1)["branchName"] == "branch1"
    }

    def "GetMergeRequestsByIssueId"() {
        when: '根据issueId获取issue关联的mergerequest列表'
        def list = restTemplate.getForObject("/v1/project/1/issue/1/merge_request/baseList", List.class)

        then: '校验返回值'
        list.get(0)["sourceBranch"] == "branch"
        list.get(1)["sourceBranch"] == "branch1"
    }

    def "CountCommitAndMergeRequest"() {
        when: '根据issueId获取issue关联的mergerequest和commit数量'
        def issueDTO = restTemplate.getForObject("/v1/project/1/issue/1/commit_and_merge_request/count", IssueDTO.class)

        then: '校验返回值'
        issueDTO["branchCount"] == 2
        issueDTO["mergeRequestStatus"] == "opened"

        // 删除app
        List<ApplicationDTO> list = applicationMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (ApplicationDTO e : list) {
                applicationMapper.delete(e)
            }
        }
        // 删除branch
        List<DevopsBranchDO> list1 = devopsBranchMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsBranchDO e : list1) {
                devopsBranchMapper.delete(e)
            }
        }
        // 删除mergeRequest
        List<DevopsMergeRequestDO> list2 = devopsMergeRequestMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (DevopsMergeRequestDO e : list2) {
                devopsMergeRequestMapper.delete(e)
            }
        }
    }

    /**
     * 清理中间目录
     */
    def cleanupSpec() {
        FileUtil.deleteDirectory(new File("gitops"))
        FileUtil.deleteDirectory(new File("Charts"))
        FileUtil.deleteDirectory(new File("devopsversion"))
    }
}
