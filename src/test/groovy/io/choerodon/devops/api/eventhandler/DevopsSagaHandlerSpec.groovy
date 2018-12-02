package io.choerodon.devops.api.eventhandler

import com.google.gson.Gson
import com.jcraft.jsch.HostKey
import feign.FeignException
import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.RoleAssignmentSearchDTO
import io.choerodon.devops.api.dto.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.dto.iam.RoleDTO
import io.choerodon.devops.api.dto.iam.RoleSearchDTO
import io.choerodon.devops.api.dto.iam.UserDTO
import io.choerodon.devops.domain.application.event.GitlabProjectPayload
import io.choerodon.devops.domain.application.repository.GitlabRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.DeployKey
import io.choerodon.devops.domain.application.valueobject.ProjectHook
import io.choerodon.devops.domain.application.valueobject.RepositoryFile
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO
import io.choerodon.devops.infra.dataobject.gitlab.MemberDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.Matchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 18-12-2
 * Time: 下午9:23
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsSagaHandler)
@Stepwise
class DevopsSagaHandlerSpec extends Specification {

    private final Gson gson = new Gson()

    @Autowired
    private DevopsSagaHandler devopsSagaHandler

    @Autowired
    private IamRepository iamRepository
    @Autowired
    private GitlabRepository gitlabRepository

    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    def setup() {
        iamRepository.initMockIamService(iamServiceClient)
        gitlabRepository.initMockService(gitlabServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)
        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("testOrganization")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        GitlabProjectDO gitlabProjectDO = new GitlabProjectDO()
        gitlabProjectDO.setId(null)
        ResponseEntity<GitlabProjectDO> responseEntity2 = new ResponseEntity<>(gitlabProjectDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getProjectByName(anyInt(), anyString(), anyString())).thenReturn(responseEntity2)

        GitlabProjectDO gitlabProjectDO1 = new GitlabProjectDO()
        gitlabProjectDO1.setId(1)
        ResponseEntity<GitlabProjectDO> responseEntity3 = new ResponseEntity<>(gitlabProjectDO1, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createProject(anyInt(), anyString(), anyInt(), anyBoolean())).thenReturn(responseEntity3)

        List<DeployKey> deployKeyList = new ArrayList<>()
        ResponseEntity<List<DeployKey>> responseEntity4 = new ResponseEntity<>(deployKeyList, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getDeploykeys(anyInt(), anyInt())).thenReturn(responseEntity4)

        ResponseEntity responseEntity5 = new ResponseEntity(HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createDeploykey(anyInt(), anyString(), anyString(), anyBoolean(), anyInt())).thenReturn(responseEntity5)

        ResponseEntity<List<ProjectHook>> responseEntity6 = new ResponseEntity<>(null, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getProjectHook(anyInt(), anyInt())).thenReturn(responseEntity6)

        ProjectHook projectHook = new ProjectHook()
        projectHook.setId(1)
        ResponseEntity<ProjectHook> responseEntity7 = new ResponseEntity<>(projectHook, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createProjectHook(anyInt(), anyInt(), any(ProjectHook.class))).thenReturn(responseEntity7)

        Mockito.when(gitlabServiceClient.getFile(anyInt(), anyString(), anyString())).thenThrow(FeignException)

        RepositoryFile repositoryFile = new RepositoryFile()
        repositoryFile.setFilePath("filePath")
        ResponseEntity<RepositoryFile> responseEntity8 = new ResponseEntity<>(repositoryFile, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createFile(anyInt(), anyString(), anyString(), anyString(), anyInt())).thenReturn(responseEntity8)


        List<RoleDTO> roleDTOList = new ArrayList<>()
        RoleDTO roleDTO = new RoleDTO()
        roleDTO.setCode("role/project/default/project-owner")
        roleDTOList.add(roleDTO)
        List<ProjectWithRoleDTO> projectWithRoleDTOList = new ArrayList<>()
        ProjectWithRoleDTO projectWithRoleDTO = new ProjectWithRoleDTO()
        projectWithRoleDTO.setName("pro")
        projectWithRoleDTO.setRoles(roleDTOList)
        projectWithRoleDTOList.add(projectWithRoleDTO)
        Page<ProjectWithRoleDTO> projectWithRoleDTOPage = new Page<>()
        projectWithRoleDTOPage.setContent(projectWithRoleDTOList)
        projectWithRoleDTOPage.setTotalPages(2)
        ResponseEntity<Page<ProjectWithRoleDTO>> responseEntity9 = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(responseEntity9).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        MemberDO memberDO = new MemberDO()
        memberDO.setAccessLevel(AccessLevel.OWNER)
        ResponseEntity<MemberDO> responseEntity10 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getUserMemberByUserId(anyInt(), anyInt())).thenReturn(responseEntity10)

        Page<RoleDTO> ownerRoleDTOPage = new Page<>()
        Page<RoleDTO> memberRoleDTOPage = new Page<>()
        List<RoleDTO> ownerRoleDTOList = new ArrayList<>()
        List<RoleDTO> memberRoleDTOList = new ArrayList<>()
        RoleDTO ownerRoleDTO = new RoleDTO()
        ownerRoleDTO.setId(45L)
        ownerRoleDTO.setCode("role/project/default/project-owner")
        ownerRoleDTOList.add(ownerRoleDTO)
        ownerRoleDTOPage.setContent(ownerRoleDTOList)
        ownerRoleDTOPage.setTotalElements(1L)
        RoleDTO memberRoleDTO = new RoleDTO()
        memberRoleDTO.setId(43L)
        memberRoleDTO.setCode("role/project/default/project-member")
        memberRoleDTOList.add(memberRoleDTO)
        memberRoleDTOPage.setContent(memberRoleDTOList)
        memberRoleDTOPage.setTotalElements(1L)
        ResponseEntity<Page<RoleDTO>> responseEntity11 = new ResponseEntity<>(ownerRoleDTOPage, HttpStatus.OK)
        RoleSearchDTO ownerRoleSearchDTO = new RoleSearchDTO()
        ownerRoleSearchDTO.setCode("role/project/default/project-owner")
        ResponseEntity<Page<RoleDTO>> responseEntity12 = new ResponseEntity<>(memberRoleDTOPage, HttpStatus.OK)
        RoleSearchDTO memberRoleSearchDTO = new RoleSearchDTO()
        memberRoleSearchDTO.setCode("role/project/default/project-member")
        Mockito.when(iamServiceClient.queryRoleIdByCode(any(RoleSearchDTO.class))).thenReturn(responseEntity11).thenReturn(responseEntity12)

        Page<UserDTO> ownerUserDTOPage = new Page<>()
        List<UserDTO> ownerUserDTOList = new ArrayList<>()
        Page<UserDTO> memberUserDTOPage = new Page<>()
        List<UserDTO> memberUserDTOList = new ArrayList<>()
        UserDTO ownerUserDTO = new UserDTO()
        ownerUserDTO.setId(1L)
        ownerUserDTO.setLoginName("test")
        ownerUserDTO.setRealName("realTest")
        ownerUserDTOList.add(ownerUserDTO)
        ownerUserDTOPage.setContent(ownerUserDTOList)
        UserDTO memberUserDTO = new UserDTO()
        memberUserDTO.setId(4L)
        memberUserDTO.setLoginName("test4")
        memberUserDTO.setRealName("realTest4")
        memberUserDTOList.add(memberUserDTO)
        memberUserDTOPage.setContent(memberUserDTOList)
        ResponseEntity<Page<UserDTO>> ownerPageResponseEntity = new ResponseEntity<>(ownerUserDTOPage, HttpStatus.OK)
        ResponseEntity<Page<UserDTO>> memberPageResponseEntity = new ResponseEntity<>(memberUserDTOPage, HttpStatus.OK)
        RoleAssignmentSearchDTO roleAssignmentSearchDTO = new RoleAssignmentSearchDTO()
        roleAssignmentSearchDTO.setLoginName("")
        roleAssignmentSearchDTO.setRealName("")
        String[] param = new String[1]
        param[0] = ""
        roleAssignmentSearchDTO.setParam(param)
        Mockito.when(iamServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), anyLong(), anyLong(), anyBoolean(), any(RoleAssignmentSearchDTO.class))).thenReturn(ownerPageResponseEntity).thenReturn(memberPageResponseEntity)
    }

    def "DevopsCreateEnv"() {
        given: '初始化GitlabProjectPayload'
        List<Long> userIds = new ArrayList<>()
        userIds.add(1L)
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload()
        gitlabProjectPayload.setGroupId(1)
        gitlabProjectPayload.setUserId(1)
        gitlabProjectPayload.setPath("code")
        gitlabProjectPayload.setOrganizationId(null)
        gitlabProjectPayload.setType("env")
        gitlabProjectPayload.setLoginName("loginName")
        gitlabProjectPayload.setRealName("realName")
        gitlabProjectPayload.setClusterId(1L)
        gitlabProjectPayload.setUserIds(userIds)
        String data = gson.toJson(gitlabProjectPayload)

        and: '初始化环境'
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setCode("code")
        devopsEnvironmentDO.setClusterId(1L)
        devopsEnvironmentDO.setToken("token")
        devopsEnvironmentDO.setEnvIdRsaPub("rsaPub")
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)

        when: '调用方法'
        devopsSagaHandler.devopsCreateEnv(data)

        then: '校验结果a'
        devopsEnvironmentMapper.selectAll().get(0)["envIdRsaPub"] == "rsaPub"
    }

    def "SetEnvErr"() {
    }

    def "Gitops"() {
    }

    def "CreateApp"() {
    }

    def "UpdateGitlabUser"() {
    }

    def "SetAppErr"() {
    }

    def "SetAppTemplateErr"() {
    }

    def "CreateTemplate"() {
    }

    def "GitlabPipeline"() {
    }
}
