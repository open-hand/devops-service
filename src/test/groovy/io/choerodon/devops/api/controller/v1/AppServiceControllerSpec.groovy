package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.asgard.saga.producer.StartSagaBuilder
import io.choerodon.asgard.saga.producer.TransactionalProducer
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.*
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.*
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.iam.IamAppDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
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

import java.util.function.Function

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/3
 * Time: 20:27
 * Description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(AppServiceController)
@Stepwise
class AppServiceControllerSpec extends Specification {

    private static final String MAPPING = "/v1/projects/{project_id}/app_service"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private AppServiceMapper appServiceMapper
    @Autowired
    private AppServiceService appServiceService
    @Autowired
    private UserAttrService userAttributeService
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Autowired
    private AppServiceUserRelMapper appUserPermissionMapper
    @Autowired
    private AppServiceShareRuleMapper applicationMarketMapper
    @Autowired
    private DevopsProjectService devopsProjectService
    @Autowired
    protected DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private AppServiceVersionMapper appServiceVersionMapper
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper
    @Autowired
    private AppServiceInstanceService appServiceInstanceService

    @Autowired
    private IamService iamService
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator
    @Autowired
    private GitlabGroupService gitlabGroupService

    TransactionalProducer producer = Mockito.mock(TransactionalProducer.class)
    BaseServiceClient iamServiceClient = Mockito.mock(BaseServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)
    GitlabServiceClientOperator mockGitlabServiceClientOperator = Mockito.mock(GitlabServiceClientOperator)

    @Shared
    ProjectVO projectVO = new ProjectVO()
    @Shared
    UserAttrVO userAttrE = new UserAttrVO()
    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    AppServiceShareRuleDTO devopsAppMarketDTO = new AppServiceShareRuleDTO()
    @Shared
    DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO()
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false
    @Shared
    Long harborConfigId = 1L
    @Shared
    Long chartConfigId = 2L
    @Shared
    OrganizationDTO organization=new OrganizationDTO()

    def setupSpec() {
        given:
        organization.setId(init_id)
        organization.setCode("org")

        projectVO.setId(init_id)
        projectVO.setCode("pro")
        projectVO.setOrganizationId(organization.getId())

        userAttrE.setIamUserId(init_id)
        userAttrE.setGitlabUserId(init_id)

        Map<String, Object> params = new HashMap<>()
        params.put("name", [])
        params.put("code", ["app"])
        searchParam.put("searchParam", params)
        searchParam.put("param", "")

        devopsAppMarketDTO.setId(1L)
        devopsAppMarketDTO.setAppId(2L)
        devopsAppMarketDTO.setShareLevel("pub")

        devopsEnvPodDO.setId(1L)
        devopsEnvPodDO.setInstanceId(1L)
    }

    def setup() {

        if (isToInit) {
            DependencyInjectUtil.setAttribute(iamService, "baseServiceClient", iamServiceClient)
            DependencyInjectUtil.setAttribute(gitlabServiceClientOperator, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(gitlabGroupService, "gitlabServiceClientOperator", mockGitlabServiceClientOperator)
            DependencyInjectUtil.setAttribute(appServiceService, "sagaClient", sagaClient)

            // 删除app
            appServiceMapper.selectAll().forEach { appServiceMapper.delete(it) }

            ProjectDTO projectDO = new ProjectDTO()
            projectDO.setName("pro")
            projectDO.setOrganizationId(1L)
            ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
            Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)
            OrganizationDTO organizationDTO = new OrganizationDTO()
            organizationDTO.setId(1L)
            organizationDTO.setCode("testOrganization")
            ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
            Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

            List<RoleVO> roleDTOList = new ArrayList<>()
            RoleVO roleDTO = new RoleVO()
            roleDTO.setCode("role/project/default/project-owner")
            roleDTOList.add(roleDTO)
            List<ProjectWithRoleVO> projectWithRoleDTOList = new ArrayList<>()
            ProjectWithRoleVO projectWithRoleDTO = new ProjectWithRoleVO()
            projectWithRoleDTO.setName("pro")
            projectWithRoleDTO.setRoles(roleDTOList)
            projectWithRoleDTOList.add(projectWithRoleDTO)
            PageInfo<ProjectWithRoleVO> projectWithRoleDTOPage = new PageInfo(projectWithRoleDTOList)
            ResponseEntity<PageInfo<ProjectWithRoleVO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
            Mockito.doReturn(pageResponseEntity).when(iamServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())
        }
    }

    def cleanup() {
        if (isToClean) {
            DependencyInjectUtil.restoreDefaultDependency(iamService, "baseServiceClient")
            DependencyInjectUtil.restoreDefaultDependency(gitlabServiceClientOperator, "gitlabServiceClient")
            DependencyInjectUtil.restoreDefaultDependency(gitlabGroupService, "gitlabServiceClientOperator")
            DependencyInjectUtil.restoreDefaultDependency(appServiceService, "sagaClient")

            // 删除appInstance
            appServiceInstanceMapper.selectAll().forEach { appServiceInstanceMapper.delete(it) }
            // 删除env
            devopsEnvironmentMapper.selectAll().forEach { devopsEnvironmentMapper.delete(it) }
            // 删除app
            appServiceMapper.selectAll().forEach { appServiceMapper.delete(it) }
            // 删除appVersion
            appServiceVersionMapper.selectAll().forEach { appServiceVersionMapper.delete(it) }
            // 删除appMarket
            applicationMarketMapper.selectAll().forEach { applicationMarketMapper.delete(it) }
            // 删除appUserPermission
            appUserPermissionMapper.selectAll().forEach { appUserPermissionMapper.delete(it) }
            // 删除envPod
            devopsEnvPodMapper.selectAll().forEach { devopsEnvPodMapper.delete(it) }
        }
    }

    // 项目下创建应用
    def "create"() {
        given: '创建issueDTO'
        isToInit = false
        AppServiceReqVO applicationDTO = new AppServiceReqVO()

        and: '赋值'
        applicationDTO.setId(init_id)
        applicationDTO.setName("appName")
        applicationDTO.setCode("appCode")
        applicationDTO.setType("normal")
        applicationDTO.setProjectId(project_id)
        applicationDTO.setIsSkipCheckPermission(true)
        applicationDTO.setHarborConfigId(harborConfigId)
        applicationDTO.setChartConfigId(chartConfigId)
        List<Long> userList = new ArrayList<>()
        userList.add(2L)
        applicationDTO.setUserIds(userList)

        and: 'mock查询gitlab用户'
        MemberDTO memberDO = new MemberDTO()
        memberDO.setUserId(1)
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
        ResponseEntity<MemberDTO> memberDOResponseEntity = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(memberDOResponseEntity)

        and: 'mock iam创建用户'
        IamAppDTO iamAppDTO = new IamAppDTO()
        iamAppDTO.setProjectId(init_id)
        iamAppDTO.setOrganizationId(init_id)
        ResponseEntity<IamAppDTO> iamAppPayLoadResponseEntity = new ResponseEntity<>(iamAppDTO, HttpStatus.OK)
        Mockito.when(iamServiceClient.createIamApplication(anyLong(), any(IamAppDTO))).thenReturn(iamAppPayLoadResponseEntity)

        and: 'mock启动producer'
        Mockito.doReturn(null).when(producer).applyAndReturn(any(StartSagaBuilder), any(Function))

        when: '创建一个应用'
        def entity = restTemplate.postForEntity(MAPPING, applicationDTO, AppServiceRepVO.class, project_id)

        then: '校验结果'
        entity.statusCode.is2xxSuccessful()
        entity.getBody().getId() == 1L
        AppServiceDTO applicationDO = appServiceMapper.selectByPrimaryKey(init_id)

        expect: '校验查询结果'
        applicationDO["code"] == "appCode"
    }

    // 项目下查询单个应用信息
    def "queryByAppId"() {
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/{app_id}/detail", AppServiceRepVO.class, project_id, 1L)

        then: '校验结果'
        entity.getBody()["code"] == "appCode"
    }

    // 项目下更新应用信息
    def "update"() {
        given: '设置applicationUpdateDTO类'
        AppServiceUpdateDTO appServiceUpdateDTO = new AppServiceUpdateDTO()
        appServiceUpdateDTO.setId(init_id)
        appServiceUpdateDTO.setName("updatename")
        appServiceUpdateDTO.setIsSkipCheckPermission(true)

        and: "初始化gitlab数据"
        AppServiceDTO applicationDO = appServiceMapper.selectByPrimaryKey(init_id)
        applicationDO.setGitlabProjectId(1)
        appServiceMapper.updateByPrimaryKeySelective(applicationDO)

        when: '以前和现在都跳过权限检查，直接返回true，且该应用下无权限表记录'
        restTemplate.put(MAPPING, appServiceUpdateDTO, project_id)
        then: '校验结果'
        List<AppServiceUserRelDTO> permissionResult = appUserPermissionMapper.selectAll()
        AppServiceDTO appResult = appServiceMapper.selectByPrimaryKey(1L)
        permissionResult.size() == 0
        appResult.getIsSkipCheckPermission()

        when: '以前跳过权限检查，现在不跳过，该应用加入权限表记录'
        appServiceUpdateDTO.setIsSkipCheckPermission(false)
        List<Long> userIds = new ArrayList<>()
        userIds.add(2L)
        appServiceUpdateDTO.setUserIds(userIds)
        restTemplate.put(MAPPING, appServiceUpdateDTO, project_id)
        then: '校验结果'
        List<AppServiceUserRelDTO> permissionResult1 = appUserPermissionMapper.selectAll()
        AppServiceDTO appResult1 = appServiceMapper.selectByPrimaryKey(1L)
        permissionResult1.size() == 1
        permissionResult1.get(0).getAppId() == 1L
        !appResult1.getIsSkipCheckPermission()

        when: '以前不跳过权限检查，现在也不跳过，该应用下有权限记录表'
        appServiceUpdateDTO.setIsSkipCheckPermission(false)
        restTemplate.put(MAPPING, appServiceUpdateDTO, project_id)
        then: '校验结果'
        List<AppServiceUserRelDTO> permissionResult2 = appUserPermissionMapper.selectAll()
        AppServiceDTO appResult2 = appServiceMapper.selectByPrimaryKey(1L)
        permissionResult2.size() == 1
        permissionResult2.get(0).getAppId() == 1L
        !appResult2.getIsSkipCheckPermission()

        when: '以前不跳过权限检查，现在跳过，该应用下无权限记录表'
        appServiceUpdateDTO.setIsSkipCheckPermission(true)
        restTemplate.put(MAPPING, appServiceUpdateDTO, project_id)
        then: '校验结果'
        List<AppServiceUserRelDTO> permissionResult3 = appUserPermissionMapper.selectAll()
        AppServiceDTO appResult3 = appServiceMapper.selectByPrimaryKey(1L)
        permissionResult3.size() == 0
        appResult3.getIsSkipCheckPermission()
    }

    // 停用应用
    def "disableApp"() {
        when:
        restTemplate.put(MAPPING + "/{init_id}?active=false", Boolean, 1L, init_id)
        appServiceMapper.selectAll().forEach { println(it.getId() + it.getName() + it.getCode() + it.getActive()) }

        then: '校验是否激活'
        !appServiceMapper.selectByPrimaryKey(init_id).getActive()
    }

    // 启用应用
    def "enableApp"() {
        when:
        restTemplate.put(MAPPING + "/1?active=true", Boolean.class, 1L)

        then: '返回值'
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(init_id)

        expect: '校验是否激活'
        appServiceDTO["isActive"] == true
    }

    // 删除应用
    def "deleteByAppId"() {
        given: 'mock删除git项目'
        ResponseEntity responseEntity2 = new ResponseEntity(HttpStatus.OK)
        Mockito.when(gitlabServiceClient.deleteProjectByName(anyString(), anyString(), anyInt())).thenReturn(responseEntity2)

        when:
        restTemplate.delete(MAPPING + "/1", 1L)

        then: '校验是否删除'
        appServiceMapper.selectAll().isEmpty()

        and: '添加上删除的应用'
        AppServiceDTO appServiceDTO = new AppServiceDTO()
        appServiceDTO.setId(1L)
        appServiceDTO.setProjectId(1L)
        appServiceDTO.setName("appName")
        appServiceDTO.setCode("appCode")
        appServiceDTO.setActive(true)
        appServiceDTO.setSynchro(true)
        appServiceDTO.setType("normal")
        appServiceDTO.setGitlabProjectId(1)
        appServiceDTO.setAppTemplateId(1L)
        appServiceDTO.setIsSkipCheckPermission(true)
        appServiceMapper.insert(appServiceDTO)
    }

    // 项目下分页查询应用
    def "pageByOptions"() {
        when:
        def app = restTemplate.postForObject(MAPPING + "/list_by_options?active=true", searchParam, PageInfo.class, 1L)

        then: '返回值'
        app.getTotal() == 1

        expect: '验证返回值'
        app.getList().get(0)["code"] == "appCode"
    }

    // 根据环境id分页获取已部署正在运行实例的应用
    def "pageByEnvIdAndStatus"() {
        given: '添加应用运行实例'
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO()
        appServiceInstanceDTO.setId(init_id)
        appServiceInstanceDTO.setCode("spock-test")
        appServiceInstanceDTO.setStatus("running")
        appServiceInstanceDTO.setAppId(init_id)
        appServiceInstanceDTO.setAppVersionId(init_id)
        appServiceInstanceDTO.setEnvId(init_id)
        appServiceInstanceDTO.setCommandId(init_id)
        appServiceInstanceMapper.insert(appServiceInstanceDTO)

        and: '添加env'
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()
        devopsEnvironmentDTO.setId(init_id)
        devopsEnvironmentDTO.setCode("spock-test")
        devopsEnvironmentDTO.setGitlabEnvProjectId(init_id)
        devopsEnvironmentDTO.setHookId(init_id)
        devopsEnvironmentDTO.setDevopsEnvGroupId(init_id)
        devopsEnvironmentDTO.setProjectId(init_id)
        devopsEnvironmentMapper.insert(devopsEnvironmentDTO)

        and: '初始化appMarket对象'
        applicationMarketMapper.insert(devopsAppMarketDTO)

        and: '初始化envPod对象'
        devopsEnvPodMapper.insert(devopsEnvPodDO)

        when:
        def applicationPage = restTemplate.getForObject(MAPPING + "/pages?env_id={env_id}", PageInfo.class, project_id, 1)

        then: '返回值'
        applicationPage.getTotal() == 1

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
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO()
        appServiceVersionDTO.setId(init_id)
        appServiceVersionDTO.setVersion("0.1.0")
        appServiceVersionDTO.setAppId(init_id)
        appServiceVersionMapper.insert(appServiceVersionDTO)

        when:
        def entity = restTemplate.postForObject(MAPPING + "/list_unpublish", searchParam, PageInfo.class, 1L)

        then: '验证返回值'
        entity.getList().get(0)["code"] == "appCode"
    }

    // 项目下分页查询代码仓库
    def "listCodeRepository"() {
        when:
        def entity = restTemplate.postForObject("/v1/projects/{project_id}/apps/list_code_repository", searchParam, PageInfo.class, project_id)

        then:
        entity.getList().get(0)["code"] == "appCode"

    }

    // 获取应用下所有用户权限
    def "listAllUserPermission"() {
        when:
        def permissionList = restTemplate.getForObject(MAPPING + "/{appId}/list_all", List.class, 100L, 100L)

        then:
        permissionList.isEmpty()
    }

    // validate repository url and token
    def "validateUrlAndAccessToken"() {
        given: "准备数据"
        def url = MAPPING + "/url_validation?platform_type={platform_type}&access_token={access_token}&url={url}"

        when: "校验github公开仓库ssh协议"
        def result = restTemplate.getForEntity(url, String, 1L, "github", "", "git@github.com:git/git.git")

        then:
        result.getBody() == "false"

        when: "校验github公开仓库"
        result = restTemplate.getForEntity(url, String, 1L, "github", "", "https://github.com/git/git.git")

        then:
        result.getBody() == "true"

        when: "校验gitlab, 无token访问私有库"
        result = restTemplate.getForEntity(url, String, 1L, "gitlab", "", "http://git.staging.saas.test.com/code-x-code-x/code-i.git")

        then:
        result.getBody() == "false"

        when: "校验gitlab, 带token访问私有库"
        result = restTemplate.getForEntity(url, String, 1L, "gitlab", "munijNHhNBEh7BRNhwrV", "http://git.staging.saas.test.com/code-x-code-x/code-i.git")

        then:
        result.getBody() == "true"

        when: "校验gitlab, 带token访问私有空库"
        result = restTemplate.getForEntity(url, String, 1L, "gitlab", "munijNHhNBEh7BRNhwrV", "http://git.staging.saas.test.com/code-x-code-x/test-empty.git")

        then:
        result.getBody() == "null"
    }

    // 项目下导入应用
    def "import Application"() {
        given: '创建issueDTO'
        def url = MAPPING + "/import"
        AppServiceImportVO applicationDTO = new AppServiceImportVO()
        applicationDTO.setName("test-import-github")
        applicationDTO.setCode("test-import-gitlab")
        applicationDTO.setType("normal")
        applicationDTO.setProjectId(project_id)
        applicationDTO.setIsSkipCheckPermission(true)
        applicationDTO.setRepositoryUrl("https://github.com/choerodon/choerodon-microservice-template.git")
        applicationDTO.setPlatformType("github")
        applicationDTO.setHarborConfigId(harborConfigId)
        applicationDTO.setChartConfigId(chartConfigId)

        def searchCondition = new AppServiceDTO()
        searchCondition.setCode(applicationDTO.getCode())

        when: '导入一个github应用'
        def entity = restTemplate.postForEntity(url, applicationDTO, AppServiceRepVO.class, project_id)

        then: '校验结果'
        entity.statusCode.is2xxSuccessful()
        appServiceMapper.selectOne(searchCondition) != null
        appServiceMapper.delete(searchCondition)

        when: '导入一个不可用地址的仓库'
        applicationDTO.setName("test-import-invalid")
        applicationDTO.setCode("test-import-invalid")
        applicationDTO.setRepositoryUrl("https://github.com/choerodon/choerodon-microservice-template.gi")
        entity = restTemplate.postForEntity(url, applicationDTO, ExceptionResponse.class, project_id)

        then: '校验结果'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getCode() == "error.repository.token.invalid"

        when: '导入一个空的仓库'
        applicationDTO.setName("test-import-empty")
        applicationDTO.setCode("test-import-empty")
        applicationDTO.setRepositoryUrl("http://git.staging.saas.test.com/code-x-code-x/test-empty.git")
        applicationDTO.setPlatformType("gitlab")
        applicationDTO.setAccessToken("munijNHhNBEh7BRNhwrV")
        entity = restTemplate.postForEntity(url, applicationDTO, AppServiceRepVO.class, project_id)

        then: '校验结果'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getCode() == "error.repository.empty"

        when: '导入一个gitlab的私有仓库'
        applicationDTO.setName("test-import-gitlab")
        applicationDTO.setCode("test-import-gitlab")
        applicationDTO.setRepositoryUrl("http://git.staging.saas.test.com/code-x-code-x/code-i.git")
        applicationDTO.setPlatformType("gitlab")
        applicationDTO.setAccessToken("munijNHhNBEh7BRNhwrV")
        searchCondition.setCode(applicationDTO.getCode())
        entity = restTemplate.postForEntity(url, applicationDTO, AppServiceRepVO.class, project_id)

        then: '校验结果'
        entity.getStatusCode().is2xxSuccessful()
        appServiceMapper.selectOne(searchCondition) != null
        appServiceMapper.delete(searchCondition)
    }

    // 清除测试数据
    def "cleanupData"() {
        given:
        isToClean = true
    }
}
