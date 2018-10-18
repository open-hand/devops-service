package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsEnvGroupDTO
import io.choerodon.devops.infra.dataobject.DevopsEnvGroupDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.mapper.DevopsEnvGroupMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/7
 * Time: 16:33
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsEnvGroupControllerSpec extends Specification {

    private static flag = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper

    private DevopsEnvGroupDO devopsEnvGroupDO
    private DevopsEnvGroupDO devopsEnvGroupDO1

    def setup() {
        if (flag == 0) {
            devopsEnvGroupDO = new DevopsEnvGroupDO()
            devopsEnvGroupDO.setName("test")
            devopsEnvGroupDO.setProjectId(1L)
            devopsEnvGroupDO.setSequence(1L)
            devopsEnvGroupDO.setObjectVersionNumber(1L)

            devopsEnvGroupDO1 = new DevopsEnvGroupDO()
            devopsEnvGroupDO1.setName("test1")
            devopsEnvGroupDO1.setProjectId(1L)
            devopsEnvGroupDO1.setSequence(2L)
            devopsEnvGroupDO1.setObjectVersionNumber(1L)
            devopsEnvGroupMapper.insert(devopsEnvGroupDO)
            devopsEnvGroupMapper.insert(devopsEnvGroupDO1)

            flag = 1
        }
    }

    def "Create"() {
        when:
        def envDTO = restTemplate.postForObject("/v1/projects/1/env_groups?devopsEnvGroupName=test2", null, DevopsEnvGroupDTO.class)

        then:
        envDTO.getId() != null
    }

    def "Update"() {
        given:
        DevopsEnvGroupDTO devopsEnvGroupDTO = new DevopsEnvGroupDTO()
        devopsEnvGroupDTO.setId(1L)
        devopsEnvGroupDTO.setName("name")

        when:
        restTemplate.put("/v1/projects/1/env_groups", devopsEnvGroupDTO, DevopsEnvGroupDTO.class)

        then:
        devopsEnvGroupMapper.selectByPrimaryKey(1L).getName().equals("name")
    }

    def "ListByProject"() {
        when:
        def devopsEnvGroups = restTemplate.getForObject("/v1/projects/1/env_groups", List.class)

        then:
        devopsEnvGroups.size() == 3

    }

    def "CheckName"() {
        when:
        def isUnique = restTemplate.getForObject("/v1/projects/1/env_groups/checkName?name=test", Boolean.class)

        then:
        isUnique == true
    }

    def "Delete"() {
        given:
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setActive(true)
        devopsEnvironmentDO.setSequence(1L)
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)
        DevopsEnvironmentDO devopsEnvironmentDO1 = new DevopsEnvironmentDO()
        devopsEnvironmentDO1.setProjectId(1L)
        devopsEnvironmentDO1.setActive(true)
        devopsEnvironmentDO1.setSequence(2L)
        devopsEnvironmentDO1.setDevopsEnvGroupId(1L)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO1)

        when:
        restTemplate.delete("/v1/projects/1/env_groups/1")

        then:
        devopsEnvGroupMapper.selectByPrimaryKey(1L) == null
        devopsEnvironmentMapper.deleteByPrimaryKey(1L)
        devopsEnvironmentMapper.deleteByPrimaryKey(2L)
        true
    }
}
