package io.choerodon.devops.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.pagehelper.PageInfo
import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration

import io.choerodon.devops.api.vo.*
import io.choerodon.devops.api.vo.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.vo.iam.RoleDTO


import io.choerodon.devops.domain.application.valueobject.ReplaceResult
import io.choerodon.devops.domain.application.valueobject.RepositoryFile
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.dataobject.gitlab.MemberDTO
<<<<<<< HEAD
=======
import io.choerodon.devops.infra.dataobject.gitlab.PipelineDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.dataobject.iam.UserDO
<<<<<<< HEAD
>>>>>>> [IMP] 修改AppControler重构
=======
import io.choerodon.devops.infra.dto.gitlab.UserDTO
>>>>>>> [IMP]修改后端结构
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.*
import io.choerodon.websocket.Msg
import io.choerodon.websocket.helper.CommandSender
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

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/11/9
 * Time: 16:13
 * Description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ApplicationInstanceController)
@Stepwise
class ApplicationInstanceControllerSpec extends Specification {

    private static final String MAPPING = "/v1/projects/{project_id}/app_instances"

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil
    @Autowired
    @Qualifier("mockEnvListener")
    private EnvListener envListener
    @Autowired
    @Qualifier("mockGitUtil")
    private GitUtil gitUtil
    @Autowired
    @Qualifier("mockCommandSender")
    private CommandSender commandSender

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private ApplicationShareMapper applicationMarketMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvResourceMapper devopsEnvResourceMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private DevopsEnvCommandValueMapper devopsEnvCommandValueMapper
    @Autowired
    private DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper
    @Autowired
    private ApplicationVersionValueMapper applicationVersionValueMapper
    @Autowired
    private DevopsCommandEventMapper devopsCommandEventMapper
    @Autowired
    private DevopsEnvCommandLogMapper devopsEnvCommandLogMapper
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper
    @Autowired
    private DevopsEnvFileMapper devopsEnvFileMapper

    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    ApplicationDTO applicationDO = new ApplicationDTO()
    @Shared
    DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO()
    @Shared
    DevopsAppShareDO devopsAppMarketDO = new DevopsAppShareDO()
    @Shared
    DevopsEnvCommandDO devopsEnvCommandDO = new DevopsEnvCommandDO()
    @Shared
    DevopsEnvResourceDO devopsEnvResourceDO = new DevopsEnvResourceDO()
    @Shared
    DevopsEnvResourceDO devopsEnvResourceDO2 = new DevopsEnvResourceDO()
    @Shared
    DevopsEnvResourceDO devopsEnvResourceDO3 = new DevopsEnvResourceDO()
    @Shared
    DevopsEnvResourceDO devopsEnvResourceDO4 = new DevopsEnvResourceDO()
    @Shared
    DevopsEnvResourceDO devopsEnvResourceDO5 = new DevopsEnvResourceDO()
    @Shared
    DevopsEnvResourceDO devopsEnvResourceDO6 = new DevopsEnvResourceDO()
    @Shared
    DevopsEnvResourceDO devopsEnvResourceDO7 = new DevopsEnvResourceDO()
    @Shared
    DevopsEnvResourceDO devopsEnvResourceDO8 = new DevopsEnvResourceDO()
    @Shared
    DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
    @Shared
    ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
    @Shared
    ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()
    @Shared
    DevopsEnvCommandValueDO devopsEnvCommandValueDO = new DevopsEnvCommandValueDO()
    @Shared
    ApplicationVersionValueDO applicationVersionValueDO = new ApplicationVersionValueDO()
    @Shared
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO2 = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO3 = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO4 = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO5 = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO6 = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO7 = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO8 = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDO()
    @Shared
    DevopsCommandEventDO devopsCommandEventDO = new DevopsCommandEventDO()
    @Shared
    DevopsEnvCommandLogDO devopsEnvCommandLogDO = new DevopsEnvCommandLogDO()
    @Shared
    DevopsEnvFileResourceDO devopsEnvFileResourceDO = new DevopsEnvFileResourceDO()
    @Shared
    DevopsEnvFileDO devopsEnvFileDO = new DevopsEnvFileDO()

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
        Map<String, Object> params = new HashMap<>()
        searchParam.put("searchParam", "")
        searchParam.put("param", "")

        // de
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setClusterId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setName("envName")
        devopsEnvironmentDO.setEnvIdRsa("test")
        devopsEnvironmentDO.setCode("envCode")
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)

        // dam
        devopsAppMarketDO.setPublishLevel("pub")
        devopsAppMarketDO.setContributor("con")
        devopsAppMarketDO.setDescription("des")

        // dav
        applicationVersionDO.setId(1L)
        applicationVersionDO.setAppId(1L)
        applicationVersionDO.setValueId(1L)
        applicationVersionDO.setVersion("version")

        // dp
        devopsEnvPodDO.setId(1L)
        devopsEnvPodDO.setReady(true)
        devopsEnvPodDO.setAppInstanceId(1L)
        devopsEnvPodDO.setStatus("Running")
        devopsEnvPodDO.setNamespace("envCode")
        devopsEnvPodDO.setNamespace("envCode")
        devopsEnvPodDO.setName("test-pod-123456-abcdef")

        // cmd
        devopsEnvCommandDO.setId(1L)
        devopsEnvCommandDO.setValueId(1L)
        devopsEnvCommandDO.setObjectId(1L)
        devopsEnvCommandDO.setError("error")
        devopsEnvCommandDO.setObject("instance")
        devopsEnvCommandDO.setStatus("operating")
        devopsEnvCommandDO.setObjectVersionId(1L)
        devopsEnvCommandDO.setCommandType("create")

        // decv
        devopsEnvCommandValueDO.setId(1L)
        devopsEnvCommandValueDO.setValue("---\n" +
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
        // da
        applicationDO.setId(1L)
        applicationDO.setProjectId(1L)
        applicationDO.setName("appName")
        applicationDO.setCode("appCode")
        applicationDO.setGitlabProjectId(1)

        // dai
        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setAppId(1L)
        applicationInstanceDO.setCommandId(1L)
        applicationInstanceDO.setAppVersionId(1L)
        applicationInstanceDO.setStatus("running")
        applicationInstanceDO.setCode("appInsCode")
        applicationInstanceDO.setObjectVersionNumber(1L)

        // davv
        applicationVersionValueDO.setId(1L)
        applicationVersionValueDO.setValue("---\n" +
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

        // deup
        devopsEnvUserPermissionDO.setIamUserId(1L)
        devopsEnvUserPermissionDO.setPermitted(true)
        devopsEnvUserPermissionDO.setEnvId(1L)

        // der
        devopsEnvResourceDO.setId(1L)
        devopsEnvResourceDO.setKind("Pod")
        devopsEnvResourceDO.setAppInstanceId(1L)
        devopsEnvResourceDO.setResourceDetailId(1L)
        devopsEnvResourceDO.setName("test-pod-123456-abcdef")

        devopsEnvResourceDO2.setId(2L)
        devopsEnvResourceDO2.setKind("Deployment")
        devopsEnvResourceDO2.setAppInstanceId(1L)
        devopsEnvResourceDO2.setResourceDetailId(2L)
        devopsEnvResourceDO2.setName("test-deployment")

        devopsEnvResourceDO3.setId(3L)
        devopsEnvResourceDO3.setKind("Service")
        devopsEnvResourceDO3.setAppInstanceId(1L)
        devopsEnvResourceDO3.setResourceDetailId(3L)
        devopsEnvResourceDO3.setName("test-service")

        devopsEnvResourceDO4.setId(4L)
        devopsEnvResourceDO4.setKind("Ingress")
        devopsEnvResourceDO4.setAppInstanceId(1L)
        devopsEnvResourceDO4.setResourceDetailId(4L)
        devopsEnvResourceDO4.setName("test-ingress")

        devopsEnvResourceDO5.setId(5L)
        devopsEnvResourceDO5.setKind("ReplicaSet")
        devopsEnvResourceDO5.setAppInstanceId(1L)
        devopsEnvResourceDO5.setResourceDetailId(5L)
        devopsEnvResourceDO5.setName("test-replicaset-123456-abcdef")

        devopsEnvResourceDO6.setId(6)
        devopsEnvResourceDO6.setKind("Job")
        devopsEnvResourceDO6.setAppInstanceId(1L)
        devopsEnvResourceDO6.setResourceDetailId(6L)

        devopsEnvResourceDO7.setId(7)
        devopsEnvResourceDO7.setKind("DaemonSet")
        devopsEnvResourceDO7.setName("DaemonSet")
        devopsEnvResourceDO7.setAppInstanceId(1L)
        devopsEnvResourceDO7.setResourceDetailId(7L)

        devopsEnvResourceDO8.setId(8)
        devopsEnvResourceDO8.setKind("StatefulSet")
        devopsEnvResourceDO8.setName("StatefulSet")
        devopsEnvResourceDO8.setAppInstanceId(1L)
        devopsEnvResourceDO8.setResourceDetailId(8L)

        // derd
        devopsEnvResourceDetailDO.setId(1L)
        devopsEnvResourceDetailDO.setMessage("{\"kind\":\"Pod\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"iam-service-56946b7b9f-42xnx\",\"generateName\":\"iam-service-56946b7b9f-\",\"namespace\":\"choerodon-devops-prod\",\"selfLink\":\"/api/v1/namespaces/choerodon-devops-prod/pods/iam-service-56946b7b9f-42xnx\",\"uid\":\"1667ab32-6b40-11e8-94ae-00163e0e2443\",\"resourceVersion\":\"4333254\",\"creationTimestamp\":\"2018-06-08T17:19:23Z\",\"labels\":{\"choerodon.io/metrics-port\":\"8031\",\"choerodon.io/release\":\"iam-service\",\"choerodon.io/service\":\"iam-service\",\"choerodon.io/version\":\"0.6.0\",\"pod-template-hash\":\"1250263659\"},\"annotation\":{\"choerodon.io/metrics-group\":\"spring-boot\",\"choerodon.io/metrics-path\":\"/prometheus\",\"kubernetes.io/created-by\":\"{\\\"kind\\\":\\\"SerializedReference\\\",\\\"apiVersion\\\":\\\"v1\\\",\\\"reference\\\":{\\\"kind\\\":\\\"ReplicaSet\\\",\\\"namespace\\\":\\\"choerodon-devops-prod\\\",\\\"name\\\":\\\"iam-service-56946b7b9f\\\",\\\"uid\\\":\\\"0f7ec2d5-6b40-11e8-94ae-00163e0e2443\\\",\\\"apiVersion\\\":\\\"extensions\\\",\\\"resourceVersion\\\":\\\"4332963\\\"}}\\n\"},\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"ReplicaSet\",\"name\":\"iam-service-56946b7b9f\",\"uid\":\"0f7ec2d5-6b40-11e8-94ae-00163e0e2443\",\"controller\":true,\"blockOwnerDeletion\":true}]},\"spec\":{\"volumes\":[{\"name\":\"default-token-mjcs5\",\"secret\":{\"secretName\":\"default-token-mjcs5\",\"defaultMode\":420}}],\"containers\":[{\"name\":\"iam-service\",\"image\":\"registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service:0.6.0\",\"ports\":[{\"name\":\"http\",\"containerPort\":8030,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"CHOERODON_EVENT_CONSUMER_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"EUREKA_CLIENT_SERVICEURL_DEFAULTZONE\",\"value\":\"http://register-server.choerodon-devops-prod:8000/eureka/\"},{\"name\":\"SPRING_CLOUD_CONFIG_ENABLED\",\"value\":\"true\"},{\"name\":\"SPRING_CLOUD_CONFIG_URI\",\"value\":\"http://config-server.choerodon-devops-prod:8010/\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES\",\"value\":\"172.19.136.81:2181,172.19.136.82:2181,172.19.136.83:2181\"},{\"name\":\"SPRING_DATASOURCE_PASSWORD\",\"value\":\"JAu9p8zL\"},{\"name\":\"SPRING_DATASOURCE_URL\",\"value\":\"jdbc:mysql://rm-uf65upic89q7007h5.mysql.rds.aliyuncs.com:3306/iam_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\"},{\"name\":\"SPRING_DATASOURCE_USERNAME\",\"value\":\"c7n_iam\"},{\"name\":\"SPRING_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"}],\"resources\":{\"limits\":{\"memory\":\"3Gi\"},\"requests\":{\"memory\":\"2Gi\"}},\"volumeMounts\":[{\"name\":\"default-token-mjcs5\",\"readOnly\":true,\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\"}],\"readinessProbe\":{\"exec\":{\"command\":[\"curl\",\"localhost:8031/health\"]},\"initialDelaySeconds\":60,\"timeoutSeconds\":10,\"periodSeconds\":10,\"successThreshold\":1,\"failureThreshold\":3},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"serviceAccountName\":\"default\",\"serviceAccount\":\"default\",\"nodeName\":\"choerodon2\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"},\"status\":{\"phase\":\"Running\",\"conditions\":[{\"type\":\"Initialized\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-06-08T17:19:23Z\"},{\"type\":\"Ready\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-06-08T17:20:30Z\"},{\"type\":\"PodScheduled\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-06-08T17:19:23Z\"}],\"hostIP\":\"172.19.136.82\",\"podIP\":\"192.168.2.209\",\"startTime\":\"2018-06-08T17:19:23Z\",\"containerStatuses\":[{\"name\":\"iam-service\",\"state\":{\"running\":{\"startedAt\":\"2018-06-08T17:19:24Z\"}},\"lastState\":{},\"ready\":true,\"restartCount\":0,\"image\":\"registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service:0.6.0\",\"imageID\":\"docker-pullable://registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service@sha256:ecf370e2623a62631499a7780c6851418b806018ed2d3ae2530f54cf638cb432\",\"containerID\":\"docker://2892c582b8109dff691df6190f8555cef1f9680e11d27864472bebb57962250b\"}],\"qosClass\":\"Burstable\"}}")

        devopsEnvResourceDetailDO2.setId(2L)
        devopsEnvResourceDetailDO2.setMessage("{\"apiVersion\":\"apps/v1beta2\",\"kind\":\"Deployment\",\"metadata\":{\"annotation\":{\"deployment.kubernetes.io/revision\":\"3\"},\"creationTimestamp\":\"2018-05-20T03:36:57Z\",\"generation\":5,\"labels\":{\"choerodon.io/logs-parser\":\"spring-boot\",\"choerodon.io/release\":\"iam-service\"},\"name\":\"iam-service\",\"namespace\":\"choerodon-devops-prod\",\"resourceVersion\":\"4333256\",\"selfLink\":\"/apis/apps/v1beta2/namespaces/choerodon-devops-prod/deployments/iam-service\",\"uid\":\"0c56c1b5-5bdf-11e8-a66e-00163e0e2443\"},\"spec\":{\"progressDeadlineSeconds\":600,\"replicas\":1,\"revisionHistoryLimit\":10,\"selector\":{\"matchLabels\":{\"choerodon.io/release\":\"iam-service\"}},\"strategy\":{\"rollingUpdate\":{\"maxSurge\":\"25%\",\"maxUnavailable\":\"25%\"},\"type\":\"RollingUpdate\"},\"template\":{\"metadata\":{\"annotation\":{\"choerodon.io/metrics-group\":\"spring-boot\",\"choerodon.io/metrics-path\":\"/prometheus\"},\"creationTimestamp\":null,\"labels\":{\"choerodon.io/metrics-port\":\"8031\",\"choerodon.io/release\":\"iam-service\",\"choerodon.io/service\":\"iam-service\",\"choerodon.io/version\":\"0.6.0\"}},\"spec\":{\"containers\":[{\"env\":[{\"name\":\"CHOERODON_EVENT_CONSUMER_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"EUREKA_CLIENT_SERVICEURL_DEFAULTZONE\",\"value\":\"http://register-server.choerodon-devops-prod:8000/eureka/\"},{\"name\":\"SPRING_CLOUD_CONFIG_ENABLED\",\"value\":\"true\"},{\"name\":\"SPRING_CLOUD_CONFIG_URI\",\"value\":\"http://config-server.choerodon-devops-prod:8010/\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES\",\"value\":\"172.19.136.81:2181,172.19.136.82:2181,172.19.136.83:2181\"},{\"name\":\"SPRING_DATASOURCE_PASSWORD\",\"value\":\"JAu9p8zL\"},{\"name\":\"SPRING_DATASOURCE_URL\",\"value\":\"jdbc:mysql://rm-uf65upic89q7007h5.mysql.rds.aliyuncs.com:3306/iam_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\"},{\"name\":\"SPRING_DATASOURCE_USERNAME\",\"value\":\"c7n_iam\"},{\"name\":\"SPRING_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"}],\"image\":\"registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service:0.6.0\",\"imagePullPolicy\":\"Always\",\"name\":\"iam-service\",\"ports\":[{\"containerPort\":8030,\"name\":\"http\",\"protocol\":\"TCP\"}],\"readinessProbe\":{\"exec\":{\"command\":[\"curl\",\"localhost:8031/health\"]},\"failureThreshold\":3,\"initialDelaySeconds\":60,\"periodSeconds\":10,\"successThreshold\":1,\"timeoutSeconds\":10},\"resources\":{\"limits\":{\"memory\":\"3Gi\"},\"requests\":{\"memory\":\"2Gi\"}},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\"}],\"dnsPolicy\":\"ClusterFirst\",\"restartPolicy\":\"Always\",\"schedulerName\":\"default-scheduler\",\"securityContext\":{},\"terminationGracePeriodSeconds\":30}}},\"status\":{\"availableReplicas\":1,\"conditions\":[{\"lastTransitionTime\":\"2018-05-20T03:36:57Z\",\"lastUpdateTime\":\"2018-06-08T17:19:11Z\",\"message\":\"ReplicaSet \\\"iam-service-56946b7b9f\\\" has successfully progressed.\",\"reason\":\"NewReplicaSetAvailable\",\"status\":\"True\",\"type\":\"Progressing\"},{\"lastTransitionTime\":\"2018-06-08T17:20:30Z\",\"lastUpdateTime\":\"2018-06-08T17:20:30Z\",\"message\":\"Deployment has minimum availability.\",\"reason\":\"MinimumReplicasAvailable\",\"status\":\"True\",\"type\":\"Available\"}],\"observedGeneration\":5,\"readyReplicas\":1,\"replicas\":1,\"updatedReplicas\":1}}")

        devopsEnvResourceDetailDO3.setId(3L)
        devopsEnvResourceDetailDO3.setMessage("{\"apiVersion\":\"v1\",\"kind\":\"Service\",\"metadata\":{\"creationTimestamp\":\"2018-05-20T03:29:11Z\",\"labels\":{\"choerodon.io/release\":\"config-server\"},\"name\":\"config-server\",\"namespace\":\"choerodon-devops-prod\",\"resourceVersion\":\"4325981\",\"selfLink\":\"/api/v1/namespaces/choerodon-devops-prod/services/config-server\",\"uid\":\"f68d3f07-5bdd-11e8-a66e-00163e0e2443\"},\"spec\":{\"clusterIP\":\"192.168.28.13\",\"ports\":[{\"name\":\"http\",\"port\":8010,\"protocol\":\"TCP\",\"targetPort\":\"http\"}],\"selector\":{\"choerodon.io/release\":\"config-server\"},\"sessionAffinity\":\"None\",\"type\":\"ClusterIP\"},\"status\":{\"loadBalancer\":{}}}")

        devopsEnvResourceDetailDO4.setId(4L)
        devopsEnvResourceDetailDO4.setMessage("{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"Ingress\",\"metadata\":{\"creationTimestamp\":\"2018-05-20T03:48:33Z\",\"generation\":1,\"labels\":{\"choerodon.io/release\":\"devops-service\"},\"name\":\"devops-service\",\"namespace\":\"choerodon-devops-prod\",\"resourceVersion\":\"4337962\",\"selfLink\":\"/apis/extensions/v1beta1/namespaces/choerodon-devops-prod/ingresses/devops-service\",\"uid\":\"aadd986d-5be0-11e8-a66e-00163e0e2443\"},\"spec\":{\"rules\":[{\"host\":\"devops.service.choerodon.com.cn\",\"http\":{\"paths\":[{\"backend\":{\"serviceName\":\"devops-service\",\"servicePort\":8060},\"path\":\"/\"}]}}]},\"status\":{\"loadBalancer\":{\"ingress\":[{}]}}}")

        devopsEnvResourceDetailDO5.setId(5L)
        devopsEnvResourceDetailDO5.setMessage("{\"metadata\":{\"name\":\"springboot-14f93-55f7896455\",\"namespace\":\"ljt\",\"selfLink\":\"/apis/extensions/v1beta1/namespaces/ljt/replicasets/springboot-14f93-55f7896455\",\"uid\":\"5553489b-6c9b-11e8-ad82-525400d91faf\",\"resourceVersion\":\"24136809\",\"generation\":5,\"creationTimestamp\":\"2018-06-10T10:45:04Z\",\"labels\":{\"choerodon.io/application\":\"springboot\",\"choerodon.io/release\":\"springboot-14f93\",\"choerodon.io/version\":\"0.1.0-dev.20180530070103\",\"pod-template-hash\":\"1193452011\"},\"annotation\":{\"deployment.kubernetes.io/desired-replicas\":\"1\",\"deployment.kubernetes.io/max-replicas\":\"2\",\"deployment.kubernetes.io/revision\":\"5\",\"deployment.kubernetes.io/revision-history\":\"1,3\"},\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"Deployment\",\"name\":\"springboot-14f93\",\"uid\":\"5550ab04-6c9b-11e8-8371-6a12b79743a2\",\"controller\":true,\"blockOwnerDeletion\":true}]},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"choerodon.io/release\":\"springboot-14f93\",\"pod-template-hash\":\"1193452011\"}},\"template\":{\"metadata\":{\"creationTimestamp\":null,\"labels\":{\"choerodon.io/application\":\"springboot\",\"choerodon.io/release\":\"springboot-14f93\",\"choerodon.io/version\":\"0.1.0-dev.20180530070103\",\"pod-template-hash\":\"1193452011\"}},\"spec\":{\"containers\":[{\"name\":\"springboot-14f93\",\"image\":\"registry.saas.test.com/operation-ystest1805192/springboot:0.1.0-dev.20180530070103\",\"ports\":[{\"name\":\"http\",\"containerPort\":8080,\"protocol\":\"TCP\"}],\"resources\":{\"limits\":{\"memory\":\"500Mi\"},\"requests\":{\"memory\":\"256Mi\"}},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"}}},\"status\":{\"replicas\":1,\"fullyLabeledReplicas\":1,\"readyReplicas\":1,\"availableReplicas\":1,\"observedGeneration\":5}}")

        devopsEnvResourceDetailDO6.setId(6L)
        devopsEnvResourceDetailDO6.setMessage("{\"metadata\":{\"name\":\"cctestws-7bc7a-init-db\",\"namespace\":\"ljt\",\"selfLink\":\"/apis/batch/v1/namespaces/ljt/jobs/cctestws-7bc7a-init-db\",\"uid\":\"f56fcbf4-6d24-11e8-8371-6a12b79743a2\",\"resourceVersion\":\"19435339\",\"creationTimestamp\":\"2018-06-11T03:10:13Z\",\"labels\":{\"choerodon.io/release\":\"cctestws-7bc7a\"},\"annotation\":{\"helm.sh/hook\":\"pre-install,pre-upgrade\",\"helm.sh/hook-weight\":\"1\"}},\"spec\":{\"parallelism\":1,\"completions\":1,\"activeDeadlineSeconds\":120,\"backoffLimit\":1,\"selector\":{\"matchLabels\":{\"controller-uid\":\"f56fcbf4-6d24-11e8-8371-6a12b79743a2\"}},\"template\":{\"metadata\":{\"name\":\"cctestws-7bc7a-init-db\",\"creationTimestamp\":null,\"labels\":{\"controller-uid\":\"f56fcbf4-6d24-11e8-8371-6a12b79743a2\",\"job-name\":\"cctestws-7bc7a-init-db\"}},\"spec\":{\"volumes\":[{\"name\":\"tools-jar\",\"emptyDir\":{}}],\"initContainers\":[{\"name\":\"tools\",\"image\":\"registry.cn-hangzhou.aliyuncs.com/choerodon-tools/dbtool:0.5.0\",\"command\":[\"sh\",\"-c\",\"cp -rf /var/choerodon/* /tools\"],\"resources\":{},\"volumeMounts\":[{\"name\":\"tools-jar\",\"mountPath\":\"/tools\"}],\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"containers\":[{\"name\":\"cctestws-7bc7a-init-db\",\"image\":\"registry.saas.test.com/operation-ystest1805192/cctestws:1.8.1-hotfix-we.20180608135220\",\"command\":[\"/bin/sh\",\"-c\",\" java -Dspring.datasource.url=\\\"jdbc:mysql://192.168.12.175:3306/demo_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\\\" -Dspring.datasource.username=root -Dspring.datasource.password=choerodon -Ddata.init=true -Ddata.jar=/cctestws.jar -jar /var/choerodon/choerodon-tool-liquibase.jar; \"],\"resources\":{},\"volumeMounts\":[{\"name\":\"tools-jar\",\"mountPath\":\"/var/choerodon\"}],\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"IfNotPresent\"}],\"restartPolicy\":\"Never\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"}}},\"status\":{\"conditions\":[{\"type\":\"Failed\",\"status\":\"True\",\"lastProbeTime\":\"2018-06-11T03:10:34Z\",\"lastTransitionTime\":\"2018-06-11T03:10:34Z\",\"reason\":\"BackoffLimitExceeded\",\"message\":\"Job has reach the specified backoff limit\"}],\"startTime\":\"2018-06-11T03:10:13Z\",\"failed\":1}}")

        devopsEnvResourceDetailDO7.setId(7L)
        devopsEnvResourceDetailDO7.setMessage("{\"apiVersion\":\"v1\",\"kind\":\"Pod\",\"metadata\":{\"creationTimestamp\":\"2018-12-18T08:03:42Z\",\"generateName\":\"daemonset-name-\",\"labels\":{\"controller-revision-hash\":\"608462091\",\"name\":\"fluentd-elasticsearch\",\"pod-template-generation\":\"1\"},\"name\":\"daemonset-name-hd24n\",\"namespace\":\"default\",\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"blockOwnerDeletion\":true,\"controller\":true,\"kind\":\"DaemonSet\",\"name\":\"daemonset-name\",\"uid\":\"6f6bb78b-029b-11e9-a58a-0800272ecd84\"}],\"resourceVersion\":\"47798\",\"selfLink\":\"/api/v1/namespaces/default/pods/daemonset-name-hd24n\",\"uid\":\"6f9b9e49-029b-11e9-a58a-0800272ecd84\"},\"spec\":{\"containers\":[{\"image\":\"nginx\",\"imagePullPolicy\":\"Always\",\"name\":\"container-name\",\"resources\":{\"limits\":{\"memory\":\"200Mi\"},\"requests\":{\"cpu\":\"100m\",\"memory\":\"200Mi\"}},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"volumeMounts\":[{\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\",\"name\":\"default-token-rmhmg\",\"readOnly\":true}]}],\"dnsPolicy\":\"ClusterFirst\",\"nodeName\":\"minikube\",\"restartPolicy\":\"Always\",\"schedulerName\":\"default-scheduler\",\"securityContext\":{},\"serviceAccount\":\"default\",\"serviceAccountName\":\"default\",\"terminationGracePeriodSeconds\":30,\"tolerations\":[{\"effect\":\"NoSchedule\",\"key\":\"node-role.kubernetes.io/master\"},{\"effect\":\"NoExecute\",\"key\":\"node.kubernetes.io/not-ready\",\"operator\":\"Exists\"},{\"effect\":\"NoExecute\",\"key\":\"node.kubernetes.io/unreachable\",\"operator\":\"Exists\"},{\"effect\":\"NoSchedule\",\"key\":\"node.kubernetes.io/disk-pressure\",\"operator\":\"Exists\"},{\"effect\":\"NoSchedule\",\"key\":\"node.kubernetes.io/memory-pressure\",\"operator\":\"Exists\"}],\"volumes\":[{\"name\":\"default-token-rmhmg\",\"secret\":{\"defaultMode\":420,\"secretName\":\"default-token-rmhmg\"}}]},\"status\":{\"conditions\":[{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:03:42Z\",\"status\":\"True\",\"type\":\"Initialized\"},{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:03:51Z\",\"status\":\"True\",\"type\":\"Ready\"},{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:03:46Z\",\"status\":\"True\",\"type\":\"PodScheduled\"}],\"containerStatuses\":[{\"containerID\":\"docker://60289d333e7dc5e74665768e807bc024c6c76670ae2ca041aa6b602bb4b1b791\",\"image\":\"nginx:latest\",\"imageID\":\"docker-pullable://nginx@sha256:5d32f60db294b5deb55d078cd4feb410ad88e6fe77500c87d3970eca97f54dba\",\"lastState\":{},\"name\":\"container-name\",\"ready\":true,\"restartCount\":0,\"state\":{\"running\":{\"startedAt\":\"2018-12-18T08:03:50Z\"}}}],\"hostIP\":\"192.168.99.100\",\"phase\":\"Running\",\"podIP\":\"172.17.0.4\",\"qosClass\":\"Burstable\",\"startTime\":\"2018-12-18T08:03:42Z\"}}")


        devopsEnvResourceDetailDO8.setId(8L)
        devopsEnvResourceDetailDO8.setMessage("{\"apiVersion\":\"v1\",\"kind\":\"Pod\",\"metadata\":{\"creationTimestamp\":\"2018-12-18T08:06:20Z\",\"generateName\":\"web-\",\"labels\":{\"app\":\"nginx\",\"controller-revision-hash\":\"web-5fbd4bb9cc\",\"statefulset.kubernetes.io/pod-name\":\"web-0\"},\"name\":\"web-0\",\"namespace\":\"default\",\"ownerReferences\":[{\"apiVersion\":\"apps/v1beta1\",\"blockOwnerDeletion\":true,\"controller\":true,\"kind\":\"StatefulSet\",\"name\":\"web\",\"uid\":\"cd21c557-029b-11e9-a58a-0800272ecd84\"}],\"resourceVersion\":\"47922\",\"selfLink\":\"/api/v1/namespaces/default/pods/web-0\",\"uid\":\"cd6833e3-029b-11e9-a58a-0800272ecd84\"},\"spec\":{\"containers\":[{\"image\":\"nginx\",\"imagePullPolicy\":\"Always\",\"name\":\"nginx\",\"ports\":[{\"containerPort\":80,\"name\":\"web\",\"protocol\":\"TCP\"}],\"resources\":{},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"volumeMounts\":[{\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\",\"name\":\"default-token-rmhmg\",\"readOnly\":true}]}],\"dnsPolicy\":\"ClusterFirst\",\"hostname\":\"web-0\",\"nodeName\":\"minikube\",\"restartPolicy\":\"Always\",\"schedulerName\":\"default-scheduler\",\"securityContext\":{},\"serviceAccount\":\"default\",\"serviceAccountName\":\"default\",\"subdomain\":\"nginx\",\"terminationGracePeriodSeconds\":10,\"volumes\":[{\"name\":\"default-token-rmhmg\",\"secret\":{\"defaultMode\":420,\"secretName\":\"default-token-rmhmg\"}}]},\"status\":{\"conditions\":[{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:06:20Z\",\"status\":\"True\",\"type\":\"Initialized\"},{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:06:28Z\",\"status\":\"True\",\"type\":\"Ready\"},{\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-18T08:06:20Z\",\"status\":\"True\",\"type\":\"PodScheduled\"}],\"containerStatuses\":[{\"containerID\":\"docker://dc43b31f553024a98401891a79084bc5be99a0338b0e0666f678854c1770850d\",\"image\":\"nginx:latest\",\"imageID\":\"docker-pullable://nginx@sha256:5d32f60db294b5deb55d078cd4feb410ad88e6fe77500c87d3970eca97f54dba\",\"lastState\":{},\"name\":\"nginx\",\"ready\":true,\"restartCount\":0,\"state\":{\"running\":{\"startedAt\":\"2018-12-18T08:06:26Z\"}}}],\"hostIP\":\"192.168.99.100\",\"phase\":\"Running\",\"podIP\":\"172.17.0.4\",\"qosClass\":\"BestEffort\",\"startTime\":\"2018-12-18T08:06:20Z\"}}")

        // dce
        devopsCommandEventDO.setId(1L)
        devopsCommandEventDO.setType("Job")
        devopsCommandEventDO.setCommandId(1L)
        devopsCommandEventDO.setName("commandEvent")

        // dcl
        devopsEnvCommandLogDO.setId(1L)
        devopsEnvCommandLogDO.setLog()
        devopsEnvCommandLogDO.setCommandId(1L)

        // defr
        devopsEnvFileResourceDO.setId(1L)
        devopsEnvFileResourceDO.setEnvId(1L)
        devopsEnvFileResourceDO.setResourceId(1L)
        devopsEnvFileResourceDO.setResourceType("C7NHelmRelease")
        devopsEnvFileResourceDO.setFilePath("filePath")

        // def
        devopsEnvFileDO.setId(1L)
        devopsEnvFileDO.setEnvId(1L)
        devopsEnvFileDO.setDevopsCommit("devopsCommit")
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClient", gitlabServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("testProject")
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)
        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        List<RoleDTO> roleDTOList = new ArrayList<>()
        RoleDTO roleDTO = new RoleDTO()
        roleDTO.setCode("role/project/default/project-owner")
        roleDTOList.add(roleDTO)
        List<ProjectWithRoleDTO> projectWithRoleDTOList = new ArrayList<>()
        ProjectWithRoleDTO projectWithRoleDTO = new ProjectWithRoleDTO()
        projectWithRoleDTO.setName("pro")
        projectWithRoleDTO.setRoles(roleDTOList)
        projectWithRoleDTOList.add(projectWithRoleDTO)
        PageInfo<ProjectWithRoleDTO> projectWithRoleDTOPage = new PageInfo(projectWithRoleDTOList)
        ResponseEntity<PageInfo<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        List<ProjectDO> projectDOList = new ArrayList<>()
        projectDOList.add(projectDO)
        PageInfo<ProjectDO> projectDOPage = new PageInfo(projectDOList)
        ResponseEntity<PageInfo<ProjectDO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
        Mockito.doReturn(projectDOPageResponseEntity).when(iamServiceClient).queryProjectByOrgId(anyLong(), anyInt(), anyInt(), isNull(), isNull())

        MemberDTO memberDO = new MemberDTO()
        memberDO.setId(1)
        memberDO.setAccessLevel(AccessLevel.OWNER)
        ResponseEntity<MemberDTO> responseEntity2 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(responseEntity2)

        UserDO userDO = new UserDO()
        userDO.setId(1L)
        userDO.setLoginName("test")
        userDO.setImageUrl("imageURL")
        ResponseEntity<UserDO> responseEntity3 = new ResponseEntity<>(userDO, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryByLoginName(anyString())).thenReturn(responseEntity3)

        List<PipelineDO> pipelineDOList = new ArrayList<>()
        PipelineDO pipelineDO = new PipelineDO()
        pipelineDO.setId(1)
        UserDTO gitlabUser = new UserDTO()
        pipelineDO.setRef("")
        pipelineDO.setUser(gitlabUser)
        gitlabUser.setId(1)
        gitlabUser.setName("gitlabTestName")
        pipelineDOList.add(pipelineDO)
        ResponseEntity<List<PipelineDO>> responseEntity4 = new ResponseEntity<>(pipelineDOList, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.listPipeline(anyInt(), anyInt())).thenReturn(responseEntity4)

        ResponseEntity<PipelineDO> responseEntity5 = new ResponseEntity<>(pipelineDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryPipeline(anyInt(), anyInt(), anyInt())).thenReturn(responseEntity5)

        ResponseEntity responseEntity6 = new ResponseEntity<>(HttpStatus.OK)
        Mockito.when(gitlabServiceClient.deleteFile(anyInt(), anyString(), anyString(), anyInt())).thenReturn(responseEntity6)

        RepositoryFile repositoryFile = new RepositoryFile();
        repositoryFile.setFilePath("test")
        ResponseEntity<RepositoryFile> responseEntity8 = new ResponseEntity<>(repositoryFile, HttpStatus.OK)

        Mockito.when(gitlabServiceClient.createFile(anyInt(), anyString(), anyString(), anyString(), anyInt())).thenReturn(responseEntity8)


        List<UserDO> userDOList = new ArrayList<>()
        UserDO userDO1 = new UserDO()
        userDO1.setId(1)
        userDO1.setLoginName("loginName")
        userDO1.setRealName("realName")
        userDO1.setImageUrl("imageUrl")
        userDOList.add(userDO1)
        ResponseEntity<List<UserDO>> responseEntity7 = new ResponseEntity<>(userDOList, HttpStatus.OK)
        Mockito.when(iamServiceClient.listUsersByIds(any(Long[].class))).thenReturn(responseEntity7)
        Mockito.doReturn(responseEntity7).when(iamServiceClient).listUsersByIds(1L)
    }

    def "PageByOptions"() {
        given: '初始化数据'
        applicationMapper.insert(applicationDO)
        devopsEnvPodMapper.insert(devopsEnvPodDO)
        applicationMarketMapper.insert(devopsAppMarketDO)
        devopsEnvCommandMapper.selectAll().forEach { devopsEnvCommandMapper.delete(it) }
        devopsEnvCommandMapper.insert(devopsEnvCommandDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        devopsEnvCommandValueMapper.insert(devopsEnvCommandValueDO)
        devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO)
        applicationVersionValueMapper.insert(applicationVersionValueDO)

        devopsEnvResourceMapper.insert(devopsEnvResourceDO)
        devopsEnvResourceMapper.insert(devopsEnvResourceDO2)
        devopsEnvResourceMapper.insert(devopsEnvResourceDO3)
        devopsEnvResourceMapper.insert(devopsEnvResourceDO4)
        devopsEnvResourceMapper.insert(devopsEnvResourceDO5)
        devopsEnvResourceMapper.insert(devopsEnvResourceDO6)
        devopsEnvResourceMapper.insert(devopsEnvResourceDO7)
        devopsEnvResourceMapper.insert(devopsEnvResourceDO8)

        applicationVersionMapper.insert(applicationVersionDO)
        applicationInstanceMapper.insert(applicationInstanceDO)

        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO2)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO3)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO4)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO5)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO6)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO7)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO8)

        devopsEnvCommandLogMapper.insert(devopsEnvCommandLogDO)
        devopsCommandEventMapper.insert(devopsCommandEventDO)
        devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO)
        devopsEnvFileMapper.insert(devopsEnvFileDO)

        and: '初始化请求头'
        String infra = "{\"searchParam\":{},\"param\":\"\"}"
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        and: 'mock envUtil'
        List<Long> connectedEnvList = new ArrayList<>()
        connectedEnvList.add(1L)
        envUtil.getConnectedEnvList() >> connectedEnvList
        envUtil.getUpdatedEnvList() >> connectedEnvList

        when: '分页查询应用部署'
        def page = restTemplate.postForObject("/v1/projects/1/app_instances/list_by_options?envId=1&appId=1&page=0&size=5", strEntity, Page.class)

        then: '校验返回值'
        page.get(0)["code"] == "appInsCode"
    }

    def "ListByAppId"() {
        when: '查询应用部署'
        def list = restTemplate.getForObject("/v1/projects/1/app_instances/all?appId=1", List.class)

        then: '校验返回值'
        list.get(0)["applicationName"] == "appName"
    }

    def "QueryValue"() {
        when: '获取部署 Value'
        def result = restTemplate.getForObject("/v1/projects/1/app_instances/1/value", ReplaceResult.class)

        then:
        result.getDeltaYaml().equals("")
    }


    def "QueryUpgradeValue"() {
        when: '获取升级 Value'
        def result = restTemplate.getForObject("/v1/projects/1/app_instances/1/appVersion/1/value", ReplaceResult.class)

        then:
        result.getDeltaYaml().equals("")
    }

    def "QueryValues"() {
        when: '查询value列表'
        def result = restTemplate.getForObject("/v1/projects/1/app_instances/value?appId=1&envId=1&appVersionId=1", ReplaceResult.class)

        then: '校验返回值'
        result.getDeltaYaml().equals("")
    }

    def "PreviewValues"() {
        given:
        ReplaceResult replaceResult = new ReplaceResult()
        replaceResult.setYaml(applicationVersionValueDO.getValue())

        when: '查询value列表'
        def result = restTemplate.postForObject("/v1/projects/1/app_instances/previewValue?appVersionId=1", replaceResult, ReplaceResult.class)

        then: '校验返回值'
        result.getDeltaYaml().equals("")
    }

    def "getDeploymentDetailsJsonByInstanceId"() {
        given: "初始化数据库"
        Map<String, Long> map = new HashMap<>(3)
        String deploymentName = "test-deployment"
        initForInstance(map, deploymentName)
        Long projectId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/deployment_detail_json?deployment_name=" + deploymentName, InstanceControllerDetailDTO, projectId, map.get("instanceId"))
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
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/deployment_detail_yaml?deployment_name=" + deploymentName, InstanceControllerDetailDTO, projectId, map.get("instanceId"))

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
        String daemonSetName = getDevopsEnvResourceDO7().getName()
        Long projectId = 1L
        Long instanceId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/daemon_set_detail_json?daemon_set_name=" + daemonSetName, InstanceControllerDetailDTO, projectId, instanceId)
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
        String daemonSetName = getDevopsEnvResourceDO7().getName()
        Long projectId = 1L
        Long instanceId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/daemon_set_detail_yaml?daemon_set_name=" + daemonSetName, InstanceControllerDetailDTO, projectId, instanceId)

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
        String statefulSetName = getDevopsEnvResourceDO8().getName()
        Long projectId = 1L
        Long instanceId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/stateful_set_detail_json?stateful_set_name=" + statefulSetName, InstanceControllerDetailDTO, projectId, instanceId)
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
        String statefulSetName = getDevopsEnvResourceDO8().getName()
        Long projectId = 1L
        Long instanceId = 1L

        when: '查询真实存在的数据'
        def entity = restTemplate.getForEntity(MAPPING + "/{appInstanceId}/stateful_set_detail_yaml?stateful_set_name=" + statefulSetName, InstanceControllerDetailDTO, projectId, instanceId)

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
        ApplicationInstanceDO instanceInit = new ApplicationInstanceDO()
        instanceInit.setCode("dependency-chart-59dad")
        instanceInit.setAppId(1L)
        instanceInit.setEnvId(1L)
        instanceInit.setCommandId(1L)
        applicationInstanceMapper.insert(instanceInit)

        DevopsEnvResourceDetailDO detailInit = new DevopsEnvResourceDetailDO()
        detailInit.setMessage("{\"metadata\":{\"name\":\"ins4\",\"namespace\":\"env1112\",\"selfLink\":\"/apis/extensions/v1beta1/namespaces/env1112/deployments/ins4\",\"uid\":\"d444dd68-f44c-11e8-aca1-525400d91faf\",\"resourceVersion\":\"69026386\",\"generation\":2,\"creationTimestamp\":\"2018-11-30T03:05:45Z\",\"labels\":{\"choerodon.io\":\"2018.11.30-105053-master\",\"choerodon.io/application\":\"code-i\",\"choerodon.io/logs-parser\":\"nginx\",\"choerodon.io/release\":\"ins4\",\"choerodon.io/version\":\"2018.11.20-135445-master\"},\"annotation\":{\"deployment.kubernetes.io/revision\":\"2\"}},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"choerodon.io/release\":\"ins4\"}},\"template\":{\"metadata\":{\"creationTimestamp\":null,\"labels\":{\"choerodon.io\":\"2018.11.30-105053-master\",\"choerodon.io/application\":\"code-i\",\"choerodon.io/release\":\"ins4\",\"choerodon.io/version\":\"2018.11.20-135445-master\"}},\"spec\":{\"containers\":[{\"name\":\"ins4\",\"image\":\"registry.saas.test.com/code-x-code-x/code-i:2018.11.20-135445-master\",\"ports\":[{\"name\":\"http\",\"containerPort\":80,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"PRO_API_HOST\",\"value\":\"api.example.com.cn\"},{\"name\":\"PRO_CLIENT_ID\",\"value\":\"example\"},{\"name\":\"PRO_COOKIE_SERVER\",\"value\":\"example.com.cn\"},{\"name\":\"PRO_HEADER_TITLE_NAME\",\"value\":\"Choerodon\"},{\"name\":\"PRO_HTTP\",\"value\":\"http\"},{\"name\":\"PRO_LOCAL\",\"value\":\"true\"},{\"name\":\"PRO_TITLE_NAME\",\"value\":\"Choerodon\"}],\"resources\":{},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"IfNotPresent\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"}},\"strategy\":{\"type\":\"RollingUpdate\",\"rollingUpdate\":{\"maxUnavailable\":\"25%\",\"maxSurge\":\"25%\"}},\"revisionHistoryLimit\":10,\"progressDeadlineSeconds\":600},\"status\":{\"observedGeneration\":2,\"replicas\":1,\"updatedReplicas\":1,\"readyReplicas\":1,\"availableReplicas\":1,\"conditions\":[{\"type\":\"Available\",\"status\":\"True\",\"lastUpdateTime\":\"2018-11-30T03:05:49Z\",\"lastTransitionTime\":\"2018-11-30T03:05:49Z\",\"reason\":\"MinimumReplicasAvailable\",\"message\":\"Deployment has minimum availability.\"},{\"type\":\"Progressing\",\"status\":\"True\",\"lastUpdateTime\":\"2018-12-02T08:20:37Z\",\"lastTransitionTime\":\"2018-11-30T03:05:45Z\",\"reason\":\"NewReplicaSetAvailable\",\"message\":\"ReplicaSet \\\"ins4-786469cf45\\\" has successfully progressed.\"}]}}")
        devopsEnvResourceDetailMapper.insert(detailInit)

        DevopsEnvResourceDO resourceInit = new DevopsEnvResourceDO()
        resourceInit.setAppInstanceId(instanceInit.getId())
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

    def "FormatValue"() {
        given: '初始化replaceResult'
        ReplaceResult result = new ReplaceResult()
        result.setYaml("env:\n" +
                "  open:\n" +
                "    PRO_API_HOST: api.example.com.cn1\n" +
                "preJob:\n" +
                "  preConfig:\n" +
                "    mysql:\n" +
                "      username: root\n" +
                "      host: 192.168.12.156\n" +
                "      password: handhand\n" +
                "      dbname: demo_service")

        when: '校验values'
        def list = restTemplate.postForObject("/v1/projects/1/app_instances/value_format", result, List.class)

        then: '校验返回值'
        list.isEmpty()
    }

    //部署实例
    def "Deploy"() {
        given: '初始化applicationDeployDTO'
        ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO()
        applicationDeployDTO.setEnvironmentId(1L)
        applicationDeployDTO.setValues(applicationVersionValueDO.getValue())
        applicationDeployDTO.setAppId(1L)
        applicationDeployDTO.setAppVersionId(1L)
        applicationDeployDTO.setType("create")
        applicationDeployDTO.setAppInstanceId(1L)
        applicationDeployDTO.setIsNotChange(false)

        and: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null

        and: 'mock gitUtil'
        gitUtil.cloneBySsh(_ as String, _ as String) >> null

        when: '部署应用'
        def dto = restTemplate.postForObject("/v1/projects/1/app_instances", applicationDeployDTO, ApplicationDeployDTO.class)

        then: '校验返回值'
        dto != null
    }

    // 部署自动化测试应用
    def "deployTestApp"() {
        given: "准备数据"
        def url = MAPPING + "/deploy_test_app"
        ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO()
        applicationDeployDTO.setEnvironmentId(1L)
        applicationDeployDTO.setValues(applicationVersionValueDO.getValue())
        applicationDeployDTO.setAppId(1L)
        applicationDeployDTO.setAppVersionId(1L)
        applicationDeployDTO.setType("create")
        applicationDeployDTO.setAppInstanceId(1L)

        when: '部署应用'
        restTemplate.postForObject(url, applicationDeployDTO, Object.class, 1L)

        then: '校验'
        noExceptionThrown()
    }

    def "ListByAppVersionId"() {
        when: '查询运行中的实例'
        def list = restTemplate.getForObject("/v1/projects/1/app_instances/options?envId=1&appId=1&appVersionId=1", List.class)

        then: '校验返回值'
        list.get(0)["code"] == "appInsCode"
    }

    def "ListByAppIdAndEnvId"() {
        when: '环境下某应用运行中或失败的实例'
        def list = restTemplate.getForObject("/v1/projects/1/app_instances/listByAppIdAndEnvId?envId=1&appId=1", List.class)

        then: '校验返回值'
        list.get(0)["code"] == "appInsCode"
    }


    def "ListResources"() {
        when: '获取部署实例资源对象'
        def dto = restTemplate.getForObject("/v1/projects/1/app_instances/1/resources", DevopsEnvResourceDTO.class)

        then: '校验返回值'
        dto.getPodDTOS().get(0)["name"] == "iam-service-56946b7b9f-42xnx"
        dto.getServiceDTOS().get(0)["name"] == "config-server"
        dto.getIngressDTOS().get(0)["name"] == "devops-service"
        dto.getDeploymentDTOS().get(0)["name"] == "iam-service"
        dto.getReplicaSetDTOS().get(0)["name"] == "springboot-14f93-55f7896455"
    }

    def "ListEvents"() {
        when: '获取部署实例Event事件'
        def list = restTemplate.getForObject(MAPPING + "/1/events", List.class, 1L)

        then: '校验返回值'
        list.get(0)["podEventDTO"].get(0)["name"] == "commandEvent"
    }

    def "Stop"() {
        given: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null

        when: '校验返回值'
        restTemplate.put("/v1/projects/1/app_instances/1/stop", null)

        then:
        devopsEnvCommandMapper.selectAll().get(2)["commandType"] == "stop"
    }

    def "Start"() {
        ApplicationInstanceDO applicationInstanceDO1 = applicationInstanceMapper.selectByPrimaryKey(1L)
        applicationInstanceDO1.setStatus(InstanceStatus.STOPPED.getStatus())
        applicationInstanceMapper.updateByPrimaryKeySelective(applicationInstanceDO1)

        when: '实例重启'
        restTemplate.put("/v1/projects/1/app_instances/1/start", null)

        then: '校验返回值'
        devopsEnvCommandMapper.selectAll().get(3)["commandType"] == "restart"
    }

    def "Restart"() {
        given: 'mock commandSender'
        commandSender.sendMsg(_ as Msg) >> null

        when: '实例重新部署'
        restTemplate.put("/v1/projects/1/app_instances/1/restart", null)

        then: '校验返回值'
        devopsEnvCommandMapper.selectAll().get(4)["commandType"] == "update"
    }

    def "operatePodCount"() {
        given:
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null

        when: '测试pod增加或减少'
        restTemplate.put("/v1/projects/1/app_instances/operate_pod_count?envId=1&deploymentName=test&count=2", null)

        then: '测试有没异常抛出'
        notThrown(CommonException)
    }


    def "Delete"() {
        given: 'mock envUtil'
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null

        and: 'mock gitUtil'
        gitUtil.cloneBySsh(_ as String, _ as String) >> null

        and: '手动创建文件目录'
        File file = new File("gitops/org/pro/envCode")
        file.mkdirs()

        when: '实例删除'
        restTemplate.delete("/v1/projects/1/app_instances/1/delete")

        then: '校验是否删除'
        devopsEnvCommandMapper.selectAll().get(5)["commandType"] == "delete"
    }

    def "CheckName"() {
        when: '校验实例名唯一性'
        def exception = restTemplate.getForEntity(MAPPING + "/check_name?instance_name=uniqueName", ExceptionResponse.class, 1L)

        then: '名字不存在不抛出异常'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }

    def "ListByEnv"() {
        given: 'mock envUtil'
        List<Long> connectedEnvList = new ArrayList<>()
        connectedEnvList.add(1L)
        envUtil.getConnectedEnvList() >> connectedEnvList
        envUtil.getUpdatedEnvList() >> connectedEnvList

        when: '环境总览实例查询'
        def dto = restTemplate.postForObject("/v1/projects/1/app_instances/1/listByEnv", "{\"searchParam\":{},\"param\":\"\"}", DevopsEnvPreviewDTO.class)

        then: '校验返回值'
        dto.getDevopsEnvPreviewAppDTOS().get(0)["appName"] == "appName"
    }

    def "ListDeployTime"() {
        given: 'appIds'
        List<Long> appIds = new ArrayList<>()
        appIds.add(1L)

        DevopsEnvCommandDO e = new DevopsEnvCommandDO()
        e.setId(999L)
        e.setObjectId(1L)
        e.setStatus("success")
        e.setCommandType("create")
        devopsEnvCommandMapper.insert(e)

        Calendar cal = Calendar.getInstance()
        String year = cal.get(Calendar.YEAR)
        String month = cal.get(Calendar.MONTH) + 1
        String day = cal.get(Calendar.DATE)
        String startTime = year + "/" + month + "/" + day

        when: '获取部署时长报表'
        def dto = restTemplate.postForObject("/v1/projects/1/app_instances/env_commands/time?envId=1&startTime=" + startTime + "&endTime=" + startTime, appIds, DeployTimeDTO.class)

        then: '校验返回值'
        dto.getDeployAppDTOS().get(0)["appName"] == "appName"
    }

    def "ListDeployFrequency"() {
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
        def dto = restTemplate.postForObject("/v1/projects/1/app_instances/env_commands/frequency?appId=1&startTime=" + startTime + "&endTime=" + startTime, envIds, DeployFrequencyDTO.class)

        then: '校验返回值'
        dto.getCreationDates().size() == 1
        dto.getDeployFrequencys().size() == 1
        dto.getDeploySuccessFrequency().size() == 1
        dto.getDeployFailFrequency().size() == 1
    }

    def "PageDeployFrequencyDetail"() {
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
        def page = restTemplate.postForObject("/v1/projects/1/app_instances/env_commands/frequencyDetail?page=0&size=10&appId=1&startTime=" + startTime + "&endTime=" + startTime, envIds, Page.class)

        then: '校验返回值'
        page.get(0)["appName"] == "appName"
    }

    def "PageDeployTimeDetail"() {
        given: 'appIds'
        List<Long> appIds = new ArrayList<>()
        appIds.add(1L)

        Calendar cal = Calendar.getInstance()
        String year = cal.get(Calendar.YEAR)
        String month = cal.get(Calendar.MONTH) + 1
        String day = cal.get(Calendar.DATE)
        String startTime = year + "/" + month + "/" + day

        when: '获取部署时长报表table'
        def page = restTemplate.postForObject("/v1/projects/1/app_instances/env_commands/timeDetail?page=0&size=10&envId=1&startTime=" + startTime + "&endTime=" + startTime, appIds, Page.class)

        then: '校验返回值'
        page.get(0)["appName"] == "appName"
    }


    def "listCommandLogs"() {

        when: '获取commandLogs操作信息'
        def page = restTemplate.postForObject("/v1/projects/1/app_instances/command_log/1", null, Page.class)

        then: '校验返回值'
        page.size() == 5
    }

    // 清除测试数据
    def "cleanupData"() {
        given:
        // 删除appInstance
        List<ApplicationInstanceDO> list = applicationInstanceMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (ApplicationInstanceDO e : list) {
                applicationInstanceMapper.delete(e)
            }
        }
        // 删除appMarket
        List<DevopsAppShareDO> list1 = applicationMarketMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsAppShareDO e : list1) {
                applicationMarketMapper.delete(e)
            }
        }
        // 删除envPod
        List<DevopsEnvPodDO> list2 = devopsEnvPodMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (DevopsEnvPodDO e : list2) {
                devopsEnvPodMapper.delete(e)
            }
        }
        // 删除appMarket
        List<DevopsAppShareDO> list3 = applicationMarketMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (DevopsAppShareDO e : list3) {
                applicationMarketMapper.delete(e)
            }
        }
        // 删除appVersion
        List<ApplicationVersionDO> list4 = applicationVersionMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (ApplicationVersionDO e : list4) {
                applicationVersionMapper.delete(e)
            }
        }
        // 删除appVersionValue
        List<ApplicationVersionValueDO> list5 = applicationVersionValueMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (ApplicationVersionValueDO e : list5) {
                applicationVersionValueMapper.delete(e)
            }
        }
        // 删除app
        List<ApplicationDTO> list6 = applicationMapper.selectAll()
        if (list6 != null && !list6.isEmpty()) {
            for (ApplicationDTO e : list6) {
                applicationMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDO> list7 = devopsEnvironmentMapper.selectAll()
        if (list7 != null && !list7.isEmpty()) {
            for (DevopsEnvironmentDO e : list7) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除envCommand
        List<DevopsEnvCommandDO> list8 = devopsEnvCommandMapper.selectAll()
        if (list8 != null && !list8.isEmpty()) {
            for (DevopsEnvCommandDO e : list8) {
                devopsEnvCommandMapper.delete(e)
            }
        }
        // 删除envCommandValue
        List<DevopsEnvCommandValueDO> list9 = devopsEnvCommandValueMapper.selectAll()
        if (list9 != null && !list9.isEmpty()) {
            for (DevopsEnvCommandValueDO e : list9) {
                devopsEnvCommandValueMapper.delete(e)
            }
        }
        // 删除envFile
        List<DevopsEnvFileDO> list10 = devopsEnvFileMapper.selectAll()
        if (list10 != null && !list10.isEmpty()) {
            for (DevopsEnvFileDO e : list10) {
                devopsEnvFileMapper.delete(e)
            }
        }
        // 删除envFileResource
        List<DevopsEnvFileResourceDO> list11 = devopsEnvFileResourceMapper.selectAll()
        if (list11 != null && !list11.isEmpty()) {
            for (DevopsEnvFileResourceDO e : list11) {
                devopsEnvFileResourceMapper.delete(e)
            }
        }
        // 删除envResource
        List<DevopsEnvResourceDO> list12 = devopsEnvResourceMapper.selectAll()
        if (list12 != null && !list12.isEmpty()) {
            for (DevopsEnvResourceDO e : list12) {
                devopsEnvResourceMapper.delete(e)
            }
        }
        // 删除envResourceDetail
        List<DevopsEnvResourceDetailDO> list13 = devopsEnvResourceDetailMapper.selectAll()
        if (list13 != null && !list13.isEmpty()) {
            for (DevopsEnvResourceDetailDO e : list13) {
                devopsEnvResourceDetailMapper.delete(e)
            }
        }
        // 删除envUserPermission
        List<DevopsEnvUserPermissionDO> list14 = devopsEnvUserPermissionMapper.selectAll()
        if (list14 != null && !list14.isEmpty()) {
            for (DevopsEnvUserPermissionDO e : list14) {
                devopsEnvUserPermissionMapper.delete(e)
            }
        }
        // 删除commandEvent
        List<DevopsCommandEventDO> list15 = devopsCommandEventMapper.selectAll()
        if (list15 != null && !list15.isEmpty()) {
            for (DevopsCommandEventDO e : list15) {
                devopsCommandEventMapper.delete(e)
            }
        }
        // 删除envCommandLog
        List<DevopsEnvCommandLogDO> list16 = devopsEnvCommandLogMapper.selectAll()
        if (list16 != null && !list16.isEmpty()) {
            for (DevopsEnvCommandLogDO e : list16) {
                devopsEnvCommandLogMapper.delete(e)
            }
        }
        FileUtil.deleteDirectory(new File("gitops"))
    }
}
