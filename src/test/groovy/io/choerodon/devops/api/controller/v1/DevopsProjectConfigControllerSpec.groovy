package io.choerodon.devops.api.controller.v1


import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsProjectConfigDTO
import io.choerodon.devops.api.dto.ProjectConfigDTO
import io.choerodon.devops.app.service.ApplicationService
import io.choerodon.devops.app.service.ProjectConfigHarborService
import io.choerodon.devops.infra.mapper.DevopsProjectConfigMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author zongw.lee@gmail.com 
 * @since 2019/03/15
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsProjectConfigController)
@Stepwise
class DevopsProjectConfigControllerSpec extends Specification {

    private static final String MAPPING = "/v1/projects/{project_id}/project_config"

    @Shared
    Long projectId = 999L
    @Shared
    List<Long> configIds = new ArrayList<>()

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    @Qualifier("mockProjectConfigHarborService")
    private ProjectConfigHarborService configHarborService

    @Autowired
    @Qualifier("mockApplicationService")
    private ApplicationService applicationService

    @Autowired
    DevopsProjectConfigMapper configMapper


    def "Create"() {
        given: '初始化数据'
        ProjectConfigDTO configDTO = new ProjectConfigDTO(url: "https://registry.saas.hand-china.com",
                userName: "admin", password: "Handhand123", email: "zhuang.chang@hand-china.com")
        ProjectConfigDTO configDTO2 = new ProjectConfigDTO(url: "http://helm-charts.staging.saas.hand-china.com/")

        DevopsProjectConfigDTO harborProjectConfigDTO = new DevopsProjectConfigDTO(name: "newTestHarborCreate", type: "harbor", config: configDTO)
        DevopsProjectConfigDTO chartprojectConfigDTO = new DevopsProjectConfigDTO(name: "newTestChartCreate", type: "chart", config: configDTO2)

        when: '创建项目配置'
        def res = restTemplate.postForEntity(MAPPING, harborProjectConfigDTO, DevopsProjectConfigDTO.class, projectId)

        then:"mock"
        1 * applicationService.checkHarborIsUsable(_,_,_,_,_) >> true
        1 * configHarborService.createHarbor(_,_)

        then: '校验结果'
        res.statusCode.is2xxSuccessful()
        def check = configMapper.selectByPrimaryKey(res.getBody())
        check.name.equals("newTestHarborCreate")
        check.projectId == projectId
        configIds.add(res.getBody().getId())

        when: '创建项目配置'
        res = restTemplate.postForEntity(MAPPING, chartprojectConfigDTO, DevopsProjectConfigDTO.class, projectId)

        then:"mock"
        1 * applicationService.checkChartIsUsable(_) >> true

        then: '校验结果'
        res.statusCode.is2xxSuccessful()
        def check2 = configMapper.selectByPrimaryKey(res.getBody())
        check2.name.equals("newTestChartCreate")
        check2.projectId == projectId
        configIds.add(res.getBody().getId())

        when: '测试创建项目配置时候名称重复'
        def resultFailure = restTemplate.postForEntity(MAPPING, harborProjectConfigDTO, DevopsProjectConfigDTO.class, projectId)

        then:"mock"
        1 * applicationService.checkHarborIsUsable(_,_,_,_,_) >> true

        then: '校验结果'
        resultFailure.statusCode.is2xxSuccessful()

        when: '测试创建项目配置时候类型输入错误'
        resultFailure = restTemplate.postForEntity(MAPPING, harborProjectConfigDTO, DevopsProjectConfigDTO.class, projectId)

        then:"mock"
        1 * applicationService.checkHarborIsUsable(_,_,_,_,_) >> true

        then: '校验结果'
        resultFailure.statusCode.is2xxSuccessful()
    }

    def "Update"() {
        given: '初始化数据'
        DevopsProjectConfigDTO projectConfigDTO = new DevopsProjectConfigDTO(id: configIds.get(0),name: "newTestHarborUpdate",objectVersionNumber: 1L)

        when: '修改项目配置'
        restTemplate.put(MAPPING, projectConfigDTO, projectId)

        then: '校验结果'
        def check = configMapper.selectByPrimaryKey(projectConfigDTO)
        check.name.equals("newTestHarborUpdate")
        check.projectId == projectId
    }

    def "PageByOptions"() {
        given: '初始化数据'
//        PageRequest pageRequest = new PageRequest(page: 0, size: 1, sort: new Sort("name", "desc"))
//        String params = "{'searchParam': {'url': ['https:']}, param: ''}";

        when: '查询项目配置'
        def res = restTemplate.postForEntity(MAPPING + "/list_by_options?page={page}&size={size}&sort={sort}", null, Page.class, projectId, 0, 2, "name,desc")

        then: '校验结果'
        res.statusCode.is2xxSuccessful()
        res.body.size() == 2
    }

    def "QueryByPrimaryKey"() {
        when: '根据ID查找项目配置'
        def res = restTemplate.getForEntity(MAPPING+"/{project_config_id}",DevopsProjectConfigDTO.class, projectId,configIds.get(0))

        then: '校验结果'
        res.body.name.equals("newTestHarborUpdate")
        res.body.projectId == projectId
    }

    def "QueryByIdAndType"() {
        when: '通过项目ID和配置类型查询配置'
        def res = restTemplate.getForEntity(MAPPING+"/type?type={type}",List.class, projectId,"harbor")

        then: '校验结果'
        res.body.size() >= 2
    }

    def "checkIsUsed"() {
        when: '检查是否项目配置正在被使用'
        def res = restTemplate.getForEntity(MAPPING+"/{project_config_id}/check", Boolean.class,projectId,configIds.get(0))

        then: '校验结果'
        res.statusCode.is2xxSuccessful()
        res.getBody()
    }

    def "DeleteByProjectConfigId"() {
        given:
        DevopsProjectConfigDTO configDTO1 = new DevopsProjectConfigDTO(id:configIds.get(0))
        DevopsProjectConfigDTO configDTO2 = new DevopsProjectConfigDTO(id:configIds.get(1))

        when: '删除项目配置'
        restTemplate.delete(MAPPING+"/{project_config_id}", projectId,configIds.get(0))

        then: '校验结果'
        def check = configMapper.selectByPrimaryKey(configDTO1)
        check == null

        when: '删除项目配置'
        restTemplate.delete(MAPPING+"/{project_config_id}", projectId,configIds.get(1))

        then: '校验结果'
        def check2 = configMapper.selectByPrimaryKey(configDTO2)
        check2 == null
    }
}
