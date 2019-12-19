package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.asgard.saga.producer.TransactionalProducer
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.AppServiceUpdateDTO
import io.choerodon.devops.api.vo.DevopsPvcReqVO
import io.choerodon.devops.api.vo.DevopsPvcRespVO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.*
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.DevopsProjectDTO
import io.choerodon.devops.infra.dto.DevopsPvDTO
import io.choerodon.devops.infra.dto.DevopsPvcDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.devops.infra.mapper.DevopsProjectMapper
import io.choerodon.devops.infra.mapper.DevopsPvMapper
import io.choerodon.devops.infra.mapper.DevopsPvcMapper
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

import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.ArgumentMatchers.anyLong
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/3
 * Time: 20:27
 * Description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(AppServiceController)
@Stepwise
class DevopsPvcControllerSpec extends Specification {

    private static final String MAPPING = "/v1/projects/{project_id}/pvc"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private UserAttrService userAttributeService
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private DevopsProjectService devopsProjectService
    @Autowired
    protected DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator
    @Autowired
    DevopsPvcService devopsPvcService

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService
    @Autowired
    private UserAttrService userAttrService
    @Autowired
    ClusterConnectionHandler clusterConnectionHandler
    @Autowired
    private DevopsPvcMapper devopsPvcMapper
    @Autowired
    private DevopsPvMapper devopsPvMapper


    TransactionalProducer producer = Mockito.mock(TransactionalProducer.class)
    BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    Long project_id = 1L
    @Shared
    Long app_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false
    @Shared
    Long harborConfigId = 1L
    @Shared
    Long chartConfigId = 2L

    def setupSpec() {
        given:
        Map<String, Object> params = new HashMap<>()
        params.put("code", "app")
        searchParam.put("searchParam", params)
        searchParam.put("param", [])
    }

    def setup() {

        if (isToInit) {

            ProjectDTO projectDTO = new ProjectDTO()
            projectDTO.setId(1L)
            projectDTO.setName("pro")
            projectDTO.setOrganizationId(1L)
            Mockito.doReturn(new ResponseEntity(projectDTO, HttpStatus.OK)).when(baseServiceClient).queryIamProject(1L)

            OrganizationDTO organizationDTO = new OrganizationDTO()
            organizationDTO.setId(1L)
            organizationDTO.setCode("testOrganization")
            ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
            Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(1L)

            List<RoleVO> roleDTOList = new ArrayList<>()
            RoleVO roleDTO = new RoleVO()
            roleDTO.setCode("role/project/default/project-owner")
            roleDTOList.add(roleDTO)
            List<ProjectWithRoleVO> projectWithRoleDTOList = new ArrayList<>()
            ProjectWithRoleVO projectWithRoleDTO = new ProjectWithRoleVO()
            projectWithRoleDTO.setName("pro")
            projectWithRoleDTO.setRoles(roleDTOList)
            projectWithRoleDTOList.add(projectWithRoleDTO)
            PageInfo<ProjectWithRoleVO> projectWithRoleDTOPage = new PageInfo(projectWithRoleDTOList)
            ResponseEntity<PageInfo<ProjectWithRoleVO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
            Mockito.doReturn(pageResponseEntity).when(baseServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

            DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()
            devopsEnvironmentDTO.setId(1)
            devopsEnvironmentDTO.setName("test-env")
            devopsEnvironmentMapper.insert(devopsEnvironmentDTO)
        }
    }

    def cleanup() {
        if (isToClean) {
            // 删除env
            devopsEnvironmentMapper.selectAll().forEach { devopsEnvironmentMapper.delete(it) }
            // 删除PVC
            devopsPvcMapper.selectAll().forEach { devopsPvcMapper.delete(it) }
            // 删除PV
            devopsPvMapper.selectAll().forEach { devopsPvMapper.delete(it) }
            //删除所有project
            devopsProjectMapper.delete(new DevopsProjectDTO())
        }
    }

    // 项目下创建PVC
    def "create"() {
        given: '创建devopsPvcReqVO'
        isToInit = false
        DevopsPvcReqVO devopsPvcReqVO = new DevopsPvcReqVO()
        devopsPvcReqVO.setPvId(1)
        devopsPvcReqVO.setRequestResource("1Gi")
        devopsPvcReqVO.setEnvId(1)
        devopsPvcReqVO.setAccessModes("ReadWriteMany")
        devopsPvcReqVO.setName("pvc-unit-test")

        and: '创建PV'
        DevopsPvDTO devopsPvDTO = new DevopsPvDTO()
        devopsPvDTO.setId(1)
        devopsPvDTO.setName("pv-test")
        devopsPvMapper.insert(devopsPvDTO)

        when: '创建一个PVC'
        def entity = restTemplate.postForEntity(MAPPING, devopsPvcReqVO, DevopsPvcRespVO.class, project_id)

        then: '校验结果'
        entity.statusCode.is2xxSuccessful()
        entity.getBody().getId() == 1L
        devopsPvcMapper.selectByPrimaryKey(entity.getBody().getId()) != null
    }

    // 查询PVC
    def "page_by_option"() {
        when:
        def entity = restTemplate.postForEntity(MAPPING + "/page_by_options?env_id={env_id}", searchParam, PageInfo.class, project_id, 1)
        then:
        entity.statusCode.is2xxSuccessful()
        entity.getBody().size != 0
    }

    // 检查PVC名称唯一性
    def "check_name"() {
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/check_name?env_id={env_id}&name={name}", ExceptionResponse, project_id, 1, "pvc-unit-test",)
        then:
        entity.statusCode.is2xxSuccessful()
        entity.getBody().getCode() == "error.pvc.name.already.exists"
    }

    // 删除PVC
    def "delete"() {
        when: '删除PVC'
        restTemplate.delete(MAPPING + "/{envId}/{pvc_id}", project_id, 1, 1)
        then: '校验结果'
        devopsPvcMapper.selectAll().size() == 0
    }

    // 清除测试数据
    def "cleanupData"() {
        given:
        isToClean = true
    }
}
