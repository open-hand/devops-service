package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.dataobject.DevopsEnvFileErrorDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/7
 * Time: 14:54
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsEnvFileErrorControllerSpec extends Specification {

    private static flag = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvFileErrorMapper devopsEnvFileErrorMapper

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    def setup() {
        if (flag == 0) {
            Date date = new Date(2018, 9, 7, 15, 20, 0)
            DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO()
            devopsEnvFileErrorDO.setEnvId(1L)
            devopsEnvFileErrorDO.setCreationDate(date)
            DevopsEnvFileErrorDO devopsEnvFileErrorDO1 = new DevopsEnvFileErrorDO()
            devopsEnvFileErrorDO1.setEnvId(1L)
            devopsEnvFileErrorDO1.setCreationDate(date)
            devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDO)
            devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDO1)

            DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
            devopsEnvironmentDO.setDevopsEnvGroupId(1L)
            devopsEnvironmentDO.setProjectId(1)
            devopsEnvironmentDO.setCode("ecode")
            devopsEnvironmentMapper.insert(devopsEnvironmentDO)

            flag = 1
        }
    }

    def "List"() {
        given:
        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("ocode")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pcode")
        projectE.setOrganization(organization)

        when:
        def list = restTemplate.getForObject("/v1/projects/1/envs/1/error_file/list", List.class)

        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        !list.isEmpty()
    }

    def "Page"() {
        given:
        PageRequest pageRequest = new PageRequest(1, 20)

        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("ocode")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pcode")
        projectE.setOrganization(organization)

        when:
        def page = restTemplate.getForObject("/v1/projects/1/envs/1/error_file/list_by_page", Object.class)

        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        page != null
    }
}
