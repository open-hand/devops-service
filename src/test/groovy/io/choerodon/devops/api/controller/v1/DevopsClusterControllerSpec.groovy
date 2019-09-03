package io.choerodon.devops.api.controller.v1

import com.alibaba.fastjson.JSONObject
import com.github.pagehelper.PageInfo
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.*
import io.choerodon.devops.app.service.impl.ClusterNodeInfoServiceImpl
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.*
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.StringRedisTemplate
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
 * Date: 2018/11/13
 * Time: 14:03
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsClusterController)
@Stepwise
class DevopsClusterControllerSpec extends Specification {
    private static final String MAPPING = "/v1/projects/{project_id}/clusters"

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator
    @Autowired
    private DevopsClusterMapper devopsClusterMapper
    @Autowired
    private DevopsClusterProPermissionMapper devopsClusterProPermissionMapper

    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private AppServiceInstanceMapper applicationInstanceMapper

    @Autowired
    private ClusterNodeInfoServiceImpl clusterNodeInfoService

    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler clusterConnectionHandler

    BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient.class)
    StringRedisTemplate mockStringRedisTemplate = Mockito.mock(StringRedisTemplate)

    @Shared
    private DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO()
    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()
    @Shared
    private AppServiceInstanceDTO applicationInstanceDO = new AppServiceInstanceDTO()
    @Shared
    private DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO()

    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false
    private Long organizationId = 1L
    private Long projectId = 1L

    def setup() {
        if (!isToInit) {
            return
        }

        DependencyInjectUtil.setAttribute(baseServiceClientOperator, "baseServiceClient", baseServiceClient)
        DependencyInjectUtil.setAttribute(clusterNodeInfoService, "stringRedisTemplate", mockStringRedisTemplate)

        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)

        ProjectDTO projectDTO1 = new ProjectDTO()
        projectDTO1.setId(2L)
        projectDTO1.setCode("test")
        projectDTO1.setOrganizationId(1L)

        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(baseServiceClient).queryIamProject(anyLong())

        OrganizationDTO organizationDO = new OrganizationDTO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(anyLong())

        List<ProjectDTO> projectDOList = new ArrayList<>()
        projectDOList.add(projectDO)
        projectDOList.add(projectDTO1)
        PageInfo<ProjectDTO> projectDOPage = new PageInfo(projectDOList)
        ResponseEntity<PageInfo<ProjectDTO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
        Mockito.when(baseServiceClient.queryProjectByOrgId(anyLong(), anyInt(), anyInt(), isNull(), argThat(new ArgumentMatcher<String[]>() {
            @Override
            boolean matches(String[] argument) {
                return true
            }
        }))).thenReturn(projectDOPageResponseEntity)

        IamUserDTO iamUserDTO = new IamUserDTO()
        iamUserDTO.setId(1L)
        iamUserDTO.setProjectOwner(true)
        iamUserDTO.setOrganizationId(organizationId)
        List<IamUserDTO> iamUserDTOList = new ArrayList<>()
        iamUserDTOList.add(iamUserDTO)
        ResponseEntity iamUserResponseEntity = new ResponseEntity(iamUserDTOList, HttpStatus.OK)
        Mockito.when(baseServiceClient.listUsersByIds(any())).thenReturn(iamUserResponseEntity)

        devopsClusterDTO.setCode("uat")
        devopsClusterDTO.setId(1L)
        devopsClusterDTO.setName("uat")
        devopsClusterDTO.setOrganizationId(organizationId)
        devopsClusterDTO.setProjectId(projectId)
        devopsClusterMapper.insert(devopsClusterDTO)

        devopsEnvironmentDTO.setName("env")
        devopsEnvironmentDTO.setCode("env")
        devopsEnvironmentDTO.setClusterId(devopsClusterDTO.getId())
        devopsEnvironmentMapper.insert(devopsEnvironmentDTO)

        applicationInstanceDO.setAppServiceId(1L)
        applicationInstanceDO.setAppServiceName("app")
        applicationInstanceDO.setEnvId(devopsEnvironmentDTO.getId())
        applicationInstanceMapper.insert(applicationInstanceDO)

        devopsEnvPodDO.setInstanceId(applicationInstanceDO.getId())
        devopsEnvPodDO.setNodeName("uat01")
        devopsEnvPodDO.setRestartCount(11L)
        devopsEnvPodDO.setName("pod01")
        devopsEnvPodMapper.insert(devopsEnvPodDO)

        ListOperations<String, String> mockListOperations = Mockito.mock(ListOperations)
        Mockito.when(mockStringRedisTemplate.opsForList()).thenReturn(mockListOperations)
        Mockito.when(mockListOperations.size(anyString())).thenReturn(1L)

        ClusterNodeInfoVO clusterNodeInfoDTO = new ClusterNodeInfoVO()
        clusterNodeInfoDTO.setNodeName("uat01")

        Mockito.when(mockListOperations.range(anyString(), anyLong(), anyLong())).thenReturn(Arrays.asList(JSONObject.toJSONString(clusterNodeInfoDTO)))
    }

    def cleanup() {
        if (!isToClean) {
            return
        }
        DependencyInjectUtil.restoreDefaultDependency(baseServiceClientOperator, "baseServiceClient")
        DependencyInjectUtil.restoreDefaultDependency(clusterNodeInfoService, "stringRedisTemplate")

        // 删除cluster
        devopsClusterMapper.selectAll().forEach { devopsClusterMapper.delete(it) }

        // 删除clusterProRel
        devopsClusterProPermissionMapper.selectAll().forEach { devopsClusterProPermissionMapper.delete(it) }
        devopsEnvironmentMapper.selectAll().forEach { devopsEnvironmentMapper.delete(it) }
        devopsEnvPodMapper.selectAll().forEach { devopsEnvPodMapper.delete(it) }
        applicationInstanceMapper.selectAll().forEach { applicationInstanceMapper.delete(it) }

    }

    def "Create"() {
        given: '初始化DTO'
        isToInit = false
        DevopsClusterReqVO devopsClusterReqVO = new DevopsClusterReqVO()
        devopsClusterReqVO.setCode("cluster")
        devopsClusterReqVO.setName("cluster")
        devopsClusterReqVO.setDescription("testCluster")

        when: '组织下创建集群'
        def str = restTemplate.postForObject(MAPPING, devopsClusterReqVO, String.class, 1L)

        then: '校验返回值'
        str != null
        devopsClusterMapper.selectAll().size() != 0
    }

    def "Update"() {
        given: '构建查询条件'
        def searchCondition = new DevopsClusterDTO()
        searchCondition.setCode("cluster")
        searchCondition.setName("cluster")
        def createdCluster = devopsClusterMapper.selectOne(searchCondition)

        DevopsClusterUpdateVO devopsClusterUpdateVO = new DevopsClusterUpdateVO()
        devopsClusterUpdateVO.setName("updateCluster")
        devopsClusterUpdateVO.setId(createdCluster.getId())
        devopsClusterUpdateVO.setObjectVersionNumber(createdCluster.getObjectVersionNumber())

        searchCondition.setName("updateCluster")

        when: '更新集群下的项目'
        restTemplate.put(MAPPING + "/{cluster_id}", devopsClusterUpdateVO, projectId, createdCluster.getId())

        then: '校验是否更新'
        devopsClusterMapper.selectOne(searchCondition).getName() == "updateCluster"
    }

    def "Query"() {
        given:
        def searchCondition = new DevopsClusterDTO()
        searchCondition.setCode("cluster")
        searchCondition.setName("updateCluster")
        def createdCluster = devopsClusterMapper.selectOne(searchCondition)

        when: '查询单个集群信息'
        def dto = restTemplate.getForObject(MAPPING + "/{cluster_id}", DevopsClusterRepVO.class, 1L, createdCluster.getId())

        then: '校验返回值'
        dto["name"] == "updateCluster"
    }

    def "queryByCode"() {
        print(devopsClusterMapper.selectAll())
        when: '根据code查询'
        def dto = restTemplate.getForObject(MAPPING + "/query_by_code?code={code}", DevopsClusterRepVO.class, projectId, "cluster")
        then:
        dto["name"] == "updateCluster"
    }

    def "CheckName"() {
        when: '校验集群名唯一性'
        def exception = restTemplate.getForEntity(MAPPING + "/check_name?name=uniqueName", ExceptionResponse.class, 1L)

        then: '名字不存在不抛出异常'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }

    def "CheckCode"() {
        when: '校验集群编码唯一性'
        def exception = restTemplate.getForEntity(MAPPING + "/check_code?code=uniqueCode", ExceptionResponse.class, 1L)

        then: '编码不存在不抛出异常'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }

    def "pageCluster"() {
        given: 'mock envUtil'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        when: '分页查询项目列表'
        def e = restTemplate.postForEntity(MAPPING + "/page_cluster", null, PageInfo.class, 1L)

        then: '校验返回值'
        e.getBody().getList().size() != 0
    }

    def "ListClusterProjects"() {
        given: '插入集群与项目关联关系数据'
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO()
        devopsClusterProPermissionDTO.setProjectId(projectId)
        devopsClusterProPermissionDTO.setClusterId(1L)
        devopsClusterProPermissionMapper.insert(devopsClusterProPermissionDTO)

        when: '查询已有权限的项目列表'
        def e = restTemplate.postForEntity(MAPPING + "/{cluster_id}/permission/page_related", null, PageInfo.class, 1L, 1L)

        then: '校验返回值'
        e.getBody().getList().get(0)["code"] == "pro"
    }

    def "listAllNonRelatedProjects"() {
        when: '查询没有关联关系的项目'
        def result = restTemplate.postForEntity(MAPPING + "/{cluster_id}/permission/list_non_related", null, List.class, projectId, 1L)
        then:
        result.getBody().size() != 0
    }

    def "assignPermission"() {
        given: '创建一个集群'
        DevopsClusterDTO devopsClusterDTO1 = new DevopsClusterDTO()
        devopsClusterDTO1.setId(100L)
        devopsClusterDTO1.setProjectId(projectId)
        devopsClusterDTO1.setSkipCheckProjectPermission(true)
        devopsClusterMapper.insert(devopsClusterDTO1)

        DevopsClusterPermissionUpdateVO devopsClusterPermissionUpdateVO = new DevopsClusterPermissionUpdateVO()
        List<Long> ids = new ArrayList<>()
        ids.add(1L)
        ids.add(2L)
        devopsClusterPermissionUpdateVO.setClusterId(100L)
        devopsClusterPermissionUpdateVO.setProjectIds(ids)
        devopsClusterPermissionUpdateVO.setObjectVersionNumber(1)

        when: '原本跳过，现在也跳过权限检查'
        devopsClusterPermissionUpdateVO.setSkipCheckProjectPermission(true)
        def result = restTemplate.postForEntity(MAPPING + "/{cluster_id}/permission", devopsClusterPermissionUpdateVO, null, projectId, 1L)
        then:
        result.statusCode.is2xxSuccessful()


        when: '原本跳过，现在不跳过权限检查'
        devopsClusterPermissionUpdateVO.setSkipCheckProjectPermission(false)
        result = restTemplate.postForEntity(MAPPING + "/{cluster_id}/permission", devopsClusterPermissionUpdateVO, null, projectId, 1L)
        then:
        result.statusCode.is2xxSuccessful()
        devopsClusterProPermissionMapper.selectAll().size() != 0

        when: '原本不跳过，现在不跳过'
        devopsClusterPermissionUpdateVO.setSkipCheckProjectPermission(false)
        result = restTemplate.postForEntity(MAPPING + "/{cluster_id}/permission", devopsClusterPermissionUpdateVO, null, projectId, 1L)
        then:
        result.statusCode.is2xxSuccessful()
        devopsClusterProPermissionMapper.selectAll().size() != 0

        when: '原本不跳过，现在跳过'
        devopsClusterPermissionUpdateVO.setSkipCheckProjectPermission(true)
        devopsClusterPermissionUpdateVO.setObjectVersionNumber(2)
        result = restTemplate.postForEntity(MAPPING + "/{cluster_id}/permission", devopsClusterPermissionUpdateVO, null, projectId, 1L)
        then:
        result.statusCode.is2xxSuccessful()
        devopsClusterMapper.selectByPrimaryKey(100L).getSkipCheckProjectPermission()

        devopsClusterMapper.deleteByPrimaryKey(100L)
    }

    def "deletePermissionOfProject"() {
        when: "删除权限"
        restTemplate.delete(MAPPING + "/{cluster_id}/permission?delete_project_id={id}", projectId, 1L, projectId)
        then:
        devopsClusterProPermissionMapper.selectAll().size() == 0
    }

    def "queryClustersAndNodes"() {
        when: "查询"
        def result = restTemplate.getForEntity(MAPPING + "/tree_menu", List.class, projectId)
        then:
        result.getBody().size() != 0
    }

    def "QueryShell"() {
        given: 'mock envUtil'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        when: '查询shell脚本'
        def e = restTemplate.getForEntity(MAPPING + "/query_shell/{cluster_id}", String.class, 1L, 1L)

        then: '校验返回值'
        e.getBody() != null
    }

    def "ListCluster"() {
        given: '查询参数'
        String str = new String("{}")

        and: 'mock envUtil'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        when: '集群列表查询'
        def e = restTemplate.postForEntity(MAPPING + "/page_cluster?page=0&size=10&doPage=true", str, PageInfo.class, 1L)

        then: '校验返回值'
        e.getBody().getList().get(0)["code"] == "uat"
    }

    def "get pods in node"() {
        given: '准备查询数据'

        when: '发送请求'
        def e = restTemplate.postForEntity(MAPPING + "/page_node_pods?page=0&size=10&cluster_id={clusterId}&node_name={nodeName}", "{}", PageInfo.class, 1L, devopsClusterDTO.getId(), devopsEnvPodDO.getNodeName())

        then: '校验结果'
        e.getStatusCode().is2xxSuccessful()
        e.getBody().getList().size() != 0
    }

    // 分页查询集群下的节点
    def "list cluster nodes"() {
        given: "准备数据"
        def url = MAPPING + "/page_nodes?cluster_id={cluster_id}&page=0&size=10"

        when: "发送请求"
        def res = restTemplate.getForEntity(url, PageInfo.class, devopsClusterDTO.getProjectId(), devopsClusterDTO.getId())

        then: "校验结果"
        res.getStatusCode().is2xxSuccessful()
        res.getBody().getList().size() == 1
    }


    def "checkConnectEnvs"() {
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/{cluster_id}/check_connect_envs", Boolean.class, projectId, 1000L)
        then:
        entity.getBody()
    }


    // 根据集群id和节点名查询节点状态信息
    def "get certain node information"() {
        given: "准备数据"
        def url = MAPPING + "/nodes?cluster_id={clusterId}&node_name={nodeName}"

        when: "发送请求"
        def res = restTemplate.getForEntity(url, ClusterNodeInfoVO, devopsClusterDTO.getProjectId(), devopsClusterDTO.getId(), devopsEnvPodDO.getNodeName())

        then: "校验结果"
        res.getStatusCode().is2xxSuccessful()
        res.getBody().getNodeName() == devopsEnvPodDO.getNodeName()
    }

    def "DeleteCluster"() {
        given: 'mock envUtil'
        List<Long> envList = new ArrayList<>()
        envList.add(999L)

        def searchCondition = new DevopsClusterDTO()
        searchCondition.setCode("cluster")
        searchCondition.setName("updateCluster")
        def createdCluster = devopsClusterMapper.selectOne(searchCondition)

        when: '删除集群'
        restTemplate.delete(MAPPING + "/{clusterId}", projectId, createdCluster.getId())

        then: '校验返回值'
        devopsClusterMapper.selectByPrimaryKey(createdCluster.getId()) == null
    }

    def clean() {
        given:
        isToClean = true
    }
}
