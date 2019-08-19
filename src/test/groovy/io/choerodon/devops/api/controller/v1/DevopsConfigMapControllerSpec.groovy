package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsConfigMapRespVO
import io.choerodon.devops.api.vo.DevopsConfigMapVO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.GitlabGroupMemberService
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.app.service.impl.DevopsConfigMapServiceImpl
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.RepositoryFileDTO
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.DevopsConfigMapMapper
import io.choerodon.devops.infra.mapper.DevopsEnvFileResourceMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.devops.infra.util.FileUtil
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

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsConfigMapController)
@Stepwise
class DevopsConfigMapControllerSpec extends Specification {

    private static final String MAPPING = "/v1/projects/{project_id}/config_maps"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsConfigMapServiceImpl devopsConfigMapServiceImpl
    @Autowired
    private DevopsConfigMapMapper devopsConfigMapMapper
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper
    @Autowired
    private IamService iamRepository
    @Autowired
    private GitlabServiceClientOperator gitlabRepository
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberRepository


    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler envUtil

    BaseServiceClient iamServiceClient = Mockito.mock(BaseServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)
    GitlabServiceClientOperator gitlabServiceClientOperator = Mockito.mock(GitlabServiceClientOperator)

    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()
    @Shared
    private DevopsEnvFileResourceDTO devopsEnvFileResourceDO = new DevopsEnvFileResourceDTO()
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false


    void setup() {
        if (!isToInit) {
            return
        }

        DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClientOperator", gitlabServiceClientOperator)

        ProjectDTO projectDTO = new ProjectDTO()
        projectDTO.setName("pro")
        projectDTO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDTO, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryIamProject(anyLong())).thenReturn(responseEntity)

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
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
        ResponseEntity<MemberDTO> responseEntity1 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(responseEntity1)

        RepositoryFileDTO file = new RepositoryFileDTO()
        file.setFilePath("filePath")
        ResponseEntity<RepositoryFileDTO> responseEntity2 = new ResponseEntity<>(file, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createFile(anyInt(), anyString(), anyString(), anyString(), anyInt())).thenReturn(responseEntity2)

        Mockito.when(gitlabServiceClient.updateFile(anyInt(), anyString(), anyString(), anyString(), anyInt())).thenReturn(responseEntity2)
    }

    def cleanup() {
        if (!isToClean) {
            return
        }

        DependencyInjectUtil.restoreDefaultDependency(iamRepository, "baseServiceClient")
        DependencyInjectUtil.restoreDefaultDependency(gitlabRepository, "gitlabServiceClient")
        DependencyInjectUtil.restoreDefaultDependency(gitlabGroupMemberRepository, "gitlabServiceClientOperator")
//        DependencyInjectUtil.restoreDefaultDependency(devopsConfigMapServiceImpl, "devopsEnvironmentService")

        // 删除secret
        devopsConfigMapMapper.selectAll().forEach { devopsConfigMapMapper.delete(it) }
        // 删除envFileResource
        devopsEnvFileResourceMapper.selectAll().forEach { devopsEnvFileResourceMapper.delete(it) }
        // 删除env
        devopsEnvironmentMapper.selectAll().forEach { devopsEnvironmentMapper.delete(it) }
        // 删除gitops
        FileUtil.deleteDirectory(new File("gitops"))
    }

    def setupSpec() {
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)

        devopsEnvFileResourceDO.setId(1L)
        devopsEnvFileResourceDO.setEnvId(1L)
        devopsEnvFileResourceDO.setResourceId(1L)
        devopsEnvFileResourceDO.setFilePath("configMap-test.yaml")
        devopsEnvFileResourceDO.setResourceType("ConfigMap")
    }

    def "Create"() {

        given: '初始化数据'
        isToInit = false

        FileUtil.copyFile("src/test/gitops/testConfigMap/configMap-test.yaml", "gitops/testConfigMap")
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO)

        and: '初始化DTO'
        DevopsConfigMapVO devopsConfigMapDTO = new DevopsConfigMapVO()
        devopsConfigMapDTO.setId(1L)
        devopsConfigMapDTO.setName("asdasdqqqq")
        devopsConfigMapDTO.setEnvId(1L)
        devopsConfigMapDTO.setDescription("ggggg")
        devopsConfigMapDTO.setType("create")
        Map<String, String> valueMap = new HashMap<>()
        valueMap.put("xxxx", "xxxxx")
        devopsConfigMapDTO.setValue(valueMap)

        and: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long) >> null
        envUtil.handDevopsEnvGitRepository(_ as Long, _ as String, _ as String) >> "src/test/gitops/testConfigMap"

        when: '创建'
        restTemplate.postForObject(MAPPING, devopsConfigMapDTO, Object.class, 1L)

        then: '校验结果'
        devopsConfigMapMapper.selectAll().get(0).getDescription() == "ggggg"

        when: '更新但是key-value不改变'
        devopsConfigMapDTO.setId(devopsConfigMapMapper.selectAll().get(0).getId())
        devopsConfigMapDTO.setType("update")
        devopsConfigMapDTO.setDescription("gggggnew")
        restTemplate.postForObject(MAPPING, devopsConfigMapDTO, Object.class, 1L)

        then: '校验结果'
        devopsConfigMapMapper.selectAll().get(0).getDescription() == "gggggnew"

        when: '更新同时更新key-value'
        valueMap.put("xxxxnew", "xxxxxnew")
        devopsConfigMapDTO.setValue(valueMap)
        restTemplate.postForObject(MAPPING, devopsConfigMapDTO, Object.class, 1L)

        then: '校验结果'
        devopsConfigMapMapper.selectAll().get(0).getValue().contains("xxxxnew")
    }

    def "CheckName"() {
        when: '校验名字唯一性'
        restTemplate.getForEntity(MAPPING + "/check_name?envId=1&configMapName=asdasd", Object.class, 1L)

        then: '校验结果'
        noExceptionThrown()
    }

    def "Query"() {
        when: '根据id查询'
        def dto = restTemplate.getForEntity(MAPPING + "/1", DevopsConfigMapRespVO.class, 1L)

        then: '校验结果'
        dto.getBody()["name"] == "asdasdqqqq"
    }

    def "ListByEnv"() {
        given: '查询参数'
        String params = "{\"searchParam\":{},\"param\":\"\"}"

        when: '分页插叙'
        def page = restTemplate.postForEntity(MAPPING + "/1/listByEnv?page=0&size=10", params, PageInfo.class, 1L)

        then: '校验结果'
        page.getBody().get(0)["name"] == "asdasdqqqq"
    }

    def "Delete"() {
        given: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long) >> null

        when: '删除密钥'
        restTemplate.delete(MAPPING + "/1/delete", 1L)

        then: '校验结果'
        noExceptionThrown()
    }

    def "clean data"() {
        given: "清理数据"
        isToClean = true
    }
}
