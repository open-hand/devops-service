package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.base.domain.PageRequest
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsEnvPodInfoVO
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
    private final static String MAPPING="/v1/projects/1/pods"
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private AppServiceShareRuleMapper applicationMarketMapper
    @Autowired
    private AppServiceVersionMapper applicationVersionMapper
    @Autowired
    private AppServiceInstanceMapper applicationInstanceMapper
    @Autowired
    private DevopsEnvResourceMapper devopsEnvResourceMapper
    @Autowired
    private DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper

    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler envUtil

    @Shared
    DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO()
    @Shared
    AppServiceVersionDTO applicationVersionDO = new AppServiceVersionDTO()
    @Shared
    AppServiceVersionDTO applicationVersionDO1 = new AppServiceVersionDTO()
    @Shared
    AppServiceDTO applicationDO = new AppServiceDTO()
    @Shared
    AppServiceDTO applicationDO1 = new AppServiceDTO()
    @Shared
    AppServiceShareRuleDTO devopsAppMarketDO = new AppServiceShareRuleDTO()
    @Shared
    AppServiceInstanceDTO applicationInstanceDO = new AppServiceInstanceDTO()
    @Shared
    DevopsEnvResourceDTO devopsEnvResourceDO = new DevopsEnvResourceDTO()
    @Shared
    DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO = new DevopsEnvResourceDetailDTO()
    @Shared
    DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()

    def isToClean=false

    def setupSpec() {
        devopsEnvPodDO.setId(1L)
        devopsEnvPodDO.setName("test-pod")
        devopsEnvPodDO.setEnvId(1L)
        devopsEnvPodDO.setInstanceId(1L)

        applicationVersionDO.setId(1L)
        applicationVersionDO.setAppServiceId(1L)
        applicationVersionDO.setValueId(1L)
        applicationVersionDO.setIsPublish(1L)
        applicationVersionDO.setCommit("commit")
        applicationVersionDO.setVersion("versions")
        applicationVersionDO.setCreationDate(new Date(2018, 9, 14, 13, 40, 0))

        applicationVersionDO1.setId(2L)
        applicationVersionDO1.setAppServiceId(2L)
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
        applicationDO.setAppId(1L)
        applicationDO1.setAppId(1L)
        applicationDO.setName("appname")
        applicationDO1.setName("appname1")
        applicationDO.setGitlabProjectId(1)
        applicationDO1.setGitlabProjectId(1)
        applicationDO.setObjectVersionNumber(1L)
        applicationDO1.setObjectVersionNumber(1L)

        devopsAppMarketDO.setId(1L)
        devopsAppMarketDO.setAppServiceId(1L)
        devopsAppMarketDO.setShareLevel("organization")

        devopsEnvResourceDetailDO.setMessage("{\"metadata\":{\"name\":\"code-i-multi-container-7bb85795f4-sbmpc\",\"generateName\":\"code-i-multi-container-7bb85795f4-\",\"namespace\":\"ccccs\",\"selfLink\":\"/api/v1/namespaces/ccccs/pods/code-i-multi-container-7bb85795f4-sbmpc\",\"uid\":\"cf2dbed7-072a-11e9-ab66-5254003416d6\",\"resourceVersion\":\"81142959\",\"creationTimestamp\":\"2018-12-24T03:20:05Z\",\"labels\":{\"choerodon.io\":\"0.12.0\",\"choerodon.io/application\":\"code-i\",\"choerodon.io/release\":\"code-i-multi-container\",\"choerodon.io/service\":\"code-i\",\"choerodon.io/version\":\"2018.12.24-104231-multi-container\",\"pod-template-hash\":\"3664135190\"},\"ownerReferences\":[{\"apiVersion\":\"extensions/v1beta1\",\"kind\":\"ReplicaSet\",\"name\":\"code-i-multi-container-7bb85795f4\",\"uid\":\"ceedb6e8-072a-11e9-ab66-5254003416d6\",\"controller\":true,\"blockOwnerDeletion\":true}]},\"spec\":{\"volumes\":[{\"name\":\"data\",\"emptyDir\":{}},{\"name\":\"default-token-4wg24\",\"secret\":{\"secretName\":\"default-token-4wg24\",\"defaultMode\":420}}],\"containers\":[{\"name\":\"multi-container1\",\"image\":\"registry.saas.test.com/code-x-code-x/code-i:2018.12.24-104231-multi-container\",\"ports\":[{\"name\":\"http\",\"containerPort\":80,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"PRO_API_HOST\",\"value\":\"api.example.com.cn\"},{\"name\":\"PRO_CLIENT_ID\",\"value\":\"example\"},{\"name\":\"PRO_COOKIE_SERVER\",\"value\":\"example.com.cn\"},{\"name\":\"PRO_HEADER_TITLE_NAME\",\"value\":\"Choerodon\"},{\"name\":\"PRO_HTTP\",\"value\":\"http\"},{\"name\":\"PRO_LOCAL\",\"value\":\"true\"},{\"name\":\"PRO_TITLE_NAME\",\"value\":\"Choerodon\"}],\"resources\":{},\"volumeMounts\":[{\"name\":\"data\",\"mountPath\":\"/Charts\"},{\"name\":\"default-token-4wg24\",\"readOnly\":true,\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\"}],\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"},{\"name\":\"multi-container2\",\"image\":\"registry.saas.test.com/code-x-code-x/code-i:2018.12.24-104231-multi-container\",\"ports\":[{\"name\":\"http\",\"containerPort\":80,\"protocol\":\"TCP\"}],\"env\":[{\"name\":\"PRO_API_HOST\",\"value\":\"api.example.com.cn\"},{\"name\":\"PRO_CLIENT_ID\",\"value\":\"example\"},{\"name\":\"PRO_COOKIE_SERVER\",\"value\":\"example.com.cn\"},{\"name\":\"PRO_HEADER_TITLE_NAME\",\"value\":\"Choerodon\"},{\"name\":\"PRO_HTTP\",\"value\":\"http\"},{\"name\":\"PRO_LOCAL\",\"value\":\"true\"},{\"name\":\"PRO_TITLE_NAME\",\"value\":\"Choerodon\"}],\"resources\":{},\"volumeMounts\":[{\"name\":\"data\",\"mountPath\":\"/Charts\"},{\"name\":\"default-token-4wg24\",\"readOnly\":true,\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\"}],\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"Always\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"ClusterFirst\",\"serviceAccountName\":\"default\",\"serviceAccount\":\"default\",\"nodeName\":\"clusternode14\",\"securityContext\":{},\"schedulerName\":\"default-scheduler\"},\"status\":{\"phase\":\"Running\",\"conditions\":[{\"type\":\"Initialized\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-24T03:20:06Z\"},{\"type\":\"Ready\",\"status\":\"False\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-25T02:23:50Z\",\"reason\":\"ContainersNotReady\",\"message\":\"containers with unready status: [multi-container2]\"},{\"type\":\"PodScheduled\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2018-12-24T03:20:05Z\"}],\"hostIP\":\"192.168.12.114\",\"podIP\":\"10.233.71.145\",\"startTime\":\"2018-12-24T03:20:06Z\",\"containerStatuses\":[{\"name\":\"multi-container1\",\"state\":{\"running\":{\"startedAt\":\"2018-12-24T03:20:10Z\"}},\"lastState\":{},\"ready\":true,\"restartCount\":0,\"image\":\"registry.saas.test.com/code-x-code-x/code-i:2018.12.24-104231-multi-container\",\"imageID\":\"docker-pullable://registry.saas.test.com/code-x-code-x/code-i@sha256:c1ac454ae523c002aa4fa5653fa44c429e37abae6d82aba221ed7da5b705b001\",\"containerID\":\"docker://df7d79c8a22f4d1cdd2dda0565c3fd389354431ba084c344b2288b7d2f2e5e63\"},{\"name\":\"multi-container2\",\"state\":{\"waiting\":{\"reason\":\"CrashLoopBackOff\",\"message\":\"Back-off 5m0s restarting failed container=multi-container2 pod=code-i-multi-container-7bb85795f4-sbmpc_ccccs(cf2dbed7-072a-11e9-ab66-5254003416d6)\"}},\"lastState\":{\"terminated\":{\"exitCode\":1,\"reason\":\"Error\",\"startedAt\":\"2018-12-25T02:23:44Z\",\"finishedAt\":\"2018-12-25T02:23:49Z\",\"containerID\":\"docker://96f9b1f49a7f8a2c2d54a33c83d63d2182f59b380a4f4a5776aa50d74641e07e\"}},\"ready\":false,\"restartCount\":1,\"image\":\"registry.saas.test.com/code-x-code-x/code-i:2018.12.24-104231-multi-container\",\"imageID\":\"docker-pullable://registry.saas.test.com/code-x-code-x/code-i@sha256:c1ac454ae523c002aa4fa5653fa44c429e37abae6d82aba221ed7da5b705b001\",\"containerID\":\"docker://96f9b1f49a7f8a2c2d54a33c83d63d2182f59b380a4f4a5776aa50d74641e07e\"}],\"qosClass\":\"BestEffort\"}}")
        devopsEnvResourceDetailDO.setId(1L)

        devopsEnvResourceDO.setId(1L)
        devopsEnvResourceDO.setName("test-pod")
        devopsEnvResourceDO.setKind("Pod")
        devopsEnvResourceDO.setInstanceId(1L)
        devopsEnvResourceDO.setResourceDetailId(1L)

        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setAppServiceVersionId(1L)

        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(1L)
    }

    def "PageByOptions"() {
        given: '初始化变量'
        devopsEnvPodMapper.selectAll().forEach { devopsEnvPodMapper.delete(it) }
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

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/json;UTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        List<Long> connectedEnvList = new ArrayList<>()
        connectedEnvList.add(1L)
        List<Long> updateEnvList = new ArrayList<>()
        updateEnvList.add(1L)
        envUtil.getConnectedEnvList() >> connectedEnvList
        envUtil.getUpdatedEnvList() >> updateEnvList

        when: '分页查询容器管理'
        def entity = restTemplate.postForEntity(MAPPING+"/page_by_options?env_id=1&app_service_id=1",strEntity,PageInfo.class,1L)

        then: '校验返回值'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
        !entity.getBody().getList().isEmpty()
    }
    def "queryEnvPodInfo"(){
      when:
      def entity=restTemplate.getForEntity(MAPPING+"/pod_ranking?env_id=1",List.class)
      then:
      entity.getStatusCode().is2xxSuccessful()
      entity.getBody()!=null
     }
     def "cleanupData"() {
        given:
        isToClean = true
     }
    def cleanup() {
        if (isToClean) {
            // 删除envPod
            List<DevopsEnvPodDTO> list = devopsEnvPodMapper.selectAll()
            if (list != null && !list.isEmpty()) {
                for (DevopsEnvPodDTO e : list) {
                    devopsEnvPodMapper.delete(e)
                }
            }
            // 删除appVersion
            List<AppServiceVersionDTO> list1 = applicationVersionMapper.selectAll()
            if (list1 != null && !list1.isEmpty()) {
                for (AppServiceVersionDTO e : list1) {
                    applicationVersionMapper.delete(e)
                }
            }
            // 删除app
            List<AppServiceDTO> list2 = applicationMapper.selectAll()
            if (list2 != null && !list2.isEmpty()) {
                for (AppServiceDTO e : list2) {
                    applicationMapper.delete(e)
                }
            }
            // 删除appMarket
            List<AppServiceShareRuleDTO> list3 = applicationMarketMapper.selectAll()
            if (list3 != null && !list3.isEmpty()) {
                for (AppServiceShareRuleDTO e : list3) {
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

}
