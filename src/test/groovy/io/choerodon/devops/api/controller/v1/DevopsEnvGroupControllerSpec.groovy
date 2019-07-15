package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsEnvGroupVO
import io.choerodon.devops.infra.dataobject.DevopsEnvGroupDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.mapper.DevopsEnvGroupMapper
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
 * Date: 2018/9/7
 * Time: 16:33
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvGroupController)
@Stepwise
class DevopsEnvGroupControllerSpec extends Specification {

    private static flag = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    DevopsEnvGroupMapper devopsEnvGroupMapper
    @Autowired
    DevopsEnvironmentMapper devopsEnvironmentMapper

    @Shared
    DevopsEnvGroupDO devopsEnvGroupDO = new DevopsEnvGroupDO()
    @Shared
    DevopsEnvGroupDO devopsEnvGroupDO1 = new DevopsEnvGroupDO()

    def setupSpec() {
        devopsEnvGroupDO.setId(1L)
        devopsEnvGroupDO.setName("test")
        devopsEnvGroupDO.setProjectId(1L)
        devopsEnvGroupDO.setSequence(1L)
        devopsEnvGroupDO.setObjectVersionNumber(1L)

        devopsEnvGroupDO1.setId(2L)
        devopsEnvGroupDO1.setName("test1")
        devopsEnvGroupDO1.setProjectId(1L)
        devopsEnvGroupDO1.setSequence(2L)
        devopsEnvGroupDO1.setObjectVersionNumber(1L)

    }

    def "Create"() {
        given: '创建环境组'
        devopsEnvGroupMapper.insert(devopsEnvGroupDO)
        devopsEnvGroupMapper.insert(devopsEnvGroupDO1)

        when: '项目下创建环境组'
        def envDTO = restTemplate.postForObject("/v1/projects/1/env_groups?devopsEnvGroupName=test2", null, DevopsEnvGroupVO.class)

        then: '校验返回结果'
        envDTO["projectId"] == 1
    }

    def "Update"() {
        given: '初始化更新DTO类'
        DevopsEnvGroupVO devopsEnvGroupDTO = new DevopsEnvGroupVO()
        devopsEnvGroupDTO.setId(1L)
        devopsEnvGroupDTO.setName("name")

        when: '项目下更新环境组'
        restTemplate.put("/v1/projects/1/env_groups", devopsEnvGroupDTO, DevopsEnvGroupVO.class)

        then: '校验更新结果'
        devopsEnvGroupMapper.selectByPrimaryKey(1L)["name"] == "name"
    }

    def "ListByProject"() {
        when: '项目下查询环境组'
        def devopsEnvGroups = restTemplate.getForObject("/v1/projects/1/env_groups", List.class)

        then: '校验返回结果'
        devopsEnvGroups.size() == 3
    }

    def "CheckName"() {
        when: '校验环境组名唯一性'
        def isUnique = restTemplate.getForObject("/v1/projects/1/env_groups/checkName?name=test", Boolean.class)

        then: '返回值'
        isUnique
    }

    def "Delete"() {
        given: '创建环境DO类'
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setActive(true)
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)
        devopsEnvironmentDO.setSequence(1L)
        devopsEnvironmentDO.setCode("env")
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)
        DevopsEnvironmentDO devopsEnvironmentDO1 = new DevopsEnvironmentDO()
        devopsEnvironmentDO1.setId(2L)
        devopsEnvironmentDO1.setProjectId(1L)
        devopsEnvironmentDO1.setActive(true)
        devopsEnvironmentDO1.setCode("env1")
        devopsEnvironmentDO1.setGitlabEnvProjectId(1L)
        devopsEnvironmentDO1.setSequence(2L)
        devopsEnvironmentDO1.setDevopsEnvGroupId(1L)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO1)

        when: '环境组删除'
        restTemplate.delete("/v1/projects/1/env_groups/1")

        then: '验证是否删除'
        devopsEnvGroupMapper.selectByPrimaryKey(1L) == null

        and: '清理数据'
        // 删除envGroup
        List<DevopsEnvGroupDO> list = devopsEnvGroupMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (DevopsEnvGroupDO e : list) {
                devopsEnvGroupMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDO> list1 = devopsEnvironmentMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvironmentDO e : list1) {
                devopsEnvironmentMapper.delete(e)
            }
        }
    }
}
