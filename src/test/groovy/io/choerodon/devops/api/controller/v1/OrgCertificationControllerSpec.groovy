package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.controller.v1.DevopsClusterController
import io.choerodon.devops.api.dto.OrgCertificationDTO
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper
import io.choerodon.devops.infra.mapper.DevopsCertificationProRelMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.Matchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsClusterController)
@Stepwise
class OrgCertificationControllerSpec extends Specification {

    private static final String MAPPING = "/v1/organizations/{organization_id}/certs"
    private static Long ID;


    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private IamRepository iamRepository
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper
    @Autowired
    private DevopsCertificationProRelMapper devopsCertificationProRelMapper

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    void setup() {
        iamRepository.initMockIamService(iamServiceClient)

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

        Page<ProjectDO> projectDOPage = new Page<>()
        List<ProjectDO> projectDOList = new ArrayList<>()
        projectDOList.add(projectDO)
        projectDOPage.setContent(projectDOList)
        ResponseEntity<Page<ProjectDO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryProjectByOrgId(anyLong(), anyInt(), anyInt(), anyString(), any(String[].class))).thenReturn(projectDOPageResponseEntity)
    }

    def "Create"() {
        given: '初始化DTO'
        OrgCertificationDTO orgCertificationDTO = new OrgCertificationDTO()
        orgCertificationDTO.setName("test")
        orgCertificationDTO.setDomain("test")
        orgCertificationDTO.setCertValue("test")
        orgCertificationDTO.setSkipCheckProjectPermission(false)
        List<Long> projectIds = new ArrayList<>()
        projectIds.add(1L)
        orgCertificationDTO.setProjects(projectIds)

        when: '组织下创建集群'
        restTemplate.postForObject(MAPPING, orgCertificationDTO, Object.class, 1L)

        then: '校验返回值'
        devopsCertificationMapper.selectAll().size() == 1

    }

    def "Update"() {
        given: '初始化DTO'
        OrgCertificationDTO orgCertificationDTO = new OrgCertificationDTO()
        List<Long> projectIds = new ArrayList<>()
        projectIds.add(2L)
        orgCertificationDTO.setProjects(projectIds)
        ID = devopsCertificationMapper.selectAll().get(0).getId()
        orgCertificationDTO.setSkipCheckProjectPermission(false)

        when: '更新集群下的项目'
        restTemplate.put(MAPPING + "/" + ID, orgCertificationDTO, 1L)

        then: '校验是否更新'
        devopsCertificationProRelMapper.selectAll().get(0).getProjectId() == 2
    }

    def "Query"() {
        when: '查询单个集群信息'
        def dto = restTemplate.getForObject(MAPPING + "/" + ID, OrgCertificationDTO.class, 1L)

        then: '校验返回值'
        dto["name"] == "test"
    }

    def "CheckName"() {
        when: '校验集群名唯一性'
        def exception = restTemplate.getForEntity(MAPPING + "/check_name?name=uniqueName", ExceptionResponse.class, 1L)

        then: '名字不存在不抛出异常'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }

    def "PageProjects"() {
        given: '模糊查询参数'
        String[] str = new String[1]
        str[0] = "{}"

        when: '分页查询项目列表'
        def e = restTemplate.postForEntity(MAPPING + "/page_projects?page=0&size=10&certId=" + ID, str, Page.class, 1L)

        then: '校验返回值'
        e.getBody().get(0)["code"] == "pro"
    }

    def "ListCertProjects"() {
        when: '查询已有权限的项目列表'
        def e = restTemplate.getForEntity(MAPPING + "/list_cert_projects/{certId}", List.class, 1L, ID)

        then: '校验返回值'
        e.getBody().get(0)["code"] == "pro"
    }

    def "ListOrgCert"() {
        given: '查询参数'
        String str = new String("{}")


        when: '集群列表查询'
        def e = restTemplate.postForEntity(MAPPING + "/page_cert?page=0&size=10", str, Page.class, 1L)

        then: '校验返回值'
        e.getBody().get(0)["name"] == "test"
    }

    def "DeleteOrgCert"() {

        when: '删除集群'
        restTemplate.delete(MAPPING + "/{certId}", 1L, ID)

        then: '校验返回值'
        devopsCertificationMapper.selectAll().size() == 0
        devopsCertificationProRelMapper.selectAll().size() == 0
    }
}
