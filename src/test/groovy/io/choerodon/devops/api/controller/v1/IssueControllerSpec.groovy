package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.IssueVO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.*
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsBranchDTO
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO
import io.choerodon.devops.infra.dto.gitlab.CommitDTO
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.mapper.AppServiceMapper
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

import static org.mockito.ArgumentMatchers.*
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
    private AppServiceService applicationRepository
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper
    @Autowired
    private DevopsMergeRequestService devopsMergeRequestRepository
    @Autowired
    private DevopsGitService devopsGitRepository
    @Autowired
    private AppServiceMapper applicationMapper

    @Autowired
    private IamService iamRepository
    @Autowired
    private GitlabServiceClientOperator gitlabRepository
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberRepository

    BaseServiceClient iamServiceClient = Mockito.mock(BaseServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    @Shared
    Date date = new Date(2018, 9, 7, 9, 18, 0)
    @Shared
    Date date1 = new Date(2018, 9, 7, 9, 18, 0)
    @Shared
    AppServiceDTO applicationDO = new AppServiceDTO()
    @Shared
    DevopsBranchDTO devopsBranchDO = new DevopsBranchDTO()
    @Shared
    DevopsBranchDTO devopsBranchDO1 = new DevopsBranchDTO()
    @Shared
    DevopsMergeRequestDTO devopsMergeRequestDO = new DevopsMergeRequestDTO()
    @Shared
    DevopsMergeRequestDTO devopsMergeRequestDO1 = new DevopsMergeRequestDTO()
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
        DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(devopsGitRepository, "gitlabServiceClient", gitlabServiceClient)

        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

        OrganizationDTO organizationDO = new OrganizationDTO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        List<IamUserDTO> addIamUserList = new ArrayList<>()
        IamUserDTO userDO = new IamUserDTO()
        userDO.setId(1L)
        userDO.setLoginName("test")
        userDO.setRealName("realTest")
        userDO.setImageUrl("imageURL")
        addIamUserList.add(userDO)
        ResponseEntity<List<IamUserDTO>> responseEntity2 = new ResponseEntity<>(addIamUserList, HttpStatus.OK)
        Mockito.when(iamServiceClient.listUsersByIds(any(Long[].class))).thenReturn(responseEntity2)

        List<CommitDTO> commitDOS1 = new ArrayList<>()
        CommitDTO commitDO = new CommitDTO()
        commitDO.setId("test")
        commitDOS1.add(commitDO)
        ResponseEntity<List<CommitDTO>> responseEntity3 = new ResponseEntity<>(commitDOS1, HttpStatus.OK)
        Mockito.doReturn(responseEntity3).when(gitlabServiceClient).getCommits(anyInt(), anyString(), anyString())


        MemberDTO memberDO = new MemberDTO()
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
        ResponseEntity<MemberDTO> responseEntity4 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(responseEntity4)

        List<RoleVO> roleDTOList = new ArrayList<>()
        RoleVO roleDTO = new RoleVO()
        roleDTO.setCode("role/project/default/project-owner")
        roleDTOList.add(roleDTO)
        List<ProjectWithRoleVO> projectWithRoleDTOList = new ArrayList<>()
        ProjectWithRoleVO projectWithRoleDTO = new ProjectWithRoleVO()
        projectWithRoleDTO.setName("pro")
        projectWithRoleDTO.setRoles(roleDTOList)
        projectWithRoleDTOList.add(projectWithRoleDTO)
        PageInfo<ProjectWithRoleVO> projectWithRoleDTOPage = new PageInfo<>(projectWithRoleDTOList)
        ResponseEntity<PageInfo<ProjectWithRoleVO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
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
        def issueDTO = restTemplate.getForObject("/v1/project/1/issue/1/commit_and_merge_request/count", IssueVO.class)

        then: '校验返回值'
        issueDTO["branchCount"] == 2
        issueDTO["mergeRequestStatus"] == "opened"

        // 删除app
        List<AppServiceDTO> list = applicationMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (AppServiceDTO e : list) {
                applicationMapper.delete(e)
            }
        }
        // 删除branch
        List<DevopsBranchDTO> list1 = devopsBranchMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsBranchDTO e : list1) {
                devopsBranchMapper.delete(e)
            }
        }
        // 删除mergeRequest
        List<DevopsMergeRequestDTO> list2 = devopsMergeRequestMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (DevopsMergeRequestDTO e : list2) {
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
