package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.mapper.*
import io.choerodon.websocket.helper.EnvListener
import io.choerodon.websocket.helper.EnvSession
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
    private DevopsEnvResourceDetailMapper devopsEnvResourceDetailMapper

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

    def setupSpec() {
        // de
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setName("envName")
        devopsEnvironmentDO.setCode("envCode")
        devopsEnvironmentDO.setClusterId(1L)

        // dam
        devopsAppMarketDO.setPublishLevel("pub")
        devopsAppMarketDO.setContributor("con")
        devopsAppMarketDO.setDescription("des")

        // dav
        applicationVersionDO.setId(1L)
        applicationVersionDO.setVersion("version")

        // dp
        devopsEnvPodDO.setId(1L)
        devopsEnvPodDO.setReady(true)
        devopsEnvPodDO.setAppInstanceId(1L)
        devopsEnvPodDO.setStatus("Running")
        devopsEnvPodDO.setNamespace("envCode")
        devopsEnvPodDO.setNamespace("envCode")

        // cmd
        devopsEnvCommandDO.setId(1L)
        devopsEnvCommandDO.setError("error")
        devopsEnvCommandDO.setStatus("operating")
        devopsEnvCommandDO.setObjectVersionId(1L)
        devopsEnvCommandDO.setCommandType("commandType")

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

        // der
        devopsEnvResourceDO.setId(1L)
        devopsEnvResourceDO.setKind("Pod")
        devopsEnvResourceDO.setAppInstanceId(1L)
        devopsEnvResourceDO.setResourceDetailId(1L)

        devopsEnvResourceDO2.setId(2L)
        devopsEnvResourceDO2.setKind("Deployment")
        devopsEnvResourceDO2.setAppInstanceId(1L)
        devopsEnvResourceDO2.setResourceDetailId(2L)

        devopsEnvResourceDO3.setId(3L)
        devopsEnvResourceDO3.setKind("Service")
        devopsEnvResourceDO3.setAppInstanceId(1L)
        devopsEnvResourceDO3.setResourceDetailId(3L)

        devopsEnvResourceDO4.setId(4L)
        devopsEnvResourceDO4.setKind("Ingress")
        devopsEnvResourceDO4.setAppInstanceId(1L)
        devopsEnvResourceDO4.setResourceDetailId(4L)

        devopsEnvResourceDO5.setId(5L)
        devopsEnvResourceDO5.setKind("ReplicaSet")
        devopsEnvResourceDO5.setAppInstanceId(1L)
        devopsEnvResourceDO5.setResourceDetailId(5L)

        devopsEnvResourceDO0.setId(6)
        devopsEnvResourceDO0.setKind(null)
        devopsEnvResourceDO0.setAppInstanceId(1L)
        devopsEnvResourceDO0.setResourceDetailId(6L)

        // derd
        devopsEnvResourceDetailDO.setId(1L)
        devopsEnvResourceDetailDO.setMessage("This is Pod")

        devopsEnvResourceDetailDO2.setId(2L)
        devopsEnvResourceDetailDO2.setMessage("This is Deployment")

        devopsEnvResourceDetailDO3.setId(3L)
        devopsEnvResourceDetailDO3.setMessage("This is Service")

        devopsEnvResourceDetailDO4.setId(4L)
        devopsEnvResourceDetailDO4.setMessage("This is Ingress")

        devopsEnvResourceDetailDO5.setId(5L)
        devopsEnvResourceDetailDO5.setMessage("This is ReplicaSet")

        devopsEnvResourceDetailDO0.setId(6L)
        devopsEnvResourceDetailDO0.setMessage("This is 0")
    }

    def "PageByOptions"() {
        given: '初始化数据'
        applicationMapper.insert(applicationDO)
        devopsEnvPodMapper.insert(devopsEnvPodDO)
        applicationMarketMapper.insert(devopsAppMarketDO)
        devopsEnvCommandMapper.insert(devopsEnvCommandDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)

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
        page != null
    }

    def "ListByAppId"() {
    }

    def "QueryValue"() {
    }

    def "QueryUpgradeValue"() {
    }

    def "QueryValues"() {
    }

    def "PreviewValues"() {
    }

    def "FormatValue"() {
    }

    def "Deploy"() {
    }

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
