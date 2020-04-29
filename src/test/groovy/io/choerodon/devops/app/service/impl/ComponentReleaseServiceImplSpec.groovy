package io.choerodon.devops.app.service.impl

import java.util.function.Consumer

import org.powermock.api.mockito.PowerMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.asgard.saga.dto.StartInstanceDTO
import io.choerodon.asgard.saga.producer.StartSagaBuilder
import io.choerodon.asgard.saga.producer.TransactionalProducer
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.eventhandler.DevopsSagaHandler
import io.choerodon.devops.app.service.ComponentReleaseService
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO
import io.choerodon.devops.infra.dto.AppServiceVersionDTO
import io.choerodon.devops.infra.dto.DevopsClusterDTO
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO
import io.choerodon.devops.infra.dto.DevopsEnvCommandValueDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.enums.*
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper
import io.choerodon.devops.infra.mapper.DevopsClusterMapper
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper
import io.choerodon.devops.infra.mapper.DevopsEnvCommandValueMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.devops.infra.util.ComponentVersionUtil
import io.choerodon.devops.infra.util.CustomContextUtil

/**
 *
 * @author zmf
 * @since 11/29/19
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Subject(ComponentReleaseServiceImpl)
@Import(IntegrationTestConfiguration)
@Stepwise
class ComponentReleaseServiceImplSpec extends Specification {
    private static final Long INSTANCE_ID = 1L
    private static final Long COMMAND_ID = 10000L

    @Autowired
    private TransactionalProducer transactionalProducer
    @Autowired
    private DevopsSagaHandler devopsSagaHandler
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;
    @Autowired
    private ComponentReleaseService componentReleaseService

    @Autowired
    @Qualifier("mockBaseServiceClientOperator")
    private BaseServiceClientOperator baseServiceClientOperator

    @Autowired
    @Qualifier("mockGitlabServiceClientOperator")
    private GitlabServiceClientOperator gitlabServiceClientOperator

    @Autowired
    private DevopsEnvCommandValueMapper devopsEnvCommandValueMapper

    @Autowired
    private DevopsClusterMapper devopsClusterMapper

    private static boolean isToInit = true

    private static boolean isToClean = false

    void setup() {
        if (!isToInit) {
            return
        }

        mockData()

        PowerMockito.when(baseServiceClientOperator.isProjectOwner(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong())).thenReturn(true)
        PowerMockito.when(gitlabServiceClientOperator.getFile(anyInt(), anyString(), anyString())).thenReturn(false)
    }

    void cleanup() {
        if (!isToClean) {
            return
        }

        appServiceInstanceMapper.delete(null)
        devopsEnvCommandMapper.delete(null)
        devopsEnvironmentMapper.delete(null)
        devopsEnvCommandValueMapper.delete(null)
        devopsClusterMapper.delete(null)
    }

    void mockData() {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO()
        devopsClusterDTO.setId(1000L)
        devopsClusterDTO.setCode("cluster")
        devopsClusterDTO.setSystemEnvId(10000L)
        devopsClusterMapper.insertSelective(devopsClusterDTO)

        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()
        devopsEnvironmentDTO.setId(10000L)
        devopsEnvironmentDTO.setClusterId(devopsClusterDTO.getId())
        devopsEnvironmentDTO.setCode("env-code")
        devopsEnvironmentDTO.setDevopsEnvGroupId(1000L)
        devopsEnvironmentDTO.setType(EnvironmentType.SYSTEM.getValue())
        devopsEnvironmentMapper.insertSelective(devopsEnvironmentDTO)

        DevopsEnvCommandValueDTO valueDTO = new DevopsEnvCommandValueDTO()
        valueDTO.setValue("value: test")
        devopsEnvCommandValueMapper.insertSelective(valueDTO)

        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO()
        devopsEnvCommandDTO.setId(COMMAND_ID)
        devopsEnvCommandDTO.setValueId(valueDTO.getId())
        devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType())
        devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus())
        devopsEnvCommandDTO.setObject(ObjectType.INSTANCE.getType())
        devopsEnvCommandDTO.setObjectId(INSTANCE_ID)
        devopsEnvCommandDTO.setError("error")
        devopsEnvCommandMapper.insertSelective(devopsEnvCommandDTO)

        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO()
        appServiceInstanceDTO.setId(INSTANCE_ID)
        appServiceInstanceDTO.setEnvId(devopsEnvironmentDTO.getId())
        appServiceInstanceDTO.setCode("code-i")
        appServiceInstanceDTO.setCommandId(devopsEnvCommandDTO.getId())
        appServiceInstanceDTO.setStatus(InstanceStatus.FAILED.getStatus())

        AppServiceVersionDTO componentVersion = ComponentVersionUtil.getComponentVersion(ClusterResourceType.PROMETHEUS)
        appServiceInstanceDTO.setComponentChartName(componentVersion.getChartName())
        appServiceInstanceDTO.setCommandVersion(componentVersion.getVersion())
        appServiceInstanceMapper.insertSelective(appServiceInstanceDTO)
    }


    def "RetryPushingToGitLab"() {
        given: "准备"
        isToInit = false
        PowerMockito.when(transactionalProducer.apply((StartSagaBuilder) any(StartSagaBuilder), (Consumer<StartSagaBuilder>) any(Consumer)))
                .then({
            println("mocking saga...")
            StartSagaBuilder builder = (StartSagaBuilder) it.getArgument(0)
            StartInstanceDTO instanceDTO = (StartInstanceDTO) builder['startInstanceDTO']
            devopsSagaHandler.devopsCreateInstance(instanceDTO.input)
        })
        CustomContextUtil.setUserContext(1L)

        when: "调用"
        componentReleaseService.retryPushingToGitLab(INSTANCE_ID, ClusterResourceType.PROMETHEUS)

        then: "无异常"
        noExceptionThrown()
        // mock静态方法后才能验证
//        appServiceInstanceMapper.selectByPrimaryKey(INSTANCE_ID).getStatus() == InstanceStatus.OPERATING.getStatus()
//        devopsEnvCommandMapper.selectByPrimaryKey(COMMAND_ID).getStatus() == CommandStatus.OPERATING.getStatus()
//        devopsEnvCommandMapper.selectByPrimaryKey(COMMAND_ID).getError() == null
    }

    def "RestartComponentInstance"() {
        given: "准备"
        isToClean = true
        PowerMockito.when(transactionalProducer.apply((StartSagaBuilder) any(StartSagaBuilder), (Consumer<StartSagaBuilder>) any(Consumer)))
                .then({
            println("mocking saga...")
            StartSagaBuilder builder = (StartSagaBuilder) it.getArgument(0)
            StartInstanceDTO instanceDTO = (StartInstanceDTO) builder['startInstanceDTO']
            devopsSagaHandler.devopsCreateInstance(instanceDTO.input)
        })
        CustomContextUtil.setUserContext(1L)

        when: "调用"
        componentReleaseService.retryPushingToGitLab(INSTANCE_ID, ClusterResourceType.PROMETHEUS)

        then: "无异常"
        noExceptionThrown()
    }
}
