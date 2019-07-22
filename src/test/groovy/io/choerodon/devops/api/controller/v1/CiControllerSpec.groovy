package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ProjectVO
<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.UserAttrE
<<<<<<< HEAD
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository
<<<<<<< HEAD

=======
import io.choerodon.devops.domain.application.repository.IamRepository
>>>>>>> [IMP] 修改AppControler重构
=======
>>>>>>> [IMP]重构后端代码
=======

>>>>>>> [IMP] refactor validator
import io.choerodon.devops.domain.application.valueobject.OrganizationVO
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.dataobject.ApplicationDTO
import io.choerodon.devops.infra.dataobject.ApplicationVersionDO
import io.choerodon.devops.infra.dataobject.ApplicationVersionReadmeDO
import io.choerodon.devops.infra.dataobject.ApplicationVersionValueDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper
import io.choerodon.devops.infra.mapper.ApplicationVersionReadmeMapper
import io.choerodon.devops.infra.mapper.ApplicationVersionValueMapper
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
    private ApplicationMapper applicationMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private
    ApplicationVersionReadmeMapper applicationVersionReadmeMapper
    @Autowired
    private ApplicationVersionValueMapper applicationVersionValueMapper
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository

    @Autowired
    private IamRepository iamRepository

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    @Shared
    OrganizationVO organization = new OrganizationVO()
    @Shared
    ProjectVO projectE = new ProjectVO()
    @Shared
    UserAttrE userAttrE = new UserAttrE()
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    ApplicationDTO applicationDO

    def setupSpec() {
        given:
        organization.setId(init_id)
        organization.setCode("org")

        projectE.setId(init_id)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        userAttrE.setIamUserId(init_id)
        userAttrE.setGitlabUserId(init_id)

        applicationDO = new ApplicationDTO()
        applicationDO.setId(1L)
        applicationDO.setProjectId(project_id)
        applicationDO.setToken("token")
        applicationDO.setCode("app")
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)
    }

    def "QueryFile"() {
        given: '创建应用'
        applicationMapper.insert(applicationDO)

        when: '应用查询ci脚本文件'
        def str = restTemplate.getForObject("/ci?token=token", String.class)

        then: '校验返回结果'
        str != null && "" != str
    }

    def "Create"() {
        given: '创建应用版本'
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
        applicationVersionDO.setId(1L)
        applicationVersionDO.setAppId(init_id)
        applicationVersionDO.setVersion("oldVersion")
        applicationVersionDO.setReadmeValueId(init_id)
        applicationVersionMapper.insert(applicationVersionDO)

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
        // 删除appVersionReadme
        List<ApplicationVersionReadmeDO> list2 = applicationVersionReadmeMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (ApplicationVersionReadmeDO e : list2) {
                applicationVersionReadmeMapper.delete(e)
            }
        }
        // 删除appVersionValue
        List<ApplicationVersionValueDO> list3 = applicationVersionValueMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (ApplicationVersionValueDO e : list3) {
                applicationVersionValueMapper.delete(e)
            }
        }
    }

    //清除测试数据
    def cleanupSpec() {
        FileUtil.deleteDirectory(new File("Charts"))
    }
}
