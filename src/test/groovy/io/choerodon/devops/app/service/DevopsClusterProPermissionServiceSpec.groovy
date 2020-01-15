package io.choerodon.devops.app.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO
import io.choerodon.devops.infra.mapper.DevopsClusterProPermissionMapper

/**
 *
 * @author zmf
 * @since 20-1-13
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Stepwise
@Import(IntegrationTestConfiguration)
@Subject(DevopsClusterProPermissionService)
class DevopsClusterProPermissionServiceSpec extends Specification {
    @Autowired
    private DevopsClusterProPermissionService devopsClusterProPermissionService
    @Autowired
    private DevopsClusterProPermissionMapper devopsClusterProPermissionMapper
    private boolean isToInit = true
    private boolean isToClean = false

    private static final Long PROJECT_ID = 10L
    private static final Long CLUSTER_ID = 11L
    private DevopsClusterProPermissionDTO devopsClusterProPermissionDTO

    def setup() {
        if (!isToInit) {
            return
        }

        devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO()
        devopsClusterProPermissionDTO.setClusterId(CLUSTER_ID)
        devopsClusterProPermissionDTO.setProjectId(PROJECT_ID)
        devopsClusterProPermissionMapper.insert(devopsClusterProPermissionDTO)
    }

    def cleanup() {
        if (!isToClean) {
            return
        }

        devopsClusterProPermissionMapper.deleteByPrimaryKey(devopsClusterProPermissionDTO)
    }

    def "QueryPermission"() {
        given:
        isToInit = false
        isToClean = true

        when: "调用"
        def result = devopsClusterProPermissionService.queryPermission(PROJECT_ID, CLUSTER_ID)

        then: "校验结果"
        result != null

        when: "调用"
        devopsClusterProPermissionService.queryPermission(null, CLUSTER_ID)

        then: "校验结果"
        thrown(NullPointerException)

        when: "调用"
        devopsClusterProPermissionService.queryPermission(PROJECT_ID, null)

        then: "校验结果"
        thrown(NullPointerException)

        when: "调用"
        def result4 = devopsClusterProPermissionService.queryPermission(PROJECT_ID, CLUSTER_ID + 1)

        then: "校验结果"
        result4 == null
    }
}
