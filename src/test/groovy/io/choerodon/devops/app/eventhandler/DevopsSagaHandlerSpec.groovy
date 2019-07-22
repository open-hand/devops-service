package io.choerodon.devops.app.eventhandler

import com.google.gson.Gson
import io.choerodon.devops.api.vo.CommitVO
import io.choerodon.devops.api.vo.PipelineWebHookDTO
import io.choerodon.devops.api.vo.PushWebHookVO
import io.choerodon.devops.app.service.*


import io.choerodon.devops.app.eventhandler.payload.DevOpsAppPayload
import io.choerodon.devops.app.eventhandler.payload.DevOpsUserPayload
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload


import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository

import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import spock.lang.Specification

import static org.mockito.Matchers.*

/**
 * Created by n!Ck
 * Date: 18-12-3
 * Time: 上午11:28
 * Description: 
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik)
class DevopsSagaHandlerSpec extends Specification {

    private Gson gson = new Gson()

    private DevopsEnvironmentService devopsEnvironmentService = PowerMockito.mock(DevopsEnvironmentService)
    private DevopsGitService devopsGitService = PowerMockito.mock(DevopsGitService)
    private ApplicationTemplateService applicationTemplateService = PowerMockito.mock(ApplicationTemplateService)
    private ApplicationService applicationService = PowerMockito.mock(ApplicationService)
    private DevopsGitlabPipelineService devopsGitlabPipelineService = PowerMockito.mock(DevopsGitlabPipelineService)
    private ApplicationRepository applicationRepository = PowerMockito.mock(ApplicationRepository)
    private ApplicationTemplateRepository applicationTemplateRepository = PowerMockito.mock(ApplicationTemplateRepository)
    private DevopsEnvironmentRepository devopsEnvironmentRepository = PowerMockito.mock(DevopsEnvironmentRepository)
    private GitlabRepository gitlabRepository = PowerMockito.mock(GitlabRepository)
    private ApplicationInstanceService applicationInstanceService = PowerMockito.mock(ApplicationInstanceService)


    private DevopsSagaHandler devopsSagaHandler = new DevopsSagaHandler(devopsEnvironmentService,
            devopsGitService, applicationTemplateService, applicationService, devopsGitlabPipelineService, applicationRepository,
            applicationTemplateRepository, devopsEnvironmentRepository,gitlabRepository,applicationInstanceService)

    def "DevopsCreateEnv"() {
        given: '初始化GitlabProjectPayload'
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload()
        gitlabProjectPayload.setUserId(1)
        String data = gson.toJson(gitlabProjectPayload)

        and: '构造DevopsEnvironmentE'
        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE()
        devopsEnvironmentE.setFailed(true)
        PowerMockito.when(devopsEnvironmentRepository.baseQueryByClusterIdAndCode(any(), any())).thenReturn(devopsEnvironmentE)

        when: '方法调用'
        def str = devopsSagaHandler.devopsCreateEnv(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"userId\":1}"
    }

    def "SetEnvErr"() {
        given: '初始化GitlabProjectPayload'
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload()
        gitlabProjectPayload.setUserId(1)
        String data = gson.toJson(gitlabProjectPayload)

        and: '构造DevopsEnvironmentE'
        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE()
        devopsEnvironmentE.setFailed(true)
        PowerMockito.when(devopsEnvironmentRepository.baseQueryByClusterIdAndCode(any(), any())).thenReturn(devopsEnvironmentE)

        when: '方法调用'
        def str = devopsSagaHandler.setEnvErr(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"userId\":1}"
    }

    def "Gitops"() {
        given: '初始化PushWebHookDTO'
        List<CommitVO> commitDTOList = new ArrayList<>()
        PushWebHookVO pushWebHookDTO = new PushWebHookVO()
        pushWebHookDTO.setToken("token")
        pushWebHookDTO.setCommits(commitDTOList)
        String data = gson.toJson(pushWebHookDTO)

        when: '方法调用'
        def str = devopsSagaHandler.gitops(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"commits\":[],\"token\":\"token\"}"
    }

    def "CreateApp"() {
        given: '初始化DevOpsAppPayload'
        DevOpsAppPayload devOpsAppPayload = new DevOpsAppPayload()
        devOpsAppPayload.setAppId(1L)
        devOpsAppPayload.setType("application")
        String data = gson.toJson(devOpsAppPayload)

        and: 'mock方法调用'
        ApplicationE applicationE = new ApplicationE()
        applicationE.setFailed(true)
        PowerMockito.when(applicationRepository.query(any())).thenReturn(applicationE)
        PowerMockito.when(applicationRepository.update(any(ApplicationE.class))).thenReturn(0)

        when: '方法调用'
        def str = devopsSagaHandler.createApp(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"type\":\"application\",\"appId\":1}"
    }

    def "UpdateGitlabUser"() {
        given: '初始化DevOpsUserPayload'
        List<Long> iamUserIds = new ArrayList<>()
        iamUserIds.add(1L)
        DevOpsUserPayload devOpsAppPayload = new DevOpsUserPayload()
        devOpsAppPayload.setIamProjectId(1L)
        devOpsAppPayload.setAppId(1L)
        devOpsAppPayload.setAppId(1L)
        devOpsAppPayload.setIamUserIds(iamUserIds)
        devOpsAppPayload.setOption(1)
        String data = gson.toJson(devOpsAppPayload)

        when: '方法调用'
        devopsSagaHandler.updateGitlabUser(data)

        // 不起容器无法在方法内new出bean对象，会抛出npe
        then: '校验方法调用'
        thrown(NullPointerException)
    }

    def "SetAppErr"() {
        given: '初始化DevOpsAppPayload'
        DevOpsAppPayload devOpsAppPayload = new DevOpsAppPayload()
        devOpsAppPayload.setAppId(1L)
        devOpsAppPayload.setType("application")
        String data = gson.toJson(devOpsAppPayload)

        and: 'mock方法调用'
        ApplicationE applicationE = new ApplicationE()
        PowerMockito.when(applicationRepository.query(any())).thenReturn(applicationE)
        PowerMockito.when(applicationRepository.update(any(ApplicationE.class))).thenReturn(0)

        when: '方法调用'
        def str = devopsSagaHandler.setAppErr(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"type\":\"application\",\"appId\":1}"
    }

    def "SetAppTemplateErr"() {
        given: '初始化DevOpsAppPayload'
        DevOpsAppPayload devOpsAppPayload = new DevOpsAppPayload()
        devOpsAppPayload.setAppId(1L)
        devOpsAppPayload.setType("application")
        String data = gson.toJson(devOpsAppPayload)

        and: 'mock方法调用'
        ApplicationTemplateE applicationTemplateE = new ApplicationTemplateE(1L)
        PowerMockito.when(applicationTemplateRepository.baseQueryByCode(any(), any())).thenReturn(applicationTemplateE)

        when: '方法调用'
        def str = devopsSagaHandler.setAppTemplateErr(data)

        then: '校验返回值'
        noExceptionThrown()
        str == "{\"type\":\"application\",\"appId\":1}"
    }

    def "CreateTemplate"() {
        given: '初始化GitlabProjectPayload'
        GitlabProjectPayload gitlabProjectEventDTO = new GitlabProjectPayload()
        gitlabProjectEventDTO.setType("template")
        gitlabProjectEventDTO.setOrganizationId(1L)
        gitlabProjectEventDTO.setPath("pro")
        String data = gson.toJson(gitlabProjectEventDTO)

        and: 'mock方法调用'
        ApplicationTemplateE applicationTemplateE = new ApplicationTemplateE(1L)
        applicationTemplateE.setFailed(true)
        PowerMockito.when(applicationTemplateRepository.baseQueryByCode(any(), any())).thenReturn(applicationTemplateE)

        when: '方法调用'
        def str = devopsSagaHandler.createTemplate(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"path\":\"pro\",\"type\":\"template\",\"organizationId\":1}"
    }

    def "GitlabPipeline"() {
        given: '初始化PipelineWebHookDTO'
        PipelineWebHookDTO pipelineWebHookDTO = new PipelineWebHookDTO()
        String data = gson.toJson(pipelineWebHookDTO)

        when: '方法调用'
        def str = devopsSagaHandler.gitlabPipeline(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{}"
    }
}
