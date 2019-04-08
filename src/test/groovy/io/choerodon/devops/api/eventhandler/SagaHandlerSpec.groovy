package io.choerodon.devops.api.eventhandler

import com.google.gson.Gson
import io.choerodon.devops.api.dto.GitlabGroupMemberDTO
import io.choerodon.devops.api.dto.GitlabUserDTO
import io.choerodon.devops.api.dto.RegisterOrganizationDTO
import io.choerodon.devops.app.service.*
import io.choerodon.devops.domain.application.event.OrganizationEventPayload
import io.choerodon.devops.domain.application.event.ProjectEvent
import io.choerodon.devops.domain.application.repository.ApplicationRepository
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import spock.lang.Specification

/**
 * Created by n!Ck
 * Date: 18-12-3
 * Time: 下午4:01
 * Description: 
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik)
class SagaHandlerSpec extends Specification {

    private final Gson gson = new Gson()

    private ApplicationService applicationService =  PowerMockito.mock(ApplicationService)
    private GitlabGroupService gitlabGroupService = PowerMockito.mock(GitlabGroupService)
    private HarborService harborService = PowerMockito.mock(HarborService)
    private OrganizationService organizationService = PowerMockito.mock(OrganizationService)
    private GitlabGroupMemberService gitlabGroupMemberService = PowerMockito.mock(GitlabGroupMemberService)
    private GitlabUserService gitlabUserService = PowerMockito.mock(GitlabUserService)
    private ApplicationRepository applicationRepository = PowerMockito.mock(ApplicationRepository)

    private SagaHandler sagaHandler = new SagaHandler(gitlabGroupService, harborService,
            organizationService, gitlabGroupMemberService, gitlabUserService,  applicationService,applicationRepository)




    def "HandleGitlabGroupEvent"() {
        given: '初始化msg'
        ProjectEvent projectEvent = new ProjectEvent()
        String data = gson.toJson(projectEvent)

        when: '方法调用'
        def str = sagaHandler.handleGitlabGroupEvent(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{}"
    }

    def "HandleGitOpsGroupEvent"() {
        given: '初始化msg'
        ProjectEvent projectEvent = new ProjectEvent()
        String data = gson.toJson(projectEvent)

        when: '方法调用'
        def str = sagaHandler.handleGitOpsGroupEvent(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{}"
    }

    def "HandleUpdateGitlabGroupEvent"() {
        given: '初始化msg'
        ProjectEvent projectEvent = new ProjectEvent()
        String data = gson.toJson(projectEvent)

        when: '方法调用'
        def str = sagaHandler.handleUpdateGitlabGroupEvent(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{}"
    }

    def "HandleUpdateGitOpsGroupEvent"() {
        given: '初始化msg'
        ProjectEvent projectEvent = new ProjectEvent()
        String data = gson.toJson(projectEvent)

        when: '方法调用'
        def str = sagaHandler.handleUpdateGitOpsGroupEvent(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{}"
    }

    def "HandleHarborEvent"() {
        given: '初始化msg'
        ProjectEvent projectEvent = new ProjectEvent()
        projectEvent.setProjectId(1L)
        projectEvent.setOrganizationCode("org")
        projectEvent.setProjectCode("pro")
        String data = gson.toJson(projectEvent)

        when: '方法调用'
        def str = sagaHandler.handleHarborEvent(data)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"projectId\":1,\"projectCode\":\"pro\",\"organizationCode\":\"org\"}"
    }

    def "HandleOrganizationCreateEvent"() {
        given: '初始化OrganizationEventPayload'
        OrganizationEventPayload organizationEventPayload = new OrganizationEventPayload()
        String payload = gson.toJson(organizationEventPayload)

        when: '方法调用'
        def str = sagaHandler.handleOrganizationCreateEvent(payload)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{}"
    }

    def "HandleGitlabGroupMemberEvent"() {
        given: '初始化List<GitlabGroupMemberDTO>'
        List<GitlabGroupMemberDTO> list = new ArrayList<>()
        String payload = gson.toJson(list)

        when: '方法调用'
        def returnList = sagaHandler.handleGitlabGroupMemberEvent(payload)

        then: '校验方法调用'
        noExceptionThrown()
        returnList.size() == 0
    }

    def "HandleDeleteMemberRoleEvent"() {
        given: '初始化List<GitlabGroupMemberDTO>'
        List<GitlabGroupMemberDTO> list = new ArrayList<>()
        String payload = gson.toJson(list)

        when: '方法调用'
        def returnList = sagaHandler.handleDeleteMemberRoleEvent(payload)

        then: '校验方法调用'
        noExceptionThrown()
        returnList.size() == 0
    }

    def "HandleCreateUserEvent"() {
        given: '初始化List<GitlabUserDTO>'
        List<GitlabUserDTO> list = new ArrayList<>()
        GitlabUserDTO gitlabUserDTO = new GitlabUserDTO()
        gitlabUserDTO.setId("1")
        gitlabUserDTO.setUsername("userName")
        gitlabUserDTO.setEmail("email")
        gitlabUserDTO.setName("name")
        list.add(gitlabUserDTO)
        String payload = gson.toJson(list)

        when: '方法调用'
        def returnList = sagaHandler.handleCreateUserEvent(payload)

        then: '校验方法调用'
        noExceptionThrown()
        returnList.size() == 1
    }

    def "HandleUpdateUserEvent"() {
        given: '初始化GitlabUserDTO'
        GitlabUserDTO gitlabUserDTO = new GitlabUserDTO()
        gitlabUserDTO.setId("1")
        gitlabUserDTO.setUsername("userName")
        gitlabUserDTO.setEmail("email")
        gitlabUserDTO.setName("name")
        String payload = gson.toJson(gitlabUserDTO)

        when: '方法调用'
        def str = sagaHandler.handleUpdateUserEvent(payload)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"username\":\"userName\",\"email\":\"email\",\"id\":\"1\",\"name\":\"name\"}"
    }

    def "HandleIsEnabledUserEvent"() {
        given: '初始化GitlabUserDTO'
        GitlabUserDTO gitlabUserDTO = new GitlabUserDTO()
        gitlabUserDTO.setId("1")
        String payload = gson.toJson(gitlabUserDTO)

        when: '方法调用'
        def str = sagaHandler.handleIsEnabledUserEvent(payload)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"id\":\"1\"}"
    }

    def "HandleDisEnabledUserEvent"() {
        given: '初始化GitlabUserDTO'
        GitlabUserDTO gitlabUserDTO = new GitlabUserDTO()
        gitlabUserDTO.setId("1")
        String payload = gson.toJson(gitlabUserDTO)

        when: '方法调用'
        def str = sagaHandler.handleDisEnabledUserEvent(payload)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{\"id\":\"1\"}"
    }

    def "RegisterOrganization"() {
        given: '初始化RegisterOrganizationDTO'
        RegisterOrganizationDTO registerOrganizationDTO = new RegisterOrganizationDTO()
        String payload = gson.toJson(registerOrganizationDTO)

        when: '方法调用'
        def str = sagaHandler.registerOrganization(payload)

        then: '校验方法调用'
        noExceptionThrown()
        str == "{}"
    }
}
