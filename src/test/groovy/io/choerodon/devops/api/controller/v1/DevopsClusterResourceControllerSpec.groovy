package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ClusterResourceVO
import io.choerodon.devops.api.vo.DevopsPrometheusVO
import io.choerodon.devops.app.service.AgentCommandService
import io.choerodon.devops.app.service.DevopsClusterResourceService
import io.choerodon.devops.infra.dto.iam.ClientDTO
import io.choerodon.devops.infra.dto.iam.ClientVO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.util.GenerateUUID
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

    BaseServiceClientOperator baseServiceClientOperator = Mockito.mock(BaseServiceClientOperator.class)

    def setup(){
        DependencyInjectUtil.setAttribute(devopsClusterResourceService,"baseServiceClientOperator",baseServiceClientOperator)
        ClientDTO clientDTO = new ClientDTO()
        clientDTO.setId(1L)
        ClientVO clientVO = new ClientVO()
        String clientName = String.format("%s%s", "test", GenerateUUID.generateUUID().substring(0, 5))
        clientVO.setName(clientName);
        clientVO.setOrganizationId(1L);
        clientVO.setAuthorizedGrantTypes("password,implicit,client_credentials,refresh_token,authorization_code")
        clientVO.setSecret("secret");
        clientVO.setRefreshTokenValidity(3600L)
        clientVO.setAccessTokenValidity(3600L)
        clientVO.setSourceId(1L);
        clientVO.setSourceType("cluster")
        Mockito.doReturn(clientDTO).when(baseServiceClientOperator).createClient(1L,clientVO)
    }
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
        when:
        def entity = restTemplate.getForEntity(MAPPING+"?cluster_id=1",List.class,1L)
        then:
        entity.getStatusCode().is2xxSuccessful()
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
        def entity = restTemplate.exchange(MAPPING + "/prometheus/unload?cluster_id=1", DevopsPrometheusVO.class, 1L)
        then:
        entity.body != null
    }

    def "getGrafanaUrl"() {
        given:
        when:
        def entity = restTemplate.exchange(MAPPING + "/grafana_url?cluster_id=1&type=node", String.class, 1L)
        then:
        entity.statusCode.is2xxSuccessful()
    }

}
