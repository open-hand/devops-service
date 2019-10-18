package io.choerodon.devops.api.controller.v1

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.pagehelper.PageInfo
import org.powermock.api.mockito.PowerMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.*
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO
import io.choerodon.devops.app.service.GitlabGroupMemberService
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO
import io.choerodon.devops.infra.dto.gitlab.GitlabPipelineDTO
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.enums.InstanceStatus
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.*
import io.choerodon.devops.infra.util.ConvertUtils
import io.choerodon.devops.infra.util.FileUtil
import io.choerodon.devops.infra.util.GitUtil
import io.choerodon.devops.infra.util.JsonYamlConversionUtil

/**
 * Created by n!Ck
 * Date: 2018/11/9
 * Time: 16:13
 * Description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(AppServiceInstanceController)
@Stepwise
class AppServiceInstanceControllerSpec extends Specification {

    private static final String MAPPING = "/v1/projects/{project_id}/app_service_instances"
    private static final Long PROJECT_ID = 1L
    private static final String EMPTY_SEARCH_PARAM = "{\"params\":[],\"searchParam\":{}}"

    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler envUtil
//    @Autowired
//    @Qualifier("mockEnvListener")
//    private EnvListener envListener
    @Autowired
    @Qualifier("mockGitUtil")
    private GitUtil gitUtil
//    @Autowired
//    @Qualifier("mockCommandSender")
//    private CommandSender commandSender

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private AppServiceMapper appServiceMapper
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvResourceMapper devopsEnvResourceMapper
    @Autowired
    private AppServiceVersionMapper appServiceVersionMapper
    @Autowired
    private AppServiceInstanceMapper applicationInstanceMapper
    @Autowired
    private DevopsEnvCommandValueMapper devopsEnvCommandValueMapper
    @Autowired
    private DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper
    @Autowired
    private AppServiceVersionValueMapper applicationVersionValueMapper
    @Autowired
    private DevopsCommandEventMapper devopsCommandEventMapper
    @Autowired
    private DevopsEnvCommandLogMapper devopsEnvCommandLogMapper
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper
    @Autowired
    private DevopsEnvFileMapper devopsEnvFileMapper
    @Autowired
    private DevopsDeployValueMapper deployValueMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper

    @Shared
    private DevopsDeployValueDTO devopsDeployValueDTO

    @Shared
    private DevopsProjectDTO devopsProjectDTO

    @Autowired()
    @Qualifier("mockBaseServiceClientOperator")
    private BaseServiceClientOperator baseServiceClientOperator

    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    AppServiceDTO appServiceDTO = new AppServiceDTO()
    @Shared
    DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO()
    @Shared
    AppServiceShareRuleDTO appServiceShareRuleDTO = new AppServiceShareRuleDTO()
    @Shared
    DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO()
    @Shared
    private List<DevopsEnvResourceDTO> devopsEnvResources = new ArrayList<>(8)

    @Shared
    DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()
    @Shared
    AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO()
    @Shared
    AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO()
    @Shared
    DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO()
    @Shared
    AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO()

    @Shared
    private List<DevopsEnvResourceDetailDTO> resourceDetailDTOList = new ArrayList<>(8)

    @Shared
    DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO()
    @Shared
    DevopsCommandEventDTO devopsCommandEventDTO = new DevopsCommandEventDTO()
    @Shared
    DevopsEnvCommandLogDTO devopsEnvCommandLogDTO = new DevopsEnvCommandLogDTO()
    @Shared
    DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = new DevopsEnvFileResourceDTO()
    @Shared
    DevopsEnvFileDTO devopsEnvFileDTO = new DevopsEnvFileDTO()

    @Autowired
    @Qualifier("mockGitlabServiceClientOperator")
    private GitlabServiceClientOperator gitlabServiceClientOperator
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService

    @Shared
    def isToInit = true
    @Shared
    def isToClean = false

    def setup() {
        if (!isToInit) {
            return
        }

        mockData()
        insertMockData()
        mockBehavior()
    }

    def cleanup() {
        if (!isToClean) {
            return
        }

        devopsProjectMapper.delete(null)
        // 删除appInstance
        applicationInstanceMapper.delete(null)
        // 删除appMarket
        appServiceShareRuleMapper.delete(null)
        // 删除envPod
        devopsEnvPodMapper.delete(null)
        // 删除appVersion
        appServiceVersionMapper.delete(null)
        // 删除appVersionValue
        applicationVersionValueMapper.delete(null)
        // 删除app
        appServiceMapper.delete(null)
        // 删除env
        devopsEnvironmentMapper.delete(null)
        // 删除envCommand
        devopsEnvCommandMapper.delete(null)
        // 删除envCommandValue
        devopsEnvCommandValueMapper.delete(null)
        // 删除envFile
        devopsEnvFileMapper.delete(null)
        // 删除envFileResource
        devopsEnvFileResourceMapper.delete(null)
        // 删除envResource
        devopsEnvResourceMapper.delete(null)
        // 删除envResourceDetail
        devopsEnvResourceDetailMapper.delete(null)
        // 删除envUserPermission
        devopsEnvUserPermissionMapper.delete(null)
        // 删除commandEvent
        devopsCommandEventMapper.delete(null)
        // 删除envCommandLog
        devopsEnvCommandLogMapper.delete(null)
        deployValueMapper.delete(null)
        FileUtil.deleteDirectory(new File("gitops"))
    }

    def mockData() {
        DevopsEnvResourceDTO devopsEnvResourceDO = new DevopsEnvResourceDTO()
        DevopsEnvResourceDTO devopsEnvResourceDO2 = new DevopsEnvResourceDTO()
        DevopsEnvResourceDTO devopsEnvResourceDO3 = new DevopsEnvResourceDTO()
        DevopsEnvResourceDTO devopsEnvResourceDO4 = new DevopsEnvResourceDTO()
        DevopsEnvResourceDTO devopsEnvResourceDO5 = new DevopsEnvResourceDTO()
        DevopsEnvResourceDTO devopsEnvResourceDO6 = new DevopsEnvResourceDTO()
        DevopsEnvResourceDTO devopsEnvResourceDO7 = new DevopsEnvResourceDTO()
        DevopsEnvResourceDTO devopsEnvResourceDO8 = new DevopsEnvResourceDTO()

        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO = new DevopsEnvResourceDetailDTO()
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO2 = new DevopsEnvResourceDetailDTO()
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO3 = new DevopsEnvResourceDetailDTO()
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO4 = new DevopsEnvResourceDetailDTO()
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO5 = new DevopsEnvResourceDetailDTO()
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO6 = new DevopsEnvResourceDetailDTO()
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO7 = new DevopsEnvResourceDetailDTO()
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO8 = new DevopsEnvResourceDetailDTO()

        Map<String, Object> params = new HashMap<>()
        params.put("code", "app")
        searchParam.put("searchParam", "")
        searchParam.put("param", [])

        devopsProjectDTO = new DevopsProjectDTO()
        devopsProjectDTO.setIamProjectId(PROJECT_ID)
        devopsProjectDTO.setDevopsAppGroupId(1L)
        devopsProjectDTO.setDevopsEnvGroupId(1L)

        // da
        appServiceDTO.setId(1L)
        appServiceDTO.setProjectId(1L)
        appServiceDTO.setName("appName")
        appServiceDTO.setCode("appCode")
        appServiceDTO.setGitlabProjectId(1)

        // de
        devopsEnvironmentDTO.setId(1L)
        devopsEnvironmentDTO.setClusterId(1L)
        devopsEnvironmentDTO.setProjectId(1L)
        devopsEnvironmentDTO.setName("envName")
        devopsEnvironmentDTO.setEnvIdRsa("test")
        devopsEnvironmentDTO.setCode("envCode")
        devopsEnvironmentDTO.setGitlabEnvProjectId(1L)

        // dam
        appServiceShareRuleDTO.setShareLevel("pub")
        appServiceShareRuleDTO.setAppServiceId(appServiceDTO.getId())
        appServiceShareRuleDTO.setVersionType("master")

        // dav
        appServiceVersionDTO.setId(1L)
        appServiceVersionDTO.setAppServiceId(1L)
        appServiceVersionDTO.setValueId(1L)
        appServiceVersionDTO.setVersion("version")

        // dai
        appServiceInstanceDTO.setId(1L)
        appServiceInstanceDTO.setValueId(1L)
        appServiceInstanceDTO.setEnvId(1L)
        appServiceInstanceDTO.setAppServiceId(1L)
        appServiceInstanceDTO.setCommandId(1L)
        appServiceInstanceDTO.setAppServiceVersionId(1L)
        appServiceInstanceDTO.setStatus("running")
        appServiceInstanceDTO.setCode("appInsCode")
        appServiceInstanceDTO.setObjectVersionNumber(1L)

        // dp
        devopsEnvPodDTO.setId(1L)
        devopsEnvPodDTO.setReady(true)
        devopsEnvPodDTO.setInstanceId(appServiceInstanceDTO.getId())
        devopsEnvPodDTO.setStatus("Running")
        devopsEnvPodDTO.setNamespace(devopsEnvironmentDTO.getCode())
        devopsEnvPodDTO.setName("test-pod-123456-abcdef")

        // davv
        appServiceVersionValueDTO.setId(1L)
        appServiceVersionValueDTO.setValue("---\n" +
                "image:\n" +
                "  tag: \"0.1.0-dev.20180519090059\"\n" +
                "  repository: \"registry.saas.test.com/hand-rdc-choerodon/event-store-service\"\n" +
                "  pullPolicy: \"Always\"\n" +
                "replicaCount: 1\n" +
                "service:\n" +
                "  port: 9010\n" +
                "  enable: false\n" +
                "  type: \"ClusterIP\"\n" +
                "resources:\n" +
                "  requests:\n" +
                "    memory: \"2Gi\"\n" +
                "  limits:\n" +
                "    memory: \"3Gi\"\n" +
                "metrics:\n" +
                "  path: \"/prometheus\"\n" +
                "  label: \"java-spring\"\n" +
                "env:\n" +
                "  open:\n" +
                "    SPRING_CLOUD_CONFIG_URI: \"http://config-server.choerodon-devops-prod:8010/\"\n" +
                "    SPRING_CLOUD_STREAM_DEFAULT_BINDER: \"kafka\"\n" +
                "    SPRING_CLOUD_CONFIG_ENABLED: true\n" +
                "    SPRING_DATASOURCE_PASSWORD: \"handhand\"\n" +
                "    SPRING_DATASOURCE_URL: \"jdbc:mysql://hapcloud-mysql.db:3306/event_store_service?useUnicode=true&characterEncoding=utf-8&useSSL=false\"\n" +
                "    SPRING_DATASOURCE_USERNAME: \"root\"\n" +
                "    SPRING_KAFKA_BOOTSTRAP_SERVERS: \"kafka-0.kafka-headless.kafka.svc.cluster.local:9092,kafka-1.kafka-headless.kafka.svc.cluster.local:9092,kafka-2.kafka-headless.kafka.svc.cluster.local:9092\"\n" +
                "    SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS: \"kafka-0.kafka-headless.kafka.svc.cluster.local:9092,kafka-1.kafka-headless.kafka.svc.cluster.local:9092,kafka-2.kafka-headless.kafka.svc.cluster.local:9092\"\n" +
                "    SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES: \"zookeeper-0.zookeeper-headless.zookeeper.svc.cluster.local:2181,zookeeper-1.zookeeper-headless.zookeeper.svc.cluster.local:2181,zookeeper-2.zookeeper-headless.zookeeper.svc.cluster.local:2181\"\n" +
                "    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: \"http://register-server.choerodon-devops-prod:8000/eureka/\"\n" +
                "logs:\n" +
                "  parser: \"java-spring\"\n" +
                "preJob:\n" +
                "  preConfig:\n" +
                "    mysql:\n" +
                "      database: \"manager_service\"\n" +
                "      password: \"handhand\"\n" +
                "      port: 3306\n" +
                "      host: \"hapcloud-mysql.db\"\n" +
                "      username: \"root\"\n" +
                "  preInitDB:\n" +
                "    mysql:\n" +
                "      database: \"event_store_service\"\n" +
                "      password: \"handhand\"\n" +
                "      port: 3306\n" +
                "      host: \"hapcloud-mysql.db\"\n" +
                "      username: \"root\"\n" +
                "deployment:\n" +
                "  managementPort: 9011")

        devopsDeployValueDTO = new DevopsDeployValueDTO()
        devopsDeployValueDTO.setId(1L)
        devopsDeployValueDTO.setAppServiceId(appServiceDTO.getId())
        devopsDeployValueDTO.setEnvId(devopsEnvironmentDTO.getId())
        devopsDeployValueDTO.setEnvName(devopsEnvironmentDTO.getName())
        devopsDeployValueDTO.setProjectId(PROJECT_ID)
        devopsDeployValueDTO.setName("Staging部署配置")
        devopsDeployValueDTO.setValue(appServiceVersionValueDTO.getValue())

        // decv
        devopsEnvCommandValueDTO.setId(1L)
        devopsEnvCommandValueDTO.setValue(devopsDeployValueDTO.getValue())

        // cmd
        devopsEnvCommandDTO.setId(1L)
        devopsEnvCommandDTO.setValueId(devopsEnvCommandValueDTO.getId())
        devopsEnvCommandDTO.setObjectId(1L)
        devopsEnvCommandDTO.setError("error")
        devopsEnvCommandDTO.setObject("instance")
        devopsEnvCommandDTO.setStatus("operating")
        devopsEnvCommandDTO.setObjectVersionId(1L)
        devopsEnvCommandDTO.setCommandType("create")


        // deup
        devopsEnvUserPermissionDTO.setIamUserId(1L)
        devopsEnvUserPermissionDTO.setPermitted(true)
        devopsEnvUserPermissionDTO.setEnvId(1L)

        // der
        devopsEnvResourceDO.setId(1L)
        devopsEnvResourceDO.setKind("Pod")
        devopsEnvResourceDO.setInstanceId(1L)
        devopsEnvResourceDO.setResourceDetailId(1L)
        devopsEnvResourceDO.setName("test-pod-123456-abcdef")

        devopsEnvResourceDO2.setId(2L)
        devopsEnvResourceDO2.setKind("Deployment")
        devopsEnvResourceDO2.setInstanceId(1L)
        devopsEnvResourceDO2.setResourceDetailId(2L)
        devopsEnvResourceDO2.setName("test-deployment")

        devopsEnvResourceDO3.setId(3L)
        devopsEnvResourceDO3.setKind("Service")
        devopsEnvResourceDO3.setInstanceId(1L)
        devopsEnvResourceDO3.setResourceDetailId(3L)
        devopsEnvResourceDO3.setName("test-service")

        devopsEnvResourceDO4.setId(4L)
        devopsEnvResourceDO4.setKind("Ingress")
        devopsEnvResourceDO4.setInstanceId(1L)
        devopsEnvResourceDO4.setResourceDetailId(4L)
        devopsEnvResourceDO4.setName("test-ingress")

        devopsEnvResourceDO5.setId(5L)
        devopsEnvResourceDO5.setKind("ReplicaSet")
        devopsEnvResourceDO5.setInstanceId(1L)
        devopsEnvResourceDO5.setResourceDetailId(5L)
        devopsEnvResourceDO5.setName("test-replicaset-123456-abcdef")

        devopsEnvResourceDO6.setId(6)
        devopsEnvResourceDO6.setKind("Job")
        devopsEnvResourceDO6.setInstanceId(1L)
        devopsEnvResourceDO6.setResourceDetailId(6L)

        devopsEnvResourceDO7.setId(7)
        devopsEnvResourceDO7.setKind("DaemonSet")
        devopsEnvResourceDO7.setName("DaemonSet")
        devopsEnvResourceDO7.setInstanceId(1L)
        devopsEnvResourceDO7.setResourceDetailId(7L)

        devopsEnvResourceDO8.setId(8)
        devopsEnvResourceDO8.setKind("StatefulSet")
        devopsEnvResourceDO8.setName("StatefulSet")
        devopsEnvResourceDO8.setInstanceId(1L)
        devopsEnvResourceDO8.setResourceDetailId(8L)

        devopsEnvResources.add(devopsEnvResourceDO)
        devopsEnvResources.add(devopsEnvResourceDO2)
        devopsEnvResources.add(devopsEnvResourceDO3)
        devopsEnvResources.add(devopsEnvResourceDO4)
        devopsEnvResources.add(devopsEnvResourceDO5)
        devopsEnvResources.add(devopsEnvResourceDO6)
        devopsEnvResources.add(devopsEnvResourceDO7)
        devopsEnvResources.add(devopsEnvResourceDO8)

        // derd
        devopsEnvResourceDetailDO.setId(1L)
        devopsEnvResourceDetailDO.setMessage("{\"kind\":\"Pod\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"iam-service-56946b7b9f-42xnx\",\"generateName\":\"iam-service-56946b7b9f-\",\"namespace\":\"choerodon-devops-prod\",\"selfLink\":\"/api/v1/namespaces/choerodon-devops-prod/pods/iam-service-56946b7b9f-42xnx\",\"uid\":\"1667ab32-6b40-11e8-94ae-00163e0e2443\",\"resourceVersion\":\"4333254\",\"creationTimestamp\":\"2018-06-08T17:19:23Z\",\"labels\":{\"choerodon.io/metrics-port\":\"8031\",\"choerodon.io/release\":\"iam-service\",\"choerodon.io/service\":\"iam-service\",\"choerodon.io/version\":\"0.6.0\",\"pod-template-hash\":\"1250263659\"},\"annotation\":{\"choerodon.io/metrics-group\":\"spring-boot\",\"choerodon.io/metrics-path\":\"/prometheus\",\"kubernetes.io/created-by\":\"{\\\"kind\\\":\\\"SerializedReference\\\",\\\"apiVersion\\\":\\\"v1\\\",\\\"reference\\\":{\\\"kind\\\":\\\"ReplicaSet\\\",\\\"namespace\\\":\\\"choerodon-devops-prod\\\",\\\"name\\\":\\\"iam-service-56946b7b9f\\\",\\\"uid\\\":\\\"0f7ec2d5-6b40-11e8-94ae-00163e0e2443\\\",\\\"apiVersion\\\":\\\"extensions\\\",\\\"resourceVersion\\\":\\\"4332963\\\"}}\\n\"},\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"ReplicaSet\",\"name\":\"iam-service-56946b7b9f\",\"uid\":\"0f7ec2d5-6b40-11e8-94ae-00163e0e2443\",\"controller\":true,\"blockOwnerDeletion\":true}]},\"spec\":{\"volumes\":[{\"name\":\"default-token-mjcs5\",\"secret\":{\"secretName\":\"default-token-mjcs5\",\"defaultMode\":420}}],\"containers\":[{\"name\":\"iam-service\",\"image\":\"registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service:0.6.0\",\"ports\":[{\"name\":\"http\",\"containerPort\":8030,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"CHOERODON_EVENT_CONSUMER_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"EUREKA_CLIENT_SERVICEURL_DEFAULTZONE\",\"value\":\"http://register-server.choerodon-devops-prod:8000/eureka/\"},{\"name\":\"SPRING_CLOUD_CONFIG_ENABLED\",\"value\":\"true\"},{\"name\":\"SPRING_CLOUD_CONFIG_URI\",\"value\":\"http://config-server.choerodon-devops-prod:8010/\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES\",\"value\":\"172.19.136.81:2181,172.19.136.82:2181,172.19.136.83:2181\"},{\"name\":\"SPRING_DATASOURCE_PASSWORD\",\"value\":\"JAu9p8zL\"},{\"name\":\"SPRING_DATASOURCE_URL\",\"value\":\"jdbc:mysql://rm-uf65upic89q7007h5.mysql.rds.aliyuncs.com:3306/iam_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\"},{\"name\":\"SPRING_DATASOURCE_USERNAME\",\"value\":\"c7n_iam\"},{\"name\":\"SPRING_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"}],\"resources\":{\"limits\":{\"memory\":\"3Gi\"},\"requests\":{\"memory\":\"2Gi\"}},\"volumeMounts\":[{\"name\":\"default-token-mjcs5\",\"readOnly\":true,\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\"}],\"readinessProbe\":{\"exec\":{\"command\":[\"curl\",\"localhost:8031/health\"]},\"initialDelaySeconds\":60,\"timeoutSeconds\":10,\"periodSeconds\":10,\"successThreshold\":1,\"failureThreshold\":3},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"serviceAccountName\":\"default\",\"serviceAccount\":\"default\",\"nodeName\":\"choerodon2\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"},\"status\":{\"phase\":\"Running\",\"conditions\":[{\"type\":\"Initialized\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-06-08T17:19:23Z\"},{\"type\":\"Ready\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-06-08T17:20:30Z\"},{\"type\":\"PodScheduled\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-06-08T17:19:23Z\"}],\"hostIP\":\"172.19.136.82\",\"podIP\":\"192.168.2.209\",\"startTime\":\"2018-06-08T17:19:23Z\",\"containerStatuses\":[{\"name\":\"iam-service\",\"state\":{\"running\":{\"startedAt\":\"2018-06-08T17:19:24Z\"}},\"lastState\":{},\"ready\":true,\"restartCount\":0,\"image\":\"registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service:0.6.0\",\"imageID\":\"docker-pullable://registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service@sha256:ecf370e2623a62631499a7780c6851418b806018ed2d3ae2530f54cf638cb432\",\"containerID\":\"docker://2892c582b8109dff691df6190f8555cef1f9680e11d27864472bebb57962250b\"}],\"qosClass\":\"Burstable\"}}")

        devopsEnvResourceDetailDO2.setId(2L)
        devopsEnvResourceDetailDO2.setMessage("{\"apiVersion\":\"apps/v1beta2\",\"kind\":\"Deployment\",\"metadata\":{\"annotation\":{\"deployment.kubernetes.io/revision\":\"3\"},\"creationTimestamp\":\"2018-05-20T03:36:57Z\",\"generation\":5,\"labels\":{\"choerodon.io/logs-parser\":\"spring-boot\",\"choerodon.io/release\":\"iam-service\"},\"name\":\"iam-service\",\"namespace\":\"choerodon-devops-prod\",\"resourceVersion\":\"4333256\",\"selfLink\":\"/apis/apps/v1beta2/namespaces/choerodon-devops-prod/deployments/iam-service\",\"uid\":\"0c56c1b5-5bdf-11e8-a66e-00163e0e2443\"},\"spec\":{\"progressDeadlineSeconds\":600,\"replicas\":1,\"revisionHistoryLimit\":10,\"selector\":{\"matchLabels\":{\"choerodon.io/create\":\"iam-service\"}},\"strategy\":{\"rollingUpdate\":{\"maxSurge\":\"25%\",\"maxUnavailable\":\"25%\"},\"type\":\"RollingUpdate\"},\"template\":{\"metadata\":{\"annotation\":{\"choerodon.io/metrics-group\":\"spring-boot\",\"choerodon.io/metrics-path\":\"/prometheus\"},\"creationTimestamp\":null,\"labels\":{\"choerodon.io/metrics-port\":\"8031\",\"choerodon.io/create\":\"iam-service\",\"choerodon.io/service\":\"iam-service\",\"choerodon.io/version\":\"0.6.0\"}},\"spec\":{\"containers\":[{\"env\":[{\"name\":\"CHOERODON_EVENT_CONSUMER_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"EUREKA_CLIENT_SERVICEURL_DEFAULTZONE\",\"value\":\"http://register-server.choerodon-devops-prod:8000/eureka/\"},{\"name\":\"SPRING_CLOUD_CONFIG_ENABLED\",\"value\":\"true\"},{\"name\":\"SPRING_CLOUD_CONFIG_URI\",\"value\":\"http://config-server.choerodon-devops-prod:8010/\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES\",\"value\":\"172.19.136.81:2181,172.19.136.82:2181,172.19.136.83:2181\"},{\"name\":\"SPRING_DATASOURCE_PASSWORD\",\"value\":\"JAu9p8zL\"},{\"name\":\"SPRING_DATASOURCE_URL\",\"value\":\"jdbc:mysql://rm-uf65upic89q7007h5.mysql.rds.aliyuncs.com:3306/iam_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\"},{\"name\":\"SPRING_DATASOURCE_USERNAME\",\"value\":\"c7n_iam\"},{\"name\":\"SPRING_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"}],\"image\":\"registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service:0.6.0\",\"imagePullPolicy\":\"Always\",\"name\":\"iam-service\",\"ports\":[{\"containerPort\":8030,\"name\":\"http\",\"protocol\":\"TCP\"}],\"readinessProbe\":{\"exec\":{\"command\":[\"curl\",\"localhost:8031/health\"]},\"failureThreshold\":3,\"initialDelaySeconds\":60,\"periodSeconds\":10,\"successThreshold\":1,\"timeoutSeconds\":10},\"resources\":{\"limits\":{\"memory\":\"3Gi\"},\"requests\":{\"memory\":\"2Gi\"}},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\"}],\"dnsPolicy\":\"ClusterFirst\",\"restartPolicy\":\"Always\",\"schedulerName\":\"default-scheduler\",\"securityContext\":{},\"terminationGracePeriodSeconds\":30}}},\"status\":{\"availableReplicas\":1,\"conditions\":[{\"lastTransitionTime\":\"2018-05-20T03:36:57Z\",\"lastUpdateTime\":\"2018-06-08T17:19:11Z\",\"message\":\"ReplicaSet \\\"iam-service-56946b7b9f\\\" has successfully progressed.\",\"reason\":\"NewReplicaSetAvailable\",\"status\":\"True\",\"type\":\"Progressing\"},{\"lastTransitionTime\":\"2018-06-08T17:20:30Z\",\"lastUpdateTime\":\"2018-06-08T17:20:30Z\",\"message\":\"Deployment has minimum availability.\",\"reason\":\"MinimumReplicasAvailable\",\"status\":\"True\",\"type\":\"Available\"}],\"observedGeneration\":5,\"readyReplicas\":1,\"replicas\":1,\"updatedReplicas\":1}}")

        devopsEnvResourceDetailDO3.setId(3L)
        devopsEnvResourceDetailDO3.setMessage("{\"apiVersion\":\"v1\",\"kind\":\"Service\",\"metadata\":{\"creationTimestamp\":\"2018-05-20T03:29:11Z\",\"labels\":{\"choerodon.io/release\":\"config-server\"},\"name\":\"config-server\",\"namespace\":\"choerodon-devops-prod\",\"resourceVersion\":\"4325981\",\"selfLink\":\"/api/v1/namespaces/choerodon-devops-prod/services/config-server\",\"uid\":\"f68d3f07-5bdd-11e8-a66e-00163e0e2443\"},\"spec\":{\"clusterIP\":\"192.168.28.13\",\"ports\":[{\"name\":\"http\",\"port\":8010,\"protocol\":\"TCP\",\"targetPort\":\"http\"}],\"selector\":{\"choerodon.io/create\":\"config-server\"},\"sessionAffinity\":\"None\",\"type\":\"ClusterIP\"},\"status\":{\"loadBalancer\":{}}}")

        devopsEnvResourceDetailDO4.setId(4L)
        devopsEnvResourceDetailDO4.setMessage("{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"Ingress\",\"metadata\":{\"creationTimestamp\":\"2018-05-20T03:48:33Z\",\"generation\":1,\"labels\":{\"choerodon.io/release\":\"devops-service\"},\"name\":\"devops-service\",\"namespace\":\"choerodon-devops-prod\",\"resourceVersion\":\"4337962\",\"selfLink\":\"/apis/extensions/v1beta1/namespaces/choerodon-devops-prod/ingresses/devops-service\",\"uid\":\"aadd986d-5be0-11e8-a66e-00163e0e2443\"},\"spec\":{\"rules\":[{\"host\":\"devops.service.choerodon.com.cn\",\"http\":{\"paths\":[{\"backend\":{\"serviceName\":\"devops-service\",\"servicePort\":8060},\"path\":\"/\"}]}}]},\"status\":{\"loadBalancer\":{\"ingress\":[{}]}}}")

        devopsEnvResourceDetailDO5.setId(5L)
        devopsEnvResourceDetailDO5.setMessage("{\"metadata\":{\"name\":\"springboot-14f93-55f7896455\",\"namespace\":\"ljt\",\"selfLink\":\"/apis/extensions/v1beta1/namespaces/ljt/replicasets/springboot-14f93-55f7896455\",\"uid\":\"5553489b-6c9b-11e8-ad82-525400d91faf\",\"resourceVersion\":\"24136809\",\"generation\":5,\"creationTimestamp\":\"2018-06-10T10:45:04Z\",\"labels\":{\"choerodon.io/application\":\"springboot\",\"choerodon.io/release\":\"springboot-14f93\",\"choerodon.io/version\":\"0.1.0-dev.20180530070103\",\"pod-template-hash\":\"1193452011\"},\"annotation\":{\"deployment.kubernetes.io/desired-replicas\":\"1\",\"deployment.kubernetes.io/max-replicas\":\"2\",\"deployment.kubernetes.io/revision\":\"5\",\"deployment.kubernetes.io/revision-history\":\"1,3\"},\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"Deployment\",\"name\":\"springboot-14f93\",\"uid\":\"5550ab04-6c9b-11e8-8371-6a12b79743a2\",\"controller\":true,\"blockOwnerDeletion\":true}]},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"choerodon.io/create\":\"springboot-14f93\",\"pod-template-hash\":\"1193452011\"}},\"template\":{\"metadata\":{\"creationTimestamp\":null,\"labels\":{\"choerodon.io/application\":\"springboot\",\"choerodon.io/create\":\"springboot-14f93\",\"choerodon.io/version\":\"0.1.0-dev.20180530070103\",\"pod-template-hash\":\"1193452011\"}},\"spec\":{\"containers\":[{\"name\":\"springboot-14f93\",\"image\":\"registry.saas.test.com/operation-ystest1805192/springboot:0.1.0-dev.20180530070103\",\"ports\":[{\"name\":\"http\",\"containerPort\":8080,\"protocol\":\"TCP\"}],\"resources\":{\"limits\":{\"memory\":\"500Mi\"},\"requests\":{\"memory\":\"256Mi\"}},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"}}},\"status\":{\"replicas\":1,\"fullyLabeledReplicas\":1,\"readyReplicas\":1,\"availableReplicas\":1,\"observedGeneration\":5}}")

        devopsEnvResourceDetailDO6.setId(6L)
        devopsEnvResourceDetailDO6.setMessage("{\"metadata\":{\"name\":\"cctestws-7bc7a-init-db\",\"namespace\":\"ljt\",\"selfLink\":\"/apis/batch/v1/namespaces/ljt/jobs/cctestws-7bc7a-init-db\",\"uid\":\"f56fcbf4-6d24-11e8-8371-6a12b79743a2\",\"resourceVersion\":\"19435339\",\"creationTimestamp\":\"2018-06-11T03:10:13Z\",\"labels\":{\"choerodon.io/release\":\"cctestws-7bc7a\"},\"annotation\":{\"helm.sh/hook\":\"pre-install,pre-upgrade\",\"helm.sh/hook-weight\":\"1\"}},\"spec\":{\"parallelism\":1,\"completions\":1,\"activeDeadlineSeconds\":120,\"backoffLimit\":1,\"selector\":{\"matchLabels\":{\"controller-uid\":\"f56fcbf4-6d24-11e8-8371-6a12b79743a2\"}},\"template\":{\"metadata\":{\"name\":\"cctestws-7bc7a-init-db\",\"creationTimestamp\":null,\"labels\":{\"controller-uid\":\"f56fcbf4-6d24-11e8-8371-6a12b79743a2\",\"job-name\":\"cctestws-7bc7a-init-db\"}},\"spec\":{\"volumes\":[{\"name\":\"tools-jar\",\"emptyDir\":{}}],\"initContainers\":[{\"name\":\"tools\",\"image\":\"registry.cn-hangzhou.aliyuncs.com/choerodon-tools/dbtool:0.5.0\",\"command\":[\"sh\",\"-c\",\"cp -rf /var/choerodon/* /tools\"],\"resources\":{},\"volumeMounts\":[{\"name\":\"tools-jar\",\"mountPath\":\"/tools\"}],\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"containers\":[{\"name\":\"cctestws-7bc7a-init-db\",\"image\":\"registry.saas.test.com/operation-ystest1805192/cctestws:1.8.1-hotfix-we.20180608135220\",\"command\":[\"/bin/sh\",\"-c\",\" java -Dspring.datasource.url=\\\"jdbc:mysql://192.168.12.175:3306/demo_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\\\" -Dspring.datasource.username=root -Dspring.datasource.password=choerodon -Ddata.init=true -Ddata.jar=/cctestws.jar -jar /var/choerodon/choerodon-tool-liquibase.jar; \"],\"resources\":{},\"volumeMounts\":[{\"name\":\"tools-jar\",\"mountPath\":\"/var/choerodon\"}],\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"IfNotPresent\"}],\"restartPolicy\":\"Never\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"}}},\"status\":{\"conditions\":[{\"type\":\"Failed\",\"status\":\"True\",\"lastProbeTime\":\"2018-06-11T03:10:34Z\",\"lastTransitionTime\":\"2018-06-11T03:10:34Z\",\"reason\":\"BackoffLimitExceeded\",\"message\":\"Job has reach the specified backoff limit\"}],\"startTime\":\"2018-06-11T03:10:13Z\",\"failed\":1}}")

        devopsEnvResourceDetailDO7.setId(7L)
        devopsEnvResourceDetailDO7.setMessage("{\"apiVersion\":\"v1\",\"kind\":\"Pod\",\"metadata\":{\"creationTimestamp\":\"2018-12-18T08:03:42Z\",\"generateName\":\"daemonset-name-\",\"labels\":{\"controller-revision-hash\":\"608462091\",\"name\":\"fluentd-elasticsearch\",\"pod-template-generation\":\"1\"},\"name\":\"daemonset-name-hd24n\",\"namespace\":\"default\",\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"blockOwnerDeletion\":true,\"controller\":true,\"kind\":\"DaemonSet\",\"name\":\"daemonset-name\",\"uid\":\"6f6bb78b-029b-11e9-a58a-0800272ecd84\"}],\"resourceVersion\":\"47798\",\"selfLink\":\"/api/v1/namespaces/default/pods/daemonset-name-hd24n\",\"uid\":\"6f9b9e49-029b-11e9-a58a-0800272ecd84\"},\"spec\":{\"containers\":[{\"image\":\"nginx\",\"imagePullPolicy\":\"Always\",\"name\":\"container-name\",\"resources\":{\"limits\":{\"memory\":\"200Mi\"},\"requests\":{\"cpu\":\"100m\",\"memory\":\"200Mi\"}},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"volumeMounts\":[{\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\",\"name\":\"default-token-rmhmg\",\"readOnly\":true}]}],\"dnsPolicy\":\"ClusterFirst\",\"nodeName\":\"minikube\",\"restartPolicy\":\"Always\",\"schedulerName\":\"default-scheduler\",\"securityContext\":{},\"serviceAccount\":\"default\",\"serviceAccountName\":\"default\",\"terminationGracePeriodSeconds\":30,\"tolerations\":[{\"effect\":\"NoSchedule\",\"key\":\"node-role.kubernetes.io/master\"},{\"effect\":\"NoExecute\",\"key\":\"node.kubernetes.io/not-ready\",\"operator\":\"Exists\"},{\"effect\":\"NoExecute\",\"key\":\"node.kubernetes.io/unreachable\",\"operator\":\"Exists\"},{\"effect\":\"NoSchedule\",\"key\":\"node.kubernetes.io/disk-pressure\",\"operator\":\"Exists\"},{\"effect\":\"NoSchedule\",\"key\":\"node.kubernetes.io/memory-pressure\",\"operator\":\"Exists\"}],\"volumes\":[{\"name\":\"default-token-rmhmg\",\"secret\":{\"defaultMode\":420,\"secretName\":\"default-token-rmhmg\"}}]},\"status\":{\"conditions\":[{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:03:42Z\",\"status\":\"True\",\"type\":\"Initialized\"},{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:03:51Z\",\"status\":\"True\",\"type\":\"Ready\"},{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:03:46Z\",\"status\":\"True\",\"type\":\"PodScheduled\"}],\"containerStatuses\":[{\"containerID\":\"docker://60289d333e7dc5e74665768e807bc024c6c76670ae2ca041aa6b602bb4b1b791\",\"image\":\"nginx:latest\",\"imageID\":\"docker-pullable://nginx@sha256:5d32f60db294b5deb55d078cd4feb410ad88e6fe77500c87d3970eca97f54dba\",\"lastState\":{},\"name\":\"container-name\",\"ready\":true,\"restartCount\":0,\"state\":{\"running\":{\"startedAt\":\"2018-12-18T08:03:50Z\"}}}],\"hostIP\":\"192.168.99.100\",\"phase\":\"Running\",\"podIP\":\"172.17.0.4\",\"qosClass\":\"Burstable\",\"startTime\":\"2018-12-18T08:03:42Z\"}}")


        devopsEnvResourceDetailDO8.setId(8L)
        devopsEnvResourceDetailDO8.setMessage("{\"apiVersion\":\"v1\",\"kind\":\"Pod\",\"metadata\":{\"creationTimestamp\":\"2018-12-18T08:06:20Z\",\"generateName\":\"web-\",\"labels\":{\"app\":\"nginx\",\"controller-revision-hash\":\"web-5fbd4bb9cc\",\"statefulset.kubernetes.io/pod-name\":\"web-0\"},\"name\":\"web-0\",\"namespace\":\"default\",\"ownerReferences\":[{\"apiVersion\":\"apps/v1beta1\",\"blockOwnerDeletion\":true,\"controller\":true,\"kind\":\"StatefulSet\",\"name\":\"web\",\"uid\":\"cd21c557-029b-11e9-a58a-0800272ecd84\"}],\"resourceVersion\":\"47922\",\"selfLink\":\"/api/v1/namespaces/default/pods/web-0\",\"uid\":\"cd6833e3-029b-11e9-a58a-0800272ecd84\"},\"spec\":{\"containers\":[{\"image\":\"nginx\",\"imagePullPolicy\":\"Always\",\"name\":\"nginx\",\"ports\":[{\"containerPort\":80,\"name\":\"web\",\"protocol\":\"TCP\"}],\"resources\":{},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"volumeMounts\":[{\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\",\"name\":\"default-token-rmhmg\",\"readOnly\":true}]}],\"dnsPolicy\":\"ClusterFirst\",\"hostname\":\"web-0\",\"nodeName\":\"minikube\",\"restartPolicy\":\"Always\",\"schedulerName\":\"default-scheduler\",\"securityContext\":{},\"serviceAccount\":\"default\",\"serviceAccountName\":\"default\",\"subdomain\":\"nginx\",\"terminationGracePeriodSeconds\":10,\"volumes\":[{\"name\":\"default-token-rmhmg\",\"secret\":{\"defaultMode\":420,\"secretName\":\"default-token-rmhmg\"}}]},\"status\":{\"conditions\":[{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:06:20Z\",\"status\":\"True\",\"type\":\"Initialized\"},{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:06:28Z\",\"status\":\"True\",\"type\":\"Ready\"},{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:06:20Z\",\"status\":\"True\",\"type\":\"PodScheduled\"}],\"containerStatuses\":[{\"containerID\":\"docker://dc43b31f553024a98401891a79084bc5be99a0338b0e0666f678854c1770850d\",\"image\":\"nginx:latest\",\"imageID\":\"docker-pullable://nginx@sha256:5d32f60db294b5deb55d078cd4feb410ad88e6fe77500c87d3970eca97f54dba\",\"lastState\":{},\"name\":\"nginx\",\"ready\":true,\"restartCount\":0,\"state\":{\"running\":{\"startedAt\":\"2018-12-18T08:06:26Z\"}}}],\"hostIP\":\"192.168.99.100\",\"phase\":\"Running\",\"podIP\":\"172.17.0.4\",\"qosClass\":\"BestEffort\",\"startTime\":\"2018-12-18T08:06:20Z\"}}")

        resourceDetailDTOList.add(devopsEnvResourceDetailDO)
        resourceDetailDTOList.add(devopsEnvResourceDetailDO2)
        resourceDetailDTOList.add(devopsEnvResourceDetailDO3)
        resourceDetailDTOList.add(devopsEnvResourceDetailDO4)
        resourceDetailDTOList.add(devopsEnvResourceDetailDO5)
        resourceDetailDTOList.add(devopsEnvResourceDetailDO6)
        resourceDetailDTOList.add(devopsEnvResourceDetailDO7)
        resourceDetailDTOList.add(devopsEnvResourceDetailDO8)

        // dce
        devopsCommandEventDTO.setId(1L)
        devopsCommandEventDTO.setType("Job")
        devopsCommandEventDTO.setCommandId(1L)
        devopsCommandEventDTO.setName("commandEvent")

        // dcl
        devopsEnvCommandLogDTO.setId(1L)
        devopsEnvCommandLogDTO.setLog()
        devopsEnvCommandLogDTO.setCommandId(1L)

        // defr
        devopsEnvFileResourceDTO.setId(1L)
        devopsEnvFileResourceDTO.setEnvId(1L)
        devopsEnvFileResourceDTO.setResourceId(1L)
        devopsEnvFileResourceDTO.setResourceType("C7NHelmRelease")
        devopsEnvFileResourceDTO.setFilePath("filePath")

        // def
        devopsEnvFileDTO.setId(1L)
        devopsEnvFileDTO.setEnvId(1L)
        devopsEnvFileDTO.setDevopsCommit("devopsCommit")
    }

    def insertMockData() {
        devopsProjectMapper.insert(devopsProjectDTO)
        deployValueMapper.insert(devopsDeployValueDTO)
        appServiceMapper.insert(appServiceDTO)
        devopsEnvPodMapper.insert(devopsEnvPodDTO)
        appServiceShareRuleMapper.insert(appServiceShareRuleDTO)
        devopsEnvCommandMapper.selectAll().forEach { devopsEnvCommandMapper.delete(it) }
        devopsEnvCommandMapper.insert(devopsEnvCommandDTO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDTO)
        devopsEnvCommandValueMapper.insert(devopsEnvCommandValueDTO)
        devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDTO)
        applicationVersionValueMapper.insert(appServiceVersionValueDTO)

        devopsEnvResources.forEach({ it -> devopsEnvResourceMapper.insert(it) })

        appServiceVersionMapper.insert(appServiceVersionDTO)
        applicationInstanceMapper.insert(appServiceInstanceDTO)

        resourceDetailDTOList.forEach({ it -> devopsEnvResourceDetailMapper.insert(it) })

        devopsEnvCommandLogMapper.insert(devopsEnvCommandLogDTO)
        devopsCommandEventMapper.insert(devopsCommandEventDTO)
        devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDTO)
        devopsEnvFileMapper.insert(devopsEnvFileDTO)
    }

    def mockBehavior() {
        ProjectDTO projectDTO = new ProjectDTO()
        projectDTO.setName("testProject")
        projectDTO.setCode("pro")
        projectDTO.setOrganizationId(1L)
        PowerMockito.when(baseServiceClientOperator.queryIamProjectById(1L)).thenReturn(projectDTO)

        OrganizationDTO organizationDTO = new OrganizationDTO()
        organizationDTO.setId(1L)
        organizationDTO.setCode("org")
        PowerMockito.when(baseServiceClientOperator.queryOrganizationById(1L)).thenReturn(organizationDTO)

        List<RoleVO> roleVOS = new ArrayList<>()
        RoleVO roleDTO = new RoleVO()
        roleDTO.setCode("role/project/default/project-owner")
        roleVOS.add(roleDTO)
        List<ProjectWithRoleVO> projectWithRoleVOS = new ArrayList<>()
        ProjectWithRoleVO projectWithRoleDTO = new ProjectWithRoleVO()
        projectWithRoleDTO.setName("pro")
        projectWithRoleDTO.setRoles(roleVOS)
        projectWithRoleVOS.add(projectWithRoleDTO)
        PowerMockito.when(baseServiceClientOperator.listProjectWithRoleDTO(anyLong())).thenReturn(projectWithRoleVOS)

        List<ProjectDTO> projectDTOS = new ArrayList<>()
        projectDTOS.add(projectDTO)
        PowerMockito.when(baseServiceClientOperator.listIamProjectByOrgId(anyLong())).thenReturn(projectDTOS)

        MemberDTO memberDTO = new MemberDTO()
        memberDTO.setId(1)
        memberDTO.setAccessLevel(AccessLevel.OWNER.toValue())
        PowerMockito.when(gitlabServiceClientOperator.queryGroupMember(anyInt(), anyInt())).thenReturn(memberDTO)

//        IamUserDTO iamUserDTO = new IamUserDTO()
//        iamUserDTO.setId(1L)
//        iamUserDTO.setLoginName("test")
//        iamUserDTO.setImageUrl("imageURL")
//        ResponseEntity<IamUserDTO> responseEntity3 = new ResponseEntity<>(iamUserDTO, HttpStatus.OK)
//        Mockito.when(baseServiceClient.queryByLoginName(anyString())).thenReturn(responseEntity3)
//        PowerMockito.when(baseServiceClientOperator.queryBy)

        List<GitlabPipelineDTO> gitlabPipelineDTOS = new ArrayList<>()
        GitlabPipelineDTO gitlabPipelineDTO = new GitlabPipelineDTO()
        gitlabPipelineDTO.setId(1)
        GitLabUserDTO gitLabUserDTO = new GitLabUserDTO()
        gitlabPipelineDTO.setRef("")
        gitlabPipelineDTO.setUser(gitLabUserDTO)
        gitLabUserDTO.setId(1)
        gitLabUserDTO.setName("gitlabTestName")
        gitlabPipelineDTOS.add(gitlabPipelineDTO)
        PowerMockito.when(gitlabServiceClientOperator.listPipeline(anyInt(), anyInt())).thenReturn(gitlabPipelineDTOS)

        PowerMockito.when(gitlabServiceClientOperator.queryPipeline(anyInt(), anyInt(), anyInt())).thenReturn(gitlabPipelineDTO)

        List<IamUserDTO> userDOList = new ArrayList<>()
        IamUserDTO userDO1 = new IamUserDTO()
        userDO1.setId(1)
        userDO1.setLoginName("loginName")
        userDO1.setRealName("realName")
        userDO1.setImageUrl("imageUrl")
        userDOList.add(userDO1)
        PowerMockito.when(baseServiceClientOperator.listUsersByIds(any(List))).thenReturn(userDOList)
    }

    def "QueryInstanceInformationById"() {
        given: "准备数据"
        isToInit = false
        def url = MAPPING + "/{instance_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", PROJECT_ID)
        params.put("instance_id", appServiceInstanceDTO.getId())

        when: "调用方法"
        def result = restTemplate.getForObject(url, AppServiceInstanceInfoVO, params)

        then: "校验结果"
        result != null
        result.getCode() == appServiceInstanceDTO.getCode()
    }

    def "PageByOptions"() {
        given: '初始化数据'
        String infra = "{\"searchParam\":{},\"param\":\"\"}"

        when: '分页查询应用部署'
        def page = restTemplate.postForObject(MAPPING + "/page_by_options?page=1&size=10", infra, PageInfo.class, 1L)

        then: '校验返回值'
        page.getList().get(0)["code"] == "appInsCode"
    }
//
//    def "ListByAppId"() {
//        when: '查询应用部署'
//        def list = restTemplate.getForObject("/v1/projects/1/app_instances/all?appId=1", List.class)
//
//        then: '校验返回值'
//        list.get(0)["applicationName"] == "appName"
//    }

    def "分页查询服务部署"() {
        given: "准备数据"
        def url = MAPPING + "/page_by_options?env_id={env_id}"
        def map = new HashMap<String, Object>()
        map.put("project_id", PROJECT_ID)
        map.put("env_id", devopsEnvironmentDTO.getId())

        when: "调用方法"
        def page = restTemplate.postForObject(url, EMPTY_SEARCH_PARAM, PageInfo, map)

        then: "校验结果"
        page != null
        page.getTotal() == 1
        page.getList().size() == 1
        page.getList().get(0)["code"] == appServiceInstanceDTO.getCode()
    }

    def "获取实例上次部署配置"() {
        given: "准备数据"
        def url = MAPPING + "/{instance_Id}/last_deploy_value"
        def map = new HashMap<String, Object>()
        map.put("project_id", PROJECT_ID)
        map.put("instance_Id", appServiceInstanceDTO.getId())

        when: "调用方法"
        def value = restTemplate.getForObject(url, String, map)

        then: "校验结果"
        value != null
    }


    def "getDeploymentDetailsJsonByInstanceId"() {
        given: "初始化数据库"
        Map<String, Long> map = new HashMap<>(3)
        String deploymentName = "test-deployment"
        initForInstance(map, deploymentName)
        Long projectId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/deployment_detail_json?deployment_name=" + deploymentName, InstanceControllerDetailVO, projectId, map.get("instanceId"))
        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        ((Map<String, Map>) entity.getBody().getDetail()).get("metadata") != null

        and: "删除初始化的数据"
        cleanDataInitializedInInstanceInit(map)

        when: '查询不存在的数据时'
        entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/deployment_detail_json?deployment_name=" + deploymentName, ExceptionResponse, projectId, map.get("instanceId"))

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getCode() == "error.instance.resource.not.found"
    }

    def "getDeploymentDetailsYamlByInstanceId"() {
        given: "初始化数据库"
        Map<String, Long> map = new HashMap<>(3)
        String deploymentName = "test-deployment"
        initForInstance(map, deploymentName)
        Long projectId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/deployment_detail_yaml?deployment_name=" + deploymentName, InstanceControllerDetailVO, projectId, map.get("instanceId"))

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        new ObjectMapper().readTree(JsonYamlConversionUtil.yaml2json(entity.getBody().getDetail().toString())).get("metadata") != null

        and: "删除初始化的数据"
        cleanDataInitializedInInstanceInit(map)

        when: '查询不存在的数据时'
        entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/deployment_detail_yaml?deployment_name=" + deploymentName, ExceptionResponse, projectId, map.get("instanceId"))

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getCode() == "error.instance.resource.not.found"
    }

    def "getDaemonSetDetailsJsonByInstanceId"() {
        given: "准备数据"
        String daemonSetName = devopsEnvResources.get(6).getName()
        Long projectId = 1L
        Long instanceId = appServiceInstanceDTO.getId()

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/daemon_set_detail_json?daemon_set_name=" + daemonSetName, InstanceControllerDetailVO, projectId, instanceId)
        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        ((Map<String, Map>) entity.getBody().getDetail()).get("metadata") != null

        when: '查询不存在的数据时'
        entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/daemon_set_detail_json?daemon_set_name=" + daemonSetName, ExceptionResponse, 1000L, 1000L)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getCode() == "error.instance.resource.not.found"
    }

    def "getDaemonSetDetailsYamlByInstanceId"() {
        given: "初始化数据库"
        String daemonSetName = devopsEnvResources.get(6).getName()
        Long projectId = 1L
        Long instanceId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/daemon_set_detail_yaml?daemon_set_name=" + daemonSetName, InstanceControllerDetailVO, projectId, instanceId)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        new ObjectMapper().readTree(JsonYamlConversionUtil.yaml2json(entity.getBody().getDetail().toString())).get("metadata") != null

        when: '查询不存在的数据时'
        entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/daemon_set_detail_yaml?daemon_set_name=" + daemonSetName, ExceptionResponse, 1000L, 1000L)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getCode() == "error.instance.resource.not.found"
    }

    def "getStatefulSetDetailsJsonByInstanceId"() {
        given: "准备数据"
        String statefulSetName = devopsEnvResources.get(7).getName()
        Long projectId = 1L
        Long instanceId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/stateful_set_detail_json?stateful_set_name=" + statefulSetName, InstanceControllerDetailVO, projectId, instanceId)
        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        ((Map<String, Map>) entity.getBody().getDetail()).get("metadata") != null

        when: '查询不存在的数据时'
        entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/stateful_set_detail_json?stateful_set_name=" + statefulSetName, ExceptionResponse, 1000L, 1000L)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getCode() == "error.instance.resource.not.found"
    }

    def "getStatefulSetDetailsYamlByInstanceId"() {
        given: "初始化数据库"
        String statefulSetName = devopsEnvResources.get(7).getName()
        Long projectId = 1L
        Long instanceId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/stateful_set_detail_yaml?stateful_set_name=" + statefulSetName, InstanceControllerDetailVO, projectId, instanceId)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        new ObjectMapper().readTree(JsonYamlConversionUtil.yaml2json(entity.getBody().getDetail().toString())).get("metadata") != null

        when: '查询不存在的数据时'
        entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/stateful_set_detail_yaml?stateful_set_name=" + statefulSetName, ExceptionResponse, 1000L, 1000L)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getCode() == "error.instance.resource.not.found"
    }

    def initForInstance(Map<String, Long> map, String deploymentName) {
        AppServiceInstanceDTO instanceInit = new AppServiceInstanceDTO()
        instanceInit.setCode("dependency-chart-59dad")
        instanceInit.setAppServiceId(1L)
        instanceInit.setEnvId(1L)
        instanceInit.setCommandId(1L)
        applicationInstanceMapper.insert(instanceInit)

        DevopsEnvResourceDetailDTO detailInit = new DevopsEnvResourceDetailDTO()
        detailInit.setMessage("{\"metadata\":{\"name\":\"ins4\",\"namespace\":\"env1112\",\"selfLink\":\"/apis/extensions/v1beta1/namespaces/env1112/deployments/ins4\",\"uid\":\"d444dd68-f44c-11e8-aca1-525400d91faf\",\"resourceVersion\":\"69026386\",\"generation\":2,\"creationTimestamp\":\"2018-11-30T03:05:45Z\",\"labels\":{\"choerodon.io\":\"2018.11.30-105053-master\",\"choerodon.io/application\":\"code-i\",\"choerodon.io/logs-parser\":\"nginx\",\"choerodon.io/release\":\"ins4\",\"choerodon.io/version\":\"2018.11.20-135445-master\"},\"annotation\":{\"deployment.kubernetes.io/revision\":\"2\"}},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"choerodon.io/create\":\"ins4\"}},\"template\":{\"metadata\":{\"creationTimestamp\":null,\"labels\":{\"choerodon.io\":\"2018.11.30-105053-master\",\"choerodon.io/application\":\"code-i\",\"choerodon.io/create\":\"ins4\",\"choerodon.io/version\":\"2018.11.20-135445-master\"}},\"spec\":{\"containers\":[{\"name\":\"ins4\",\"image\":\"registry.saas.test.com/code-x-code-x/code-i:2018.11.20-135445-master\",\"ports\":[{\"name\":\"http\",\"containerPort\":80,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"PRO_API_HOST\",\"value\":\"api.example.com.cn\"},{\"name\":\"PRO_CLIENT_ID\",\"value\":\"example\"},{\"name\":\"PRO_COOKIE_SERVER\",\"value\":\"example.com.cn\"},{\"name\":\"PRO_HEADER_TITLE_NAME\",\"value\":\"Choerodon\"},{\"name\":\"PRO_HTTP\",\"value\":\"http\"},{\"name\":\"PRO_LOCAL\",\"value\":\"true\"},{\"name\":\"PRO_TITLE_NAME\",\"value\":\"Choerodon\"}],\"resources\":{},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"IfNotPresent\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"}},\"strategy\":{\"type\":\"RollingUpdate\",\"rollingUpdate\":{\"maxUnavailable\":\"25%\",\"maxSurge\":\"25%\"}},\"revisionHistoryLimit\":10,\"progressDeadlineSeconds\":600},\"status\":{\"observedGeneration\":2,\"replicas\":1,\"updatedReplicas\":1,\"readyReplicas\":1,\"availableReplicas\":1,\"conditions\":[{\"type\":\"Available\",\"status\":\"True\",\"lastUpdateTime\":\"2018-11-30T03:05:49Z\",\"lastTransitionTime\":\"2018-11-30T03:05:49Z\",\"reason\":\"MinimumReplicasAvailable\",\"message\":\"Deployment has minimum availability.\"},{\"type\":\"Progressing\",\"status\":\"True\",\"lastUpdateTime\":\"2018-12-02T08:20:37Z\",\"lastTransitionTime\":\"2018-11-30T03:05:45Z\",\"reason\":\"NewReplicaSetAvailable\",\"message\":\"ReplicaSet \\\"ins4-786469cf45\\\" has successfully progressed.\"}]}}")
        devopsEnvResourceDetailMapper.insert(detailInit)

        DevopsEnvResourceDTO resourceInit = new DevopsEnvResourceDTO()
        resourceInit.setInstanceId(instanceInit.getId())
        resourceInit.setKind("Deployment")
        resourceInit.setResourceDetailId(detailInit.getId())
        resourceInit.setName(deploymentName)
        devopsEnvResourceMapper.insert(resourceInit)

        map.put("instanceId", instanceInit.getId())
        map.put("resourceId", resourceInit.getId())
        map.put("detailId", detailInit.getId())
    }

    def cleanDataInitializedInInstanceInit(Map<String, Long> map) {
        devopsEnvResourceDetailMapper.deleteByPrimaryKey(map.get("detailId"))
        devopsEnvResourceMapper.deleteByPrimaryKey(map.get("resourceId"))
        applicationInstanceMapper.deleteByPrimaryKey(map.get("instanceId"))
    }

    def "QueryUpgradeValue"() {
        given: "准备数据"
        def url = MAPPING + "/{instance_id}/appServiceVersion/{version_id}/upgrade_value"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("instance_id", appServiceInstanceDTO.getId())
        map.put("version_id", appServiceVersionDTO.getId())

        when: '获取升级 Value'
        def result = restTemplate.getForObject(url, InstanceValueVO.class, map)

        then:
        result != null
        result.getId() == devopsDeployValueDTO.getId()
        result.getYaml() != null
        result.getName() == devopsDeployValueDTO.getName()
    }

    def "QueryValue"() {
        given: "准备数据"
        def url = MAPPING + "/deploy_value?instance_id={instance_id}&type={type}&version_id={version_id}"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("instance_id", appServiceInstanceDTO.getId())
        map.put("version_id", appServiceVersionDTO.getId())
        map.put("type", "create")

        when: '创建时获取部署Value'
        def result = restTemplate.getForObject(url, InstanceValueVO.class, map)

        then: "校验结果"
        result.getYaml() != null

        when: "更新时获取部署Value"
        map.put("type", "update")
        def value = restTemplate.getForObject(url, InstanceValueVO.class, map)

        then: "校验结果"
        value != null
        value.getId() == devopsDeployValueDTO.getId()
        value.getYaml() != null
        value.getName() == devopsDeployValueDTO.getName()
    }

    def "PreviewValues"() {
        given: "准备数据"
        def url = MAPPING + "/preview_value?version_id={version_id}"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("version_id", appServiceVersionDTO.getId())

        InstanceValueVO replaceResult = new InstanceValueVO()
        replaceResult.setYaml(appServiceVersionValueDTO.getValue())

        when: '查询value列表'
        def result = restTemplate.postForObject(url, replaceResult, InstanceValueVO.class, map)

        then: '校验返回值'
        result != null
    }

    def "校验values"() {
        given: "准备数据"
        def url = MAPPING + "/value_format"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        InstanceValueVO valueVO = new InstanceValueVO()
        valueVO.setYaml("---\nname:\nzzz")

        when: "调用方法校验格式错误的yaml"
        def output = restTemplate.postForObject(url, valueVO, List, map)

        then: "校验结果"
        output.size() > 0

        when: "调用方法校验格式正确的yaml"
        valueVO.setYaml("---\nname:\n  correct")
        output = restTemplate.postForObject(url, valueVO, List, map)

        then: "校验结果"
        output.size() == 0
    }


//    def "QueryValues"() {
//        when: '查询value列表'
//        def result = restTemplate.getForObject("/v1/projects/1/app_instances/value?appId=1&envId=1&appVersionId=1", InstanceValueVO.class)
//
//        then: '校验返回值'
//        result.getDeltaYaml() == ""
//    }



//    def "FormatValue"() {
//        given: '初始化replaceResult'
//        InstanceValueVO result = new InstanceValueVO()
//        result.setYaml("env:\n" +
//                "  open:\n" +
//                "    PRO_API_HOST: api.example.com.cn1\n" +
//                "preJob:\n" +
//                "  preConfig:\n" +
//                "    mysql:\n" +
//                "      username: root\n" +
//                "      host: 192.168.12.156\n" +
//                "      password: handhand\n" +
//                "      dbname: demo_service")
//
//        when: '校验values'
//        def list = restTemplate.postForObject("/v1/projects/1/app_instances/value_format", result, List.class)
//
//        then: '校验返回值'
//        list.isEmpty()
//    }

    // 部署实例
    def "部署实例"() {
        given: '初始化applicationDeployDTO'
        def url = MAPPING
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)

        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO()
        appServiceDeployVO.setEnvironmentId(devopsEnvironmentDTO.getId())
        appServiceDeployVO.setValues(appServiceVersionValueDTO.getValue())
        appServiceDeployVO.setAppServiceId(appServiceDTO.getId())
        appServiceDeployVO.setAppServiceVersionId(appServiceVersionDTO.getId())
        appServiceDeployVO.setType("create")
        appServiceDeployVO.setIsNotChange(false)
        appServiceDeployVO.setInstanceName("create-instance-0ac4a")

        when: '部署应用服务'
        def dto = restTemplate.postForObject(url, appServiceDeployVO, AppServiceInstanceVO.class, map)

        then: '校验返回值'
        dto != null
        dto.getCode() == appServiceDeployVO.getInstanceName()
        dto.getId() != null
    }

    def "更新实例"() {
        given: '初始化applicationDeployDTO'
        def url = MAPPING
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)

        def search = new AppServiceInstanceDTO()
        search.setCode("create-instance-0ac4a")
        search.setEnvId(devopsEnvironmentDTO.getId())
        AppServiceInstanceDTO instance = applicationInstanceMapper.selectOne(search)

        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO()
        appServiceDeployVO.setEnvironmentId(devopsEnvironmentDTO.getId())
        appServiceDeployVO.setValues(appServiceVersionValueDTO.getValue() + "\nchanged:\n  xyz")
        appServiceDeployVO.setAppServiceId(appServiceDTO.getId())
        appServiceDeployVO.setAppServiceVersionId(appServiceVersionDTO.getId())
        appServiceDeployVO.setType("update")
        appServiceDeployVO.setIsNotChange(false)
        appServiceDeployVO.setInstanceId(instance.getId())
        appServiceDeployVO.setInstanceName(instance.getCode())

        when: '更新实例'
        restTemplate.put(url, appServiceDeployVO, AppServiceInstanceVO.class, map)
        def result = applicationInstanceMapper.selectOne(search)

        then: '校验返回值'
        result != null
        result.getObjectVersionNumber() == 2
    }

    def "查询运行中的实例"() {
        given: "准备数据"
        def url = MAPPING + "/list_running_instance?env_id={env_id}"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("env_id", devopsEnvironmentDTO.getId())

        when: '查询运行中的实例'
        def list = restTemplate.getForObject(url, List.class, map)

        then: '校验返回值'
        list != null
        list.size() == 1
        list.get(0)["code"] == "appInsCode"
    }

    def "环境下某服务运行中或失败的实例"() {
        given: "准备数据"
        def url = MAPPING + "/list_running_and_failed?env_id={env_id}&app_service_id={app_service_id}"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("env_id", devopsEnvironmentDTO.getId())
        map.put("app_service_id", appServiceDTO.getId())

        when: '查询运行中的实例'
        def list = restTemplate.getForObject(url, List.class, map)

        then: '校验返回值'
        list != null
        list.size() == 1
        list.get(0)["code"] == "appInsCode"
    }

    def "获取部署实例release相关对象"() {
        given: "准备数据"
        def url = MAPPING + "/{instance_id}/resources?env_id={env_id}"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("instance_id", appServiceInstanceDTO.getId())
        map.put("env_id", devopsEnvironmentDTO.getId())

        when: '获取部署实例资源对象'
        def dto = restTemplate.getForObject(url, DevopsEnvResourceVO.class, map)

        then: '校验返回值'
        dto.getPodVOS().get(0)["name"] == "iam-service-56946b7b9f-42xnx"
        dto.getServiceVOS().get(0)["name"] == "config-server"
        dto.getIngressVOS().get(0)["name"] == "devops-service"
        dto.getDeploymentVOS().get(0)["name"] == "iam-service"
        dto.getReplicaSetVOS().get(0)["name"] == "springboot-14f93-55f7896455"
    }


    def "获取部署实例Event事件"() {
        given:
        def url = MAPPING + "/{instance_id}/events"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("instance_id", appServiceInstanceDTO.getId())

        when: '获取部署实例Event事件'
        def list = restTemplate.getForObject(url, List.class, map)

        then: '校验返回值'
        list.get(0)["podEventVO"].get(0)["name"] == "commandEvent"
    }

    def "Stop"() {
        given:
        def url = MAPPING + "/{instance_id}/stop"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("instance_id", appServiceInstanceDTO.getId())

        when: '校验返回值'
        restTemplate.put(url, null, map)

        then:
        devopsEnvCommandMapper.selectAll().last()["commandType"] == "stop"
    }

    def "Start"() {
        given:
        def url = MAPPING + "/{instance_id}/start"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("instance_id", appServiceInstanceDTO.getId())

        AppServiceInstanceDTO appServiceInstanceDTO1 = applicationInstanceMapper.selectByPrimaryKey(appServiceInstanceDTO.getId())
        appServiceInstanceDTO1.setStatus(InstanceStatus.STOPPED.getStatus())
        applicationInstanceMapper.updateByPrimaryKeySelective(appServiceInstanceDTO1)

        when: '实例重启'
        restTemplate.put(url, null, map)

        then: '校验返回值'
        devopsEnvCommandMapper.selectAll().last()["commandType"] == "restart"
    }

    def "Restart"() {
        given:
        def url = MAPPING + "/{instance_id}/restart"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("instance_id", appServiceInstanceDTO.getId())

        when: '实例重新部署'
        restTemplate.put(url, null, map)

        then: '校验返回值'
        devopsEnvCommandMapper.selectAll().last()["commandType"] == "update"
    }

    def "Delete"() {
        given:
        def url = MAPPING + "/{instance_id}/delete"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("instance_id", appServiceInstanceDTO.getId())

        def condition = new DevopsEnvCommandDTO()
        condition.setObjectId(appServiceInstanceDTO.getId())
        condition.setObject("instance")

        when: '实例删除'
        restTemplate.delete(url,map)

        then: '校验是否删除'
        applicationInstanceMapper.selectByPrimaryKey(appServiceInstanceDTO.getId()) == null
        devopsEnvCommandMapper.select(condition).isEmpty()
    }

    def "CheckName"() {
        given:
        def url = MAPPING + "/check_name?instance_name={instance_name}&env_id={env_id}"
        def map = new HashMap()
        map.put("project_id", PROJECT_ID)
        map.put("env_id", devopsEnvironmentDTO.getId())
        map.put("instance_name", applicationInstanceMapper.selectAll().get(0).getCode())

        when: '集群下校验重复的实例名唯一性'
        ExceptionResponse exception = restTemplate.getForObject(url, ExceptionResponse.class, map)

        then: '名字存在抛出异常'
        exception != null
        exception.failed

        when: '集群下校验不重复的实例名唯一性'
        map.put("instance_name", "unique-ac4d")
        exception = restTemplate.getForObject(url, ExceptionResponse.class, map)

        then: '名字不存在不抛出异常'
        exception == null
    }

    def "获取部署时长报表"() {
        given: '准备数据'
        List<Long> appIds = new ArrayList<>()
        appIds.add(appServiceDTO.getId())
        def newInstanceId = 1000L
        def commandId = 1000L

        AppServiceInstanceDTO instance = ConvertUtils.convertObject(appServiceInstanceDTO, AppServiceInstanceDTO)
        instance.setId(newInstanceId)
        instance.setCommandId(commandId)
        applicationInstanceMapper.insert(instance)

        DevopsEnvCommandDTO e = new DevopsEnvCommandDTO()
        e.setId(commandId)
        e.setObjectId(newInstanceId)
        e.setObject("instance")
        e.setStatus("success")
        e.setCommandType("create")
        e.setObjectVersionId(appServiceVersionDTO.getId())
        devopsEnvCommandMapper.insert(e)


        Calendar cal = Calendar.getInstance()
        String year = cal.get(Calendar.YEAR)
        String month = cal.get(Calendar.MONTH) + 1
        String day = cal.get(Calendar.DATE)
        String startTime = year + "/" + month + "/" + day

        when: '获取部署时长报表'
        def dto = restTemplate.postForObject("/v1/projects/1/app_service_instances/env_commands/time?env_id=1&startTime=" + startTime + "&endTime=" + startTime, appIds, DeployTimeVO.class)

        then: '校验返回值'
        dto.getDeployAppVOS().get(0)["appServiceName"] == "appName"
    }

    def "获取部署次数报表"() {
        given: '初始化时间'
        Calendar cal = Calendar.getInstance()
        String year = cal.get(Calendar.YEAR)
        String month = cal.get(Calendar.MONTH) + 1
        String day = cal.get(Calendar.DATE)
        String startTime = year + "/" + month + "/" + day

        and: 'envIds'
        List<Long> envIds = new ArrayList<>()
        envIds.add(1L)

        when: '获取部署次数报表'
        def dto = restTemplate.postForObject("/v1/projects/1/app_service_instances/env_commands/frequency?app_service_id=1&startTime=" + startTime + "&endTime=" + startTime, envIds, DeployFrequencyVO.class)

        then: '校验返回值'
        dto.getCreationDates().size() == 1
        dto.getDeployFrequencys().size() == 1
        dto.getDeploySuccessFrequency().size() == 1
        dto.getDeployFailFrequency().size() == 1
    }

    def "分页获取部署次数列表"() {
        given: '初始化时间'
        Calendar cal = Calendar.getInstance()
        String year = cal.get(Calendar.YEAR)
        String month = cal.get(Calendar.MONTH) + 1
        String day = cal.get(Calendar.DATE)
        String startTime = year + "/" + month + "/" + day

        and: 'envIds'
        List<Long> envIds = new ArrayList<>()
        envIds.add(1L)

        when: '获取部署次数报表table'
        def page = restTemplate.postForObject("/v1/projects/1/app_service_instances/env_commands/frequencyTable?page=1&size=10&app_service_id=1&startTime=" + startTime + "&endTime=" + startTime, envIds, PageInfo.class)

        then: '校验返回值'
        page.getList().get(0)["appServiceName"] == "appName"
    }

    def "分页获取部署时长列表"() {
        given: 'appIds'
        List<Long> appIds = new ArrayList<>()
        appIds.add(1L)

        Calendar cal = Calendar.getInstance()
        String year = cal.get(Calendar.YEAR)
        String month = cal.get(Calendar.MONTH) + 1
        String day = cal.get(Calendar.DATE)
        String startTime = year + "/" + month + "/" + day

        when: '获取部署时长报表table'
        def page = restTemplate.postForObject("/v1/projects/1/app_service_instances/env_commands/timeTable?page=0&size=10&envId=1&startTime=" + startTime + "&endTime=" + startTime, appIds, PageInfo.class)

        then: '校验返回值'
        page.getList().get(0)["appServiceName"] == "appName"
    }

    // 部署自动化测试应用
    def "deployTestApp"() {
        given: "准备数据"
        def url = MAPPING + "/deploy_test_app"
        AppServiceDeployVO applicationDeployDTO = new AppServiceDeployVO()
        applicationDeployDTO.setEnvironmentId(1L)
        applicationDeployDTO.setValues(appServiceVersionValueDTO.getValue())
        applicationDeployDTO.setAppServiceId(1L)
        applicationDeployDTO.setAppServiceVersionId(1L)
        applicationDeployDTO.setType("create")
        applicationDeployDTO.setInstanceId(1L)

        when: '部署应用'
        restTemplate.postForObject(url, applicationDeployDTO, Object.class, 1L)

        then: '校验'
        noExceptionThrown()
    }

    def "operatePodCount"() {
        given: "准备数据"
        def url = MAPPING + "/operate_pod_count?name={name}&envId={envId}&count={count}"
        def map = new HashMap<String, Object>()
        map.put("project_id", PROJECT_ID)
        map.put("envId", devopsEnvironmentDTO.getId())
        map.put("name", "test")
        map.put("count", 2)

        when: '测试pod增加或减少'
        restTemplate.put(url, null, map)

        then: '测试有没异常抛出'
        notThrown(CommonException)
    }

    def "queryByCommandId"() {
        given: "准备数据"
        isToClean = true
        def url = MAPPING + "/query_by_command/{command_id}"
        def map = new HashMap<String, Object>()
        map.put("project_id", PROJECT_ID)
        map.put("command_id", 1000L)

        when: "调用方法"
        def value = restTemplate.getForObject(url, AppServiceInstanceRepVO, map)

        then: "校验结果"
        value != null
        value.getInstanceId() == 1000L
    }
}
