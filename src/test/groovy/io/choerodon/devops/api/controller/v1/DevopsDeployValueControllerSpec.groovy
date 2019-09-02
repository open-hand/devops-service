package io.choerodon.devops.api.controller.v1

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.ArgumentMatchers.eq

import com.alibaba.fastjson.JSONArray
import com.github.pagehelper.PageInfo
import org.powermock.api.mockito.PowerMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsDeployValueVO
import io.choerodon.devops.app.service.DevopsDeployValueService
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper
import io.choerodon.devops.infra.mapper.AppServiceMapper
import io.choerodon.devops.infra.mapper.DevopsDeployValueMapper
import io.choerodon.devops.infra.mapper.DevopsEnvUserPermissionMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper

/**
 *
 * @author zmf
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsDeployValueController)
@Stepwise
class DevopsDeployValueControllerSpec extends Specification {
    def rootUrl = "/v1/projects/{project_id}/deploy_value"
    def projectId = 1L
    def envId = 1L
    def appServiceId = 1L
    def userId = 1L

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private DevopsDeployValueService deployValueService
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler

    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper
    @Autowired
    private DevopsDeployValueMapper deployValueMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private AppServiceMapper appServiceMapper
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper

    private BaseServiceClientOperator mockBaseServiceClientOperator = PowerMockito.mock(BaseServiceClientOperator)

    @Shared
    private DevopsDeployValueDTO deployValueDTO
    @Shared
    private DevopsDeployValueDTO deployValueDTO2
    @Shared
    private AppServiceInstanceDTO appServiceInstanceDTO
    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDTO
    @Shared
    private AppServiceDTO appServiceDTO
    @Shared
    private DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false

    void setup() {
        if (isToInit) {
            DependencyInjectUtil.setAttribute(deployValueService, "baseServiceClientOperator", mockBaseServiceClientOperator)

            mock()

            devopsEnvironmentDTO = new DevopsEnvironmentDTO()
            devopsEnvironmentDTO.setId(envId)
            devopsEnvironmentDTO.setName("env-name")
            devopsEnvironmentDTO.setCode("env-code")
            devopsEnvironmentDTO.setSkipCheckPermission(true)
            devopsEnvironmentDTO.setFailed(false)
            devopsEnvironmentDTO.setActive(true)
            devopsEnvironmentDTO.setSynchro(true)
            devopsEnvironmentMapper.insert(devopsEnvironmentDTO)

            devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO()
            devopsEnvUserPermissionDTO.setEnvId(envId)
            devopsEnvUserPermissionDTO.setIamUserId(userId)
            devopsEnvUserPermissionDTO.setPermitted(true)
            devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDTO)

            appServiceDTO = new AppServiceDTO()
            appServiceDTO.setId(appServiceId)
            appServiceDTO.setName("app-name")
            appServiceDTO.setCode("app-code")
            appServiceDTO.setSynchro(true)
            appServiceDTO.setFailed(false)
            appServiceDTO.setActive(true)
            appServiceDTO.setAppId(1L)
            appServiceMapper.insert(appServiceDTO)

            deployValueDTO = new DevopsDeployValueDTO()
            deployValueDTO.setId(1L)
            deployValueDTO.setName("unique-name-1")
            deployValueDTO.setEnvId(envId)
            deployValueDTO.setAppServiceId(appServiceId)
            deployValueDTO.setProjectId(projectId)
            deployValueDTO.setDescription("desc2")
            deployValueDTO.setValue("value1")
            deployValueMapper.insert(deployValueDTO)

            deployValueDTO2 = new DevopsDeployValueDTO()
            deployValueDTO2.setId(2L)
            deployValueDTO2.setName("unique-name-2")
            deployValueDTO2.setEnvId(envId)
            deployValueDTO2.setAppServiceId(appServiceId)
            deployValueDTO2.setProjectId(projectId)
            deployValueDTO2.setDescription("desc3")
            deployValueDTO2.setValue("value2")
            deployValueMapper.insert(deployValueDTO2)

            appServiceInstanceDTO = new AppServiceInstanceDTO()
            appServiceInstanceDTO.setId(1L)
            appServiceInstanceDTO.setCode("code")
            appServiceInstanceDTO.setProjectId(projectId)
            appServiceInstanceDTO.setValueId(deployValueDTO.getId())
            appServiceInstanceMapper.insert(appServiceInstanceDTO)
        }
    }

    void cleanup() {
        if (isToClean) {
            DependencyInjectUtil.restoreDefaultDependency(deployValueService, "baseServiceClientOperator")

            deployValueMapper.delete(null)
            appServiceInstanceMapper.delete(null)
            appServiceMapper.delete(null)
            devopsEnvironmentMapper.delete(null)
            devopsEnvUserPermissionMapper.delete(null)
        }
    }

    def mock() {
        PowerMockito.when(clusterConnectionHandler.getUpdatedEnvList()).thenReturn([1])

        PowerMockito.when(mockBaseServiceClientOperator.queryIamProjectById(eq(1))).thenReturn(new ProjectDTO())
        PowerMockito.when(mockBaseServiceClientOperator.isProjectOwner(anyLong(), any(ProjectDTO))).thenReturn(true)

        IamUserDTO iamUserDTO = new IamUserDTO()
        PowerMockito.when(mockBaseServiceClientOperator.queryUserByUserId(anyLong())).thenReturn(iamUserDTO)
    }

    def "CreateOrUpdate"() {
        given: "准备"
        isToInit = false
        def url = rootUrl
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)

        DevopsDeployValueVO request = new DevopsDeployValueVO()
        request.setName("unique-name")
        request.setEnvId(envId)
        request.setAppServiceId(appServiceId)
        request.setProjectId(projectId)
        request.setDescription("desc")
        request.setValue("value")

        when: "创建部署配置"
        def response = restTemplate.postForObject(url, request, DevopsDeployValueVO.class, params)

        then: "校验结果"
        response != null
        response.getId() != null
        deployValueMapper.selectByPrimaryKey(response.getId()) != null

        when: "更新部署配置"
        request = response
        request.setValue("update value")
        response = restTemplate.postForObject(url, request, DevopsDeployValueVO.class, params)

        then: "校验结果"
        response.getObjectVersionNumber() > request.getObjectVersionNumber()
        deployValueMapper.selectByPrimaryKey(response.getId()) != null
        deployValueMapper.selectByPrimaryKey(response.getId()).getValue() == request.getValue()
    }

    def "PageByOptions"() {
        given: "准备"
        def url = rootUrl + "/page_by_options?page={page}&size={size}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("page", 1)
        params.put("size", 10)

        when: "发送请求"
        def page = restTemplate.postForObject(url, null, PageInfo.class, params)

        then: "校验结果"
        page != null
        page.getList().size() == 3
        page.getTotal() == 3
    }

    def "Query"() {
        given: "准备"
        def url = rootUrl + "?value_id={value_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("value_id", deployValueDTO.getId())

        when: "发送请求"
        def resp = restTemplate.getForObject(url, DevopsDeployValueVO, params)

        then: "校验结果"
        resp != null
        resp.getId() == deployValueDTO.getId()
    }

    def "CheckDelete"() {
        given: "准备"
        def url = rootUrl + "/check_delete?value_id={value_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("value_id", deployValueDTO.getId())

        when: "发送请求校验不通过"
        def resp = restTemplate.getForObject(url, Boolean.class, params)

        then: "校验结果"
        !resp


        when: "发送请求校验通过"
        params.put("value_id", deployValueDTO2.getId())
        resp = restTemplate.getForObject(url, Boolean.class, params)

        then: "校验结果"
        resp
    }

    def "Delete"() {
        given: "准备"
        def url = rootUrl + "?value_id={value_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("value_id", deployValueDTO2.getId())

        when: "发送请求"
        restTemplate.delete(url, params)

        then: "校验结果"
        deployValueMapper.selectByPrimaryKey(deployValueDTO2.getId()) == null
    }

    def "CheckName"() {
        given: "准备"
        def url = rootUrl + "/check_name?name={name}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("name", deployValueDTO.getName())

        when: "发送请求，校验重复的名称"
        def resp = restTemplate.getForObject(url, ExceptionResponse.class, params)

        then: "校验结果"
        resp != null


        when: "发送请求，校验不重复的名称"
        params.put("name", UUID.randomUUID().toString())
        resp = restTemplate.getForObject(url, ExceptionResponse.class, params)

        then: "校验结果"
        resp == null
    }

    def "ListByEnvAndApp"() {
        given: "准备"
        isToClean = false
        def url = rootUrl + "/list_by_env_and_app?app_service_id={app_service_id}&env_id={env_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("app_service_id", deployValueDTO.getAppServiceId())
        params.put("env_id", deployValueDTO.getEnvId())

        when: "发送请求"
        def resp = JSONArray.parseArray(restTemplate.getForObject(url, String.class, params), DevopsDeployValueVO.class)

        then: "校验结果"
        resp != null
        resp.size() == 2
        resp.get(0).getId() == deployValueDTO.getId()
    }
}
