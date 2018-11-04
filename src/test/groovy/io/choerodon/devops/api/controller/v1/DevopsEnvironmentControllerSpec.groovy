package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsEnviromentDTO
import io.choerodon.devops.api.dto.DevopsEnvironmentUpdateDTO
import io.choerodon.devops.api.dto.EnvSyncStatusDTO
import io.choerodon.devops.api.dto.RoleAssignmentSearchDTO
import io.choerodon.devops.api.dto.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.dto.iam.RoleDTO
import io.choerodon.devops.api.dto.iam.UserDTO
import io.choerodon.devops.app.service.DevopsEnvironmentService
import io.choerodon.devops.domain.application.entity.DevopsServiceE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.dataobject.iam.UserDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.*
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import io.choerodon.websocket.helper.EnvListener
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.*
import spock.lang.Specification
import spock.lang.Stepwise

import static org.mockito.Matchers.anyInt
import static org.mockito.Matchers.anyLong
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/4
 * Time: 15:49
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsEnvironmentControllerSpec extends Specification {

    private static flag = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsServiceRepository devopsServiceRepository
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService
    @Autowired
    private DevopsEnvCommitMapper devopsEnvCommitMapper
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil

    @Autowired
    @Qualifier("mockGitlabRepository")
    private GitlabRepository gitlabRepository
    @Autowired
    @Qualifier("mockGitlabProjectRepository")
    private GitlabProjectRepository gitlabProjectRepository

    @Autowired
    private UserAttrRepository userAttrRepository
    @Autowired
    private IamRepository iamRepository

    SagaClient sagaClient = Mockito.mock(SagaClient.class)
    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    def setup() {
        if (flag == 0) {
            Organization organization = initOrg(1L, "testOrganization")
            ProjectE projectE = initProj(1L, "testProject", organization)

            DevopsEnvironmentDO devopsEnvironmentDO = initEnv(
                    "testNameEnv", "testCodeEnv", projectE, true, 1L, "testToken")
            devopsEnvironmentDO.setGitlabEnvProjectId(1L)
            DevopsEnvironmentDO devopsEnvironmentDO1 = initEnv(
                    "testNameEnv1", "testCodeEnv1", projectE, true, 2L, "testToken1")
            devopsEnvironmentDO1.setGitlabEnvProjectId(2L)
            devopsEnvironmentDO.setId(1L)
            devopsEnvironmentDO1.setId(2L)
            devopsEnvironmentMapper.insert(devopsEnvironmentDO)
            devopsEnvironmentMapper.insert(devopsEnvironmentDO1)

            DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDO()
            devopsEnvUserPermissionDO.setIamUserId(1L)
            devopsEnvUserPermissionDO.setEnvId(1L)
            devopsEnvUserPermissionDO.setLoginName("test")
            devopsEnvUserPermissionDO.setRealName("realTest")
            devopsEnvUserPermissionDO.setPermitted(true)
            DevopsEnvUserPermissionDO devopsEnvUserPermissionDO1 = new DevopsEnvUserPermissionDO()
            devopsEnvUserPermissionDO1.setIamUserId(2L)
            devopsEnvUserPermissionDO1.setEnvId(1L)
            devopsEnvUserPermissionDO1.setLoginName("test1")
            devopsEnvUserPermissionDO1.setRealName("realTest1")
            devopsEnvUserPermissionDO1.setPermitted(true)
            DevopsEnvUserPermissionDO devopsEnvUserPermissionDO2 = new DevopsEnvUserPermissionDO()
            devopsEnvUserPermissionDO2.setIamUserId(3L)
            devopsEnvUserPermissionDO2.setEnvId(1L)
            devopsEnvUserPermissionDO2.setLoginName("test2")
            devopsEnvUserPermissionDO2.setRealName("realTest2")
            devopsEnvUserPermissionDO2.setPermitted(true)
            devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO)
            devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO1)
            devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO2)
            flag = 1
        }
    }

    def "Create"() {
        given:
        DevopsEnviromentDTO devopsEnviromentDTO = new DevopsEnviromentDTO()
        devopsEnviromentDTO.setCode("testCodeChange")
        devopsEnviromentDTO.setName("testNameChange")

        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)

        devopsEnvironmentService.initMockService(sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(null, null)

        iamRepository.initMockIamService(iamServiceClient)
        ProjectDO projectDO = new ProjectDO(1L)
        projectDO.setCode("testProject")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("testOrganization")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        UserDO userDO = new UserDO()
        userDO.setLoginName("loginName")
        userDO.setRealName("realName")
        ResponseEntity<UserDO> responseEntity2 = new ResponseEntity<>(userDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity2).when(iamServiceClient).queryById(1L)

        userAttrRepository.queryById(_ as Long) >> userAttrE
        when:
        restTemplate.postForObject("/v1/projects/1/envs", devopsEnviromentDTO, String.class)

        then:
        devopsEnvironmentRepository.queryByProjectIdAndCode(1L, "testCodeChange") != null
    }

    def "ListByProjectIdDeployed"() {
        given:
        DevopsServiceE devopsServiceE = new DevopsServiceE()
        devopsServiceE.setId(1L)
        devopsServiceE.setEnvId(1L)
        devopsServiceE.setStatus("running")
        DevopsServiceE devopsServiceE1 = new DevopsServiceE()
        devopsServiceE.setId(2L)
        devopsServiceE1.setEnvId(2L)
        devopsServiceE1.setStatus("running")

        devopsServiceRepository.insert(devopsServiceE)
        devopsServiceRepository.insert(devopsServiceE1)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        iamRepository.initMockIamService(iamServiceClient)
        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("pro")
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

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

        ResponseEntity<Page<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        when:
        def envs = restTemplate.getForObject("/v1/projects/1/envs/deployed", List.class)

        then:
        envs.size() == 2
        devopsServiceRepository.delete(1L)
        devopsServiceRepository.delete(2L)
    }

    def "ListByProjectIdAndActive"() {
        given:
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        iamRepository.initMockIamService(iamServiceClient)
        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("pro")
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

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

        ResponseEntity<Page<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        when:
        def envs = restTemplate.getForObject("/v1/projects/1/envs?active=true", List.class)

        then:
        envs.size() == 3
    }

    def "ListByProjectIdAndActiveWithGroup"() {
        given:
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        iamRepository.initMockIamService(iamServiceClient)
        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("pro")
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

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

        ResponseEntity<Page<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        DevopsEnvGroupDO devopsEnvGroupDO = new DevopsEnvGroupDO()
        devopsEnvGroupDO.setId(1L)
        devopsEnvGroupDO.setProjectId(1L)
        DevopsEnvGroupDO devopsEnvGroupDO1 = new DevopsEnvGroupDO()
        devopsEnvGroupDO1.setId(2L)
        devopsEnvGroupDO1.setProjectId(1L)
        devopsEnvGroupMapper.insert(devopsEnvGroupDO)
        devopsEnvGroupMapper.insert(devopsEnvGroupDO1)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        def list = restTemplate.getForObject("/v1/projects/1/envs/groups?active=true", List.class)

        then:
        !list.isEmpty()
    }

    def "QueryShell"() {
        when:
        String shell = restTemplate.getForObject("/v1/projects/1/envs/1/shell", String.class)
        then:
        !shell.isEmpty()
    }

    def "EnableOrDisableEnv"() {
        given:
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        restTemplate.put("/v1/projects/1/envs/3/active?active=false", Boolean.class)

        then:
        !devopsEnvironmentMapper.selectByPrimaryKey(3L).getActive()
    }

    def "Query"() {
        when:
        DevopsEnvironmentUpdateDTO a = restTemplate.getForObject("/v1/projects/1/envs/1", DevopsEnvironmentUpdateDTO.class)

        then:
        a != null
    }

    def "Update"() {
        given:
        DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO = new DevopsEnvironmentUpdateDTO()
        devopsEnvironmentUpdateDTO.setId(3L)
        devopsEnvironmentUpdateDTO.setName("testNameChange1222")

        when:
        restTemplate.put("/v1/projects/1/envs", devopsEnvironmentUpdateDTO, DevopsEnvironmentUpdateDTO.class)

        then:
        devopsEnvironmentMapper.selectByPrimaryKey(3L).getName() == "testNameChange1222"
    }

    def "Sort"() {
        given:
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        Long[] sequence = [2L, 1L]
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        restTemplate.put("/v1/projects/1/envs/sort", sequence, List.class)

        then:
        devopsEnvironmentMapper.selectByPrimaryKey(1L).getSequence() == 2L
    }

    def "CheckName"() {
        when:
        restTemplate.getForObject("/v1/projects/1/envs/checkName?name=testCheckName", Object.class)

        then:
        notThrown(CommonException)
    }

    def "CheckCode"() {
        when:
        restTemplate.getForObject("/v1/projects/1/envs/checkCode?code=testCheckCode", Object.class)

        then:
        notThrown(CommonException)
    }

    def "ListByProjectId"() {
        given:
        List<Long> envList = new ArrayList<>()
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()
        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setAppId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setEnvCode("env")
        applicationInstanceDO.setCode("instance")
        applicationInstanceDO.setAppVersionId(1L)
        applicationInstanceDO.setStatus("running")
        applicationInstanceDO.setEnvName("env")
        applicationInstanceDO.setAppName("appname")
        applicationInstanceDO.setCommandId(1L)
        applicationInstanceDO.setObjectVersionNumber(1L)
        ApplicationInstanceDO applicationInstanceDO1 = new ApplicationInstanceDO()
        applicationInstanceDO1.setId(2L)
        applicationInstanceDO1.setAppId(2L)
        applicationInstanceDO1.setEnvId(2L)
        applicationInstanceDO1.setEnvCode("env1")
        applicationInstanceDO1.setCode("instance1")
        applicationInstanceDO1.setAppVersionId(1L)
        applicationInstanceDO1.setStatus("running")
        applicationInstanceDO1.setEnvName("env")
        applicationInstanceDO1.setAppName("appname1")
        applicationInstanceDO1.setCommandId(1L)
        applicationInstanceDO1.setObjectVersionNumber(1L)
        applicationInstanceMapper.insert(applicationInstanceDO)
        applicationInstanceMapper.insert(applicationInstanceDO1)
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        iamRepository.initMockIamService(iamServiceClient)
        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("pro")
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

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

        ResponseEntity<Page<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        when:
        def envs = restTemplate.getForObject("/v1/projects/1/envs/instance", List.class)

        then:
        envs.size() == 2
    }

    def "QueryEnvSyncStatus"() {
        given:
        iamRepository.initMockIamService(iamServiceClient)
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

        DevopsEnvCommitDO devopsEnvCommitDO = new DevopsEnvCommitDO()
        devopsEnvCommitDO.setId(1L)
        devopsEnvCommitDO.setCommitSha("testCommitSha")
        devopsEnvCommitMapper.insert(devopsEnvCommitDO)
        devopsEnvironmentMapper.updateDevopsEnvCommit(1L, 1L, 1L, 1L)

        when:
        def envSyncStatusDTO = restTemplate.getForObject("/v1/projects/1/envs/1/status", EnvSyncStatusDTO.class)

        then:
        envSyncStatusDTO.getAgentSyncCommit().equals("testCommitSha")
    }

    def "ListUserPermissionByEnvId"() {
        given:
        String params = "{\"searchParam\": {\"loginName\": [],\"realName\": []},\"param\": \"\"}"
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(params, headers)

        iamRepository.initMockIamService(iamServiceClient)
        List<RoleDTO> roleDTOList = new ArrayList<>()
        RoleDTO roleDTO = new RoleDTO()
        roleDTO.setId(1L)
        roleDTO.setCode("role/project/default/project-member")
        roleDTOList.add(roleDTO)
        ResponseEntity<List<RoleDTO>> responseEntity = new ResponseEntity<>(roleDTOList, HttpStatus.OK)
        RoleAssignmentSearchDTO roleAssignmentSearchDTO = new RoleAssignmentSearchDTO();
        Mockito.doReturn(responseEntity).when(iamServiceClient).listRolesWithUserCountOnProjectLevel(1L, roleAssignmentSearchDTO)

        Page<UserDTO> userDTOPage = new Page<>()
        List<UserDTO> userDTOList = new ArrayList<>()
        UserDTO userDTO = new UserDTO()
        userDTO.setId(1L)
        userDTO.setLoginName("test")
        userDTO.setRealName("realTest")
        userDTOList.add(userDTO)
        userDTOPage.setContent(userDTOList)
        ResponseEntity<Page<UserDTO>> pageResponseEntity = new ResponseEntity<>(userDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).pagingQueryUsersByRoleIdOnProjectLevel(0, 5, 43, 1, roleAssignmentSearchDTO)
        iamRepository.pagingQueryUsersByRoleIdOnProjectLevel(_ as PageRequest, _ as RoleAssignmentSearchDTO, _ as Long, _ as Long) >> userDTOPage

        when:
        def page = restTemplate.postForObject("/v1/projects/1/envs/list?page=0&size5&env_id=null", strEntity, Page.class)

        then:
        page.get(0)["loginName"] == "test"
    }
//
//    def "ListAllUserPermission"() {
//        when:
//        def list = restTemplate.getForObject("/v1/projects/1/envs/1/list_all", List.class)
//
//        then:
//        !list.isEmpty()
//        list.get(0)["loginName"] == "test"
//        list.get(1)["loginName"] == "test1"
//        list.get(2)["loginName"] == "test2"
//    }
//
//    def "UpdateEnvUserPermission"() {
//        given:
//        List<Long> userIds = new ArrayList<>()
//        userIds.add(2L)
//        userIds.add(4L)
//
//        List<UserE> addIamUsers = new ArrayList<>()
//        UserE userE = new UserE()
//        userE.setId(4L)
//        userE.setLoginName("test4")
//        userE.setRealName("realTest4")
//        addIamUsers.add(userE)
//        iamRepository.listUsersByIds(_ as List<Long>) >> addIamUsers
//
//        // 添加4
//        UserAttrE userAttrE = new UserAttrE()
//        userAttrE.setGitlabUserId(4L)
//        // 删掉1和3
//        UserAttrE userAttrE1 = new UserAttrE()
//        userAttrE1.setGitlabUserId(1L)
//        UserAttrE userAttrE2 = new UserAttrE()
//        userAttrE2.setGitlabUserId(3)
//        3 * userAttrRepository.queryById(_ as Long) >> userAttrE >> userAttrE1 >> userAttrE2
//        gitlabRepository.addMemberIntoProject(_ as Integer, _ as MemberDTO) >> null
//
//        GitlabMemberE gitlabGroupMemberE = new GitlabMemberE()
//        gitlabGroupMemberE.setId(1)
//        GitlabMemberE gitlabGroupMemberE1 = new GitlabMemberE()
//        gitlabGroupMemberE1.setId(3)
//        2 * gitlabProjectRepository.getProjectMember(_ as Integer, _ as Integer) >> gitlabGroupMemberE >> gitlabGroupMemberE1
//        2 * gitlabRepository.removeMemberFromProject(_ as Integer, _ as Integer) >> null >> null
//
//        when:
//        def count = restTemplate.postForObject("/v1/projects/1/envs/1/permission", userIds, Boolean.class)
//
//        then:
//        devopsEnvUserPermissionMapper.selectAll().get(0)["iamUserId"] == 2
//        devopsEnvUserPermissionMapper.selectAll().get(1)["iamUserId"] == 4
//
//        devopsEnvCommitMapper.deleteByPrimaryKey(1L)
//        applicationInstanceMapper.deleteByPrimaryKey(1L)
//        applicationInstanceMapper.deleteByPrimaryKey(2L)
//        devopsServiceRepository.delete(1L)
//        devopsServiceRepository.delete(2L)
//        devopsServiceRepository.delete(3L)
//        devopsEnvGroupMapper.deleteByPrimaryKey(1L)
//        devopsEnvGroupMapper.deleteByPrimaryKey(2L)
//        devopsEnvironmentMapper.deleteByPrimaryKey(1L)
//        devopsEnvironmentMapper.deleteByPrimaryKey(2L)
//        devopsEnvironmentMapper.deleteByPrimaryKey(3L)
//
//        DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDO()
//        devopsEnvUserPermissionDO.setEnvId(1L)
//        devopsEnvUserPermissionMapper.delete(devopsEnvUserPermissionDO)
//    }

    private static Organization initOrg(Long id, String code) {
        Organization organization = new Organization()
        organization.setId(id)
        organization.setCode(code)
        organization
    }

    private static ProjectE initProj(Long id, String code, Organization organization) {
        ProjectE projectE = new ProjectE()
        projectE.setId(id)
        projectE.setCode(code)
        projectE.setOrganization(organization)
        projectE
    }

    private
    static DevopsEnvironmentDO initEnv(String name, String code, ProjectE projectE, Boolean active, Long sequence, String token) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
        devopsEnvironmentDO.setName(name)
        devopsEnvironmentDO.setCode(code)
        devopsEnvironmentDO.setProjectId(projectE.getId())
        devopsEnvironmentDO.setActive(active)
        devopsEnvironmentDO.setSequence(sequence)
        devopsEnvironmentDO.setToken(token)
        devopsEnvironmentDO
    }

}