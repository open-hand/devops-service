package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationDTO
import io.choerodon.devops.api.dto.ApplicationRepDTO
import io.choerodon.devops.api.dto.ApplicationUpdateDTO
import io.choerodon.devops.app.service.ApplicationService
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.repository.UserAttrRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.*
import io.choerodon.devops.infra.mapper.*
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.Matchers.anyObject
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.mockito.Matchers.anyString

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
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository
    @Autowired
    @Qualifier("mockGitlabGroupMemberRepository")
    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    SagaClient sagaClient = Mockito.mock(SagaClient.class)

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
        params.put("name",[])
        params.put("code",["app"])
        searchParam.put("searchParam", params)
        searchParam.put("param", "")
    }
    //项目下创建应用
    def "create"() {
        given: '创建issueDTO'
        ApplicationDTO applicationDTO = new ApplicationDTO()

        and: '赋值'
        applicationDTO.setId(init_id)
        applicationDTO.setName("dtoname")
        applicationDTO.setCode("ddtoapp")
        applicationDTO.setProjectId(project_id)
        applicationDTO.setApplictionTemplateId(init_id)

        and: '设置gitlab组'
        GitlabGroupE gitlabGroupE = new GitlabGroupE()
        gitlabGroupE.setDevopsAppGroupId(init_id)
        gitlabGroupE.setProjectE(projectE)
        GitlabMemberE gitlabMemberE = new GitlabMemberE()
        gitlabMemberE.setAccessLevel(AccessLevel.OWNER.toValue())

        and: 'sagaClient'
        applicationService.initMockService(sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(anyString(), anyObject())

        and: '默认返回值'
        userAttrRepository.queryById(_) >> userAttrE
        iamRepository.queryIamProject(_) >> projectE
        iamRepository.queryOrganizationById(_) >> organization
        gitlabGroupMemberRepository.getUserMemberByUserId(*_) >> gitlabMemberE

        when: '创建一个应用'
        def entity = restTemplate.postForEntity("/v1/projects/{project_id}/apps", applicationDTO, ApplicationRepDTO.class, project_id)

        then: '返回值'
        entity.statusCode.is2xxSuccessful()
        ApplicationDO applicationDo = applicationMapper.selectByPrimaryKey(init_id)

        expect: '验证更新是否成功'
        applicationDo.getCode() == 'ddtoapp'
    }

    //项目下查询单个应用信息
    def "queryByAppId"() {
        given:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when:
        def entity = restTemplate.getForEntity("/v1/projects/{project_id}/apps/{app_id}/detail", ApplicationRepDTO.class, project_id, 1L)

        then:
        entity.body.code == 'ddtoapp'
    }

    //项目下更新应用信息
    def "update"() {
        given:
        ApplicationUpdateDTO applicationUpdateDTO = new ApplicationUpdateDTO()
        applicationUpdateDTO.setId(init_id)
        applicationUpdateDTO.setName("updatename")

        when:
        restTemplate.put("/v1/projects/{project_id}/apps", applicationUpdateDTO, project_id)

        then:
        ApplicationDO applicationDo2 = applicationMapper.selectByPrimaryKey(init_id)

        expect:
        applicationDo2.name == "updatename"
    }

    //停用应用
    def "disableApp"() {
        when:
        restTemplate.put("/v1/projects/1/apps/1?active=false", Boolean.class)
        then:
        ApplicationDO applicationDo = applicationMapper.selectByPrimaryKey(init_id)
        expect:
        !applicationDo.active
    }

    //启用应用
    def "enableApp"() {
        when:
        restTemplate.put("/v1/projects/1/apps/1?active=true", Boolean.class)
        then:
        ApplicationDO applicationDo = applicationMapper.selectByPrimaryKey(init_id)
        expect:
        applicationDo.active
    }

    //项目下分页查询应用
    def "pageByOptions"() {
        given:
        ApplicationVersionReadmeDO applicationVersionReadmeDO = new ApplicationVersionReadmeDO()
        applicationVersionReadmeDO.setReadme("readme")

        and:
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when:
        def object = restTemplate.postForObject("/v1/projects/{project_id}/apps/list_by_options?active=true&has_version=false",searchParam, Page.class, project_id)

        then:
        object.size() == 1

        expect:
        object.get(0).name == "updatename"
    }

    //根据环境id分页获取已部署正在运行实例的应用
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
        def applicationPage = restTemplate.getForObject("/v1/projects/{project_id}/apps/pages?env_id={env_id}",Page.class,project_id,  1)
        then:
        applicationPage.size() == 1
        expect:
        applicationPage.get(0).name == "updatename"
    }

    //根据环境id获取已部署正在运行实例的应用
    def "listByEnvIdAndStatus"() {
        when:
        def applicationList = restTemplate.getForObject("/v1/projects/1/apps/options?envId=1&status=running", List.class)

        then:
        applicationList.size() == 1
        expect:
        applicationList.get(0).name == "updatename"
    }

    //项目下查询所有已经启用的应用
    def "listByActive"() {
        given: '更新gitlabprojectID'
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(1L)
        applicationDO.setGitlabProjectId(1)
        applicationMapper.updateByPrimaryKey(applicationDO)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization

        when:
        def applicationList = restTemplate.getForObject("/v1/projects/{project_id}/apps", List.class, project_id)

        then:
        applicationList.size() == 1

        expect:
        applicationList.get(0).name == "updatename"
    }

    //项目下查询所有已经启用的应用
    def "listAll"() {
        when:
        def applicationList = restTemplate.getForObject("/v1/projects/{project_id}/apps/list_all", List.class, project_id)

        then:
        applicationList.size() == 1

        expect:
        applicationList.get(0).name == "updatename"
    }

    //创建应用校验名称是否存在
    def "checkName"() {
        when:
        def entity = restTemplate.getForEntity("/v1/projects/{project_id}/apps/checkName?name={name}", Object.class, project_id, "name1")

        then:
        entity.statusCode.is2xxSuccessful()
        entity.body == null

        when:
        def entity2 = restTemplate.getForEntity("/v1/projects/{project_id}/apps/checkName?name={name}", Object.class, project_id, "updatename")

        then:
        entity2.statusCode.is2xxSuccessful()
        entity2.body.failed == true
    }

    //创建应用校验编码是否存在
    def "checkCode"() {
        when:
        def entity = restTemplate.getForEntity("/v1/projects/{project_id}/apps/checkCode?code={code}", Object.class, project_id, "code1")

        then:
        entity.statusCode.is2xxSuccessful()
        entity.body == null


        when:
        def entity2 = restTemplate.getForEntity("/v1/projects/{project_id}/apps/checkCode?code={code}", Object.class, project_id, "ddtoapp")

        then:
        entity2.statusCode.is2xxSuccessful()
        entity2.body.failed == true
    }

    //查询所有应用模板
    def "listTemplate"() {
        given:
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
        and:
        iamRepository.queryIamProject(_ as Long) >> projectE

        when:
        def templateList = restTemplate.getForObject("/v1/projects/{project_id}/apps/template", List.class, project_id)

        then:
        templateList.size() == 4

        expect:
        templateList.get(3).code == "tempcode"
    }

    //项目下查询所有已经启用的且未发布的且有版本的应用
    def "listByActiveAndPubAndVersion"() {
        given:
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
        applicationVersionDO.setId(init_id)
        applicationVersionDO.setVersion("0.1.0-dev.20180521111826")
        applicationVersionDO.setAppId(init_id)
        applicationVersionMapper.insert(applicationVersionDO)

        when:
        def object = restTemplate.postForObject("/v1/projects/{project_id}/apps/list_unpublish", searchParam,Page.class , project_id)

        then:
        object.get(0).code == "ddtoapp"
    }

    //项目下分页查询代码仓库
    def "listCodeRepository"() {
        given:
        iamRepository.queryIamProject(_) >> projectE
        iamRepository.queryOrganizationById(_) >> organization

        when:
        def object = restTemplate.postForObject("/v1/projects/{project_id}/apps/list_code_repository", searchParam,Page.class, project_id)

        then:
        object.get(0).code == "ddtoapp"

    }
    //清除测试数据
    def "cleanupData"() {
        given:
        applicationInstanceMapper.deleteByPrimaryKey(init_id)
        devopsEnvironmentMapper.deleteByPrimaryKey(init_id)
        applicationMapper.deleteByPrimaryKey(init_id)
        applicationTemplateMapper.deleteByPrimaryKey(4L)
        applicationVersionMapper.deleteByPrimaryKey(1L)
    }
}
