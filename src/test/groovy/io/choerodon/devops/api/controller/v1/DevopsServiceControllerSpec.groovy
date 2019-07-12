package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.core.domain.Page
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsServiceDTO
import io.choerodon.devops.api.vo.DevopsServiceReqDTO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.vo.iam.RoleDTO
import io.choerodon.devops.api.vo.iam.entity.PortMapE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.RepositoryFile
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.common.util.GitUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.gitlab.MemberDTO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.*
import io.choerodon.websocket.helper.EnvListener
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

import static org.mockito.Matchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

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
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsServiceMapper devopsServiceMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private DevopsProjectRepository devopsProjectRepository
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper
    @Autowired
    private DevopsServiceAppInstanceMapper devopsServiceAppInstanceMapper
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository
    @Autowired
    private DevopsServiceRepository devopsServiceRepository

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil
    @Autowired
    @Qualifier("mockGitUtil")
    private GitUtil gitUtil
    @Autowired
    @Qualifier("mockEnvListener")
    private EnvListener envListener

    @Shared
    DevopsEnvCommandDO devopsEnvCommandDO = new DevopsEnvCommandDO()
    @Shared
    DevopsEnvFileResourceDO devopsEnvFileResourceDO = new DevopsEnvFileResourceDO()
    @Shared
    DevopsServiceAppInstanceDO devopsServiceAppInstanceDO = new DevopsServiceAppInstanceDO()
    @Shared
    ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()
    @Shared
    DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()

    @Autowired
    private IamRepository iamRepository
    @Autowired
    private GitlabRepository gitlabRepository
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    def setupSpec() {
        FileUtil.copyFile("src/test/gitops/org/pro/env/test-svc.yaml", "gitops/org/pro/env")

        devopsEnvCommandDO.setId(1L)

        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setActive(true)
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)
        devopsEnvironmentDO.setSequence(1L)
        devopsEnvironmentDO.setCode("env")
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)

        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setProjectId(1L)
        applicationInstanceDO.setCode("test")
        applicationInstanceDO.setAppId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setAppVersionId(1L)

        devopsEnvFileResourceDO.setId(1L)
        devopsEnvFileResourceDO.setEnvId(1L)
        devopsEnvFileResourceDO.setResourceId(1L)
        devopsEnvFileResourceDO.setResourceType("Service")
        devopsEnvFileResourceDO.setFilePath("test-svc.yaml")

        devopsServiceAppInstanceDO.setId(1L)
        devopsServiceAppInstanceDO.setServiceId(1L)
        devopsServiceAppInstanceDO.setAppInstanceId(1L)
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClient", gitlabServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        MemberDTO memberDO = new MemberDTO()
        memberDO.setAccessLevel(AccessLevel.OWNER)
        ResponseEntity<MemberDTO> responseEntity2 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(responseEntity2)

        List<RoleDTO> roleDTOList = new ArrayList<>()
        RoleDTO roleDTO = new RoleDTO()
        roleDTO.setCode("role/project/default/project-owner")
        roleDTOList.add(roleDTO)
        List<ProjectWithRoleDTO> projectWithRoleDTOList = new ArrayList<>()
        ProjectWithRoleDTO projectWithRoleDTO = new ProjectWithRoleDTO()
        projectWithRoleDTO.setName("pro")
        projectWithRoleDTO.setRoles(roleDTOList)
        projectWithRoleDTOList.add(projectWithRoleDTO)
        PageInfo<ProjectWithRoleDTO> projectWithRoleDTOPage = new PageInfo<>(projectWithRoleDTOList)
        ResponseEntity<PageInfo<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        RepositoryFile repositoryFile = new RepositoryFile()
        repositoryFile.setFilePath("testFilePath")
        ResponseEntity<RepositoryFile> responseEntity3 = new ResponseEntity<>(repositoryFile, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createFile(anyInt(), anyString(), anyString(), anyString(), anyInt())).thenReturn(responseEntity3)
        Mockito.when(gitlabServiceClient.updateFile(anyInt(), anyString(), anyString(), anyString(), anyInt())).thenReturn(responseEntity3)
    }

    def "CheckName"() {
        given: '插入数据'
        devopsEnvCommandMapper.insert(devopsEnvCommandDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        applicationInstanceMapper.insert(applicationInstanceDO)
        devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO)
        devopsServiceAppInstanceMapper.insert(devopsServiceAppInstanceDO)

        when: '检查网络唯一性'
        def exist = restTemplate.getForObject("/v1/projects/1/service/check?envId=1&name=svc", Boolean.class)

        then: '校验返回值'
        exist
    }

    def "Create"() {
        given:
        List<PortMapE> portMapES = new ArrayList<>()
        PortMapE portMapE = new PortMapE()
        portMapE.setPort(7777L)
        portMapE.setNodePort(9999L)
        portMapE.setTargetPort("8888")
        portMapES.add(portMapE)

        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO()
        devopsServiceReqDTO.setPorts()
        devopsServiceReqDTO.setAppId(1L)
        devopsServiceReqDTO.setEnvId(1L)
        devopsServiceReqDTO.setType("ClusterIP")
        devopsServiceReqDTO.setName("svcsvc")
        devopsServiceReqDTO.setPorts(portMapES)
        devopsServiceReqDTO.setExternalIp("1.1.1.1")

        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null

        when: '部署网络'
        def result = restTemplate.postForObject("/v1/projects/1/service", devopsServiceReqDTO, Boolean.class)

        then: '校验返回值'
        result
    }

    def "Update"() {
        given: 'mock envUtil'
        List<PortMapE> portMapES = new ArrayList<>()
        PortMapE portMapE = new PortMapE()
        portMapE.setPort(7777L)
        portMapE.setNodePort(9999L)
        portMapE.setTargetPort("8888")
        portMapES.add(portMapE)

        List<String> appInstances = new ArrayList<>()
        appInstances.add(applicationInstanceDO.getCode())
        DevopsServiceReqDTO newDevopsServiceReqDTO = new DevopsServiceReqDTO()
        newDevopsServiceReqDTO.setAppId(1L)
        newDevopsServiceReqDTO.setEnvId(1L)
        newDevopsServiceReqDTO.setType("ClusterIP")
        newDevopsServiceReqDTO.setName("svcsvc")
        newDevopsServiceReqDTO.setAppInstance(appInstances)
        newDevopsServiceReqDTO.setPorts(portMapES)
        newDevopsServiceReqDTO.setExternalIp("1.2.1.1")

        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        id = devopsServiceRepository.selectByNameAndEnvId("svcsvc", 1L).getId()
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
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null

        ResponseEntity responseEntity = new ResponseEntity<>(HttpStatus.OK)
        Mockito.when(gitlabServiceClient.deleteFile(anyInt(), anyString(), anyString(), anyInt())).thenReturn(responseEntity)

        when: '删除网络'
        restTemplate.delete("/v1/projects/1/service/{id}", id)

        then: '校验返回值'
        devopsEnvCommandRepository.baseQueryByObject("service", id).getCommandType() == "delete"
    }

    def "ListByEnvId"() {
        given: '初始化网络DO类'
        DevopsServiceDO devopsServiceDO = new DevopsServiceDO()
        devopsServiceDO.setEnvId(1L)
        devopsServiceDO.setAppId(1L)
        devopsServiceDO.setName("svcsvc2")
        devopsServiceDO.setStatus("running")
        devopsServiceDO.setPorts("[{\"port\":7777}]")
        devopsServiceDO.setExternalIp("1.1.1.1")
        devopsServiceDO.setType("ClusterIP")
        devopsServiceDO.setCommandId(1L)
        devopsServiceMapper.insert(devopsServiceDO)

        when: '分页查询网络列表'
        def list = restTemplate.getForObject("/v1/projects/1/service?envId=1", List.class)

        then: '校验返回值'
        list.size() == 1
    }

    def "Query"() {
        when: '查询单个网络'
        def dto = restTemplate.getForObject("/v1/projects/1/service/{id}", DevopsServiceDTO.class, id)

        then: '校验返回值'
        dto != null
    }

    def "ListByEnv"() {
        given: '设置请求头'
        String infra = "{\"searchParam\":{\"name\":[\"svc\"]}}"

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList() >> envList
        envUtil.getUpdatedEnvList() >> envList

        when: '环境总览网络查询'
        def page = restTemplate.postForObject("/v1/projects/1/service/1/listByEnv", strEntity, Page.class)

        then: '校验返回值'
        page.size() == 2

        // 删除envCommand
        List<DevopsEnvCommandDO> list = devopsEnvCommandMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (DevopsEnvCommandDO e : list) {
                devopsEnvCommandMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDO> list1 = devopsEnvironmentMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvironmentDO e : list1) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除appInstance
        List<ApplicationInstanceDO> list2 = applicationInstanceMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (ApplicationInstanceDO e : list2) {
                applicationInstanceMapper.delete(e)
            }
        }
        // 删除envFileResource
        List<DevopsEnvFileResourceDO> list3 = devopsEnvFileResourceMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (DevopsEnvFileResourceDO e : list3) {
                devopsEnvFileResourceMapper.delete(e)
            }
        }
        // 删除serviceInstance
        List<DevopsServiceAppInstanceDO> list4 = devopsServiceAppInstanceMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (DevopsServiceAppInstanceDO e : list4) {
                devopsServiceAppInstanceMapper.delete(e)
            }
        }
        // 删除service
        List<DevopsServiceDO> list5 = devopsServiceMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (DevopsServiceDO e : list5) {
                devopsServiceMapper.delete(e)
            }
        }
        FileUtil.deleteDirectory(new File("gitops"))
    }
}
