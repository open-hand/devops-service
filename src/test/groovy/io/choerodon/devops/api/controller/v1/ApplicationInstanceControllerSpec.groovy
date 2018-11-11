package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.dto.iam.RoleDTO
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.ReplaceResult
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.GitUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
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
 * Date: 2018/11/9
 * Time: 16:13
 * Description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ApplicationInstanceController)
@Stepwise
class ApplicationInstanceControllerSpec extends Specification {

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
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private ApplicationMarketMapper applicationMarketMapper
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

    @Shared
    ApplicationDO applicationDO = new ApplicationDO()
    @Shared
    DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO()
    @Shared
    DevopsAppMarketDO devopsAppMarketDO = new DevopsAppMarketDO()
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
    DevopsEnvResourceDO devopsEnvResourceDO0 = new DevopsEnvResourceDO()
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
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO0 = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDO()

    @Autowired
    private IamRepository iamRepository
//    @Autowired
//    private GitlabRepository gitlabRepository
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    // SagaClient sagaClient = Mockito.mock(SagaClient.class)
    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    def setupSpec() {
        // de
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setClusterId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setName("envName")
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
        devopsEnvCommandDO.setCommandType("commandType")

        // decv
        devopsEnvCommandValueDO.setId(1L)
        devopsEnvCommandValueDO.setValue("env:\n" +
                "  open:\n" +
                "    PRO_API_HOST: api.example.com.cn1\n" +
                "preJob:\n" +
                "  preConfig:\n" +
                "    mysql:\n" +
                "      username: root\n" +
                "      host: 192.168.12.156\n" +
                "      password: handhand\n" +
                "      dbname: demo_service")

        // da
        applicationDO.setId(1L)
        applicationDO.setProjectId(1L)
        applicationDO.setName("appName")

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
        applicationVersionValueDO.setValue("{\"image\":{\"tag\":\"0.1.0-dev.20180519090059\",\"repository\":\"registry.saas.hand-china.com/hand-rdc-choerodon/event-store-service\",\"pullPolicy\":\"Always\"},\"replicaCount\":1,\"service\":{\"port\":9010,\"enable\":false,\"type\":\"ClusterIP\"},\"resources\":{\"requests\":{\"memory\":\"2Gi\"},\"limits\":{\"memory\":\"3Gi\"}},\"metrics\":{\"path\":\"/prometheus\",\"label\":\"java-spring\"},\"env\":{\"open\":{\"SPRING_CLOUD_CONFIG_URI\":\"http://config-server.choerodon-devops-prod:8010/\",\"SPRING_CLOUD_STREAM_DEFAULT_BINDER\":\"kafka\",\"SPRING_CLOUD_CONFIG_ENABLED\":true,\"SPRING_DATASOURCE_PASSWORD\":\"handhand\",\"SPRING_DATASOURCE_URL\":\"jdbc:mysql://hapcloud-mysql.db:3306/event_store_service?useUnicode=true&characterEncoding=utf-8&useSSL=false\",\"SPRING_DATASOURCE_USERNAME\":\"root\",\"SPRING_KAFKA_BOOTSTRAP_SERVERS\":\"kafka-0.kafka-headless.kafka.svc.cluster.local:9092,kafka-1.kafka-headless.kafka.svc.cluster.local:9092,kafka-2.kafka-headless.kafka.svc.cluster.local:9092\",\"SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS\":\"kafka-0.kafka-headless.kafka.svc.cluster.local:9092,kafka-1.kafka-headless.kafka.svc.cluster.local:9092,kafka-2.kafka-headless.kafka.svc.cluster.local:9092\",\"SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES\":\"zookeeper-0.zookeeper-headless.zookeeper.svc.cluster.local:2181,zookeeper-1.zookeeper-headless.zookeeper.svc.cluster.local:2181,zookeeper-2.zookeeper-headless.zookeeper.svc.cluster.local:2181\",\"EUREKA_CLIENT_SERVICEURL_DEFAULTZONE\":\"http://register-server.choerodon-devops-prod:8000/eureka/\"}},\"logs\":{\"parser\":\"java-spring\"},\"preJob\":{\"preConfig\":{\"mysql\":{\"database\":\"manager_service\",\"password\":\"handhand\",\"port\":3306,\"host\":\"hapcloud-mysql.db\",\"username\":\"root\"}},\"preInitDB\":{\"mysql\":{\"database\":\"event_store_service\",\"password\":\"handhand\",\"port\":3306,\"host\":\"hapcloud-mysql.db\",\"username\":\"root\"}}},\"deployment\":{\"managementPort\":9011}}")

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

        devopsEnvResourceDO0.setId(6)
        devopsEnvResourceDO0.setKind(null)
        devopsEnvResourceDO0.setAppInstanceId(1L)
        devopsEnvResourceDO0.setResourceDetailId(6L)

        // derd
        devopsEnvResourceDetailDO.setId(1L)
        devopsEnvResourceDetailDO.setMessage("{\"kind\":\"Pod\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"iam-service-56946b7b9f-42xnx\",\"generateName\":\"iam-service-56946b7b9f-\",\"namespace\":\"choerodon-devops-prod\",\"selfLink\":\"/api/v1/namespaces/choerodon-devops-prod/pods/iam-service-56946b7b9f-42xnx\",\"uid\":\"1667ab32-6b40-11e8-94ae-00163e0e2443\",\"resourceVersion\":\"4333254\",\"creationTimestamp\":\"2018-06-08T17:19:23Z\",\"labels\":{\"choerodon.io/metrics-port\":\"8031\",\"choerodon.io/release\":\"iam-service\",\"choerodon.io/service\":\"iam-service\",\"choerodon.io/version\":\"0.6.0\",\"pod-template-hash\":\"1250263659\"},\"annotations\":{\"choerodon.io/metrics-group\":\"spring-boot\",\"choerodon.io/metrics-path\":\"/prometheus\",\"kubernetes.io/created-by\":\"{\\\"kind\\\":\\\"SerializedReference\\\",\\\"apiVersion\\\":\\\"v1\\\",\\\"reference\\\":{\\\"kind\\\":\\\"ReplicaSet\\\",\\\"namespace\\\":\\\"choerodon-devops-prod\\\",\\\"name\\\":\\\"iam-service-56946b7b9f\\\",\\\"uid\\\":\\\"0f7ec2d5-6b40-11e8-94ae-00163e0e2443\\\",\\\"apiVersion\\\":\\\"extensions\\\",\\\"resourceVersion\\\":\\\"4332963\\\"}}\\n\"},\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"ReplicaSet\",\"name\":\"iam-service-56946b7b9f\",\"uid\":\"0f7ec2d5-6b40-11e8-94ae-00163e0e2443\",\"controller\":true,\"blockOwnerDeletion\":true}]},\"spec\":{\"volumes\":[{\"name\":\"default-token-mjcs5\",\"secret\":{\"secretName\":\"default-token-mjcs5\",\"defaultMode\":420}}],\"containers\":[{\"name\":\"iam-service\",\"image\":\"registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service:0.6.0\",\"ports\":[{\"name\":\"http\",\"containerPort\":8030,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"CHOERODON_EVENT_CONSUMER_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"EUREKA_CLIENT_SERVICEURL_DEFAULTZONE\",\"value\":\"http://register-server.choerodon-devops-prod:8000/eureka/\"},{\"name\":\"SPRING_CLOUD_CONFIG_ENABLED\",\"value\":\"true\"},{\"name\":\"SPRING_CLOUD_CONFIG_URI\",\"value\":\"http://config-server.choerodon-devops-prod:8010/\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES\",\"value\":\"172.19.136.81:2181,172.19.136.82:2181,172.19.136.83:2181\"},{\"name\":\"SPRING_DATASOURCE_PASSWORD\",\"value\":\"JAu9p8zL\"},{\"name\":\"SPRING_DATASOURCE_URL\",\"value\":\"jdbc:mysql://rm-uf65upic89q7007h5.mysql.rds.aliyuncs.com:3306/iam_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\"},{\"name\":\"SPRING_DATASOURCE_USERNAME\",\"value\":\"c7n_iam\"},{\"name\":\"SPRING_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"}],\"resources\":{\"limits\":{\"memory\":\"3Gi\"},\"requests\":{\"memory\":\"2Gi\"}},\"volumeMounts\":[{\"name\":\"default-token-mjcs5\",\"readOnly\":true,\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\"}],\"readinessProbe\":{\"exec\":{\"command\":[\"curl\",\"localhost:8031/health\"]},\"initialDelaySeconds\":60,\"timeoutSeconds\":10,\"periodSeconds\":10,\"successThreshold\":1,\"failureThreshold\":3},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"serviceAccountName\":\"default\",\"serviceAccount\":\"default\",\"nodeName\":\"choerodon2\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"},\"status\":{\"phase\":\"Running\",\"conditions\":[{\"type\":\"Initialized\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-06-08T17:19:23Z\"},{\"type\":\"Ready\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-06-08T17:20:30Z\"},{\"type\":\"PodScheduled\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-06-08T17:19:23Z\"}],\"hostIP\":\"172.19.136.82\",\"podIP\":\"192.168.2.209\",\"startTime\":\"2018-06-08T17:19:23Z\",\"containerStatuses\":[{\"name\":\"iam-service\",\"state\":{\"running\":{\"startedAt\":\"2018-06-08T17:19:24Z\"}},\"lastState\":{},\"ready\":true,\"restartCount\":0,\"image\":\"registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service:0.6.0\",\"imageID\":\"docker-pullable://registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service@sha256:ecf370e2623a62631499a7780c6851418b806018ed2d3ae2530f54cf638cb432\",\"containerID\":\"docker://2892c582b8109dff691df6190f8555cef1f9680e11d27864472bebb57962250b\"}],\"qosClass\":\"Burstable\"}}")

        devopsEnvResourceDetailDO2.setId(2L)
        devopsEnvResourceDetailDO2.setMessage("{\"apiVersion\":\"apps/v1beta2\",\"kind\":\"Deployment\",\"metadata\":{\"annotations\":{\"deployment.kubernetes.io/revision\":\"3\"},\"creationTimestamp\":\"2018-05-20T03:36:57Z\",\"generation\":5,\"labels\":{\"choerodon.io/logs-parser\":\"spring-boot\",\"choerodon.io/release\":\"iam-service\"},\"name\":\"iam-service\",\"namespace\":\"choerodon-devops-prod\",\"resourceVersion\":\"4333256\",\"selfLink\":\"/apis/apps/v1beta2/namespaces/choerodon-devops-prod/deployments/iam-service\",\"uid\":\"0c56c1b5-5bdf-11e8-a66e-00163e0e2443\"},\"spec\":{\"progressDeadlineSeconds\":600,\"replicas\":1,\"revisionHistoryLimit\":10,\"selector\":{\"matchLabels\":{\"choerodon.io/release\":\"iam-service\"}},\"strategy\":{\"rollingUpdate\":{\"maxSurge\":\"25%\",\"maxUnavailable\":\"25%\"},\"type\":\"RollingUpdate\"},\"template\":{\"metadata\":{\"annotations\":{\"choerodon.io/metrics-group\":\"spring-boot\",\"choerodon.io/metrics-path\":\"/prometheus\"},\"creationTimestamp\":null,\"labels\":{\"choerodon.io/metrics-port\":\"8031\",\"choerodon.io/release\":\"iam-service\",\"choerodon.io/service\":\"iam-service\",\"choerodon.io/version\":\"0.6.0\"}},\"spec\":{\"containers\":[{\"env\":[{\"name\":\"CHOERODON_EVENT_CONSUMER_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"EUREKA_CLIENT_SERVICEURL_DEFAULTZONE\",\"value\":\"http://register-server.choerodon-devops-prod:8000/eureka/\"},{\"name\":\"SPRING_CLOUD_CONFIG_ENABLED\",\"value\":\"true\"},{\"name\":\"SPRING_CLOUD_CONFIG_URI\",\"value\":\"http://config-server.choerodon-devops-prod:8010/\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES\",\"value\":\"172.19.136.81:2181,172.19.136.82:2181,172.19.136.83:2181\"},{\"name\":\"SPRING_DATASOURCE_PASSWORD\",\"value\":\"JAu9p8zL\"},{\"name\":\"SPRING_DATASOURCE_URL\",\"value\":\"jdbc:mysql://rm-uf65upic89q7007h5.mysql.rds.aliyuncs.com:3306/iam_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\"},{\"name\":\"SPRING_DATASOURCE_USERNAME\",\"value\":\"c7n_iam\"},{\"name\":\"SPRING_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"}],\"image\":\"registry-vpc.cn-shanghai.aliyuncs.com/choerodon/iam-service:0.6.0\",\"imagePullPolicy\":\"Always\",\"name\":\"iam-service\",\"ports\":[{\"containerPort\":8030,\"name\":\"http\",\"protocol\":\"TCP\"}],\"readinessProbe\":{\"exec\":{\"command\":[\"curl\",\"localhost:8031/health\"]},\"failureThreshold\":3,\"initialDelaySeconds\":60,\"periodSeconds\":10,\"successThreshold\":1,\"timeoutSeconds\":10},\"resources\":{\"limits\":{\"memory\":\"3Gi\"},\"requests\":{\"memory\":\"2Gi\"}},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\"}],\"dnsPolicy\":\"ClusterFirst\",\"restartPolicy\":\"Always\",\"schedulerName\":\"default-scheduler\",\"securityContext\":{},\"terminationGracePeriodSeconds\":30}}},\"status\":{\"availableReplicas\":1,\"conditions\":[{\"lastTransitionTime\":\"2018-05-20T03:36:57Z\",\"lastUpdateTime\":\"2018-06-08T17:19:11Z\",\"message\":\"ReplicaSet \\\"iam-service-56946b7b9f\\\" has successfully progressed.\",\"reason\":\"NewReplicaSetAvailable\",\"status\":\"True\",\"type\":\"Progressing\"},{\"lastTransitionTime\":\"2018-06-08T17:20:30Z\",\"lastUpdateTime\":\"2018-06-08T17:20:30Z\",\"message\":\"Deployment has minimum availability.\",\"reason\":\"MinimumReplicasAvailable\",\"status\":\"True\",\"type\":\"Available\"}],\"observedGeneration\":5,\"readyReplicas\":1,\"replicas\":1,\"updatedReplicas\":1}}")

        devopsEnvResourceDetailDO3.setId(3L)
        devopsEnvResourceDetailDO3.setMessage("{\"apiVersion\":\"v1\",\"kind\":\"Service\",\"metadata\":{\"creationTimestamp\":\"2018-05-20T03:29:11Z\",\"labels\":{\"choerodon.io/release\":\"config-server\"},\"name\":\"config-server\",\"namespace\":\"choerodon-devops-prod\",\"resourceVersion\":\"4325981\",\"selfLink\":\"/api/v1/namespaces/choerodon-devops-prod/services/config-server\",\"uid\":\"f68d3f07-5bdd-11e8-a66e-00163e0e2443\"},\"spec\":{\"clusterIP\":\"192.168.28.13\",\"ports\":[{\"name\":\"http\",\"port\":8010,\"protocol\":\"TCP\",\"targetPort\":\"http\"}],\"selector\":{\"choerodon.io/release\":\"config-server\"},\"sessionAffinity\":\"None\",\"type\":\"ClusterIP\"},\"status\":{\"loadBalancer\":{}}}")

        devopsEnvResourceDetailDO4.setId(4L)
        devopsEnvResourceDetailDO4.setMessage("{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"Ingress\",\"metadata\":{\"creationTimestamp\":\"2018-05-20T03:48:33Z\",\"generation\":1,\"labels\":{\"choerodon.io/release\":\"devops-service\"},\"name\":\"devops-service\",\"namespace\":\"choerodon-devops-prod\",\"resourceVersion\":\"4337962\",\"selfLink\":\"/apis/extensions/v1beta1/namespaces/choerodon-devops-prod/ingresses/devops-service\",\"uid\":\"aadd986d-5be0-11e8-a66e-00163e0e2443\"},\"spec\":{\"rules\":[{\"host\":\"devops.service.choerodon.com.cn\",\"http\":{\"paths\":[{\"backend\":{\"serviceName\":\"devops-service\",\"servicePort\":8060},\"path\":\"/\"}]}}]},\"status\":{\"loadBalancer\":{\"ingress\":[{}]}}}")

        devopsEnvResourceDetailDO5.setId(5L)
        devopsEnvResourceDetailDO5.setMessage("{\"metadata\":{\"name\":\"agile-service-6c7c77bf88\",\"namespace\":\"choerodon-devops-prod\",\"selfLink\":\"/apis/extensions/v1beta1/namespaces/choerodon-devops-prod/replicasets/agile-service-6c7c77bf88\",\"uid\":\"a682bf2a-6d49-11e8-94ae-00163e0e2443\",\"resourceVersion\":\"5105851\",\"generation\":2,\"creationTimestamp\":\"2018-06-11T07:32:52Z\",\"labels\":{\"choerodon.io/metrics-port\":\"8379\",\"choerodon.io/release\":\"agile-service\",\"choerodon.io/service\":\"agile-service\",\"choerodon.io/version\":\"0.5.1\",\"pod-template-hash\":\"2737336944\"},\"annotations\":{\"deployment.kubernetes.io/desired-replicas\":\"1\",\"deployment.kubernetes.io/max-replicas\":\"2\",\"deployment.kubernetes.io/revision\":\"3\"},\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"Deployment\",\"name\":\"agile-service\",\"uid\":\"9b62acbd-6b47-11e8-94ae-00163e0e2443\",\"controller\":true,\"blockOwnerDeletion\":true}]},\"spec\":{\"replicas\":0,\"selector\":{\"matchLabels\":{\"choerodon.io/release\":\"agile-service\",\"pod-template-hash\":\"2737336944\"}},\"template\":{\"metadata\":{\"creationTimestamp\":null,\"labels\":{\"choerodon.io/metrics-port\":\"8379\",\"choerodon.io/release\":\"agile-service\",\"choerodon.io/service\":\"agile-service\",\"choerodon.io/version\":\"0.5.1\",\"pod-template-hash\":\"2737336944\"},\"annotations\":{\"choerodon.io/metrics-group\":\"spring-boot\",\"choerodon.io/metrics-path\":\"/prometheus\"}},\"spec\":{\"containers\":[{\"name\":\"agile-service\",\"image\":\"registry.choerodon.com.cn/choerodon-framework/agile-service:0.5.1\",\"ports\":[{\"name\":\"http\",\"containerPort\":8378,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"CHOERODON_EVENT_CONSUMER_KAFKA_BOOTSTRAP_SERVERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"EUREKA_CLIENT_SERVICEURL_DEFAULTZONE\",\"value\":\"http://register-server.choerodon-devops-prod:8000/eureka/\"},{\"name\":\"SERVICES_ATTACHMENT_URL\",\"value\":\"https://minio.choerodon.com.cn/agile-service/\"},{\"name\":\"SPRING_CLOUD_CONFIG_URI\",\"value\":\"http://config-server.choerodon-devops-prod:8010/\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS\",\"value\":\"172.19.136.81:9092,172.19.136.82:9092,172.19.136.83:9092\"},{\"name\":\"SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES\",\"value\":\"172.19.136.81:2181,172.19.136.82:2181,172.19.136.83:2181\"},{\"name\":\"SPRING_DATASOURCE_PASSWORD\",\"value\":\"CAu0p8zL\"},{\"name\":\"SPRING_DATASOURCE_URL\",\"value\":\"jdbc:mysql://rm-uf65upic89q7007h5.mysql.rds.aliyuncs.com:3306/agile_service?useUnicode=true\\u0026characterEncoding=utf-8\\u0026useSSL=false\"},{\"name\":\"SPRING_DATASOURCE_USERNAME\",\"value\":\"c7n_agile\"}],\"resources\":{\"limits\":{\"memory\":\"3Gi\"},\"requests\":{\"memory\":\"2Gi\"}},\"readinessProbe\":{\"exec\":{\"command\":[\"curl\",\"localhost:8379/health\"]},\"initialDelaySeconds\":120,\"timeoutSeconds\":10,\"periodSeconds\":10,\"successThreshold\":1,\"failureThreshold\":3},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"}}},\"status\":{\"replicas\":0,\"observedGeneration\":2}}")

        devopsEnvResourceDetailDO0.setId(6L)
        devopsEnvResourceDetailDO0.setMessage(null)
    }

    def setup() {
        iamRepository.initMockIamService(iamServiceClient)
//        gitlabRepository.initMockService(gitlabServiceClient)
        gitlabGroupMemberRepository.initMockService(gitlabServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)
        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("testOrganization")
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
        Page<ProjectWithRoleDTO> projectWithRoleDTOPage = new Page<>()
        projectWithRoleDTOPage.setContent(projectWithRoleDTOList)
        projectWithRoleDTOPage.setTotalPages(2)
        ResponseEntity<Page<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        Page<ProjectDO> projectDOPage = new Page<>()
        List<ProjectDO> projectDOList = new ArrayList<>()
        projectDOList.add(projectDO)
        projectDOPage.setContent(projectDOList)
        ResponseEntity<Page<ProjectDO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryProjectByOrgId(anyLong(), anyInt(), anyInt(), anyString(), any(String[].class))).thenReturn(projectDOPageResponseEntity)

        MemberDO memberDO = new MemberDO()
        memberDO.setAccessLevel(AccessLevel.OWNER)
        ResponseEntity<MemberDO> responseEntity2 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getUserMemberByUserId(anyInt(), anyInt())).thenReturn(responseEntity2)
    }

    def "PageByOptions"() {
        given: '初始化数据'
        applicationMapper.insert(applicationDO)
        devopsEnvPodMapper.insert(devopsEnvPodDO)
        applicationMarketMapper.insert(devopsAppMarketDO)
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

        applicationVersionMapper.insert(applicationVersionDO)
        applicationInstanceMapper.insert(applicationInstanceDO)

        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO2)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO3)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO4)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO5)

        and: '初始化请求头'
        String infra = "{\"searchParam\":{},\"param\":\"\"}"
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        and: 'mock envListener'
        Map<String, EnvSession> envs = new HashMap<>()
        EnvSession envSession = new EnvSession()
        envSession.setVersion("0.10.0")
        envSession.setClusterId(1L)
        envs.put("testenv", envSession)
        envListener.connectedEnv() >> envs

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

//    技术问题暂时不测试该方法
//    def "QueryValue"() {
//        when: '获取部署 Value'
//        def result = restTemplate.getForObject("/v1/projects/1/app_instances/1/value", ReplaceResult.class)
//
//        then:
//        result != null
//    }
//    技术问题暂时不测试该方法
//    def "QueryUpgradeValue"() {
//    }
//
//    技术问题暂时不测试该方法
//    def "QueryValues"() {
//        when: '查询value列表'
//        def result = restTemplate.getForObject("/v1/projects/1/app_instances/value?appId=1&envId=1&appVersionId=1", ReplaceResult.class)
//
//        then: '校验返回值'
//        result != null
//    }
//
//    技术问题暂时不测试该方法
//    def "PreviewValues"() {
//    }

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

//    技术问题暂时不测试该方法
//    def "Deploy"() {
//        given: '初始化applicationDeployDTO'
//        ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO()
//        applicationDeployDTO.setEnvironmentId(1L)
//        applicationDeployDTO.setValues("env:\n" +
//                "  open:\n" +
//                "    PRO_API_HOST: api.example.com.cn1\n" +
//                "preJob:\n" +
//                "  preConfig:\n" +
//                "    mysql:\n" +
//                "      username: root\n" +
//                "      host: 192.168.12.156\n" +
//                "      password: handhand\n" +
//                "      dbname: demo_service")
//        applicationDeployDTO.setAppId(1L)
//        applicationDeployDTO.setAppVerisonId(1L)
//        applicationDeployDTO.setType("update")
//        applicationDeployDTO.setAppInstanceId(1L)
//        applicationDeployDTO.setIsNotChange(false)
//
//        and: 'mock envUtil'
//        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
//
//        and: 'mock gitUtil'
//        gitUtil.cloneBySsh(_ as String, _ as String) >> null
//
//        when: '部署应用'
//        def dto = restTemplate.postForObject("/v1/projects/1/app_instances", applicationDeployDTO, ApplicationDeployDTO.class)
//
//        then: '校验返回值'
//        dto != null
//    }

    def "QueryVersionFeatures"() {
    }

    def "ListByAppVersionId"() {
    }

    def "ListByAppIdAndEnvId"() {
    }

    def "ListResources"() {
    }

    def "ListStages"() {
    }

    def "Stop"() {
    }

    def "Start"() {
    }

    def "Restart"() {
    }

    def "Delete"() {
    }

    def "ListByEnv"() {
    }

    def "ListEnvFiles"() {
    }

    def "ListDeployTime"() {
    }

    def "ListDeployFrequency"() {
    }

    def "PageDeployFrequencyDetail"() {
    }

    def "PageDeployTimeDetail"() {
    }
}
