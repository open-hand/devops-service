package io.choerodon.devops.app.eventhandler

import com.github.pagehelper.PageInfo
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.ApplicationService
import io.choerodon.devops.app.service.GitlabGroupMemberService
import io.choerodon.devops.app.service.GitlabUserService
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsProjectDTO
import io.choerodon.devops.infra.dto.UserAttrDTO
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO
import io.choerodon.devops.infra.dto.gitlab.GitlabUserReqDTO
import io.choerodon.devops.infra.dto.gitlab.GroupDTO
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.mapper.AppServiceMapper
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
    ResponseEntity<GroupDTO> groupDOResponseEntity = null
    @Shared
    ResponseEntity<IamUserDTO> userDOResponseEntity = null

    @Autowired
    private SagaHandler sagaHandler
    @Autowired
    private IamService iamRepository
    @Autowired
    private GitlabServiceClientOperator gitlabRepository
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberRepository
    @Autowired
    private UserAttrMapper userAttrMapper
    @Autowired
    private ApplicationService applicationService
    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private GitlabUserService gitlabUserRepository
    @Autowired
    private DevopsProjectMapper devopsProjectMapper

    BaseServiceClient iamServiceClient = Mockito.mock(BaseServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)
    GitlabServiceClientOperator mockGitlabServiceClientOperator = Mockito.mock(GitlabServiceClientOperator.class)

    def setupSpec() {

    }


    def setup() {

        if (isToInit) {

            DemoEnvSetupSagaHandler.beforeInvoke("admin", 1L, 1L)

            DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", iamServiceClient)
            DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(gitlabGroupMemberRepository, "gitlabServiceClientOperator", mockGitlabServiceClientOperator)
            DependencyInjectUtil.setAttribute(applicationService, "sagaClient", sagaClient)
            DependencyInjectUtil.setAttribute(gitlabProjectRepository, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(gitlabUserRepository, "gitlabServiceClient", gitlabServiceClient)


            ProjectDTO projectDO = new ProjectDTO()
            projectDO.setName("pro")
            projectDO.setOrganizationId(org_id)
            ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
            Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(anyLong())
            OrganizationDTO organizationDO = new OrganizationDTO()
            organizationDO.setId(org_id)
            organizationDO.setCode("testOrganization")
            ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
            Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(anyLong())

            PageInfo<ProjectDTO> projectDOPage = new PageInfo<>()
            List<ProjectDTO> projectDOList = new ArrayList<>()
            projectDOList.add(projectDO)
            projectDOPage.setList(projectDOList)
            ResponseEntity<PageInfo<ProjectDTO>> projectDOPageResponseEntity = new ResponseEntity<>(projectDOPage, HttpStatus.OK)
            Mockito.doReturn(projectDOPageResponseEntity).when(iamServiceClient).queryProjectByOrgId(anyLong(), anyInt(), anyInt(), anyString(), any(String[]))


            GroupDTO groupDO = new GroupDTO()
            groupDO.setName("test")
            groupDO.setId(2)
            groupDOResponseEntity = new ResponseEntity<>(groupDO, HttpStatus.OK)
            Mockito.doReturn(groupDOResponseEntity).when(gitlabServiceClient).createGroup(any(GroupDTO), anyInt())


            MemberDTO memberDO = new MemberDTO()
            memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
            ResponseEntity<MemberDTO> memberDOResponseEntity = new ResponseEntity<>(memberDO, HttpStatus.OK)
            Mockito.doReturn(memberDOResponseEntity).when(gitlabServiceClient).queryGroupMember(anyInt(), anyInt())

            Mockito.doReturn(null).when(gitlabServiceClient).deleteMember(anyInt(), anyInt())


            Mockito.doReturn(memberDOResponseEntity).when(gitlabServiceClient).getProjectMember(anyInt(), anyInt())

            GitlabProjectDTO gitlabProjectDO = new GitlabProjectDTO()
            gitlabProjectDO.setId(1)
            ResponseEntity<GitlabProjectDTO> gitlabProjectDOResponseEntity = new ResponseEntity<>(gitlabProjectDO, HttpStatus.OK)
            Mockito.doReturn(gitlabProjectDOResponseEntity).when(gitlabServiceClient).queryProjectById(anyInt())

            Mockito.doReturn(null).when(gitlabServiceClient).deleteProjectMember(anyInt(), anyInt())

            IamUserDTO userDO = new IamUserDTO()
            userDO.setName("test")
            userDO.setId(1)

            userDOResponseEntity = new ResponseEntity<>(userDO, HttpStatus.OK)
            Mockito.doReturn(userDOResponseEntity).when(gitlabServiceClient).queryUserById(anyInt())


            Mockito.doReturn(userDOResponseEntity).when(gitlabServiceClient).queryUserByUserName(anyString())
            Mockito.doReturn(null).when(gitlabServiceClient).updateGitLabUser(anyInt(), anyInt(), any(GitlabUserReqDTO))
            Mockito.doReturn(null).when(gitlabServiceClient).disableUser(anyInt())
            Mockito.doReturn(null).when(gitlabServiceClient).enableUser(anyInt())
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
        Mockito.doReturn(null).when(gitlabServiceClient).updateGroup(anyInt(), anyInt(), any(GroupDTO))
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
        List<AppServiceDTO> applicationDOS = applicationMapper.selectAll()
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
        List<AppServiceDTO> applicationDOList = applicationMapper.selectAll()
        for (AppServiceDTO application : applicationDOList) {
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
