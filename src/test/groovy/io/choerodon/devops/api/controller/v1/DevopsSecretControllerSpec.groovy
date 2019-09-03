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
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.SecretReqVO
import io.choerodon.devops.api.vo.SecretRespVO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.impl.DevopsSecretServiceImpl
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.DevopsSecretDTO
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.DevopsEnvFileResourceMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.devops.infra.mapper.DevopsSecretMapper
import io.choerodon.devops.infra.util.FileUtil

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
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler envUtil

    @Qualifier("mockBaseServiceClientOperator")
    @Autowired
    private BaseServiceClientOperator mockBaseServiceClientOperator

    @Qualifier("mockGitlabServiceClientOperator")
    @Autowired
    private GitlabServiceClientOperator mockGitlabServiceClientOperator

    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()
    @Shared
    private DevopsEnvFileResourceDTO devopsEnvFileResourceDO = new DevopsEnvFileResourceDTO()

    def setup() {
        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setName("pro")
        projectDO.setOrganizationId(1L)
        Mockito.when(mockBaseServiceClientOperator.queryIamProjectById(anyLong())).thenReturn(projectDO)

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

        MemberDTO memberDO = new MemberDTO()
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
        Mockito.when(mockGitlabServiceClientOperator.queryGroupMember(anyInt(), anyInt())).thenReturn(memberDO)

        IamUserDTO iamUserDTO = new IamUserDTO()
        iamUserDTO.setId(1L)
        iamUserDTO.setRealName("aa")

        Mockito.doReturn(iamUserDTO).when(mockBaseServiceClientOperator).queryUserByUserId(1L)
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
        SecretReqVO secretReqDTO = new SecretReqVO()
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

        when: '分页查询'
        def page = restTemplate.postForEntity(MAPPING + "/page_by_options?page=0&size=10", params, PageInfo.class, 1L)

        then: '校验结果'
        page.getBody().getList().get(0)["name"] == "secret"
    }

    def "QuerySecret"() {
        when: '根据密钥id查询密钥'
        def dto = restTemplate.getForEntity(MAPPING + "/1", SecretRespVO.class, 1L)

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
        List<DevopsSecretDTO> list = devopsSecretMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (DevopsSecretDTO e : list) {
                devopsSecretMapper.delete(e)
            }
        }
        // 删除envFileResource
        List<DevopsEnvFileResourceDTO> list1 = devopsEnvFileResourceMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvFileResourceDTO e : list1) {
                devopsEnvFileResourceMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDTO> list2 = devopsEnvironmentMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (DevopsEnvironmentDTO e : list2) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除gitops
        FileUtil.deleteDirectory(new File("gitops"))
    }

}
