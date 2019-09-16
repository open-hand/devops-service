package io.choerodon.devops.app.eventhandler

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.when
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import com.github.pagehelper.PageInfo
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleSearchVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.api.vo.iam.UserVO
import io.choerodon.devops.api.vo.kubernetes.RepositoryFile
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.app.service.GitlabGroupMemberService
import io.choerodon.devops.app.service.GitlabUserService
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.gitlab.*
import io.choerodon.devops.infra.dto.iam.ApplicationDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.mapper.*
import io.choerodon.devops.infra.util.FileUtil
import io.choerodon.devops.infra.util.GitUtil

/**
 * Created by Sheep on 2019/4/9.
 */


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DemoEnvSetupSagaHandler)
@Stepwise
class DemoEnvSetupSagaHandlerSpec extends Specification {


    @Shared
    private boolean isToInit = true
    @Shared
    private int org_id = 1

    @Autowired
    private DemoEnvSetupSagaHandler demoEnvSetupSagaHandler
    @Autowired
    private IamService iamRepository
    @Autowired
    private GitlabServiceClientOperator gitlabRepository
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberRepository
    @Autowired
    private GitlabUserService gitlabUserRepository
    @Autowired
    private DevopsGitService devopsGitRepository
    @Autowired
    @Qualifier("mockGitUtil")
    private GitUtil gitUtil
    @Autowired
    private UserAttrMapper userAttrMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private AppServiceVersionMapper applicationVersionMapper
    @Autowired
    private DevopsBranchMapper devopsBranchMapper
    @Autowired
    private AppServiceShareRuleMapper applicationMarketMapper

    SagaClient sagaClient = Mockito.mock(SagaClient.class)
    BaseServiceClient iamServiceClient = Mockito.mock(BaseServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)
    GitlabServiceClientOperator gitlabServiceClientOperator = Mockito.mock(GitlabServiceClientOperator)

    def setup() {
        if (isToInit) {
            DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", iamServiceClient)
            DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClientOperator", gitlabServiceClientOperator)
            DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(gitlabUserRepository, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(devopsGitRepository, "gitlabServiceClient", gitlabServiceClient)

            ProjectDTO projectDO = new ProjectDTO()
            projectDO.setName("pro")
            projectDO.setOrganizationId(org_id)
            ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
            Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(anyLong())
            OrganizationDTO organizationDO = new OrganizationDTO()
            organizationDO.setId(org_id)
            organizationDO.setCode("testOrganization")
            ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
            Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(anyLong())


            MemberDTO memberDO = new MemberDTO()
            memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
            ResponseEntity<MemberDTO> memberDOResponseEntity = new ResponseEntity<>(memberDO, HttpStatus.OK)
            Mockito.doReturn(memberDOResponseEntity).when(gitlabServiceClient).queryGroupMember(anyInt(), anyInt())

            GroupDTO groupDO = new GroupDTO()
            groupDO.setName("test")
            groupDO.setId(2)
            ResponseEntity<GroupDTO> groupDOResponseEntity = new ResponseEntity<>(groupDO, HttpStatus.OK)
            Mockito.doReturn(groupDOResponseEntity).when(gitlabServiceClient).createGroup(any(GroupDTO), anyInt())
            Mockito.doReturn(null).when(gitlabServiceClient).createTag(anyInt(), anyString(), anyString(), anyString(), anyString(), anyInt())


            RoleVO roleDTO = new RoleVO()
            roleDTO.setId(234L)
            roleDTO.setCode("role/project/default/project-owner")
            PageInfo pageInfo = new PageInfo(0, 10, true)
            List<RoleVO> roleDTOS = Arrays.asList(roleDTO)
            PageInfo<RoleVO> page = new PageInfo(roleDTOS, pageInfo, 1)
            when(iamServiceClient.queryRoleIdByCode(any(RoleSearchVO))).thenReturn(new ResponseEntity<>(page, HttpStatus.OK))
            when(gitlabServiceClient.listDeploykey(anyInt(), anyInt())).thenReturn(new ResponseEntity<>(new ArrayList(), HttpStatus.OK))
            when(gitlabServiceClient.getFile(anyInt(), anyString(), anyString())).thenReturn(new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK))
            when(gitlabServiceClient.queryProjectByName(anyInt(), anyString(), anyString())).thenReturn(new ResponseEntity<>(new GitlabProjectDTO(), HttpStatus.OK))
            when(gitlabServiceClient.listProjectHook(anyInt(), anyInt())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK))

            GitlabProjectDTO gitlabProjectDO = new GitlabProjectDTO()
            gitlabProjectDO.setId(1)
            ResponseEntity<GitlabProjectDTO> gitlabProjectDOResponseEntity = new ResponseEntity<>(gitlabProjectDO, HttpStatus.OK)
            Mockito.doReturn(gitlabProjectDOResponseEntity).when(gitlabServiceClient).queryProjectByName(any(), any(), any())

            BranchDTO branchDO = new BranchDTO()
            CommitDTO commitE = new CommitDTO()
            commitE.setMessage("message")
            commitE.setId("EcommitId")
            commitE.setCommittedDate(new Date(2018, 11, 9, 0, 0, 0))
            commitE.setAuthorName("testAuthorName")
            branchDO.setCommit(commitE)
            branchDO.setProtected(true)


            ResponseEntity<BranchDTO> responseEntity6 = new ResponseEntity<>(branchDO, HttpStatus.OK)
            Mockito.when(gitlabServiceClient.createBranch(anyInt(), anyString(), anyString(), anyInt())).thenReturn(responseEntity6)


            RepositoryFile repositoryFile = new RepositoryFile()
            repositoryFile.setFilePath("test")
            ResponseEntity<RepositoryFile> repositoryFileResponseEntity = new ResponseEntity<>(repositoryFile, HttpStatus.OK)

            Mockito.when(gitlabServiceClient.createFile(anyInt(), anyString(), anyString(), anyString(), anyInt())).thenReturn(repositoryFileResponseEntity)
            Mockito.when(gitlabServiceClient.createFile(anyInt(), anyString(), anyString(), anyString(), anyInt(), any())).thenReturn(repositoryFileResponseEntity)

            Mockito.when(gitlabServiceClient.queryBranch(any(), any())).thenReturn(responseEntity6)

            MergeRequestDTO mergeRequestDO = new MergeRequestDTO()
            mergeRequestDO.setId(1)
            ResponseEntity<MergeRequestDTO> mergeRequestDOResponseEntity = new ResponseEntity<>(MergeRequestDTO, HttpStatus.OK)
            Mockito.doReturn(mergeRequestDOResponseEntity).when(gitlabServiceClient).createMergeRequest(any(), any(), any(), any(), any(), any())
            Mockito.doReturn(null).when(gitlabServiceClient).acceptMergeRequest(any(), any(), any(), any(), any(), any())

            PageInfo<UserVO> ownerUserDTOPage = new PageInfo<>()
            List<UserVO> ownerUserDTOList = new ArrayList<>()
            PageInfo<UserVO> memberUserDTOPage = new PageInfo<>()
            List<UserVO> memberUserDTOList = new ArrayList<>()
            UserVO ownerUserDTO = new UserVO()
            ownerUserDTO.setId(1L)
            ownerUserDTO.setLoginName("test")
            ownerUserDTO.setRealName("realTest")
            ownerUserDTOList.add(ownerUserDTO)
            ownerUserDTOPage.setContent(ownerUserDTOList)
            UserVO memberUserDTO = new UserVO()
            memberUserDTO.setId(4L)
            memberUserDTO.setLoginName("test4")
            memberUserDTO.setRealName("realTest4")
            memberUserDTOList.add(memberUserDTO)
            memberUserDTOPage.setContent(memberUserDTOList)
            ResponseEntity<PageInfo<UserVO>> ownerPageResponseEntity = new ResponseEntity<>(ownerUserDTOPage, HttpStatus.OK)
            RoleAssignmentSearchVO roleAssignmentSearchDTO = new RoleAssignmentSearchVO()
            roleAssignmentSearchDTO.setLoginName("")
            roleAssignmentSearchDTO.setRealName("")
            String[] param = new String[1]
            param[0] = ""
            roleAssignmentSearchDTO.setParam(param)
            Mockito.when(iamServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), anyLong(), anyLong(), anyBoolean(), any(RoleAssignmentSearchVO.class))).thenReturn(ownerPageResponseEntity)

            ImpersonationTokenDTO impersonationTokenDTO = new ImpersonationTokenDTO()
            impersonationTokenDTO.setToken("test")
            impersonationTokenDTO.setId(1)
            impersonationTokenDTO.setName("test")
            ResponseEntity<ImpersonationTokenDTO> impersonationToken = new ResponseEntity<>(impersonationTokenDTO, HttpStatus.OK)
            Mockito.when(gitlabServiceClient.createProjectToken(anyInt())).thenReturn(impersonationToken)


            IamUserDTO userDO = new IamUserDTO()
            userDO.setName("test")
            userDO.setId(1)

            ResponseEntity<IamUserDTO> userDOResponseEntity = new ResponseEntity<>(userDO, HttpStatus.OK)
            Mockito.doReturn(userDOResponseEntity).when(gitlabServiceClient).queryUserById(any())

            List<VariableDTO> variableList = new ArrayList<>()
            VariableDTO variable = new VariableDTO()
            variable.setKey("test")
            variable.setValue("test")
            ResponseEntity<List<VariableDTO>> listResponseEntity = new ResponseEntity<>(variableList, HttpStatus.OK)
            Mockito.doReturn(listResponseEntity).when(gitlabServiceClient).listVariable(any(), any())

            ProjectHookDTO projectHook = new ProjectHookDTO()
            projectHook.setId(100)

            ResponseEntity<ProjectHookDTO> projectHookResponseEntity = new ResponseEntity<>(projectHook, HttpStatus.OK)
            Mockito.doReturn(projectHookResponseEntity).when(gitlabServiceClient).createProjectHook(any(), any(), any())

            List<RoleVO> roleDTOList = new ArrayList<>()
            roleDTOList.add(roleDTO)
            List<ProjectWithRoleVO> projectWithRoleDTOList = new ArrayList<>()
            ProjectWithRoleVO projectWithRoleDTO = new ProjectWithRoleVO()
            projectWithRoleDTO.setName("pro")
            projectWithRoleDTO.setRoles(roleDTOList)
            projectWithRoleDTOList.add(projectWithRoleDTO)
            PageInfo<ProjectWithRoleVO> projectWithRoleDTOPage = new PageInfo<>()
            projectWithRoleDTOPage.setContent(projectWithRoleDTOList)
            projectWithRoleDTOPage.setTotalPages(2)
            ResponseEntity<PageInfo<ProjectWithRoleVO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
            Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())
        }
    }


    def "HandleDemoOrganizationCreateEvent"() {
        given:
        String msg = "{\"organization\":{\"id\":101,\"code\":\"org-xiecwm6vw6\",\"name\":\"新疆克拉玛依市红有软件有限公司\"},\"user\":{\"id\":1,\"loginName\":\"glqwcd22zr\",\"email\":\"soxueren@126.com\"},\"userA\":{\"id\":14902,\"loginName\":\"rty4188vki\",\"email\":\"rty4188vki@demo.com\"},\"userB\":{\"id\":14903,\"loginName\":\"d9e35e81jd\",\"email\":\"d9e35e81jd@demo.com\"},\"project\":null}"

        when:
        def result = demoEnvSetupSagaHandler.handleDemoOrganizationCreateEvent(msg)

        then:
        result != null
    }


    def "InitDemoProject"() {
        given:
        String msg = "{\"organization\":{\"id\":1,\"code\":\"org-xiecwm6vw6\",\"name\":\"新疆克拉玛依市红有软件有限公司\"},\"user\":{\"id\":1,\"loginName\":\"glqwcd22zr\",\"email\":\"soxueren@126.com\"},\"userA\":{\"id\":14902,\"loginName\":\"rty4188vki\",\"email\":\"rty4188vki@demo.com\"},\"userB\":{\"id\":14903,\"loginName\":\"d9e35e81jd\",\"email\":\"d9e35e81jd@demo.com\"},\"project\":{\"id\":1,\"name\":\"test\",\"code\":\"test\"}}"

        when:
        def result = demoEnvSetupSagaHandler.initDemoProject(msg)

        then:

        1 * gitUtil.getWorkingDirectory(_) >> new File("chart")
        1 * gitUtil.push(_, _, _, _, _)
        result != null


        List<UserAttrDTO> userAttrDOList = userAttrMapper.selectAll()
        for (UserAttrDTO userAttrDO : userAttrDOList) {
            if (userAttrDO.getIamUserId() != 1) {
                userAttrMapper.delete(userAttrDO)
            }
        }
        List<ApplicationDTO> applicationDOList = applicationMapper.selectAll()
        for (ApplicationDTO application : applicationDOList) {
            applicationMapper.delete(application)
        }
        List<DevopsProjectDTO> devopsProjectDOList = devopsProjectMapper.selectAll()
        for (DevopsProjectDTO devopsProjectDO : devopsProjectDOList) {
            if (devopsProjectDO.getIamProjectId() != 1) {
                devopsProjectMapper.delete(devopsProjectDO)
            }
        }
        List<AppServiceVersionDTO> applicationVersionDOList = applicationVersionMapper.selectAll()
        for (AppServiceVersionDTO applicationVersionDO : applicationVersionDOList) {
            applicationVersionMapper.delete(applicationVersionDO)
        }
        List<AppServiceShareRuleDTO> devopsAppMarketDOList = applicationMarketMapper.selectAll()
        for (AppServiceShareRuleDTO devopsAppMarketDO : devopsAppMarketDOList) {
            applicationMarketMapper.delete(devopsAppMarketDO)
        }
        List<DevopsBranchDTO> devopsBranchDTOList = devopsBranchMapper.selectAll()
        for (DevopsBranchDTO devopsBranchDTO : devopsBranchDTOList) {
            devopsBranchMapper.delete(devopsBranchDTO)
        }
        FileUtil.deleteFile(new File("template"))
    }
}
