package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.dataobject.DevopsEnvFileErrorDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
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
 * Date: 2018/9/7
 * Time: 14:54
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvFileErrorController)
@Stepwise
class DevopsEnvFileErrorControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvFileErrorMapper devopsEnvFileErrorMapper

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    @Shared
    Long init_id = 1L
    @Shared
    Long project_id = 1L
    @Shared
    ProjectE projectE = new ProjectE()
    @Shared
    UserAttrE userAttrE = new UserAttrE()
    @Shared
    Organization organization = new Organization()

    def setupSpec() {
        given:
        organization.setId(init_id)
        organization.setCode("org")

        projectE.setId(project_id)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        userAttrE.setIamUserId(init_id)
        userAttrE.setGitlabUserId(init_id)
    }

    def "List"() {
        given: '插入数据'
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO()
        devopsEnvFileErrorDO.setId(1L)
        devopsEnvFileErrorDO.setEnvId(1L)
        devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDO)
        DevopsEnvFileErrorDO devopsEnvFileErrorDO1 = new DevopsEnvFileErrorDO()
        devopsEnvFileErrorDO.setId(2L)
        devopsEnvFileErrorDO1.setEnvId(1L)
        devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDO1)

        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setCode("env")
        devopsEnvironmentDO.setProjectId(1)
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)

        and: '设置默认值'
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when:
        def list = restTemplate.getForObject("/v1/projects/1/envs/1/error_file/list", List.class)

        then:
        list.size() == 2
    }

    def "Page"() {
        given: '设置默认值'
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when:
        def page = restTemplate.getForObject("/v1/projects/1/envs/1/error_file/list_by_page", Page.class)

        then:
        page.size() == 2

        // 清理数据
        devopsEnvFileErrorMapper.deleteByPrimaryKey(1L)
        devopsEnvFileErrorMapper.deleteByPrimaryKey(2L)
        devopsEnvironmentMapper.deleteByPrimaryKey(1L)
    }
}
