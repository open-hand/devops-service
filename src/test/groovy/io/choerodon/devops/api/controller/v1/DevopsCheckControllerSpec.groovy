package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.dto.StartInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.api.vo.iam.RoleSearchVO
import io.choerodon.devops.api.vo.iam.UserVO
import io.choerodon.devops.api.vo.iam.UserWithRoleVO
import io.choerodon.devops.api.vo.iam.UserWithRoleVO
import io.choerodon.devops.app.service.DevopsCheckLogService
<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.gitlab.CommitE
import io.choerodon.devops.domain.application.valueobject.ProjectHook
import io.choerodon.devops.infra.dataobject.gitlab.PipelineDO
=======
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.infra.dto.gitlab.ProjectHookDTO
>>>>>>> [IMP]修改后端结构
import io.choerodon.devops.infra.common.util.TypeUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.common.util.enums.PipelineStatus
<<<<<<< HEAD
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.dataobject.gitlab.*
=======
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO
import io.choerodon.devops.infra.dataobject.UserAttrDTO
import io.choerodon.devops.infra.dataobject.gitlab.CommitDTO

import io.choerodon.devops.infra.dataobject.gitlab.MemberDTO
import io.choerodon.devops.infra.dataobject.gitlab.PipelineDO
>>>>>>> [IMP] 修改environment Controller
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.*
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

import java.util.concurrent.TimeUnit

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.when

/**
 *
 * @author zmf
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsCheckController)
@Stepwise
class DevopsCheckControllerSpec extends Specification {
    def BASE_URL = "/v1/upgrade?version={version}"
    def SLEEP_TIME_SECOND = 3

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private DevopsCheckLogService devopsCheckLogService
    @Autowired
    private GitlabRepository gitlabRepository
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository
    @Autowired
    private DevopsGitRepository devopsGitRepository
    @Autowired
    private GitlabUserRepository gitlabUserRepository
    @Autowired
    private IamRepository iamRepository
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    private GitlabServiceClient mockGitlabServiceClient = Mockito.mock(GitlabServiceClient)
    private IamServiceClient mockIamServiceClient = Mockito.mock(IamServiceClient)
    private SagaClient mockSagaClient = Mockito.mock(SagaClient)

    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private UserAttrMapper userAttrMapper
    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper
    @Autowired
    private DevopsBranchMapper devopsBranchMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper
    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsEnvResourceMapper devopsEnvResourceMapper
    @Autowired
    private DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper

    @Shared
    private DevopsEnvResourceDetailDO devopsEnvResourceDetailDO = new DevopsEnvResourceDetailDO()
    @Shared
    private DevopsEnvResourceDO devopsEnvResourceDO = new DevopsEnvResourceDO()
    @Shared
    private DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO()
    @Shared
    private ApplicationDTO applicationDO = new ApplicationDTO()
    @Shared
    private DevopsGitlabCommitDO devopsGitlabCommitDO = new DevopsGitlabCommitDO()
    @Shared
    private UserAttrDTO userAttrDO = new UserAttrDTO()
    @Shared
    private UserAttrDTO userAttrDO2 = new UserAttrDTO()
    @Shared
    private DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
    @Shared
    private DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO()
    @Shared
    private DevopsProjectDTO newDevopsProjectDO = new DevopsProjectDTO()
    @Shared
    private DevopsBranchDO branchDO = new DevopsBranchDO()
    @Shared
    private DevopsGitlabPipelineDO devopsGitlabPipelineDO = new DevopsGitlabPipelineDO()
    @Shared
    private List<DevopsProjectDTO> previousDevopsProjectDOList = new ArrayList<>()
    @Shared
    private ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false


    def setup() {
        if (!isToInit) {
            return
        }

        DependencyInjectUtil.setAttribute(devopsGitRepository, "gitlabServiceClient", mockGitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", mockGitlabServiceClient)
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", mockIamServiceClient)
        DependencyInjectUtil.setAttribute(devopsCheckLogService, "sagaClient", mockSagaClient)
        DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", mockGitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClient", mockGitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabUserRepository, "gitlabServiceClient", mockGitlabServiceClient)
        devopsProjectMapper.selectAll().stream().peek { previousDevopsProjectDOList.add(it) }.forEach {
            devopsProjectMapper.delete(it)
        }

        // 准备升级0.8的数据
        applicationDO.setToken("feadb7a5-3bce-4944-9c0b-ffabd8da6060")
        applicationDO.setGitlabProjectId(1)
        applicationDO.setProjectId(1L)
        applicationDO.setActive(true)
        applicationDO.setGitlabProjectId(1)
        applicationDO.setCode("test")
        applicationDO.setName("test")
        applicationMapper.insert(applicationDO)

        userAttrDO.setIamUserId(100L)
        userAttrDO.setGitlabUserName("gitlabUsername")
        userAttrDO.setGitlabUserId(100L)
        userAttrMapper.insert(userAttrDO)
        DevopsGitlabPipelineDO
        devopsGitlabCommitDO.setAppId(applicationDO.getId())
        devopsGitlabCommitDO.setUserId(100L)
        devopsGitlabCommitDO.setCommitSha("test")
        devopsGitlabCommitDO.setCommitContent("test")
        devopsGitlabCommitDO.setRef("test")
        devopsGitlabCommitDO.setCommitDate(new Date())
        devopsGitlabCommitMapper.insert(devopsGitlabCommitDO)

        branchDO.setBranchName("feature-c7ncd-122")
        branchDO.setAppId(applicationDO.getId())
        devopsBranchMapper.insert(branchDO)


        ResponseEntity<List<BranchDO>> branchRes = new ResponseEntity<>(Arrays.asList(createMockBranchDO()), HttpStatus.OK)
        when(mockGitlabServiceClient.listBranch(anyInt(), anyInt())).thenReturn(branchRes)

        ProjectHookDTO hook = new ProjectHookDTO()
        hook.setId(2)
        ResponseEntity<ProjectHookDTO> res = new ResponseEntity<>(hook, HttpStatus.OK)
        when(mockGitlabServiceClient.createProjectHook(anyInt(), anyInt(), any(ProjectHookDTO))).thenReturn(res)

        // 准备升级0.9的数据
        devopsEnvironmentDO.setName("fakeEnv")
        devopsEnvironmentDO.setCode("fakeEnv")
        devopsEnvironmentDO.setProjectId(1231L)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)

        devopsProjectDO.setDevopsAppGroupId(104L)
        devopsProjectDO.setIamProjectId(122L)
        devopsProjectDO.setDevopsEnvGroupId(231244L)
        newDevopsProjectDO.setDevopsAppGroupId(105L)
        newDevopsProjectDO.setIamProjectId(123L)
        devopsProjectMapper.insert(devopsProjectDO)
        devopsProjectMapper.insert(newDevopsProjectDO)

        when(mockIamServiceClient.queryIamProject(anyLong())).thenReturn(new ResponseEntity<>(createFakeProjectDO(), HttpStatus.OK))
        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(23L)
        organizationDO.setCode("fakeOrg")
        organizationDO.setName("fakeOrg")
        when(mockIamServiceClient.queryOrganizationById(anyLong())).thenReturn(new ResponseEntity<>(organizationDO, HttpStatus.OK))
        GroupDO groupDO = new GroupDO()
        groupDO.setId(1242)
        when(mockGitlabServiceClient.createGroup(any(GroupDO), anyInt())).thenReturn(new ResponseEntity<>(groupDO, HttpStatus.OK))

        GitlabProjectDTO gitlabProjectDO = new GitlabProjectDTO()
        gitlabProjectDO.setId(12523)
        when(mockGitlabServiceClient.createProject(anyInt(), anyString(), anyInt(), anyBoolean())).thenReturn(new ResponseEntity<>(gitlabProjectDO, HttpStatus.OK))
        when(mockGitlabServiceClient.listDeploykey(anyInt(), anyInt())).thenReturn(new ResponseEntity<>(new ArrayList(), HttpStatus.OK))
        when(mockGitlabServiceClient.getFile(anyInt(), anyString(), anyString())).thenReturn(new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK))
        when(mockGitlabServiceClient.queryProjectByName(anyInt(), anyString(), anyString())).thenReturn(new ResponseEntity<>(new GitlabProjectDTO(), HttpStatus.OK))
        when(mockGitlabServiceClient.listProjectHook(anyInt(), anyInt())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK))

        RoleVO roleDTO = new RoleVO()
        roleDTO.setId(234L)
//        PageInfo pageInfo = new PageInfo(0, 10, true)
        List<RoleVO> roleDTOS = Arrays.asList(roleDTO)
        PageInfo<RoleVO> page = new PageInfo(roleDTOS)
        when(mockIamServiceClient.queryRoleIdByCode(any(RoleSearchVO))).thenReturn(new ResponseEntity<>(page, HttpStatus.OK))
        List<UserVO> userDTOS = new ArrayList<>()
        PageInfo<RoleVO> page1 = new PageInfo(userDTOS)
        when(mockIamServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), anyLong(), anyLong(), anyBoolean(), any(RoleAssignmentSearchVO))).thenReturn(new ResponseEntity<>(page1, HttpStatus.OK))

        // 准备升级到0.10 的数据
        when(mockGitlabServiceClient.updateProjectHook(anyInt(), anyInt(), anyInt())).thenReturn(new ResponseEntity<>(new ProjectHookDTO(), HttpStatus.OK))
        when(mockGitlabServiceClient.listCommits(anyInt(), eq(1), anyInt(), anyInt())).thenReturn(new ResponseEntity<>(createVersion10MockCommits(), HttpStatus.OK))
        when(mockGitlabServiceClient.listCommits(anyInt(), eq(2), anyInt(), anyInt())).thenReturn(new ResponseEntity<>(new ArrayList(), HttpStatus.OK))
        IamUserDTO userDO = new IamUserDTO()
        userDO.setId(234L)
        List<IamUserDTO> users = new ArrayList<>()
        users.add(userDO)
//        PageInfo userPageInfo = new PageInfo(1, 10, true)
        PageInfo<IamUserDTO> userPage = new PageInfo(users)
        when(mockIamServiceClient.listUsersByEmail(anyLong(), anyInt(), anyInt(), anyString())).thenReturn(new ResponseEntity<>(userPage, HttpStatus.OK))


        when(mockGitlabServiceClient.listPipeline(anyInt(), anyInt())).thenReturn(new ResponseEntity<>(createMockPipelinesForVersion10SyncPinelines(), HttpStatus.OK))

        PipelineDO po2 = new PipelineDO()
        UserDO u2 = new UserDO()
        u2.setId(TypeUtil.objToInteger(userAttrDO.getGitlabUserId()))
        po2.setUser(u2)
        po2.setId(10000)
        po2.setStatus(PipelineStatus.RUNNING)
        po2.setCreatedAt("2018-10-13 15:33:12")
        po2.setSha("582b27c68b9fe0acbce77b873eb11c66b97409af")
        po2.setRef("feature-C7NCD-1778")
        when(mockGitlabServiceClient.queryPipeline(anyInt(), eq(10000), anyInt())).thenReturn(new ResponseEntity<>(po2, HttpStatus.OK))

        PipelineDO po3 = new PipelineDO()
        UserDO u3 = new UserDO()
        u3.setId(TypeUtil.objToInteger(userAttrDO.getGitlabUserId()))
        po3.setUser(u3)
        po3.setId(10001)
        po3.setStatus(PipelineStatus.SUCCESS)
        po3.setCreatedAt("2018-10-13 15:33:18")
        po3.setSha("582b27c68b9fe0acbce77b873eb11c66b974093a")
        po3.setRef("feature-C7NCD-1779")
        when(mockGitlabServiceClient.queryPipeline(anyInt(), eq(10001), anyInt())).thenReturn(new ResponseEntity<>(po3, HttpStatus.OK))

        // 准备升级到0.10.4的数据
        devopsGitlabPipelineDO.setAppId(applicationDO.getId())
        devopsGitlabPipelineDO.setCommitId(devopsGitlabCommitDO.getId())
        devopsGitlabPipelineDO.setPipelineId(324L)
        devopsGitlabPipelineMapper.insert(devopsGitlabPipelineDO)

        when(mockGitlabServiceClient.queryPipeline(anyInt(), eq((int) TypeUtil.objToInteger(devopsGitlabPipelineDO.getPipelineId())), anyInt())).thenReturn(new ResponseEntity<>(createMockPipelineDO(), HttpStatus.OK))
        when(mockGitlabServiceClient.listJobs(anyInt(), anyInt(), anyInt())).thenReturn(new ResponseEntity<>(createMockJobDOs(), HttpStatus.OK))
        when(mockGitlabServiceClient.listCommitStatus(anyInt(), anyString(), anyInt())).thenReturn(new ResponseEntity<>(createMockCommitStatusDOs(), HttpStatus.OK))

        // 准备升级到0.11.0的数据
        UserWithRoleVO userWithRoleDTO = new UserWithRoleVO()
        userWithRoleDTO.setRoles(Collections.emptyList())
        userWithRoleDTO.setLoginName("userWithRoleDTO")
        userWithRoleDTO.setId(userAttrDO.getIamUserId())
        List<UserWithRoleVO> userWithRoleDTOList = Arrays.asList(userWithRoleDTO)
        PageInfo userWithRolePageInfo = new PageInfo(userWithRoleDTOList)
//        PageInfo<UserWithRoleVO> userWithRoleDTOPage = new PageInfo<>(userWithRoleDTOList, userWithRolePageInfo, 1)
        when(mockIamServiceClient.queryUserByProjectId(eq(devopsProjectDO.getIamProjectId()), anyInt(), anyInt(), anyBoolean(), any(RoleAssignmentSearchVO))).thenReturn(new ResponseEntity<>(userWithRolePageInfo, HttpStatus.OK))

        MemberDTO memberDO = new MemberDTO()
        memberDO.setId(TypeUtil.objToInteger(userAttrDO.getGitlabUserId()))
        memberDO.setAccessLevel(AccessLevel.MASTER)
        when(mockGitlabServiceClient.queryGroupMember(eq(TypeUtil.objToInteger(devopsProjectDO.getDevopsEnvGroupId())), eq(TypeUtil.objToInteger(userAttrDO.getGitlabUserId())))).thenReturn(new ResponseEntity<>(memberDO, HttpStatus.OK))
        when(mockGitlabServiceClient.queryGroupMember(eq(TypeUtil.objToInteger(devopsProjectDO.getDevopsAppGroupId())), eq(TypeUtil.objToInteger(userAttrDO.getGitlabUserId())))).thenReturn(new ResponseEntity<>(memberDO, HttpStatus.OK))

        // 准备升级到0.12.0的数据
        userAttrDO2.setIamUserId(101L)
        userAttrDO2.setGitlabUserName("gitlabUsername")
        userAttrDO2.setGitlabUserId(101L)
        userAttrMapper.insert(userAttrDO2)

        IamUserDTO userRet1 = new IamUserDTO()
        userRet1.setId(userAttrDO.getIamUserId())
        userRet1.setLoginName("admin")

        IamUserDTO userRet2 = new IamUserDTO()
        userRet2.setId(userAttrDO2.getIamUserId())
        userRet2.setLoginName("12241&*(@^1`")

        Long[] ids1 = new Long[1]
        ids1[0] = userAttrDO.getIamUserId()

        Long[] ids2 = new Long[1]
        ids2[0] = userAttrDO2.getIamUserId()

        when(mockIamServiceClient.listUsersByIds(ids1)).thenReturn(new ResponseEntity<>(Arrays.asList(userRet1), HttpStatus.OK))
        when(mockIamServiceClient.listUsersByIds(ids2)).thenReturn(new ResponseEntity<>(Arrays.asList(userRet2), HttpStatus.OK))

        UserDO userDOForGitlabUsername = new UserDO()
        userDOForGitlabUsername.setUsername("validUsername")
        when(mockGitlabServiceClient.queryUserById(eq(TypeUtil.objToInteger(userAttrDO2.getGitlabUserId())))).thenReturn(new ResponseEntity<>(userDOForGitlabUsername, HttpStatus.OK))

        //  准备升级到0.14.0的数据
        devopsEnvPodDO.setId(100L)
        devopsEnvPodDO.setName("fssc-db1f0-ffddcfcdc-kbj84")
        devopsEnvPodDO.setAppInstanceId(1022L)
        devopsEnvPodMapper.insert(devopsEnvPodDO)

        devopsEnvResourceDetailDO.setId(124L)
        devopsEnvResourceDetailDO.setMessage("{\"metadata\":{\"name\":\"fssc-db1f0-ffddcfcdc-kbj84\",\"generateName\":\"fssc-db1f0-ffddcfcdc-\",\"namespace\":\"cmcc-fssc-cmcc\",\"selfLink\":\"/api/v1/namespaces/cmcc-fssc-cmcc/pods/fssc-db1f0-ffddcfcdc-kbj84\",\"uid\":\"8b11c181-5f4a-11e8-b11c-00163e04f544\",\"resourceVersion\":\"16049404\",\"creationTimestamp\":\"2018-05-24T12:04:00Z\",\"labels\":{\"choerodon.io/release\":\"fssc-db1f0\",\"pod-template-hash\":\"998879787\"},\"annotation\":{\"kubernetes.io/created-by\":\"{\\\"kind\\\":\\\"SerializedReference\\\",\\\"apiVersion\\\":\\\"v1\\\",\\\"reference\\\":{\\\"kind\\\":\\\"ReplicaSet\\\",\\\"namespace\\\":\\\"cmcc-fssc-cmcc\\\",\\\"name\\\":\\\"fssc-db1f0-ffddcfcdc\\\",\\\"uid\\\":\\\"8b0e4cbe-5f4a-11e8-b11c-00163e04f544\\\",\\\"apiVersion\\\":\\\"extensions\\\",\\\"resourceVersion\\\":\\\"16048306\\\"}}\\n\"},\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"ReplicaSet\",\"name\":\"fssc-db1f0-ffddcfcdc\",\"uid\":\"8b0e4cbe-5f4a-11e8-b11c-00163e04f544\",\"controller\":true,\"blockOwnerDeletion\":true}]},\"spec\":{\"volumes\":[{\"name\":\"default-token-wmrfv\",\"secret\":{\"secretName\":\"default-token-wmrfv\",\"defaultMode\":420}}],\"containers\":[{\"name\":\"fssc-db1f0\",\"image\":\"registry.choerodon.com.cn/cmcc-fssc/fssc:0.1.0-dev.20180524192305\",\"ports\":[{\"name\":\"http\",\"containerPort\":8080,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"ORACLE_HOST\",\"value\":\"116.228.77.183\"},{\"name\":\"ORACLE_PASS\",\"value\":\"hap_dev\"},{\"name\":\"ORACLE_PORT\",\"value\":\"30171\"},{\"name\":\"ORACLE_SID\",\"value\":\"TEST\"},{\"name\":\"ORACLE_USER\",\"value\":\"hap_dev\"},{\"name\":\"RABBITMQ_HOST\",\"value\":\"baozhang-rabbitmq.baozhang.svc\"},{\"name\":\"RABBITMQ_PASSWORD\",\"value\":\"guest\"},{\"name\":\"RABBITMQ_PORT\",\"value\":\"5672\"},{\"name\":\"RABBITMQ_USERNAME\",\"value\":\"guest\"},{\"name\":\"REDIS_DB\",\"value\":\"8\"},{\"name\":\"REDIS_IP\",\"value\":\"baozhang-redis.baozhang.svc\"}],\"resources\":{\"limits\":{\"memory\":\"2560Mi\"},\"requests\":{\"memory\":\"1536Mi\"}},\"volumeMounts\":[{\"name\":\"default-token-wmrfv\",\"readOnly\":true,\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\"}],\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"serviceAccountName\":\"default\",\"serviceAccount\":\"default\",\"nodeName\":\"cmccnode3\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"},\"status\":{\"phase\":\"Running\",\"conditions\":[{\"type\":\"Initialized\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-05-24T12:04:00Z\"},{\"type\":\"Ready\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-05-24T12:14:59Z\"},{\"type\":\"PodScheduled\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-05-24T12:04:00Z\"}],\"hostIP\":\"172.20.117.95\",\"podIP\":\"192.168.17.49\",\"startTime\":\"2018-05-24T12:04:00Z\",\"containerStatuses\":[{\"name\":\"fssc-db1f0\",\"state\":{\"running\":{\"startedAt\":\"2018-05-24T12:14:59Z\"}},\"lastState\":{},\"ready\":true,\"restartCount\":1,\"image\":\"registry.choerodon.com.cn/cmcc-fssc/fssc:0.1.0-dev.20180524192305\",\"imageID\":\"docker-pullable://registry.choerodon.com.cn/cmcc-fssc/fssc@sha256:f94a9149b0d21f5eb8ec2968b3336081b5c7daa597deb2e0bd2cd87a42b9905a\",\"containerID\":\"docker://db7e30d1bccbaf51d40783aafe4b547a9a11e1be5ac847404d4abf785897e70e\"}],\"qosClass\":\"Burstable\"}}")
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO)

        devopsEnvResourceDO.setId(143L)
        devopsEnvResourceDO.setName(devopsEnvPodDO.getName())
        devopsEnvResourceDO.setAppInstanceId(devopsEnvPodDO.getAppInstanceId())
        devopsEnvResourceDO.setKind("Pod")
        devopsEnvResourceDO.setResourceDetailId(devopsEnvResourceDetailDO.getId())
        devopsEnvResourceMapper.insert(devopsEnvResourceDO)

        // 准备升级到0.15.0的数据
        Mockito.doReturn(new SagaInstanceDTO()).when(mockSagaClient).startSaga(anyString(), any(StartInstanceDTO))
        applicationVersionDO.setAppId(1L)
        applicationVersionDO.setRepository("test")
        applicationVersionDO.setVersion("test")
        applicationVersionDO.setImage("test")


        List<MemberDTO> memberDOList = new ArrayList<>()
        memberDOList.add(memberDO)
        ResponseEntity<List<MemberDTO>> listResponseEntity = new ResponseEntity<>(memberDOList, HttpStatus.OK)
        Mockito.doReturn(listResponseEntity).when(mockGitlabServiceClient).listMemberByProject(any())
        Mockito.doReturn(null).when(mockGitlabServiceClient).updateProjectMember(any(), any())
    }

    def cleanup() {
        if (!isToClean) {
            return
        }

        DependencyInjectUtil.restoreDefaultDependency(devopsGitRepository, "gitlabServiceClient")
        DependencyInjectUtil.restoreDefaultDependency(gitlabRepository, "gitlabServiceClient")
        DependencyInjectUtil.restoreDefaultDependency(iamRepository, "iamServiceClient")
        DependencyInjectUtil.restoreDefaultDependency(devopsCheckLogService, "sagaClient")
        DependencyInjectUtil.restoreDefaultDependency(gitlabProjectRepository, "gitlabServiceClient")
        DependencyInjectUtil.restoreDefaultDependency(gitlabGroupMemberRepository, "gitlabServiceClient")
        DependencyInjectUtil.restoreDefaultDependency(gitlabUserRepository, "gitlabServiceClient")

        devopsBranchMapper.selectAll().forEach { devopsBranchMapper.delete(it) }
        applicationMapper.delete(applicationDO)
        userAttrMapper.delete(userAttrDO)
        userAttrMapper.delete(userAttrDO2)
        devopsGitlabCommitMapper.selectAll().forEach { devopsGitlabCommitMapper.delete(it) }
        devopsProjectMapper.delete(devopsProjectDO)
        devopsProjectMapper.delete(newDevopsProjectDO)
        devopsEnvironmentMapper.delete(devopsEnvironmentDO)
        devopsCheckLogMapper.selectAll().forEach { devopsCheckLogMapper.delete(it) }
        devopsGitlabPipelineMapper.selectAll().forEach { devopsGitlabPipelineMapper.delete(it) }
        previousDevopsProjectDOList.forEach { devopsProjectMapper.insert(it) }
        devopsEnvPodMapper.selectAll().forEach { devopsEnvPodMapper.delete(it) }
        devopsEnvResourceMapper.selectAll().forEach { devopsEnvResourceMapper.delete(it) }
        devopsEnvResourceDetailMapper.selectAll().forEach { devopsEnvResourceDetailMapper.delete(it) }
    }

    // 平滑升级到0.8版本
    def "upgrade 0.8"() {
        given: "准备搜索条件"
        isToInit = false
        def searchCondition = new DevopsBranchDO()
        searchCondition.setAppId(applicationDO.getId())
        searchCondition.setBranchName("feature-c7ncd-123")

        when: "平滑升级到0.8"
        restTemplate.getForEntity(BASE_URL, Object, "0.8")
        // 睡眠一定时间等待异步升级任务完成执行
        TimeUnit.SECONDS.sleep(SLEEP_TIME_SECOND)

        then: "校验结果"
        applicationMapper.selectByPrimaryKey(applicationDO.getId()).getHookId() == 2L
        devopsBranchMapper.selectOne(searchCondition) != null
    }

    // 平滑升级到0.9版本
    def "upgrade 0.9"() {
        when: "平滑升级到0.9"
        restTemplate.getForEntity(BASE_URL, Object, "0.9")
        // 睡眠一定时间等待异步升级任务完成执行
        TimeUnit.SECONDS.sleep(SLEEP_TIME_SECOND)

        then: "校验结果"
        devopsEnvironmentMapper.selectOne(devopsEnvironmentDO).getGitlabEnvProjectId() != null
    }

    def "upgrade 0.10.0"() {
        given: "准备校验条件"
        DevopsGitlabCommitDO searchCondition = new DevopsGitlabCommitDO()
        searchCondition.setCommitContent("mock version10 Mock commits")
        DevopsGitlabPipelineDO condition = new DevopsGitlabPipelineDO()
        condition.setPipelineId(10000L)

        when: "升级到0.10.0"
        restTemplate.getForEntity(BASE_URL, Object, "0.10.0")
        // 睡眠一定时间等待异步升级任务完成执行
        TimeUnit.SECONDS.sleep(SLEEP_TIME_SECOND)

        then: "校验结果"
        devopsGitlabCommitMapper.select(searchCondition) != null
        devopsGitlabCommitMapper.select(searchCondition).size() == 1
    }

    // 升级到0.10.4
    def "upgrade 0.10.4"() {
        when: "升级到0.10.4"
        restTemplate.getForEntity(BASE_URL, Object, "0.10.4")
        // 睡眠一定时间等待异步升级任务完成执行
        TimeUnit.SECONDS.sleep(SLEEP_TIME_SECOND)

        then: "校验结果"
        devopsGitlabPipelineMapper.selectByPrimaryKey(devopsGitlabPipelineDO.getId()).getStatus() != null
        devopsGitlabPipelineMapper.selectByPrimaryKey(devopsGitlabPipelineDO.getId()).getStage() != null
    }

    // 升级到0.11.0
    def "upgrade 0.11.0"() {
        when: "升级到0.11.0"
        restTemplate.getForEntity(BASE_URL, Object, "0.11.0")
        // 睡眠一定时间等待异步升级任务完成执行
        TimeUnit.SECONDS.sleep(SLEEP_TIME_SECOND)

        then: "校验结果"
        noExceptionThrown()
    }

    // 升级到0.12.0
    def "upgrade 0.12.0"() {
        when: "升级到0.12.0"
        restTemplate.getForEntity(BASE_URL, Object, "0.12.0")
        // 睡眠一定时间等待异步升级任务完成执行
        TimeUnit.SECONDS.sleep(SLEEP_TIME_SECOND)

        then: "校验结果"
        userAttrMapper.selectByPrimaryKey(userAttrDO.getIamUserId()).getGitlabUserName() == "root"
        userAttrMapper.selectByPrimaryKey(userAttrDO2.getIamUserId()).getGitlabUserName() == "validUsername"
    }

    def "upgrade 0.14.0"() {
        given: "准备校验条件"
        def searchCondition = new DevopsEnvPodDO()
        searchCondition.setName("fssc-db1f0-ffddcfcdc-kbj84")

        when: "升级到0.14.0"
        restTemplate.getForEntity(BASE_URL, Object, "0.14.0")
        // 睡眠一定时间等待异步升级任务完成执行
        TimeUnit.SECONDS.sleep(SLEEP_TIME_SECOND)
        def result = devopsEnvPodMapper.selectOne(searchCondition)

        then: "校验结果"
        result != null
        result.getNodeName() == "cmccnode3"
        result.getRestartCount() == 1
    }


    def "upgrade 0.15.0"() {
        given:
        applicationVersionMapper.insert(applicationVersionDO)

        when: "升级到0.15.0"
        restTemplate.getForEntity(BASE_URL, Object, "0.15.0")
        // 睡眠一定时间等待异步升级任务完成执行
        TimeUnit.SECONDS.sleep(SLEEP_TIME_SECOND)

        then: "校验结果"
        noExceptionThrown()
    }

    def "clean data"() {
        given: "清除数据"
        isToClean = true
    }

    // create mock pipeline for version 10 sync pipelines
    private List<PipelineDO> createMockPipelinesForVersion10SyncPinelines() {
        List<PipelineDO> pipelineDOList = new ArrayList<>()
        PipelineDO pipelineDO1 = new PipelineDO()
        pipelineDO1.setId(10000)
        PipelineDO pipelineDO2 = new PipelineDO()
        pipelineDO2.setId(10001)

        pipelineDOList.add(pipelineDO1)
        pipelineDOList.add(pipelineDO2)
        return pipelineDOList
    }

    private List<CommitDTO> createVersion10MockCommits() {
        List<CommitDTO> commitDOList = new ArrayList<>()
        CommitDTO commitDO1 = new CommitDTO()
        commitDO1.setId("e68aa4bff794e35b3e8800237e2ecb6484dd1cb9")
        commitDO1.setMessage("mock version10 Mock commits")
        commitDO1.setAuthorName("root")
        commitDO1.setTimestamp(new Date())
        commitDO1.setCommitterEmail("root@gmail.com")

        CommitDTO commitDO2 = new CommitDTO()
        commitDO2.setId("a413150a63c7c51eebf71ca155a93ffe4f38d26a")
        commitDO2.setMessage("mock version10 Mock commits")
        commitDO2.setAuthorName("rsad")
        commitDO2.setTimestamp(new Date())
        commitDO2.setCommitterEmail("rsad@gmail.com")

        commitDOList.add(commitDO1)
        commitDOList.add(commitDO2)

        return commitDOList
    }

    private ProjectDO createFakeProjectDO() {
        ProjectDO projectDO = new ProjectDO()
        projectDO.setId(devopsProjectDO.getIamProjectId())
        projectDO.setName("fakeProject")
        projectDO.setCode("fakeProject")
        projectDO.setOrganizationId(23L)
        return projectDO
    }

    // create fake branchDO
    private static BranchDO createMockBranchDO() {
        io.choerodon.devops.api.vo.iam.entity.gitlab.CommitDTO commitE = new io.choerodon.devops.api.vo.iam.entity.gitlab.CommitDTO()
        commitE.setId("5ffeae40a2440a2be046d58514e0a7c7ef7d7362")
        commitE.setAuthorName("gitlabUsername")
        commitE.setAuthorEmail("zzz@gmail.com")
        commitE.setCommittedDate(new Date())
        commitE.setMessage("[ADD] add unit test")

        BranchDO branchDO = new BranchDO()
        branchDO.setName("feature-c7ncd-123")
        branchDO.setMerged(Boolean.FALSE)
        branchDO.setCommit(commitE)
        branchDO.setProtected(Boolean.FALSE)
        branchDO.setDevelopersCanMerge(Boolean.TRUE)
        branchDO.setDevelopersCanPush(Boolean.TRUE)
        return branchDO
    }

    private PipelineDO createMockPipelineDO() {
        PipelineDO pipelineDO = new PipelineDO()
        pipelineDO.setStatus(PipelineStatus.RUNNING)
        return pipelineDO
    }

    private List<JobDO> createMockJobDOs() {
        List<JobDO> jobDOList = new ArrayList<>()
        JobDO jobDO = new JobDO()
        jobDO.setId(235)
        jobDOList.add(jobDO)
        return jobDOList
    }

    private List<CommitStatuseDO> createMockCommitStatusDOs() {
        List<CommitStatuseDO> commitStatuseDOList = new ArrayList<>()
        CommitStatuseDO commitStatuseDO1 = new CommitStatuseDO()
        commitStatuseDO1.setId(235)
        commitStatuseDO1.setName("commit1")
        commitStatuseDO1.setDescription("useful commit")

        CommitStatuseDO commitStatuseDO2 = new CommitStatuseDO()
        commitStatuseDO2.setId(2351)
        commitStatuseDO2.setName("sonarqube")
        commitStatuseDO2.setDescription("useful commit")

        commitStatuseDOList.add(commitStatuseDO1)
        commitStatuseDOList.add(commitStatuseDO2)

        return commitStatuseDOList
    }
}
