package io.choerodon.devops.app.eventhandler


import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.ApplicationService
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO
import io.choerodon.devops.infra.dataobject.UserAttrDTO
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO
import io.choerodon.devops.infra.dataobject.gitlab.MemberDO
import io.choerodon.devops.infra.dataobject.gitlab.UserDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.DevopsProjectMapper
import io.choerodon.devops.infra.mapper.UserAttrMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by Sheep on 2019/4/9.
 */


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(SagaHandler)
@Stepwise
class SagaHandlerNewSpec extends Specification {

    @Shared
    private boolean isToInit = true
    @Shared
    Long project_id = 1L
    @Shared
    Long org_id = 1L
    @Shared
    ResponseEntity<GroupDO> groupDOResponseEntity = null
    @Shared
    ResponseEntity<UserDO> userDOResponseEntity = null

    @Autowired
    private SagaHandler sagaHandler
    @Autowired
    private IamRepository iamRepository
    @Autowired
    private GitlabRepository gitlabRepository
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository
    @Autowired
    private UserAttrMapper userAttrMapper
    @Autowired
    private ApplicationService applicationService
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository
    @Autowired
    private GitlabUserRepository gitlabUserRepository
    @Autowired
    private DevopsProjectMapper devopsProjectMapper

    SagaClient sagaClient = Mockito.mock(SagaClient.class)
    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    def setupSpec() {

    }


    def setup() {

        if (isToInit) {

            DemoEnvSetupSagaHandler.beforeInvoke("admin", 1L, 1L)

            DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
            DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(applicationService, "sagaClient", sagaClient)
            DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(gitlabUserRepository, "gitlabServiceClient", gitlabServiceClient)


            ProjectDO projectDO = new ProjectDO()
            projectDO.setName("pro")
            projectDO.setOrganizationId(org_id)
            ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
            Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(anyLong())
            OrganizationDO organizationDO = new OrganizationDO()
            organizationDO.setId(org_id)
            organizationDO.setCode("testOrganization")
            ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
            Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(anyLong())

            Page<ProjectDO> projectDOPage = new Page<>()
            List<ProjectDO> projectDOList = new ArrayList<>()
            projectDOList.add(projectDO)
            projectDOPage.setContent(projectDOList)
            ResponseEntity<Page<ProjectDO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
            Mockito.doReturn(projectDOPageResponseEntity).when(iamServiceClient).queryProjectByOrgId(anyLong(), anyInt(), anyInt(), any(), isNull())


            GroupDO groupDO = new GroupDO()
            groupDO.setName("test")
            groupDO.setId(2)
            groupDOResponseEntity = new ResponseEntity<>(groupDO, HttpStatus.OK)
            Mockito.doReturn(groupDOResponseEntity).when(gitlabServiceClient).createGroup(any(), any())

            Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(anyString(), any())

            MemberDO memberDO = new MemberDO()
            memberDO.setAccessLevel(AccessLevel.OWNER)
            ResponseEntity<MemberDO> memberDOResponseEntity = new ResponseEntity<>(memberDO, HttpStatus.OK)
            Mockito.doReturn(memberDOResponseEntity).when(gitlabServiceClient).getUserMemberByUserId(any(), any())

            Mockito.doReturn(null).when(gitlabServiceClient).deleteMember(any(), any())


            Mockito.doReturn(memberDOResponseEntity).when(gitlabServiceClient).getProjectMember(any(), any())

            GitlabProjectDO gitlabProjectDO = new GitlabProjectDO()
            gitlabProjectDO.setId(1)
            ResponseEntity<GitlabProjectDO> gitlabProjectDOResponseEntity = new ResponseEntity<>(gitlabProjectDO, HttpStatus.OK)
            Mockito.doReturn(gitlabProjectDOResponseEntity).when(gitlabServiceClient).getProjectById(any())

            Mockito.doReturn(null).when(gitlabServiceClient).removeMemberFromProject(any(), any())

            UserDO userDO = new UserDO()
            userDO.setName("test")
            userDO.setId(1)

            userDOResponseEntity = new ResponseEntity<>(userDO, HttpStatus.OK)
            Mockito.doReturn(userDOResponseEntity).when(gitlabServiceClient).queryUserByUserId(any())


            Mockito.doReturn(userDOResponseEntity).when(gitlabServiceClient).queryUserByUserName(any())
            Mockito.doReturn(null).when(gitlabServiceClient).updateGitLabUser(any(), any(), any())
            Mockito.doReturn(null).when(gitlabServiceClient).disEnabledUserByUserId(any())
            Mockito.doReturn(null).when(gitlabServiceClient).enabledUserByUserId(any())
            isToInit = false

        }
    }


    //创建gitlab组
    def "HandleGitlabGroupEvent"() {
        String msg = "{\"projectId\":2,\"projectCode\":\"front-demo\",\"projectName\":\"Choerodon演示\",\"projectCategory\":\"AGILE\",\"organizationCode\":\"org-8xvmnghjll\",\"organizationName\":\"汉得信息\",\"userName\":\"pk1mexhxsa\",\"userId\":1,\"imageUrl\":null,\"roleLabels\":[\"project.deploy.admin\",\"project.owner\",\"project.wiki.admin\",\"project.gitlab.owner\"]}"
        Mockito.doReturn(groupDOResponseEntity).when(gitlabServiceClient).queryGroupByName(anyString(), anyInt())


        when:
        def result = sagaHandler.handleGitlabGroupEvent(msg)
        then:
        result != null
    }

    //创建gitlab环境组
    def "HandleGitOpsGroupEvent"() {
        String msg = "{\"projectId\":2,\"projectCode\":\"front-demo\",\"projectName\":\"Choerodon演示\",\"projectCategory\":\"AGILE\",\"organizationCode\":\"org-8xvmnghjll\",\"organizationName\":\"汉得信息\",\"userName\":\"pk1mexhxsa\",\"userId\":1,\"imageUrl\":null,\"roleLabels\":[\"project.deploy.admin\",\"project.owner\",\"project.wiki.admin\",\"project.gitlab.owner\"]}"
        groupDOResponseEntity.getBody().setId(3)
        Mockito.doReturn(groupDOResponseEntity).when(gitlabServiceClient).queryGroupByName(anyString(), anyInt())
        when:
        def result = sagaHandler.handleGitOpsGroupEvent(msg)

        then:
        result != null
    }

    def "HandleUpdateGitlabGroupEvent"() {
        String msg = "{\"projectId\":2,\"projectCode\":\"front-demo\",\"projectName\":\"Choerodon演示aaaa\",\"projectCategory\":\"AGILE\",\"organizationCode\":\"org-8xvmnghjll\",\"organizationName\":\"汉得信息\",\"userName\":\"pk1mexhxsa\",\"userId\":1,\"imageUrl\":null,\"roleLabels\":[\"project.deploy.admin\",\"project.owner\",\"project.wiki.admin\",\"project.gitlab.owner\"]}"
        Mockito.doReturn(groupDOResponseEntity).when(gitlabServiceClient).queryGroupByName(anyString(), anyInt())
        Mockito.doReturn(null).when(gitlabServiceClient).updateGroup(anyInt(), anyInt(), any())
        when:
        def result = sagaHandler.handleUpdateGitlabGroupEvent(msg)

        then:
        result != null

        when:
        def resultNew = sagaHandler.handleUpdateGitOpsGroupEvent(msg)

        then:
        resultNew != null
    }


    def "HandleHarborEvent"() {
        String msg = "{\"projectId\":2,\"projectCode\":\"front-demo\",\"projectName\":\"Choerodon演示aaaa\",\"projectCategory\":\"AGILE\",\"organizationCode\":\"org-8xvmnghjll\",\"organizationName\":\"汉得信息\",\"userName\":\"pk1mexhxsa\",\"userId\":1,\"imageUrl\":null,\"roleLabels\":[\"project.deploy.admin\",\"project.owner\",\"project.wiki.admin\",\"project.gitlab.owner\"]}"

        when:
        def result = sagaHandler.handleHarborEvent(msg)

        then:
        result != null
    }

    def "HandleOrganizationCreateEvent"() {
        String msg = "{\"id\":81,\"name\":\"CRM全友\",\"code\":\"crm-quanyou\",\"userId\":1,\"imageUrl\":null}"

        when:
        def result = sagaHandler.handleOrganizationCreateEvent(msg)

        then:
        result != null
    }

    def "HandleIamCreateApplication"() {
        String msg = "{\"id\":1254,\"organizationId\":7,\"projectId\":2,\"name\":\"hzero-maven项目模板\",\"code\":\"hzero-maven-archetype\",\"enabled\":true,\"applicationCategory\":\"application\",\"applicationType\":\"normal\",\"appCount\":null,\"objectVersionNumber\":null,\"param\":null,\"from\":\"devops-service\",\"projectName\":null,\"projectCode\":null,\"imageUrl\":null}"

        when:
        def result = sagaHandler.handleIamCreateApplication(msg)

        then:
        result != null

    }

    def "HandleIamUpdateApplication"() {
        String msg = "{\"id\":1254,\"organizationId\":7,\"projectId\":2,\"name\":\"hzero-maven项目模板1\",\"code\":\"hzero-maven-archetype\",\"enabled\":true,\"applicationCategory\":\"application\",\"applicationType\":\"normal\",\"appCount\":null,\"objectVersionNumber\":null,\"param\":null,\"from\":\"devops-service\",\"projectName\":null,\"projectCode\":null,\"imageUrl\":null}"

        when:
        def result = sagaHandler.handleIamUpdateApplication(msg)

        then:
        result != null
    }

    def "HandleIamEnableApplication"() {
        String msg = "{\"id\":1254,\"organizationId\":7,\"projectId\":2,\"name\":\"hzero-maven项目模板1\",\"code\":\"hzero-maven-archetype\",\"enabled\":true,\"applicationCategory\":\"application\",\"applicationType\":\"normal\",\"appCount\":null,\"objectVersionNumber\":null,\"param\":null,\"from\":\"devops-service\",\"projectName\":null,\"projectCode\":null,\"imageUrl\":null}"

        when:
        def result = sagaHandler.handleIamEnableApplication(msg)

        then:
        result != null


    }

    def "HandleIamDisableApplication"() {
        String msg = "{\"id\":1254,\"organizationId\":7,\"projectId\":2,\"name\":\"hzero-maven项目模板1\",\"code\":\"hzero-maven-archetype\",\"enabled\":true,\"applicationCategory\":\"application\",\"applicationType\":\"normal\",\"appCount\":null,\"objectVersionNumber\":null,\"param\":null,\"from\":\"devops-service\",\"projectName\":null,\"projectCode\":null,\"imageUrl\":null}"

        when:
        def result = sagaHandler.handleIamDisableApplication(msg)

        then:
        result != null
    }

    def "HandleGitlabGroupMemberEvent"() {
        List<ApplicationDO> applicationDOS = applicationMapper.selectAll()
        applicationDOS.get(0).setGitlabProjectId(1)
        applicationMapper.updateByPrimaryKey(applicationDOS.get(0))


        when:
        String msg = "[{\"username\":\"3803\",\"resourceId\":2,\"resourceType\":\"project\",\"roleLabels\":[\"project.owner\",\"project.wiki.admin\",\"project.gitlab.developer\"],\"userId\":1,\"uuid\":null}]"
        def result = sagaHandler.handleGitlabGroupMemberEvent(msg)


        then:
        result != null


        when:
        String newMsg = "[{\"username\":\"3803\",\"resourceId\":2,\"resourceType\":\"project\",\"roleLabels\":[\"project.owner\",\"project.wiki.admin\",\"project.gitlab.owner\"],\"userId\":1,\"uuid\":null}]"
        def newResult = sagaHandler.handleGitlabGroupMemberEvent(newMsg)

        then:
        newResult != null
    }

    def "HandleDeleteMemberRoleEvent"() {

        String msg = "[{\"userId\":1,\"username\":\"5563\",\"resourceId\":2,\"resourceType\":\"project\",\"roleLabels\":null,\"uuid\":null}]"
        when:
        def result = sagaHandler.handleDeleteMemberRoleEvent(msg)

        then:
        result != null

    }

    def "HandleCreateUserEvent"() {

        String msg = "[{\"id\":\"14894\",\"name\":\"项目成员B\",\"username\":\"t2q43qe11y\",\"email\":\"t2q43qe11y@demo.com\",\"fromUserId\":0,\"organizationId\":97}]"

        when:
        def result = sagaHandler.handleCreateUserEvent(msg)

        then:
        result != null


    }

    def "HandleUpdateUserEvent"() {

        String msg = "{\"id\":\"14894\",\"name\":\"项目成员B\",\"username\":\"t2q43qe11y\",\"email\":\"t2q43qe11y@demo.com\",\"fromUserId\":0,\"organizationId\":97}"

        when:
        def result = sagaHandler.handleUpdateUserEvent(msg)

        then:
        result != null
    }

    def "HandleIsEnabledUserEvent"() {

        String msg = "{\"id\":\"14894\",\"name\":null,\"username\":\"oco9l377ss\",\"email\":null,\"fromUserId\":null,\"organizationId\":null}"

        when:
        def result = sagaHandler.handleIsEnabledUserEvent(msg)

        then:
        result != null
    }

    def "HandleDisEnabledUserEvent"() {

        String msg = "{\"id\":\"14894\",\"name\":null,\"username\":\"oco9l377ss\",\"email\":null,\"fromUserId\":null,\"organizationId\":null}"

        when:
        def result = sagaHandler.handleDisEnabledUserEvent(msg)

        then:
        result != null
        List<UserAttrDTO> userAttrDOList = userAttrMapper.selectAll()
        for (UserAttrDTO userAttrDO : userAttrDOList) {
            if (userAttrDO.getIamUserId() != 1) {
                userAttrMapper.delete(userAttrDO)
            }
        }
        List<ApplicationDO> applicationDOList = applicationMapper.selectAll()
        for (ApplicationDO application : applicationDOList) {
            applicationMapper.delete(application)
        }
        List<DevopsProjectDTO> devopsProjectDOList = devopsProjectMapper.selectAll()
        for (DevopsProjectDTO devopsProjectDO : devopsProjectDOList) {
            if (devopsProjectDO.getIamProjectId() != 1) {
                devopsProjectMapper.delete(devopsProjectDO)
            }
        }
        DemoEnvSetupSagaHandler.afterInvoke()

    }


}
