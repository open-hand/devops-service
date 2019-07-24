package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.base.domain.PageRequest
import io.choerodon.core.domain.Page
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.ExportOctetStream2HttpMessageConverter
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.AppMarketDownloadVO
import io.choerodon.devops.api.vo.AppMarketTgzVO
import io.choerodon.devops.api.vo.AppMarketVersionVO
import io.choerodon.devops.api.vo.ApplicationReleasingVO
import io.choerodon.devops.api.vo.ProjectVO

import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.OrganizationVO
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.*
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/10/23
 * Time: 21:34
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ApplicationShareController)
@Stepwise
class ApplicationMarketControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationShareMapper applicationMarketMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private ApplicationVersionValueMapper applicationVersionValueMapper
    @Autowired
    private ApplicationVersionReadmeMapper applicationVersionReadmeMapper

    @Autowired
    private IamRepository iamRepository

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    @Shared
    ApplicationDTO applicationDO = new ApplicationDTO()
    @Shared
    DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
    @Shared
    ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
    @Shared
    ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()

    @Shared
    OrganizationVO organization = new OrganizationVO()
    @Shared
    ProjectVO projectE = new ProjectVO()
    @Shared
    UserAttrE userAttrE = new UserAttrE()
    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    PageRequest pageRequest = new PageRequest(1,0)
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    String fileCode

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
        applicationVersionDO.setRepository("http://helm-charts.saas.test.com/ystest/ystest/")

        // dai
        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setAppId(1L)

        // de
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(2L)
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(anyLong())

        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(anyLong())

        List<ProjectDO> projectDOList = new ArrayList<>()
        projectDOList.add(projectDO)
        PageInfo<ProjectDO> projectDOPage = new PageInfo(projectDOList)
        ResponseEntity<PageInfo<ProjectDO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryProjectByOrgId(anyLong(), anyInt(), anyInt(), isNull(), isNull())).thenReturn(projectDOPageResponseEntity)
    }

    def "Create"() {
        given: '插入数据'
        applicationMapper.insert(applicationDO)
        applicationVersionMapper.insert(applicationVersionDO)

        and: '准备DTO'
        ApplicationReleasingVO applicationReleasingDTO = new ApplicationReleasingVO()
        applicationReleasingDTO.setAppId(1L)
        applicationReleasingDTO.setImgUrl("imgUrl")
        applicationReleasingDTO.setCategory("category")
        applicationReleasingDTO.setContributor("contributor")
        applicationReleasingDTO.setDescription("description")
        applicationReleasingDTO.setPublishLevel("public")

        and: '应用版本'
        List<AppMarketVersionVO> appVersions = new ArrayList<>()
        AppMarketVersionVO appMarketVersionDTO = new AppMarketVersionVO()
        appMarketVersionDTO.setId(1L)
        appVersions.add(appMarketVersionDTO)
        applicationReleasingDTO.setAppVersions(appVersions)

        when: '应用发布'
        def marketId = restTemplate.postForObject("/v1/projects/1/apps_market", applicationReleasingDTO, Long.class)

        then: '验证创建后的id'
        applicationMarketMapper.selectAll().get(0)["id"] == marketId
    }

    def "PageListMarketAppsByProjectId"() {
        given: '插入数据'
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        applicationInstanceMapper.insert(applicationInstanceDO)

        when: '查询所有发布在应用市场的应用'
        def page = restTemplate.postForObject("/v1/projects/1/apps_market/baseList", searchParam, Page.class)

        then: '验证返回值'
        page.getContent().get(0)["name"] == "appName"
    }

    def "ListAllApp"() {
        when: '查询发布级别为全局或者在本组织下的所有应用市场的应用'
        def page = restTemplate.postForObject("/v1/projects/1/apps_market/list_all", searchParam, Page.class)

        then: '验证返回值'
        page.getContent().get(0)["name"] == "appName"
    }

    def "QueryAppInProject"() {
        when: '查询项目下单个应用市场的应用详情'
        def dto = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}/detail", ApplicationReleasingVO.class
                , applicationMarketMapper.selectAll().get(0).getId())

        then: '验证返回值'
        dto["code"] == "appCode"
    }

    def "QueryApp"() {
        when: '查询项目下单个应用市场的应用详情'
        def dto = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}", ApplicationReleasingVO.class,
                applicationMarketMapper.selectAll().get(0).getId())

        then: '验证返回值'
        dto["code"] == "appCode"
    }

    def "QueryAppVersionsInProject"() {
        when: '查询项目下单个应用市场的应用的版本'
        def list = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}/versions", List.class,
                applicationMarketMapper.selectAll().get(0).getId())

        then: '验证返回值'
        list.get(0)["version"] == "0.0"
    }

    def "QueryAppVersionsInProjectByPage"() {
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

        then: '验证返回值'
        str == "readme"
    }

    def "Update"() {
        given: '初始化DTO'
        Long appMarketId = applicationMarketMapper.selectAll().get(0).getId()
        ApplicationReleasingVO applicationReleasingDTO = new ApplicationReleasingVO()
        applicationReleasingDTO.setId(appMarketId)
        applicationReleasingDTO.setContributor("newContributor")
        applicationReleasingDTO.setPublishLevel("public")
        when: '更新单个应用市场的应用'
        restTemplate.put("/v1/projects/1/apps_market/{app_market_id}", applicationReleasingDTO, appMarketId)

        then: '验w证更新后的contributor字段'
        applicationMarketMapper.selectAll().get(0)["contributor"] == "newContributor"
    }

    def "UpdateVersions"() {
        given: '准备dotList'
        AppMarketVersionVO appMarketVersionDTO = new AppMarketVersionVO()
        appMarketVersionDTO.setId(1L)
        List<AppMarketVersionVO> dtoList = new ArrayList<>()
        dtoList.add(appMarketVersionDTO)

        when: '更新单个应用市场的应用'
        restTemplate.put("/v1/projects/1/apps_market/{app_market_id}/versions", dtoList,
                applicationMarketMapper.selectAll().get(0).getId())

        then: '验证返回值'
        applicationMarketMapper.selectAll().get(0)["contributor"] == "newContributor"
    }

    def "UploadApps"() {
        given: '设置multipartFile'
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.parseMediaType("multipart/form-data"))

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>()
        FileSystemResource fileSystemResource = new FileSystemResource("src/test/resources/charts.zip")
        map.add("file", fileSystemResource)
        map.add("filename", fileSystemResource.getFilename())

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(map, headers)

        when: '应用市场解析导入应用'
        def dto = restTemplate.postForObject("/v1/projects/1/apps_market/upload", requestEntity, AppMarketTgzVO.class)
        fileCode = dto.fileCode

        then: '验证返回值'
        dto.getAppMarketList().get(0)["id"] == 27
    }

    def "ImportApps"() {
        when: '应用市场导入应用'
        def bool = restTemplate.postForObject("/v1/projects/1/apps_market/import?file_name=" + fileCode + "&public=true",
                null, Boolean.class)

        then: '验证返回值'
        bool
        applicationMarketMapper.selectAll().get(1)["contributor"] == "Choerodon"
        applicationMarketMapper.selectAll().get(0)["contributor"] == "newContributor"
    }

    def "DeleteZip"() {
        when: '应用市场取消导入应用'
        restTemplate.postForObject("/v1/projects/1/apps_market/import_cancel?file_name=", null, Object.class)

        then: '验证返回值'
//        File file = new File("tmp/org")
//        file.listFiles().size() == 1
        FileUtil.deleteDirectory(new File("Charts"))
    }

    def "ExportFile"() {
        given: '准备dto baseList'
        List<AppMarketDownloadVO> dtoList = new ArrayList<>()
        AppMarketDownloadVO appMarketDownloadDTO = new AppMarketDownloadVO()
        appMarketDownloadDTO.setAppMarketId(applicationMarketMapper.selectAll().get(0).getId())
        List<Long> appVersionList = new ArrayList<>()
        appVersionList.add(1L)
        appMarketDownloadDTO.setAppVersionIds(appVersionList)
        dtoList.add(appMarketDownloadDTO)

        and: '设置http响应返回值类型'
        restTemplate.getRestTemplate().getMessageConverters().add(new ExportOctetStream2HttpMessageConverter())
        HttpHeaders headers = new HttpHeaders()
        List headersList = new ArrayList<>()
        headersList.add(MediaType.APPLICATION_OCTET_STREAM)
        headers.setAccept(headersList)

        HttpEntity<List<AppMarketDownloadVO>> reqHttpEntity = new HttpEntity<>(dtoList)

        when: '导出应用市场应用信息'
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange("/v1/projects/1/apps_market/export?fileName=testChart", HttpMethod.POST, reqHttpEntity, byte[].class)

        then: '验证返回值'
        responseEntity.getHeaders().get("Content-Length").get(0).toString().toInteger() != 0

        // 删除app
        List<ApplicationDTO> list = applicationMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (ApplicationDTO e : list) {
                applicationMapper.delete(e)
            }
        }
        // 删除appVersion
        List<ApplicationVersionDO> list1 = applicationVersionMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (ApplicationVersionDO e : list1) {
                applicationVersionMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDO> list2 = devopsEnvironmentMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (DevopsEnvironmentDO e : list2) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除appVersionReadme
        List<ApplicationVersionReadmeDO> list3 = applicationVersionReadmeMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (ApplicationVersionReadmeDO e : list3) {
                applicationVersionReadmeMapper.delete(e)
            }
        }
        // 删除appVersionValue
        List<ApplicationVersionValueDO> list4 = applicationVersionValueMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (ApplicationVersionValueDO e : list4) {
                applicationVersionValueMapper.delete(e)
            }
        }
        // 删除appMarket
        List<DevopsAppShareDO> list5 = applicationMarketMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (DevopsAppShareDO e : list5) {
                applicationMarketMapper.delete(e)
            }
        }
        // 删除appInstance
        List<ApplicationInstanceDO> list6 = applicationInstanceMapper.selectAll()
        if (list6 != null && !list6.isEmpty()) {
            for (ApplicationInstanceDO e : list6) {
                applicationInstanceMapper.delete(e)
            }
        }
    }

    // 清除测试数据
    def cleanupSpec() {
        FileUtil.deleteDirectory(new File("Charts"))
        FileUtil.deleteDirectory(new File("devops-service"))
        FileUtil.deleteDirectory(new File("tmp"))
    }
}
