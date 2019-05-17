package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.core.domain.Page
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsIngressDTO
import io.choerodon.devops.api.dto.DevopsIngressPathDTO
import io.choerodon.devops.api.dto.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.dto.iam.RoleDTO
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.RepositoryFile
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.common.util.GitUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.common.util.enums.CertificationStatus
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.dataobject.gitlab.MemberDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.*
import io.choerodon.websocket.helper.EnvListener
import io.choerodon.websocket.helper.EnvSession
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
 * Time: 11:07
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsIngressController)
@Stepwise
class DevopsIngressControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsIngressMapper devopsIngressMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private DevopsServiceMapper devopsServiceMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private DevopsProjectRepository devopsProjectRepository
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil
    @Autowired
    @Qualifier("mockGitUtil")
    private GitUtil gitUtil
    @Autowired
    @Qualifier("mockEnvListener")
    private EnvListener envListener

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

    @Shared
    DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO()
    @Shared
    DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO()
    @Shared
    DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
    @Shared
    DevopsServiceDO devopsServiceDO = new DevopsServiceDO()
    @Shared
    DevopsIngressDO devopsIngressDO = new DevopsIngressDO()
    @Shared
    DevopsEnvCommandDO devopsEnvCommandDO = new DevopsEnvCommandDO()
    @Shared
    DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO()
    @Shared
    DevopsEnvFileResourceDO devopsEnvFileResourceDO = new DevopsEnvFileResourceDO()

    def setupSpec() {
        FileUtil.copyFile("src/test/gitops/org/pro/env/test-ing.yaml", "gitops/org/pro/env")

        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setCode("env")
        devopsEnvironmentDO.setActive(true)
        devopsEnvironmentDO.setSequence(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setClusterId(1L)
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)

        devopsIngressPathDTO.setServiceId(1L)
        devopsIngressPathDTO.setPath("/bootz")
        devopsIngressPathDTO.setServicePort(7777L)
        devopsIngressPathDTO.setServiceName("test")
        devopsIngressPathDTO.setServiceStatus("running")
        List<DevopsIngressPathDTO> pathList = new ArrayList<>()
        pathList.add(devopsIngressPathDTO)

        devopsIngressDTO.setEnvId(1L)
        devopsIngressDTO.setCertId(1L)
        devopsIngressDTO.setName("test.ing")
        devopsIngressDTO.setPathList(pathList)
        devopsIngressDTO.setDomain("test.hand-china.com")

        devopsServiceDO.setId(1L)
        devopsServiceDO.setEnvId(1L)
        devopsServiceDO.setAppId(1L)
        devopsServiceDO.setName("svc")
        devopsServiceDO.setCommandId(1L)
        devopsServiceDO.setStatus("running")
        devopsServiceDO.setType("ClusterIP")
        devopsServiceDO.setExternalIp("1.1.1.1")
        devopsServiceDO.setPorts("[{\"port\":7777}]")

        devopsIngressDO.setId(1L)
        devopsIngressDO.setEnvId(1L)
        devopsIngressDO.setCertId(1L)
        devopsIngressDO.setUsable(true)
        devopsIngressDO.setName("ingdo")
        devopsIngressDO.setCommandId(1L)
        devopsIngressDO.setProjectId(1L)
        devopsIngressDO.setStatus("running")
        devopsIngressDO.setCommandType("create")
        devopsIngressDO.setObjectVersionNumber(1L)
        devopsIngressDO.setCommandStatus("success")
        devopsIngressDO.setDomain("test.hand-china.com")

        devopsEnvCommandDO.setId(1L)
        devopsEnvCommandDO.setStatus("success")
        devopsEnvCommandDO.setCommandType("create")

        devopsIngressPathDO.setId(1L)
        devopsIngressPathDO.setIngressId(1L)
        devopsIngressPathDO.setServiceId(1L)
        devopsIngressPathDO.setPath("testpath")

        devopsEnvFileResourceDO.setId(1L)
        devopsEnvFileResourceDO.setEnvId(1L)
        devopsEnvFileResourceDO.setResourceId(1L)
        devopsEnvFileResourceDO.setResourceType("Ingress")
        devopsEnvFileResourceDO.setFilePath("test-ing.yaml")
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)
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

        MemberDO memberDO = new MemberDO()
        memberDO.setAccessLevel(AccessLevel.OWNER)
        ResponseEntity<MemberDO> responseEntity2 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getUserMemberByUserId(anyInt(), anyInt())).thenReturn(responseEntity2)

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

    def "Create"() {
        given: '初始化参数'
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        devopsServiceMapper.insert(devopsServiceDO)
        devopsIngressMapper.insert(devopsIngressDO)
        devopsIngressPathMapper.insert(devopsIngressPathDO)
        devopsEnvCommandMapper.insert(devopsEnvCommandDO)
        devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO)

        and: '创建证书'
        CertificationDO certificationDO = new CertificationDO()
        certificationDO.setId(1L)
        certificationDO.setName("cert")
        certificationDO.setStatus(CertificationStatus.ACTIVE.getStatus())
        devopsCertificationMapper.insert(certificationDO)

        and: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null

        when: '项目下创建域名'
        restTemplate.postForEntity("/v1/projects/1/ingress?envId=1", devopsIngressDTO, Object.class)

        then: '校验返回值'
        devopsIngressMapper.selectAll().get(0)["name"] == "ingdo"
    }

    def "Update"() {
        given: '初始化DTO类'
        devopsIngressPathDTO = new DevopsIngressPathDTO()
        devopsIngressPathDTO.setPath("/bootz")
        devopsIngressPathDTO.setServiceId(1L)
        devopsIngressPathDTO.setServicePort(7777L)
        devopsIngressPathDTO.setServiceName("test")
        devopsIngressPathDTO.setServiceStatus("running")
        List<DevopsIngressPathDTO> pathList = new ArrayList<>()
        pathList.add(devopsIngressPathDTO)
        // 修改后的DTO
        DevopsIngressDTO newDevopsIngressDTO = new DevopsIngressDTO()
        newDevopsIngressDTO.setId(1L)
        newDevopsIngressDTO.setEnvId(1L)
        newDevopsIngressDTO.setCertId(1L)
        newDevopsIngressDTO.setCertName("newcertname")
        newDevopsIngressDTO.setName("ingdo")
        newDevopsIngressDTO.setPathList(pathList)
        newDevopsIngressDTO.setDomain("test.test-test.test")

        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        gitUtil.cloneBySsh(_ as String, _ as String) >> null

        when: '项目下更新域名'
        restTemplate.put("/v1/projects/1/ingress/1", newDevopsIngressDTO, Object.class)

        then: '校验返回值'
        devopsIngressMapper.selectByPrimaryKey(1L).getDomain() == "test.test-test.test"
    }

    def "QueryDomainId"() {
        when: '项目下查询域名'
        def dto = restTemplate.getForObject("/v1/projects/1/ingress/1", DevopsIngressDTO.class)

        then: '校验返回值'
        dto["domain"] == "test.test-test.test"
    }

    def "Delete"() {
        given: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null

        when: '项目下删除域名'
        restTemplate.delete("/v1/projects/1/ingress/1")

        then: '校验返回值'
        devopsEnvCommandRepository.queryByObject("ingress", 1L).getCommandType() == "delete"
    }

    def "CheckName"() {
        when: '检查域名唯一性'
        boolean exist = restTemplate.getForObject("/v1/projects/1/ingress/check_name?name=test&envId=1", Boolean.class)

        then: '校验返回值'
        exist
    }

    def "CheckDomain"() {
        when: '检查域名名称唯一性'
        def exist = restTemplate.getForObject("/v1/projects/1/ingress/check_domain?envId=1&domain=test.test&path=testpath&id=1", Boolean.class)

        then: '校验返回值'
        exist
    }

    def "ListByEnv"() {
        given: '初始化请求头'
        String infra = "{\"searchParam\":{\"name\":[\"test\"]}}"

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        Map<String, EnvSession> envs = new HashMap<>()
        EnvSession envSession = new EnvSession()
        envSession.setVersion("0.10.0")
        envSession.setClusterId(1L)
        envs.put("testenv", envSession)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envListener.connectedEnv() >> envs
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when: '环境总览域名查询'
        def page = restTemplate.postForObject("/v1/projects/1/ingress/1/listByEnv", strEntity, Page.class)

        then: '校验返回值'
        page.size() == 1

        and: '清理数据'

        // 删除cert
        List<CertificationDO> list = devopsCertificationMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (CertificationDO e : list) {
                devopsCertificationMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDO> list1 = devopsEnvironmentMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvironmentDO e : list1) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除ingress
        List<DevopsIngressDO> list2 = devopsIngressMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (DevopsIngressDO e : list2) {
                devopsIngressMapper.delete(e)
            }
        }
        // 删除service
        List<DevopsServiceDO> list3 = devopsServiceMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (DevopsServiceDO e : list3) {
                devopsServiceMapper.delete(e)
            }
        }
        // 删除envCommand
        List<DevopsEnvCommandDO> list4 = devopsEnvCommandMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (DevopsEnvCommandDO e : list4) {
                devopsEnvCommandMapper.delete(e)
            }
        }
        // 删除envFileResource
        List<DevopsEnvFileResourceDO> list5 = devopsEnvFileResourceMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (DevopsEnvFileResourceDO e : list5) {
                devopsEnvFileResourceMapper.delete(e)
            }
        }
        // 删除ingressPath
        List<DevopsIngressPathDO> list6 = devopsIngressPathMapper.selectAll()
        if (list6 != null && !list6.isEmpty()) {
            for (DevopsIngressPathDO e : list6) {
                devopsIngressPathMapper.delete(e)
            }
        }
        FileUtil.deleteDirectory(new File("gitops"))
    }
}
