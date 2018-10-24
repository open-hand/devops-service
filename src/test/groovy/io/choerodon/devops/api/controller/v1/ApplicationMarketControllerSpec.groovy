package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.AppMarketTgzDTO
import io.choerodon.devops.api.dto.AppMarketVersionDTO
import io.choerodon.devops.api.dto.ApplicationReleasingDTO
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.mapper.*
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
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

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationMarketMapper applicationMarketMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private ApplicationVersionValueMapper applicationVersionValueMapper
    @Autowired
    private ApplicationVersionReadmeMapper applicationVersionReadmeMapper

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
        applicationVersionDO.setReadmeValueId(1L)

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

        then: '验证创建后的id'
        marketId == applicationMapper.selectAll().get(0).getId()
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
        page.getContent().get(0)["name"] == "appName"
    }

    def "ListAllApp"() {
        given: '设置默认值'
        List<ProjectE> projectEList = new ArrayList<>()
        projectEList.add(projectE)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.listIamProjectByOrgId(_, _) >> projectEList

        when: '查询发布级别为全局或者在本组织下的所有应用市场的应用'
        def page = restTemplate.postForObject("/v1/projects/1/apps_market/list_all", searchParam, Page.class)

        then:
        page.getContent().get(0)["name"] == "appName"
    }

    def "QueryAppInProject"() {
        given: '设置默认值'
        List<ProjectE> projectEList = new ArrayList<>()
        projectEList.add(projectE)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.listIamProjectByOrgId(_, _) >> projectEList

        when: '查询项目下单个应用市场的应用详情'
        def dto = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}/detail", ApplicationReleasingDTO.class
                , applicationMarketMapper.selectAll().get(0).getId())

        then:
        dto.getName() == "appName"
    }

    def "QueryApp"() {
        given: '设置默认值'
        List<ProjectE> projectEList = new ArrayList<>()
        projectEList.add(projectE)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.listIamProjectByOrgId(_, _) >> projectEList

        when: '查询项目下单个应用市场的应用详情'
        def dto = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}", ApplicationReleasingDTO.class,
                applicationMarketMapper.selectAll().get(0).getId())

        then:
        dto.getName() == "appName"
    }

    def "QueryAppVersionsInProject"() {
        given: '设置默认值'
        List<ProjectE> projectEList = new ArrayList<>()
        projectEList.add(projectE)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.listIamProjectByOrgId(_, _) >> projectEList

        when: '查询项目下单个应用市场的应用的版本'
        def list = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}/versions", List.class,
                applicationMarketMapper.selectAll().get(0).getId())

        then:
        list.get(0)["version"] == "0.0"
    }

    def "QueryAppVersionsInProjectByPage"() {
        given: '设置默认值'
        List<ProjectE> projectEList = new ArrayList<>()
        projectEList.add(projectE)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.listIamProjectByOrgId(_, _) >> projectEList

        when: '分页查询项目下单个应用市场的应用的版本'
        def page = restTemplate.postForObject("/v1/projects/1/apps_market/{app_market_id}/versions", searchParam, Page.class,
                applicationMarketMapper.selectAll().get(0).getId())

        then:
        page.getContent().get(0)["version"] == "0.0"
    }

    def "QueryAppVersionReadme"() {
        given: '插入App Version Readme'
        ApplicationVersionReadmeDO applicationVersionReadmeDO = new ApplicationVersionReadmeDO()
        applicationVersionReadmeDO.setId(1L)
        applicationVersionReadmeDO.setReadme("readme")
        applicationVersionReadmeMapper.insert(applicationVersionReadmeDO)

        when: '查询单个应用市场的应用的单个版本README'
        def str = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}/versions/{version_id}/readme",
                String.class, applicationMarketMapper.selectAll().get(0).getId(), applicationVersionMapper.selectAll().get(0).getId())

        then:
        str == "readme"
    }

    def "Update"() {
        given: '设置默认值'
        List<ProjectE> projectEList = new ArrayList<>()
        projectEList.add(projectE)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.listIamProjectByOrgId(_, _) >> projectEList

        and: '准备DTO'
        ApplicationReleasingDTO applicationReleasingDTO = new ApplicationReleasingDTO()
        applicationReleasingDTO.setId(1L)
        applicationReleasingDTO.setContributor("newContributor")
        applicationReleasingDTO.setPublishLevel("organization")

        when: '更新单个应用市场的应用'
        restTemplate.put("/v1/projects/1/apps_market/{app_market_id}", applicationReleasingDTO,
                applicationMarketMapper.selectAll().get(0).getId())

        then: '验证更新后的contributor字段'
        applicationMarketMapper.selectAll().get(0).getContributor() == "newContributor"
    }

    def "UpdateVersions"() {
        given: '准备dotList'
        AppMarketVersionDTO appMarketVersionDTO = new AppMarketVersionDTO()
        appMarketVersionDTO.setId(1L)
        List<AppMarketVersionDTO> dtoList = new ArrayList<>()
        dtoList.add(appMarketVersionDTO)

        and: '设置默认值'
        List<ProjectE> projectEList = new ArrayList<>()
        projectEList.add(projectE)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.listIamProjectByOrgId(_, _) >> projectEList

        when: '更新单个应用市场的应用'
        restTemplate.put("/v1/projects/1/apps_market/{app_market_id}/versions", dtoList,
                applicationMarketMapper.selectAll().get(0).getId())

        then:
        applicationMarketMapper.selectAll().get(0).getId() == 1
    }

    def "UploadApps"() {
        given: '设置multipartFile'
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.parseMediaType("multipart/form-data"))

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>()
        FileSystemResource fileSystemResource = new FileSystemResource("src/test/resources/chart.zip")
        map.add("file", fileSystemResource)
        map.add("filename", fileSystemResource.getFilename())

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(map, headers)

        and: '设置默认值'
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when: '应用市场解析导入应用'
        def dto = restTemplate.postForObject("/v1/projects/1/apps_market/upload", requestEntity, AppMarketTgzDTO.class)

        then:
        dto.getAppMarketList().get(0).getId() == 27
    }

    def "ImportApps"() {
        given:
        true

        and: '设置默认值'
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        and: '获取文件名'
        File file = new File("tmp/org/pro")
        String fileName = file.listFiles()[0].getName()

        when: '应用市场导入应用'
        def bool = restTemplate.postForObject("/v1/projects/1/apps_market/import?file_name=" + fileName + "&public=true",
                null, Boolean.class)

        then:
        bool == true
        applicationMarketMapper.selectAll().get(1).getContributor() == "Choerodon"
    }

    def "DeleteZip"() {
        given: '设置默认值'
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        and: '获取文件名'

        when: '应用市场取消导入应用'
        restTemplate.postForObject("/v1/projects/1/apps_market/import_cancel?file_name=", null, Object.class)

        then:
        File file = new File("tmp/org")
        file.listFiles().size() == 0

        applicationMapper.deleteByPrimaryKey(1L)
        applicationMapper.deleteByPrimaryKey(2L)

        DevopsAppMarketDO devopsAppMarketDO = new DevopsAppMarketDO()
        devopsAppMarketDO.setContributor("newContributor")
        applicationMarketMapper.delete(devopsAppMarketDO)
        devopsAppMarketDO.setContributor("Choerodon")
        applicationMarketMapper.delete(devopsAppMarketDO)

        applicationVersionMapper.deleteByPrimaryKey(1L)
        ApplicationVersionDO versionDO = new ApplicationVersionDO()
        versionDO.setVersion("0.8.4")
        applicationVersionMapper.delete(versionDO)

        ApplicationInstanceDO instanceDO = new ApplicationInstanceDO()
        instanceDO.setAppId(1L)
        applicationInstanceMapper.delete(instanceDO)

        devopsEnvironmentMapper.deleteByPrimaryKey(1L)

        applicationVersionReadmeMapper.deleteByPrimaryKey(1L)
        applicationVersionReadmeMapper.deleteByPrimaryKey(2L)
        applicationVersionValueMapper.deleteByPrimaryKey(1L)
    }

//    def "ExportFile"() {
//        given: '准备dto list'
//        List<AppMarketDownloadDTO> dtoList = new ArrayList<>()
//        AppMarketDownloadDTO appMarketDownloadDTO = new AppMarketDownloadDTO()
//        appMarketDownloadDTO.setAppMarketId(1L)
//
//        dtoList.add(AppMarketDownloadDTO)
//
//        and: '设置默认值'
//        iamRepository.queryIamProject(_ as Long) >> projectE
//        iamRepository.queryOrganizationById(_ as Long) >> organization
//
//
//        when: '导出应用市场应用信息'
//        restTemplate.postForObject("/v1/projects/1/apps_market/export", dtoList)
//
//        then:
//        applicationMapper.deleteByPrimaryKey(1L)
//        applicationMarketMapper.deleteByPrimaryKey(1L)
//        applicationMarketMapper.deleteByPrimaryKey(2L)
//        applicationVersionMapper.deleteByPrimaryKey(1L)
//        applicationVersionMapper.deleteByPrimaryKey(2L)
//        applicationInstanceMapper.deleteByPrimaryKey(1L)
//        devopsEnvironmentMapper.deleteByPrimaryKey(1L)
//        applicationVersionReadmeMapper.deleteByPrimaryKey(1L)
//        applicationVersionReadmeMapper.deleteByPrimaryKey(2L)
//        applicationVersionValueMapper.deleteByPrimaryKey(1L)
//        applicationMapper.deleteByPrimaryKey(1L)
//        applicationMapper.deleteByPrimaryKey(2L)
//    }
    //清除测试数据
    def cleanupSpec() {
        FileUtil.deleteDirectory(new File("Charts"))
        FileUtil.deleteDirectory(new File("devops-service"))
        FileUtil.deleteDirectory(new File("tmp"))
    }
}
