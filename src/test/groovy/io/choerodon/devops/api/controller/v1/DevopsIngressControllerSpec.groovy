package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsIngressDTO
import io.choerodon.devops.api.dto.DevopsIngressPathDTO
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.common.util.GitUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.common.util.enums.CertificationStatus
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.mapper.*
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import io.choerodon.websocket.helper.EnvListener
import io.choerodon.websocket.helper.EnvSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/12
 * Time: 11:07
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsIngressControllerSpec extends Specification {

    private static flag = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsIngressMapper devopsIngressMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private DevopsServiceMapper devopsServiceMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private DevopsProjectRepository devopsProjectRepository
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository


    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil
    @Autowired
    @Qualifier("mockGitUtil")
    private GitUtil gitUtil
    @Autowired
    @Qualifier("mockEnvListener")
    private EnvListener envListener
    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository
    @Autowired
    @Qualifier("mockGitlabRepository")
    private GitlabRepository gitlabRepository
    @Autowired
    @Qualifier("mockUserAttrRepository")
    private UserAttrRepository userAttrRepository
    @Autowired
    @Qualifier("mockGitlabGroupMemberRepository")
    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    private DevopsIngressDTO devopsIngressDTO
    private DevopsIngressPathDTO devopsIngressPathDTO

    def setup() {
        if (flag == 0) {

            FileUtil.copyFile("src/test/gitops/org/pro/env/test-ing.yaml", "gitops/org/pro/env")


            DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
            devopsEnvironmentDO.setId(1L)
            devopsEnvironmentDO.setProjectId(1L)
            devopsEnvironmentDO.setActive(true)
            devopsEnvironmentDO.setGitlabEnvProjectId(1L)
            devopsEnvironmentDO.setSequence(1L)
            devopsEnvironmentDO.setCode("env")
            devopsEnvironmentDO.setDevopsEnvGroupId(1L)
            devopsEnvironmentMapper.insert(devopsEnvironmentDO)

            devopsIngressPathDTO = new DevopsIngressPathDTO()
            devopsIngressPathDTO.setServiceId(1L)
            devopsIngressPathDTO.setPath("/bootz")
            devopsIngressPathDTO.setServicePort(7777L)
            devopsIngressPathDTO.setServiceName("test")
            devopsIngressPathDTO.setServiceStatus("running")
            List<DevopsIngressPathDTO> pathList = new ArrayList<>()
            pathList.add(devopsIngressPathDTO)

            devopsIngressDTO = new DevopsIngressDTO()
            devopsIngressDTO.setEnvId(1L)
            devopsIngressDTO.setCertId(1L)
            devopsIngressDTO.setName("test.ing")
            devopsIngressDTO.setPathList(pathList)
            devopsIngressDTO.setDomain("test.hand-china.com")


            DevopsServiceDO devopsServiceDO = new DevopsServiceDO()
            devopsServiceDO.setId(1L)
            devopsServiceDO.setEnvId(1L)
            devopsServiceDO.setAppId(1L)
            devopsServiceDO.setName("svc")
            devopsServiceDO.setCommandId(1L)
            devopsServiceDO.setStatus("running")
            devopsServiceDO.setType("ClusterIP")
            devopsServiceDO.setExternalIp("1.1.1.1")
            devopsServiceDO.setPorts("[{\"port\":7777}]")
            devopsServiceMapper.insert(devopsServiceDO)


            DevopsIngressDO devopsIngressDO = new DevopsIngressDO()
            devopsIngressDO.setEnvId(1L)
            devopsIngressDO.setCertId(1L)
            devopsIngressDO.setUsable(true)
            devopsIngressDO.setName("ingdo")
            devopsIngressDO.setCommandId(1L)
            devopsIngressDO.setProjectId(1L)
            devopsIngressDO.setStatus("running")
            devopsIngressDO.setCommandType("create")
            devopsIngressDO.setObjectVersionNumber(1L)
            devopsIngressDO.setCommandStatus("success")
            devopsIngressDO.setDomain("test.hand-china.com")
            devopsIngressMapper.insert(devopsIngressDO)


            DevopsEnvCommandDO devopsEnvCommandDO = new DevopsEnvCommandDO()
            devopsEnvCommandDO.setId(1L)
            devopsEnvCommandDO.setStatus("success")
            devopsEnvCommandDO.setCommandType("create")
            devopsEnvCommandMapper.insert(devopsEnvCommandDO)


            DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO()
            devopsIngressPathDO.setId(2L)
            devopsIngressPathDO.setIngressId(1L)
            devopsIngressPathDO.setServiceId(1L)
            devopsIngressPathDO.setPath("testpath")
            devopsIngressPathMapper.insert(devopsIngressPathDO)

            DevopsEnvFileResourceDO devopsEnvFileResourceDO = new DevopsEnvFileResourceDO()
            devopsEnvFileResourceDO.setId(1L)
            devopsEnvFileResourceDO.setEnvId(1L)
            devopsEnvFileResourceDO.setResourceId(1L)
            devopsEnvFileResourceDO.setResourceType("Ingress")
            devopsEnvFileResourceDO.setFilePath("test-ing.yaml")
            devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO)

            flag = 1
        }
    }

    def "Create"() {
        given:
        UserAttrE userAttrE = new UserAttrE(1, 1)

        CertificationDO certificationDO = new CertificationDO()
        certificationDO.setStatus(CertificationStatus.ACTIVE.getStatus())
        certificationDO.setName("cert")
        devopsCertificationMapper.insert(certificationDO)

        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE()
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue())

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setDevopsEnvGroupId(1)
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        userAttrRepository.queryById(_ as Long) >> userAttrE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> groupMemberE
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE

        when:
        restTemplate.postForEntity("/v1/projects/1/ingress", devopsIngressDTO, Object.class)

        then:
        devopsIngressMapper.selectByPrimaryKey(1L).getId() != null

    }

    def "Update"() {
        given:
        devopsIngressPathDTO = new DevopsIngressPathDTO()
        devopsIngressPathDTO.setPath("/bootz")
        devopsIngressPathDTO.setServiceId(1L)
        devopsIngressPathDTO.setServicePort(7777L)
        devopsIngressPathDTO.setServiceName("test")
        devopsIngressPathDTO.setServiceStatus("running")
        List<DevopsIngressPathDTO> pathList = new ArrayList<>()
        pathList.add(devopsIngressPathDTO)
        // 修改后的DTO
        DevopsIngressDTO newDevopsIngressDTO = new DevopsIngressDTO()
        newDevopsIngressDTO = new DevopsIngressDTO()
        newDevopsIngressDTO.setId(1L)
        newDevopsIngressDTO.setEnvId(1L)
        newDevopsIngressDTO.setCertId(1L)
        newDevopsIngressDTO.setCertName("newcertname")
        newDevopsIngressDTO.setName("test.test")
        newDevopsIngressDTO.setPathList(pathList)
        newDevopsIngressDTO.setDomain("test.test-test.test")

        UserAttrE userAttrE = new UserAttrE(1, 1)

        GitlabGroupMemberE groupMemberE
        groupMemberE = new GitlabGroupMemberE()
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue())

        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setDevopsEnvGroupId(1)

        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        userAttrRepository.queryById(_ as Long) >> userAttrE
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> groupMemberE
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        gitUtil.cloneBySsh(_ as String, _ as String) >> null
        gitlabRepository.updateFile(_ as Integer, _ as String, _ as String, _ as String, _ as Integer) >> null

        when:
        restTemplate.put("/v1/projects/1/ingress/1", newDevopsIngressDTO, Object.class)

        then:
        devopsIngressMapper.selectByPrimaryKey(1L).getDomain().equals("test.test-test.test")

    }

    def "PageByOptions"() {
        given:
        String infra = "{\"searchParam\":{\"name\":[\"ing\"]}}"

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        Map<String, EnvSession> envs = new HashMap<>()
        EnvSession envSession = new EnvSession()
        envSession.setEnvId(1L)
        envSession.setVersion("0.10.0")
        envs.put("testenv", envSession)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envListener.connectedEnv() >> envs
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList
        when:
        def page = restTemplate.postForObject("/v1/projects/1/ingress/list_by_options", strEntity, Page.class)

        then:
        page.size() == 1
    }

    def "QueryDomainId"() {
        when:
        def dto = restTemplate.getForObject("/v1/projects/1/ingress/1", DevopsIngressDTO.class)

        then:
        dto != null
    }

    def "Delete"() {
        given:
        UserAttrE userAttrE = new UserAttrE(1, 1)

        GitlabGroupMemberE groupMemberE
        groupMemberE = new GitlabGroupMemberE()
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue())

        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setDevopsEnvGroupId(1)
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        userAttrRepository.queryById(_ as Long) >> userAttrE
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> groupMemberE
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when:
        restTemplate.delete("/v1/projects/1/ingress/1")

        then:
        devopsEnvCommandRepository.queryByObject("ingress", 1L).getCommandType().equals("delete")

    }

    def "CheckName"() {
        when:
        boolean exist = restTemplate.getForObject("/v1/projects/1/ingress/check_name?name=test&envId=1", Boolean.class)

        then:
        exist == true
    }

    def "CheckDomain"() {
        when:
        boolean exist = restTemplate.getForObject("/v1/projects/1/ingress/check_domain?domain=test.test&path=testpath&id=1", Boolean.class)

        then:
        exist == true
    }

    def "ListByEnv"() {
        given:
        String infra = "{\"searchParam\":{\"name\":[\"ing\"]}}"
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        Map<String, EnvSession> envs = new HashMap<>()
        EnvSession envSession = new EnvSession()
        envSession.setEnvId(1L)
        envSession.setVersion("0.10.0")
        envs.put("testenv", envSession)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envListener.connectedEnv() >> envs
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        def page = restTemplate.postForObject("/v1/projects/1/ingress/1/listByEnv", strEntity, Page.class)

        then:
        page.size() == 1
        devopsEnvironmentMapper.deleteByPrimaryKey(1L)
        devopsIngressMapper.deleteByPrimaryKey(1L)
        devopsServiceMapper.deleteByPrimaryKey(1L)
        devopsEnvCommandMapper.deleteByPrimaryKey(1L)
        devopsEnvFileResourceMapper.deleteByPrimaryKey(1L)
        devopsIngressMapper.deleteByPrimaryKey(2L)
        FileUtil.deleteDirectory(new File("gitops"))
    }
}