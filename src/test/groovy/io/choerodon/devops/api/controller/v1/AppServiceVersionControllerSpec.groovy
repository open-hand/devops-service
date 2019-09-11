package io.choerodon.devops.api.controller.v1

import static org.mockito.Matchers.anyInt
import static org.mockito.Matchers.anyLong
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import com.github.pagehelper.PageInfo
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DeployVersionVO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.mapper.*

/**
 * Created by n!Ck
 * Date: 2018/9/17
 * Time: 13:43
 * Description:
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(AppServiceVersionController)
@Stepwise
class AppServiceVersionControllerSpec extends Specification {

    private static final String mapping = "/v1/projects/{project_id}/app_versions"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private AppServiceUserRelMapper appUserPermissionMapper
    @Autowired
    private AppServiceVersionMapper applicationVersionMapper
    @Autowired
    private AppServiceInstanceMapper applicationInstanceMapper
    @Autowired
    private AppServiceVersionValueMapper applicationVersionValueMapper
    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper
    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper

    @Autowired
    private IamService iamRepository

    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L
    @Shared
    Map<String, Object> searchParam = new HashMap<>()
    @Shared
    DevopsEnvCommandDTO devopsEnvCommandDO = new DevopsEnvCommandDTO()
    @Shared
    AppServiceVersionValueDTO applicationVersionValueDO = new AppServiceVersionValueDTO()
    @Shared
    AppServiceUserRelDTO appUserPermissionDO = new AppServiceUserRelDTO()
    @Shared
    DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()
    @Shared
    AppServiceInstanceDTO applicationInstanceDO = new AppServiceInstanceDTO()
    @Shared
    AppServiceVersionDTO applicationVersionDO = new AppServiceVersionDTO()
    @Shared
    DevopsGitlabPipelineDTO devopsGitlabPipelineDO = new DevopsGitlabPipelineDTO()
    @Shared
    DevopsGitlabCommitDTO devopsGitlabCommitDO = new DevopsGitlabCommitDTO()
    @Shared
    AppServiceDTO applicationDO = new AppServiceDTO()

    BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient.class)

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", baseServiceClient)

        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setName("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(baseServiceClient).queryIamProject(1L)
        OrganizationDTO organizationDO = new OrganizationDTO()
        organizationDO.setId(1L)
        organizationDO.setCode("testOrganization")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(1L)

        List<ProjectWithRoleVO> list = new ArrayList<>()
        List<RoleVO> roleDTOList = new ArrayList<>()
        RoleVO roleDTO = new RoleVO()
        roleDTO.setId(44)
        roleDTO.setCode("role/project/default/project-owner")
        roleDTOList.add(roleDTO)
        ProjectWithRoleVO projectWithRoleDTO = new ProjectWithRoleVO()
        projectWithRoleDTO.setName("test-name")
        projectWithRoleDTO.setRoles(roleDTOList)
        list.add(projectWithRoleDTO)
        PageInfo<ProjectWithRoleVO> page = new PageInfo(list)
        ResponseEntity<PageInfo<ProjectWithRoleVO>> responseEntity2 = new ResponseEntity<>(page, HttpStatus.OK)
        Mockito.when(baseServiceClient.listProjectWithRole(anyLong(), anyInt(), anyInt())).thenReturn(responseEntity2)
        List<IamUserDTO> userDOList = new ArrayList<>()
        IamUserDTO userDO1 = new IamUserDTO()
        userDO1.setLoginName("loginName")
        userDO1.setRealName("realName")
        userDOList.add(userDO1)
        ResponseEntity<List<IamUserDTO>> responseEntity3 = new ResponseEntity<>(userDOList, HttpStatus.OK)
        Mockito.doReturn(responseEntity3).when(baseServiceClient).listUsersByIds(1L)
    }

    def setupSpec() {
        given: '初始化分页条件参数'
        Map<String, Object> params = new HashMap<>()
        params.put("version", [])
        params.put("appName", [])
        params.put("appCode", ["app"])
        searchParam.put("searchParam", params)
        searchParam.put("param", "")

        appUserPermissionDO.setAppServiceId(1L)
        appUserPermissionDO.setIamUserId(1L)

        devopsEnvironmentDO.setId(init_id)
        devopsEnvironmentDO.setCode("spock-test")
        devopsEnvironmentDO.setGitlabEnvProjectId(init_id)
        devopsEnvironmentDO.setHookId(init_id)
        devopsEnvironmentDO.setDevopsEnvGroupId(init_id)
        devopsEnvironmentDO.setProjectId(init_id)

        applicationInstanceDO.setId(init_id)
        applicationInstanceDO.setCode("spock-test1")
        applicationInstanceDO.setStatus("running")
        applicationInstanceDO.setAppServiceId(init_id)
        applicationInstanceDO.setAppServiceVersionId(init_id)
        applicationInstanceDO.setEnvId(init_id)
        applicationInstanceDO.setCommandId(init_id)

        applicationDO.setId(init_id)
        applicationDO.setName("app_name")
        applicationDO.setCode("app_code")
        applicationDO.setProjectId(project_id)
        applicationDO.setGitlabProjectId(1)
        applicationDO.setSkipCheckPermission(true)

        applicationVersionDO.setId(init_id)
        applicationVersionDO.setValueId(1L)
        applicationVersionDO.setIsPublish(1)
        applicationVersionDO.setCommit("test")
        applicationVersionDO.setAppServiceId(init_id)
        applicationVersionDO.setVersion("0.1.0-dev.20180521111826")

        devopsEnvCommandDO.setId(1L)
        devopsEnvCommandDO.setObjectVersionId(1L)

        applicationVersionValueDO.setId(1L)
        applicationVersionValueDO.setValue("test-value")

        devopsGitlabPipelineDO.setId(init_id)
        devopsGitlabPipelineDO.setAppServiceId(init_id)
        devopsGitlabPipelineDO.setCommitId(init_id)
        devopsGitlabPipelineDO.setPipelineId(init_id)

        devopsGitlabCommitDO.setAppServiceId(init_id)
        devopsGitlabCommitDO.setId(init_id)
        devopsGitlabCommitDO.setUserId(init_id)
        devopsGitlabCommitDO.setRef("0.1.0-dev.20180521111826")
        devopsGitlabCommitDO.setCommitSha("test")
    }

    // 分页查询应用版本
    def "PageByOptions"() {
        given: '初始化数据'
        applicationMapper.insert(applicationDO)
        applicationVersionMapper.insert(applicationVersionDO)
        applicationInstanceMapper.insert(applicationInstanceDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        appUserPermissionMapper.insert(appUserPermissionDO)
        devopsEnvCommandMapper.insert(devopsEnvCommandDO)
        applicationVersionValueMapper.insert(applicationVersionValueDO)
        devopsGitlabPipelineMapper.insert(devopsGitlabPipelineDO)
        devopsGitlabCommitMapper.insert(devopsGitlabCommitDO)

        when: '分页查询应用版本'
        def page = restTemplate.postForObject(mapping + "/list_by_options?page=0&size=0&appId={app_id}", searchParam, PageInfo.class, project_id, init_id)

        then: '返回值'
        page.getTotal() == 1

        expect: '校验返回结果'
        page.getList().get(0).version == "0.1.0-dev.20180521111826"
    }

    // 应用下查询应用所有版本
    def "QueryByAppId"() {
        given:
        String version = "0.1.0-dev.20180521111826"
        when: '应用下查询应用所有版本'
        def page = restTemplate.getForObject(mapping + "/list_by_app/{app_id}?is_publish=true&page=0&size=10&version={version}", PageInfo.class, project_id, init_id, version)

        then: '返回值'
        page.getTotal() == 1

        expect: '校验返回结果'
        page.getList().get(0).version == "0.1.0-dev.20180521111826"
    }

    // 项目下查询应用所有已部署版本
    def "QueryDeployedByAppId"() {
        when: '项目下查询应用所有已部署版本'
        def list = restTemplate.getForObject(mapping + "/list_deployed_by_app/{app_id}", List.class, project_id, init_id)

        then: '返回值'
        list.size() == 1

        expect: '校验返回结果'
        list.get(0).version == "0.1.0-dev.20180521111826"
    }

    // 查询部署在某个环境应用的应用版本
    def "QueryByAppIdAndEnvId"() {
        when: '查询部署在某个环境应用的应用版本'
        def list = restTemplate.getForObject(mapping + "/app/{app_id}/env/{envId}/query", List.class, 1L, 1L, 1L)

        then: '返回值'
        list.size() == 1

        expect: '校验返回结果'
        list.get(0).version == "0.1.0-dev.20180521111826"
    }

    // 实例下查询可升级版本
    def "GetUpgradeAppVersion"() {
        given: '初始化应用版本DO类'
        AppServiceVersionDTO applicationVersionDO = new AppServiceVersionDTO()
        applicationVersionDO.setId(2L)
        applicationVersionDO.setVersion("0.2.0-dev.20180521111826")
        applicationVersionDO.setAppId(init_id)
        applicationVersionMapper.insert(applicationVersionDO)

        when: '实例下查询可升级版本'
        def list = restTemplate.getForObject(mapping + "/version/{app_version_id}/upgrade_version", List.class, 1L, 1L)

        then: '返回值'
        list.size() == 1

        expect: '校验返回结果'
        list.get(0).version == "0.2.0-dev.20180521111826"
    }

    // 项目下查询应用最新的版本和各环境下部署的版本
    def "GetDeployVersions"() {
        when: '项目下查询应用最新的版本和各环境下部署的版本'
        def dto = restTemplate.getForObject(mapping + "/app/{app_id}/deployVersions", DeployVersionVO.class, 1L, 1L)

        then: '校验返回结果'
        dto["latestVersion"] == "0.2.0-dev.20180521111826"
    }

    // 根据版本id获取版本values
    def "GetVersionValue"() {
        when: '根据版本id获取版本values'
        def str = restTemplate.getForObject(mapping + "/{app_verisonId}/queryValue", String.class, 1L, 1L)

        then: '校验返回值'
        str == "test-value"
    }

    // 根据版本id查询版本信息
    def "GetAppversion"() {
        given: '配置请求参数'
        HttpEntity<List<Long>> entity = new HttpEntity<>(Arrays.asList(applicationVersionDO.getId()))

        when: '根据版本id查询版本信息'
        def dto = restTemplate.postForEntity(mapping + "/list_by_appVersionIds", entity, List, project_id)

        then: '校验返回值'
        dto.statusCode.is2xxSuccessful()
        dto.getBody().size() != 0
        ((LinkedHashMap) dto.getBody().get(0)).get("version") == applicationVersionDO.getVersion()
    }

    def "getAppversionByBranch"() {

        when: '根据分支名查询版本'
        def list = restTemplate.getForObject(mapping + "/list_by_branch?appId=1&branch=0.1.0-dev.20180521111826", List.class, 1L)

        then: '校验返回值'
        list.size() == 1
    }

    def "queryByPipeline"() {

        when: '根据pipeline和分支名查询版本'
        def result = restTemplate.getForObject(mapping + "/query_by_pipeline?pipelineId=1&branch=0.1.0-dev.20180521111826", Boolean.class, 1L)


        then: '校验返回值'
        result


    }

    // 清除测试数据
    def "cleanupData"() {
        given:
        // 删除appInstance
        List<AppServiceInstanceDTO> list = applicationInstanceMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (AppServiceInstanceDTO e : list) {
                applicationInstanceMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDTO> list1 = devopsEnvironmentMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvironmentDTO e : list1) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除app
        List<AppServiceDTO> list2 = applicationMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (AppServiceDTO e : list2) {
                applicationMapper.delete(e)
            }
        }
        // 删除appVersion
        List<AppServiceVersionDTO> list3 = applicationVersionMapper.selectAll()
        if (list3 != null && !list3.isEmpty()) {
            for (AppServiceVersionDTO e : list3) {
                applicationVersionMapper.delete(e)
            }
        }
        // 删除appUserPermission
        List<AppServiceUserRelDTO> list4 = appUserPermissionMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (AppServiceUserRelDTO e : list4) {
                appUserPermissionMapper.delete(e)
            }
        }
        // 删除appVersionValue
        List<AppServiceVersionValueDTO> list5 = applicationVersionValueMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (AppServiceVersionValueDTO e : list5) {
                applicationVersionValueMapper.delete(e)
            }
        }
        // 删除envCommand
        List<DevopsEnvCommandDTO> list6 = devopsEnvCommandMapper.selectAll()
        if (list6 != null && !list6.isEmpty()) {
            for (DevopsEnvCommandDTO e : list6) {
                devopsEnvCommandMapper.delete(e)
            }
        }

        //删除gitlabPipeline
        List<DevopsGitlabPipelineDTO> list7 = devopsGitlabPipelineMapper.selectAll()
        if (list7 != null && !list7.isEmpty()) {
            for (DevopsGitlabPipelineDTO e : list7) {
                devopsGitlabPipelineMapper.delete(e)
            }
        }

        //删除gitlabCommit
        List<DevopsGitlabCommitDTO> list8 = devopsGitlabCommitMapper.selectAll()
        if (list8 != null && !list8.isEmpty()) {
            for (DevopsGitlabCommitDTO e : list8) {
                devopsGitlabCommitMapper.delete(e)
            }
        }

    }
}
