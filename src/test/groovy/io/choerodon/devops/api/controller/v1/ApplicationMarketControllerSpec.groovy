package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.AppMarketVersionDTO
import io.choerodon.devops.api.dto.ApplicationReleasingDTO
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO
import io.choerodon.devops.infra.dataobject.ApplicationVersionDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/10/23
 * Time: 21:34
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ApplicationMarketController)
@Stepwise
class ApplicationMarketControllerSpec extends Specification {
    private static flag = 0

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    @Shared
    ApplicationDO applicationDO = new ApplicationDO()
    @Shared
    DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
    @Shared
    ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
    @Shared
    ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()

    @Shared
    Organization organization = new Organization()
    @Shared
    ProjectE projectE = new ProjectE()
    @Shared
    UserAttrE userAttrE = new UserAttrE()
    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    PageRequest pageRequest = new PageRequest()
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L

    def setupSpec() {
        organization.setId(init_id)
        organization.setCode("org")

        projectE.setId(init_id)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        userAttrE.setIamUserId(init_id)
        userAttrE.setGitlabUserId(init_id)

        Map<String, Object> xxx = new HashMap<>()
        xxx.put("name", [])
        xxx.put("code", ["app"])
        searchParam.put("searchParam", xxx)
        searchParam.put("param", "")

        pageRequest.size = 10
        pageRequest.page = 0

        // da
        applicationDO.setId(1L)
        applicationDO.setActive(true)
        applicationDO.setProjectId(1L)
        applicationDO.setCode("appCode")
        applicationDO.setName("appName")

        // dav
        applicationVersionDO.setId(1L)
        applicationVersionDO.setAppId(1L)
        applicationVersionDO.setIsPublish(1L)
        applicationVersionDO.setVersion("0.0")

        // dai
        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setAppId(1L)

        // de
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(2L)
    }

    def "Create"() {
        given: '插入数据'
        applicationMapper.insert(applicationDO)
        applicationVersionMapper.insert(applicationVersionDO)

        and: '准备DTO'
        ApplicationReleasingDTO applicationReleasingDTO = new ApplicationReleasingDTO()
        applicationReleasingDTO.setAppId(1L)
        applicationReleasingDTO.setImgUrl("imgUrl")
        applicationReleasingDTO.setCategory("category")
        applicationReleasingDTO.setContributor("contributor")
        applicationReleasingDTO.setDescription("description")
        applicationReleasingDTO.setPublishLevel("organization")

        and: '应用版本'
        List<AppMarketVersionDTO> appVersions = new ArrayList<>()
        AppMarketVersionDTO appMarketVersionDTO = new AppMarketVersionDTO()
        appMarketVersionDTO.setId(1L)
        appVersions.add(appMarketVersionDTO)
        applicationReleasingDTO.setAppVersions(appVersions)

        when: '应用发布'
        def marketId = restTemplate.postForObject("/v1/projects/1/apps_market", applicationReleasingDTO, Long.class)

        then:
        marketId == 1L
    }

    def "PageListMarketAppsByProjectId"() {
        given: '插入数据'
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        applicationInstanceMapper.insert(applicationInstanceDO)

        and: '设置默认值'
        List<ProjectE> projectEList = new ArrayList<>()
        projectEList.add(projectE)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.listIamProjectByOrgId(_, _) >> projectEList

        when: '查询所有发布在应用市场的应用'
        def page = restTemplate.postForObject("/v1/projects/1/apps_market/list", searchParam, Page.class)

        then:
        page.getContent().get(0)["name"]=="appName"
    }

    def "ListAllApp"() {
    }

    def "QueryAppInProject"() {
    }

    def "QueryApp"() {
    }

    def "QueryAppVersionsInProject"() {
    }

    def "QueryAppVersionsInProjectByPage"() {
    }

    def "QueryAppVersionReadme"() {
    }

    def "Update"() {
    }

    def "UpdateVersions"() {
    }

    def "UploadApps"() {
    }

    def "ImportApps"() {
    }

    def "DeleteZip"() {
    }

    def "ExportFile"() {
    }
}
