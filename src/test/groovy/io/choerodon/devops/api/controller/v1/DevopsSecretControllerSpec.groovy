package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.core.domain.Page
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.SecretRepDTO
import io.choerodon.devops.api.vo.SecretReqDTO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.vo.iam.RoleDTO
import io.choerodon.devops.app.service.DevopsEnvironmentService
import io.choerodon.devops.app.service.impl.DevopsSecretServiceImpl
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository
import io.choerodon.devops.domain.application.repository.GitlabRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.RepositoryFile
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.DevopsEnvFileResourceDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.dataobject.DevopsSecretDO
import io.choerodon.devops.infra.dataobject.gitlab.MemberDTO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.DevopsEnvFileResourceMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.devops.infra.mapper.DevopsSecretMapper
import io.choerodon.websocket.helper.EnvListener
import org.mockito.Mockito
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

import static org.mockito.Matchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 18-12-6
 * Time: 上午10:51
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsSecretController)
@Stepwise
class DevopsSecretControllerSpec extends Specification {

    private static final String MAPPING = "/v1/projects/{project_id}/secret"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsSecretServiceImpl devopsSecretServiceImpl
    @Autowired
    private DevopsSecretMapper devopsSecretMapper
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil

    @Autowired
    private IamRepository iamRepository
    @Autowired
    private GitlabRepository gitlabRepository
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    DevopsEnvironmentService devopsEnvironmentService = Mockito.mock(DevopsEnvironmentService.class)
    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    @Shared
    private DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
    @Shared
    private DevopsEnvFileResourceDO devopsEnvFileResourceDO = new DevopsEnvFileResourceDO()

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClient", gitlabServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryIamProject(anyLong())).thenReturn(responseEntity)

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

        MemberDTO memberDO = new MemberDTO()
        memberDO.setAccessLevel(AccessLevel.OWNER)
        ResponseEntity<MemberDTO> responseEntity1 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getUserMemberByUserId(anyInt(), anyInt())).thenReturn(responseEntity1)

        RepositoryFile file = new RepositoryFile()
        file.setFilePath("filePath")
        ResponseEntity<RepositoryFile> responseEntity2 = new ResponseEntity<>(file, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createFile(anyInt(), anyString(), anyString(), anyString(), anyInt())).thenReturn(responseEntity2)

        Mockito.when(gitlabServiceClient.updateFile(anyInt(), anyString(), anyString(), anyString(), anyInt())).thenReturn(responseEntity2)

    }

    def setupSpec() {
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)

        devopsEnvFileResourceDO.setId(1L)
        devopsEnvFileResourceDO.setEnvId(1L)
        devopsEnvFileResourceDO.setResourceId(1L)
        devopsEnvFileResourceDO.setFilePath("sct-test1207a.yaml")
        devopsEnvFileResourceDO.setResourceType("Secret")

    }

    def "CreateOrUpdate"() {
        given: '初始化数据'
        FileUtil.copyFile("src/test/gitops/testSecret/sct-test1207a.yaml", "gitops/testSecret")
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO)

        and: '初始化DTO'
        SecretReqDTO secretReqDTO = new SecretReqDTO()
        secretReqDTO.setEnvId(1L)
        secretReqDTO.setName("secret")
        secretReqDTO.setDescription("des")
        secretReqDTO.setType("create")
        Map<String, String> valueMap = new HashMap<>()
        valueMap.put("test", "test")
        secretReqDTO.setValue(valueMap)

        and: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long) >> null

        when: '创建密钥'
        restTemplate.put(MAPPING, secretReqDTO, 1L)

        then: '校验结果'
        devopsSecretMapper.selectAll().get(0).getValue() == "{\"test\":\"dGVzdA\\u003d\\u003d\"}"

        when: '更新密钥但是key-value不改变'
        secretReqDTO.setId(1L)
        secretReqDTO.setType("update")
        restTemplate.put(MAPPING, secretReqDTO, 1L)

        then: '校验结果'
        devopsSecretMapper.selectAll().get(0).getValue() == "{\"test\":\"dGVzdA\\u003d\\u003d\"}"

        when: '更新密钥同时更新key-value'
        valueMap.put("testnew", "testnew")
        restTemplate.put(MAPPING, secretReqDTO, 1L)

        then: '校验结果'
        devopsSecretMapper.selectAll().size() == 1
    }

    def "ListByOption"() {
        given: '查询参数'
        String params = "{\"searchParam\":{},\"param\":\"\"}"

        when: '分页插叙'
        def page = restTemplate.postForEntity(MAPPING + "/1/list_by_option?page=0&size=10", params, Page.class, 1L)

        then: '校验结果'
        page.getBody().get(0)["name"] == "secret"
    }

    def "QuerySecret"() {
        when: '根据密钥id查询密钥'
        def dto = restTemplate.getForEntity(MAPPING + "/1", SecretRepDTO.class, 1L)

        then: '校验结果'
        dto.getBody()["name"] == "secret"
    }

    def "CheckName"() {
        when: '校验名字唯一性'
        restTemplate.getForEntity(MAPPING + "/check_name?secret_name=test1207a", Object.class, 1L)

        then: '校验结果'
        noExceptionThrown()
    }

    def "DeleteSecret"() {
        given: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long) >> null

        when: '删除密钥'
        restTemplate.delete(MAPPING + "/1/1", 1L)

        then: '校验结果'
        noExceptionThrown()

        and: '清理数据'
        // 删除secret
        List<DevopsSecretDO> list = devopsSecretMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (DevopsSecretDO e : list) {
                devopsSecretMapper.delete(e)
            }
        }
        // 删除envFileResource
        List<DevopsEnvFileResourceDO> list1 = devopsEnvFileResourceMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvFileResourceDO e : list1) {
                devopsEnvFileResourceMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDO> list2 = devopsEnvironmentMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (DevopsEnvironmentDO e : list2) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除gitops
        FileUtil.deleteDirectory(new File("gitops"))
    }

}
