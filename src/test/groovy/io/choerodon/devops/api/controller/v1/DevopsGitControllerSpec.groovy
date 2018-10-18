package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.mapper.ApplicationMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsGitControllerSpec extends Specification {


    private static flag = 0


    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationMapper applicationMapper

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository


    void setup() {
        if (flag == 0) {


            ApplicationDO applicationDO = new ApplicationDO()
            applicationDO.setCode("test")
            applicationDO.setName("test")
            applicationDO.setGitlabProjectId(1)
            applicationMapper.insert(applicationDO)

            flag = 1
        }
    }

    def "GetUrl"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")
        ProjectE projectE = initProj(1L, "testProject", organization)

        when:
        def url = restTemplate.getForObject("/v1/projects/1/apps/1/git/url", String.class)


        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        !url.equals("")


    }

    def "CreateTag"() {
    }

    def "UpdateTagRelease"() {
    }

    def "GetTagByPage"() {
    }

    def "GetTagList"() {
    }

    def "CheckTag"() {
    }

    def "DeleteTag"() {
    }

    def "CreateBranch"() {
    }

    def "ListByAppId"() {
    }

    def "QueryByAppId"() {
    }

    def "Update"() {
    }

    def "Delete"() {
    }

    def "GetMergeRequestList"() {
    }


    private static Organization initOrg(Long id, String code) {
        Organization organization = new Organization()
        organization.setId(id)
        organization.setCode(code)
        organization
    }

    private static ProjectE initProj(Long id, String code, Organization organization) {
        ProjectE projectE = new ProjectE()
        projectE.setId(id)
        projectE.setCode(code)
        projectE.setOrganization(organization)
        projectE
    }
}
