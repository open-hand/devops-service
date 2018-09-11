package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.domain.application.entity.ApplicationE
import io.choerodon.devops.domain.application.entity.ApplicationVersionE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.repository.ApplicationRepository
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.ApplicationVersionReadmeV
import io.choerodon.devops.domain.application.valueobject.Organization
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.io.FileSystemResource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/6
 * Time: 16:36
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class CiControllerSpec extends Specification {
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationRepository applicationRepository
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    def "QueryFile"() {
        given:
        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("ocode")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pcode")
        projectE.setOrganization(organization)

        ApplicationE applicationE = new ApplicationE()
        applicationE.setProjectE(projectE)
        applicationE.setToken("token")
        applicationE.setCode("acode")
        applicationRepository.create(applicationE)

        when:
        restTemplate.getForObject("/ci?token=token", String.class)

        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
    }

    def "Create"() {
        given:
        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("ocode")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pcode")
        projectE.setOrganization(organization)

        ApplicationE applicationE = new ApplicationE()
        applicationE.setProjectE(projectE)
        applicationE.setToken("token2222")
        applicationE.setCode("acode")
        applicationRepository.create(applicationE)

        ApplicationVersionReadmeV applicationVersionReadmeV = new ApplicationVersionReadmeV()
        applicationVersionReadmeV.setReadme("readme")
        ApplicationVersionE applicationVersionE = new ApplicationVersionE()
        applicationVersionE.setApplicationE(applicationE)
        applicationVersionE.setVersion("version")
        applicationVersionE.setApplicationVersionReadmeV(applicationVersionReadmeV)
        ApplicationVersionE applicationVersionE1 = new ApplicationVersionE()
        applicationVersionE1.setApplicationE(applicationE)
        applicationVersionE1.setVersion("version")
        applicationVersionE1.setApplicationVersionReadmeV(applicationVersionReadmeV)
        applicationVersionRepository.create(applicationVersionE)
        applicationVersionRepository.create(applicationVersionE1)

        FileSystemResource resource = new FileSystemResource(new File("src/test/resources/key.tar.gz"))
        MultiValueMap<String, Object> file = new LinkedMultiValueMap<>()
        file.add("file", resource)

        when:
        restTemplate.postForObject("/ci?image=iamge&token=token&version=version&commit=commit", file, String.class)

        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
    }
}
