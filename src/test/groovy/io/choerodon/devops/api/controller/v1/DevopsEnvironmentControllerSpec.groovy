package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.*
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleSearchVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.api.vo.iam.UserVO
import io.choerodon.devops.app.service.*
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.*
import io.choerodon.devops.infra.util.GitUtil
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.*
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/4
 * Time: 15:49
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvironmentController)
@Stepwise
class DevopsEnvironmentControllerSpec extends Specification {
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private UserAttrMapper userAttrMapper
    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsServiceService devopsServiceService
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService
    @Autowired
    private DevopsEnvCommitMapper devopsEnvCommitMapper
    @Autowired
    private AppServiceInstanceService appServiceInstanceService
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper
    @Autowired
    private DevopsIngressMapper devopsIngressMapper
    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper
    @Autowired
    private DevopsServiceMapper devopsServiceMapper
    @Autowired
    private DevopsClusterMapper devopsClusterMapper
    @Autowired
    private DevopsClusterProPermissionMapper devopsClusterProPermissionMapper
    @Autowired
    private DevopsServiceInstanceService devopsServiceAppInstanceMapper

    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler mockClusterConnectionHandler

    @Autowired
    @Qualifier("mockGitUtil")
    private GitUtil gitUtil

    @Autowired
    private UserAttrService userAttrRepository
    @Autowired
    private IamService iamRepository
    @Autowired
    private GitlabServiceClientOperator gitlabRepository
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberRepository

    BaseServiceClient iamServiceClient = Mockito.mock(BaseServiceClient)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)
    GitlabServiceClientOperator gitlabServiceClientOperator = Mockito.mock(GitlabServiceClientOperator)

    @Shared
    ProjectVO projectE = new ProjectVO()
    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()
    @Shared
    DevopsEnvironmentDTO devopsEnvironmentDO1 = new DevopsEnvironmentDTO()
    @Shared
    DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO()
    @Shared
    DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO1 = new DevopsEnvUserPermissionDTO()
    @Shared
    DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO2 = new DevopsEnvUserPermissionDTO()
    @Shared
    DevopsClusterDTO devopsClusterDO = new DevopsClusterDTO()

    def setupSpec() {
//        given:
//        organization.setId(init_id)
//        organization.setCode("org")

        projectE.setId(init_id)
        projectE.setCode("pro")
        projectE.setOrganizationId(init_id)

        Map<String, Object> params = new HashMap<>()
        params.put("name", [])
        params.put("code", ["app"])
        searchParam.put("searchParam", params)
        searchParam.put("param", "")

        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setActive(true)
        devopsEnvironmentDO.setClusterId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setFailed(false)
        devopsEnvironmentDO.setToken("testToken")
        devopsEnvironmentDO.setName("testNameEnv")
        devopsEnvironmentDO.setCode("testCodeEnv")
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)

        devopsEnvironmentDO1.setId(2L)
        devopsEnvironmentDO1.setActive(true)
        devopsEnvironmentDO1.setClusterId(1L)
        devopsEnvironmentDO1.setProjectId(1L)
        devopsEnvironmentDO1.setFailed(false)
        devopsEnvironmentDO1.setToken("testToken1")
        devopsEnvironmentDO1.setCode("testCodeEnv1")
        devopsEnvironmentDO1.setName("testNameEnv1")
        devopsEnvironmentDO1.setGitlabEnvProjectId(2L)

        devopsEnvUserPermissionDO.setEnvId(1L)
        devopsEnvUserPermissionDO.setIamUserId(1L)
        devopsEnvUserPermissionDO.setPermitted(true)
        devopsEnvUserPermissionDO.setLoginName("test")
        devopsEnvUserPermissionDO.setRealName("realTest")

        devopsEnvUserPermissionDO1.setEnvId(1L)
        devopsEnvUserPermissionDO1.setIamUserId(2L)
        devopsEnvUserPermissionDO1.setPermitted(true)
        devopsEnvUserPermissionDO1.setLoginName("test1")
        devopsEnvUserPermissionDO1.setRealName("realTest1")

        devopsEnvUserPermissionDO2.setEnvId(1L)
        devopsEnvUserPermissionDO2.setIamUserId(3L)
        devopsEnvUserPermissionDO2.setPermitted(true)
        devopsEnvUserPermissionDO2.setLoginName("test2")
        devopsEnvUserPermissionDO2.setRealName("realTest2")

        devopsClusterDO.setId(1L)
        devopsClusterDO.setName("testCluster")
        devopsClusterDO.setChoerodonId("choerodon")
        devopsClusterDO.setSkipCheckProjectPermission(true)
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClientOperator", gitlabServiceClient)


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

        MemberDTO memberDO = new MemberDTO()
        memberDO.setAccessLevel(AccessLevel.OWNER.value)
        ResponseEntity<MemberDTO> responseEntity2 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(responseEntity2)

        List<RoleVO> ownerRoleDTOList = new ArrayList<>()
        List<RoleVO> memberRoleDTOList = new ArrayList<>()
        RoleVO ownerRoleDTO = new RoleVO()
        ownerRoleDTO.setId(45L)
        ownerRoleDTO.setCode("role/project/default/project-owner")
        ownerRoleDTOList.add(ownerRoleDTO)
        PageInfo<RoleVO> ownerRoleDTOPage = new PageInfo<>(ownerRoleDTOList)
        RoleVO memberRoleDTO = new RoleVO()
        memberRoleDTO.setId(43L)
        memberRoleDTO.setCode("role/project/default/project-member")
        memberRoleDTOList.add(memberRoleDTO)
        PageInfo<RoleVO> memberRoleDTOPage = new PageInfo<>(memberRoleDTOList)
        ResponseEntity<PageInfo<RoleVO>> responseEntity3 = new ResponseEntity<>(ownerRoleDTOPage, HttpStatus.OK)
        RoleSearchVO ownerRoleSearchDTO = new RoleSearchVO()
        ownerRoleSearchDTO.setCode("role/project/default/project-owner")
        ResponseEntity<PageInfo<RoleVO>> responseEntity4 = new ResponseEntity<>(memberRoleDTOPage, HttpStatus.OK)
        RoleSearchVO memberRoleSearchDTO = new RoleSearchVO()
        memberRoleSearchDTO.setCode("role/project/default/project-member")
        Mockito.when(iamServiceClient.queryRoleIdByCode(any(RoleSearchVO.class))).thenReturn(responseEntity3).thenReturn(responseEntity4)

        and: 'mock查询项目成员和所有者的角色列表'
        List<UserVO> ownerUserDTOList = new ArrayList<>()
        List<UserVO> memberUserDTOList = new ArrayList<>()
        UserVO ownerUserDTO = new UserVO()
        ownerUserDTO.setId(1L)
        ownerUserDTO.setLoginName("test")
        ownerUserDTO.setRealName("realTest")
        ownerUserDTOList.add(ownerUserDTO)
        PageInfo<UserVO> ownerUserDTOPage = new PageInfo<>(ownerUserDTOList)
        UserVO memberUserDTO = new UserVO()
        memberUserDTO.setId(4L)
        memberUserDTO.setLoginName("test4")
        memberUserDTO.setRealName("realTest4")
        memberUserDTOList.add(memberUserDTO)
        PageInfo<UserVO> memberUserDTOPage = new PageInfo<>(memberUserDTOList)
        ResponseEntity<PageInfo<UserVO>> ownerPageResponseEntity = new ResponseEntity<>(ownerUserDTOPage, HttpStatus.OK)
        ResponseEntity<PageInfo<UserVO>> memberPageResponseEntity = new ResponseEntity<>(memberUserDTOPage, HttpStatus.OK)
        RoleAssignmentSearchVO roleAssignmentSearchDTO = new RoleAssignmentSearchVO()
        roleAssignmentSearchDTO.setLoginName("")
        roleAssignmentSearchDTO.setRealName("")
        String[] param = new String[1]
        param[0] = ""
        roleAssignmentSearchDTO.setParam(param)
        Mockito.when(iamServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), anyLong(), anyLong(), anyBoolean(), any(RoleAssignmentSearchVO.class))).thenReturn(ownerPageResponseEntity).thenReturn(memberPageResponseEntity)
    }

    def "Create"() {
        given: '插入env'
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO1)
        devopsClusterMapper.insert(devopsClusterDO)

        and: '插入envUserPermission'
        devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO)
        devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO1)
        devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO2)

        and: '设置DTO类'
        DevopsEnviromentRepVO devopsEnviromentDTO = new DevopsEnviromentRepVO()
        devopsEnviromentDTO.setClusterId(1L)
        devopsEnviromentDTO.setCode("testCodeChange")
        devopsEnviromentDTO.setName("testNameChange")
        devopsEnviromentDTO.setDevopsEnvGroupId(1L)

        and: '设置用户'
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)

        and: '初始化sagaClient mock对象'
        devopsEnvironmentService.initMockService(sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(null, null)

        and: 'mock查询用户'
        List<IamUserDTO> userDOList = new ArrayList<>()
        IamUserDTO userDO = new IamUserDTO()
        userDO.setLoginName("loginName")
        userDO.setRealName("realName")
        userDOList.add(userDO)
        ResponseEntity<List<IamUserDTO>> responseEntity2 = new ResponseEntity<>(userDOList, HttpStatus.OK)
        Mockito.when(iamServiceClient.listUsersByIds(any(Long[].class))).thenReturn(responseEntity2)
        Mockito.doReturn(responseEntity2).when(iamServiceClient).listUsersByIds(1L)
        userAttrRepository.baseQueryById(_ as Long) >> userAttrE

        and: 'mock envUtil'
        GitConfigVO gitConfigDTO = new GitConfigVO()
        gitUtil.getGitConfig(_ as Long) >> gitConfigDTO
        when: '项目下创建环境'
        restTemplate.postForObject("/v1/projects/1/envs", devopsEnviromentDTO, String.class)

        then: '返回值'
        devopsEnvironmentMapper.selectAll().size() == 3
    }

    def "ListByProjectIdDeployed"() {
        given: '设置网络对象'
        DevopsServiceDTO devopsServiceE = new DevopsServiceDTO()
        devopsServiceE.setId(1L)
        devopsServiceE.setEnvId(1L)
        devopsServiceE.setStatus("running")
        DevopsServiceDTO devopsServiceE1 = new DevopsServiceDTO()
        devopsServiceE.setId(2L)
        devopsServiceE1.setEnvId(2L)
        devopsServiceE1.setStatus("running")
        devopsServiceService.baseCreate(devopsServiceE)
        devopsServiceService.baseCreate(devopsServiceE1)

        and: 'mock envUtil方法'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        List<Long> connectedClusterList = new ArrayList<>()
        connectedClusterList.add(3L)
        mockClusterConnectionHandler.getConnectedEnvList() >> connectedClusterList
        mockClusterConnectionHandler.getUpdatedEnvList() >> envList

        when: '项目下查询存在网络环境'
        def envs = restTemplate.getForObject("/v1/projects/1/envs/deployed", List.class)

        then: '校验返回值'
        envs.size() == 2

    }

    def "ListByProjectIdAndActive"() {
        given: 'mock envUtil方法'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        mockClusterConnectionHandler.getConnectedEnvList() >> envList
        mockClusterConnectionHandler.getUpdatedEnvList() >> envList

        when: '项目下查询环境'
        def envs = restTemplate.getForObject("/v1/projects/1/envs?active=true", List.class)

        then: '返回值'
        envs.size() == 3
    }

    def "ListByProjectIdAndActiveWithGroup"() {
        given: '初始化envList'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        and: 'mock envUtil方法'
        DevopsEnvGroupDTO devopsEnvGroupDO = new DevopsEnvGroupDTO()
        devopsEnvGroupDO.setId(1L)
        devopsEnvGroupDO.setProjectId(1L)
        DevopsEnvGroupDTO devopsEnvGroupDO1 = new DevopsEnvGroupDTO()
        devopsEnvGroupDO1.setId(2L)
        devopsEnvGroupDO1.setProjectId(1L)
        devopsEnvGroupMapper.insert(devopsEnvGroupDO)
        devopsEnvGroupMapper.insert(devopsEnvGroupDO1)
        mockClusterConnectionHandler.getConnectedEnvList() >> envList
        mockClusterConnectionHandler.getUpdatedEnvList() >> envList

        when: '项目下环境流水线查询环境'
        def list = restTemplate.getForObject("/v1/projects/1/envs/groups?active=true", List.class)

        then: '返回值'
        list.size() == 3
    }

//    def "QueryShell"() {
//        when: '项目下查询单个环境的可执行shell'
//        String shell = restTemplate.getForObject("/v1/projects/1/envs/1/shell", String.class)
//
//        then: '返回值'
//        !shell.isEmpty()
//    }

    def "EnableOrDisableEnv"() {
        given: 'mock envUtil方法'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        List<Long> connectedEnvList = new ArrayList<>()
        connectedEnvList.add(3L)
        mockClusterConnectionHandler.getConnectedEnvList() >> connectedEnvList
        mockClusterConnectionHandler.getUpdatedEnvList() >> envList

        when: '项目下启用停用环境'
        restTemplate.put("/v1/projects/1/envs/1/active?active=false", Boolean.class)

        then: '返回值'
        !devopsEnvironmentMapper.selectByPrimaryKey(1L).getActive()
    }

    def "Query"() {
        when: '项目下查询单个环境'
        DevopsEnvironmentUpdateVO dto = restTemplate.getForObject("/v1/projects/1/envs/1", DevopsEnvironmentUpdateVO.class)

        then: '返回值'
        dto["code"] == "testCodeEnv"
    }

    def "Update"() {
        given: '初始化环境更新DTO对象'
        def envs = devopsEnvironmentMapper.selectAll()
        def envId = envs.get(envs.size() - 1).getId()
        DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO = new DevopsEnvironmentUpdateVO()
        devopsEnvironmentUpdateDTO.setId(envId)
        devopsEnvironmentUpdateDTO.setClusterId(1L)
        devopsEnvironmentUpdateDTO.setDevopsEnvGroupId(2L)
        devopsEnvironmentUpdateDTO.setName("testNameChange1222")

        when: '项目下更新环境'
        restTemplate.put("/v1/projects/1/envs", devopsEnvironmentUpdateDTO, DevopsEnvironmentUpdateVO.class)

        then: '返回值'
        devopsEnvironmentMapper.selectByPrimaryKey(envId).getName() == "testNameChange1222"
    }

    def "Sort"() {
        given: 'mock envUtil方法'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        Long[] sequence = [2L, 1L]
        mockClusterConnectionHandler.getConnectedEnvList() >> envList
        mockClusterConnectionHandler.getUpdatedEnvList() >> envList

        when: '项目下环境流水线排序'
        restTemplate.put("/v1/projects/1/envs/sort", sequence, List.class)

        then: '返回值'
        devopsEnvironmentMapper.selectByPrimaryKey(1L).getSequence() == 2L
    }

    def "CheckCode"() {
        when: '创建环境校验编码是否存在'
        def exception = restTemplate.getForEntity("/v1/projects/1/envs/check_code?cluster_id=1&code=testCheckCode", ExceptionResponse.class)

        then: '返回值'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }

    def "ListByProjectId"() {
        given: '初始化应用实例DO对象'
        List<Long> envList = new ArrayList<>()
        AppServiceInstanceDTO applicationInstanceDO = new AppServiceInstanceDTO()
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
        AppServiceInstanceDTO applicationInstanceDO1 = new AppServiceInstanceDTO()
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
        appServiceInstanceMapper.insert(applicationInstanceDO)
        appServiceInstanceMapper.insert(applicationInstanceDO1)

        and: 'mock envUtil方法'
        envList.add(1L)
        envList.add(2L)
        mockClusterConnectionHandler.getConnectedEnvList() >> envList
        mockClusterConnectionHandler.getUpdatedEnvList() >> envList

        when: '项目下查询有正在运行实例的环境'
        def envs = restTemplate.getForObject("/v1/projects/1/envs/instance", List.class)

        then: '返回值'
        envs.size() == 1
    }

    def "QueryEnvSyncStatus"() {
        given: '更新devopsEnvCommit对象'
        DevopsEnvCommitDTO devopsEnvCommitDTO = new DevopsEnvCommitDTO()
        devopsEnvCommitDTO.setId(1L)
        devopsEnvCommitDTO.setCommitSha("testCommitSha")
        devopsEnvCommitMapper.insert(devopsEnvCommitDTO)
        devopsEnvironmentMapper.updateAgentSyncEnvCommit(1L, 1L)
        devopsEnvironmentMapper.updateDevopsSyncEnvCommit(1L, 1L)
        devopsEnvironmentMapper.updateSagaSyncEnvCommit(1L, 1L)

        when: '查询环境同步状态'
        def envSyncStatusDTO = restTemplate.getForObject("/v1/projects/1/envs/1/status", EnvSyncStatusVO.class)

        then: '返回值'
        envSyncStatusDTO.getAgentSyncCommit() == "testCommitSha"
    }

    def "ListUserPermissionByEnvId"() {
        given: '初始化param参数'
        String params = "{\"searchParam\": {\"loginName\": [],\"realName\": []},\"param\": \"\"}"
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(params, headers)

        when: '分页查询项目下用户权限'
        def page = restTemplate.postForObject("/v1/projects/1/envs/baseList?page=0&size=5", strEntity, PageInfo.class)

        then: '返回值'
        page != null

        expect: '校验查询结果'
        page.getList().get(0)["loginName"] == "test"
        page.getList().get(0)["iamUserId"] == 1
        page.getList().get(0)["realName"] == "realTest"
    }

    def "ListAllUserPermission"() {
        when: '获取环境下所有用户权限'
        def list = restTemplate.getForObject("/v1/projects/1/envs/1/list_all", List.class)

        then: '返回值'
        !list.isEmpty()

        expect: '校验查询结果'
        list.get(0)["loginName"] == "test"
        list.get(1)["loginName"] == "test1"
        list.get(2)["loginName"] == "test2"
    }

    def "UpdateEnvUserPermission"() {
        given: '初始化有权限的userIds'
        List<Long> userIds = new ArrayList<>()
        userIds.add(2L)
        userIds.add(4L)

        and: 'mock待添加的iam用户列表'
        List<IamUserDTO> addIamUserList = new ArrayList<>()
        IamUserDTO iamUserDTO = new IamUserDTO()
        iamUserDTO.setId(4L)
        iamUserDTO.setLoginName("test4")
        iamUserDTO.setRealName("realTest4")
        addIamUserList.add(iamUserDTO)
        ResponseEntity<List<IamUserDTO>> addIamUserResponseEntity = new ResponseEntity<>(addIamUserList, HttpStatus.OK)
        Mockito.when(iamServiceClient.listUsersByIds(any(Long[].class))).thenReturn(addIamUserResponseEntity)

        and: '初始化用户3，4的gitlab对象'
        UserAttrDTO userAttrDO1 = new UserAttrDTO()
        userAttrDO1.setIamUserId(3L)
        userAttrDO1.setGitlabUserId(3L)
        userAttrMapper.insert(userAttrDO1)
        UserAttrDTO userAttrDO2 = new UserAttrDTO()
        userAttrDO2.setIamUserId(4L)
        userAttrDO2.setGitlabUserId(4L)
        userAttrMapper.insert(userAttrDO2)

        and: '添加用户4'
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createProjectMember(anyInt(), any(MemberDTO.class))).thenReturn(responseEntity)

        and: '查询gitlab项目下是否有1和3用户'
        MemberDTO memberDO1 = new MemberDTO()
        memberDO1.setId(1)
        memberDO1.setAccessLevel(AccessLevel.NONE.value)
        ResponseEntity<MemberDTO> memberDOResponseEntity1 = new ResponseEntity<>(memberDO1, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getProjectMember(anyInt(), anyInt())).thenReturn(memberDOResponseEntity1)

        and: '删除1和3的gitlab用户'
        ResponseEntity responseEntity1 = new ResponseEntity(HttpStatus.OK)
        ResponseEntity responseEntity2 = new ResponseEntity(HttpStatus.OK)
        Mockito.when(gitlabServiceClient.deleteProjectMember(anyInt(), anyInt())).thenReturn(responseEntity1).thenReturn(responseEntity2)

        when: '环境下为用户分配权限'
        restTemplate.postForObject("/v1/projects/1/envs/1/permission", userIds, Boolean.class)

        then: '返回值'
        List<DevopsEnvUserPermissionDTO> lastUsers = devopsEnvUserPermissionMapper.selectAll()

        expect: '校验用户4和用户2'
        lastUsers.get(0)["iamUserId"] == 2
        lastUsers.get(1)["iamUserId"] == 4
    }

    def "ListDevopsClusters"() {
        given: '创建集群和项目关联关系'

        DevopsClusterProPermissionDTO devopsClusterProPermissionDO = new DevopsClusterProPermissionDTO()
        devopsClusterProPermissionDO.setClusterId(1L)
        devopsClusterProPermissionDO.setProjectId(1L)
        devopsClusterProPermissionMapper.insert(devopsClusterProPermissionDO)

        and: 'mock envUtil'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        mockClusterConnectionHandler.getConnectedEnvList() >> envList
        mockClusterConnectionHandler.getUpdatedEnvList() >> envList

        when: '项目下查询集群信息'
        def list = restTemplate.getForObject("/v1/projects/1/envs/clusters", List.class)

        then: '校验返回值'
        list.get(0)["name"] == "testCluster"

        // 删除user，保留默认初始化的1号用户
        List<UserAttrDTO> list0 = userAttrMapper.selectAll()
        if (list0 != null && !list0.isEmpty()) {
            for (UserAttrDTO e : list0) {
                if (e.getIamUserId() != 1L) {
                    userAttrMapper.delete(e)
                }
            }
        }
        // 删除envCommit
        List<DevopsEnvCommitDTO> list1 = devopsEnvCommitMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvCommitDTO e : list1) {
                devopsEnvCommitMapper.delete(e)
            }
        }
        // 删除appInstance
        List<AppServiceInstanceDTO> list2 = appServiceInstanceMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (AppServiceInstanceDTO e : list2) {
                appServiceInstanceMapper.delete(e)
            }
        }
        // 删除service
        List<DevopsServiceDTO> list3 = devopsServiceMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (DevopsServiceDTO e : list3) {
                devopsServiceMapper.delete()
            }
        }
        // 删除envGroup
        List<DevopsEnvGroupDTO> list4 = devopsEnvGroupMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (DevopsEnvGroupDTO e : list4) {
                devopsEnvGroupMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDTO> list5 = devopsEnvironmentMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (DevopsEnvironmentDTO e : list5) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除cluster
        List<DevopsClusterDTO> list6 = devopsClusterMapper.selectAll()
        if (list6 != null && !list6.isEmpty()) {
            for (DevopsClusterDTO e : list6) {
                devopsClusterMapper.delete(e)
            }
        }
        // 删除clusterProPermission
        List<DevopsClusterProPermissionDTO> list7 = devopsClusterProPermissionMapper.selectAll()
        if (list7 != null && !list7.isEmpty()) {
            for (DevopsClusterProPermissionDTO e : list7) {
                devopsClusterProPermissionMapper.delete(e)
            }
        }
        // 删除envUserPermission
        List<DevopsEnvUserPermissionDTO> list8 = devopsEnvUserPermissionMapper.selectAll()
        if (list8 != null && !list8.isEmpty()) {
            for (DevopsEnvUserPermissionDTO e : list8) {
                devopsEnvUserPermissionMapper.delete(e)
            }
        }
    }

    def "DeleteDeactivatedEnvironment"() {
        given: '插入关联环境的对象'
        DevopsEnvironmentDTO devopsEnvironmentDODel = new DevopsEnvironmentDTO()
        devopsEnvironmentDODel.setId(999L)
        devopsEnvironmentDODel.setClusterId(1L)
        devopsEnvironmentDODel.setGitlabEnvProjectId(888L)
        devopsEnvironmentMapper.insert(devopsEnvironmentDODel)

        AppServiceInstanceDTO applicationInstanceDODel = new AppServiceInstanceDTO()
        applicationInstanceDODel.setId(999L)
        applicationInstanceDODel.setEnvId(999L)
        appServiceInstanceMapper.insert(applicationInstanceDODel)

        DevopsIngressDTO devopsIngressDODel = new DevopsIngressDTO()
        devopsIngressDODel.setId(1000L)
        devopsIngressDODel.setEnvId(999L)
        devopsIngressMapper.insert(devopsIngressDODel)

        DevopsIngressDTO devopsIngressDODel1 = new DevopsIngressDTO()
        devopsIngressDODel1.setId(2000L)
        devopsIngressDODel1.setEnvId(999L)
        devopsIngressMapper.insert(devopsIngressDODel1)

        DevopsIngressPathDTO devopsIngressPathDODel = new DevopsIngressPathDTO()
        devopsIngressPathDODel.setId(100L)
        devopsIngressPathDODel.setIngressId(1000L)
        devopsIngressPathMapper.insert(devopsIngressPathDODel)

        DevopsIngressPathDTO devopsIngressPathDODel1 = new DevopsIngressPathDTO()
        devopsIngressPathDODel1.setId(200L)
        devopsIngressPathDODel1.setIngressId(2000L)
        devopsIngressPathMapper.insert(devopsIngressPathDODel1)

        DevopsServiceDTO devopsServiceDODel = new DevopsServiceDTO()
        devopsServiceDODel.setId(1L)
        devopsServiceDODel.setEnvId(999L)
        devopsServiceMapper.insert(devopsServiceDODel)

        DevopsServiceDTO devopsServiceDODel1 = new DevopsServiceDTO()
        devopsServiceDODel1.setId(2L)
        devopsServiceDODel1.setEnvId(999L)
        devopsServiceMapper.insert(devopsServiceDODel1)

        DevopsServiceInstanceDTO devopsServiceAppInstanceDODel = new DevopsServiceInstanceDTO()
        devopsServiceAppInstanceDODel.setId(7L)
        devopsServiceAppInstanceDODel.setServiceId(1L)
        devopsServiceAppInstanceMapper.insert(devopsServiceAppInstanceDODel)

        DevopsServiceInstanceDTO devopsServiceAppInstanceDODel1 = new DevopsServiceInstanceDTO()
        devopsServiceAppInstanceDODel1.setId(8L)
        devopsServiceAppInstanceDODel1.setServiceId(2L)
        devopsServiceAppInstanceMapper.insert(devopsServiceAppInstanceDODel1)

        and: 'mock 删除gitlab仓库'
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.OK)
        Mockito.when(gitlabServiceClient.deleteProjectById(anyInt(), anyInt())).thenReturn(responseEntity)
        GitlabProjectDTO gitlabProjectDO = new GitlabProjectDTO()
        gitlabProjectDO.setId(1)
        Mockito.when(gitlabServiceClient.queryProjectById(anyInt())).thenReturn(new ResponseEntity<>(gitlabProjectDO, HttpStatus.OK))

        when: '删除已停用的环境'
        restTemplate.delete("/v1/projects/1/envs/999")

        then: '校验所有关联对象是否被删除'
        devopsEnvironmentMapper.selectAll().size() == 0
        appServiceInstanceMapper.selectAll().size() == 0
        devopsIngressMapper.selectAll().size() == 0
        devopsIngressPathMapper.selectAll().size() == 0
        devopsServiceMapper.selectAll().size() == 0
        devopsServiceAppInstanceMapper.selectAll().size() == 0
    }
}