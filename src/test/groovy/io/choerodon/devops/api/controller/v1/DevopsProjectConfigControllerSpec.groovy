package io.choerodon.devops.api.controller.v1


import io.choerodon.core.convertor.ConvertHelper
import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsProjectConfigVO
import io.choerodon.devops.api.vo.ProjectConfigVO
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectConfigE
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository
import io.choerodon.devops.infra.feign.HarborClient
import org.mockito.Mockito
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
 * Created by Sheep on 2019/4/9.
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsProjectConfigController)
@Stepwise
class DevopsProjectConfigControllerSpec extends Specification {

    HarborClient harborClient = Mockito.mock(HarborClient.class)

    private static final String MAPPING = "/v1/projects/{project_id}/project_config"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsProjectConfigRepository devopsProjectConfigRepository
    @Shared
    Long project_id = 1L

    //创建配置
    def "Create"() {
        DevopsProjectConfigVO devopsProjectConfigDTO = new DevopsProjectConfigVO()
        devopsProjectConfigDTO.setName("test")
        devopsProjectConfigDTO.setType("chart")
        devopsProjectConfigDTO.setProjectId(project_id)
        ProjectConfigVO projectConfigDTO = new ProjectConfigVO()
        projectConfigDTO.setEmail("test")
        projectConfigDTO.setPassword("test")
        projectConfigDTO.setPrivate(true)
        projectConfigDTO.setProject("test")
        projectConfigDTO.setUrl("http://chart.choerodon.com.cn")
        projectConfigDTO.setUserName("test")
        devopsProjectConfigDTO.setConfig(projectConfigDTO)

        when: '创建配置'
        def entity = restTemplate.postForEntity(MAPPING, devopsProjectConfigDTO, DevopsProjectConfigVO.class, project_id)

        then:
        entity.getBody().getName().equals("test")
    }

    //校验名字是否存在
    def "CheckName"() {
        when: '创建应用校验名称是否存在'
        def entity = restTemplate.getForEntity(MAPPING + "/check_name?name=test", Object, 1L)

        then: '名字存在抛出异常'
        entity.getBody().getAt("failed") == true
    }


    //更新配置
    def "Update"() {
        DevopsProjectConfigE devopsProjectConfigE = devopsProjectConfigRepository.baseQuery(3L)
        DevopsProjectConfigVO devopsProjectConfigDTO = ConvertHelper.convert(devopsProjectConfigE, DevopsProjectConfigVO.class)
        devopsProjectConfigDTO.setName("testnew")

        when:
        restTemplate.put(MAPPING, devopsProjectConfigDTO, project_id)

        then:
        devopsProjectConfigRepository.baseQuery(3L).getName().equals("testnew")

    }

    //分页查询配置
    def "PageByOptions"() {
        when:
        def page = restTemplate.postForObject(MAPPING + "/list_by_options", null, Page.class, 1L)

        then:
        page.size() == 3
    }


    //根据id查询配置
    def "QueryByPrimaryKey"() {
        when:
        def object = restTemplate.getForObject(MAPPING + "/{project_config_id}", DevopsProjectConfigVO.class, 1, 3)

        then:
        object.getName().equals("testnew")
    }

    //根据类型查询配置列表
    def "QueryByIdAndType"() {
        when:
        def list = restTemplate.getForObject(MAPPING + "/type?type=chart", List.class, 1)

        then:
        list.size() == 2

    }


    //检查配置是否被使用过
    def "CheckIsUsed"() {

        when:
        def bool = restTemplate.getForObject(MAPPING + "/{project_config_id}/check", Boolean.class, 1, 3)

        then:
        bool == true
    }


    //删除配置
    def "DeleteByProjectConfigId"() {
        when:
        restTemplate.delete(MAPPING + "/{project_config_id}", 1, 3)

        then:
        devopsProjectConfigRepository.baseQuery(3) == null

    }
}
