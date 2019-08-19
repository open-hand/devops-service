package io.choerodon.devops.app.service.impl

import com.google.gson.Gson
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.PushWebHookVO
import io.choerodon.devops.app.service.*
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.gitlab.CompareResultDTO
import io.choerodon.devops.infra.dto.gitlab.DiffDTO
import io.choerodon.devops.infra.dto.gitlab.TagDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.CommandStatus
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.*
import io.choerodon.devops.infra.util.FileUtil
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsGitServiceImpl)
class GitlabWebHookServiceimplSpec extends Specification {

    @Autowired
    GitlabWebHookService gitlabWebHookService

    @Autowired
    private DevopsGitService devopsGitService

    @Autowired
    private IamService iamService

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService

    @Autowired
    private DevopsEnvCommitService devopsEnvCommitService

    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceRepository

    @Autowired
    private DevopsEnvFileService devopsEnvFileRepository

    @Autowired
    private DevopsEnvFileErrorService devopsEnvFileErrorRepository

    @Autowired
    private AppServiceInstanceMapper applicationInstanceMapper

    @Autowired
    private DevopsServiceMapper devopsServiceMapper

    @Autowired
    private DevopsIngressMapper devopsIngressMapper

    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper

    @Autowired
    private DevopsSecretMapper devopsSecretMapper

    @Autowired
    private DevopsConfigMapMapper devopsConfigMapMapper

    @Autowired
    private AppServiceMapper applicationMapper

    @Autowired
    private AppServiceVersionMapper applicationVersionMapper

    @Autowired
    private AppServiceVersionValueMapper applicationVersionValueMapper

    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper

    @Autowired
    private DevopsEnvCommitMapper devopsEnvCommitMapper

    @Autowired
    private DevopsEnvFileMapper devopsEnvFileMapper

    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper


    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper

    @Autowired
    private DevopsEnvCommandValueMapper devopsEnvCommandValueMapper


    @Autowired
    private DevopsCertificationFileMapper devopsCertificationFileMapper

    @Autowired
    private DevopsEnvCommandValueService devopsEnvCommandValueRepository

    @Autowired
    private AppServiceInstanceService applicationInstanceRepository

    @Autowired
    private DevopsServiceService devopsServiceRepository

    @Autowired
    private DevopsIngressService devopsIngressRepository

    @Autowired
    private AppServiceService applicationRepository

    @Autowired
    private AppServiceVersionService applicationVersionRepository

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandRepository

    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper

    @Autowired
    private AppServiceVersionValueMapper applicationVersionValueRepository

    @Autowired
    private CertificationServiceImpl certificationRepository

    @Autowired
    private DevopsSecretService devopsSecretRepository

    @Autowired
    private DevopsConfigMapService devopsConfigMapRepository

    @Autowired
    private DevopsGitService devopsGitRepository

    @Autowired
    private AgentCommandService deployService

    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler envUtil

    @Shared
    Gson gson = new Gson()

    @Shared
    DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()

    @Shared
    AppServiceDTO applicationDO = new AppServiceDTO()

    @Shared
    AppServiceVersionDTO applicationVersionDO = new AppServiceVersionDTO()

    @Shared
    AppServiceVersionValueDTO applicationVersionValueDO = new AppServiceVersionValueDTO()

    @Shared
    CertificationFileDTO certificationFileDO = new CertificationFileDTO()

    @Shared
    PushWebHookVO pushWebHookDTO = new PushWebHookVO()

    @Shared
    DevopsEnvCommitDTO devopsEnvCommitDO = new DevopsEnvCommitDTO()

    SagaClient sagaClient = Mockito.mock(SagaClient.class)

    BaseServiceClient iamServiceClient = Mockito.mock(BaseServiceClient.class)

    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    def setupSpec() {
        //初始化文件
        FileUtil.copyFile("src/test/gitops/gitopsa.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsb.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsc.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsd.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopse.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsf.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsg.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsh.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsi.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsj.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsk.yaml", "gitops/org/pro/testEnv")
        FileUtil.copyFile("src/test/gitops/gitopsl.yaml", "gitops/org/pro/testEnv")

        //初始化pushWebHookDTO
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)

        //初始化环境
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setToken("123456")
        devopsEnvironmentDO.setCode("testEnv")
        devopsEnvironmentDO.setSagaSyncCommit(1L)
        devopsEnvironmentDO.setProjectId(1L)

        //初始化应用
        applicationDO.setId(1L)
        applicationDO.setName("testApp")
        applicationDO.setCode("testApp")
        applicationDO.setAppId(1L)

        //初始化应用版本value
        applicationVersionValueDO.setId(1L)
        applicationVersionValueDO.setValue("resources:\n" +
                "  requests:\n" +
                "    memory: 1.6Gi\n" +
                "  limits:\n" +
                "    memory: 2.5Gi\n" +
                "env:\n" +
                "  open:\n" +
                "    SPRING_REDIS_HOST: redis.tools.svc\n" +
                "    SERVICES_GITLAB_URL: http://git.staging.saas.test.com\n" +
                "    SERVICES_GATEWAY_URL: http://api.staging.saas.test.com\n" +
                "    AGENT_SERVICEURL: ws://devops-service-front.staging.saas.test.com/agent/\n" +
                "    SERVICES_SONARQUBE_URL: http://sonarqube.staging.saas.test.com\n" +
                "    AGENT_REPOURL: http://chart.choerodon.com.cn/choerodon/c7ncd/\n" +
                "    SECURITY_IGNORED: /ci,/webhook,/v2/api-docs,/agent/**,/ws/**,/webhook/**,/workflow/**\n" +
                "    SPRING_CLOUD_CONFIG_URI: http://config-server.choerodon-framework-staging:8010\n" +
                "    SERVICES_HARBOR_PASSWORD: Handhand123\n" +
                "    SERVICES_HELM_URL: http://helm-charts.staging.saas.test.com\n" +
                "    SERVICES_HARBOR_BASEURL: https://registry.saas.test.com\n" +
                "    SPRING_DATASOURCE_URL: jdbc:mysql://hapcloud-mysql.db:3306/devops_service?useUnicode=true&characterEncoding=utf-8&useSSL=false\n" +
                "    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://register-server.choerodon-framework-staging:8000/eureka/\n" +
                "    SPRING_DATASOURCE_PASSWORD: handhand\n" +
                "    SERVICES_GITLAB_SSHURL: git.vk.vu\n" +
                "    AGENT_VERSION: 0.9.2\n" +
                "preJob:\n" +
                "  preConfig:\n" +
                "    mysql:\n" +
                "      host: hapcloud-mysql.db\n" +
                "      password: handhand\n" +
                "  preInitDB:\n" +
                "    mysql:\n" +
                "      host: hapcloud-mysql.db\n" +
                "      password: handhand\n" +
                "persistence:\n" +
                "  existingClaim: chartmuseum-pv")

        //初始化应用版本
        applicationVersionDO.setId(1L)
        applicationVersionDO.setAppServiceId(1L)
        applicationVersionDO.setVersion("2018.8.21-091848-release-0-9-0")
        applicationVersionDO.setReadmeValueId(1L)
        applicationVersionDO.setValueId(1L)

        //初始化环境commit
        devopsEnvCommitDO.setId(1L)
        devopsEnvCommitDO.setEnvId(1L)
        devopsEnvCommitDO.setCommitSha("123456")

        //初始化证书文件
        certificationFileDO.setId(1L)
        certificationFileDO.setCertFile("test")
        certificationFileDO.setKeyFile("test")
    }

    def setup() {
        //通过反射方式注入sagaClient
        DependencyInjectUtil.setAttribute(iamService, "baseServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(devopsGitRepository, "gitlabServiceClient", gitlabServiceClient)

        //mock查询Project
        ProjectDTO projectDTO = new ProjectDTO()
        projectDTO.setName("testProject")
        projectDTO.setCode("pro")
        projectDTO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDTO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

        //mock查询Organization
        OrganizationDTO organizationDTO = new OrganizationDTO()
        organizationDTO.setId(1L)
        organizationDTO.setCode("org")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        //mock查询TagDTO
        List<TagDTO> tagDOS = new ArrayList<>()
        TagDTO tagDO = new TagDTO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)
        ResponseEntity<List<TagDTO>> responseEntity2 = new ResponseEntity<>(tagDOS, HttpStatus.OK)
        Mockito.doReturn(responseEntity2).when(gitlabServiceClient).getTags(1, 1)


    }

    def hand(String code, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        //初始化实例
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO()
        appServiceInstanceDTO.setCode("ins" + code)
        appServiceInstanceDTO.setAppServiceId(1L)
        appServiceInstanceDTO.setAppServiceVersionId(1L)
        appServiceInstanceDTO.setEnvId(devopsEnvironmentDTO.getId())
        appServiceInstanceDTO = applicationInstanceRepository.baseCreate(appServiceInstanceDTO)

        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO()
        devopsEnvCommandValueDTO.setValue(applicationVersionValueDO.getValue())

        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO()
        devopsEnvCommandDTO.setObject("instance")
        devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId())
        devopsEnvCommandDTO.setCommandType("create")
        devopsEnvCommandDTO.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandDTO.initDevopsEnvCommandValueE(devopsEnvCommandValueRepository.baseCreate(devopsEnvCommandValueDTO).getId())

        appServiceInstanceDTO.setCommandId(devopsEnvCommandRepository.baseCreate(devopsEnvCommandDTO).getId())
        applicationInstanceRepository.update(appServiceInstanceDTO)

        //初始化实例文件关系
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = new DevopsEnvFileResourceDTO()
        devopsEnvFileResourceDTO.setEnvId(devopsEnvironmentDTO.getId())
        devopsEnvFileResourceDTO.setResourceId(appServiceInstanceDTO.getId())
        devopsEnvFileResourceDTO.setResourceType("C7NHelmRelease")
        devopsEnvFileResourceDTO.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.baseCreate(devopsEnvFileResourceDTO)

        //初始化service
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO()
        devopsServiceDTO.setName("svc" + code)
        devopsServiceDTO.setEnvId(devopsEnvironmentDTO.getId())
        devopsServiceDTO.setAppId(1L)
        devopsServiceDTO.setPorts("{}")
        devopsServiceDTO = devopsServiceRepository.baseCreate(devopsServiceDTO)

        DevopsEnvCommandDTO devopsEnvCommandDTO1 = new DevopsEnvCommandDTO()
        devopsEnvCommandDTO1.setObject("service")
        devopsEnvCommandDTO1.setObjectId(devopsServiceDTO.getId())
        devopsEnvCommandDTO1.setCommandType("create")
        devopsEnvCommandDTO1.setStatus(CommandStatus.SUCCESS.getStatus())

        devopsServiceDTO.setCommandId(devopsEnvCommandRepository.baseCreate(devopsEnvCommandDTO1).getId())
        devopsServiceRepository.baseUpdate(devopsServiceDTO)

        //初始化service文件关系
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO1 = new DevopsEnvFileResourceDTO()
        devopsEnvFileResourceDTO1.setEnvironment(devopsEnvironmentDTO)
        devopsEnvFileResourceDTO1.setResourceId(devopsServiceDTO.getId())
        devopsEnvFileResourceDTO1.setResourceType("Service")
        devopsEnvFileResourceDTO1.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.baseCreate(devopsEnvFileResourceDTO1)

        //初始化域名
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO()
        devopsIngressDTO.setName("ing" + code)
        devopsIngressDTO.setEnvId(devopsEnvironmentDTO.getId())
        devopsIngressDTO.setDomain("devops-service2-front.staging.saas.test.com")
        devopsIngressDTO = devopsIngressRepository.baseCreateIngress(devopsIngressDTO)

        DevopsEnvCommandDTO devopsEnvCommandDTO2 = new DevopsEnvCommandDTO()
        devopsEnvCommandDTO2.setObject("ingress")
        devopsEnvCommandDTO2.setObjectId(devopsIngressDTO.getId())
        devopsEnvCommandDTO2.setCommandType("create")
        devopsEnvCommandDTO2.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandRepository.create(devopsEnvCommandDTO2)

        DevopsIngressDTO devopsIngressDO = devopsIngressMapper.selectByPrimaryKey(devopsIngressDTO.getId())
        devopsIngressDO.setCommandId(devopsEnvCommandRepository.baseCreate(devopsEnvCommandDTO2).getId())
        devopsIngressRepository.baseUpdateIngress(devopsIngressDO)

        //初始化域名文件关系
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO2 = new DevopsEnvFileResourceDTO()
        devopsEnvFileResourceDTO2.setEnvironment(devopsEnvironmentDTO)
        devopsEnvFileResourceDTO2.setResourceId(devopsIngressDTO.getId())
        devopsEnvFileResourceDTO2.setResourceType("Ingress")
        devopsEnvFileResourceDTO2.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.baseCreate(devopsEnvFileResourceDTO2)

        //初始化证书
        CertificationDTO certificationE = new CertificationDTO()
        certificationE.setName("cert" + code)
        certificationE.setEnvironmentE(devopsEnvironmentDTO)
        certificationE.setCertificationFileId(1L)
        certificationE.setDomains("[\"test.saas.test.com\"]")
        certificationE = certificationRepository.baseCreate(certificationE)

        DevopsEnvCommandDTO devopsEnvCommandE3 = new DevopsEnvCommandDTO()
        devopsEnvCommandE3.setObject("certificate")
        devopsEnvCommandE3.setObjectId(certificationE.getId())
        devopsEnvCommandE3.setCommandType("create")
        devopsEnvCommandE3.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandRepository.baseCreate(devopsEnvCommandE3)

        certificationE.setCommandId(devopsEnvCommandRepository.baseCreate(devopsEnvCommandE3).getId())
        certificationRepository.baseUpdateCommandId(certificationE)

        //初始化证书文件关系
        DevopsEnvFileResourceDTO devopsEnvFileResourceE3 = new DevopsEnvFileResourceDTO()
        devopsEnvFileResourceE3.setEnvironment(devopsEnvironmentDTO)
        devopsEnvFileResourceE3.setResourceId(certificationE.getId())
        devopsEnvFileResourceE3.setResourceType("Certificate")
        devopsEnvFileResourceE3.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.baseCreate(devopsEnvFileResourceE3)

        //初始化Secret
        DevopsSecretDTO devopsSecretE = new DevopsSecretDTO()
        devopsSecretE.setName("sec" + code)
        devopsSecretE.setEnvId(devopsEnvironmentDTO.getId())
        devopsSecretE.setValue("{\"test\": \"test\"}")
        devopsSecretE.setDescription("test")
        devopsSecretE = devopsSecretRepository.baseCreate(devopsSecretE)

        DevopsEnvCommandDTO devopsEnvCommandE4 = new DevopsEnvCommandDTO()
        devopsEnvCommandE4.setObject("secret")
        devopsEnvCommandE4.setObjectId(devopsSecretE.getId())
        devopsEnvCommandE4.setCommandType("create")
        devopsEnvCommandE4.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandRepository.create(devopsEnvCommandE4)

        devopsSecretE.setCommandId(devopsEnvCommandRepository.baseCreate(devopsEnvCommandE4).getId())
        devopsSecretRepository.baseUpdate(devopsSecretE)

        //初始化Secret文件关系
        DevopsEnvFileResourceDTO devopsEnvFileResourceE4 = new DevopsEnvFileResourceDTO()
        devopsEnvFileResourceE4.setEnvId(devopsEnvironmentDTO.getId())
        devopsEnvFileResourceE4.setResourceId(devopsSecretE.getId())
        devopsEnvFileResourceE4.setResourceType("Secret")
        devopsEnvFileResourceE4.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.baseCreate(devopsEnvFileResourceE4)

        //初始化ConfigMap
        DevopsConfigMapDTO devopsConfigMapE = new DevopsConfigMapDTO()
        devopsConfigMapE.setName("configMap" + code)
        devopsConfigMapE.setEnvId(devopsEnvironmentDTO.getId())
        devopsConfigMapE.setValue(gson.toJson(keys))
        devopsConfigMapE.setDescription("test")
        devopsConfigMapE = devopsConfigMapRepository.create(devopsConfigMapE)

        DevopsEnvCommandDTO devopsEnvCommandE5 = new DevopsEnvCommandDTO()
        devopsEnvCommandE5.setObject("configMap")
        devopsEnvCommandE5.setObjectId(devopsConfigMapE.getId())
        devopsEnvCommandE5.setCommandType("create")
        devopsEnvCommandE5.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandRepository.create(devopsEnvCommandE5)

        devopsConfigMapE.setCommandId(devopsEnvCommandRepository.baseCreate(devopsEnvCommandE5).getId())
        devopsConfigMapRepository.baseUpdate(devopsConfigMapE)

        //初始化ConfigMap文件关系
        DevopsEnvFileResourceDTO devopsEnvFileResourceE5 = new DevopsEnvFileResourceDTO()
        devopsEnvFileResourceE5.setEnvironment(devopsEnvironmentDTO)
        devopsEnvFileResourceE5.setResourceId(devopsConfigMapE.getId())
        devopsEnvFileResourceE5.setResourceType("ConfigMap")
        devopsEnvFileResourceE5.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.baseCreate(devopsEnvFileResourceE5)

    }

//    def "GitOpsWebHook"() {
//
//        given:
//        String webhook = "{\n" +
//                "  \"object_kind\": \"push\",\n" +
//                "  \"checkout_sha\": \"123456\",\n" +
//                "  \"commits\": [\n" +
//                "    {\n" +
//                "      \"id\": \"123456\",\n" +
//                "      \"timestamp\": \"2018-08-21T13:07:28+08:00\"\n" +
//                "    }\n" +
//                "  ]\n" +
//                "}"
//        String token = "123456"
//        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(null, null)
//
//        when:
//        gitlabWebHookService.gitOpsWebHook(webhook, token)
//
//        then:
//        token == "123456"
//
//    }


    def "create a instance,service,ingress ,delete a instance,service,ingress,update a instance,service,ingress"() {
        given:
        applicationMapper.insert(applicationDO)
        applicationVersionMapper.insert(applicationVersionDO)
        applicationVersionValueMapper.insert(applicationVersionValueDO)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)
        devopsEnvCommitMapper.insert(devopsEnvCommitDO)
        devopsCertificationFileMapper.insert(certificationFileDO)

        //初始化对象
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()
        devopsEnvironmentDTO.setId(1L)
        hand("gitopsa", devopsEnvironmentDTO)
        hand("gitopsb", devopsEnvironmentDTO)

        //初始化gitops tag 比较结果
        CompareResultDTO compareResultsE = new CompareResultDTO()
        List<DiffDTO> diffES = new ArrayList<>()
        DiffDTO diffE = new DiffDTO()
        diffE.setDeletedFile(true)
        diffE.setRenamedFile(false)
        diffE.setNewFile(false)
        diffE.setNewPath("gitopsa.yaml")
        diffE.setOldPath("gitopsa.yaml")
        diffES.add(diffE)
        DiffDTO diffE1 = new DiffDTO()
        diffE1.setDeletedFile(false)
        diffE1.setRenamedFile(false)
        diffE1.setNewFile(false)
        diffE1.setOldPath("gitopsb.yaml")
        diffE1.setNewPath("gitopsb.yaml")
        diffES.add(diffE1)
        DiffDTO diffE2 = new DiffDTO()
        diffE2.setDeletedFile(false)
        diffE2.setRenamedFile(false)
        diffE2.setNewFile(true)
        diffE2.setNewPath("gitopsc.yaml")
        diffE2.setOldPath("gitopsc.yaml")
        diffES.add(diffE2)
        compareResultsE.setDiffs(diffES)


        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO()
        appServiceInstanceDTO.setCode("insgitopsC")
        appServiceInstanceDTO.setAppServiceId(1L)
        appServiceInstanceDTO.setAppServiceVersionId(1L)
        appServiceInstanceDTO.setEnvId(devopsEnvironmentDTO.getId())

        DevopsServiceDTO devopsServiceE = new DevopsServiceDTO()
        devopsServiceE.setName("svcgitopsC")
        devopsServiceE.setEnvId(devopsEnvironmentDTO.getId())
        devopsServiceE.setPorts("")

        DevopsIngressDTO devopsIngressE = new DevopsIngressDTO()
        devopsIngressE.setName("inggitopsC")
        devopsIngressE.setEnvId(devopsEnvironmentDTO.getId())

        List<TagDTO> tagDOS = new ArrayList<>()
        TagDTO tagDO = new TagDTO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        DevopsIngressPathDTO devopsIngressPathDO = new DevopsIngressPathDTO()
        devopsIngressPathDO.setPath("/")
        devopsIngressPathDO.setIngressId(2L)
        devopsIngressPathMapper.insert(devopsIngressPathDO)

        ResponseEntity<CompareResultDTO> responseEntity2 = new ResponseEntity<>(compareResultsE, HttpStatus.OK)
        Mockito.doReturn(responseEntity2).when(gitlabServiceClient).queryCompareResult(1, "devops-sync", "123456")


        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        envUtil.checkEnvConnection(_ as Long) >> null
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceRepository.baseQueryByEnvIdAndPath(1, "gitopsc.yaml")
        devopsEnvFileResourceDTOS.size() == 6
    }

//    def "move a instance,service,ingress file to other file"() {
//        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
//        pushWebHookDTO.setToken("123456")
//        pushWebHookDTO.setProjectId(1)
//        pushWebHookDTO.setUserId(1)
//        Organization organization = new Organization()
//        organization.setCode("test")
//        ProjectE projectE = new ProjectE()
//        projectE.setOrganization(organization)
//        projectE.setCode("test")
//        CompareResultsE compareResultsE = new CompareResultsE()
//        List<DiffE> diffES = new ArrayList<>()
//        DiffE diffE = new DiffE()
//        diffE.setDeletedFile(true)
//        diffE.setRenamedFile(false)
//        diffE.setNewFile(false)
//        diffE.setNewPath("gitopsb.yaml")
//        diffE.setOldPath("gitopsb.yaml")
//        diffES.add(diffE)
//        DiffE diffE1 = new DiffE()
//        diffE1.setDeletedFile(false)
//        diffE1.setRenamedFile(false)
//        diffE1.setNewFile(true)
//        diffE1.setOldPath("gitopsd.yaml")
//        diffE1.setNewPath("gitopsd.yaml")
//        diffES.add(diffE1)
//        compareResultsE.setDiffs(diffES)
//        List<TagDTO> tagDOS = new ArrayList<>()
//        TagDTO tagDO = new TagDTO()
//        tagDO.setName("devops-sync")
//        tagDOS.add(tagDO)
//        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE()
//        devopsEnvCommandE.setId(4L)
//        devopsEnvCommandE.setCommandType(CommandType.CREATE.getType())
//        devopsEnvCommandE.setObjectId(2L)
//        devopsEnvCommandE.setObject(ObjectType.INSTANCE.getType())
//        devopsEnvCommandRepository.update(devopsEnvCommandE)
//
//
//        when:
//        devopsGitService.fileResourceSync(pushWebHookDTO)
//
//        then:
//        3 * envUtil.checkEnvConnection(_, _)
//        2 * iamRepository.queryIamProject(_) >> projectE
//        2 * iamRepository.queryOrganizationById(_) >> organization
//        1 * deployService.sendCommand(_)
//        List<DevopsEnvFileResourceE> devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndPath(1, "gitopsb.yaml")
//        devopsEnvFileResourceE.size() == 0
//        List<DevopsEnvFileResourceE> newdevopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndPath(1, "gitopsd.yaml")
//        newdevopsEnvFileResourceE.size() == 3
//    }
//
//    def "create a same release object"() {
//
//        given:
//        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
//        pushWebHookDTO.setToken("123456")
//        pushWebHookDTO.setProjectId(1)
//        pushWebHookDTO.setUserId(1)
//        Organization organization = new Organization()
//        organization.setCode("test")
//        ProjectE projectE = new ProjectE()
//        projectE.setOrganization(organization)
//        projectE.setCode("test")
//        CompareResultsE compareResultsE = new CompareResultsE()
//        List<DiffE> diffES = new ArrayList<>()
//        DiffE diffE = new DiffE()
//        diffE.setDeletedFile(false)
//        diffE.setRenamedFile(false)
//        diffE.setNewFile(true)
//        diffE.setOldPath("gitopse.yaml")
//        diffE.setNewPath("gitopse.yaml")
//        diffES.add(diffE)
//        compareResultsE.setDiffs(diffES)
//        List<TagDTO> tagDOS = new ArrayList<>()
//        TagDTO tagDO = new TagDTO()
//        tagDO.setName("devops-sync")
//        tagDOS.add(tagDO)
//
//        when:
//        devopsGitService.fileResourceSync(pushWebHookDTO)
//
//        then:
//        1 * iamRepository.queryIamProject(_) >> projectE
//        1 * iamRepository.queryOrganizationById(_) >> organization
//        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
//        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
//        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.baseListByEnvId(1L)
//        devopsEnvFileErrorE.get(0).getError() == "the another file already has the same object: instest2";
//        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
//    }
//
//
//    def "create a same service object"() {
//        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
//        pushWebHookDTO.setToken("123456")
//        pushWebHookDTO.setProjectId(1)
//        pushWebHookDTO.setUserId(1)
//        Organization organization = new Organization()
//        organization.setCode("test")
//        ProjectE projectE = new ProjectE()
//        projectE.setOrganization(organization)
//        projectE.setCode("test")
//        CompareResultsE compareResultsE = new CompareResultsE()
//        List<DiffE> diffES = new ArrayList<>()
//        DiffE diffE = new DiffE()
//        diffE.setDeletedFile(false)
//        diffE.setRenamedFile(false)
//        diffE.setNewFile(true)
//        diffE.setOldPath("gitopsf.yaml")
//        diffE.setNewPath("gitopsf.yaml")
//        diffES.add(diffE)
//        compareResultsE.setDiffs(diffES)
//        List<TagDTO> tagDOS = new ArrayList<>()
//        TagDTO tagDO = new TagDTO()
//        tagDO.setName("devops-sync")
//        tagDOS.add(tagDO)
//
//        when:
//        devopsGitService.fileResourceSync(pushWebHookDTO)
//
//        then:
//        1 * iamRepository.queryIamProject(_) >> projectE
//        1 * iamRepository.queryOrganizationById(_) >> organization
//        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
//        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
//        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.baseListByEnvId(1L)
//        devopsEnvFileErrorE.get(0).getError() == "the another file already has the same object: svctest2"
//        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
//
//    }
//
//
//    def "create a same ingress object"() {
//        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
//        pushWebHookDTO.setToken("123456")
//        pushWebHookDTO.setProjectId(1)
//        pushWebHookDTO.setUserId(1)
//        Organization organization = new Organization()
//        organization.setCode("test")
//        ProjectE projectE = new ProjectE()
//        projectE.setOrganization(organization)
//        projectE.setCode("test")
//        CompareResultsE compareResultsE = new CompareResultsE()
//        List<DiffE> diffES = new ArrayList<>()
//        DiffE diffE = new DiffE()
//        diffE.setDeletedFile(false)
//        diffE.setRenamedFile(false)
//        diffE.setNewFile(true)
//        diffE.setOldPath("gitopsg.yaml")
//        diffE.setNewPath("gitopsg.yaml")
//        diffES.add(diffE)
//        compareResultsE.setDiffs(diffES)
//        List<TagDTO> tagDOS = new ArrayList<>()
//        TagDTO tagDO = new TagDTO()
//        tagDO.setName("devops-sync")
//        tagDOS.add(tagDO)
//
//        when:
//        devopsGitService.fileResourceSync(pushWebHookDTO)
//
//        then:
//        1 * iamRepository.queryIamProject(_) >> projectE
//        1 * iamRepository.queryOrganizationById(_) >> organization
//        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
//        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
//        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.baseListByEnvId(1L)
//        devopsEnvFileErrorE.get(0).getError() == "the another file already has the same object: ingtest2"
//        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
//
//    }
//
//    def "create a release object when has no app"() {
//        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
//        pushWebHookDTO.setToken("123456")
//        pushWebHookDTO.setProjectId(1)
//        pushWebHookDTO.setUserId(1)
//        Organization organization = new Organization()
//        organization.setCode("test")
//        ProjectE projectE = new ProjectE()
//        projectE.setOrganization(organization)
//        projectE.setCode("test")
//        CompareResultsE compareResultsE = new CompareResultsE()
//        List<DiffE> diffES = new ArrayList<>()
//        DiffE diffE = new DiffE()
//        diffE.setDeletedFile(false)
//        diffE.setRenamedFile(false)
//        diffE.setNewFile(true)
//        diffE.setOldPath("gitopsh.yaml")
//        diffE.setNewPath("gitopsh.yaml")
//        diffES.add(diffE)
//        compareResultsE.setDiffs(diffES)
//        List<TagDTO> tagDOS = new ArrayList<>()
//        TagDTO tagDO = new TagDTO()
//        tagDO.setName("devops-sync")
//        tagDOS.add(tagDO)
//
//        when:
//        devopsGitService.fileResourceSync(pushWebHookDTO)
//
//        then:
//        2 * iamRepository.queryIamProject(_) >> projectE
//        2 * iamRepository.queryOrganizationById(_) >> organization
//        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
//        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
//        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.baseListByEnvId(1L)
//        devopsEnvFileErrorE.get(0).getError() == "the App: testappasdasdasdnot exist in the devops-service"
//        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
//
//    }
//
//
//    def "create a service when has no instance"() {
//        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
//        pushWebHookDTO.setToken("123456")
//        pushWebHookDTO.setProjectId(1)
//        pushWebHookDTO.setUserId(1)
//        Organization organization = new Organization()
//        organization.setCode("test")
//        ProjectE projectE = new ProjectE()
//        projectE.setOrganization(organization)
//        projectE.setCode("test")
//        CompareResultsE compareResultsE = new CompareResultsE()
//        List<DiffE> diffES = new ArrayList<>()
//        DiffE diffE = new DiffE()
//        diffE.setDeletedFile(false)
//        diffE.setRenamedFile(false)
//        diffE.setNewFile(true)
//        diffE.setOldPath("gitopsi.yaml")
//        diffE.setNewPath("gitopsi.yaml")
//        diffES.add(diffE)
//        compareResultsE.setDiffs(diffES)
//        List<TagDTO> tagDOS = new ArrayList<>()
//        TagDTO tagDO = new TagDTO()
//        tagDO.setName("devops-sync")
//        tagDOS.add(tagDO)
//
//        when:
//        devopsGitService.fileResourceSync(pushWebHookDTO)
//
//        then:
//        1 * iamRepository.queryIamProject(_) >> projectE
//        1 * iamRepository.queryOrganizationById(_) >> organization
//        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
//        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
//        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.baseListByEnvId(1L)
//        devopsEnvFileErrorE.get(0).getError() == "The related instance of the service not found: instest212"
//        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
//
//    }
//
//
//    def "create a ingress when has no service"() {
//        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
//        pushWebHookDTO.setToken("123456")
//        pushWebHookDTO.setProjectId(1)
//        pushWebHookDTO.setUserId(1)
//        Organization organization = new Organization()
//        organization.setCode("test")
//        ProjectE projectE = new ProjectE()
//        projectE.setOrganization(organization)
//        projectE.setCode("test")
//        CompareResultsE compareResultsE = new CompareResultsE()
//        List<DiffE> diffES = new ArrayList<>()
//        DiffE diffE = new DiffE()
//        diffE.setDeletedFile(false)
//        diffE.setRenamedFile(false)
//        diffE.setNewFile(true)
//        diffE.setOldPath("gitopsj.yaml")
//        diffE.setNewPath("gitopsj.yaml")
//        diffES.add(diffE)
//        compareResultsE.setDiffs(diffES)
//        List<TagDTO> tagDOS = new ArrayList<>()
//        TagDTO tagDO = new TagDTO()
//        tagDO.setName("devops-sync")
//        tagDOS.add(tagDO)
//
//        when:
//        devopsGitService.fileResourceSync(pushWebHookDTO)
//
//        then:
//        1 * iamRepository.queryIamProject(_) >> projectE
//        1 * iamRepository.queryOrganizationById(_) >> organization
//        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
//        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
//        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.baseListByEnvId(1L)
//        devopsEnvFileErrorE.get(0).getError() == "the related service of the ingress not exist:svctest2asd"
//        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
//    }
//
//
//    def "create a ingress when has same domain and path"() {
//        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
//        pushWebHookDTO.setToken("123456")
//        pushWebHookDTO.setProjectId(1)
//        pushWebHookDTO.setUserId(1)
//        Organization organization = new Organization()
//        organization.setCode("test")
//        ProjectE projectE = new ProjectE()
//        projectE.setOrganization(organization)
//        projectE.setCode("test")
//        CompareResultsE compareResultsE = new CompareResultsE()
//        List<DiffE> diffES = new ArrayList<>()
//        DiffE diffE = new DiffE()
//        diffE.setDeletedFile(false)
//        diffE.setRenamedFile(false)
//        diffE.setNewFile(true)
//        diffE.setOldPath("gitopsk.yaml")
//        diffE.setNewPath("gitopsk.yaml")
//        diffES.add(diffE)
//        compareResultsE.setDiffs(diffES)
//        List<TagDTO> tagDOS = new ArrayList<>()
//        TagDTO tagDO = new TagDTO()
//        tagDO.setName("devops-sync")
//        tagDOS.add(tagDO)
//
//        when:
//        devopsGitService.fileResourceSync(pushWebHookDTO)
//
//        then:
//        1 * iamRepository.queryIamProject(_) >> projectE
//        1 * iamRepository.queryOrganizationById(_) >> organization
//        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
//        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
//        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.baseListByEnvId(1L)
//        devopsEnvFileErrorE.get(0).getError() == "the ingress domain and path is already exist!"
//        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
//    }
//
//
//    def "create a object when has no name"() {
//        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
//        pushWebHookDTO.setToken("123456")
//        pushWebHookDTO.setProjectId(1)
//        pushWebHookDTO.setUserId(1)
//        Organization organization = new Organization()
//        organization.setCode("test")
//        ProjectE projectE = new ProjectE()
//        projectE.setOrganization(organization)
//        projectE.setCode("test")
//        CompareResultsE compareResultsE = new CompareResultsE()
//        List<DiffE> diffES = new ArrayList<>()
//        DiffE diffE = new DiffE()
//        diffE.setDeletedFile(false)
//        diffE.setRenamedFile(false)
//        diffE.setNewFile(true)
//        diffE.setOldPath("gitopsl.yaml")
//        diffE.setNewPath("gitopsl.yaml")
//        diffES.add(diffE)
//        compareResultsE.setDiffs(diffES)
//        List<TagDTO> tagDOS = new ArrayList<>()
//        TagDTO tagDO = new TagDTO()
//        tagDO.setName("devops-sync")
//        tagDOS.add(tagDO)
//
//        when:
//        devopsGitService.fileResourceSync(pushWebHookDTO)
//
//        then:
//        1 * iamRepository.queryIamProject(_) >> projectE
//        1 * iamRepository.queryOrganizationById(_) >> organization
//        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
//        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
//        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.baseListByEnvId(1L)
//        devopsEnvFileErrorE.get(0).getError() == "The C7nHelmRelease does not define name properties"
//        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
//    }


    def "cleanupData"() {
        given:
        // 删除appInstance
        List<AppServiceInstanceDTO> list = applicationInstanceMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (AppServiceInstanceDTO e : list) {
                applicationInstanceMapper.delete(e)
            }
        }

        // 删除appVersion
        List<AppServiceVersionDTO> list4 = applicationVersionMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (AppServiceVersionDTO e : list4) {
                applicationVersionMapper.delete(e)
            }
        }
        // 删除appVersionValue
        List<AppServiceVersionValueDTO> list5 = applicationVersionValueMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (AppServiceVersionValueDTO e : list5) {
                applicationVersionValueMapper.delete(e)
            }
        }
        // 删除app
        List<AppServiceDTO> list6 = applicationMapper.selectAll()
        if (list6 != null && !list6.isEmpty()) {
            for (AppServiceDTO e : list6) {
                applicationMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDTO> list7 = devopsEnvironmentMapper.selectAll()
        if (list7 != null && !list7.isEmpty()) {
            for (DevopsEnvironmentDTO e : list7) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除envCommand
        List<DevopsEnvCommandDTO> list8 = devopsEnvCommandMapper.selectAll()
        if (list8 != null && !list8.isEmpty()) {
            for (DevopsEnvCommandDTO e : list8) {
                devopsEnvCommandMapper.delete(e)
            }
        }
        // 删除envCommandValue
        List<DevopsEnvCommandValueDTO> list9 = devopsEnvCommandValueMapper.selectAll()
        if (list9 != null && !list9.isEmpty()) {
            for (DevopsEnvCommandValueDTO e : list9) {
                devopsEnvCommandValueMapper.delete(e)
            }
        }
        // 删除envFile
        List<DevopsEnvFileDTO> list10 = devopsEnvFileMapper.selectAll()
        if (list10 != null && !list10.isEmpty()) {
            for (DevopsEnvFileDTO e : list10) {
                devopsEnvFileMapper.delete(e)
            }
        }
        // 删除envFileResource
        List<DevopsEnvFileResourceDTO> list11 = devopsEnvFileResourceMapper.selectAll()
        if (list11 != null && !list11.isEmpty()) {
            for (DevopsEnvFileResourceDTO e : list11) {
                devopsEnvFileResourceMapper.delete(e)
            }
        }

        // 删除ingress
        List<DevopsIngressDTO> list12 = devopsIngressMapper.selectAll()
        if (list12 != null && !list12.isEmpty()) {
            for (DevopsIngressDTO e : list12) {
                devopsIngressMapper.delete(e)
            }
        }

        // 删除service
        List<DevopsServiceDTO> list13 = devopsServiceMapper.selectAll()
        if (list13 != null && !list13.isEmpty()) {
            for (DevopsServiceDTO e : list13) {
                devopsServiceMapper.delete(e)
            }
        }

        // 删除cert
        List<CertificationDTO> list14 = devopsCertificationMapper.selectAll()
        if (list14 != null && !list14.isEmpty()) {
            for (CertificationDTO e : list14) {
                devopsCertificationMapper.delete(e)
            }
        }

        // 删除secret
        List<DevopsSecretDTO> list15 = devopsSecretMapper.selectAll()
        if (list15 != null && !list15.isEmpty()) {
            for (DevopsSecretDTO e : list15) {
                devopsSecretMapper.delete(e)
            }
        }

        // 删除secret
        List<DevopsConfigMapDTO> list16 = devopsConfigMapMapper.selectAll()
        if (list16 != null && !list16.isEmpty()) {
            for (DevopsConfigMapDTO e : list16) {
                devopsConfigMapMapper.delete(e)
            }
        }

        FileUtil.deleteDirectory(new File("gitops"))
    }
}
