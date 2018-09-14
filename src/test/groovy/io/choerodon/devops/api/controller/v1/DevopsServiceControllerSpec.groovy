package io.choerodon.devops.api.controller.v1


import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsServiceDTO
import io.choerodon.devops.api.dto.DevopsServiceReqDTO
import io.choerodon.devops.domain.application.entity.PortMapE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.GitUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.mapper.*
import io.choerodon.mybatis.pagehelper.domain.PageRequest
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

    private ApplicationDO applicationDO
    private DevopsEnvCommandDO devopsEnvCommandDO
    private DevopsEnvFileResourceDO devopsEnvFileResourceDO
    private DevopsServiceAppInstanceDO devopsServiceAppInstanceDO

    def setup() {
        if (flag == 0) {
            devopsEnvCommandDO = new DevopsEnvCommandDO()
            devopsEnvCommandMapper.insert(devopsEnvCommandDO)

            devopsEnvFileResourceDO = new DevopsEnvFileResourceDO()
            devopsEnvFileResourceDO.setEnvId(1L)
            devopsEnvFileResourceDO.setResourceId(1L)
            devopsEnvFileResourceDO.setResourceType("Service")
            devopsEnvFileResourceDO.setFilePath("test-svc.yaml")
            devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO)

            devopsServiceAppInstanceDO = new DevopsServiceAppInstanceDO()
            devopsServiceAppInstanceDO.setServiceId(1L)
            devopsServiceAppInstanceDO.setAppInstanceId(1L)
            devopsServiceAppInstanceMapper.insert(devopsServiceAppInstanceDO)

            flag = 1
        }
    }

    def "CheckName"() {
        when:
        restTemplate.getForObject("/v1/projects/1/service/check?envId=1&name=svc", Boolean.class)

        then:
        true
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
        devopsServiceReqDTO.setName("svc")
        devopsServiceReqDTO.setPorts(portMapES)
        devopsServiceReqDTO.setExternalIp("1.1.1.1")

        UserAttrE userAttrE = new UserAttrE(1L, 1L)

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setEnvGroupId(1)

        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE()
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue())

        Organization organization = new Organization()
        organization.setId(1L)
        organization.setCode("org")

        ProjectE projectE = new ProjectE()
        projectE.setId(1L)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        DevopsProjectDO devopsProjectDO = new DevopsProjectDO()
        devopsProjectDO.setId(1L)
        devopsProjectDO.setEnvGroupId(1)
        devopsProjectDO.setGitlabGroupId(1)
        devopsProjectMapper.insert(devopsProjectDO)

        when:
        restTemplate.postForObject("/v1/projects/1/service", devopsServiceReqDTO, Boolean.class)

        then:
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        userAttrRepository.queryById(_ as Long) >> userAttrE
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> groupMemberE
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
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
        newDevopsServiceReqDTO.setName("svc")
        newDevopsServiceReqDTO.setAppInstance(longList)
        newDevopsServiceReqDTO.setPorts(portMapES)
        newDevopsServiceReqDTO.setExternalIp("1.1.1.1")

        UserAttrE userAttrE = new UserAttrE(1, 1)

        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setEnvGroupId(1)

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

        when:
        restTemplate.put("/v1/projects/1/service/1", newDevopsServiceReqDTO, Boolean.class)

        then:
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        userAttrRepository.queryById(_ as Long) >> userAttrE
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> groupMemberE
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
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
        gitlabGroupE.setEnvGroupId(1)

        GitlabGroupMemberE groupMemberE
        groupMemberE = new GitlabGroupMemberE()
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue())

        when:
        restTemplate.delete("/v1/projects/1/service/1")

        then:
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        userAttrRepository.queryById(_ as Long) >> userAttrE
        devopsProjectRepository.queryDevopsProject(_ as Long) >> gitlabGroupE
        gitlabGroupMemberRepository.getUserMemberByUserId(_ as Integer, _ as Integer) >> groupMemberE
        gitlabRepository.deleteFile(_ as Integer, _ as String, _ as String, _ as Integer) >> null
    }

    def "PageByOptions"() {
        given:
        String infra = "{\"searchParam\":{\"name\":[\"svc\"]}}"
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        when:
        restTemplate.postForObject("/v1/projects/1/service/list_by_options", strEntity, Object.class)

        then:
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList
    }

    def "ListByEnvId"() {
        given:
        DevopsServiceDO devopsServiceDO = new DevopsServiceDO()
        devopsServiceDO.setEnvId(1L)
        devopsServiceDO.setAppId(1L)
        devopsServiceDO.setName("svc")
        devopsServiceDO.setStatus("running")
        devopsServiceDO.setPorts("[{\"port\":7777}]")
        devopsServiceDO.setExternalIp("1.1.1.1")
        devopsServiceDO.setType("ClusterIP")
        devopsServiceDO.setCommandId(1L)
        devopsServiceMapper.insert(devopsServiceDO)
        when:
        def list = restTemplate.getForObject("/v1/projects/1/service?envId=3", List.class)

        then:
        true
    }

    def "Query"() {
        when:
        def dto = restTemplate.getForObject("/v1/projects/1/service/1", DevopsServiceDTO.class)

        then:
        dto != null
    }

    def "ListByEnv"() {
        given:
        String infra = "{\"searchParam\":{\"name\":[\"svc\"]}}"
        PageRequest pageRequest = new PageRequest(1, 20)

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf("application/jsonUTF-8"))
        HttpEntity<String> strEntity = new HttpEntity<String>(infra, headers)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        when:
        restTemplate.postForObject("/v1/projects/1/service/1/listByEnv", strEntity, Object.class)

        then:
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList
    }
}
