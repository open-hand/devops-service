package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationRepDTO
import io.choerodon.devops.api.dto.ApplicationReqDTO
import io.choerodon.devops.api.dto.ApplicationUpdateDTO
import io.choerodon.devops.api.dto.iam.ProjectWithRoleDTO
import io.choerodon.devops.api.dto.iam.RoleDTO
import io.choerodon.devops.app.service.ApplicationService
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.dataobject.gitlab.MemberDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.*
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.Matchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/3
 * Time: 20:27
 * Description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ApplicationController)
@Stepwise
class ApplicationControllerSpec extends Specification {

    private static final String MAPPING = "/v1/projects/{project_id}/apps"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private ApplicationService applicationService
    @Autowired
    private UserAttrRepository userAttrRepository
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private AppUserPermissionMapper appUserPermissionMapper
    @Autowired
    private ApplicationMarketMapper applicationMarketMapper
    @Autowired
    private DevopsProjectRepository devopsProjectRepository
    @Autowired
    protected DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private ApplicationTemplateMapper applicationTemplateMapper
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository

    @Autowired
    private IamRepository iamRepository
    @Autowired
    private GitlabRepository gitlabRepository
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    SagaClient sagaClient = Mockito.mock(SagaClient.class)
    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    @Shared
    Organization organization = new Organization()
    @Shared
    ProjectE projectE = new ProjectE()
    @Shared
    UserAttrE userAttrE = new UserAttrE()
    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    DevopsAppMarketDO devopsAppMarketDO = new DevopsAppMarketDO()
    @Shared
    DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO()

    def setupSpec() {
        given:
        organization.setId(init_id)
        organization.setCode("org")

        projectE.setId(init_id)
        projectE.setCode("pro")
        projectE.setOrganization(organization)

        userAttrE.setIamUserId(init_id)
        userAttrE.setGitlabUserId(init_id)

        Map<String, Object> params = new HashMap<>()
        params.put("name", [])
        params.put("code", ["app"])
        searchParam.put("searchParam", params)
        searchParam.put("param", "")

        devopsAppMarketDO.setId(1L)
        devopsAppMarketDO.setAppId(2L)
        devopsAppMarketDO.setPublishLevel("pub")
        devopsAppMarketDO.setContributor("con")
        devopsAppMarketDO.setDescription("des")

        devopsEnvPodDO.setId(1L)
        devopsEnvPodDO.setAppInstanceId(1L)
    }

    def setup() {
        iamRepository.initMockIamService(iamServiceClient)
        gitlabRepository.initMockService(gitlabServiceClient)
        gitlabGroupMemberRepository.initMockService(gitlabServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)
        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("testOrganization")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        List<RoleDTO> roleDTOList = new ArrayList<>()
        RoleDTO roleDTO = new RoleDTO()
        roleDTO.setCode("role/project/default/project-owner")
        roleDTOList.add(roleDTO)
        List<ProjectWithRoleDTO> projectWithRoleDTOList = new ArrayList<>()
        ProjectWithRoleDTO projectWithRoleDTO = new ProjectWithRoleDTO()
        projectWithRoleDTO.setName("pro")
        projectWithRoleDTO.setRoles(roleDTOList)
        projectWithRoleDTOList.add(projectWithRoleDTO)
        Page<ProjectWithRoleDTO> projectWithRoleDTOPage = new Page<>()
        projectWithRoleDTOPage.setContent(projectWithRoleDTOList)
        projectWithRoleDTOPage.setTotalPages(2)
        ResponseEntity<Page<ProjectWithRoleDTO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

    }
    // 项目下创建应用
    def "create"() {
        given: '创建issueDTO'
        ApplicationReqDTO applicationDTO = new ApplicationReqDTO()

        and: '赋值'
        applicationDTO.setId(init_id)
        applicationDTO.setName("dtoname")
        applicationDTO.setCode("ddtoapp")
        applicationDTO.setType("normal")
        applicationDTO.setProjectId(project_id)
        applicationDTO.setApplicationTemplateId(init_id)
        applicationDTO.setIsSkipCheckPermission(true)
        List<Long> userList = new ArrayList<>()
        userList.add(2L)
        applicationDTO.setUserIds(userList)

        and: 'mock查询gitlab用户'
        MemberDO memberDO = new MemberDO()
        memberDO.setId(1)
        memberDO.setAccessLevel(AccessLevel.OWNER)
        ResponseEntity<MemberDO> memberDOResponseEntity = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getUserMemberByUserId(anyInt(), anyInt())).thenReturn(memberDOResponseEntity)

        and: 'mock启动sagaClient'
        applicationService.initMockService(sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(anyString(), anyObject())

        when: '创建一个应用'
        def entity = restTemplate.postForEntity(MAPPING, applicationDTO, ApplicationRepDTO.class, project_id)

        then: '校验结果'
        entity.statusCode.is2xxSuccessful()
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(init_id)

        expect: '校验查询结果'
        applicationDO["code"] == "ddtoapp"
    }

    // 项目下查询单个应用信息
    def "queryByAppId"() {
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/{app_id}/detail", ApplicationRepDTO.class, project_id, 1L)

        then: '校验结果'
        entity.getBody()["code"] == "ddtoapp"
    }

    // 项目下更新应用信息
    def "update"() {
        given: '设置applicationUpdateDTO类'
        ApplicationUpdateDTO applicationUpdateDTO = new ApplicationUpdateDTO()
        applicationUpdateDTO.setId(init_id)
        applicationUpdateDTO.setName("updatename")
        applicationUpdateDTO.setIsSkipCheckPermission(true)

        and: 'mock启动sagaClient'
        applicationService.initMockService(sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(anyString(), anyObject())

        when: '以前和现在都跳过权限检查，直接返回true，且该应用下无权限表记录'
        restTemplate.put(MAPPING, applicationUpdateDTO, project_id)
        then: '校验结果'
        List<AppUserPermissionDO> permissionResult = appUserPermissionMapper.selectAll()
        ApplicationDO appResult = applicationMapper.selectByPrimaryKey(1L)
        permissionResult.size() == 0
        appResult.getIsSkipCheckPermission() == true

        when: '以前跳过权限检查，现在不跳过，该应用加入权限表记录'
        applicationUpdateDTO.setIsSkipCheckPermission(false)
        List<Long> userIds = new ArrayList<>()
        userIds.add(2L)
        applicationUpdateDTO.setUserIds(userIds)
        restTemplate.put(MAPPING, applicationUpdateDTO, project_id)
        then: '校验结果'
        List<AppUserPermissionDO> permissionResult1 = appUserPermissionMapper.selectAll()
        ApplicationDO appResult1 = applicationMapper.selectByPrimaryKey(1L)
        permissionResult1.size() == 1
        permissionResult1.get(0).getAppId() == 1L
        appResult1.getIsSkipCheckPermission() == false

        when: '以前不跳过权限检查，现在也不跳过，该应用下有权限记录表'
        applicationUpdateDTO.setIsSkipCheckPermission(false)
        restTemplate.put(MAPPING, applicationUpdateDTO, project_id)
        then: '校验结果'
        List<AppUserPermissionDO> permissionResult2 = appUserPermissionMapper.selectAll()
        ApplicationDO appResult2 = applicationMapper.selectByPrimaryKey(1L)
        permissionResult2.size() == 1
        permissionResult2.get(0).getAppId() == 1L
        appResult2.getIsSkipCheckPermission() == false

        when: '以前不跳过权限检查，现在跳过，该应用下无权限记录表'
        applicationUpdateDTO.setIsSkipCheckPermission(true)
        restTemplate.put(MAPPING, applicationUpdateDTO, project_id)
        then: '校验结果'
        List<AppUserPermissionDO> permissionResult3 = appUserPermissionMapper.selectAll()
        ApplicationDO appResult3 = applicationMapper.selectByPrimaryKey(1L)
        permissionResult3.size() == 0
        appResult3.getIsSkipCheckPermission() == true
    }

    // 停用应用
    def "disableApp"() {
        when:
        restTemplate.put(MAPPING + "/1?active=false", Boolean.class, 1L)

        then: '返回值'
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(init_id)

        expect: '校验是否激活'
        applicationDO["isActive"] == false
    }

    // 启用应用
    def "enableApp"() {
        when:
        restTemplate.put(MAPPING + "/1?active=true", Boolean.class, 1L)

        then: '返回值'
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(init_id)

        expect: '校验是否激活'
        applicationDO["isActive"] == true
    }

    // 删除应用
    def "deleteByAppId"() {
        given: 'mock删除git项目'
        ResponseEntity responseEntity2 = new ResponseEntity(HttpStatus.OK)
        Mockito.when(gitlabServiceClient.deleteProjectByProjectName(anyString(), anyString(), anyInt())).thenReturn(responseEntity2)

        when:
        restTemplate.delete(MAPPING + "/1", 1L)

        then: '校验是否删除'
        applicationMapper.selectAll().isEmpty()

        and: '添加上删除的应用'
        ApplicationDO applicationDO = new ApplicationDO()
        applicationDO.setId(1L)
        applicationDO.setProjectId(1L)
        applicationDO.setName("appName")
        applicationDO.setCode("appCode")
        applicationDO.setActive(true)
        applicationDO.setSynchro(true)
        applicationDO.setType("normal")
        applicationDO.setGitlabProjectId(1)
        applicationDO.setAppTemplateId(1L)
        applicationDO.setIsSkipCheckPermission(true)
        applicationMapper.insert(applicationDO)
    }

    // 项目下分页查询应用
    def "pageByOptions"() {
        when:
        def app = restTemplate.postForObject(MAPPING + "/list_by_options?active=true", searchParam, Page.class, 1L)

        then: '返回值'
        app.size() == 1

        expect: '验证返回值'
        app.getContent().get(0)["code"] == "appCode"
    }

    // 根据环境id分页获取已部署正在运行实例的应用
    def "pageByEnvIdAndStatus"() {
        given: '添加应用运行实例'
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO()
        applicationInstanceDO.setId(init_id)
        applicationInstanceDO.setCode("spock-test")
        applicationInstanceDO.setStatus("running")
        applicationInstanceDO.setAppId(init_id)
        applicationInstanceDO.setAppVersionId(init_id)
        applicationInstanceDO.setEnvId(init_id)
        applicationInstanceDO.setCommandId(init_id)
        applicationInstanceMapper.insert(applicationInstanceDO)

        and: '添加env'
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
        devopsEnvironmentDO.setId(init_id)
        devopsEnvironmentDO.setCode("spock-test")
        devopsEnvironmentDO.setGitlabEnvProjectId(init_id)
        devopsEnvironmentDO.setHookId(init_id)
        devopsEnvironmentDO.setDevopsEnvGroupId(init_id)
        devopsEnvironmentDO.setProjectId(init_id)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)

        and: '初始化appMarket对象'
        applicationMarketMapper.insert(devopsAppMarketDO)

        and: '初始化envPod对象'
        devopsEnvPodMapper.insert(devopsEnvPodDO)

        when:
        def applicationPage = restTemplate.getForObject(MAPPING + "/pages?env_id={env_id}", Page.class, project_id, 1)

        then: '返回值'
        applicationPage.size() == 1

        expect: '验证返回值'
        applicationPage.getContent().get(0)["code"] == "appCode"
    }

    // 根据环境id获取已部署正在运行实例的应用
    def "listByEnvIdAndStatus"() {
        when:
        def applicationList = restTemplate.getForObject(MAPPING + "/options?envId=1&status=running&appId=1", List.class, 1L)

        then: '返回值'
        applicationList.size() == 1

        expect: '验证返回值'
        applicationList.get(0)["code"] == "appCode"
    }

    // 项目下查询所有已经启用的应用
    def "listByActive"() {
        when:
        def applicationList = restTemplate.getForObject(MAPPING, List.class, project_id)

        then: '返回值'
        applicationList.size() == 1

        expect: '验证返回值'
        applicationList.get(0)["code"] == "appCode"
    }

    // 项目下查询所有已经启用的应用
    def "listAll"() {
        when:
        def applicationList = restTemplate.getForObject(MAPPING + "/list_all", List.class, project_id)

        then: '返回值'
        applicationList.size() == 1

        expect: '验证返回值'
        applicationList.get(0)["code"] == "appCode"
    }

    // 创建应用校验名称是否存在
    def "checkName"() {
        when: '创建应用校验名称是否存在'
        def exception = restTemplate.getForEntity(MAPPING + "/check_name?name=testName", ExceptionResponse.class, 1L)

        then: '名字不存在不抛出异常'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }

    // 创建应用校验编码是否存在
    def "checkCode"() {
        when: '创建应用校验编码是否存在'
        def exception = restTemplate.getForEntity(MAPPING + "/check_code?code=testCode", ExceptionResponse.class, 1L)

        then: '编码不存在不抛出异常'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }

    // 查询所有应用模板
    def "listTemplate"() {
        given: '初始化appTemplateDO类'
        ApplicationTemplateDO applicationTemplateDO = new ApplicationTemplateDO()
        applicationTemplateDO.setId(4L)
        applicationTemplateDO.setName("tempname")
        applicationTemplateDO.setCode("tempcode")
        applicationTemplateDO.setOrganizationId(init_id)
        applicationTemplateDO.setDescription("tempdes")
        applicationTemplateDO.setCopyFrom(init_id)
        applicationTemplateDO.setRepoUrl("tempurl")
        applicationTemplateDO.setType(null)
        applicationTemplateDO.setUuid("tempuuid")
        applicationTemplateDO.setSynchro(true)
        applicationTemplateDO.setGitlabProjectId(init_id)
        applicationTemplateMapper.insert(applicationTemplateDO)

        when:
        def templateList = restTemplate.getForObject(MAPPING + "/template", List.class, 1L)

        then: '返回值'
        templateList.size() == 1

        expect: '校验返回值'
        templateList.get(0)["code"] == "tempcode"
    }

    // 项目下查询所有已经启用的且未发布的且有版本的应用
    def "listByActiveAndPubAndVersion"() {
        given: '初始化appVersionDO类'
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
        applicationVersionDO.setId(init_id)
        applicationVersionDO.setVersion("0.1.0")
        applicationVersionDO.setAppId(init_id)
        applicationVersionMapper.insert(applicationVersionDO)

        when:
        def entity = restTemplate.postForObject(MAPPING + "/list_unpublish", searchParam, Page.class, 1L)

        then: '验证返回值'
        entity.get(0)["code"] == "appCode"
    }

    // 项目下分页查询代码仓库
    def "listCodeRepository"() {
        when:
        def entity = restTemplate.postForObject("/v1/projects/{project_id}/apps/list_code_repository", searchParam, Page.class, project_id)

        then:
        entity.get(0)["code"] == "appCode"

    }
    // 清除测试数据
    def "cleanupData"() {
        given:
        // 删除appInstance
        List<ApplicationInstanceDO> list = applicationInstanceMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (ApplicationInstanceDO e : list) {
                applicationInstanceMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDO> list1 = devopsEnvironmentMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvironmentDO e : list1) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除app
        List<ApplicationDO> list2 = applicationMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (ApplicationDO e : list2) {
                applicationMapper.delete(e)
            }
        }
        // 删除appVersion
        List<ApplicationVersionDO> list3 = applicationVersionMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (ApplicationVersionDO e : list3) {
                applicationVersionMapper.delete(e)
            }
        }
        // 删除appMarket
        List<DevopsAppMarketDO> list4 = applicationMarketMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (DevopsAppMarketDO e : list4) {
                applicationMarketMapper.delete(e)
            }
        }
        // 删除appTemplet
        List<ApplicationTemplateDO> list5 = applicationTemplateMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (ApplicationTemplateDO e : list5) {
                if (e.getId() >= 4) {
                    applicationTemplateMapper.delete(e)
                }
            }
        }
        // 删除appUserPermission
        List<AppUserPermissionDO> list6 = appUserPermissionMapper.selectAll()
        if (list6 != null && !list6.isEmpty()) {
            for (AppUserPermissionDO e : list6) {
                appUserPermissionMapper.delete(e)
            }
        }
        // 删除envPod
        List<DevopsEnvPodDO> list7 = devopsEnvPodMapper.selectAll()
        if (list7 != null && !list7.isEmpty()) {
            for (DevopsEnvPodDO e : list7) {
                devopsEnvPodMapper.delete(e)
            }
        }
    }
}
