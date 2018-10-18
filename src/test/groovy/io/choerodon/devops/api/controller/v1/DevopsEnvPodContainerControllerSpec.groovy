package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.infra.dataobject.DevopsEnvPodContainerDO
import io.choerodon.devops.infra.dataobject.DevopsEnvPodDO
import io.choerodon.devops.infra.mapper.DevopsEnvPodContainerMapper
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/10
 * Time: 10:36
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsEnvPodContainerControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper
    @Autowired
    private DevopsEnvPodContainerMapper devopsEnvPodContainerMapper

    def "QueryLogByPod"() {
        given:
        DevopsEnvPodContainerDO devopsEnvPodContainerDO = new DevopsEnvPodContainerDO()
        devopsEnvPodContainerDO.setPodId(1L)
        devopsEnvPodContainerDO.setContainerName("test1")
        devopsEnvPodContainerMapper.insert(devopsEnvPodContainerDO)

        DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO()
        devopsEnvPodDO.setName("name")
        devopsEnvPodMapper.insert(devopsEnvPodDO)

        when:
        def list = restTemplate.getForObject("/v1/projects/1/app_pod/1/containers/logs", List.class)

        then:
        list.size() == 1
    }

    def "HandleShellByPod"() {
        when:
        def list = restTemplate.getForObject("/v1/projects/1/app_pod/1/containers/logs/shell", List.class)

        then:
        list.size() == 1
        devopsEnvPodContainerMapper.deleteByPrimaryKey(1L)
        devopsEnvPodMapper.deleteByPrimaryKey(1L)
    }
}
