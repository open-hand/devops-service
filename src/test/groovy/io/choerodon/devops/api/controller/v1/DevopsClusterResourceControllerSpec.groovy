package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ClusterResourceVO
import io.choerodon.devops.api.vo.DevopsPrometheusVO
import io.choerodon.devops.app.service.AgentCommandService
import io.choerodon.devops.app.service.DevopsClusterResourceService
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author zhaotianxin* @since 2019/10/31
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsClusterResourceController)
@Stepwise
class DevopsClusterResourceControllerSpec extends Specification {
    private static final String MAPPING = "/v1/projects/{project_id}/cluster_resource"
    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;
    @Autowired
    private TestRestTemplate restTemplate


    def "DeployCertManager"() {
        when:
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("cluster_id", 1L);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<MultiValueMap<String, Object>>(paramMap, headers);
        def entity = restTemplate.postForEntity(MAPPING + "/cert_manager/deploy", httpEntity, null, 1L);
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "ListClusterResource"() {
    }

    def "UnloadCertManager"() {
        when:
        HttpEntity httpEntity = new HttpEntity();
        def entity = restTemplate.exchange(MAPPING + "/cert_manager/unload?cluster_id=1", HttpMethod.DELETE, null, Boolean.class, 1L)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "createPrometheus"() {
        given:
        DevopsPrometheusVO devopsPrometheusVO = new DevopsPrometheusVO()
        devopsPrometheusVO.setAdminPassword("test")
        devopsPrometheusVO.setClusterName("uat")
        devopsPrometheusVO.setGrafanaDomain("www.hand.com")
        devopsPrometheusVO.setPvName("test")
        when:
        def entity = restTemplate.postForEntity(MAPPING + "/prometheus/create?cluster_id=1", devopsPrometheusVO, null, 1L)
        then:
        entity.statusCode.is2xxSuccessful()

    }

    def "updatePrometheus"() {
        given:
        DevopsPrometheusVO devopsPrometheusVO = new DevopsPrometheusVO()
        devopsPrometheusVO.setId(1L)
        devopsPrometheusVO.setAdminPassword("test")
        devopsPrometheusVO.setClusterName("uat")
        devopsPrometheusVO.setGrafanaDomain("www.hand.com")
        devopsPrometheusVO.setPvName("test")
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/prometheus/update?cluster_id=1", devopsPrometheusVO, null, 1L)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "queryPrometheus"() {
        given:
        DevopsPrometheusVO devopsPrometheusVO = new DevopsPrometheusVO()
        devopsPrometheusVO.setId(1L)
        devopsPrometheusVO.setAdminPassword("test")
        devopsPrometheusVO.setClusterName("uat")
        devopsPrometheusVO.setGrafanaDomain("www.hand.com")
        devopsPrometheusVO.setPvName("test")
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/prometheus?cluster_id=1", null, DevopsPrometheusVO.class, 1L)
        then:
        entity.body != null
    }

    def "getDeployStatus"() {
        given:
        DevopsPrometheusVO devopsPrometheusVO = new DevopsPrometheusVO()
        devopsPrometheusVO.setId(1L)
        devopsPrometheusVO.setAdminPassword("test")
        devopsPrometheusVO.setClusterName("uat")
        devopsPrometheusVO.setGrafanaDomain("www.hand.com")
        devopsPrometheusVO.setPvName("test")
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/prometheus?cluster_id=1", ClusterResourceVO.class, 1L)
        then:
        entity.body.getStatus() != null
    }

    def "deletePrometheus"() {
        given:
        when:
        HttpEntity httpEntity = new HttpEntity();
        def entity = restTemplate.exchange(MAPPING + "/prometheus/unload?cluster_id=1", DevopsPrometheusVO.class, 1L)
        then:
        entity.body != null
    }
}
