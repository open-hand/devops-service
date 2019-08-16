package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.base.domain.PageRequest
import io.choerodon.core.domain.Page
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.ExportOctetStream2HttpMessageConverter
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.AppServiceMarketDownloadVO
import io.choerodon.devops.api.vo.AppServiceMarketVO
import io.choerodon.devops.api.vo.AppServiceMarketVersionVO
import io.choerodon.devops.api.vo.AppServiceReleasingVO
import io.choerodon.devops.api.vo.DevopsEnvApplicationVO
import io.choerodon.devops.api.vo.UserAttrVO
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO
import io.choerodon.devops.infra.dto.AppServiceShareRuleDTO
import io.choerodon.devops.infra.dto.AppServiceVersionDTO
import io.choerodon.devops.infra.dto.AppServiceVersionReadmeDTO
import io.choerodon.devops.infra.dto.AppServiceVersionValueDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.iam.ApplicationDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
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
@Subject(AppShareRuleController)
@Stepwise
class AppServiceMarketControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private AppServiceShareRuleMapper applicationMarketMapper
    @Autowired
    private AppServiceVersionMapper applicationVersionMapper
    @Autowired
    private AppServiceInstanceMapper applicationInstanceMapper
    @Autowired
    private AppServiceVersionValueMapper appServiceVersionValueMapper
    @Autowired
    private AppServiceVersionReadmeMapper appServiceVersionReadmeMapper

    @Autowired
    private IamService iamService

    BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient.class)

    @Shared
    ApplicationDTO applicationDTO = new ApplicationDTO()
    @Shared
    DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()
    @Shared
    AppServiceVersionDTO applicationVersionDTO = new AppServiceVersionDTO()
    @Shared
    AppServiceInstanceDTO applicationInstanceDO = new AppServiceInstanceDTO()

    @Shared
    OrganizationDTO organization = new OrganizationDTO()
    @Shared
    ProjectDTO projectDTO = new ProjectDTO()
    @Shared
    UserAttrVO userAttrE = new UserAttrVO()

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

        projectDTO.setId(init_id)
        projectDTO.setCode("pro")
        projectDTO.setOrganizationId(organization.getId())

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
        applicationDTO.setId(1L)
        applicationDTO.setActive(true)
        applicationDTO.setProjectId(1L)
        applicationDTO.setCode("appCode")
        applicationDTO.setName("appName")

        // dav
        applicationVersionDTO.setId(1L)
        applicationVersionDTO.setAppServiceId(1L)
        applicationVersionDTO.setIsPublish(1L)
        applicationVersionDTO.setVersion("0.0")
        applicationVersionDTO.setReadmeValueId(1L)
        applicationVersionDTO.setRepository("http://helm-charts.saas.test.com/ystest/ystest/")

        // dai
        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setAppServiceId(1L)

        // de
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(2L)
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamService, "baseServiceClient", baseServiceClient)

        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(baseServiceClient).queryIamProject(anyLong())

        OrganizationDTO organizationDO = new OrganizationDTO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(anyLong())

        List<ProjectDTO> projectDOList = new ArrayList<>()
        projectDOList.add(projectDO)
        PageInfo<ProjectDTO> projectDOPage = new PageInfo(projectDOList)
        ResponseEntity<PageInfo<ProjectDTO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
        Mockito.when(baseServiceClient.queryProjectByOrgId(anyLong(), anyInt(), anyInt(), isNull(), isNull())).thenReturn(projectDOPageResponseEntity)
    }

    def "Create"() {
        given: '插入数据'
        applicationMapper.insert(applicationDTO)
        applicationVersionMapper.insert(applicationVersionDTO)

        and: '准备DTO'

        AppServiceReleasingVO applicationReleasingDTO = new AppServiceReleasingVO()
        applicationReleasingDTO.setAppServiceId(1L)
        applicationReleasingDTO.setImgUrl("imgUrl")
        applicationReleasingDTO.setCategory("category")
        applicationReleasingDTO.setContributor("contributor")
        applicationReleasingDTO.setDescription("description")
        applicationReleasingDTO.setPublishLevel("public")

        and: '应用版本'
        List<AppServiceMarketVO> appVersions = new ArrayList<>()
        AppServiceMarketVersionVO appMarketVersionDTO = new AppServiceMarketVersionVO()
        appMarketVersionDTO.setId(1L)
        appVersions.add(appMarketVersionDTO)
        applicationReleasingDTO.setAppServiceVersions(appVersions)

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
        def dto = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}/detail", AppServiceReleasingVO.class
                , applicationMarketMapper.selectAll().get(0).getId())

        then: '验证返回值'
        dto["code"] == "appCode"
    }

    def "QueryApp"() {
        when: '查询项目下单个应用市场的应用详情'
        def dto = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}", AppServiceReleasingVO.class,
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
        AppServiceVersionReadmeDTO applicationVersionReadmeDO = new AppServiceVersionReadmeDTO()
        applicationVersionReadmeDO.setId(1L)
        applicationVersionReadmeDO.setReadme("readme")
        appServiceVersionReadmeMapper.insert(applicationVersionReadmeDO)

        when: '查询单个应用市场的应用的单个版本README'
        def str = restTemplate.getForObject("/v1/projects/1/apps_market/{app_market_id}/versions/{version_id}/readme",
                String.class, applicationMarketMapper.selectAll().get(0).getId(), applicationVersionMapper.selectAll().get(0).getId())

        then: '验证返回值'
        str == "readme"
    }

    def "Update"() {
        given: '初始化DTO'
        Long appMarketId = applicationMarketMapper.selectAll().get(0).getId()
        AppServiceReleasingVO applicationReleasingDTO = new AppServiceReleasingVO()
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
        AppServiceMarketVersionVO appMarketVersionDTO = new AppServiceMarketVersionVO()
        appMarketVersionDTO.setId(1L)
        List<AppServiceMarketVO> dtoList = new ArrayList<>()
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
        def dto = restTemplate.postForObject("/v1/projects/1/apps_market/upload", requestEntity, AppServiceMarketVO.class)
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
        List<AppServiceMarketDownloadVO> dtoList = new ArrayList<>()
        AppServiceMarketDownloadVO appMarketDownloadDTO = new AppServiceMarketDownloadVO()
        appMarketDownloadDTO.setAppMarketId(applicationMarketMapper.selectAll().get(0).getId())
        List<Long> appVersionList = new ArrayList<>()
        appVersionList.add(1L)
        appMarketDownloadDTO.setAppServiceVersionIds(appVersionList)
        dtoList.add(appMarketDownloadDTO)

        and: '设置http响应返回值类型'
        restTemplate.getRestTemplate().getMessageConverters().add(new ExportOctetStream2HttpMessageConverter())
        HttpHeaders headers = new HttpHeaders()
        List headersList = new ArrayList<>()
        headersList.add(MediaType.APPLICATION_OCTET_STREAM)
        headers.setAccept(headersList)

        HttpEntity<List<AppServiceMarketDownloadVO>> reqHttpEntity = new HttpEntity<>(dtoList)

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
        List<AppServiceVersionDTO> list1 = applicationVersionMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (AppServiceVersionDTO e : list1) {
                applicationVersionMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvApplicationVO> list2 = devopsEnvironmentMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (DevopsEnvApplicationVO e : list2) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除appVersionReadme
        List<AppServiceVersionReadmeDTO> list3 = appServiceVersionReadmeMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (AppServiceVersionReadmeDTO e : list3) {
                appServiceVersionReadmeMapper.delete(e)
            }
        }
        // 删除appVersionValue
        List<AppServiceVersionValueDTO> list4 = appServiceVersionValueMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (AppServiceVersionValueDTO e : list4) {
                appServiceVersionValueMapper.delete(e)
            }
        }
        // 删除appMarket
        List<AppServiceShareRuleDTO> list5 = applicationMarketMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (AppServiceShareRuleDTO e : list5) {
                applicationMarketMapper.delete(e)
            }
        }
        // 删除appInstance
        List<AppServiceVersionDTO> list6 = applicationInstanceMapper.selectAll()
        if (list6 != null && !list6.isEmpty()) {
            for (AppServiceVersionDTO e : list6) {
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
