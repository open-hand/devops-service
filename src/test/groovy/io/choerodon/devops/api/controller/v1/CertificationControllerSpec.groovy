package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.infra.dataobject.CertificationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 *
 * @author zmf
 *
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(CertificationController)
@Stepwise
class CertificationControllerSpec extends Specification {
    private static final String BASE_URL = "/v1/projects/{project_id}/certifications"

    private static Long organizationId = 1L
    private static Long projectId = 1L

    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper
    @Autowired
    private IamRepository iamRepository
    @Autowired
    private TestRestTemplate restTemplate

    private IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient)

    @Shared
    private CertificationDO certificationDO = new CertificationDO()

    def setupSpec() {
        certificationDO.setOrganizationId(organizationId)
        certificationDO.setDomains("[\"aaa.c7n.wenqi.us\"]")
        certificationDO.setSkipCheckProjectPermission(Boolean.TRUE)
    }

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setId(projectId)
        projectDO.setOrganizationId(organizationId)
        ResponseEntity<ProjectDO> iamPro = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryIamProject(Mockito.anyLong())).thenReturn(iamPro)

//        OrganizationDO organizationDO = new OrganizationDO()
//        organizationDO.setId(organizationId)
//        ResponseEntity<OrganizationDO> organization = new ResponseEntity<>(organizationDO, HttpStatus.OK)
//        Mockito.when(iamServiceClient.queryOrganizationById(Mockito.anyLong())).thenReturn(organization)
    }

    def "Create"() {
        given: "插入数据"
        devopsCertificationMapper.insert(certificationDO)
    }

    def "Delete"() {
    }

    def "ListByOptions"() {
    }

    def "GetActiveByDomain"() {
    }

    def "CheckCertNameUniqueInEnv"() {
    }

    // 查询项目下有权限的组织层证书
    def "ListOrgCert"() {
        when: "发送请求"
        def entity = restTemplate.getForEntity(BASE_URL + "/list_org_cert", List, projectId)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
        !entity.getBody().isEmpty()
    }
}
