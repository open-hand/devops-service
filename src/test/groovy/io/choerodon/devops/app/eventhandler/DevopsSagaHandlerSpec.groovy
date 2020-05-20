package io.choerodon.devops.app.eventhandler

import com.google.gson.Gson
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.api.vo.CommitVO
import io.choerodon.devops.api.vo.PipelineWebHookVO
import io.choerodon.devops.api.vo.PushWebHookVO
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppServicePayload
import io.choerodon.devops.app.eventhandler.payload.DevOpsUserPayload
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload
import io.choerodon.devops.app.service.*
import io.choerodon.devops.app.service.impl.UpdateEnvUserPermissionServiceImpl
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import spock.lang.Shared
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.ArgumentMatchers.anyString

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
    private AppServiceService appServiceService = PowerMockito.mock(AppServiceService)
    private DevopsGitlabPipelineService devopsGitlabPipelineService = PowerMockito.mock(DevopsGitlabPipelineService)

    private AppServiceInstanceService appServiceInstanceService = PowerMockito.mock(AppServiceInstanceService)
    private PipelineTaskRecordService pipelineTaskRecordService = PowerMockito.mock(PipelineTaskRecordService)
    private PipelineStageRecordService pipelineStageRecordService = PowerMockito.mock(PipelineStageRecordService)
    private PipelineService pipelineService = PowerMockito.mock(PipelineService)
    private PipelineRecordService pipelineRecordService = PowerMockito.mock(PipelineRecordService)
    private DevopsServiceService devopsServiceService = PowerMockito.mock(DevopsServiceService)
    private DevopsIngressService devopsIngressService = PowerMockito.mock(DevopsIngressService)

    private UpdateEnvUserPermissionServiceImpl updateUserEnvPermissionService = PowerMockito.mock(UpdateEnvUserPermissionServiceImpl)

    @Shared
    private boolean toSetUp = true
    @Shared
    private boolean toCleanUp = false

    private DevopsSagaHandler devopsSagaHandler = new DevopsSagaHandler()

    def setup() {
        if (toSetUp) {
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "devopsEnvironmentService", devopsEnvironmentService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "devopsGitService", devopsGitService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "appServiceService", appServiceService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "devopsGitlabPipelineService", devopsGitlabPipelineService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "appServiceInstanceService", appServiceInstanceService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "pipelineTaskRecordService", pipelineTaskRecordService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "pipelineStageRecordService", pipelineStageRecordService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "pipelineService", pipelineService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "pipelineRecordService", pipelineRecordService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "devopsServiceService", devopsServiceService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "devopsIngressService", devopsIngressService)
            DependencyInjectUtil.setAttribute(devopsSagaHandler, "updateUserEnvPermissionService", updateUserEnvPermissionService)
        }
    }

    def cleanup() {
        if (toCleanUp) {
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "devopsEnvironmentService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "devopsGitService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "appServiceService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "devopsGitlabPipelineService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "appServiceInstanceService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "pipelineTaskRecordService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "pipelineStageRecordService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "pipelineService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "pipelineRecordService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "devopsServiceService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "devopsIngressService")
            DependencyInjectUtil.restoreDefaultDependency(devopsSagaHandler, "updateUserEnvPermissionService")
        }
    }

    def "DevopsCreateEnv"() {
        given: '初始化GitlabProjectPayload'
        toSetUp = false
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload()
        gitlabProjectPayload.setUserId(1)
        String data = gson.toJson(gitlabProjectPayload)

        and: '构造DevopsEnvironmentDTO'
        DevopsEnvironmentDTO devopsEnvironmentE = new DevopsEnvironmentDTO()
        devopsEnvironmentE.setFailed(true)
        PowerMockito.when(devopsEnvironmentService.baseQueryByClusterIdAndCode(anyLong(), anyString())).thenReturn(devopsEnvironmentE)

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

        and: '构造DevopsEnvironmentDTO'
        DevopsEnvironmentDTO devopsEnvironmentE = new DevopsEnvironmentDTO()
        devopsEnvironmentE.setFailed(true)
        PowerMockito.when(devopsEnvironmentService.baseQueryByClusterIdAndCode(anyLong(), anyString())).thenReturn(devopsEnvironmentE)

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
        DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload()
        devOpsAppServicePayload.setAppId(1L)
        String data = gson.toJson(devOpsAppServicePayload)

        and: 'mock方法调用'
        AppServiceDTO appServiceDTO = new AppServiceDTO()
        appServiceDTO.setFailed(true)
        PowerMockito.when(appServiceService.baseQuery(anyLong())).thenReturn(appServiceDTO)
        PowerMockito.when(appServiceService.baseUpdate(any(AppServiceDTO.class))).thenReturn(appServiceDTO)

        when: '方法调用'
        def str = devopsSagaHandler.createAppService(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"appId\":1}"
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
        DevOpsAppServicePayload devOpsAppPayload = new DevOpsAppServicePayload()
        devOpsAppPayload.setAppId(1L)
        String data = gson.toJson(devOpsAppPayload)

        and: 'mock方法调用'
        AppServiceDTO appServiceDTO = new AppServiceDTO()
        PowerMockito.when(appServiceService.baseQuery(anyLong())).thenReturn(appServiceDTO)
        PowerMockito.when(appServiceService.baseUpdate(any(AppServiceDTO.class))).thenReturn(appServiceDTO)

        when: '方法调用'
        def str = devopsSagaHandler.setAppErr(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"appId\":1}"
    }



    def "GitlabPipeline"() {
        given: '初始化PipelineWebHookDTO'
        PipelineWebHookVO pipelineWebHookDTO = new PipelineWebHookVO()
        String data = gson.toJson(pipelineWebHookDTO)

        when: '方法调用'
        def str = devopsSagaHandler.gitlabPipeline(data)
        toCleanUp = true

        then: '校验方法调用'
        noExceptionThrown()
        str == "{}"
    }
}
