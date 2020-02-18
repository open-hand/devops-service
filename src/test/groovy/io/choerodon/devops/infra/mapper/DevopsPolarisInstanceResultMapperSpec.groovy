package io.choerodon.devops.infra.mapper

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.InstanceWithPolarisResultVO
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO
import io.choerodon.devops.infra.dto.DevopsPolarisInstanceResultDTO
import io.choerodon.devops.infra.dto.DevopsPolarisResultDetailDTO

/**
 *
 * @author zmf
 * @since 2/18/20
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Subject(DevopsPolarisInstanceResultMapper)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsPolarisInstanceResultMapperSpec extends Specification {
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper
    @Autowired
    private AppServiceMapper appServiceMapper
    @Autowired
    private DevopsPolarisInstanceResultMapper devopsPolarisInstanceResultMapper
    @Autowired
    private DevopsPolarisResultDetailMapper detailMapper

    private static boolean isToInit = true
    private static boolean isToClean = false
    private static final Long ENV_ID = 1L
    private static final RECORD_ID = 1L
    private static final APP_SERVICE_ID = 1L
    private static final APP_SERVICE_INSTANCE_ID = 1L

    def setup() {
        if (!isToInit) {
            return
        }
        AppServiceDTO appServiceDTO = new AppServiceDTO()
        appServiceDTO.setId(APP_SERVICE_ID)
        appServiceDTO.setCode("app")
        appServiceDTO.setName("app-name")
        appServiceMapper.insertSelective(appServiceDTO)

        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO()
        appServiceInstanceDTO.setId(APP_SERVICE_INSTANCE_ID)
        appServiceInstanceDTO.setCode("instance-code")
        appServiceInstanceDTO.setAppServiceId(APP_SERVICE_ID)
        appServiceInstanceDTO.setEnvId(ENV_ID)
        appServiceInstanceMapper.insertSelective(appServiceInstanceDTO)

        DevopsPolarisInstanceResultDTO devopsPolarisInstanceResultDTO = new DevopsPolarisInstanceResultDTO()
        devopsPolarisInstanceResultDTO.setEnvId(ENV_ID)
        devopsPolarisInstanceResultDTO.setRecordId(RECORD_ID)
        devopsPolarisInstanceResultDTO.setInstanceId(APP_SERVICE_INSTANCE_ID)

        DevopsPolarisResultDetailDTO detailDTO = new DevopsPolarisResultDetailDTO()

        devopsPolarisInstanceResultDTO.setResourceName("dep1")
        devopsPolarisInstanceResultDTO.setResourceKind("Deployment")
        detailDTO.setDetail("one")
        detailMapper.insertSelective(detailDTO)

        devopsPolarisInstanceResultDTO.setDetailId(detailDTO.getId())
        devopsPolarisInstanceResultMapper.insertSelective(devopsPolarisInstanceResultDTO)

        devopsPolarisInstanceResultDTO.setId(null)
        devopsPolarisInstanceResultDTO.setResourceName("dep2")
        devopsPolarisInstanceResultDTO.setResourceKind("Deployment")
        detailDTO.setId(null)
        detailDTO.setDetail("two")
        detailMapper.insertSelective(detailDTO)

        devopsPolarisInstanceResultDTO.setDetailId(detailDTO.getId())
        devopsPolarisInstanceResultMapper.insertSelective(devopsPolarisInstanceResultDTO)


        devopsPolarisInstanceResultDTO.setId(null)
        devopsPolarisInstanceResultDTO.setResourceName("dep3")
        devopsPolarisInstanceResultDTO.setResourceKind("Deployment")
        detailDTO.setId(null)
        detailDTO.setDetail("three")
        detailMapper.insertSelective(detailDTO)

        devopsPolarisInstanceResultDTO.setDetailId(detailDTO.getId())
        devopsPolarisInstanceResultMapper.insertSelective(devopsPolarisInstanceResultDTO)
    }

    def cleanup() {
        if (!isToClean) {
            true
        }
        appServiceInstanceMapper.delete(null)
        appServiceMapper.delete(null)
        devopsPolarisInstanceResultMapper.delete(null)
        detailMapper.delete(null)
    }

    def "QueryInstanceWithResult"() {
        given:
        isToInit = false
        isToClean = true

        when:
        List<InstanceWithPolarisResultVO> list = devopsPolarisInstanceResultMapper.queryInstanceWithResult(RECORD_ID, ENV_ID)

        then:
        list.size() == 1
        list.get(0).getResultJson().size() == 3
    }
}
