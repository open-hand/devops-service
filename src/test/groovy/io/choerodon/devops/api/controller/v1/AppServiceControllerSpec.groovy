package io.choerodon.devops.api.controller.v1

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import java.util.function.Function

import com.github.pagehelper.PageInfo
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
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

import io.choerodon.asgard.saga.producer.StartSagaBuilder
import io.choerodon.asgard.saga.producer.TransactionalProducer
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.*
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleSearchVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.*
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.gitlab.*
import io.choerodon.devops.infra.dto.iam.*
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.mapper.*
import io.choerodon.devops.infra.util.GitUtil

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
    private AppServiceUserRelMapper appServiceUserRelMapper
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper
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
    private GitlabServiceClientOperator gitlabServiceClientOperator
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator
    @Autowired
    private DevopsProjectService projectService
    @Autowired
    AppShareResourceMapper appShareResourceMapper

    TransactionalProducer producer = Mockito.mock(TransactionalProducer.class)
    BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    Long project_id = 1L
    @Shared
    Long app_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false
    @Shared
    Long harborConfigId = 1L
    @Shared
    Long chartConfigId = 2L

    def setupSpec() {
        given:
        Map<String, Object> params = new HashMap<>()
        params.put("code", "app")
        searchParam.put("searchParam", params)
        searchParam.put("param", [])


    }

    def setup() {

        if (isToInit) {
            DependencyInjectUtil.setAttribute(gitlabServiceClientOperator, "gitlabServiceClient", gitlabServiceClient)
            DependencyInjectUtil.setAttribute(baseServiceClientOperator, "baseServiceClient", baseServiceClient)

            ProjectDTO projectDTO = new ProjectDTO()
            projectDTO.setId(1L)
            projectDTO.setName("pro")
            projectDTO.setOrganizationId(1L)
            Mockito.doReturn(new ResponseEntity(projectDTO, HttpStatus.OK)).when(baseServiceClient).queryIamProject(1L)

            OrganizationDTO organizationDTO = new OrganizationDTO()
            organizationDTO.setId(1L)
            organizationDTO.setCode("testOrganization")
            ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
            Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(1L)

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
            Mockito.doReturn(pageResponseEntity).when(baseServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())
        }
    }

    def cleanup() {
        if (isToClean) {
            DependencyInjectUtil.restoreDefaultDependency(gitlabServiceClientOperator, "gitlabServiceClient")
            DependencyInjectUtil.restoreDefaultDependency(baseServiceClientOperator, "baseServiceClient")


            // 删除env
            devopsEnvironmentMapper.selectAll().forEach { devopsEnvironmentMapper.delete(it) }
            // 删除app
            appServiceMapper.selectAll().forEach { appServiceMapper.delete(it) }
            // 删除appVersion
            appServiceVersionMapper.selectAll().forEach { appServiceVersionMapper.delete(it) }
            // 删除envPod
            devopsEnvPodMapper.selectAll().forEach { devopsEnvPodMapper.delete(it) }
            //删除所有project
            devopsProjectMapper.delete(new DevopsProjectDTO())
            //删除所有instance
            appServiceInstanceMapper.delete(new AppServiceInstanceDTO())
            //删除所有environment
            devopsEnvironmentMapper.delete(new DevopsEnvironmentDTO())
            //删除所有serviceShareRule
            appServiceShareRuleMapper.delete(new AppServiceShareRuleDTO())
            //删除所有appServiceUserRel
            appServiceUserRelMapper.delete(new AppServiceUserRelDTO())
        }
    }

    // 项目下创建应用
    def "create"() {
        given: '创建issueDTO'
        AppServiceReqVO appServiceReqVO = new AppServiceReqVO()

        and: '插入一条DevopsProjectDTO数据'
        DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO()
        devopsProjectDTO.setAppId(app_id)
        devopsProjectDTO.setIamProjectId(project_id)
        devopsProjectDTO.setDevopsAppGroupId(1)
        devopsProjectDTO.setDevopsEnvGroupId(1)
        devopsProjectMapper.insert(devopsProjectDTO)

        and: '赋值'
        appServiceReqVO.setId(init_id)
        appServiceReqVO.setName("appName")
        appServiceReqVO.setCode("appCode")
        appServiceReqVO.setType("normal")
        appServiceReqVO.setProjectId(project_id)
        appServiceReqVO.setHarborConfigId(harborConfigId)
        appServiceReqVO.setChartConfigId(chartConfigId)

        and: 'mock GitlabProjectDTO'
        GitlabProjectDTO gitlabProjectDTO = new GitlabProjectDTO()
        gitlabProjectDTO.setId(1)
        ResponseEntity responseEntity = new ResponseEntity(gitlabProjectDTO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryProjectByName(any(), any(), any())).thenReturn(responseEntity)

        and: 'mock VariableDTO'
        VariableDTO variableDTO = new VariableDTO()
        variableDTO.setKey("key")
        variableDTO.setValue("value")
        List<VariableDTO> variableDTOList = new ArrayList<>()
        variableDTOList.add(variableDTO)
        ResponseEntity variableResponseEntity = new ResponseEntity(variableDTOList, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.listVariable(anyInt(), anyInt())).thenReturn(variableResponseEntity)

        and: 'mock ProjectHookDTO'
        ProjectHookDTO projectHookDTO = new ProjectHookDTO()
        projectHookDTO.setId(1)
        List<ProjectHookDTO> projectHookDTOList = new ArrayList<>()
        projectHookDTOList.add(projectHookDTO)
        ResponseEntity listProjectHookResponseEntity = new ResponseEntity(projectHookDTOList, HttpStatus.OK)
        ResponseEntity projectHookResponseEntity = new ResponseEntity(projectHookDTO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.listProjectHook(anyInt(), anyInt())).thenReturn(listProjectHookResponseEntity)
        Mockito.when(gitlabServiceClient.createProjectHook(anyInt(), anyInt(), any())).thenReturn(projectHookResponseEntity)

        and: 'mock RoleVO'
        RoleVO roleVO = new RoleVO()
        roleVO.setId(1)
        roleVO.setCode("member")
        roleVO.setName("appName")
        List<RoleVO> roleVOList = new ArrayList<>()
        roleVOList.add(roleVO)
        PageInfo rolePageInfo = new PageInfo()
        rolePageInfo.setList(roleVOList)
        ResponseEntity roleResponseEntity = new ResponseEntity(rolePageInfo, HttpStatus.OK)
        Mockito.when(baseServiceClient.queryRoleIdByCode(any())).thenReturn(roleResponseEntity)

        and: 'mock IamUserDTO'
        IamUserDTO iamUserDTOMember = new IamUserDTO()
        iamUserDTOMember.setId(1)
        iamUserDTOMember.setOrganizationId(init_id)

        IamUserDTO iamUserDTOOwner = new IamUserDTO()
        iamUserDTOOwner.setId(2)
        iamUserDTOOwner.setOrganizationId(init_id)

        and: 'Mock 所有成员responseEntity'
        List<IamUserDTO> iamUserDTOMemberArrayList = new ArrayList<>()
        iamUserDTOMemberArrayList.add(iamUserDTOMember)
        iamUserDTOMemberArrayList.add(iamUserDTOOwner)
        PageInfo iamUserMemberPageInfo = new PageInfo()
        iamUserMemberPageInfo.setList(iamUserDTOMemberArrayList)
        ResponseEntity iamUserMemberResponseEntity = new ResponseEntity(iamUserMemberPageInfo, HttpStatus.OK)
        Mockito.when(baseServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), eq(1L), anyLong(), anyBoolean(), any())).thenReturn(iamUserMemberResponseEntity)

        and: 'Mock OwnerResponseEntity'
        List<IamUserDTO> iamUserDTOOwnerArrayList = new ArrayList<>()
        iamUserDTOOwnerArrayList.add(iamUserDTOOwner)
        PageInfo iamUserOwnerPageInfo = new PageInfo()
        iamUserOwnerPageInfo.setList(iamUserDTOOwnerArrayList)
        ResponseEntity iamUserOwnerResponseEntity = new ResponseEntity(iamUserOwnerPageInfo, HttpStatus.OK)
        Mockito.when(baseServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), eq(2L), anyLong(), anyBoolean(), any())).thenReturn(iamUserOwnerResponseEntity)


        and: 'mock查询gitlab用户'
        MemberDTO memberDO = new MemberDTO()
        memberDO.setId(1)
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
        ResponseEntity<MemberDTO> memberDOResponseEntity = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(memberDOResponseEntity)

        and: 'mock iam创建用户'
        IamAppDTO iamAppDTO = new IamAppDTO()
        iamAppDTO.setProjectId(init_id)
        iamAppDTO.setOrganizationId(init_id)
        ResponseEntity<IamAppDTO> iamAppPayLoadResponseEntity = new ResponseEntity<>(iamAppDTO, HttpStatus.OK)
        Mockito.when(baseServiceClient.createIamApplication(anyLong(), any(IamAppDTO))).thenReturn(iamAppPayLoadResponseEntity)

        and: 'mock启动producer'
        Mockito.doReturn(null).when(producer).applyAndReturn(any(StartSagaBuilder), any(Function))

        when: '创建一个应用'
        def entity = restTemplate.postForEntity(MAPPING, appServiceReqVO, AppServiceRepVO.class, project_id)

        then: '校验结果'
        entity.statusCode.is2xxSuccessful()
        entity.getBody().getId() == 1L
        AppServiceDTO applicationDO = appServiceMapper.selectByPrimaryKey(init_id)

        expect: '校验查询结果'
        applicationDO["code"] == "appCode"
    }

    // 项目下查询单个应用信息
    def "queryByAppId"() {
        given:
        IamUserDTO iamUserDTO = new IamUserDTO()
        iamUserDTO.setId(1)
        iamUserDTO.setOrganizationId(1)
        List<IamUserDTO> iamUserDTOList = new ArrayList<>()
        iamUserDTOList.add(iamUserDTO)
        ResponseEntity responseEntity = new ResponseEntity(iamUserDTOList, HttpStatus.OK)
        Mockito.when(baseServiceClient.listUsersByIds(any())).thenReturn(responseEntity)

        when: '查询应用信息'
        def entity = restTemplate.getForEntity(MAPPING + "/{app_service_id}", AppServiceRepVO.class, project_id, 1L)

        then: '校验结果'
        entity.getBody()["code"] == "appCode"
    }

    // 项目下更新应用信息
    def "update"() {
        given: '设置applicationUpdateDTO类'
        AppServiceUpdateDTO appServiceUpdateDTO = new AppServiceUpdateDTO()
        appServiceUpdateDTO.setId(init_id)
        appServiceUpdateDTO.setName("updatename")

        and: "初始化gitlab数据"
        AppServiceDTO applicationDO = appServiceMapper.selectByPrimaryKey(init_id)
        applicationDO.setGitlabProjectId(1)
        appServiceMapper.updateByPrimaryKeySelective(applicationDO)

        when: '更新应用信息'
        restTemplate.put(MAPPING, appServiceUpdateDTO, project_id)
        then: '校验结果'
        AppServiceDTO appServiceDTO = new AppServiceDTO()
        appServiceDTO.setId(init_id)
        appServiceDTO.setName("updatename")
        appServiceMapper.selectOne(appServiceDTO) != null
    }

    // 停用应用
    def "disableApp"() {
        when: '发起停用请求'
        restTemplate.put(MAPPING + "/{init_id}?active=false", Boolean.class, 1L, init_id)

        then: '校验是否激活'
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(init_id)
        !appServiceDTO.active
    }

    // 启用应用
    def "enableApp"() {
        when: '发起启用请求'
        restTemplate.put(MAPPING + "/{init_id}?active=true", Boolean.class, 1L, init_id)

        then: '返回值'
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(init_id)
        appServiceDTO.active
    }

    // 删除应用
    def "deleteByAppId"() {
        given: 'mock删除git项目'
        ResponseEntity responseEntity1 = new ResponseEntity(HttpStatus.OK)
        GitlabProjectDTO gitlabProjectDTO = new GitlabProjectDTO()
        gitlabProjectDTO.setId(1)
        ResponseEntity responseEntity2 = new ResponseEntity(gitlabProjectDTO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.deleteProjectByName(anyString(), anyString(), anyInt())).thenReturn(responseEntity1)
        Mockito.when(gitlabServiceClient.deleteProjectById(anyInt(), anyInt())).thenReturn(null)
        Mockito.when(gitlabServiceClient.queryProjectById(1)).thenReturn(responseEntity2)

        when: '删除一个应用'
        restTemplate.delete(MAPPING + "/{app_service_id}", 1L, init_id)

        then: '校验是否删除'
        appServiceMapper.selectAll().isEmpty()

        and: '添加上删除的应用'
        AppServiceDTO appServiceDTO = new AppServiceDTO()
        appServiceDTO.setProjectId(init_id)
        appServiceDTO.setId(init_id)
        appServiceDTO.setName("appName")
        appServiceDTO.setCode("appCode")
        appServiceDTO.setType('normal')
        appServiceDTO.setActive(true)
        appServiceDTO.setSynchro(true)
        appServiceDTO.setGitlabProjectId(1)
        appServiceDTO.setIsSkipCheckPermission(true)
        appServiceMapper.insert(appServiceDTO)
    }

    // 项目下分页查询应用
    def "pageByOptions"() {
        given:
        AppServiceDTO appServiceDTO = new AppServiceDTO()
        appServiceDTO.setProjectId(1)
        println appServiceMapper.selectOne(appServiceDTO)

        IamUserDTO iamUserDTO = new IamUserDTO()
        iamUserDTO.setId(1)
        iamUserDTO.setOrganizationId(1)
        List<IamUserDTO> iamUserDTOList = new ArrayList<>()
        iamUserDTOList.add(iamUserDTO)
        ResponseEntity responseEntity = new ResponseEntity(iamUserDTOList, HttpStatus.OK)
        Mockito.when(baseServiceClient.listUsersByIds(any())).thenReturn(responseEntity)
        when: '查询应用'
        def page = restTemplate.postForObject(MAPPING + "/page_by_options?page=1&size=1", searchParam, PageInfo.class, 1L)

        then: '返回值'
        page.getTotal() == 1

        expect: '验证返回值'
        page.getList().get(0)["code"] == "appCode"
    }

    // 根据环境id分页获取已部署正在运行实例的应用
    def "pageByEnvIdAndStatus"() {
        given: '添加应用运行实例'
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO()
        appServiceInstanceDTO.setId(init_id)
        appServiceInstanceDTO.setCode("spock-test")
        appServiceInstanceDTO.setStatus("running")
        appServiceInstanceDTO.setAppServiceId(init_id)
        appServiceInstanceDTO.setAppServiceVersionId(init_id)
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

        and: '插入AppServiceShareRuleDTO类'
        AppServiceShareRuleDTO appServiceShareRuleDTO = new AppServiceShareRuleDTO()
        appServiceShareRuleDTO.setAppServiceId(init_id)
        appServiceShareRuleDTO.setId(init_id)
        appServiceShareRuleDTO.setVersion("0.1.0")
        appServiceShareRuleMapper.insert(appServiceShareRuleDTO)

        and: '初始化envPod对象'
        DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO()
        devopsEnvPodDO.setId(1L)
        devopsEnvPodDO.setInstanceId(1L)
        devopsEnvPodMapper.insert(devopsEnvPodDO)

        when: '查询运行实例的应用'
        def applicationPage = restTemplate.getForObject(MAPPING + "/page_by_ids?env_id={env_id}", PageInfo.class, project_id, 1)

        then: '返回值'
        applicationPage.getTotal() == 1

        expect: '验证返回值'
        applicationPage.getList().get(0)["code"] == "appCode"

        appServiceShareRuleMapper.delete(appServiceShareRuleDTO)
    }

    // 根据环境id获取已部署正在运行实例的应用
    def "listByEnvIdAndStatus"() {
        when:
        def applicationList = restTemplate.getForObject(MAPPING + "/list_by_env?envId=1&status=running&appServiceId=1", List.class, 1L)

        then: '返回值'
        applicationList.size() == 1

        expect: '验证返回值'
        applicationList.get(0)["code"] == "appCode"
    }

    // 项目下查询所有已经启用的应用
    def "listByActive"() {
        given:
        IamUserDTO iamUserDTO = new IamUserDTO()
        iamUserDTO.setId(1)
        iamUserDTO.setOrganizationId(1)
        List<IamUserDTO> iamUserDTOList = new ArrayList<>()
        iamUserDTOList.add(iamUserDTO)
        ResponseEntity responseEntity = new ResponseEntity(iamUserDTOList, HttpStatus.OK)
        Mockito.when(baseServiceClient.listUsersByIds(any())).thenReturn(responseEntity)

        when: '查询启用的应用'
        def applicationList = restTemplate.getForObject(MAPPING + "/list_by_active", List.class, project_id)

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

        when: '创建应用校验名称是否存在'
        exception = restTemplate.getForEntity(MAPPING + "/check_name?name=appName", ExceptionResponse.class, 1L)
        then: '名字存在抛出异常'
        exception.statusCode.is2xxSuccessful()
        exception.getBody().getCode() == "error.name.exist"
    }

    // 创建应用校验编码是否存在
    def "checkCode"() {
        when: '创建应用校验编码是否存在'
        def exception = restTemplate.getForEntity(MAPPING + "/check_code?code=testCode", ExceptionResponse.class, 1L)

        then: '编码不存在不抛出异常'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }


    //项目下查询所有已经启用的且未发布的且有版本的应用
    def "pageByActiveAndPubAndVersion"() {
        given: '初始化appVersionDO类'
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO()
        appServiceVersionDTO.setId(init_id)
        appServiceVersionDTO.setVersion("0.1.0")
        appServiceVersionDTO.setAppServiceId(init_id)
        appServiceVersionMapper.insert(appServiceVersionDTO)
        println appServiceVersionMapper.select(appServiceVersionDTO)

        when: '查询启用未发版的应用'
        def entity = restTemplate.postForObject(MAPPING + "/page_unPublish", searchParam, PageInfo.class, 1L)

        then: '验证返回值'
        entity.getList().get(0)["code"] == "appCode"
    }

    // 项目下分页查询代码仓库
    def "listCodeRepository"() {
        when: '查询代码仓库'
        def entity = restTemplate.postForObject(MAPPING + "/page_code_repository?page=1&size=1", searchParam, PageInfo.class, project_id)

        then: '检验结果'
        entity.getList().get(0)["code"] == "appCode"

    }

    // 获取应用下所有用户权限
    def "listAllUserPermission"() {
        when: '获取所有用户权限'
        def permissionList = restTemplate.getForObject(MAPPING + "/{appId}/list_all", List.class, 100L, 100L)

        then: '返回的结果不为空'
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
        result = restTemplate.getForEntity(url, String, 1L, "gitlab", "", "https://code.choerodon.com.cn/choerodon-trainning/electricight.git")

        then:
        result.getBody() == "false"

        when: "校验gitlab, 带token访问私有库"
        result = restTemplate.getForEntity(url, String, 1L, "gitlab", "hBxj1US4nUqgjM239zM7", "https://code.choerodon.com.cn/choerodon-trainning/electricight.git")

        then:
        result.getBody() == "true"

        when: "校验gitlab, 带token访问私有空库"
        result = restTemplate.getForEntity(url, String, 1L, "gitlab", "hBxj1US4nUqgjM239zM7", "https://code.choerodon.com.cn/choerodon-trainning/test-empty-repository.git")

        then:
        result.getBody() == "null"
    }

    //项目下导入外部应用
    def "import external Application"() {
        given: '创建issueDTO'
        def url = MAPPING + "/import/external"
        AppServiceImportVO applicationDTO = new AppServiceImportVO()
        applicationDTO.setName("test-import-github")
        applicationDTO.setCode("test-import-gitlab")
        applicationDTO.setType("normal")
        applicationDTO.setProjectId(project_id)
        applicationDTO.setRepositoryUrl("https://github.com/choerodon/choerodon-microservice-template.git")
        applicationDTO.setPlatformType("github")
        applicationDTO.setHarborConfigId(harborConfigId)
        applicationDTO.setChartConfigId(chartConfigId)

        def searchCondition = new AppServiceDTO()
        searchCondition.setCode(applicationDTO.getCode())

        and: 'mock查询gitlab用户'
        MemberDTO memberDO = new MemberDTO()
        memberDO.setId(1)
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
        ResponseEntity<MemberDTO> memberDOResponseEntity = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(memberDOResponseEntity)

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
        applicationDTO.setRepositoryUrl("https://code.choerodon.com.cn/choerodon-trainning/test-empty-repository.git")
        applicationDTO.setPlatformType("gitlab")
        applicationDTO.setAccessToken("hBxj1US4nUqgjM239zM7")
        entity = restTemplate.postForEntity(url, applicationDTO, AppServiceRepVO.class, project_id)

        then: '校验结果'
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody().getCode() == "error.repository.empty"

        when: '导入一个gitlab的私有仓库'
        applicationDTO.setName("test-import-gitlab")
        applicationDTO.setCode("test-import-gitlab")
        applicationDTO.setRepositoryUrl("https://code.choerodon.com.cn/choerodon-trainning/electricight.git")
        applicationDTO.setPlatformType("gitlab")
        applicationDTO.setAccessToken("hBxj1US4nUqgjM239zM7")
        searchCondition.setCode(applicationDTO.getCode())
        entity = restTemplate.postForEntity(url, applicationDTO, AppServiceRepVO.class, project_id)

        then: '校验结果'
        entity.getStatusCode().is2xxSuccessful()
        appServiceMapper.selectOne(searchCondition) != null
        appServiceMapper.delete(searchCondition)
    }

    // 项目下导入内部应用
    def "import internal Application"() {
        given: '创建issueDTO'
        def url = MAPPING + "/import/internal"
        ApplicationImportInternalVO applicationImportInternalVO = new ApplicationImportInternalVO()
        applicationImportInternalVO.setAppName("test-app")
        applicationImportInternalVO.setAppCode("appInternal")
        applicationImportInternalVO.setType("normal")
        applicationImportInternalVO.setVersionId(1L)
        applicationImportInternalVO.setAppServiceId(1L)
        List<ApplicationImportInternalVO> applicationImportExternalVOList = new ArrayList<>()
        applicationImportExternalVOList.add(applicationImportInternalVO)

        and: 'mock 查询GitlabUserDTO'
        GitLabUserDTO gitLabUserDTO = new GitLabUserDTO()
        gitLabUserDTO.setName("gitlib-user")
        gitLabUserDTO.setId(1)
        Mockito.when(gitlabServiceClient.queryUserById(anyInt())).thenReturn(new ResponseEntity<GitLabUserDTO>(gitLabUserDTO, HttpStatus.OK))

        and: 'mock查询gitlabMember'
        MemberDTO memberDO = new MemberDTO()
        memberDO.setId(1)
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
        ResponseEntity<MemberDTO> memberDOResponseEntity = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupMember(anyInt(), anyInt())).thenReturn(memberDOResponseEntity)

        and: 'mock gitlabProjectDTO'
        GitlabProjectDTO gitlabProjectDTO = new GitlabProjectDTO()
        gitlabProjectDTO.setId(1)
        ResponseEntity gitlabProjectDTOResponseEntity = new ResponseEntity(gitlabProjectDTO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryProjectById(1)).thenReturn(gitlabProjectDTOResponseEntity)
        Mockito.when(gitlabServiceClient.createProject(anyInt(), anyString(), anyInt(), anyBoolean())).thenReturn(gitlabProjectDTOResponseEntity)

        and: 'mock ImpersonationTokenDTO'
        ImpersonationTokenDTO impersonationTokenDTO = new ImpersonationTokenDTO()
        impersonationTokenDTO.setId(1)
        impersonationTokenDTO.setName("token")
        impersonationTokenDTO.setToken("sadfasdjfklsd")
        ResponseEntity impersonationTokenResponseEntity = new ResponseEntity(impersonationTokenDTO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createProjectToken(anyInt())).thenReturn(impersonationTokenResponseEntity)
        Mockito.when(gitlabServiceClient.getAdminToken()).thenReturn(new ResponseEntity<String>("sadfasdjfklsd", HttpStatus.OK))

        and: 'mock git'
        def gitUtil = Mockito.mock(GitUtil.class)
        DependencyInjectUtil.setAttribute(appServiceService, "gitUtil", gitUtil)
        Git git = new Git(new FileRepository(new File("/")))

        Mockito.when(gitUtil.cloneAppMarket(anyString(), anyString(), anyString(), anyString())).thenReturn(git)

        Mockito.doAnswer(new Answer<Object>() {
            Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments()
                return "called with arguments: " + args
            }
        }).when(gitUtil).push(any(), anyString(), anyString(), anyString(), anyString())

        when: '导入应用'
        def entity = restTemplate.postForEntity(url, applicationImportExternalVOList, Object, project_id)

        then: '校验结果'
        entity.statusCode.is2xxSuccessful()

        DependencyInjectUtil.restoreDefaultDependency(appServiceService, "gitUtil")
    }

    // 查询本项目下或者服务市场在该项目下部署过的服务
    def "list_all"() {
        given:
        def url = MAPPING + "/list_all"

        when: '查询部署的服务'
        def result = restTemplate.getForEntity(url, List.class, project_id)
        then:
        result.body.size() != 0
    }

    // 批量校验appServiceCode和appServiceName
    def "batch_check"() {
        given:
        def url = MAPPING + "/batch_check"
        AppServiceBatchCheckVO appServiceBatchCheckVO = new AppServiceBatchCheckVO()
        List<String> listCode = new ArrayList<>()
        List<String> listName = new ArrayList<>()
        listCode.add("appCode")
        listCode.add("testCode")
        listName.add("appName")
        listName.add("testName")
        appServiceBatchCheckVO.setListCode(listCode)
        appServiceBatchCheckVO.setListName(listName)

        when: '批量校验'
        def result = restTemplate.postForEntity(url, appServiceBatchCheckVO, AppServiceBatchCheckVO, project_id)
        then:
        result.getBody().listName.size() == 1
        result.getBody().listCode.size() == 1
    }

    // 根据服务编码查询服务
    def "query_by_code"() {
        given:
        def url = MAPPING + "/query_by_code"
        def code = "appCode"

        when: '查询服务编码'
        def result = restTemplate.getForEntity(url + "?code={code}", AppServiceRepVO, project_id, code)
        then:
        result.getBody().getCode() == code
    }

    // 查询拥有服务权限的项目成员及项目所有者
    def 'pagePermissionUsers'() {
        given:
        def url = MAPPING + '/{app_service_id}/page_permission_users'
        def PROJECT_OWNER = "role/project/default/project-owner"
        def PROJECT_MEMBER = "role/project/default/project-member"

        and: 'mock RoleVO'
        // member
        RoleVO memberRoleVo = new RoleVO()
        memberRoleVo.setId(1)
        memberRoleVo.setCode("owner")
        memberRoleVo.setName("owner")
        RoleSearchVO roleSearchVO = new RoleSearchVO()
        roleSearchVO.setCode(PROJECT_MEMBER)

        List<RoleVO> memberRoleVoList = new ArrayList<>()
        memberRoleVoList.add(memberRoleVo)
        PageInfo<RoleVO> memberPageInfo = new PageInfo<>()
        memberPageInfo.setList(memberRoleVoList)
        Mockito.when(baseServiceClient.queryRoleIdByCode(any())).thenReturn(new ResponseEntity<PageInfo<RoleVO>>(memberPageInfo, HttpStatus.OK))

        // owner
        RoleVO ownerRoleVO = new RoleVO()
        ownerRoleVO.setId(1)
        ownerRoleVO.setCode("owner")
        ownerRoleVO.setName("owner")
        RoleSearchVO ownerRoleSearchVO = new RoleSearchVO()
        ownerRoleSearchVO.setCode(PROJECT_OWNER)

        List<RoleVO> ownerRoleVoList = new ArrayList<>()
        ownerRoleVoList.add(memberRoleVo)
        PageInfo<RoleVO> ownerPageInfo = new PageInfo()
        ownerPageInfo.setList(memberRoleVoList)
        Mockito.when(baseServiceClient.queryRoleIdByCode(any())).thenReturn(new ResponseEntity<PageInfo<RoleVO>>(ownerPageInfo, HttpStatus.OK))

        and: 'mock IamUserDTO'
        IamUserDTO iamUserDTOMember = new IamUserDTO()
        iamUserDTOMember.setId(1)
        iamUserDTOMember.setOrganizationId(init_id)

        IamUserDTO iamUserDTOOwner = new IamUserDTO()
        iamUserDTOOwner.setId(2)
        iamUserDTOOwner.setOrganizationId(init_id)

        // Mock 所有成员responseEntity
        List<IamUserDTO> iamUserDTOMemberArrayList = new ArrayList<>()
        iamUserDTOMemberArrayList.add(iamUserDTOMember)
        iamUserDTOMemberArrayList.add(iamUserDTOOwner)
        PageInfo iamUserMemberPageInfo = new PageInfo()
        iamUserMemberPageInfo.setList(iamUserDTOMemberArrayList)
        ResponseEntity iamUserMemberResponseEntity = new ResponseEntity(iamUserMemberPageInfo, HttpStatus.OK)
        Mockito.when(baseServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), eq(1L), anyLong(), anyBoolean(), any())).thenReturn(iamUserMemberResponseEntity)

        // Mock OwnerResponseEntity
        List<IamUserDTO> iamUserDTOOwnerArrayList = new ArrayList<>()
        iamUserDTOOwnerArrayList.add(iamUserDTOOwner)
        PageInfo iamUserOwnerPageInfo = new PageInfo()
        iamUserOwnerPageInfo.setList(iamUserDTOOwnerArrayList)
        ResponseEntity iamUserOwnerResponseEntity = new ResponseEntity(iamUserOwnerPageInfo, HttpStatus.OK)
        Mockito.when(baseServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), eq(2L), anyLong(), anyBoolean(), any())).thenReturn(iamUserOwnerResponseEntity)

        when: '查询拥有服务权限的项目成员及项目所有者'
        def result = restTemplate.postForEntity(url + '?page=1&size=1', searchParam, PageInfo.class, project_id, init_id)

        then:
        result.getBody().getList().size() != 0
    }

    // 项目下查询共享服务
    def "page_share_app_service"() {
        given:
        def url = MAPPING + "/page_share_app_service"

        and: '插入AppServiceShareRuleDTO类'
        AppServiceShareRuleDTO appServiceShareRuleDTO = new AppServiceShareRuleDTO()
        appServiceShareRuleDTO.setAppServiceId(init_id)
        appServiceShareRuleDTO.setId(init_id)
        appServiceShareRuleDTO.setVersion("0.1.0")
        appServiceShareRuleDTO.setProjectId(init_id)
        appServiceShareRuleMapper.insert(appServiceShareRuleDTO)

//        and: 'mock ProjectDTO'
//        OrganizationDTO organizationDO = new OrganizationDTO()
//        organizationDO.setId(1L)
//        organizationDO.setCode("org")
//        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
//        Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(anyLong())

        // 第一个projectDTO
        ProjectDTO projectDTO1 = new ProjectDTO()
        projectDTO1.setId(1L)
        projectDTO1.setCode("pro")
        projectDTO1.setOrganizationId(1L)

        // 第二个ProjectDTO
        ProjectDTO projectDTO2 = new ProjectDTO()
        projectDTO2.setId(2L)
        projectDTO2.setCode("pro")
        projectDTO2.setOrganizationId(1L)

        List<ProjectDTO> projectDTOList = new ArrayList<>()
        projectDTOList.add(projectDTO1)
        projectDTOList.add(projectDTO2)
        PageInfo<ProjectDTO> projectDTOPage = new PageInfo<>(projectDTOList)
        projectDTOPage.setList(projectDTOList)

        ResponseEntity<PageInfo<ProjectDTO>> projectDTOPageResponseEntity = new ResponseEntity<>(projectDTOPage, HttpStatus.OK)

        Mockito.doReturn(new ResponseEntity(projectDTO1, HttpStatus.OK)).when(baseServiceClient).queryIamProject(anyLong())
        Mockito.when(baseServiceClient.queryProjectByOrgId(anyLong(), anyInt(), anyInt(), eq(null), argThat(new ArgumentMatcher<String[]>() {
            @Override
            boolean matches(String[] argument) {
                return true
            }
        }))).thenReturn(projectDTOPageResponseEntity)


        when: '查询共享服务'
        def result = restTemplate.postForEntity(url + "?page=0&size=0", searchParam, PageInfo.class, project_id,)

        then:
        result.body.getList().size() != 0

        appServiceShareRuleMapper.delete(appServiceShareRuleDTO)
    }

    //查询没有服务权限的项目成员
    def 'listNonPermissionUsers'() {
        given:
        def url = MAPPING + "/{app_service_id}/list_non_permission_users"
        def PROJECT_OWNER = "role/project/default/project-owner"
        def PROJECT_MEMBER = "role/project/default/project-member"

        and: 'mock RoleVO'
        // owner
        RoleVO ownerRoleVO = new RoleVO()
        ownerRoleVO.setId(2)
        ownerRoleVO.setCode("owner")
        ownerRoleVO.setName("appName")

        RoleSearchVO ownerRoleSearchVO = new RoleSearchVO()
        ownerRoleSearchVO.setCode(PROJECT_OWNER)

        List<RoleVO> ownerRoleVOList = new ArrayList<>()
        ownerRoleVOList.add(ownerRoleVO)
        PageInfo ownerRolePageInfo = new PageInfo()
        ownerRolePageInfo.setList(ownerRoleVOList)
        Mockito.when(baseServiceClient.queryRoleIdByCode(argThat(new ArgumentMatcher<RoleSearchVO>() {
            @Override
            boolean matches(RoleSearchVO argument) {
                if (ownerRoleSearchVO.getCode() == PROJECT_OWNER) {
                    return true
                }
                return false
            }
        }))).thenReturn(new ResponseEntity(ownerRolePageInfo, HttpStatus.OK))

        // member
        RoleVO memberRoleVO = new RoleVO()
        memberRoleVO.setId(1)
        memberRoleVO.setCode("member")
        memberRoleVO.setName("appName")

        RoleSearchVO memberRoleSearchVO = new RoleSearchVO()
        memberRoleSearchVO.setCode(PROJECT_MEMBER)

        List<RoleVO> memberRoleVOList = new ArrayList<>()
        memberRoleVOList.add(memberRoleVO)
        PageInfo memberRolePageInfo = new PageInfo()
        memberRolePageInfo.setList(memberRoleVOList)
        Mockito.when(baseServiceClient.queryRoleIdByCode(argThat(new ArgumentMatcher<RoleSearchVO>() {
            @Override
            boolean matches(RoleSearchVO argument) {
                if (memberRoleSearchVO.getCode() == PROJECT_MEMBER) {
                    return true
                }
                return false
            }
        }))).thenReturn(new ResponseEntity(memberRolePageInfo, HttpStatus.OK))

        and: 'Mock 所有成员responseEntity'
        IamUserDTO iamUserDTOMember = new IamUserDTO()
        iamUserDTOMember.setId(1)
        iamUserDTOMember.setOrganizationId(init_id)

        IamUserDTO iamUserDTOOwner = new IamUserDTO()
        iamUserDTOOwner.setId(2)
        iamUserDTOOwner.setOrganizationId(init_id)

        List<IamUserDTO> iamUserDTOMemberArrayList = new ArrayList<>()
        iamUserDTOMemberArrayList.add(iamUserDTOMember)
        iamUserDTOMemberArrayList.add(iamUserDTOOwner)
        PageInfo iamUserMemberPageInfo = new PageInfo()
        iamUserMemberPageInfo.setList(iamUserDTOMemberArrayList)
        ResponseEntity iamUserMemberResponseEntity = new ResponseEntity(iamUserMemberPageInfo, HttpStatus.OK)
        Mockito.when(baseServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(anyInt(), anyInt(), eq(1L), anyLong(), anyBoolean(), any())).thenReturn(iamUserMemberResponseEntity)

        when: '查询没有权限的成员'
        def result = restTemplate.getForEntity(url, List.class, project_id, init_id)
        then:
        result.statusCode.is2xxSuccessful()
    }

    //服务权限更新
    def 'updatePermission'() {
        given:
        def url = MAPPING + '/{app_service_id}/update_permission'
        AppServiceDTO appServiceDTO = new AppServiceDTO()
        appServiceDTO.setId(2L)
        appServiceDTO.setSkipCheckPermission(false)
        appServiceMapper.insert(appServiceDTO)

        AppServicePermissionVO appServicePermissionVO = new AppServicePermissionVO()
        List<Long> ids = new ArrayList<>()
        ids.add(1L)
        appServicePermissionVO.setUserIds(ids)

        when: '原本跳过，现在也跳过权限检查'
        appServicePermissionVO.setSkipCheckPermission(true)
        def result = restTemplate.postForEntity(url, appServicePermissionVO, null, project_id, init_id)
        then:
        result.statusCode.is2xxSuccessful()


        when: '原本跳过，现在不跳过权限检查'
        appServicePermissionVO.setSkipCheckPermission(false)
        result = restTemplate.postForEntity(url, appServicePermissionVO, null, project_id, init_id)
        then:
        result.statusCode.is2xxSuccessful()
        appServiceUserRelMapper.selectAll().size() != 0

        when: '原本不跳过，现在不跳过'
        appServicePermissionVO.setSkipCheckPermission(false)
        result = restTemplate.postForEntity(url, appServicePermissionVO, null, project_id, 2L)
        then:
        result.statusCode.is2xxSuccessful()
        appServiceUserRelMapper.selectAll().size() != 0

        when: '原本不跳过，现在跳过'
        appServicePermissionVO.setSkipCheckPermission(true)
        result = restTemplate.postForEntity(url, appServicePermissionVO, null, project_id, 2L)
        then:
        result.statusCode.is2xxSuccessful()
        appServiceMapper.selectByPrimaryKey(2L).getSkipCheckPermission()

        appServiceMapper.deleteByPrimaryKey(2L)

    }

    //服务权限删除
    def 'deletePermission'() {
        given:
        def url = MAPPING + '/{app_service_id}/delete_permission'
        when: '权限删除'
        restTemplate.delete(url + "?user_id={userId}", project_id, 1L, 1L, 1L)
        then:
        appServiceUserRelMapper.selectAll().size() == 0


    }

    //项目下查询组织下所有项目，除当前项目
    def 'listProjects'() {
        given:
        def url = MAPPING + '/{organization_id}/list_projects'

        and: 'mock pagingProjectByOptions'
        ApplicationDTO applicationDTO1 = new ApplicationDTO()
        applicationDTO1.setName("app1")
        applicationDTO1.setId(1L)

        ApplicationDTO applicationDTO2 = new ApplicationDTO()
        applicationDTO2.setName("app2")
        applicationDTO2.setId(2L)

        ProjectDTO projectDTO1 = new ProjectDTO()
        projectDTO1.setId(1L)
        projectDTO1.setApplicationDTO(applicationDTO1)

        ProjectDTO projectDTO2 = new ProjectDTO()
        projectDTO2.setApplicationDTO(applicationDTO2)
        projectDTO2.setId(2L)
        List<ProjectDTO> projectDTOList = new ArrayList<>()
        projectDTOList.add(projectDTO1)
        projectDTOList.add(projectDTO2)
        PageInfo pageInfo = new PageInfo()
        pageInfo.setList(projectDTOList)
        Mockito.when(baseServiceClient.pagingProjectByOptions(anyLong(), anyBoolean(), anyInt(), anyInt(), any())).thenReturn(new ResponseEntity<PageInfo<ProjectDTO>>(pageInfo, HttpStatus.OK))

        when: '查询所有组织'
        def result = restTemplate.getForEntity(url, List.class, project_id, init_id)
        then:
        result.body.size() != 0
    }

    //分组查询应用服务
    def "list_app_group"() {
        given:
        def url = MAPPING + "/list_app_group"

        and: '添加AppServiceShareRuleDTO类'
        AppServiceShareRuleDTO appServiceShareRuleDTO = new AppServiceShareRuleDTO()
        appServiceShareRuleDTO.setAppServiceId(init_id)
        appServiceShareRuleDTO.setId(init_id)
        appServiceShareRuleDTO.setVersion("0.1.0")
        appServiceShareRuleDTO.setShareLevel('project')
        appServiceShareRuleMapper.insert(appServiceShareRuleDTO)

        and: 'mock ApplicationDTO'
        ApplicationDTO applicationDTO = new ApplicationDTO()
        applicationDTO.setId(init_id)
        applicationDTO.setName("applicationDTO")
        applicationDTO.setOrganizationId(1L)
        applicationDTO.setSourceId(1L)
        Mockito.when(baseServiceClient.queryAppById(anyLong())).thenReturn(new ResponseEntity<ApplicationDTO>(applicationDTO, HttpStatus.OK))

        when: '分组查询应用服务'
        def result = restTemplate.getForEntity(url, List.class, project_id)
        then:
        result.getBody().size() != 0

        appServiceShareRuleMapper.delete(appServiceShareRuleDTO)
    }


    // TODO 静态方法mock问题
    //查看sonarqube相关报表
//    def "getSonarQubeTable"() {
//        given:
//        def url = MAPPING + '/{app_service_id}/sonarqube_table'
//
//        ProjectDTO projectDTO = new ProjectDTO()
//        projectDTO.setId(1L)
//        projectDTO.setName("pro")
//        projectDTO.setOrganizationId(1L)
//        Mockito.doReturn(new ResponseEntity(projectDTO, HttpStatus.OK)).when(baseServiceClient).queryIamProject(1L)
//
//        OrganizationDTO organizationDTO = new OrganizationDTO()
//        organizationDTO.setId(1L)
//        organizationDTO.setCode("testOrganization")
//        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
//        Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(1L)
//
//        SonarHistroy sonarHistroy1 = new SonarHistroy()
//        sonarHistroy1.setValue("history1")
//        sonarHistroy1.setDate("2019-08-26 22:04:21")
//
//        SonarHistroy sonarHistroy2 = new SonarHistroy()
//        sonarHistroy2.setValue("history2")
//        sonarHistroy2.setDate("2019-08-26 22:07:00")
//
//        List<SonarHistroy> histroyList = new ArrayList<>()
//        histroyList.add(sonarHistroy1)
//        histroyList.add(sonarHistroy2)
//
//        SonarTableMeasure bugMeasure = new SonarTableMeasure()
//        bugMeasure.setMetric("bugs")
//        bugMeasure.setHistory(histroyList)
//
//        SonarTableMeasure coverageMeasure = new SonarTableMeasure()
//        coverageMeasure.setMetric("COVERAGE")
//        coverageMeasure.setHistory(histroyList)
//
//        SonarTableMeasure nclocMeasure = new SonarTableMeasure()
//        nclocMeasure.setMetric("ncloc")
//        nclocMeasure.setHistory(histroyList)
//
//        List<SonarTableMeasure> sonarTableMeasureList = new ArrayList<>()
//        sonarTableMeasureList.add(bugMeasure)
//        sonarTableMeasureList.add(coverageMeasure)
//        sonarTableMeasureList.add(nclocMeasure)
//
//        SonarTables sonarTables = new SonarTables()
//        sonarTables.setMeasures(sonarTableMeasureList)
//
//        Request.Builder requestBuilder = new Request.Builder()
//        Request request = requestBuilder.url("http://sonarurl").build()
//
//        okhttp3.Response.Builder builder = new okhttp3.Response.Builder()
//        builder.code(200).request(request).protocol(Protocol.HTTP_1_1).message("test")
//
//
//        okhttp3.Response response = builder.build()
//
//        PowerMockito.mockStatic(TestUtil.class)
//        PowerMockito.mockStatic(RetrofitHandler.class)
//        def sonarClient = Mockito.mock(SonarClient.class)
//        def call = Mockito.mock(Call.class)
//        PowerMockito.when(RetrofitHandler.getSonarClient(any(), any(), any(), any())).thenReturn(sonarClient)
//        PowerMockito.when(sonarClient.getSonarTables(any())).thenReturn()
//        PowerMockito.when(call.execute()).thenReturn(Response.success(sonarTables, response))
//
//        when: 'issue'
//        def result = restTemplate.getForEntity(url, SonarTableVO, project_id, init_id, "issue", "2019-08-26 22:27:23", "2019-08-26 22:27:30")
//        then:
//        result.statusCode.is2xxSuccessful()
//
////        when: 'coverage'
////        then:
////        when: 'duplicate'
////        then:
//
//    }
    // 清除测试数据
    def "cleanupData"() {
        given:
        isToClean = true
    }
}
