package io.choerodon.devops.api.controller.v1

import com.alibaba.fastjson.JSONArray
import com.github.pagehelper.PageInfo
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.*
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleSearchVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.*
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.enums.InstanceStatus
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.*
import io.choerodon.devops.infra.util.GitUtil
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
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
 * Date: 2018/9/4
 * Time: 15:49
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvironmentController)
@Stepwise
class DevopsEnvironmentControllerSpec extends Specification {
    @Shared
    private String rootUrl = "/v1/projects/{project_id}/envs"

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
    private DevopsServiceInstanceMapper devopsServiceInstanceMapper
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsEnvAppServiceMapper devopsEnvAppServiceMapper
    @Autowired
    private AppServiceMapper appServiceMapper
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator

    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler mockClusterConnectionHandler

    @Autowired
    @Qualifier("mockGitUtil")
    private GitUtil gitUtil

    @Autowired
    @Qualifier("mockAgentPodInfoService")
    private AgentPodService agentPodService

    @Autowired
    private UserAttrService userAttrService
    @Autowired
    private IamService iamService
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService

    private BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient)
    private GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    Long projectId = 1L
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
    @Shared
    AppServiceDTO appServiceDTO = new AppServiceDTO()
    @Shared
    DevopsEnvAppServiceDTO devopsEnvAppServiceDTO = new DevopsEnvAppServiceDTO()
    @Shared
    AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO()
    @Shared
    DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO()
    @Shared
    DevopsEnvPodDTO devopsEnvPodDTO2 = new DevopsEnvPodDTO()
    @Shared
    UserAttrDTO userAttrDTO
    @Shared
    UserAttrDTO userToAssignPermission = new UserAttrDTO()
    @Shared
    DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO()
    @Shared
    DevopsEnvCommitDTO devopsEnvCommitDTO = new DevopsEnvCommitDTO()


    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToCleanup = false
    private static final Long ownerRoleId = 1L
    private static final Long memberRoleId = 2L

    def setup() {
        if (isToInit) {
            initData()

            DependencyInjectUtil.setAttribute(iamService, "baseServiceClient", baseServiceClient)
            DependencyInjectUtil.setAttribute(baseServiceClientOperator, "baseServiceClient", baseServiceClient)
            DependencyInjectUtil.setAttribute(gitlabServiceClientOperator, "gitlabServiceClient", gitlabServiceClient)

            initMock()
        }
    }

    def initData() {
        Map<String, Object> params = new HashMap<>()
        params.put("code", "app")
        searchParam.put("searchParam", params)
        searchParam.put("param", [])

        userAttrDTO = userAttrMapper.selectByPrimaryKey(1L)
        if (userAttrDTO == null) {
            throw new RuntimeException("Record with id 1 in table devops_user was deleted!!!")
        }

        userToAssignPermission.setIamUserId(5L)
        userToAssignPermission.setGitlabUserName("gitlab5")
        userToAssignPermission.setGitlabUserId(5L)

        devopsProjectDTO.setAppId(1L)
        devopsProjectDTO.setIamProjectId(1L)
        devopsProjectDTO.setDevopsEnvGroupId(1L)
        devopsProjectDTO.setDevopsAppGroupId(1L)
        devopsProjectMapper.insertSelective(devopsProjectDTO)

        devopsEnvCommitDTO.setId(1L)
        devopsEnvCommitDTO.setCommitSha("testCommitSha")
        devopsEnvCommitMapper.insertSelective(devopsEnvCommitDTO)

        devopsClusterDO.setId(1L)
        devopsClusterDO.setName("testCluster")
        devopsClusterDO.setChoerodonId("choerodon")
        devopsClusterDO.setSkipCheckProjectPermission(true)
        devopsClusterMapper.insertSelective(devopsClusterDO)

        DevopsClusterProPermissionDTO devopsClusterProPermissionDO = new DevopsClusterProPermissionDTO()
        devopsClusterProPermissionDO.setClusterId(1L)
        devopsClusterProPermissionDO.setProjectId(1L)
        devopsClusterProPermissionMapper.insert(devopsClusterProPermissionDO)

        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setActive(true)
        devopsEnvironmentDO.setClusterId(devopsClusterDO.getId())
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setFailed(false)
        devopsEnvironmentDO.setToken("testToken")
        devopsEnvironmentDO.setName("testNameEnv")
        devopsEnvironmentDO.setCode("testCodeEnv")
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)
        devopsEnvironmentDO.setSkipCheckPermission(Boolean.FALSE)
        devopsEnvironmentDO.setAgentSyncCommit(devopsEnvCommitDTO.getId())
        devopsEnvironmentDO.setDevopsSyncCommit(devopsEnvCommitDTO.getId())
        devopsEnvironmentDO.setSagaSyncCommit(devopsEnvCommitDTO.getId())
        devopsEnvironmentMapper.insertSelective(devopsEnvironmentDO)

        devopsEnvironmentDO1.setId(2L)
        devopsEnvironmentDO1.setActive(true)
        devopsEnvironmentDO1.setClusterId(devopsClusterDO.getId())
        devopsEnvironmentDO1.setProjectId(1L)
        devopsEnvironmentDO1.setFailed(false)
        devopsEnvironmentDO1.setToken("testToken1")
        devopsEnvironmentDO1.setCode("testCodeEnv1")
        devopsEnvironmentDO1.setName("testNameEnv1")
        devopsEnvironmentDO1.setGitlabEnvProjectId(2L)
        devopsEnvironmentDO1.setSkipCheckPermission(Boolean.FALSE)
        devopsEnvironmentMapper.insertSelective(devopsEnvironmentDO1)

        devopsEnvUserPermissionDO.setEnvId(devopsEnvironmentDO.getId())
        devopsEnvUserPermissionDO.setIamUserId(userAttrDTO.getIamUserId())
        devopsEnvUserPermissionDO.setPermitted(true)
        devopsEnvUserPermissionDO.setLoginName("test")
        devopsEnvUserPermissionDO.setRealName("realTest")
        devopsEnvUserPermissionMapper.insertSelective(devopsEnvUserPermissionDO)

        devopsEnvUserPermissionDO1.setEnvId(devopsEnvironmentDO.getId())
        devopsEnvUserPermissionDO1.setIamUserId(2L)
        devopsEnvUserPermissionDO1.setPermitted(true)
        devopsEnvUserPermissionDO1.setLoginName("test1")
        devopsEnvUserPermissionDO1.setRealName("realTest1")
        devopsEnvUserPermissionMapper.insertSelective(devopsEnvUserPermissionDO1)

        devopsEnvUserPermissionDO2.setEnvId(devopsEnvironmentDO.getId())
        devopsEnvUserPermissionDO2.setIamUserId(3L)
        devopsEnvUserPermissionDO2.setPermitted(true)
        devopsEnvUserPermissionDO2.setLoginName("test2")
        devopsEnvUserPermissionDO2.setRealName("realTest2")
        devopsEnvUserPermissionMapper.insertSelective(devopsEnvUserPermissionDO2)

        appServiceDTO.setId(1L)
        appServiceDTO.setCode("test-devops-service")
        appServiceDTO.setAppId(1L)
        appServiceDTO.setName("Devops服务")
        appServiceDTO.setChartConfigId(1L)
        appServiceDTO.setHarborConfigId(1L)
        appServiceDTO.setGitlabProjectId(11)
        appServiceDTO.setFailed(Boolean.FALSE)
        appServiceDTO.setSynchro(Boolean.TRUE)
        appServiceDTO.setSkipCheckPermission(Boolean.TRUE)
        appServiceMapper.insertSelective(appServiceDTO)

        appServiceInstanceDTO.setId(1L)
        appServiceInstanceDTO.setCode("test-devops-service-a7ab4c")
        appServiceInstanceDTO.setAppServiceId(appServiceDTO.getId())
        appServiceInstanceDTO.setEnvId(devopsEnvironmentDO.getId())
        appServiceInstanceDTO.setStatus(InstanceStatus.RUNNING.getStatus())
        appServiceInstanceMapper.insertSelective(appServiceInstanceDTO)

        devopsEnvPodDTO.setId(1L)
        devopsEnvPodDTO.setName("test-devops-service-a7abad11")
        devopsEnvPodDTO.setStatus("Running")
        devopsEnvPodDTO.setEnvId(devopsEnvironmentDO.getId())
        devopsEnvPodDTO.setInstanceId(appServiceInstanceDTO.getId())
        devopsEnvPodDTO.setNamespace(devopsEnvironmentDO.getCode())
        devopsEnvPodDTO.setReady(Boolean.TRUE)
        devopsEnvPodMapper.insertSelective(devopsEnvPodDTO)

        devopsEnvPodDTO2.setId(2L)
        devopsEnvPodDTO2.setName("test-devops-service-a7abad22")
        devopsEnvPodDTO2.setStatus("Running")
        devopsEnvPodDTO2.setEnvId(devopsEnvironmentDO.getId())
        devopsEnvPodDTO2.setInstanceId(appServiceInstanceDTO.getId())
        devopsEnvPodDTO2.setNamespace(devopsEnvironmentDO.getCode())
        devopsEnvPodDTO2.setReady(Boolean.TRUE)
        devopsEnvPodMapper.insertSelective(devopsEnvPodDTO2)

        devopsEnvAppServiceDTO.setEnvId(devopsEnvironmentDO.getId())
        devopsEnvAppServiceDTO.setAppServiceId(appServiceDTO.getId())
        devopsEnvAppServiceMapper.insertSelective(devopsEnvAppServiceDTO)
    }

    def mockBaseServiceQueryRoleIdByCode() {
        List<RoleVO> ownerRoleDTOList = new ArrayList<>()
        RoleVO ownerRoleDTO = new RoleVO()
        ownerRoleDTO.setId(ownerRoleId)
        ownerRoleDTO.setCode("role/project/default/project-owner")
        ownerRoleDTOList.add(ownerRoleDTO)

        RoleSearchVO ownerRoleSearchDTO = new RoleSearchVO()
        ownerRoleSearchDTO.setCode("role/project/default/project-owner")

        PageInfo<RoleVO> ownerRoleDTOPage = new PageInfo<>(ownerRoleDTOList)
        ResponseEntity<PageInfo<RoleVO>> responseEntity3 = new ResponseEntity<>(ownerRoleDTOPage, HttpStatus.OK)
        PowerMockito.when(baseServiceClient.queryRoleIdByCode(argThat(new ArgumentMatcher<RoleSearchVO>() {
            @Override
            boolean matches(RoleSearchVO argument) {
                return argument != null && argument.getCode() == ownerRoleDTO.getCode()
            }
        }))).thenReturn(responseEntity3)

        List<RoleVO> memberRoleDTOList = new ArrayList<>()
        RoleVO memberRoleDTO = new RoleVO()
        memberRoleDTO.setId(memberRoleId)
        memberRoleDTO.setCode("role/project/default/project-member")
        memberRoleDTOList.add(memberRoleDTO)
        PageInfo<RoleVO> memberRoleDTOPage = new PageInfo<>(memberRoleDTOList)
        ResponseEntity<PageInfo<RoleVO>> responseEntity4 = new ResponseEntity<>(memberRoleDTOPage, HttpStatus.OK)
        RoleSearchVO memberRoleSearchDTO = new RoleSearchVO()
        memberRoleSearchDTO.setCode("role/project/default/project-member")
        PowerMockito.when(baseServiceClient.queryRoleIdByCode(argThat(new ArgumentMatcher<RoleSearchVO>() {
            @Override
            boolean matches(RoleSearchVO argument) {
                return argument != null && argument.getCode() == memberRoleDTO.getCode()
            }
        }))).thenReturn(responseEntity4)
    }

    def mockQueryUsersByRoleId() {
        // mock查询项目成员和所有者的角色列表'
        List<IamUserDTO> ownerUserDTOList = new ArrayList<>()
        IamUserDTO ownerUserDTO = new IamUserDTO()
        ownerUserDTO.setId(4L)
        ownerUserDTO.setLoginName("test")
        ownerUserDTO.setRealName("realTest")
        ownerUserDTOList.add(ownerUserDTO)
        PageInfo<IamUserDTO> ownerUserDTOPage = new PageInfo<>(ownerUserDTOList)
        ResponseEntity<PageInfo<IamUserDTO>> ownerPageResponseEntity = new ResponseEntity<>(ownerUserDTOPage, HttpStatus.OK)
        PowerMockito.when(baseServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), eq(ownerRoleId), anyLong(), anyBoolean(), any(RoleAssignmentSearchVO.class))).thenReturn(ownerPageResponseEntity)


        IamUserDTO memberUserDTO = new IamUserDTO()
        List<IamUserDTO> memberUserDTOList = new ArrayList<>()
        memberUserDTO.setId(userToAssignPermission.getIamUserId())
        memberUserDTO.setLoginName("test4")
        memberUserDTO.setRealName("realTest4")
        memberUserDTOList.add(memberUserDTO)
        PageInfo<IamUserDTO> memberUserDTOPage = new PageInfo<>(memberUserDTOList)
        ResponseEntity<PageInfo<IamUserDTO>> memberPageResponseEntity = new ResponseEntity<>(memberUserDTOPage, HttpStatus.OK)
        Mockito.when(baseServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), eq(memberRoleId), anyLong(), anyBoolean(), any(RoleAssignmentSearchVO.class))).thenReturn(memberPageResponseEntity)
    }

    def mockQueryUsersByIds() {
        // mock查询用户
        List<IamUserDTO> iamUserDTOS = new ArrayList<>()
        IamUserDTO iamUserDTO = new IamUserDTO()
        iamUserDTO.setId(1L)
        iamUserDTO.setLoginName("loginName")
        iamUserDTO.setRealName("realName")
        iamUserDTOS.add(iamUserDTO)
        ResponseEntity<List<IamUserDTO>> responseEntity22 = new ResponseEntity<>(iamUserDTOS, HttpStatus.OK)
        PowerMockito.when(baseServiceClient.listUsersByIds(argThat(new ArgumentMatcher<Long[]>() {
            @Override
            boolean matches(Long[] argument) {
                return argument != null && Arrays.asList(argument).contains(1L)
            }
        }))).thenReturn(responseEntity22)

        List<IamUserDTO> iamUserDTOS2 = new ArrayList<>()
        IamUserDTO iamUserDTO1 = new IamUserDTO()
        iamUserDTO1.setId(5L)
        iamUserDTO1.setLoginName("loginName5")
        iamUserDTO1.setRealName("realName5")
        iamUserDTOS2.add(iamUserDTO1)
        ResponseEntity<List<IamUserDTO>> responseEntity = new ResponseEntity<>(iamUserDTOS2, HttpStatus.OK)
        PowerMockito.when(baseServiceClient.listUsersByIds(argThat(new ArgumentMatcher<Long[]>() {
            @Override
            boolean matches(Long[] argument) {
                return argument != null && Arrays.asList(argument).contains(5L)
            }
        }))).thenReturn(responseEntity)
    }

    def initMock() {
        // 查询项目
        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(baseServiceClient).queryIamProject(1L)

        // 查询组织
        OrganizationDTO organizationDO = new OrganizationDTO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(1L)

        mockBaseServiceQueryRoleIdByCode()

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
        Mockito.doReturn(pageResponseEntity).when(baseServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        // mock 查询用户在gitlab的角色
        MemberDTO memberDO = new MemberDTO()
        memberDO.setUserId(userAttrDTO.getGitlabUserId().intValue())
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
        ResponseEntity<MemberDTO> responseEntity2 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(responseEntity2)

        mockQueryUsersByIds()


        mockQueryUsersByRoleId()

        // mock clusterConnectionHandler
        List<Long> envList = new ArrayList<>(2)
        envList.add(1L)
        envList.add(2L)
        PowerMockito.when(mockClusterConnectionHandler.getConnectedEnvList()).thenReturn(envList)
        PowerMockito.when(mockClusterConnectionHandler.getUpdatedEnvList()).thenReturn(envList)

        // mock AgentPodService
        AgentPodInfoVO agentPodInfoVO = new AgentPodInfoVO()
        agentPodInfoVO.setNamespace(devopsEnvPodDTO.getNamespace())
        agentPodInfoVO.setNodeName(devopsEnvPodDTO.getNodeName())
        agentPodInfoVO.setPodName(devopsEnvPodDTO.getName())
        agentPodInfoVO.setSnapshotTime(new Date())
        agentPodInfoVO.setCpuUsed("500m")
        agentPodInfoVO.setMemoryUsed("300MiB")
        PowerMockito.when(agentPodService.queryLatestPodSnapshot(agentPodInfoVO.getPodName(), agentPodInfoVO.getNamespace())).thenReturn(agentPodInfoVO)

        AgentPodInfoVO agentPodInfoVO2 = new AgentPodInfoVO()
        agentPodInfoVO2.setNamespace(devopsEnvPodDTO2.getNamespace())
        agentPodInfoVO2.setNodeName(devopsEnvPodDTO2.getNodeName())
        agentPodInfoVO2.setPodName(devopsEnvPodDTO2.getName())
        agentPodInfoVO2.setSnapshotTime(new Date())
        agentPodInfoVO2.setCpuUsed("490m")
        agentPodInfoVO2.setMemoryUsed("400MiB")
        PowerMockito.when(agentPodService.queryLatestPodSnapshot(agentPodInfoVO2.getPodName(), agentPodInfoVO2.getNamespace())).thenReturn(agentPodInfoVO2)

        PowerMockito.when(gitlabServiceClient.queryProjectById(anyInt())).thenReturn(new ResponseEntity<GitlabProjectDTO>(HttpStatus.OK))
    }

    def cleanup() {
        if (isToCleanup) {
            DependencyInjectUtil.restoreDefaultDependency(iamService, "baseServiceClient")
            DependencyInjectUtil.restoreDefaultDependency(baseServiceClientOperator, "baseServiceClient")
            DependencyInjectUtil.restoreDefaultDependency(gitlabServiceClientOperator, "gitlabServiceClient")

            appServiceMapper.delete(null)
            devopsClusterMapper.delete(null)
            devopsEnvUserPermissionMapper.delete(null)
            devopsEnvironmentMapper.delete(null)
            appServiceInstanceMapper.delete(null)
            devopsIngressMapper.delete(null)
            devopsIngressPathMapper.delete(null)
            devopsServiceMapper.delete(null)
            devopsServiceInstanceMapper.delete(null)
            devopsProjectMapper.delete(null)
            devopsClusterProPermissionMapper.delete(null)
            devopsEnvCommitMapper.delete(null)
            userAttrMapper.deleteByPrimaryKey(userToAssignPermission.getIamUserId())
        }
    }

    def "Create"() {
        given: "准备"
        isToInit = false
        DevopsEnvironmentVO devopsEnvironmentVO = new DevopsEnvironmentVO()
        devopsEnvironmentVO.setClusterId(1L)
        devopsEnvironmentVO.setCode("testCodeChange")
        devopsEnvironmentVO.setName("testNameChange")

        and: 'mock envUtil'
        GitConfigVO gitConfigDTO = new GitConfigVO()
        gitUtil.getGitConfig(_ as Long) >> gitConfigDTO
        when: '项目下创建环境'
        restTemplate.postForObject(rootUrl, devopsEnvironmentVO, Void.class, projectId)

        then: '返回值'
        devopsEnvironmentMapper.selectAll().size() == 3
    }

    def "实例视图查询环境及其下服务及实例(listEnvTree)"() {
        given: "准备数据"
        def url = rootUrl + "/ins_tree_menu"

        when: "发送请求，期待正常数据返回"
        List<DevopsEnvironmentViewVO> response = JSONArray.parseArray(restTemplate.getForObject(url, String.class, projectId), DevopsEnvironmentViewVO)

        then: "校验结果"
        response != null
        !response.isEmpty()
        response.get(0).getApps().get(0).getInstances() != null
        response.get(0).getApps().get(0).getInstances().size() > 0
        response.get(0).getApps().get(0).getInstances().get(0).getCode() == appServiceInstanceDTO.getCode()

        when: "以不存在环境的项目id发送请求，期待空数据返回"
        response = restTemplate.getForObject(url, List.class, 12321321L)

        then: "校验结果"
        response != null
        response.isEmpty()
    }

    def "资源视图查询项目下环境及其下各种资源的基本信息(listResourceEnvTree)"() {
        given: "准备数据"
        def url = rootUrl + "/resource_tree_menu"

        when: "发送请求，期待正常数据返回"
        List<DevopsResourceEnvOverviewVO> response = JSONArray.parseArray(restTemplate.getForObject(url, String.class, projectId), DevopsResourceEnvOverviewVO.class)

        then: "校验结果"
        response != null
        response.size() == 3
        response.get(0).getInstances() != null
        response.get(0).getServices() != null
        response.get(0).getInstances().size() == 1
        response.get(0).getServices().size() == 0
        response.get(0).getInstances().get(0).getCode() == appServiceInstanceDTO.getCode()

        when: "以不存在环境的项目id发送请求，期待空数据返回"
        response = restTemplate.getForObject(url, List.class, 123L)

        then: "校验结果"
        response != null
        response.isEmpty()
    }

    def "ListByProjectIdAndActive"() {
        given: '准备数据'
        def url = rootUrl + "/list_by_active?active={active}"
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)
        urlParams.put("active", Boolean.TRUE)


        when: '项目下查询环境'
        def envs = restTemplate.getForObject(url, List.class, urlParams)

        then: '返回值'
        envs.size() == 3
    }

    def "ListByProjectIdAndActiveWithGroup"() {
        given: '初始化envList'
        def url = rootUrl + "/list_by_groups?active={active}"
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)
        urlParams.put("active", Boolean.TRUE)

        and: '插入环境组数据'
        DevopsEnvGroupDTO devopsEnvGroupDO = new DevopsEnvGroupDTO()
        devopsEnvGroupDO.setId(1L)
        devopsEnvGroupDO.setProjectId(1L)
        DevopsEnvGroupDTO devopsEnvGroupDO1 = new DevopsEnvGroupDTO()
        devopsEnvGroupDO1.setId(2L)
        devopsEnvGroupDO1.setProjectId(1L)
        devopsEnvGroupMapper.insert(devopsEnvGroupDO)
        devopsEnvGroupMapper.insert(devopsEnvGroupDO1)

        when: '项目下环境流水线查询环境'
        def list = restTemplate.getForObject(url, List.class, urlParams)

        then: '返回值'
        list.size() == 3
    }

    def "EnableOrDisableEnv"() {
        given: 'mock envUtil方法'
        def url = rootUrl + "/{environment_id}/active?active={active}"
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)
        urlParams.put("active", Boolean.FALSE)
        urlParams.put("environment_id", devopsEnvironmentDO1.getId())

        when: '项目下停用环境'
        restTemplate.put(url, Boolean.class, urlParams)

        then: '返回值'
        !devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvironmentDO1.getId()).getActive()


        when: '项目下启用环境'
        urlParams.put("active", Boolean.TRUE)
        restTemplate.put(url, Boolean.class, urlParams)

        then: '返回值'
        devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvironmentDO1.getId()).getActive()
    }

    def "Query"() {
        given: '准备数据'
        def url = rootUrl + "/{environment_id}"
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)
        urlParams.put("environment_id", devopsEnvironmentDO.getId())

        when: '项目下查询单个环境'
        DevopsEnvironmentUpdateVO dto = restTemplate.getForObject(url, DevopsEnvironmentUpdateVO.class, urlParams)

        then: '返回值'
        dto.getCode() == devopsEnvironmentDO.getCode()
    }

    def "实例视图查询单个环境信息(queryEnvInfo)"() {
        given: '准备'
        def url = rootUrl + "/{environment_id}/info"
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)
        urlParams.put("environment_id", devopsEnvironmentDO.getId())

        when: '实例视图查询单个环境信息'
        DevopsEnvironmentInfoVO dto = restTemplate.getForObject(url, DevopsEnvironmentInfoVO.class, urlParams)

        then: '返回值'
        dto.getCode() == devopsEnvironmentDO.getCode()
        dto.getSkipCheckPermission() == devopsEnvironmentDO.getSkipCheckPermission()
    }

    def "查询环境下相关资源的数量(queryEnvResourceCount)"() {
        given: '准备'
        def url = rootUrl + "/{env_id}/resource_count"
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)
        urlParams.put("env_id", devopsEnvironmentDO.getId())

        when: '发送请求'
        DevopsEnvResourceCountVO dto = restTemplate.getForObject(url, DevopsEnvResourceCountVO.class, urlParams)

        then: '返回值'
        dto != null
        dto.getInstanceCount() == 1
    }

    def "按资源用量列出环境下Pod信息（queryEnvPodInfo）"() {
        given: '准备'
        def url = rootUrl + "/{env_id}/pod_ranking?sort={sort}"
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)
        urlParams.put("env_id", devopsEnvironmentDO.getId())
        urlParams.put("sort", "memory")

        when: '按默认（按内存倒序）请求Pod'
        def response = JSONArray.parseArray(restTemplate.getForObject(url, String.class, urlParams), DevopsEnvPodInfoVO.class)

        then: '返回值'
        response != null
        response.size() == 2
        response.get(0).getName() == devopsEnvPodDTO2.getName()
        response.get(1).getName() == devopsEnvPodDTO.getName()

        when: '按CPU倒序请求Pod'
        urlParams.put("sort", "cpu")
        response = JSONArray.parseArray(restTemplate.getForObject(url, String.class, urlParams), DevopsEnvPodInfoVO.class)

        then: '返回值'
        response != null
        response.size() == 2
        response.get(0).getName() == devopsEnvPodDTO.getName()
        response.get(1).getName() == devopsEnvPodDTO2.getName()
    }

    def "Update"() {
        given: '初始化环境更新DTO对象'
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)

        devopsEnvironmentDO = devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvironmentDO.getId())

        def changedName = "testNameChange1222"

        DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO = new DevopsEnvironmentUpdateVO()
        devopsEnvironmentUpdateDTO.setId(devopsEnvironmentDO.getId())
        devopsEnvironmentUpdateDTO.setName(changedName)
        devopsEnvironmentUpdateDTO.setObjectVersionNumber(devopsEnvironmentDO.getObjectVersionNumber())

        when: '项目下更新环境'
        restTemplate.put(rootUrl, devopsEnvironmentUpdateDTO, urlParams)

        then: '返回值'
        devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvironmentDO.getId()).getName() == changedName
    }

    def "CheckCode"() {
        given: '准备'
        def url = rootUrl + "/check_code?cluster_id={cluster_id}&code={code}"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)
        map.put("cluster_id", devopsEnvironmentDO.getClusterId())
        map.put("code", devopsEnvironmentDO.getCode())

        when: '校验已存在的编码'
        def exception = restTemplate.getForEntity(url, ExceptionResponse.class, map)

        then: '返回值'
        exception.statusCode.is2xxSuccessful()

        when: '校验不存在的编码'
        map.put("code", devopsEnvironmentDO.getCode() + "non")
        restTemplate.getForEntity(url, Void.class, map)

        then: '返回值'
        noExceptionThrown()
    }

    def "项目下查询有正在运行实例的环境(ListByProjectId)"() {
        given: '初始化应用实例DO对象'
        def url = rootUrl + "/list_by_instance?app_service_id={app_service_id}"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)
        map.put("app_service_id", appServiceDTO.getId())

        when: '项目下查询有正在运行实例的环境'
        def envs = restTemplate.getForObject(url, List.class, map)

        then: '返回值'
        envs != null
        envs.size() == 1
    }

    def "QueryEnvSyncStatus"() {
        given: '准备'
        def url = rootUrl + "/{env_id}/status"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)
        map.put("env_id", devopsEnvironmentDO.getId())

        when: '查询环境同步状态'
        def envSyncStatusDTO = restTemplate.getForObject(url, EnvSyncStatusVO.class, map)

        then: '返回值'
        envSyncStatusDTO.getAgentSyncCommit() == devopsEnvCommitDTO.getCommitSha()
    }

//    def "分页查询项目下用户权限(page_by_options)"() {
//        given: '初始化param参数'
//        def url = rootUrl + "/page_by_options?page={page}&size={size}&env_id={env_id}"
//        Map<String, Object> map = new HashMap<>()
//        map.put("project_id", projectId)
//        map.put("env_id", devopsEnvironmentDO.getId())
//        map.put("page", 1)
//        map.put("size", 10)
//
//        String params = "{\"searchParam\": {},\"params\": []}"
//
//        when: '分页查询项目下用户权限'
//        def page = restTemplate.postForObject(url, params, PageInfo.class, map)
//
//        then: '返回值'
//        page != null
//        page.getList().size() > 0
//    }

    def "分页查询环境下用户权限(pageEnvUserPermissions)"() {
        given: '准备'

        def url = rootUrl + "/{env_id}/permission/page_by_options?page={page}&size={size}"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)
        map.put("env_id", devopsEnvironmentDO.getId())
        map.put("page", 1)
        map.put("size", 10)

        String params = "{\"searchParam\": {},\"params\": []}"

        when: '分页查询项目下用户权限'
        def page = restTemplate.postForObject(url, params, PageInfo.class, map)

        then: '返回值'
        page != null
        page.getList().size() == 4
    }

    def "列出项目下所有与该环境未分配权限的项目成员(listAllNonRelatedMembers)"() {
        given: '准备'

        def url = rootUrl + "/{env_id}/permission/list_non_related"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)
        map.put("env_id", devopsEnvironmentDO.getId())

        String params = "{\"searchParam\": {},\"params\": []}"

        when: '发送请求'
        def list = JSONArray.parseArray(restTemplate.postForObject(url, params, String.class, map), DevopsEnvUserVO.class)

        then: '返回值'
        list != null
        list.size() == 1
        list.get(0).getIamUserId() == 5L
    }

    def "删除该用户在该环境下的权限(deletePermissionOfUser)"() {
        given: '准备'
        def url = rootUrl + "/{env_id}/permission?user_id={user_id}"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)
        map.put("env_id", devopsEnvironmentDO.getId())
        map.put("user_id", devopsEnvUserPermissionDO.getIamUserId())

        when: '发送请求'
        restTemplate.delete(url, map)

        then: '校验结果'
        noExceptionThrown()
        devopsEnvUserPermissionMapper.selectOne(devopsEnvUserPermissionDO) == null
    }

//    def "ListAllUserPermission"() {
//        given: '准备'
//        def url = rootUrl + "/{env_id}/list_all"
//        Map<String, Object> map = new HashMap<>()
//        map.put("project_id", projectId)
//        map.put("env_id", devopsEnvironmentDO.getId())
//        map.put("user_id", devopsEnvUserPermissionDO.getIamUserId())
//
//        when: '发送请求'
//        def list = restTemplate.getForObject(url, List.class, map)
//
//        then: '校验结果'
//        list != null
//        list.size() == 2
//        list.get(0)["loginName"] == "test1"
//        list.get(1)["loginName"] == "test2"
//    }

    def "UpdateEnvUserPermission"() {
        given: "准备"
        def url = rootUrl + "/{env_id}/permission"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)
        map.put("env_id", devopsEnvironmentDO.getId())

        DevopsEnvPermissionUpdateVO devopsEnvPermissionUpdateVO = new DevopsEnvPermissionUpdateVO()
        devopsEnvPermissionUpdateVO.setSkipCheckPermission(devopsEnvironmentDO.getSkipCheckPermission())
        devopsEnvPermissionUpdateVO.setEnvId(devopsEnvironmentDO.getId())
        devopsEnvPermissionUpdateVO.setUserIds([userToAssignPermission.getIamUserId()])

        DevopsEnvUserPermissionDTO search = new DevopsEnvUserPermissionDTO()
        search.setIamUserId(userToAssignPermission.getIamUserId())
        search.setEnvId(devopsEnvironmentDO.getId())

        when: '获取环境下所有用户权限'
        restTemplate.postForObject(url, devopsEnvPermissionUpdateVO, Boolean.class, map)

        then: '校验结果'
        devopsEnvUserPermissionMapper.selectOne(search) != null
    }

    def "删掉已停用的环境"() {
        given: '准备'
        def url = rootUrl + "/{env_id}"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)
        map.put("env_id", devopsEnvironmentDO1.getId())

        when: '删除未停用的环境(预期失败)'
        restTemplate.delete(url, map)
        devopsEnvironmentDO1 = devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvironmentDO1.getId())

        then: '预期未删除'
        devopsEnvironmentDO1 != null

        when: '将其设置为停用后，发送删除请求，预期删除成功'
        def update = new DevopsEnvironmentDTO()
        update.setId(devopsEnvironmentDO1.getId())
        update.setActive(Boolean.FALSE)
        update.setObjectVersionNumber(devopsEnvironmentDO1.getObjectVersionNumber())
        devopsEnvironmentMapper.updateByPrimaryKeySelective(update)
        restTemplate.delete(url, map)

        then: '预期删除'
        devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvironmentDO1.getId()) == null
    }

    def "ListDevopsClusters"() {
        given: '准备'
        def url = rootUrl + "/list_clusters"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)

        when: '项目下查询集群信息'
        def list = restTemplate.getForObject(url, List.class, map)

        then: '校验返回值'
        list.get(0)["name"] == "testCluster"
    }

    def "根据环境编码查询环境"() {
        given: "准备"
        def url = rootUrl + "/query_by_code?code={code}"
        Map<String, Object> map = new HashMap<>()
        map.put("project_id", projectId)
        map.put("code", devopsEnvironmentDO.getCode())
        isToCleanup = true

        when: "查询"
        def response = restTemplate.getForObject(url, DevopsEnviromentRepVO.class, map)

        then: "校验"
        response != null
        response.getId() == devopsEnvironmentDO.getId()
        response.getCode() == devopsEnvironmentDO.getCode()
    }
}