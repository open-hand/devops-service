package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsServiceDTO
import io.choerodon.devops.api.dto.DevopsServiceReqDTO
import io.choerodon.devops.domain.application.entity.PortMapE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.common.util.GitUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.mapper.*
import io.choerodon.websocket.helper.EnvListener
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
 * Time: 15:22
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsServiceControllerSpec extends Specification {

    private static flag = 0
    private static id = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsServiceMapper devopsServiceMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private DevopsProjectRepository devopsProjectRepository
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper
    @Autowired
    private DevopsServiceAppInstanceMapper devopsServiceAppInstanceMapper
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository
    @Autowired
    private DevopsServiceRepository devopsServiceRepository

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

    private DevopsEnvCommandDO devopsEnvCommandDO
    private DevopsEnvFileResourceDO devopsEnvFileResourceDO
    private DevopsServiceAppInstanceDO devopsServiceAppInstanceDO

    def setup() {
        if (flag == 0) {
            FileUtil.copyFile("src/test/gitops/org/pro/env/test-svc.yaml", "gitops/org/pro/env")

            devopsEnvCommandDO = new DevopsEnvCommandDO()
            devopsEnvCommandDO.setId(1L)
            devopsEnvCommandMapper.insert(devopsEnvCommandDO)

            DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
            devopsEnvironmentDO.setId(1L)
            devopsEnvironmentDO.setProjectId(1L)
            devopsEnvironmentDO.setActive(true)
            devopsEnvironmentDO.setGitlabEnvProjectId(1L)
            devopsEnvironmentDO.setSequence(1L)
            devopsEnvironmentDO.setCode("env")
            devopsEnvironmentDO.setDevopsEnvGroupId(1L)
            devopsEnvironmentMapper.insert(devopsEnvironmentDO)

            ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()
            applicationInstanceDO.setId(1L)
            applicationInstanceDO.setProjectId(1L)
            applicationInstanceDO.setCode("test")
            applicationInstanceDO.setAppId(1L)
            applicationInstanceDO.setEnvId(1L)
            applicationInstanceDO.setAppVersionId(1L)
            applicationInstanceMapper.insert(applicationInstanceDO)

            devopsEnvFileResourceDO = new DevopsEnvFileResourceDO()
            devopsEnvFileResourceDO.setId(1L)
            devopsEnvFileResourceDO.setEnvId(1L)
            devopsEnvFileResourceDO.setResourceId(1L)
            devopsEnvFileResourceDO.setResourceType("Service")
            devopsEnvFileResourceDO.setFilePath("test-svc.yaml")
            devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO)

            devopsServiceAppInstanceDO = new DevopsServiceAppInstanceDO()
            devopsServiceAppInstanceDO.setId(1L)
            devopsServiceAppInstanceDO.setServiceId(1L)
            devopsServiceAppInstanceDO.setAppInstanceId(1L)
            devopsServiceAppInstanceMapper.insert(devopsServiceAppInstanceDO)

            flag = 1
        }
    }

    def "CheckName"() {
        when:
        def exist = restTemplate.getForObject("/v1/projects/1/service/check?envId=1&name=svc", Boolean.class)

        then:
        exist
    }

    def "Create"() {
        given:
        List<PortMapE> portMapES = new ArrayList<>()
        PortMapE portMapE = new PortMapE()
        portMapE.setPort(7777L)
        portMapE.setNodePort(9999L)
        portMapE.setTargetPort("8888")
        portMapES.add(portMapE)

        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO()
        devopsServiceReqDTO.setPorts()
        devopsServiceReqDTO.setAppId(1L)
        devopsServiceReqDTO.setEnvId(1L)
        devopsServiceReqDTO.setType("ClusterIP")
        devopsServiceReqDTO.setName("svcsvc")
        devopsServiceReqDTO.setPorts(portMapES)
        devopsServiceReqDTO.setExternalIp("1.1.1.1")

        UserAttrE userAttrE = new UserAttrE(1L, 1L)

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setDevopsEnvGroupId(1)

        GitlabMemberE gitlabMemberE = new GitlabMemberE()
        gitlabMemberE.setAccessLevel(AccessLevel.OWNER.toValue())

        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        userAttrRepository.queryById(_ as Long) >> userAttrE
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> gitlabMemberE
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when:
        def result = restTemplate.postForObject("/v1/projects/1/service", devopsServiceReqDTO, Boolean.class)

        then:
        result
    }

    def "Update"() {
        given:
        List<PortMapE> portMapES = new ArrayList<>()
        PortMapE portMapE = new PortMapE()
        portMapE.setPort(7777L)
        portMapE.setNodePort(9999L)
        portMapE.setTargetPort("8888")
        portMapES.add(portMapE)

        List<Long> longList = new ArrayList<>()
        longList.add(1L)
        DevopsServiceReqDTO newDevopsServiceReqDTO = new DevopsServiceReqDTO()
        newDevopsServiceReqDTO.setAppId(1L)
        newDevopsServiceReqDTO.setEnvId(1L)
        newDevopsServiceReqDTO.setType("ClusterIP")
        newDevopsServiceReqDTO.setName("svcsvc")
        newDevopsServiceReqDTO.setAppInstance(longList)
        newDevopsServiceReqDTO.setPorts(portMapES)
        newDevopsServiceReqDTO.setExternalIp("1.2.1.1")

        UserAttrE userAttrE = new UserAttrE(1, 1)

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setDevopsEnvGroupId(1)

        GitlabMemberE gitlabMemberE
        gitlabMemberE = new GitlabMemberE()
        gitlabMemberE.setAccessLevel(AccessLevel.OWNER.toValue())

        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pro")
        projectE.setOrganization(organization)
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        userAttrRepository.queryById(_ as Long) >> userAttrE
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> gitlabMemberE
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        id = devopsServiceRepository.selectByNameAndEnvId("svcsvc", 1L).getId()
        devopsEnvFileResourceDO = devopsEnvFileResourceMapper.selectByPrimaryKey(1L)
        devopsEnvFileResourceDO.setResourceId(id)
        devopsEnvFileResourceMapper.updateByPrimaryKey(devopsEnvFileResourceDO)

        when:
        restTemplate.put("/v1/projects/1/service/{id}", newDevopsServiceReqDTO, id)

        then:
        devopsServiceMapper.selectByPrimaryKey(id).getExternalIp().equals("1.2.1.1")
    }

    def "Delete"() {
        given:
        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        UserAttrE userAttrE = new UserAttrE(1, 1)

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setDevopsEnvGroupId(1)

        GitlabMemberE gitlabMemberE
        gitlabMemberE = new GitlabMemberE()
        gitlabMemberE.setAccessLevel(AccessLevel.OWNER.toValue())
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        userAttrRepository.queryById(_ as Long) >> userAttrE
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> gitlabMemberE
        gitlabRepository.deleteFile(_ as Integer, _ as String, _ as String, _ as Integer) >> null

        when:
        restTemplate.delete("/v1/projects/1/service/{id}", id)

        then:
        devopsEnvCommandRepository.queryByObject("service", id).getCommandType().equals("delete")
    }

    def "PageByOptions"() {
        given:
        String infra = "{\"searchParam\":{\"name\":[\"svcsvc\"]}}"

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        def pages = restTemplate.postForObject("/v1/projects/1/service/list_by_options", strEntity, Page.class)

        then:
        pages.size() == 1
    }

    def "ListByEnvId"() {
        given:
        DevopsServiceDO devopsServiceDO = new DevopsServiceDO()
        devopsServiceDO.setEnvId(1L)
        devopsServiceDO.setAppId(1L)
        devopsServiceDO.setName("svcsvc")
        devopsServiceDO.setStatus("running")
        devopsServiceDO.setPorts("[{\"port\":7777}]")
        devopsServiceDO.setExternalIp("1.1.1.1")
        devopsServiceDO.setType("ClusterIP")
        devopsServiceDO.setCommandId(1L)
        devopsServiceMapper.insert(devopsServiceDO)
        when:
        def list = restTemplate.getForObject("/v1/projects/1/service?envId=1", List.class)

        then:
        list.size() == 1
    }

    def "Query"() {
        when:
        def dto = restTemplate.getForObject("/v1/projects/1/service/{id}", DevopsServiceDTO.class, id)

        then:
        dto != null
    }

    def "ListByEnv"() {
        given:
        String infra = "{\"searchParam\":{\"name\":[\"svc\"]}}"

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        def page = restTemplate.postForObject("/v1/projects/1/service/1/listByEnv", strEntity, Page.class)

        then:
        page.size() == 2
        devopsEnvCommandMapper.deleteByPrimaryKey(1L)
        devopsEnvironmentMapper.deleteByPrimaryKey(1L)
        applicationInstanceMapper.deleteByPrimaryKey(1L)
        devopsEnvFileResourceMapper.deleteByPrimaryKey(1L)
        devopsServiceAppInstanceMapper.deleteByPrimaryKey(1L)
        devopsServiceMapper.deleteByPrimaryKey(id)
        FileUtil.deleteDirectory(new File("gitops"))
    }
}
