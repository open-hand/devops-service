package io.choerodon.devops.app.service

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.CommitDTO
import io.choerodon.devops.api.dto.PushWebHookDTO
import io.choerodon.devops.app.service.impl.DevopsGitServiceImpl
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.service.DeployService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsGitServiceTest extends Specification {


    @Autowired
    private DevopsGitService devopsGitService

    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository

    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    @Autowired
    @Qualifier("mockDevopsGitRepository")
    private DevopsGitRepository devopsGitRepository

    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository

    @Autowired
    private DevopsEnvFileRepository devopsEnvFileRepository

    @Autowired
    private DevopsEnvFileErrorRepository devopsEnvFileErrorRepository

    @Autowired
    @Qualifier("mockDeployService")
    private DeployService deployService

    @Autowired
    @Qualifier("mockApplicationInstanceRepository")
    private ApplicationInstanceRepository applicationInstanceRepository

    @Autowired
    @Qualifier("mockDevopsServiceRepository")
    private DevopsServiceRepository devopsServiceRepository

    @Autowired
    @Qualifier("mockDevopsIngressRepository")
    private DevopsIngressRepository devopsIngressRepository

    @Autowired
    @Qualifier("mockSagaClient")


    def setup() {
        DevopsGitService devopsGitService = new DevopsGitServiceImpl()
        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE()
        devopsEnvironmentE.setToken("123456")
    }

    def "FileResourceSync"() {

    }

    def "FileResourceSyncSaga"() {

        given:
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO();
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setCheckoutSha("123456")
        String token = "123456"
        CommitDTO commitDTO = new CommitDTO()
        commitDTO.setId("123456")
        commitDTO.setTimestamp(new Date())
        List<CommitDTO> commitDTOS = new ArrayList<>()
        commitDTOS.add(commitDTO)

        when:
        devopsGitService.fileResourceSyncSaga(pushWebHookDTO, token)

        then:
        pushWebHookDTO == null

    }
}
