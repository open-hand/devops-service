package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationDTO
import io.choerodon.devops.api.dto.ApplicationRepDTO
import io.choerodon.devops.api.dto.ApplicationUpdateDTO
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
    private ApplicationInstanceRepository applicationInstanceRepository
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
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

        devopsAppMarketDO = new DevopsAppMarketDO()
        devopsAppMarketDO.setId(1L)
        devopsAppMarketDO.setAppId(2L)
        devopsAppMarketDO.setPublishLevel("pub")
        devopsAppMarketDO.setContributor("con")
        devopsAppMarketDO.setDescription("des")
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
    }
    // 项目下创建应用
    def "create"() {
        given: '创建issueDTO'
        ApplicationDTO applicationDTO = new ApplicationDTO()

        and: '赋值'
        applicationDTO.setId(init_id)
        applicationDTO.setName("dtoname")
        applicationDTO.setCode("ddtoapp")
        applicationDTO.setProjectId(project_id)
        applicationDTO.setApplictionTemplateId(init_id)


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
        def entity = restTemplate.postForEntity("/v1/projects/{project_id}/apps", applicationDTO, ApplicationRepDTO.class, project_id)

        then: '校验结果'
        entity.statusCode.is2xxSuccessful()
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(init_id)

        expect: '校验查询结果'
        applicationDO["code"] == "ddtoapp"
    }

    // 项目下查询单个应用信息
    def "queryByAppId"() {
        when:
        def entity = restTemplate.getForEntity("/v1/projects/{project_id}/apps/{app_id}/detail", ApplicationRepDTO.class, project_id, 1L)

        then: '校验结果'
        entity.getBody()["code"] == "ddtoapp"
    }

    // 项目下更新应用信息
    def "update"() {
        given: '设置applicationUpdateDTO类'
        ApplicationUpdateDTO applicationUpdateDTO = new ApplicationUpdateDTO()
        applicationUpdateDTO.setId(init_id)
        applicationUpdateDTO.setName("updatename")

        when:
        restTemplate.put("/v1/projects/{project_id}/apps", applicationUpdateDTO, project_id)

        then: '校验结果'
        ApplicationDO applicationDO2 = applicationMapper.selectByPrimaryKey(init_id)

        expect: '校验查询结果'
        applicationDO2["name"] == "updatename"
    }

    // 停用应用
    def "disableApp"() {
        when:
        restTemplate.put("/v1/projects/1/apps/1?active=false", Boolean.class)

        then: '返回值'
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(init_id)

        expect: '校验是否激活'
        applicationDO["isActive"] == false
    }

    // 启用应用
    def "enableApp"() {
        when:
        restTemplate.put("/v1/projects/1/apps/1?active=true", Boolean.class)

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
        restTemplate.delete("/v1/projects/1/apps/1")

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
        applicationDO.setGitlabProjectId(1)
        applicationDO.setAppTemplateId(1L)
        applicationMapper.insert(applicationDO)
    }

    // 项目下分页查询应用
    def "pageByOptions"() {
        when:
        def app = restTemplate.postForObject("/v1/projects/1/apps/list_by_options?active=true", searchParam, Page.class)

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

        when:
        def applicationPage = restTemplate.getForObject("/v1/projects/{project_id}/apps/pages?env_id={env_id}", Page.class, project_id, 1)

        then: '返回值'
        applicationPage.size() == 1

        expect: '验证返回值'
        applicationPage.getContent().get(0)["code"] == "appCode"
    }

    // 根据环境id获取已部署正在运行实例的应用
    def "listByEnvIdAndStatus"() {
        given: '初始化appMarket对象'
        applicationMarketMapper.insert(devopsAppMarketDO)

        when:
        def applicationList = restTemplate.getForObject("/v1/projects/1/apps/options?envId=1&status=running&appId=1", List.class)

        then: '返回值'
        applicationList.size() == 1

        expect: '验证返回值'
        applicationList.get(0)["code"] == "appCode"
    }

    // 项目下查询所有已经启用的应用
    def "listByActive"() {
        when:
        def applicationList = restTemplate.getForObject("/v1/projects/{project_id}/apps", List.class, project_id)

        then: '返回值'
        applicationList.size() == 1

        expect: '验证返回值'
        applicationList.get(0)["code"] == "appCode"
    }

    // 项目下查询所有已经启用的应用
    def "listAll"() {
        when:
        def applicationList = restTemplate.getForObject("/v1/projects/{project_id}/apps/list_all", List.class, project_id)

        then: '返回值'
        applicationList.size() == 1

        expect: '验证返回值'
        applicationList.get(0)["code"] == "appCode"
    }

    // 创建应用校验名称是否存在
    def "checkName"() {
        when:
        def entity = restTemplate.getForEntity("/v1/projects/1/apps/checkName?name=appName", Object.class)

        then: '名字存在抛出异常'
        entity.statusCode.is2xxSuccessful()
        entity.getBody()["code"] == "error.name.exist"

        when:
        def entity1 = restTemplate.getForEntity("/v1/projects/1/apps/checkName?name=testName", Object.class)

        then: '名字不存在不抛出异常'
        entity1.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }

    // 创建应用校验编码是否存在
    def "checkCode"() {
        when:
        def entity = restTemplate.getForEntity("/v1/projects/1/apps/checkCode?code=appCode", Object.class)

        then:
        entity.statusCode.is2xxSuccessful()
        entity.getBody()["code"] == "error.code.exist"

        when:
        def entity1 = restTemplate.getForEntity("/v1/projects/1/apps/checkCode?code=testCode", Object.class)

        then:
        entity1.statusCode.is2xxSuccessful()
        notThrown(CommonException)
        entity1.body == null
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
        applicationTemplateDO.setGitlabProjectId(init_id)
        applicationTemplateMapper.insert(applicationTemplateDO)

        when:
        def templateList = restTemplate.getForObject("/v1/projects/1/apps/template", List.class)

        then: '返回值'
        templateList.size() == 4

        expect: '校验返回值'
        templateList.get(3)["code"] == "tempcode"
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
        def entity = restTemplate.postForObject("/v1/projects/1/apps/list_unpublish", searchParam, Page.class)

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
        applicationInstanceMapper.deleteByPrimaryKey(init_id)
        devopsEnvironmentMapper.deleteByPrimaryKey(init_id)
        applicationMapper.deleteByPrimaryKey(init_id)
        applicationTemplateMapper.deleteByPrimaryKey(4L)
        applicationVersionMapper.deleteByPrimaryKey(1L)
        applicationMarketMapper.deleteByPrimaryKey(1L)
    }
}
