package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsEnviromentDTO
import io.choerodon.devops.api.dto.DevopsEnvironmentUpdateDTO
import io.choerodon.devops.api.dto.EnvSyncStatusDTO
import io.choerodon.devops.app.service.DevopsEnvironmentService
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.domain.application.entity.DevopsEnvCommitE
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE
import io.choerodon.devops.domain.application.entity.DevopsServiceE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.dataobject.DevopsProjectDO
import io.choerodon.websocket.helper.EnvListener
import org.mockito.Mockito
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
 * Date: 2018/9/4
 * Time: 15:49
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsEnvironmentControllerSpec extends Specification {

    private static boolean initFlag = true

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private DevopsServiceRepository devopsServiceRepository
    @Autowired
    private DevopsProjectRepository devopsProjectRepository
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService
    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil
    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository
    @Autowired
    @Qualifier("mockUserAttrRepository")
    private UserAttrRepository userAttrRepository

    SagaClient sagaClient = Mockito.mock(SagaClient.class)

    def setup() {
        if (initFlag) {
            initFlag = false
            Organization organization = initOrg(1L, "testOrganization")
            ProjectE projectE = initProj(1L, "testProject", organization)

            DevopsEnvironmentE devopsEnvironmentE = initEnv(
                    "testName", "testCode", projectE, true, 1L, "testToken")
            DevopsEnvironmentE devopsEnvironmentE1 = initEnv(
                    "testName1", "testCode1", projectE, true, 2L, "testToken1")

            devopsEnvironmentRepository.create(devopsEnvironmentE)
            devopsEnvironmentRepository.create(devopsEnvironmentE1)
        }
    }

    def "Create"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")
        ProjectE projectE = initProj(1L, "testProject", organization)

        DevopsEnviromentDTO devopsEnviromentDTO = new DevopsEnviromentDTO()
        devopsEnviromentDTO.setCode("testCodeChange")
        devopsEnviromentDTO.setName("testNameChange")

        DevopsProjectDO devopsProjectDO = new DevopsProjectDO()
        devopsProjectDO.setId(1L)
        devopsProjectDO.setGitlabGroupId(1)
        devopsProjectDO.setEnvGroupId(1)
        devopsProjectRepository.createProject(devopsProjectDO)

        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setId(1L)
        userAttrE.setGitlabUserId(1L)

        devopsEnvironmentService.initMockService(sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(null, null)

        when:
        restTemplate.postForObject("/v1/projects/1/envs", devopsEnviromentDTO, String.class)

        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        userAttrRepository.queryById(_ as Long) >> userAttrE
    }

    def "ListByProjectIdDeployed"() {
        given:
        DevopsServiceE devopsServiceE = new DevopsServiceE()
        devopsServiceE.setEnvId(1L)
        devopsServiceE.setStatus("running")
        DevopsServiceE devopsServiceE1 = new DevopsServiceE()
        devopsServiceE1.setEnvId(2L)
        devopsServiceE1.setStatus("running")

        devopsServiceRepository.insert(devopsServiceE)
        devopsServiceRepository.insert(devopsServiceE1)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        when:
        restTemplate.getForObject("/v1/projects/1/envs/deployed", Object.class)

        then:
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList
    }

    def "ListByProjectIdAndActive"() {
        given:
        DevopsServiceE devopsServiceE = new DevopsServiceE()
        devopsServiceE.setEnvId(1L)
        DevopsServiceE devopsServiceE1 = new DevopsServiceE()
        devopsServiceE1.setEnvId(2L)
        devopsServiceRepository.insert(devopsServiceE)
        devopsServiceRepository.insert(devopsServiceE1)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        when:
        restTemplate.getForObject("/v1/projects/1/envs?active=true", Object.class)

        then:
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList
    }

    def "QueryShell"() {
        when:
        String shell = restTemplate.getForObject("/v1/projects/1/envs/1/shell", String.class)
        then:
        !shell.isEmpty()
    }

    def "EnableOrDisableEnv"() {
        given:
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        when:
        restTemplate.put("/v1/projects/1/envs/3/active?active=false", Boolean.class)

        then:
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

    }

    def "Query"() {
        given:
        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE()
        devopsEnvironmentRepository.create(devopsEnvironmentE)

        when:
        DevopsEnvironmentUpdateDTO a = restTemplate.getForObject("/v1/projects/1/envs/1", DevopsEnvironmentUpdateDTO.class)
        then:
        a != null
    }


    def "Update"() {
        given:
        DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO = new DevopsEnvironmentUpdateDTO()
        devopsEnvironmentUpdateDTO.setId(1L)
        devopsEnvironmentUpdateDTO.setName("testNameChange1222")

        when:
        restTemplate.put("/v1/projects/1/envs", devopsEnvironmentUpdateDTO, DevopsEnvironmentUpdateDTO.class)

        then:
        true
    }

    def "Sort"() {
        given:
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        Long[] sequence = [1L, 2L]

        when:
        restTemplate.put("/v1/projects/1/envs/sort", sequence, List.class)

        then:
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList
    }

    def "CheckName"() {
        when:
        restTemplate.getForObject("/v1/projects/1/envs/checkName?name=testCheckName", Object.class)

        then:
        true
    }

    def "CheckCode"() {

        when:
        restTemplate.getForObject("/v1/projects/1/envs/checkCode?code=testCheckCode", Object.class)

        then:
        true
    }

    def "ListByProjectId"() {
        given:
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        when:
        restTemplate.getForObject("/v1/projects/1/envs/instance", List.class)

        then:
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList
    }

    def "QueryEnvSyncStatus"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")

        ProjectE projectE = initProj(1L, "testProject", organization)


        DevopsEnvCommitE devopsEnvCommitE = new DevopsEnvCommitE()
        devopsEnvCommitE.setCommitSha("testCommitSha")
        devopsEnvCommitRepository.create(devopsEnvCommitE)

        when:
        restTemplate.getForObject("/v1/projects/1/envs/1/status", EnvSyncStatusDTO.class)

        then:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
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

    private static DevopsEnvironmentE initEnv(String testName, String testCode, ProjectE projectE, Boolean active, Long sequence, String testToken) {
        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE()
        devopsEnvironmentE.setName(testName)
        devopsEnvironmentE.setCode(testCode)
        devopsEnvironmentE.setProjectE(projectE)
        devopsEnvironmentE.setActive(active)
        devopsEnvironmentE.setSequence(sequence)
        devopsEnvironmentE.setToken(testToken)
        devopsEnvironmentE
    }
}
