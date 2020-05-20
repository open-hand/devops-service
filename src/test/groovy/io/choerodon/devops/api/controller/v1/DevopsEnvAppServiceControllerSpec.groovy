package io.choerodon.devops.api.controller.v1

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import com.alibaba.fastjson.JSONArray
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.AppServiceRepVO
import io.choerodon.devops.api.vo.DevopsEnvAppServiceVO
import io.choerodon.devops.api.vo.DevopsEnvPortVO
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.mapper.*

/**
 * @author: trump* @date: 2019/8/20 19:50
 * @description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvAppServiceController)
@Stepwise
class DevopsEnvAppServiceControllerSpec extends Specification {
    private String rootUrl = "/v1/projects/{project_id}/env/app_services"

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private AppServiceMapper appServiceMapper
    @Autowired
    private DevopsEnvAppServiceMapper devopsEnvAppServiceMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper
    @Autowired
    private DevopsEnvResourceMapper devopsEnvResourceMapper
    @Autowired
    private DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper

    @Shared
    private DevopsEnvResourceDTO devopsEnvResourceDTO
    @Shared
    private DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO
    @Shared
    private AppServiceInstanceDTO appServiceInstanceDTO
    @Shared
    private DevopsProjectDTO devopsProjectDTO
    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDTO
    @Shared
    private AppServiceDTO appServiceDTO
    @Shared
    private AppServiceDTO appServiceDTO1
    private projectId = 1L
    private appId = 1L
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false

    def setup() {
        if (isToInit) {
            devopsProjectDTO = new DevopsProjectDTO()
            devopsProjectDTO.setIamProjectId(projectId)
            devopsProjectDTO.setAppId(1L)
            devopsProjectMapper.insertSelective(devopsProjectDTO)

            devopsEnvironmentDTO = new DevopsEnvironmentDTO()
            devopsEnvironmentDTO.setId(1L)
            devopsEnvironmentDTO.setProjectId(projectId)
            devopsEnvironmentDTO.setCode("env-code")
            devopsEnvironmentDTO.setName("env-name")
            devopsEnvironmentMapper.insertSelective(devopsEnvironmentDTO)

            appServiceDTO = new AppServiceDTO()
            appServiceDTO.setId(1L)
            appServiceDTO.setCode("testapp-0110")
            appServiceDTO.setName("testapp-0110")
            appServiceDTO.setProjectId(appId)
            appServiceDTO.setSynchro(true)
            appServiceDTO.setActive(true)
            appServiceMapper.insertSelective(appServiceDTO)

            appServiceDTO1 = new AppServiceDTO()
            appServiceDTO1.setId(2L)
            appServiceDTO1.setCode("testapp-01102")
            appServiceDTO1.setName("testapp-01102")
            appServiceDTO1.setProjectId(appId)
            appServiceDTO1.setSynchro(true)
            appServiceDTO1.setActive(true)
            appServiceMapper.insertSelective(appServiceDTO1)

            appServiceInstanceDTO = new AppServiceInstanceDTO()
            appServiceInstanceDTO.setId(1L)
            appServiceInstanceDTO.setAppServiceId(1L)
            appServiceInstanceDTO.setEnvId(devopsEnvironmentDTO.getId())
            appServiceInstanceMapper.insert(appServiceInstanceDTO)

            devopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO()
            devopsEnvResourceDetailDTO.setId(1L)
            devopsEnvResourceDetailDTO.setMessage("{\"kind\":\"Deployment\",\"apiVersion\":\"apps/v1\",\"metadata\":{\"name\":\"testlog-zzy\",\"namespace\":\"envtestenv\",\"selfLink\":\"/apis/apps/v1/namespaces/envtestenv/deployments/testlog-zzy\",\"uid\":\"f6de98f7-c932-11e9-b783-5254020234ec\",\"resourceVersion\":\"84696987\",\"generation\":1,\"creationTimestamp\":\"2019-08-28T01:27:13Z\",\"labels\":{\"choerodon.io\":\"2019.8.19-203424-master\",\"choerodon.io/application\":\"testlog\",\"choerodon.io/logs-parser\":\"spring-boot\",\"choerodon.io/release\":\"testlog-zzy\",\"choerodon.io/version\":\"2019.8.28-091519-master\"},\"annotations\":{\"deployment.kubernetes.io/revision\":\"1\"}},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"choerodon.io/release\":\"testlog-zzy\"}},\"template\":{\"metadata\":{\"creationTimestamp\":null,\"labels\":{\"choerodon.io\":\"2019.8.19-203424-master\",\"choerodon.io/application\":\"testlog\",\"choerodon.io/metrics-port\":\"18081\",\"choerodon.io/release\":\"testlog-zzy\",\"choerodon.io/service\":\"testlog\",\"choerodon.io/version\":\"2019.8.28-091519-master\"},\"annotations\":{\"choerodon.io/metrics-group\":\"spring-boot\",\"choerodon.io/metrics-path\":\"/prometheus\"}},\"spec\":{\"containers\":[{\"name\":\"testlog-zzy\",\"image\":\"registry.saas.hand-china.com/testorg0110-testpro0110/testlog:2019.8.28-091519-master\",\"ports\":[{\"name\":\"http\",\"containerPort\":18080,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"EUREKA_CLIENT_SERVICEURL_DEFAULTZONE\",\"value\":\"http://go-register-server-b468b0.c7nf-uat:8000/eureka/\"},{\"name\":\"SPRING_CLOUD_CONFIG_URI\",\"value\":\"http://config-server.framework:8010/\"},{\"name\":\"SPRING_DATASOURCE_PASSWORD\",\"value\":\"123456\"},{\"name\":\"SPRING_DATASOURCE_URL\",\"value\":\"jdbc:mysql://localhost::3306/demo_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\"},{\"name\":\"SPRING_DATASOURCE_USERNAME\",\"value\":\"choerodon\"}],\"resources\":{\"limits\":{\"memory\":\"2Gi\"},\"requests\":{\"memory\":\"1536Mi\"}},\"readinessProbe\":{\"exec\":{\"command\":[\"/bin/sh\",\"-c\",\"curl -s localhost:18081/health --fail \\u0026\\u0026 nc -z localhost 18080\"]},\"initialDelaySeconds\":60,\"timeoutSeconds\":10,\"periodSeconds\":10,\"successThreshold\":1,\"failureThreshold\":3},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"securityContext\":{},\"imagePullSecrets\":[{\"name\":\"registry-secret-25-254e0\"}],\"schedulerName\":\"default-scheduler\"}},\"strategy\":{\"type\":\"RollingUpdate\",\"rollingUpdate\":{\"maxUnavailable\":\"25%\",\"maxSurge\":\"25%\"}},\"revisionHistoryLimit\":10,\"progressDeadlineSeconds\":600},\"status\":{\"observedGeneration\":1,\"replicas\":1,\"updatedReplicas\":1,\"readyReplicas\":1,\"availableReplicas\":1,\"conditions\":[{\"type\":\"Available\",\"status\":\"True\",\"lastUpdateTime\":\"2019-08-28T01:28:26Z\",\"lastTransitionTime\":\"2019-08-28T01:28:26Z\",\"reason\":\"MinimumReplicasAvailable\",\"message\":\"Deployment has minimum availability.\"},{\"type\":\"Progressing\",\"status\":\"True\",\"lastUpdateTime\":\"2019-08-28T01:28:26Z\",\"lastTransitionTime\":\"2019-08-28T01:27:13Z\",\"reason\":\"NewReplicaSetAvailable\",\"message\":\"ReplicaSet \\\"testlog-zzy-78c96bf788\\\" has successfully progressed.\"}]}}")
            devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDTO)

            devopsEnvResourceDTO = new DevopsEnvResourceDTO()
            devopsEnvResourceDTO.setId(1L)
            devopsEnvResourceDTO.setEnvId(devopsEnvironmentDTO.getId())
            devopsEnvResourceDTO.setKind("Deployment")
            devopsEnvResourceDTO.setInstanceId(appServiceInstanceDTO.getId())
            devopsEnvResourceDTO.setResourceDetailId(devopsEnvResourceDetailDTO.getId())
            devopsEnvResourceMapper.insert(devopsEnvResourceDTO)
        }
    }

    def cleanup() {
        if (isToClean) {
            devopsEnvironmentMapper.delete(null)
            appServiceMapper.delete(null)
            devopsEnvAppServiceMapper.delete(null)
            devopsProjectMapper.delete(null)
        }
    }

    def "BatchCreate"() {
        given: '准备'
        isToInit = false
        def url = rootUrl + "/batch_create"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        DevopsEnvAppServiceVO request = new DevopsEnvAppServiceVO()
        request.setEnvId(devopsEnvironmentDTO.getId())
        Long[] ids = new Long[2]
        ids[0] = appServiceDTO.getId()
        ids[1] = appServiceDTO1.getId()
        request.setAppServiceIds(ids)

        when: '批量创建'
        restTemplate.postForObject(url, request, String.class, params)
        then: '校验结果'
        devopsEnvAppServiceMapper.selectAll().size() == 2
    }

    def "delete"() {
        given: '准备'
        def url = rootUrl + "?env_id={env_id}&app_service_id={app_service_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("env_id", devopsEnvironmentDTO.getId())
        params.put("app_service_id", appServiceDTO1.getId())

        when: '删除'
        restTemplate.delete(url, params)

        then: '校验结果'
        devopsEnvAppServiceMapper.selectAll().size() == 1
    }

    def "listAppServicesInEnv"() {
        given: '准备'
        def url = rootUrl + "/list_by_env?env_id={env_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("env_id", devopsEnvironmentDTO.getId())

        when: '查询'
        def resp = JSONArray.parseArray(restTemplate.getForObject(url, String.class, params), AppServiceRepVO.class)

        then: '校验结果'
        resp != null
        resp.size() == 1
        resp.get(0).getId() == appServiceDTO.getId()
    }

    def "ListLabelByAppAndEnvId"() {
        given: '准备'
        def url = rootUrl + "/list_label?env_id={env_id}&app_service_id={app_service_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("env_id", devopsEnvironmentDTO.getId())
        params.put("app_service_id", appServiceDTO.getId())

        when: '查询'
        def resp = JSONArray.parseArray(restTemplate.getForObject(url, String.class, params), Map.class)

        then: '校验结果'
        resp != null
        resp.size() == 1
        resp.get(0).size() == 5
    }

    def "ListPortByAppAndEnvId"() {
        given: '准备'
        def url = rootUrl + "/list_port?env_id={env_id}&app_service_id={app_service_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("env_id", devopsEnvironmentDTO.getId())
        params.put("app_service_id", appServiceDTO.getId())

        when: '查询'
        def resp = JSONArray.parseArray(restTemplate.getForObject(url, String.class, params), DevopsEnvPortVO.class)

        then: '校验结果'
        resp != null
        resp.size() == 1
    }

    def "ListNonRelatedAppService"() {
        given: '准备'
        isToClean = true
        def url = rootUrl + "/non_related_app_service?env_id={env_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("env_id", devopsEnvironmentDTO.getId())

        when: '查询'
        def resp = JSONArray.parseArray(restTemplate.getForObject(url, String.class, params), AppServiceRepVO.class)

        then: '校验结果'
        resp != null
        resp.size() == 1
        resp.get(0).getId() == appServiceDTO1.getId()
    }
}
