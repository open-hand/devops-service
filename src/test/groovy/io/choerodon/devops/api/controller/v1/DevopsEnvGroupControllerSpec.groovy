package io.choerodon.devops.api.controller.v1

import com.alibaba.fastjson.JSONArray
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsEnvGroupVO
import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO
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
    private String rootUrl = "/v1/projects/{project_id}/env_groups"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper

    @Shared
    private DevopsEnvGroupDTO devopsEnvGroupDO = new DevopsEnvGroupDTO()
    @Shared
    private DevopsEnvGroupDTO devopsEnvGroupDO1 = new DevopsEnvGroupDTO()

    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false

    private static final PROJECT_ID = 1L

    def setup() {
        if (isToInit) {
            devopsEnvGroupDO.setId(1L)
            devopsEnvGroupDO.setName("test")
            devopsEnvGroupDO.setProjectId(PROJECT_ID)

            devopsEnvGroupDO1.setId(2L)
            devopsEnvGroupDO1.setName("test1")
            devopsEnvGroupDO1.setProjectId(PROJECT_ID)

            devopsEnvGroupMapper.insert(devopsEnvGroupDO)
            devopsEnvGroupMapper.insert(devopsEnvGroupDO1)
        }
    }

    def cleanup() {
        if (isToClean) {
            devopsEnvGroupMapper.delete(null)
        }
    }

    def "Create"() {
        given: '创建环境组'
        isToInit = false
        def url = rootUrl + "?name={name}"
        def groupName = "test-create"

        Map<String, Object> params = new HashMap<>()
        params.put("name", groupName)
        params.put("project_id", PROJECT_ID)

        when: '项目下创建环境组'
        DevopsEnvGroupVO groupVO = restTemplate.postForObject(url, null, DevopsEnvGroupVO.class, params)

        then: '校验返回结果'
        groupVO.getName() == groupName
        groupVO.getId() != null
        devopsEnvGroupMapper.selectByPrimaryKey(groupVO.getId()) != null
    }

    def "Update"() {
        given: '准备'
        def updateName = "test-update"
        devopsEnvGroupDO.setName(updateName)

        Map<String, Object> params = new HashMap<>()
        params.put("project_id", PROJECT_ID)

        when: '项目下更新环境组'
        restTemplate.put(rootUrl, devopsEnvGroupDO, params)

        then: '校验更新结果'
        devopsEnvGroupMapper.selectByPrimaryKey(devopsEnvGroupDO.getId()).getName() == updateName
    }

    def "ListByProject"() {
        given: '准备'
        def url = rootUrl + "/list_by_project"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", PROJECT_ID)

        when: '项目下查询环境组'
        def devopsEnvGroups = JSONArray.parseArray(restTemplate.getForObject(url, String.class, params), DevopsEnvGroupVO)

        then: '校验返回结果'
        devopsEnvGroups != null
        devopsEnvGroups.size() == 3
    }

    def "CheckName"() {
        given: "准备"
        def url = rootUrl + "/check_name?name={name}"
        Map<String, Object> params = new HashMap<>()
        params.put("name", devopsEnvGroupDO.getName())
        params.put("project_id", PROJECT_ID)

        when: '校验环境组名唯一性，除开组自身'
        def isUnique = restTemplate.getForObject(url, Boolean.class, params)

        then: '返回值'
        !isUnique


        when: '校验环境组名唯一性，除开组自身'
        url = rootUrl + "/check_name?name={name}&group_id={group_id}"
        params = new HashMap<>()
        params.put("name", devopsEnvGroupDO.getName())
        params.put("project_id", PROJECT_ID)
        params.put("group_id", devopsEnvGroupDO.getId())
        isUnique = restTemplate.getForObject(url, Boolean.class, params)

        then: '返回值'
        isUnique
    }

    def "Delete"() {
        given: '创建环境DO类'
        isToClean = true
        def url = rootUrl + "/{group_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("group_id", devopsEnvGroupDO.getId())
        params.put("project_id", PROJECT_ID)

        when: '环境组删除'
        restTemplate.delete(url, params)

        then: '验证是否删除'
        devopsEnvGroupMapper.selectByPrimaryKey(devopsEnvGroupDO.getId()) == null
    }
}
