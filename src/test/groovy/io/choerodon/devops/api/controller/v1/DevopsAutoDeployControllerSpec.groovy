package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsAutoDeployDTO
import io.choerodon.devops.api.dto.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.dto.iam.RoleDTO
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.DevopsAutoDeployDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.DevopsAutoDeployMapper
import io.choerodon.devops.infra.mapper.DevopsAutoDeployRecordMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.websocket.helper.EnvListener
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.ArgumentMatchers.anyLong
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:41 2019/3/4
 * Description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsAutoDeployController)
@Stepwise
class DevopsAutoDeployControllerSpec extends Specification {
    private static final String MAPPING = "/v1/{project_id}/auto_deploy"


    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsAutoDeployMapper devopsAutoDeployMapper
    @Autowired
    private DevopsAutoDeployRecordMapper devopsAutoDeployRecordMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private IamRepository iamRepository
    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil
    @Shared
    private boolean isToInit = true
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    ApplicationDO applicationDO = new ApplicationDO()
    @Shared
    DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
    @Shared
    private boolean isToClean = false



    def setup() {

        if (isToInit) {
            DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)

            ProjectDO projectDO = new ProjectDO()
            projectDO.setName("pro")
            projectDO.setOrganizationId(1L)
            ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
            Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)


            List<RoleDTO> roleDTOList = new ArrayList<>()
            RoleDTO roleDTO = new RoleDTO()
            roleDTO.setCode("role/project/default/project-owner")
            roleDTOList.add(roleDTO)
            List<ProjectWithRoleDTO> projectWithRoleDTOList = new ArrayList<>()
            ProjectWithRoleDTO projectWithRoleDTO = new ProjectWithRoleDTO()
            projectWithRoleDTO.setName("pro")
            projectWithRoleDTO.setRoles(roleDTOList)
            projectWithRoleDTOList.add(projectWithRoleDTO)
            Page<ProjectWithRoleDTO> projectWithRoleDTOPage = new Page<>()
            projectWithRoleDTOPage.setContent(projectWithRoleDTOList)
            projectWithRoleDTOPage.setTotalPages(2)
            ResponseEntity<Page<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
            Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())
            isToInit = false
        }
    }

    def setupSpec() {

        //app
        applicationDO.setId(1L)
        applicationDO.setProjectId(1L)
        applicationDO.setName("appName")
        applicationDO.setCode("appCode")
        applicationDO.setGitlabProjectId(1)
        applicationDO.setHarborConfigId(1L)
        applicationDO.setChartConfigId(2L)
        //env
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setClusterId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setName("envName")
        devopsEnvironmentDO.setEnvIdRsa("test")
        devopsEnvironmentDO.setCode("envCode")
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)

    }

    def cleanup() {
        if (isToClean) {
            // 删除env
            devopsEnvironmentMapper.selectAll().forEach { devopsEnvironmentMapper.delete(it) }
            // 删除app
            applicationMapper.selectAll().forEach { applicationMapper.delete(it) }
        }
    }

    def "create"() {
        given: '初始化数据'
        applicationMapper.insert(applicationDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        DevopsAutoDeployDTO autoDeployDTO = new DevopsAutoDeployDTO()

        and: '赋值'
        autoDeployDTO.setTaskName("task1")
        autoDeployDTO.setAppId(1L)
        autoDeployDTO.setEnvId(1L)
        autoDeployDTO.setProjectId(1L)
        List<String> versionList = new ArrayList<>()
        versionList.add("release")
        versionList.add("feature")
        autoDeployDTO.setTriggerVersion(versionList);
        autoDeployDTO.setValue("# Default values for api-gateway.\\n# This is a YAML-formatted file.\\n# Declare variables to be passed into your templates.\\n\\nreplicaCount: 1\\n\\nimage:\\n  repository: registry.choerodon.com.cn/choerodon-c7ncd/choerodon-front-devops\\n  pullPolicy: Always\\n\\npreJob:\\n  timeout: 300\\n  preConfig:\\n    enable: true\\n    enabledelete: true\\n    upattrs: sort\\n\\nservice:\\n  enable: false\\n  type: ClusterIP\\n  port: 80\\n  name: choerodon-front-devops\\n\\ningress:\\n  enable: false\\n  host: devops-service.choerodon.com.cn\\n\\nenv:\\n  open:\\n    PRO_API_HOST: api.staging.saas.hand-china.com\\n    PRO_DEVOPS_HOST: ws://devops-service-front.staging.saas.hand-china.com\\n    PRO_CLIENT_ID: devops\\n    PRO_LOCAL: true\\n    PRO_TITLE_NAME: Choerodon\\n    PRO_HEADER_TITLE_NAME: Choerodon1\\n    PRO_COOKIE_SERVER: choerodon.staging.saas.hand-china.com\\n    PRO_HTTP: http\\n    PRO_FILE_SERVER: //minio.staging.saas.hand-china.com\\n\\nmetrics:\\n  path: /prometheus\\n  group: nginx\\n\\nlogs:\\n  parser: nginx\\n\\nresources:\\n  # We usually recommend not to specify default resources and to leave this as a conscious\\n  # choice for the user. This also increases chances charts run on environments with little\\n  # resources,such as Minikube. If you do want to specify resources,uncomment the following\\n  # lines,adjust them as necessary,and remove the curly braces after 'resources:'.\\n  limits:\\n    # cpu: 100m\\n    # memory: 2Gi\\n  requests:\\n")

        when: '创建自动部署'
        def entity = restTemplate.postForEntity(MAPPING, autoDeployDTO, DevopsAutoDeployDTO.class, project_id)

        then: '校验结果'
        entity.statusCode.is2xxSuccessful()
        entity.getBody().getId() == 1L
        DevopsAutoDeployDO devopsAutoDeployDO = devopsAutoDeployMapper.selectByPrimaryKey(init_id)

        expect: '校验查询结果'
        devopsAutoDeployDO.getTaskName() == "task1"
    }

    def "update"() {
        given: '初始化数据'
        DevopsAutoDeployDTO autoDeployDTO = new DevopsAutoDeployDTO()

        and: '赋值'
        autoDeployDTO.setId(init_id)
        autoDeployDTO.setTaskName("task2")
        autoDeployDTO.setAppId(1L)
        autoDeployDTO.setEnvId(1L)
        autoDeployDTO.setProjectId(1L)
        List<String> versionList = new ArrayList<>()
        versionList.add("release")
        versionList.add("feature")
        autoDeployDTO.setTriggerVersion(versionList)
        autoDeployDTO.setValueId(1L)
        autoDeployDTO.setValue("# Default values for api-gateway.\\n# This is a YAML-formatted file.\\n# Declare variables to be passed into your templates.\\n\\nreplicaCount: 1\\n\\nimage:\\n  repository: registry.choerodon.com.cn/choerodon-c7ncd/choerodon-front-devops\\n  pullPolicy: Always\\n\\npreJob:\\n  timeout: 300\\n  preConfig:\\n    enable: true\\n    enabledelete: true\\n    upattrs: sort\\n\\nservice:\\n  enable: false\\n  type: ClusterIP\\n  port: 80\\n  name: choerodon-front-devops\\n\\ningress:\\n  enable: false\\n  host: devops-service.choerodon.com.cn\\n\\nenv:\\n  open:\\n    PRO_API_HOST: api.staging.saas.hand-china.com\\n    PRO_DEVOPS_HOST: ws://devops-service-front.staging.saas.hand-china.com\\n    PRO_CLIENT_ID: devops\\n    PRO_LOCAL: true\\n    PRO_TITLE_NAME: Choerodon\\n    PRO_HEADER_TITLE_NAME: Choerodon1\\n    PRO_COOKIE_SERVER: choerodon.staging.saas.hand-china.com\\n    PRO_HTTP: http\\n    PRO_FILE_SERVER: //minio.staging.saas.hand-china.com\\n\\nmetrics:\\n  path: /prometheus\\n  group: nginx\\n\\nlogs:\\n  parser: nginx\\n\\nresources:\\n  # We usually recommend not to specify default resources and to leave this as a conscious\\n  # choice for the user. This also increases chances charts run on environments with little\\n  # resources,such as Minikube. If you do want to specify resources,uncomment the following\\n  # lines,adjust them as necessary,and remove the curly braces after 'resources:'.\\n  limits:\\n    # cpu: 100m\\n    # memory: 2Gi\\n  requests:\\n")
        autoDeployDTO.setObjectVersionNumber(1L)

        when: '创建自动查询'
        def entity = restTemplate.postForEntity(MAPPING, autoDeployDTO, DevopsAutoDeployDTO.class, project_id)

        then: '校验结果'
        entity.statusCode.is2xxSuccessful()
        entity.getBody().getId() == 1L
        DevopsAutoDeployDO devopsAutoDeployDO = devopsAutoDeployMapper.selectByPrimaryKey(init_id)

        expect: '校验查询结果'
        devopsAutoDeployDO.getTaskName() == "task2"
    }

    def "pageByOptions"() {
        given: '初始化请求头'
        String infra = "{\"searchParam\":{},\"param\":\"\"}"
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> searchParam = new HttpEntity<String>(infra, headers)

        and: '数据准备'
        List<Long> connectedEnvList = new ArrayList<>()
        connectedEnvList.add(1L)
        List<Long> updateEnvList = new ArrayList<>()
        updateEnvList.add(1L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> connectedEnvList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> updateEnvList

        when: '分页查询应用部署'
        def page = restTemplate.postForObject(MAPPING + "/list_by_options?user_id=1&env_id=1&app_id=1", searchParam, Page.class, 1L)

        then: '返回值'
        page.size() == 1

        expect: '验证返回值'
        page.getContent().get(0)["appId"] == 1L
    }

    def "queryByProjectId"() {
        when: '分页查询应用部署'
        def list = restTemplate.getForObject(MAPPING + "/list?user_id=1", List.class, 1L)

        then: '返回值'
        list.size() == 1
    }

    def "queryById"() {
        when: '分页查询应用部署'
        def entity = restTemplate.getForEntity(MAPPING + "/{auto_deploy_id}/detail", DevopsAutoDeployDTO.class, 1L, init_id)

        then: '返回值'
        entity.getBody().getId() == 1L

        expect: '校验查询结果'
        entity.getBody().getTaskName() == "task2"
    }

    def "queryRecord"() {
        given: '初始化请求头'
        String infra = "{\"searchParam\":{},\"param\":\"\"}"
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> searchParam = new HttpEntity<String>(infra, headers)

        and: '数据准备'
        List<Long> connectedEnvList = new ArrayList<>()
        connectedEnvList.add(1L)
        List<Long> updateEnvList = new ArrayList<>()
        updateEnvList.add(1L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> connectedEnvList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> updateEnvList

        when: '分页查询应用部署'
        def page = restTemplate.postForObject(MAPPING + "/list_record_options?user_id=1&env_id=1&app_id=1&task_name=task3", searchParam, Page.class, 1L)

        then: '返回值'
        page.size() == 0
    }

    def "checkName"() {
        when: '分页查询应用部署'
        restTemplate.getForEntity(MAPPING + "/check_name&name=task2", ExceptionResponse.class, 1L)

        then: '返回值'
        notThrown(CommonException)
    }

    def "updateIsEnabled"() {
        when: '分页查询应用部署'
        restTemplate.put(MAPPING + "/{auto_deploy_id}?isEnabled=0", DevopsAutoDeployDTO.class, 1L, init_id)

        then: '返回值'
        devopsAutoDeployMapper.selectByPrimaryKey(init_id).getIsEnabled() == 0
    }

    def "delete"() {
        when: '分页查询应用部署'
        restTemplate.delete(MAPPING + "/{auto_deploy_id}", 1L, init_id)

        then: '返回值'
        devopsAutoDeployMapper.selectAll().size() == 0
    }

    // 清除测试数据
    def "cleanupData"() {
        given:
        isToClean = true
    }

}
