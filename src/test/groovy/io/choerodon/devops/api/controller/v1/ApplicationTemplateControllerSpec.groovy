package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.dto.StartInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ApplicationTemplateDTO
import io.choerodon.devops.api.vo.ApplicationTemplateRepVO
import io.choerodon.devops.api.vo.ApplicationTemplateUpdateDTO
import io.choerodon.devops.app.service.ApplicationTemplateService
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.domain.application.entity.DevopsProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.GitlabRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.domain.application.valueobject.ProjectHook
import io.choerodon.devops.infra.common.util.enums.Visibility
import io.choerodon.devops.infra.dataobject.ApplicationTemplateDO
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.ApplicationTemplateMapper
import io.choerodon.devops.infra.persistence.impl.ApplicationTemplateRepositoryImpl
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

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/11
 * Time: 10:30
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ApplicationTemplateController)
@Stepwise
class ApplicationTemplateControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private ApplicationTemplateMapper applicationTemplateMapper
    @Autowired
    private ApplicationTemplateService applicationTemplateService
    @Autowired
    private ApplicationTemplateRepositoryImpl applicationTemplateRepository
    @Autowired
    private IamRepository iamRepository
    @Autowired
    private GitlabRepository gitlabRepository

    SagaClient sagaClient = Mockito.mock(SagaClient.class)
    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    @Shared
    Organization organization = new Organization()
    @Shared
    OrganizationDO organizationDO = new OrganizationDO()
    @Shared
    UserAttrE userAttrE = new UserAttrE()
    @Shared
    DevopsProjectE devopsProjectE = new DevopsProjectE()
    @Shared
    Map<String, Object> searchParam = new HashMap<>();
    @Shared
    Long org_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    Long template_id = 4L

     //初始化部分对象
    def setupSpec() {
        given:
        organization.setId(init_id)
        organization.setCode("org")

        organizationDO.setCode("orgDO")
        organizationDO.setId(org_id)

//        gitlabGroupE.setName("org_template")
//        gitlabGroupE.setPath("org_template")
//        gitlabGroupE.setVisibility(Visibility.PUBLIC)

        userAttrE.setIamUserId(init_id)
        userAttrE.setGitlabUserId(init_id)

        Map<String, Object> params = new HashMap<>();
        params.put("name", [])
        params.put("code", ["code"])
        searchParam.put("searchParam", params)
        searchParam.put("param", "")
    }

    //组织下创建应用模板
    def "createTemplate"() {
        given: "初始化数据"
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO()
        applicationTemplateDTO.setId(4L)
        applicationTemplateDTO.setCode("code")
        applicationTemplateDTO.setName("app")
        applicationTemplateDTO.setDescription("des")
        applicationTemplateDTO.setOrganizationId(1L)

        and: 'mock saga'
        DependencyInjectUtil.setAttribute(applicationTemplateService, "sagaClient", sagaClient)
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(anyString(), any(StartInstanceDTO))


        and: 'mock 查询组织'
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
        Mockito.doReturn(new ResponseEntity<OrganizationDO>(organizationDO, HttpStatus.OK)).when(iamServiceClient).queryOrganizationById(anyLong())

        and: 'mock 查询gitlab组'
        applicationTemplateRepository.initMockService(iamServiceClient)
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)
        GroupDO groupDO = null
        ResponseEntity<GroupDO> responseEntity = new ResponseEntity<>(groupDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.queryGroupByName(anyString(), anyInt())).thenReturn(responseEntity)

        and: 'mock 创建gitlab组'
        GroupDO newGroupDO = new GroupDO()
        newGroupDO.setId(1)
        ResponseEntity<GroupDO> newResponseEntity = new ResponseEntity<>(newGroupDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createGroup(any(GroupDO.class), anyInt())).thenReturn(newResponseEntity)

        when: '组织下创建应用模板'
        def entity = restTemplate.postForEntity("/v1/organizations/1/app_templates", applicationTemplateDTO, ApplicationTemplateRepVO.class)

        then: '验证响应状态码'
        entity.statusCode.is2xxSuccessful()

        expect: '验证创建的模板结果'
        ApplicationTemplateDO applicationTemplateDO = applicationTemplateMapper.selectByPrimaryKey(4L)
        applicationTemplateDO["code"] == "code"
    }

    // 组织下更新应用模板
    def "updateTemplate"() {
        given: '初始化模板更新dto类'
        ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO = new ApplicationTemplateUpdateDTO()
        applicationTemplateUpdateDTO.setId(4L)
        applicationTemplateUpdateDTO.setName("updateName")
        applicationTemplateUpdateDTO.setDescription("des")

        when: '组织下更新应用模板'
        restTemplate.put("/v1/organizations/1/app_templates", applicationTemplateUpdateDTO, ApplicationTemplateRepVO.class)

        then: '返回值'
        ApplicationTemplateDO applicationTemplateDO = applicationTemplateMapper.selectByPrimaryKey(4L)

        expect: '验证返回结果'
        applicationTemplateMapper.selectAll().get(3)["name"] == "updateName"
    }

    // 组织下查询单个应用模板
    def "queryByAppTemplateId"() {
        when: '组织下查询单个应用模板'
        def object = restTemplate.getForObject("/v1/organizations/{org_id}/app_templates/{template_id}", ApplicationTemplateRepVO.class, org_id, template_id)

        then: '验证返回结果'
        object["code"] == "code"
    }

    // 组织下分页查询应用模板
    def "listByOptions"() {
        when: '组织下分页查询应用模板'
        def page = restTemplate.postForObject("/v1/organizations/{org_id}/app_templates/list_by_options", searchParam, Page.class, org_id)

        then: '验证返回结果'
        page.size() == 1

        expect: '验证返回结果'
        page.get(0)["code"] == "code"
    }

    // 组织下分页查询应用模板
    def "listByOrgId"() {
        when: '组织下分页查询应用模板'
        def list = restTemplate.getForObject("/v1/organizations/{org_id}/app_templates", List.class, org_id)

        then: '验证返回结果'
        list.size() == 4

        expect: '验证返回结果'
        list.get(3)["code"] == "code"
    }

    // 创建模板校验名称是否存在
    def "checkName"() {
        when: '创建模板校验名称是否存在'
        def exception = restTemplate.getForEntity("/v1/organizations/{org_id}/app_templates/check_name?name={name}", ExceptionResponse.class, org_id, "name")

        then: '校验通过，没有抛出异常'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)
    }

    // 创建模板校验编码是否存在
    def "checkCode"() {
        when: '创建模板校验编码是否存在'
        def entity = restTemplate.getForEntity("/v1/organizations/{org_id}/app_templates/check_code?code={code}", Object.class, org_id, "testCode")

        then: '校验通过，没有抛出异常'
        entity.statusCode.is2xxSuccessful()
        entity.getBody() == null

        when: '创建模板校验编码是否存在'
        def exception = restTemplate.getForEntity("/v1/organizations/{org_id}/app_templates/check_code?code={code}", ExceptionResponse.class, org_id, "code")

        then: '校验未通过，抛出异常'
        exception.statusCode.is2xxSuccessful()
        exception.getBody()["code"] == "error.code.exist"
    }

    // 组织下删除应用模板
    def "deleteTemplate"() {
        given: 'mock 删除gitlab项目'
        DependencyInjectUtil.setAttribute(gitlabRepository, "gitlabServiceClient", gitlabServiceClient)

        ProjectHook projectHook = new ProjectHook()
        ResponseEntity<ProjectHook> responseEntity = new ResponseEntity<>(projectHook, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.deleteProject(anyInt(), anyInt())).thenReturn(responseEntity)

        when: '组织下删除应用模板'
        restTemplate.delete("/v1/organizations/{project_id}/app_templates/{template_id}", org_id, 4L)

        then: '返回值'
        ApplicationTemplateDO applicationTemplateDO = applicationTemplateMapper.selectByPrimaryKey(4L)

        expect: '验证是否删除'
        applicationTemplateDO == null
    }
}
