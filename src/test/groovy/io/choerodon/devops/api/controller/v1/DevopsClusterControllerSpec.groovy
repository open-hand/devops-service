package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsClusterRepDTO
import io.choerodon.devops.api.dto.DevopsClusterReqDTO
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.dataobject.DevopsClusterDO
import io.choerodon.devops.infra.dataobject.DevopsClusterProPermissionDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.DevopsClusterMapper
import io.choerodon.devops.infra.mapper.DevopsClusterProPermissionMapper
import io.choerodon.websocket.helper.EnvListener
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
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
 * Date: 2018/11/13
 * Time: 14:03
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsClusterController)
@Stepwise
class DevopsClusterControllerSpec extends Specification {

    private static final String MAPPING = "/v1/organizations/{organization_id}/clusters"

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private IamRepository iamRepository
    @Autowired
    private DevopsClusterMapper devopsClusterMapper
    @Autowired
    private DevopsClusterProPermissionMapper devopsClusterProPermissionMapper

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    def setup() {
        iamRepository.initMockIamService(iamServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(anyLong())

        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(anyLong())

        Page<ProjectDO> projectDOPage = new Page<>()
        List<ProjectDO> projectDOList = new ArrayList<>()
        projectDOList.add(projectDO)
        projectDOPage.setContent(projectDOList)
        ResponseEntity<Page<ProjectDO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryProjectByOrgId(anyLong(), anyInt(), anyInt(), anyString(), any(String[].class))).thenReturn(projectDOPageResponseEntity)
    }

    def "Create"() {
        given: '初始化DTO'
        DevopsClusterReqDTO devopsClusterReqDTO = new DevopsClusterReqDTO()
        List<Long> projectIds = new ArrayList<>()
        projectIds.add(1L)
        devopsClusterReqDTO.setCode("cluster")
        devopsClusterReqDTO.setProjects(projectIds)
        devopsClusterReqDTO.setSkipCheckProjectPermission(false)

        when: '组织下创建集群'
        def str = restTemplate.postForObject(MAPPING, devopsClusterReqDTO, String.class, 1L)

        then: '校验返回值'
        str != null
    }

    def "Update"() {
        given: '初始化DTO'
        DevopsClusterReqDTO devopsClusterReqDTO = new DevopsClusterReqDTO()
        List<Long> projectIds = new ArrayList<>()
        projectIds.add(2L)
        devopsClusterReqDTO.setCode("cluster")
        devopsClusterReqDTO.setProjects(projectIds)
        devopsClusterReqDTO.setName("updateCluster")
        devopsClusterReqDTO.setSkipCheckProjectPermission(false)

        when: '更新集群下的项目'
        restTemplate.put(MAPPING + "?clusterId=1", devopsClusterReqDTO, 2L)

        then: '校验是否更新'
        devopsClusterMapper.selectAll().get(0)["name"] == "updateCluster"
    }

    def "Query"() {
        when: '查询单个集群信息'
        def dto = restTemplate.getForObject(MAPPING + "/1", DevopsClusterRepDTO.class, 1L)

        then: '校验返回值'
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

    def "PageProjects"() {
        given: '模糊查询参数'
        String[] str = new String[1]
        str[0] = "{}"

        when: '分页查询项目列表'
        def e = restTemplate.postForEntity(MAPPING + "/page_projects?page=0&size=10&cluster_id=1", str, Page.class, 1L)

        then: '校验返回值'
        e.getBody().get(0)["code"] == "pro"
    }

    def "ListClusterProjects"() {
        when: '查询已有权限的项目列表'
        def e = restTemplate.getForEntity(MAPPING + "/list_cluster_projects/{clusterId}", List.class, 1L, 1L)

        then: '校验返回值'
        e.getBody().get(0)["code"] == "pro"
    }

    def "QueryShell"() {
        given: 'mock envUtil'
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when: '查询shell脚本'
        def e = restTemplate.getForEntity(MAPPING + "/query_shell/{clusterId}", String.class, 1L, 1L)

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
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when: '集群列表查询'
        def e = restTemplate.postForEntity(MAPPING + "/page_cluster?page=0&size=10&doPage=true", str, Page.class, 1L)

        then: '校验返回值'
        e.getBody().get(0)["code"] == "cluster"
    }

    def "DeleteCluster"() {
        given: 'mock envUtil'
        List<Long> envList = new ArrayList<>()
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList

        when: '删除集群'
        restTemplate.delete(MAPPING + "/{clusterId}", 1L, 1L)

        then: '校验返回值'
        devopsClusterMapper.selectAll().size() == 0

        and: '清理数据'
        // 删除cluster
        List<DevopsClusterDO> list = devopsClusterMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (DevopsClusterDO e : list) {
                devopsClusterMapper.delete(e)
            }
        }
        // 删除clusterProRel
        List<DevopsClusterProPermissionDO> list1 = devopsClusterProPermissionMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsClusterProPermissionDO e : list1) {
                devopsClusterProPermissionMapper.delete(e)
            }
        }
    }
}
