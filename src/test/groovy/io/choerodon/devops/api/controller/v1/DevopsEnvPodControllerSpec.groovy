package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsEnvPodDTO
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO
import io.choerodon.devops.infra.dataobject.ApplicationVersionDO
import io.choerodon.devops.infra.dataobject.DevopsAppMarketDO
import io.choerodon.devops.infra.dataobject.DevopsEnvPodDO
import io.choerodon.devops.infra.dataobject.DevopsEnvResourceDO
import io.choerodon.devops.infra.dataobject.DevopsEnvResourceDetailDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.mapper.*
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import io.choerodon.websocket.helper.EnvListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/10
 * Time: 11:17
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvPodController)
@Stepwise
class DevopsEnvPodControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationMarketMapper applicationMarketMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private DevopsEnvResourceMapper devopsEnvResourceMapper
    @Autowired
    private DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil

    @Shared
    DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO()
    @Shared
    ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
    @Shared
    ApplicationVersionDO applicationVersionDO1 = new ApplicationVersionDO()
    @Shared
    ApplicationDO applicationDO = new ApplicationDO()
    @Shared
    ApplicationDO applicationDO1 = new ApplicationDO()
    @Shared
    DevopsAppMarketDO devopsAppMarketDO = new DevopsAppMarketDO()
    @Shared
    ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()
    @Shared
    DevopsEnvResourceDO devopsEnvResourceDO = new DevopsEnvResourceDO()
    @Shared
    DevopsEnvResourceDetailDO devopsEnvResourceDetailDO = new DevopsEnvResourceDetailDO()
    @Shared
    DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()

    def setupSpec() {
        devopsEnvPodDO.setId(1L)
        devopsEnvPodDO.setName("test-pod")
        devopsEnvPodDO.setEnvId(1L)
        devopsEnvPodDO.setAppInstanceId(1L)

        applicationVersionDO.setId(1L)
        applicationVersionDO.setAppId(1L)
        applicationVersionDO.setValueId(1L)
        applicationVersionDO.setIsPublish(1L)
        applicationVersionDO.setCommit("commit")
        applicationVersionDO.setVersion("versions")
        applicationVersionDO.setCreationDate(new Date(2018, 9, 14, 13, 40, 0))

        applicationVersionDO1.setId(2L)
        applicationVersionDO1.setAppId(2L)
        applicationVersionDO1.setValueId(1L)
        applicationVersionDO1.setIsPublish(1L)
        applicationVersionDO1.setCommit("commit1")
        applicationVersionDO1.setVersion("version1")
        applicationVersionDO1.setCreationDate(new Date(2018, 9, 14, 13, 40, 0))

        applicationDO.setId(1L)
        applicationDO1.setId(2L)
        applicationDO.setActive(true)
        applicationDO1.setActive(true)
        applicationDO.setSynchro(true)
        applicationDO1.setSynchro(true)
        applicationDO.setCode("app")
        applicationDO1.setCode("app1")
        applicationDO.setProjectId(1L)
        applicationDO1.setProjectId(1L)
        applicationDO.setName("appname")
        applicationDO1.setName("appname1")
        applicationDO.setGitlabProjectId(1)
        applicationDO1.setGitlabProjectId(1)
        applicationDO.setAppTemplateId(1L)
        applicationDO1.setAppTemplateId(1L)
        applicationDO.setObjectVersionNumber(1L)
        applicationDO1.setObjectVersionNumber(1L)

        devopsAppMarketDO.setId(1L)
        devopsAppMarketDO.setAppId(1L)
        devopsAppMarketDO.setPublishLevel("organization")
        devopsAppMarketDO.setContributor("testman")
        devopsAppMarketDO.setDescription("I Love Test")

        devopsEnvResourceDetailDO.setMessage("{\"metadata\":{\"name\":\"test-pod\",\"generateName\":\"frontapp0110-9a710new-76585778b9-\",\"namespace\":\"envtest0110\",\"selfLink\":\"/api/v1/namespaces/envtest0110/pods/frontapp0110-9a710new-76585778b9-vqkpw\",\"uid\":\"3481898e-fdb1-11e8-8453-5254003416d6\",\"resourceVersion\":\"75270309\",\"creationTimestamp\":\"2018-12-12T01:56:56Z\",\"labels\":{\"choerodon.io\":\"2018.12.10-112732-master\",\"choerodon.io/application\":\"frontapp0110\",\"choerodon.io/release\":\"frontapp0110-9a710new\",\"choerodon.io/version\":\"0.14.0\",\"pod-template-hash\":\"3214133465\"},\"annotations\":{\"kubernetes.io/created-by\":\"{\\\"kind\\\":\\\"SerializedReference\\\",\\\"apiVersion\\\":\\\"v1\\\",\\\"reference\\\":{\\\"kind\\\":\\\"ReplicaSet\\\",\\\"namespace\\\":\\\"envtest0110\\\",\\\"name\\\":\\\"frontapp0110-9a710new-76585778b9\\\",\\\"uid\\\":\\\"3439c91f-fdb1-11e8-8453-5254003416d6\\\",\\\"apiVersion\\\":\\\"extensions\\\",\\\"resourceVersion\\\":\\\"71667927\\\"}}\\n\"},\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"ReplicaSet\",\"name\":\"frontapp0110-9a710new-76585778b9\",\"uid\":\"3439c91f-fdb1-11e8-8453-5254003416d6\",\"controller\":true,\"blockOwnerDeletion\":true}]},\"spec\":{\"volumes\":[{\"name\":\"default-token-f4fgd\",\"secret\":{\"secretName\":\"default-token-f4fgd\",\"defaultMode\":420}}],\"containers\":[{\"name\":\"frontapp0110-9a710new\",\"image\":\"registry.saas.hand-china.com/testorg0110-testpro0110/frontapp0110:0.14.0\",\"ports\":[{\"name\":\"http\",\"containerPort\":80,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"PRO_API_HOST\",\"value\":\"api.example.com.cn\"},{\"name\":\"PRO_CLIENT_ID\",\"value\":\"example\"},{\"name\":\"PRO_COOKIE_SERVER\",\"value\":\"example.com.cn1\"},{\"name\":\"PRO_HEADER_TITLE_NAME\",\"value\":\"Choerodon\"},{\"name\":\"PRO_HTTP\",\"value\":\"http\"},{\"name\":\"PRO_LOCAL\",\"value\":\"true\"},{\"name\":\"PRO_TITLE_NAME\",\"value\":\"Choerodon12\"}],\"resources\":{},\"volumeMounts\":[{\"name\":\"default-token-f4fgd\",\"readOnly\":true,\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\"}],\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"IfNotPresent\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"serviceAccountName\":\"default\",\"serviceAccount\":\"default\",\"nodeName\":\"clusternode2\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"},\"status\":{\"phase\":\"Running\",\"conditions\":[{\"type\":\"Initialized\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-12T01:56:58Z\"},{\"type\":\"Ready\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-12T01:57:37Z\"},{\"type\":\"PodScheduled\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-12T01:56:57Z\"}],\"hostIP\":\"192.168.12.157\",\"podIP\":\"10.233.67.52\",\"startTime\":\"2018-12-12T01:56:58Z\",\"containerStatuses\":[{\"name\":\"frontapp0110-9a710new\",\"state\":{\"running\":{\"startedAt\":\"2018-12-20T07:21:59Z\"}},\"lastState\":{},\"ready\":true,\"restartCount\":1,\"image\":\"registry.saas.hand-china.com/testorg0110-testpro0110/frontapp0110:0.14.0\",\"imageID\":\"docker-pullable://registry.saas.hand-china.com/testorg0110-testpro0110/frontapp0110@sha256:9b7ca0c7ca9d873c76652986f694c635454f6ccb8e21bc38418e8e695e97f271\",\"containerID\":\"docker://bae0f72a45a4d86c77a36bdc18069d0ddbbf6464736399097969cf5a1d49da34\"}],\"qosClass\":\"BestEffort\"}}")
        devopsEnvResourceDetailDO.setId(1L)

        devopsEnvResourceDO.setId(1L)
        devopsEnvResourceDO.setName("test-pod")
        devopsEnvResourceDO.setKind("Pod")
        devopsEnvResourceDO.setAppInstanceId(1L)
        devopsEnvResourceDO.setResourceDetailId(1L)

        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setAppVersionId(1L)

        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(1L)
    }

    def "PageByOptions"() {
        given: '初始化变量'
        devopsEnvPodMapper.insert(devopsEnvPodDO)
        applicationVersionMapper.insert(applicationVersionDO)
        applicationVersionMapper.insert(applicationVersionDO1)
        applicationMapper.insert(applicationDO)
        applicationMapper.insert(applicationDO1)
        applicationMarketMapper.insert(devopsAppMarketDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        applicationInstanceMapper.insert(applicationInstanceDO)
        devopsEnvResourceDetailMapper.insert(devopsEnvResourceDetailDO)
        devopsEnvResourceMapper.insert(devopsEnvResourceDO)

        and: '设置请求头'
        String infra = null
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/json;UTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        List<Long> connectedEnvList = new ArrayList<>()
        connectedEnvList.add(1L)
        List<Long> updateEnvList = new ArrayList<>()
        updateEnvList.add(1L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> connectedEnvList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> updateEnvList

        when: '分页查询容器管理'
        def entity = restTemplate.postForEntity("/v1/projects/1/app_pod/list_by_options?envId=1&appId=1", strEntity, Page.class)

        then: '校验返回值和清除数据'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
        !entity.getBody().isEmpty()
        (entity.getBody().get(0) as LinkedHashMap).get("containers") != null

        and:'清理数据'
        // 删除envPod
        List<DevopsEnvPodDO> list = devopsEnvPodMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (DevopsEnvPodDO e : list) {
                devopsEnvPodMapper.delete(e)
            }
        }
        // 删除appVersion
        List<ApplicationVersionDO> list1 = applicationVersionMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (ApplicationVersionDO e : list1) {
                applicationVersionMapper.delete(e)
            }
        }
        // 删除app
        List<ApplicationDO> list2 = applicationMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (ApplicationDO e : list2) {
                applicationMapper.delete(e)
            }
        }
        // 删除appMarket
        List<DevopsAppMarketDO> list3 = applicationMarketMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (DevopsAppMarketDO e : list3) {
                applicationMarketMapper.delete(e)
            }
        }

        // 删除环境
        devopsEnvironmentMapper.delete(devopsEnvironmentDO)

        // 删除resource
        devopsEnvResourceMapper.delete(devopsEnvResourceDO)

        // 删除resource detail
        devopsEnvResourceDetailMapper.delete(devopsEnvResourceDetailDO)

        // 删除application instance
        applicationInstanceMapper.delete(applicationInstanceDO)
    }
}
