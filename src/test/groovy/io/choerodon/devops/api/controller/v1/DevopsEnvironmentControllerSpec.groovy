package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.exception.CommonException
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsEnviromentDTO
import io.choerodon.devops.api.dto.DevopsEnvironmentUpdateDTO
import io.choerodon.devops.api.dto.EnvSyncStatusDTO
import io.choerodon.devops.app.service.DevopsEnvironmentService
import io.choerodon.devops.domain.application.entity.DevopsServiceE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.mapper.*
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

    private static flag = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsServiceRepository devopsServiceRepository
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService
    @Autowired
    private DevopsEnvCommitMapper devopsEnvCommitMapper
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper

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
        if (flag == 0) {
            Organization organization = initOrg(1L, "testOrganization")
            ProjectE projectE = initProj(1L, "testProject", organization)

            DevopsEnvironmentDO devopsEnvironmentDO = initEnv(
                    "testNameEnv", "testCodeEnv", projectE, true, 1L, "testToken")
            DevopsEnvironmentDO devopsEnvironmentDO1 = initEnv(
                    "testNameEnv1", "testCodeEnv1", projectE, true, 2L, "testToken1")

            devopsEnvironmentDO.setId(1L)
            devopsEnvironmentDO1.setId(2L)
            devopsEnvironmentMapper.insert(devopsEnvironmentDO)
            devopsEnvironmentMapper.insert(devopsEnvironmentDO1)
            flag = 1
        }
    }

    def "Create"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")
        ProjectE projectE = initProj(1L, "testProject", organization)

        DevopsEnviromentDTO devopsEnviromentDTO = new DevopsEnviromentDTO()
        devopsEnviromentDTO.setCode("testCodeChange")
        devopsEnviromentDTO.setName("testNameChange")

        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)

        devopsEnvironmentService.initMockService(sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(null, null)

        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        userAttrRepository.queryById(_ as Long) >> userAttrE
        when:
        restTemplate.postForObject("/v1/projects/1/envs", devopsEnviromentDTO, String.class)

        then:
        devopsEnvironmentRepository.queryByProjectIdAndCode(1L, "testCodeChange") != null
    }

    def "ListByProjectIdDeployed"() {
        given:
        DevopsServiceE devopsServiceE = new DevopsServiceE()
        devopsServiceE.setId(1L)
        devopsServiceE.setEnvId(1L)
        devopsServiceE.setStatus("running")
        DevopsServiceE devopsServiceE1 = new DevopsServiceE()
        devopsServiceE.setId(2L)
        devopsServiceE1.setEnvId(2L)
        devopsServiceE1.setStatus("running")

        devopsServiceRepository.insert(devopsServiceE)
        devopsServiceRepository.insert(devopsServiceE1)

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList
        when:
        def envs = restTemplate.getForObject("/v1/projects/1/envs/deployed", List.class)

        then:

        envs.size() == 2
        devopsServiceRepository.delete(1L)
        devopsServiceRepository.delete(2L)
    }

    def "ListByProjectIdAndActive"() {
        given:

        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList
        when:
        def envs = restTemplate.getForObject("/v1/projects/1/envs?active=true", List.class)

        then:
        envs.size() == 3
    }

    def "ListByProjectIdAndActiveWithGroup"() {
        given:
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        DevopsEnvGroupDO devopsEnvGroupDO = new DevopsEnvGroupDO()
        devopsEnvGroupDO.setId(1L)
        devopsEnvGroupDO.setProjectId(1L)
        DevopsEnvGroupDO devopsEnvGroupDO1 = new DevopsEnvGroupDO()
        devopsEnvGroupDO1.setId(2L)
        devopsEnvGroupDO1.setProjectId(1L)
        devopsEnvGroupMapper.insert(devopsEnvGroupDO)
        devopsEnvGroupMapper.insert(devopsEnvGroupDO1)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        def list = restTemplate.getForObject("/v1/projects/1/envs/groups?active=true", List.class)

        then:
        !list.isEmpty()
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
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        restTemplate.put("/v1/projects/1/envs/3/active?active=false", Boolean.class)

        then:
        devopsEnvironmentMapper.selectByPrimaryKey(3L).getActive() == false
    }

    def "Query"() {

        when:
        DevopsEnvironmentUpdateDTO a = restTemplate.getForObject("/v1/projects/1/envs/1", DevopsEnvironmentUpdateDTO.class)
        then:
        a != null
    }


    def "Update"() {
        given:
        DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO = new DevopsEnvironmentUpdateDTO()
        devopsEnvironmentUpdateDTO.setId(3L)
        devopsEnvironmentUpdateDTO.setName("testNameChange1222")

        when:
        restTemplate.put("/v1/projects/1/envs", devopsEnvironmentUpdateDTO, DevopsEnvironmentUpdateDTO.class)

        then:
        devopsEnvironmentMapper.selectByPrimaryKey(3L).getName().equals("testNameChange1222")
    }

    def "Sort"() {
        given:
        List<Long> envList = new ArrayList<>()
        envList.add(1L)
        envList.add(2L)

        Long[] sequence = [2L, 1L]
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        restTemplate.put("/v1/projects/1/envs/sort", sequence, List.class)

        then:
        devopsEnvironmentMapper.selectByPrimaryKey(1L).getSequence() == 2L
    }

    def "CheckName"() {
        when:
        restTemplate.getForObject("/v1/projects/1/envs/checkName?name=testCheckName", Object.class)

        then:
        notThrown(CommonException)

    }

    def "CheckCode"() {

        when:
        restTemplate.getForObject("/v1/projects/1/envs/checkCode?code=testCheckCode", Object.class)

        then:
        notThrown(CommonException)
    }

    def "ListByProjectId"() {
        given:
        List<Long> envList = new ArrayList<>()
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()
        applicationInstanceDO.setId(1L)
        applicationInstanceDO.setAppId(1L)
        applicationInstanceDO.setEnvId(1L)
        applicationInstanceDO.setEnvCode("env")
        applicationInstanceDO.setCode("instance")
        applicationInstanceDO.setAppVersionId(1L)
        applicationInstanceDO.setStatus("running")
        applicationInstanceDO.setEnvName("env")
        applicationInstanceDO.setAppName("appname")
        applicationInstanceDO.setCommandId(1L)
        applicationInstanceDO.setObjectVersionNumber(1L)
        ApplicationInstanceDO applicationInstanceDO1 = new ApplicationInstanceDO()
        applicationInstanceDO1.setId(2L)
        applicationInstanceDO1.setAppId(2L)
        applicationInstanceDO1.setEnvId(2L)
        applicationInstanceDO1.setEnvCode("env1")
        applicationInstanceDO1.setCode("instance1")
        applicationInstanceDO1.setAppVersionId(1L)
        applicationInstanceDO1.setStatus("running")
        applicationInstanceDO1.setEnvName("env")
        applicationInstanceDO1.setAppName("appname1")
        applicationInstanceDO1.setCommandId(1L)
        applicationInstanceDO1.setObjectVersionNumber(1L)
        applicationInstanceMapper.insert(applicationInstanceDO)
        applicationInstanceMapper.insert(applicationInstanceDO1)
        envList.add(1L)
        envList.add(2L)
        envUtil.getConnectedEnvList(_ as EnvListener) >> envList
        envUtil.getUpdatedEnvList(_ as EnvListener) >> envList

        when:
        def envs = restTemplate.getForObject("/v1/projects/1/envs/instance", List.class)

        then:
        envs.size() == 2
    }

    def "QueryEnvSyncStatus"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")

        ProjectE projectE = initProj(1L, "testProject", organization)


        DevopsEnvCommitDO devopsEnvCommitDO = new DevopsEnvCommitDO()
        devopsEnvCommitDO.setId(1L)
        devopsEnvCommitDO.setCommitSha("testCommitSha")
        devopsEnvCommitMapper.insert(devopsEnvCommitDO)
        devopsEnvironmentMapper.updateDevopsEnvCommit(1L, 1L, 1L, 1L)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when:
        def envSyncStatusDTO = restTemplate.getForObject("/v1/projects/1/envs/1/status", EnvSyncStatusDTO.class)

        then:
        envSyncStatusDTO.getAgentSyncCommit().equals("testCommitSha")

        devopsEnvCommitMapper.deleteByPrimaryKey(1L)
        applicationInstanceMapper.deleteByPrimaryKey(1L)
        applicationInstanceMapper.deleteByPrimaryKey(2L)
        devopsServiceRepository.delete(1L)
        devopsServiceRepository.delete(2L)
        devopsEnvGroupMapper.deleteByPrimaryKey(1L)
        devopsEnvGroupMapper.deleteByPrimaryKey(2L)
        devopsEnvironmentMapper.deleteByPrimaryKey(1L)
        devopsEnvironmentMapper.deleteByPrimaryKey(2L)
        devopsEnvironmentMapper.deleteByPrimaryKey(3L)

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

    private
    static DevopsEnvironmentDO initEnv(String name, String code, ProjectE projectE, Boolean active, Long sequence, String token) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
        devopsEnvironmentDO.setName(name)
        devopsEnvironmentDO.setCode(code)
        devopsEnvironmentDO.setProjectId(projectE.getId())
        devopsEnvironmentDO.setActive(active)
        devopsEnvironmentDO.setSequence(sequence)
        devopsEnvironmentDO.setToken(token)
        devopsEnvironmentDO
    }

}