package io.choerodon.devops.infra.mapper

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import java.text.SimpleDateFormat
import java.util.stream.Collectors

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.kubernetes.Command
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO
import io.choerodon.devops.infra.dto.CertificationDTO
import io.choerodon.devops.infra.dto.DevopsClusterDTO
import io.choerodon.devops.infra.dto.DevopsConfigMapDTO
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.DevopsIngressDTO
import io.choerodon.devops.infra.dto.DevopsPvDTO
import io.choerodon.devops.infra.dto.DevopsPvcDTO
import io.choerodon.devops.infra.dto.DevopsSecretDTO
import io.choerodon.devops.infra.dto.DevopsServiceDTO
import io.choerodon.devops.infra.enums.CommandStatus
import io.choerodon.devops.infra.enums.ObjectType
import io.choerodon.devops.infra.enums.PersistentVolumeType

/**
 * @author zmf
 * @since 11/26/19
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvCommandMapper)
@Stepwise
class DevopsEnvCommandMapperSpec extends Specification {
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper
    @Autowired
    private DevopsServiceMapper devopsServiceMapper
    @Autowired
    private DevopsIngressMapper devopsIngressMapper
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper
    @Autowired
    private DevopsConfigMapMapper devopsConfigMapMapper
    @Autowired
    private DevopsSecretMapper devopsSecretMapper
    @Autowired
    private DevopsPvMapper devopsPvMapper
    @Autowired
    private DevopsPvcMapper devopsPvcMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsClusterMapper devopsClusterMapper

    private static final Long ENV_ID = 12222L
    private static final Long CLUSTER_ID = 12222L
    private static final Date PASSED
    private static final Date CURRENT

    private static final List<Long> expectedCommandIds = Arrays.asList(12345L, 12347L, 12349L, 12351L, 12353L, 12355L, 12357L, 12359L, 12346L, 12348L, 12350L, 12352L, 12354L, 12356L, 12358L, 12360L)

    static {
        long current = System.currentTimeMillis()
        CURRENT = new Date(current)

        PASSED = new Date(current + (40 * 60 + 1) * 1000)
    }


    def "cleanup"() {
        cleanupData()
        println("clean up DevopsEnvCommandMapperSpec...")
    }


    def "listInstanceCommandsToSync"() {
        given: "mockData"
        mockData()
        String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(PASSED)

        when: "查询结果"
        List<Command> toSync = devopsEnvCommandMapper.listCommandsToSync(ENV_ID, dateString)

        then: "校验结果"
        toSync != null
        !toSync.isEmpty()
        toSync.stream().map({it.getId()}).collect(Collectors.toList()).containsAll(expectedCommandIds)
    }

    def mockData() {
        mockEnvAndCluster()
        mockAppServiceInstance()
        mockService()
        mockIngress()
        mockCertification()
        mockConfigMap()
        mockSecret()
        mockPv()
        mockPvc()
    }

    def mockEnvAndCluster() {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()
        devopsEnvironmentDTO.setId(ENV_ID)
        devopsEnvironmentDTO.setCode("env-code")
        devopsEnvironmentDTO.setSynchro(true)
        devopsEnvironmentDTO.setFailed(false)
        devopsEnvironmentDTO.setClusterId(CLUSTER_ID)
        devopsEnvironmentMapper.insertSelective(devopsEnvironmentDTO)

        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO()
        devopsClusterDTO.setId(CLUSTER_ID)
        devopsClusterDTO.setCode("cluster-1")
        devopsClusterMapper.insertSelective(devopsClusterDTO)
    }

    def mockAppServiceInstance() {
        AppServiceInstanceDTO instanceDTO1 = new AppServiceInstanceDTO()
        instanceDTO1.setId(12345L)
        instanceDTO1.setCode("code1")
        instanceDTO1.setCommandId(12345L)
        instanceDTO1.setEnvId(ENV_ID)
        appServiceInstanceMapper.insertSelective(instanceDTO1)

        DevopsEnvCommandDTO commandDTO = new DevopsEnvCommandDTO()
        commandDTO.setId(12345L)
        commandDTO.setObject(ObjectType.INSTANCE.getType())
        commandDTO.setObjectId(instanceDTO1.getId())
        commandDTO.setStatus(CommandStatus.OPERATING.getStatus())
        devopsEnvCommandMapper.insert(commandDTO)

        instanceDTO1.setId(12346L)
        instanceDTO1.setCode("code2")
        instanceDTO1.setCommandId(12346L)
        appServiceInstanceMapper.insertSelective(instanceDTO1)

        commandDTO.setId(12346L)
        commandDTO.setObject(ObjectType.INSTANCE.getType())
        commandDTO.setObjectId(instanceDTO1.getId())
        devopsEnvCommandMapper.insert(commandDTO)
    }

    def mockService() {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO()
        devopsServiceDTO.setId(12347L)
        devopsServiceDTO.setEnvId(ENV_ID)
        devopsServiceDTO.setName("code1")
        devopsServiceDTO.setCommandId(12347L)
        devopsServiceMapper.insertSelective(devopsServiceDTO)

        DevopsEnvCommandDTO commandDTO = new DevopsEnvCommandDTO()
        commandDTO.setId(12347L)
        commandDTO.setObject(ObjectType.SERVICE.getType())
        commandDTO.setObjectId(devopsServiceDTO.getId())
        commandDTO.setStatus(CommandStatus.OPERATING.getStatus())
        devopsEnvCommandMapper.insertSelective(commandDTO)

        devopsServiceDTO.setId(12348L)
        devopsServiceDTO.setName("code2")
        devopsServiceDTO.setCommandId(12348L)
        devopsServiceMapper.insertSelective(devopsServiceDTO)

        commandDTO.setId(12348L)
        commandDTO.setObject(ObjectType.SERVICE.getType())
        commandDTO.setObjectId(devopsServiceDTO.getId())
        devopsEnvCommandMapper.insertSelective(commandDTO)
    }

    def mockIngress() {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO()
        devopsIngressDTO.setId(12349L)
        devopsIngressDTO.setName("code1")
        devopsIngressDTO.setCommandId(12349L)
        devopsIngressDTO.setEnvId(ENV_ID)
        devopsIngressMapper.insertSelective(devopsIngressDTO)

        DevopsEnvCommandDTO commandDTO = new DevopsEnvCommandDTO()
        commandDTO.setId(12349L)
        commandDTO.setObject(ObjectType.INGRESS.getType())
        commandDTO.setObjectId(devopsIngressDTO.getId())
        commandDTO.setStatus(CommandStatus.OPERATING.getStatus())
        devopsEnvCommandMapper.insertSelective(commandDTO)

        devopsIngressDTO.setId(12350L)
        devopsIngressDTO.setName("code2")
        devopsIngressDTO.setCommandId(12350L)
        devopsIngressMapper.insertSelective(devopsIngressDTO)

        commandDTO.setId(12350L)
        commandDTO.setObject(ObjectType.INGRESS.getType())
        commandDTO.setObjectId(devopsIngressDTO.getId())
        devopsEnvCommandMapper.insertSelective(commandDTO)
    }

    def mockCertification () {
        CertificationDTO certificationDTO = new CertificationDTO()
        certificationDTO.setId(12351L)
        certificationDTO.setName("code1")
        certificationDTO.setCommandId(12351L)
        certificationDTO.setEnvId(ENV_ID)
        devopsCertificationMapper.insertSelective(certificationDTO)

        DevopsEnvCommandDTO commandDTO = new DevopsEnvCommandDTO()
        commandDTO.setId(12351L)
        commandDTO.setObject(ObjectType.CERTIFICATE.getType())
        commandDTO.setObjectId(certificationDTO.getId())
        commandDTO.setStatus(CommandStatus.OPERATING.getStatus())
        devopsEnvCommandMapper.insertSelective(commandDTO)

        certificationDTO.setId(12352L)
        certificationDTO.setName("code2")
        certificationDTO.setCommandId(12352L)
        devopsCertificationMapper.insertSelective(certificationDTO)

        commandDTO.setId(12352L)
        commandDTO.setObject(ObjectType.CERTIFICATE.getType())
        commandDTO.setObjectId(certificationDTO.getId())
        devopsEnvCommandMapper.insertSelective(commandDTO)
    }

    def mockConfigMap () {
        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO()
        devopsConfigMapDTO.setId(12353L)
        devopsConfigMapDTO.setName("code1")
        devopsConfigMapDTO.setCommandId(12353L)
        devopsConfigMapDTO.setEnvId(ENV_ID)
        devopsConfigMapMapper.insertSelective(devopsConfigMapDTO)

        DevopsEnvCommandDTO commandDTO = new DevopsEnvCommandDTO()
        commandDTO.setId(12353L)
        commandDTO.setObject(ObjectType.CONFIGMAP.getType())
        commandDTO.setObjectId(devopsConfigMapDTO.getId())
        commandDTO.setStatus(CommandStatus.OPERATING.getStatus())
        devopsEnvCommandMapper.insertSelective(commandDTO)

        devopsConfigMapDTO.setId(12354L)
        devopsConfigMapDTO.setName("code2")
        devopsConfigMapDTO.setCommandId(12354L)
        devopsConfigMapMapper.insertSelective(devopsConfigMapDTO)

        commandDTO.setId(12354L)
        commandDTO.setObject(ObjectType.CONFIGMAP.getType())
        commandDTO.setObjectId(devopsConfigMapDTO.getId())
        devopsEnvCommandMapper.insertSelective(commandDTO)
    }

    def mockSecret() {
        DevopsSecretDTO devopsSecretDTO = new DevopsSecretDTO()
        devopsSecretDTO.setId(12355L)
        devopsSecretDTO.setName("code1")
        devopsSecretDTO.setCommandId(12355L)
        devopsSecretDTO.setEnvId(ENV_ID)
        devopsSecretMapper.insertSelective(devopsSecretDTO)

        DevopsEnvCommandDTO commandDTO = new DevopsEnvCommandDTO()
        commandDTO.setId(12355L)
        commandDTO.setObject(ObjectType.SECRET.getType())
        commandDTO.setObjectId(devopsSecretDTO.getId())
        commandDTO.setStatus(CommandStatus.OPERATING.getStatus())
        devopsEnvCommandMapper.insertSelective(commandDTO)

        devopsSecretDTO.setId(12356L)
        devopsSecretDTO.setName("code2")
        devopsSecretDTO.setCommandId(12356L)
        devopsSecretMapper.insertSelective(devopsSecretDTO)

        commandDTO.setId(12356L)
        commandDTO.setObject(ObjectType.SECRET.getType())
        commandDTO.setObjectId(devopsSecretDTO.getId())
        devopsEnvCommandMapper.insertSelective(commandDTO)
    }

    def mockPv() {
        DevopsPvDTO devopsPvDTO = new DevopsPvDTO()
        devopsPvDTO.setId(12357L)
        devopsPvDTO.setName("code1")
        devopsPvDTO.setCommandId(12357L)
        devopsPvDTO.setClusterId(CLUSTER_ID)
        devopsPvDTO.setType(PersistentVolumeType.NFS.getType())
        devopsPvMapper.insertSelective(devopsPvDTO)

        DevopsEnvCommandDTO commandDTO = new DevopsEnvCommandDTO()
        commandDTO.setId(12357L)
        commandDTO.setObject(ObjectType.PERSISTENTVOLUME.getType())
        commandDTO.setObjectId(devopsPvDTO.getId())
        commandDTO.setStatus(CommandStatus.OPERATING.getStatus())
        devopsEnvCommandMapper.insertSelective(commandDTO)

        devopsPvDTO.setId(12358L)
        devopsPvDTO.setName("code2")
        devopsPvDTO.setCommandId(12358L)
        devopsPvMapper.insertSelective(devopsPvDTO)

        commandDTO.setId(12358L)
        commandDTO.setObject(ObjectType.PERSISTENTVOLUME.getType())
        commandDTO.setObjectId(devopsPvDTO.getId())
        devopsEnvCommandMapper.insertSelective(commandDTO)
    }

    def mockPvc() {
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO()
        devopsPvcDTO.setId(12359L)
        devopsPvcDTO.setName("code1")
        devopsPvcDTO.setCommandId(12359L)
        devopsPvcDTO.setEnvId(ENV_ID)
        devopsPvcDTO.setProjectId(1L)
        devopsPvcMapper.insertSelective(devopsPvcDTO)

        DevopsEnvCommandDTO commandDTO = new DevopsEnvCommandDTO()
        commandDTO.setId(12359L)
        commandDTO.setObject(ObjectType.PERSISTENTVOLUMECLAIM.getType())
        commandDTO.setObjectId(devopsPvcDTO.getId())
        commandDTO.setStatus(CommandStatus.OPERATING.getStatus())
        devopsEnvCommandMapper.insertSelective(commandDTO)

        devopsPvcDTO.setId(12360L)
        devopsPvcDTO.setName("code2")
        devopsPvcDTO.setCommandId(12360L)
        devopsPvcMapper.insertSelective(devopsPvcDTO)

        commandDTO.setId(12360L)
        commandDTO.setObject(ObjectType.PERSISTENTVOLUMECLAIM.getType())
        commandDTO.setObjectId(devopsPvcDTO.getId())
        devopsEnvCommandMapper.insertSelective(commandDTO)
    }

    def cleanupData() {
        devopsEnvironmentMapper.delete(null)
        devopsClusterMapper.delete(null)
        appServiceInstanceMapper.delete(null)
        devopsServiceMapper.delete(null)
        devopsIngressMapper.delete(null)
        devopsConfigMapMapper.delete(null)
        devopsSecretMapper.delete(null)
        devopsPvMapper.delete(null)
        devopsPvcMapper.delete(null)
        devopsEnvCommandMapper.delete(null)
    }
}
