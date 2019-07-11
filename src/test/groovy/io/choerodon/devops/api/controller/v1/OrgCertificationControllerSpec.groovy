package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.OrgCertificationDTO

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
@Subject(OrgCertificationController)
@Stepwise
class OrgCertificationControllerSpec extends Specification {

    private static final String MAPPING = "/v1/organizations/{organization_id}/certs"
    private static Long ID


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
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)

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

        List<ProjectDO> projectDOList = new ArrayList<>()
        projectDOList.add(projectDO)
        PageInfo<ProjectDO> projectDOPage = new PageInfo<>(projectDOList)
        ResponseEntity<PageInfo<ProjectDO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryProjectByOrgId(anyLong(), anyInt(), anyInt(), any(), any())).thenReturn(projectDOPageResponseEntity)
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
        orgCertificationDTO.setCertValue("-----BEGIN CERTIFICATE-----\n" +
                "MIICYTCCAcoCCQCs45mePIbzRTANBgkqhkiG9w0BAQUFADB1MQswCQYDVQQGEwJV\n" +
                "UzENMAsGA1UECAwETWFyczETMBEGA1UEBwwKaVRyYW5zd2FycDETMBEGA1UECgwK\n" +
                "aVRyYW5zd2FycDETMBEGA1UECwwKaVRyYW5zd2FycDEYMBYGA1UEAwwPd3d3LjU5\n" +
                "MXdpZmkuY29tMB4XDTE4MTAxNzAyMTA0OFoXDTI4MTAxNDAyMTA0OFowdTELMAkG\n" +
                "A1UEBhMCVVMxDTALBgNVBAgMBE1hcnMxEzARBgNVBAcMCmlUcmFuc3dhcnAxEzAR\n" +
                "BgNVBAoMCmlUcmFuc3dhcnAxEzARBgNVBAsMCmlUcmFuc3dhcnAxGDAWBgNVBAMM\n" +
                "D3d3dy41OTF3aWZpLmNvbTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAtxtP\n" +
                "cxgppTHrbzWloh26fXfIyLZI+YpNMCnJ+4wcv3jnZZ6OZsvnoo0z/yl/A9kDY9r5\n" +
                "Rft9fwE4WKMSPNKlGd4psPLw1XNHAXhi8RAy1cHgkBMuwor6ZJhFgnsqKk4Xp68D\n" +
                "jaCI2oxu2SYIBU67Fxy+h7G5BsWKwARtj5kP8NECAwEAATANBgkqhkiG9w0BAQUF\n" +
                "AAOBgQC2Pko8q1NicJ0oPuhFTPm7n03LtPhCaV/aDf3mqtGxraYifg8iFTxVyZ1c\n" +
                "ol0eEJFsibrQrPEwdSuSVqzwif5Tab9dV92PPFm+Sq0D1Uc0xI4ziXQ+a55K9wrV\n" +
                "TKXxS48TOpnTA8fVFNkUkFNB54Lhh9AwKsx123kJmyaWccbt9Q==\n" +
                "-----END CERTIFICATE-----")
        orgCertificationDTO.setKeyValue("-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIICXgIBAAKBgQC3G09zGCmlMetvNaWiHbp9d8jItkj5ik0wKcn7jBy/eOdlno5m\n" +
                "y+eijTP/KX8D2QNj2vlF+31/AThYoxI80qUZ3imw8vDVc0cBeGLxEDLVweCQEy7C\n" +
                "ivpkmEWCeyoqThenrwONoIjajG7ZJggFTrsXHL6HsbkGxYrABG2PmQ/w0QIDAQAB\n" +
                "AoGBAIxvTcggSBCC8OciZh6oXlfMfxoxdFavU/QUmO1s0L+pow+1Q9JjoQxy7+ZL\n" +
                "lTcGQitbzsN11xKJhQW2TE6J4EVimJZQSAE4DDmYpMOrkjnBQhkUlaZkkukvDSRS\n" +
                "JqwBI/04G7se+RouHyXjRS9U76HnPM8+/IS2h+T6CbXLOpYBAkEA2j0JmyGVs+WV\n" +
                "I9sG5glamJqTBa4CfTORrdFW4EULoGkUc24ZFFqn9W4e5yfl/pCkPptCenvIrAWp\n" +
                "/ymnHeLn6QJBANbKGO9uBizAt4+o+kHYdANcbU/Cs3PLj8yOOtjkuMbH4tPNQmB6\n" +
                "/u3npiVk7/Txfkg0BjRzDDZib109eKbvGKkCQBgMneBghRS7+gFng40Z/sfOUOFR\n" +
                "WajeY/FZnk88jJlyuvQ1b8IUc2nSZslmViwFWHQlu9+vgF+kiCU8O9RJSvECQQCl\n" +
                "Vkx7giYerPqgC2MY7JXhQHSkwSuCJ2A6BgImk2npGlTw1UATJJq4Z2jtwBU2Z+7d\n" +
                "ha6BEU6FTqCLFZaaadKBAkEAxko4hrgBsX9BKpFJE3aUIUcMTJfJQdiAhq0k4DV8\n" +
                "5GVrcp8zl6mUTPZDaOmDhuAjGdAQJqj0Xo0PZ0fOZPtR+w==\n" +
                "-----END RSA PRIVATE KEY-----")
        when: '组织下创建证书'
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

        when: '更新证书下的项目'
        restTemplate.put(MAPPING + "/" + ID, orgCertificationDTO, 1L)

        then: '校验是否更新'
        devopsCertificationProRelMapper.selectAll().get(0).getProjectId() == 2
    }

    def "Query"() {
        when: '查询单个证书信息'
        def dto = restTemplate.getForObject(MAPPING + "/" + ID, OrgCertificationDTO.class, 1L)

        then: '校验返回值'
        dto["name"] == "test"
    }

    def "CheckName"() {
        when: '校验证书名唯一性'
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


        when: '证书列表查询'
        def e = restTemplate.postForEntity(MAPPING + "/page_cert?page=0&size=10", str, Page.class, 1L)

        then: '校验返回值'
        e.getBody().get(0)["name"] == "test"
    }

    def "DeleteOrgCert"() {

        when: '删除证书'
        restTemplate.delete(MAPPING + "/{certId}", 1L, ID)

        then: '校验返回值'
        devopsCertificationMapper.selectAll().size() == 0
        devopsCertificationProRelMapper.selectAll().size() == 0
    }
}
