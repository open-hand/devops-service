package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ProjectVO
import io.choerodon.devops.app.service.AppServiceVersionService
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.AppServiceVersionDTO
import io.choerodon.devops.infra.dto.AppServiceVersionReadmeDTO
import io.choerodon.devops.infra.dto.AppServiceVersionValueDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.mapper.AppServiceMapper
import io.choerodon.devops.infra.mapper.AppServiceVersionMapper
import io.choerodon.devops.infra.mapper.AppServiceVersionReadmeMapper
import io.choerodon.devops.infra.mapper.AppServiceVersionValueMapper
import io.choerodon.devops.infra.util.FileUtil
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/6
 * Time: 16:36
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(CiController)
@Stepwise
class CiControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private AppServiceVersionMapper applicationVersionMapper
    @Autowired
    private
    AppServiceVersionReadmeMapper applicationVersionReadmeMapper
    @Autowired
    private AppServiceVersionValueMapper applicationVersionValueMapper
    @Autowired
    private AppServiceVersionService applicationVersionRepository

    @Autowired
    private IamService iamRepository

    BaseServiceClient iamServiceClient = Mockito.mock(BaseServiceClient.class)

    @Shared
    ProjectVO projectE = new ProjectVO()
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    AppServiceDTO appServiceDTO

    def setupSpec() {
//        given:
//        organization.setId(init_id)
//        organization.setCode("org")

        projectE.setId(init_id)
        projectE.setCode("pro")
        projectE.setOrganizationId(init_id)


        appServiceDTO = new AppServiceDTO()
        appServiceDTO.setId(1L)
        appServiceDTO.setAppId(project_id)
        appServiceDTO.setToken("token")
        appServiceDTO.setCode("app")
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", iamServiceClient)

        ProjectDTO projectDTO = new ProjectDTO()
        projectDTO.setId(1L)
        projectDTO.setCode("pro")
        projectDTO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDTO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

        OrganizationDTO organizationDTO = new OrganizationDTO()
        organizationDTO.setId(1L)
        organizationDTO.setCode("org")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)
    }

    def "QueryFile"() {
        given: '创建应用'
        applicationMapper.insert(appServiceDTO)

        when: '应用查询ci脚本文件'
        def str = restTemplate.getForObject("/ci?token=token", String.class)

        then: '校验返回结果'
        str != null && "" != str
    }

    def "Create"() {
        given: '创建应用版本'
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO()
        appServiceVersionDTO.setId(1L)
        appServiceVersionDTO.setAppServiceId(init_id)
        appServiceVersionDTO.setVersion("oldVersion")
        appServiceVersionDTO.setReadmeValueId(init_id)
        applicationVersionMapper.insert(appServiceVersionDTO)

        FileSystemResource resource = new FileSystemResource(new File("src/test/resources/key.tar.gz"))
        MultiValueMap<String, Object> file = new LinkedMultiValueMap<>()
        file.add("file", resource)

        when: '获取应用版本信息'
        restTemplate.postForObject("/ci?image=iamge&token=token&version=version&commit=commit", file,
                String.class)

        then: '校验文件是否创建'
        File gzFile = new File("/Charts/org/pro/key.tar.gz")
        gzFile.getName() == "key.tar.gz"
        File yamlFile = new File("/devopsversion/values.yaml")
        yamlFile.getName() == "values.yaml"

        // 删除app
        List<AppServiceDTO> list = applicationMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (AppServiceDTO e : list) {
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
        // 删除appVersionReadme
        List<AppServiceVersionReadmeDTO> list2 = applicationVersionReadmeMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (AppServiceVersionReadmeDTO e : list2) {
                applicationVersionReadmeMapper.delete(e)
            }
        }
        // 删除appVersionValue
        List<AppServiceVersionValueDTO> list3 = applicationVersionValueMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (AppServiceVersionValueDTO e : list3) {
                applicationVersionValueMapper.delete(e)
            }
        }
    }

    //清除测试数据
    def cleanupSpec() {
        FileUtil.deleteDirectory(new File("Charts"))
    }
}
