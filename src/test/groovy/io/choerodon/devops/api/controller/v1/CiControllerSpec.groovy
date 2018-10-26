package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.ApplicationVersionDO
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper
import io.choerodon.devops.infra.mapper.ApplicationVersionReadmeMapper
import io.choerodon.devops.infra.mapper.ApplicationVersionValueMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.io.FileSystemResource
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

    private static flag = 0

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
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    @Shared
    Organization organization = new Organization()
    @Shared
    ProjectE projectE = new ProjectE()
    @Shared
    UserAttrE userAttrE = new UserAttrE()
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L

    private ApplicationDO applicationDO

    def setupSpec() {
        given:
        organization.setId(init_id)
        organization.setCode("org")

        projectE.setId(init_id)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        userAttrE.setIamUserId(init_id)
        userAttrE.setGitlabUserId(init_id)
    }

    def setup() {
        if (flag == 0) {
            // 创建应用
            applicationDO = new ApplicationDO()
            applicationDO.setId(1L)
            applicationDO.setProjectId(project_id)
            applicationDO.setToken("token")
            applicationDO.setCode("app")
            applicationMapper.insert(applicationDO)
            flag = 1
        }
    }

    def "QueryFile"() {
        given: '默认返回值'
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when:
        def str = restTemplate.getForObject("/ci?token=token", String.class)

        then:
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

        and: '默认返回值'
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        FileSystemResource resource = new FileSystemResource(new File("src/test/resources/key.tar.gz"))
        MultiValueMap<String, Object> file = new LinkedMultiValueMap<>()
        file.add("file", resource)

        when:
        restTemplate.postForObject("/ci?image=iamge&token=token&version=version&commit=commit", file,
                String.class)

        then:
        File gzFile = new File("/Charts/org/pro/key.tar.gz")
        gzFile != null
        File yamlFile = new File("/devopsversion/values.yaml")
        yamlFile != null
        applicationMapper.deleteByPrimaryKey(1L)
        applicationVersionMapper.deleteByPrimaryKey(1L)
    }

    //清除测试数据
    def cleanupSpec() {
        FileUtil.deleteDirectory(new File("Charts"))
    }
}
