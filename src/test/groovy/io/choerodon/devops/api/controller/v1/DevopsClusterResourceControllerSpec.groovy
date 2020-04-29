package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ClusterResourceVO
import io.choerodon.devops.api.vo.DevopsPrometheusVO
import io.choerodon.devops.api.vo.DevopsPvVO
import io.choerodon.devops.api.vo.DevopsPvcReqVO
import io.choerodon.devops.api.vo.DevopsPvcRespVO
import io.choerodon.devops.app.service.ComponentReleaseService
import io.choerodon.devops.app.service.DevopsClusterResourceService
import io.choerodon.devops.app.service.DevopsPvcService
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO
import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO
import io.choerodon.devops.infra.dto.iam.ClientDTO
import io.choerodon.devops.infra.dto.iam.ClientVO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.DevopsClusterResourceMapper
import io.choerodon.devops.infra.util.GenerateUUID
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
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
    @Autowired
    private DevopsClusterResourceMapper devopsClusterResourceMapper

    BaseServiceClientOperator baseServiceClientOperator = Mockito.mock(BaseServiceClientOperator.class)
    ComponentReleaseService componentReleaseService = Mockito.mock(ComponentReleaseService.class)
    DevopsPvcService devopsPvcService = Mockito.mock(io.choerodon.devops.app.service.DevopsPvcService.class)

    def setup() {
        DependencyInjectUtil.setAttribute(devopsClusterResourceService, "baseServiceClientOperator", baseServiceClientOperator)
        DependencyInjectUtil.setAttribute(devopsClusterResourceService, "componentReleaseService", componentReleaseService)
        DependencyInjectUtil.setAttribute(devopsClusterResourceService, "devopsPvcService", devopsPvcService)
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
        Mockito.doReturn(clientDTO).when(baseServiceClientOperator).createClient(1L, clientVO)

        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO()
        DevopsPrometheusDTO prometheusDTO = new DevopsPrometheusDTO()
        prometheusDTO.setAdminPassword("123")
        prometheusDTO.setId(2L)
        prometheusDTO.setGrafanaDomain("123")
        appServiceInstanceDTO.setId(1L)
        appServiceInstanceDTO.setEnvId(1L)
        appServiceInstanceDTO.setCommandId(1L)
        Mockito.doReturn(appServiceInstanceDTO).when(componentReleaseService).createReleaseForPrometheus(anyLong(), any(DevopsPrometheusDTO.class))
        Mockito.doReturn(appServiceInstanceDTO).when(componentReleaseService).updateReleaseForPrometheus(any(DevopsPrometheusDTO.class), anyLong(), anyLong())
        Mockito.doNothing().when(componentReleaseService).deleteReleaseForComponent(anyLong(), anyBoolean())
        DevopsPvcRespVO devopsPvcReqVO=new DevopsPvcRespVO()
        devopsPvcReqVO.setId(11)
        Mockito.doReturn(devopsPvcReqVO).when(devopsPvcService).create(anyLong(), any(DevopsPvcReqVO))


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

    def "queryCertManagerByEnvId"() {
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/cert_manager/check_by_env_id?env_id=1", Boolean.class, 1L)
        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "ListClusterResource"() {
        when:
        def entity = restTemplate.getForEntity(MAPPING + "?cluster_id=1", List.class, 1L)
        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "UnloadCertManager"() {
        when:
        def entity = restTemplate.exchange(MAPPING + "/cert_manager/unload?cluster_id=1", HttpMethod.DELETE, null, Boolean.class, 1L)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "createPrometheus"() {
        given:
        def devopsPvVO = new DevopsPvVO()
        def devopsPvVO1 = new DevopsPvVO()
        def devopsPvVO2 = new DevopsPvVO()
        List<DevopsPvVO> list = new ArrayList<>()
        devopsPvVO.setId(1L)
        devopsPvVO.setType("promtheus")
        devopsPvVO.setName("123")
        devopsPvVO.setAccessModes("test")
        devopsPvVO.setRequestResource("test2")
        devopsPvVO1.setId(2L)
        devopsPvVO1.setType("grafana")
        devopsPvVO1.setName("456")
        devopsPvVO1.setAccessModes("test")
        devopsPvVO1.setRequestResource("test2")
        devopsPvVO2.setId(3L)
        devopsPvVO2.setType("alertManager")
        devopsPvVO2.setName("789")
        devopsPvVO2.setAccessModes("test")
        devopsPvVO2.setRequestResource("test2")
        list.add(devopsPvVO)
        list.add(devopsPvVO1)
        list.add(devopsPvVO2)
        DevopsPrometheusVO devopsPrometheusVO = new DevopsPrometheusVO()
        devopsPrometheusVO.setAdminPassword("test")
        devopsPrometheusVO.setClusterCode("code101")
        devopsPrometheusVO.setGrafanaDomain("www.hand.com")
        devopsPrometheusVO.setPvs(list)
        when:
        def entity = restTemplate.postForEntity(MAPPING + "/prometheus/create?cluster_id=1", devopsPrometheusVO, Boolean.class, 1L)
        then:
        entity.body == true

    }

    def "updatePromtheus"() {

        given:
        def devopsPrometheusVO = devopsClusterResourceService.queryPrometheus(1L)
        def devopsPvVO = new DevopsPvVO()
        def devopsPvVO1 = new DevopsPvVO()
        def devopsPvVO2 = new DevopsPvVO()
        List<DevopsPvVO> list = new ArrayList<>()
        devopsPvVO.setId(1L)
        devopsPvVO.setType("update")
        devopsPvVO.setName("123")
        devopsPvVO.setAccessModes("test")
        devopsPvVO.setRequestResource("test2")
        devopsPvVO1.setId(2L)
        devopsPvVO1.setType("grafana")
        devopsPvVO1.setName("456")
        devopsPvVO1.setAccessModes("test")
        devopsPvVO1.setRequestResource("test2")
        devopsPvVO2.setId(3L)
        devopsPvVO2.setType("alertManager")
        devopsPvVO2.setName("789")
        devopsPvVO2.setAccessModes("test")
        devopsPvVO2.setRequestResource("test2")
        list.add(devopsPvVO)
        list.add(devopsPvVO1)
        list.add(devopsPvVO2)

        devopsPrometheusVO.setGrafanaDomain("www.abc.com")
        devopsPrometheusVO.setPvs(list)
        devopsPrometheusVO.setId(devopsPrometheusVO.getId())
        HttpHeaders headers = new HttpHeaders()
        HttpEntity<DevopsPrometheusVO> requestEntity = new HttpEntity<DevopsPrometheusVO>(devopsPrometheusVO, headers)
        when:
        def entity = restTemplate.exchange(MAPPING + "/prometheus/update?cluster_id=1", HttpMethod.PUT, requestEntity, ResponseEntity.class, 1L)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "queryPrometheus"() {
        given:
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/prometheus?cluster_id=1", DevopsPrometheusVO.class, 1L)
        then:
        entity.body != null
    }

    def "getDeployStatus"() {
        given:
        DevopsPrometheusVO devopsPrometheusVO = new DevopsPrometheusVO()
        devopsPrometheusVO.setId(1L)
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/prometheus/deploy_status?cluster_id=1", ClusterResourceVO.class, 1L)
        then:
        entity.body.getStatus() != null
    }

    def "deletePrometheus"() {
        given:
        when:
        def entity = restTemplate.exchange(MAPPING + "/prometheus/unload?cluster_id=1", HttpMethod.DELETE, null, ResponseEntity.class, 1L)
        then:
        entity.statusCode.is2xxSuccessful()

    }
}
