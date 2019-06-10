package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.infra.common.util.enums.InstanceStatus

import io.choerodon.devops.infra.dataobject.DevopsEnvPodDO

import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/10
 * Time: 10:36
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvPodContainerController)
@Stepwise
class DevopsEnvPodContainerControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper

//    def "QueryLogByPod"() {
//        given: '初始化envPodContainerDO类'
////        DevopsEnvPodContainerDO devopsEnvPodContainerDO = new DevopsEnvPodContainerDO()
//        devopsEnvPodContainerDO.setId(1L)
//        devopsEnvPodContainerDO.setPodId(1L)
//        devopsEnvPodContainerDO.setContainerName("test1")
//        devopsEnvPodContainerMapper.insert(devopsEnvPodContainerDO)
//
//        DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO()
//        devopsEnvPodDO.setId(1L)
//        devopsEnvPodDO.setName("name")
//        devopsEnvPodMapper.insert(devopsEnvPodDO)
//        InstanceStatus
//
//        when: '获取日志信息 By Pod'
//        def list = restTemplate.getForObject("/v1/projects/1/app_pod/1/containers/logs", List.class)
//
//        then: '校验返回结果'
//        list.size() == 1
//    }

//    def "HandleShellByPod"() {
//        when: '获取日志shell信息 By Pod'
//        def list = restTemplate.getForObject("/v1/projects/1/app_pod/1/containers/logs/shell", List.class)
//
//        then: '校验返回结果'
//        list.size() == 1
//
//        and: '清理数据'
//        // 删除envPodContainer
//        List<DevopsEnvPodContainerDO> list1 = devopsEnvPodContainerMapper.selectAll()
//        if (list1 != null && !list1.isEmpty()) {
//            for (DevopsEnvPodContainerDO e : list1) {
//                devopsEnvPodContainerMapper.delete(e)
//            }
//        }
//        // 删除envPod
//        List<DevopsEnvPodDO> list2 = devopsEnvPodMapper.selectAll()
//        if (list2 != null && !list2.isEmpty()) {
//            for (DevopsEnvPodDO e : list2) {
//                devopsEnvPodMapper.delete(e)
//            }
//        }
//    }
}
