package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.GitlabGroupMemberService
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.app.service.impl.CertificationServiceImpl
import io.choerodon.devops.infra.dto.CertificationDTO
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.RepositoryFileDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 *
 * @author zmf*
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
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private IamService iamRepository
    @Autowired
    private GitlabServiceClientOperator gitlabRepository
    @Autowired
    private CertificationServiceImpl certificationService
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler clusterConnectionHandler

    private BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient)
    private GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient)
    private GitlabGroupMemberService gitlabGroupMemberService = Mockito.mock(GitlabGroupMemberService)

    @Shared
    private CertificationDTO certificationDTO = new CertificationDTO()
    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()
    @Shared
    private DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO()
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false

    def setup() {
        if (isToInit) {
            DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", baseServiceClient)
            DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(certificationService, "gitlabGroupMemberService", gitlabGroupMemberService)

            // environment
            devopsEnvironmentDTO.setProjectId(projectId)
            devopsEnvironmentDTO.setName("env-test")
            devopsEnvironmentDTO.setClusterId(1L)
            devopsEnvironmentDTO.setCode("env-test")
            devopsEnvironmentDTO.setGitlabEnvProjectId(1L)
            devopsEnvironmentMapper.insert(devopsEnvironmentDTO)

            // devops env command
            devopsEnvCommandDTO.setCommandType("instance")
            devopsEnvCommandMapper.insert(devopsEnvCommandDTO)

            // certification
            certificationDTO.setOrganizationId(organizationId)
            certificationDTO.setDomains("[\"aaa.c7n.wenqi.us\"]")
            certificationDTO.setSkipCheckProjectPermission(Boolean.TRUE)
            certificationDTO.setEnvId(devopsEnvironmentDTO.getId())
            certificationDTO.setName("cert-name")
            certificationDTO.setCommandId(devopsEnvCommandDTO.getId())
            devopsCertificationMapper.insert(certificationDTO)

            // mock baseServiceClient
            ProjectDTO projectDTO = new ProjectDTO()
            projectDTO.setId(projectId)
            projectDTO.setOrganizationId(organizationId)
            projectDTO.setCode("cert")
            ResponseEntity<ProjectDTO> iamPro = new ResponseEntity<>(projectDTO, HttpStatus.OK)
            Mockito.when(baseServiceClient.queryIamProject(Mockito.anyLong())).thenReturn(iamPro)

            OrganizationDTO organizationDTO = new OrganizationDTO()
            organizationDTO.setId(1L)
            ResponseEntity<OrganizationDTO> organizationEntity = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
            Mockito.when(baseServiceClient.queryOrganizationById(Mockito.anyLong())).thenReturn(organizationEntity)

            RepositoryFileDTO repositoryFile = new RepositoryFileDTO()
            repositoryFile.setFilePath("test")
            ResponseEntity<RepositoryFileDTO> repositoryFileEntity = new ResponseEntity<>(repositoryFile, HttpStatus.OK)
            Mockito.when(gitlabServiceClient.createFile(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(repositoryFileEntity)
//            Mockito.doReturn(repositoryFileEntity).when(gitlabServiceClient).createFile( null, anyString(), anyString(), anyString(), anyInt())

        }


    }

    def cleanup() {
        if (isToClean) {
            DependencyInjectUtil.restoreDefaultDependency(iamRepository, "baseServiceClient")
            DependencyInjectUtil.restoreDefaultDependency(certificationService, "gitlabGroupMemberService")

            devopsEnvironmentMapper.delete(devopsEnvironmentDTO)
            devopsCertificationMapper.delete(certificationDTO)
            devopsEnvCommandMapper.delete(devopsEnvCommandDTO)
        }
    }

    def "Create"() {
        given: "插入数据"
        isToInit = false
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>()
        map.add("envId", devopsEnvironmentDTO.getId())
        map.add("envName", certificationDTO.getName())
        map.add("certName", "pro-cert-name")
        map.add("domains", Arrays.asList("cd.as.aa.aa"))
        map.add("type", "request")
        map.add("certValue", "-----BEGIN CERTIFICATE-----\n" +
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
        map.add("keyValue", "-----BEGIN RSA PRIVATE KEY-----\n" +
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

        when: "创建证书"
        restTemplate.postForEntity(BASE_URL, map, Object, projectId)

        then: "校验证书是否创建成功"
        devopsCertificationMapper.selectAll().size() == 2
    }

    def "ListByOptions"() {
        given: "准备数据"
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        clusterConnectionHandler.getConnectedEnvList() >> envList
        clusterConnectionHandler.getUpdatedEnvList() >> envList
        def url = BASE_URL + "/list_by_options?page=0&size=10&sort=id,desc&env_id={env_id}"
        def requestBody = "{\"searchParam\":{},\"param\":\"\"}"

        when: "调用方法"
        def entity = restTemplate.postForEntity(url, requestBody, Page, projectId, certificationDTO.getEnvId())

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().size() == 2
    }

    // 通过域名查询已生效的证书
    def "GetActiveByDomain"() {
        given: "准备数据"
        def url = BASE_URL + "/active?env_id={env_id}&domain={domain}"
        HttpHeaders headers = new HttpHeaders()
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf8")
        HttpEntity<Object> httpEntity = new HttpEntity<>("", headers)


        when: "通过域名查询已生效的证书"
//        def entity = restTemplate.postForEntity(url, "", String, projectId, certificationDO.getEnvId(), certificationDO.getDomains())
        def entity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String, projectId, certificationDTO.getEnvId(), certificationDTO.getDomains())

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        !entity.getBody().isEmpty()
    }

    def "CheckCertNameUniqueInEnv"() {
        given: "准备数据"
        def url = BASE_URL + "/unique?env_id={env_id}&cert_name={cert_name}"
        Map<String, Object> requestParams = new HashMap<>(2)
        requestParams.put("env_id", devopsEnvironmentDTO.getId())
        requestParams.put("cert_name", certificationDTO.getName())
        requestParams.put("project_id", projectId)

        when: "发送请求"
        def entity = restTemplate.getForEntity(url, Boolean, requestParams)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() == Boolean.FALSE

        and: "更改参数"
        requestParams.put("cert_name", certificationDTO.getName() + "non")

        when: "再次请求"
        entity = restTemplate.getForEntity(url, Boolean, requestParams)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() == Boolean.TRUE
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

    def "Delete"() {
        given: "准备数据"
        def url = BASE_URL + "?cert_id={cert_id}"
        isToClean = true

        when: "删除证书"
        restTemplate.delete(url, projectId, certificationDTO.getId())

        then: "校验结果"
        List<CertificationDTO> certificationDOList = devopsCertificationMapper.selectAll()
        certificationDOList.forEach {
            devopsCertificationMapper.delete(it)
        }
        devopsCertificationMapper.selectAll().size() == 0
    }
}
