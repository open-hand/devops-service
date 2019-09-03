package io.choerodon.devops.api.controller.v1

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import com.github.pagehelper.PageInfo
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

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsServiceReqVO
import io.choerodon.devops.api.vo.DevopsServiceVO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.DevopsEnvCommandService
import io.choerodon.devops.app.service.DevopsProjectService
import io.choerodon.devops.app.service.DevopsServiceService
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.*
import io.choerodon.devops.infra.util.FileUtil

/**
 * Created by n!Ck
 * Date: 2018/9/12
 * Time: 15:22
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsServiceController)
@Stepwise
class DevopsServiceControllerSpec extends Specification {

    private static id = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private DevopsServiceMapper devopsServiceMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private DevopsProjectService devopsProjectRepository
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private AppServiceInstanceMapper applicationInstanceMapper
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper
    @Autowired
    private DevopsServiceInstanceMapper devopsServiceAppInstanceMapper
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandRepository
    @Autowired
    private DevopsServiceService devopsServiceService

    @Qualifier("mockGitlabServiceClientOperator")
    @Autowired
    private GitlabServiceClientOperator mockGitlabServiceClientOperator

    @Qualifier("mockBaseServiceClientOperator")
    @Autowired
    private BaseServiceClientOperator mockBaseServiceClientOperator

    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler envUtil

    @Shared
    DevopsEnvCommandDTO devopsEnvCommandDO = new DevopsEnvCommandDTO()
    @Shared
    DevopsEnvFileResourceDTO devopsEnvFileResourceDO = new DevopsEnvFileResourceDTO()
    @Shared
    DevopsServiceInstanceDTO devopsServiceAppInstanceDO = new DevopsServiceInstanceDTO()
    @Shared
    AppServiceInstanceDTO applicationInstanceDO = new AppServiceInstanceDTO()
    @Shared
    DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()

    def setupSpec() {
        FileUtil.copyFile("src/test/gitops/org/pro/env/test-svc.yaml", "gitops/org/pro/env")

        devopsEnvCommandDO.setId(1L)

        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setActive(true)
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)
        devopsEnvironmentDO.setCode("env")
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)

        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setProjectId(1L)
        applicationInstanceDO.setCode("test")
        applicationInstanceDO.setAppServiceId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setAppServiceVersionId(1L)

        devopsEnvFileResourceDO.setId(1L)
        devopsEnvFileResourceDO.setEnvId(1L)
        devopsEnvFileResourceDO.setResourceId(1L)
        devopsEnvFileResourceDO.setResourceType("Service")
        devopsEnvFileResourceDO.setFilePath("test-svc.yaml")

        devopsServiceAppInstanceDO.setId(1L)
        devopsServiceAppInstanceDO.setServiceId(1L)
        devopsServiceAppInstanceDO.setInstanceId(1L)
    }

    def setup() {
        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        Mockito.doReturn(projectDO).when(mockBaseServiceClientOperator).queryIamProjectById(1L)

        OrganizationDTO organizationDO = new OrganizationDTO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        Mockito.doReturn(organizationDO).when(mockBaseServiceClientOperator).queryOrganizationById(1L)

        MemberDTO memberDO = new MemberDTO()
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())

        Mockito.when(mockGitlabServiceClientOperator.queryGroupMember(anyInt(), anyInt())).thenReturn(memberDO)

        List<RoleVO> roleDTOList = new ArrayList<>()
        RoleVO roleDTO = new RoleVO()
        roleDTO.setCode("role/project/default/project-owner")
        roleDTOList.add(roleDTO)
        List<ProjectWithRoleVO> projectWithRoleDTOList = new ArrayList<>()
        ProjectWithRoleVO projectWithRoleDTO = new ProjectWithRoleVO()
        projectWithRoleDTO.setName("pro")
        projectWithRoleDTO.setRoles(roleDTOList)
        projectWithRoleDTOList.add(projectWithRoleDTO)
        Mockito.doReturn(projectWithRoleDTOList).when(mockBaseServiceClientOperator).listProjectWithRoleDTO(anyLong())

        RepositoryFileDTO repositoryFile = new RepositoryFileDTO()
        repositoryFile.setFilePath("testFilePath")

    }

    def "CheckName"() {
        given: '插入数据'
        devopsEnvCommandMapper.insert(devopsEnvCommandDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        applicationInstanceMapper.insert(applicationInstanceDO)
        devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO)
        devopsServiceAppInstanceMapper.insert(devopsServiceAppInstanceDO)

        when: '检查网络唯一性'
        def exist = restTemplate.getForEntity("/v1/projects/{project_id}/service/check_name?env_id=1&name=svc", Boolean.class, 1L)

        then: '校验返回值'
        exist.getBody()
    }

    def "Create"() {
        given:
        List<PortMapVO> portMapES = new ArrayList<>()
        PortMapVO portMapE = new PortMapVO()
        portMapE.setPort(7777L)
        portMapE.setNodePort(9999L)
        portMapE.setTargetPort("8888")
        portMapES.add(portMapE)

        DevopsServiceReqVO devopsServiceReqDTO = new DevopsServiceReqVO()
        devopsServiceReqDTO.setPorts()
        devopsServiceReqDTO.setAppServiceId(1L)
        devopsServiceReqDTO.setEnvId(1L)
        devopsServiceReqDTO.setType("ClusterIP")
        devopsServiceReqDTO.setName("svcsvc")
        devopsServiceReqDTO.setPorts(portMapES)
        devopsServiceReqDTO.setExternalIp("1.1.1.1")

        envUtil.checkEnvConnection(_ as Long) >> null

        when: '部署网络'
        def result = restTemplate.postForEntity("/v1/projects/{project_id}/service", devopsServiceReqDTO, Boolean.class, 1L)

        then: '校验返回值'
        result.getStatusCode().is2xxSuccessful()
    }

    def "Update"() {
        given: 'mock envUtil'
        List<PortMapVO> portMapES = new ArrayList<>()
        PortMapVO portMapE = new PortMapVO()
        portMapE.setPort(7777L)
        portMapE.setNodePort(9999L)
        portMapE.setTargetPort("8888")
        portMapES.add(portMapE)

        List<String> appInstances = new ArrayList<>()
        appInstances.add(applicationInstanceDO.getCode())
        DevopsServiceReqVO newDevopsServiceReqDTO = new DevopsServiceReqVO()
        newDevopsServiceReqDTO.setAppServiceId(1L)
        newDevopsServiceReqDTO.setEnvId(1L)
        newDevopsServiceReqDTO.setType("ClusterIP")
        newDevopsServiceReqDTO.setName("svcsvc")
        newDevopsServiceReqDTO.setInstances(appInstances)
        newDevopsServiceReqDTO.setPorts(portMapES)
        newDevopsServiceReqDTO.setExternalIp("1.2.1.1")

        envUtil.checkEnvConnection(_ as Long) >> null
        id = devopsServiceService.baseQueryByNameAndEnvId("svcsvc", 1L).getId()
        devopsEnvFileResourceDO = devopsEnvFileResourceMapper.selectByPrimaryKey(1L)
        devopsEnvFileResourceDO.setResourceId(id)
        devopsEnvFileResourceMapper.updateByPrimaryKey(devopsEnvFileResourceDO)

        when: '更新网络'
        restTemplate.put("/v1/projects/1/service/{id}", newDevopsServiceReqDTO, id)

        then: '校验返回值'
        devopsServiceMapper.selectByPrimaryKey(id).getExternalIp() == "1.1.1.1"
    }

    def "Delete"() {
        given: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long) >> null
        def id = devopsServiceService.baseQueryByNameAndEnvId("svcsvc", 1L).getId()
        Mockito.doReturn(true).when(mockGitlabServiceClientOperator).getFile(anyInt(), anyString(), anyString())
        when: '删除网络'
        ResponseEntity entity = restTemplate.exchange("/v1/projects/1/service/{id}", HttpMethod.DELETE, null, Object.class, id)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
    }

    def "ListByEnvId"() {
        given: '初始化网络DO类'
        DevopsServiceDTO devopsServiceDO = new DevopsServiceDTO()
        devopsServiceDO.setEnvId(1L)
        devopsServiceDO.setAppServiceId(1L)
        devopsServiceDO.setName("svcsvc2")
        devopsServiceDO.setStatus("running")
        devopsServiceDO.setPorts("[{\"port\":7777}]")
        devopsServiceDO.setExternalIp("1.1.1.1")
        devopsServiceDO.setType("ClusterIP")
        devopsServiceDO.setCommandId(1L)
        devopsServiceMapper.insert(devopsServiceDO)

        when: '分页查询网络列表'
        def entity = restTemplate.getForEntity("/v1/projects/1/service/list_by_env?env_id=1", List.class)

        then: '校验返回值'
        entity.getBody().size() == 1
    }

    def "Query"() {
        when: '查询单个网络'
        def entity = restTemplate.getForEntity("/v1/projects/1/service/{id}", DevopsServiceVO.class, id)

        then: '校验返回值'
        entity.getBody() != null
    }

    def "queryByName"() {
        when: '根据网络名查询网络'
        def entity = restTemplate.getForEntity("/v1/projects/1/service/query_by_name?env_id=1&name=svcsvc2", DevopsServiceVO.class)
        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
    }

    def "pageByEnv"() {
        given: '设置请求头'
        String infra = "{\"params\":[],\"searchParam\":{\"name\":\"svc\"}}"

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList() >> envList
        envUtil.getUpdatedEnvList() >> envList

        when: '环境总览分页查询网络'
        def entity = restTemplate.postForEntity("/v1/projects/1/service/{env_id}/page_by_env", strEntity, PageInfo.class, 1L)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getList().size() == 2

    }

    def "pageByInstance"() {
        given: '设置请求头'
        String infra = "{\"params\":[],\"searchParam\":{}}"

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        when: '查询实例下关联的网络域名（不包含chart）'
        def entity = restTemplate.postForEntity("/v1/projects/1/service/page_by_instance?instance_id=1&app_service_id=1&page=1&size=10", strEntity, PageInfo.class)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getList() != null

        and: '清理数据'
        // 删除envCommand
        List<DevopsEnvCommandDTO> list = devopsEnvCommandMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (DevopsEnvCommandDTO e : list) {
                devopsEnvCommandMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDTO> list1 = devopsEnvironmentMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvironmentDTO e : list1) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除appInstance
        List<AppServiceInstanceDTO> list2 = applicationInstanceMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (AppServiceInstanceDTO e : list2) {
                applicationInstanceMapper.delete(e)
            }
        }
        // 删除envFileResource
        List<DevopsEnvFileResourceDTO> list3 = devopsEnvFileResourceMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (DevopsEnvFileResourceDTO e : list3) {
                devopsEnvFileResourceMapper.delete(e)
            }
        }
        // 删除serviceInstance
        List<DevopsServiceInstanceDTO> list4 = devopsServiceAppInstanceMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (DevopsServiceInstanceDTO e : list4) {
                devopsServiceAppInstanceMapper.delete(e)
            }
        }
        // 删除service
        List<DevopsServiceDTO> list5 = devopsServiceMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (DevopsServiceDTO e : list5) {
                devopsServiceMapper.delete(e)
            }
        }
        FileUtil.deleteDirectory(new File("gitops"))
    }

}
