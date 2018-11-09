package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DeployVersionDTO
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO
import io.choerodon.devops.infra.dataobject.ApplicationVersionDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/17
 * Time: 13:43
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ApplicationVersionController)
@Stepwise
class ApplicationVersionControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private ApplicationVersionMapper applicationVersionMapper
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository

    @Shared
    Long project_id = 1L
    @Shared
    Long init_id = 1L

    @Shared
    Map<String, Object> searchParam = new HashMap<>();

    def setupSpec() {
        given: '初始化分页条件参数'
        Map<String, Object> params = new HashMap<>();
        params.put("version", [])
        params.put("appName", [])
        params.put("appCode", ["app"])
        searchParam.put("searchParam", params)
        searchParam.put("param", "")
    }

    // 分页查询应用版本
    def "PageByOptions"() {
        given: '添加应用'
        ApplicationDO applicationDO = new ApplicationDO()
        applicationDO.setId(init_id)
        applicationDO.setName("app_name")
        applicationDO.setCode("app_code")
        applicationDO.setProjectId(project_id)
        applicationDO.setAppTemplateId(init_id)
        applicationDO.setGitlabProjectId(1)
        applicationMapper.insert(applicationDO)

        and: '添加应用版本'
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
        applicationVersionDO.setId(init_id)
        applicationVersionDO.setVersion("0.1.0-dev.20180521111826")
        applicationVersionDO.setAppId(init_id)
        applicationVersionDO.setIsPublish(1)
        applicationVersionMapper.insert(applicationVersionDO)

        and: '添加应用运行实例'
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO();
        applicationInstanceDO.setId(init_id)
        applicationInstanceDO.setCode("spock-test")
        applicationInstanceDO.setStatus("running")
        applicationInstanceDO.setAppId(init_id)
        applicationInstanceDO.setAppVersionId(init_id)
        applicationInstanceDO.setEnvId(init_id)
        applicationInstanceDO.setCommandId(init_id)
        applicationInstanceMapper.insert(applicationInstanceDO)

        and: '添加环境'
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
        devopsEnvironmentDO.setId(init_id)
        devopsEnvironmentDO.setCode("spock-test")
        devopsEnvironmentDO.setGitlabEnvProjectId(init_id)
        devopsEnvironmentDO.setHookId(init_id)
        devopsEnvironmentDO.setDevopsEnvGroupId(init_id)
        devopsEnvironmentDO.setProjectId(init_id)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)

        when: '分页查询应用版本'
        def page = restTemplate.postForObject("/v1/projects/{project_id}/app_version/list_by_options?appId={app_id}", searchParam, Page.class, project_id, init_id)

        then: '返回值'
        page.size() == 1

        expect: '校验返回结果'
        page.get(0).version == "0.1.0-dev.20180521111826"
    }

    // 应用下查询应用所有版本
    def "QueryByAppId"() {
        when: '应用下查询应用所有版本'
        def list = restTemplate.getForObject("/v1/projects/{project_id}/apps/{app_id}/version/list?is_publish=true", List.class, project_id, init_id)

        then: '返回值'
        list.size() == 1

        expect: '校验返回结果'
        list.get(0).version == "0.1.0-dev.20180521111826"
    }

    // 项目下查询应用所有已部署版本
    def "QueryDeployedByAppId"() {
        when: '项目下查询应用所有已部署版本'
        def list = restTemplate.getForObject("/v1/projects/{project_id}/apps/{app_id}/version/list_deployed", List.class, project_id, init_id)

        then: '返回值'
        list.size() == 1

        expect: '校验返回结果'
        list.get(0).version == "0.1.0-dev.20180521111826"
    }

    // 查询部署在某个环境的应用版本
    def "QueryByAppIdAndEnvId"() {
        when: '查询部署在某个环境的应用版本'
        def list = restTemplate.getForObject("/v1/projects/1/apps/1/version?envId=1", List.class)

        then: '返回值'
        list.size() == 1

        expect: '校验返回结果'
        list.get(0).version == "0.1.0-dev.20180521111826"
    }

    // 分页查询某应用下的所有版本
    def "PageByApp"() {
        when: '分页查询某应用下的所有版本'
        def page = restTemplate.postForObject("/v1/projects/{project_id}/apps/{app_id}/version/list_by_options", null, Page.class, project_id, init_id)

        then: '返回值'
        page.size() == 1

        expect: '校验返回结果'
        page.get(0).version == "0.1.0-dev.20180521111826"
    }

    // 根据应用版本ID查询，可升级的应用版本
    def "GetUpgradeAppVersion"() {
        given: '初始化应用版本DO类'
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()
        applicationVersionDO.setId(2L)
        applicationVersionDO.setVersion("0.2.0-dev.20180521111826")
        applicationVersionDO.setAppId(init_id)
        applicationVersionMapper.insert(applicationVersionDO)

        when: '根据应用版本ID查询，可升级的应用版本'
        def list = restTemplate.getForObject("/v1/projects/1/version/1/upgrade_version", List.class)

        then: '返回值'
        list.size() == 1

        expect: '校验返回结果'
        list.get(0).version == "0.2.0-dev.20180521111826"
    }

    def "GetDeployVersions"() {
        when: '项目下查询应用最新的版本和各环境下部署的版本'
        def dto = restTemplate.getForObject("/v1/projects/1/deployVersions?app_id=1", DeployVersionDTO.class)

        then: '校验返回结果'
        dto["latestVersion"] == "0.2.0-dev.20180521111826"
    }

    // 清除测试数据
    def "cleanupData"() {
        given:
        applicationInstanceMapper.deleteByPrimaryKey(init_id)
        devopsEnvironmentMapper.deleteByPrimaryKey(init_id)
        applicationMapper.deleteByPrimaryKey(init_id)
        applicationVersionMapper.deleteByPrimaryKey(1L)
        applicationVersionMapper.deleteByPrimaryKey(2L)
    }
}
