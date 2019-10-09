package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.HarborMarketVO
import io.choerodon.devops.api.vo.iam.MarketApplicationVO
import io.choerodon.devops.app.eventhandler.payload.*
import io.choerodon.devops.infra.dto.harbor.RobotUser
import io.choerodon.devops.infra.dto.harbor.User
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author: trump*
 * @date: 2019/8/19 19:37
 * @description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(AppServiceVersionController)
@Stepwise
class OrgAppMarketControllerSpec extends Specification {
    @Autowired
    private TestRestTemplate restTemplate

    private static final base_url = "/v1/organizations/app_market"
    BaseServiceClientOperator baseServiceClientOperator = Mockito.mock(BaseServiceClientOperator)
//    def setup() {
//        Mockito.doReturn(null).when(baseServiceClientOperator).completeDownloadApplication(1L)
//        Mockito.doReturn(null).when(baseServiceClientOperator).failToDownloadApplication()
//
//    }

    def "PageByAppId"() {
        given:
        def app_id = 1L
        when:
        def entity = restTemplate.postForEntity(base_url + "/page_app_services?app_id={app_id}", null, PageInfo.class, app_id)
        then:
        entity.statusCode.is2xxSuccessful()

    }

    def "listVersionsByAppServiceId"() {
        given:
        def app_id = 1L
        when:
        def entity = restTemplate.getForEntity(base_url + "/list_versions/{app_service_id}", List.class, app_id)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "Upload"() {
        given:
        MarketApplicationVO marketApplicationVO = new MarketApplicationVO()
        marketApplicationVO.setOrganizationId(1L)
        marketApplicationVO.setType("mkt_deploy_only")
        marketApplicationVO.setName("test")
        marketApplicationVO.setId(1L)
        marketApplicationVO.setCode(1L)
        marketApplicationVO.setVersion(1L)
        AppMarketUploadPayload appMarketUploadPayload = new AppMarketUploadPayload()
        appMarketUploadPayload.setProjectId(1L)
        AppMarketFixVersionPayload appMarketFixVersionPayload = new AppMarketFixVersionPayload()
        appMarketFixVersionPayload.setMarketApplicationVO(marketApplicationVO)
        appMarketFixVersionPayload.setFixVersionUploadPayload(appMarketUploadPayload)
        when:
        def entity = restTemplate.postForEntity(base_url + "/upload_fix_version", null, null,)
        then:
        entity.statusCode.is2xxSuccessful()
    }

    def "ListVersionsByAppServiceId"() {
        given:
        def app_service_id = 1L
        when:
        def entity = restTemplate.getForEntity(base_url + "/list_versions/{app_service_id}", List.class, app_service_id)
        then:
        entity.body.size() != 0
    }

    def "CreateHarborRepository"() {
        given:
        HarborMarketVO harborMarketVO = new HarborMarketVO()
        harborMarketVO.setProjectCode("test_scp")
        User user = new User()
        user.setEmail("test001@qq.com")
        user.setPassword("Handhand")
        user.setUsername("test001")
        user.setRealname("test001")
        harborMarketVO.setUser(user)
        when:
        def entity = restTemplate.postForEntity(base_url + "/harbor_repo", harborMarketVO, String.class)
        then:
        entity.body.size() != 0
    }

    def "DownLoadApp"() {
        given:
        RobotUser user = new RobotUser()
        user.setProjectId(1)
        user.setProjectName("test")
        user.setRobotId(1)
        user.setRobotName("test")
        user.setRobotToken("test")

        //3
        AppServiceVersionDownloadPayload appServiceVersionDownloadPayload = new AppServiceVersionDownloadPayload()
        appServiceVersionDownloadPayload.setVersion("2019.9.27-094026-master")
        appServiceVersionDownloadPayload.setImage("registry.choerodon.com.cn/app_market_9e403fd9-22be-46d3-aeeb-7ee1a5977028/notability:2019.9.27-094026-master")
        appServiceVersionDownloadPayload.setChartFilePath("http://minio.staging.saas.hand-china.com/app-market/file_2eefe960143a47d892a371696c680762_2019.9.27-094026-master.tgz?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=admin%2F20191008%2F%24%7Bminio.region%3A%2Fs3%2Faws4_request&X-Amz-Date=20191008T073038Z&X-Amz-Expires=1800&X-Amz-SignedHeaders=host&X-Amz-Signature=53794cd2300a9afd33d3138b374cfc96fd514265f315359382afd066fc9bdfec")

        List<AppServiceVersionDownloadPayload> list1 = new ArrayList()
        list1.add(appServiceVersionDownloadPayload)
        //2
        AppServiceDownloadPayload appServiceDownloadPayload = new AppServiceDownloadPayload()
        appServiceDownloadPayload.setAppId(80L)
        appServiceDownloadPayload.setAppServiceName("Notability")
        appServiceDownloadPayload.setAppServiceCode("notability")
        appServiceDownloadPayload.setAppServiceType("normal")
        appServiceDownloadPayload.setAppServiceVersionDownloadPayloads(list1)
        List<AppServiceDownloadPayload> list = new ArrayList()
        list.add(appServiceDownloadPayload)

        //1
        AppMarketDownloadPayload appMarketDownloadPayload = new AppMarketDownloadPayload()
        appMarketDownloadPayload.setIamUserId(11543L)
        appMarketDownloadPayload.setOrganizationId(1L)
        appMarketDownloadPayload.setAppId(80L)
        appMarketDownloadPayload.setUser(user)
        appMarketDownloadPayload.setAppName("123")
        appMarketDownloadPayload.setAppCode("232b530f-c469-4a1b-9ddd-602e032e2930")
        appMarketDownloadPayload.setDownloadAppType("mkt_deploy_only")
        appMarketDownloadPayload.setAppVersionId(203L)
        appMarketDownloadPayload.setMktAppVersionId(148L)
        appMarketDownloadPayload.setAppDownloadRecordId(33L)
        appMarketDownloadPayload.setAppServiceDownloadPayloads(list)
        when:
        def entity = restTemplate.postForEntity(base_url + "/download", appMarketDownloadPayload, null)
        then:
        entity.statusCode.is2xxSuccessful()
    }
}
