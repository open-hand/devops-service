package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.dataobject.*
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
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/10
 * Time: 11:17
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsEnvPodControllerSpec extends Specification {

    private static flag = 0

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
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil

    def setup() {
        if (flag == 0) {
            DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO()
            devopsEnvPodDO.setEnvId(1L)
            devopsEnvPodDO.setAppInstanceId(1L)
            devopsEnvPodMapper.insert(devopsEnvPodDO)

            DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
            devopsEnvironmentDO.setCode("env")
            devopsEnvironmentDO.setProjectId(1L)
            devopsEnvironmentDO.setName("testName")
            devopsEnvironmentDO.setEnvIdRsa("EnvIdRsa")
            devopsEnvironmentDO.setGitlabEnvProjectId(1L)
            devopsEnvironmentMapper.insert(devopsEnvironmentDO)

            ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()
            applicationInstanceDO.setEnvId(1L)
            applicationInstanceDO.setAppId(1L)
            applicationInstanceDO.setAppVersionId(1L)
            applicationInstanceMapper.insert(applicationInstanceDO)

            ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
            applicationVersionDO.setId(1L)
            applicationVersionDO.setAppId(1L)
            applicationVersionMapper.insert(applicationVersionDO)

            ApplicationDO applicationDO = new ApplicationDO()
            applicationDO.setId(1L)
            applicationMapper.insert(applicationDO)

            DevopsAppMarketDO devopsAppMarketDO = new DevopsAppMarketDO()
            devopsAppMarketDO.setId(1L)
            devopsAppMarketDO.setAppId(1L)
            applicationMarketMapper.insert(devopsAppMarketDO)

            flag = 1
        }
    }

    def "PageByOptions"() {
        given:
        String infra = null
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/json;UTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        List<Long> connectedEnvList = new ArrayList<>()
        connectedEnvList.add(1L)
        List<Long> updateEnvList = new ArrayList<>()
        updateEnvList.add(1L)

        when:
        def page = restTemplate.postForObject("/v1/projects/1/app_pod/list_by_options?envId=1&appId=1", strEntity, Page.class)

        then:
        envUtil.getConnectedEnvList(_ as EnvListener) >> connectedEnvList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> updateEnvList
        page != null
    }
}
