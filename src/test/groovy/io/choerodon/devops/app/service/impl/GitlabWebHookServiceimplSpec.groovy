package io.choerodon.devops.app.service.impl

import com.google.gson.Gson
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.PushWebHookDTO
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.app.service.GitlabWebHookService
import io.choerodon.devops.api.vo.iam.entity.*
import io.choerodon.devops.api.vo.iam.entity.gitlab.CompareResultsE
import io.choerodon.devops.api.vo.iam.entity.gitlab.DiffE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.app.service.DeployService
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.common.util.enums.CommandStatus
import io.choerodon.devops.infra.dataobject.gitlab.TagDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.*
import io.choerodon.websocket.helper.EnvListener
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
    private IamRepository iamRepository

    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository

    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository

    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository

    @Autowired
    private DevopsEnvFileRepository devopsEnvFileRepository

    @Autowired
    private DevopsEnvFileErrorRepository devopsEnvFileErrorRepository

    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper

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
    private ApplicationMapper applicationMapper

    @Autowired
    private ApplicationVersionMapper applicationVersionMapper

    @Autowired
    private ApplicationVersionValueMapper applicationVersionValueMapper

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
    private DevopsEnvCommandValueRepository devopsEnvCommandValueRepository

    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository

    @Autowired
    private DevopsServiceRepository devopsServiceRepository

    @Autowired
    private DevopsIngressRepository devopsIngressRepository

    @Autowired
    private ApplicationRepository applicationRepository

    @Autowired
    private ApplicationVersionRepository applicationVersionRepository

    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository

    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper

    @Autowired
    private ApplicationVersionValueRepository applicationVersionValueRepository

    @Autowired
    private CertificationRepository certificationRepository

    @Autowired
    private DevopsSecretRepository devopsSecretRepository

    @Autowired
    private DevopsConfigMapRepository devopsConfigMapRepository

    @Autowired
    private DevopsGitRepository devopsGitRepository

    @Autowired
    private DeployService deployService

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil

    @Shared
    Gson gson = new Gson()

    @Shared
    DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()

    @Shared
    ApplicationDTO applicationDO = new ApplicationDTO()

    @Shared
    ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO()

    @Shared
    ApplicationVersionValueDO applicationVersionValueDO = new ApplicationVersionValueDO()

    @Shared
    CertificationFileDO certificationFileDO = new CertificationFileDO()

    @Shared
    PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()

    @Shared
    DevopsEnvCommitDO devopsEnvCommitDO = new DevopsEnvCommitDO()

    SagaClient sagaClient = Mockito.mock(SagaClient.class)

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

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
        applicationDO.setProjectId(1L)

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
        applicationVersionDO.setAppId(1L)
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
        DependencyInjectUtil.setAttribute(devopsGitService, "sagaClient", sagaClient)
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
        DependencyInjectUtil.setAttribute(devopsGitRepository, "gitlabServiceClient", gitlabServiceClient)

        //mock查询Project
        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("testProject")
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

        //mock查询Organization
        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        //mock查询TagDO
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)
        ResponseEntity<List<TagDO>> responseEntity2 = new ResponseEntity<>(tagDOS, HttpStatus.OK)
        Mockito.doReturn(responseEntity2).when(gitlabServiceClient).getTags(1, 1)


    }

    def hand(String code, DevopsEnvironmentE devopsEnvironmentE) {
        //初始化实例
        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE()
        applicationInstanceE.setCode("ins" + code)
        applicationInstanceE.initApplicationEById(1L)
        applicationInstanceE.initApplicationVersionEById(1L)
        applicationInstanceE.initDevopsEnvironmentEById(devopsEnvironmentE.getId())
        applicationInstanceE = applicationInstanceRepository.create(applicationInstanceE)

        DevopsEnvCommandValueVO devopsEnvCommandValueE = new DevopsEnvCommandValueVO()
        devopsEnvCommandValueE.setValue(applicationVersionValueDO.getValue())

        DevopsEnvCommandVO devopsEnvCommandE = new DevopsEnvCommandVO()
        devopsEnvCommandE.setObject("instance")
        devopsEnvCommandE.setObjectId(applicationInstanceE.getId())
        devopsEnvCommandE.setCommandType("create")
        devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandE.initDevopsEnvCommandValueE(devopsEnvCommandValueRepository.baseCreate(devopsEnvCommandValueE).getId())

        applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId())
        applicationInstanceRepository.update(applicationInstanceE)


        //初始化实例文件关系
        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE()
        devopsEnvFileResourceE.setEnvironment(devopsEnvironmentE)
        devopsEnvFileResourceE.setResourceId(applicationInstanceE.getId())
        devopsEnvFileResourceE.setResourceType("C7NHelmRelease")
        devopsEnvFileResourceE.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE)

        //初始化service
        DevopsServiceE devopsServiceE = new DevopsServiceE()
        devopsServiceE.setName("svc" + code)
        devopsServiceE.setEnvId(devopsEnvironmentE.getId())
        devopsServiceE.setAppId(1L)
        devopsServiceE.setPorts(new ArrayList<PortMapE>())
        devopsServiceE = devopsServiceRepository.insert(devopsServiceE)

        DevopsEnvCommandVO devopsEnvCommandE1 = new DevopsEnvCommandVO()
        devopsEnvCommandE1.setObject("service")
        devopsEnvCommandE1.setObjectId(devopsServiceE.getId())
        devopsEnvCommandE1.setCommandType("create")
        devopsEnvCommandE1.setStatus(CommandStatus.SUCCESS.getStatus())

        devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE1).getId())
        devopsServiceRepository.update(devopsServiceE)

        //初始化service文件关系
        DevopsEnvFileResourceE devopsEnvFileResourceE1 = new DevopsEnvFileResourceE()
        devopsEnvFileResourceE1.setEnvironment(devopsEnvironmentE)
        devopsEnvFileResourceE1.setResourceId(devopsServiceE.getId())
        devopsEnvFileResourceE1.setResourceType("Service")
        devopsEnvFileResourceE1.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE1)

        //初始化域名
        DevopsIngressE devopsIngressE = new DevopsIngressE()
        devopsIngressE.setName("ing" + code)
        devopsIngressE.setEnvId(devopsEnvironmentE.getId())
        devopsIngressE.setDomain("devops-service2-front.staging.saas.test.com")
        devopsIngressE = devopsIngressRepository.insertIngress(devopsIngressE)

        DevopsEnvCommandVO devopsEnvCommandE2 = new DevopsEnvCommandVO()
        devopsEnvCommandE2.setObject("ingress")
        devopsEnvCommandE2.setObjectId(devopsIngressE.getId())
        devopsEnvCommandE2.setCommandType("create")
        devopsEnvCommandE2.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandRepository.create(devopsEnvCommandE2)

        DevopsIngressDO devopsIngressDO = devopsIngressMapper.selectByPrimaryKey(devopsIngressE.getId())
        devopsIngressDO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE2).getId())
        devopsIngressRepository.updateIngress(devopsIngressDO)

        //初始化域名文件关系
        DevopsEnvFileResourceE devopsEnvFileResourceE2 = new DevopsEnvFileResourceE()
        devopsEnvFileResourceE2.setEnvironment(devopsEnvironmentE)
        devopsEnvFileResourceE2.setResourceId(devopsIngressE.getId())
        devopsEnvFileResourceE2.setResourceType("Ingress")
        devopsEnvFileResourceE2.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE2)

        //初始化证书
        CertificationE certificationE = new CertificationE()
        certificationE.setName("cert" + code)
        certificationE.setEnvironmentE(devopsEnvironmentE)
        certificationE.setCertificationFileId(1L)
        List<String> domain = new ArrayList<>()
        domain.add("test.saas.test.com")
        certificationE.setDomains(domain)
        certificationE = certificationRepository.baseCreate(certificationE);

        DevopsEnvCommandVO devopsEnvCommandE3 = new DevopsEnvCommandVO()
        devopsEnvCommandE3.setObject("certificate")
        devopsEnvCommandE3.setObjectId(certificationE.getId())
        devopsEnvCommandE3.setCommandType("create")
        devopsEnvCommandE3.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandRepository.create(devopsEnvCommandE3)

        certificationE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE3).getId())
        certificationRepository.baseUpdateCommandId(certificationE)

        //初始化证书文件关系
        DevopsEnvFileResourceE devopsEnvFileResourceE3 = new DevopsEnvFileResourceE()
        devopsEnvFileResourceE3.setEnvironment(devopsEnvironmentE)
        devopsEnvFileResourceE3.setResourceId(certificationE.getId())
        devopsEnvFileResourceE3.setResourceType("Certificate")
        devopsEnvFileResourceE3.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE3)

        //初始化Secret
        DevopsSecretE devopsSecretE = new DevopsSecretE()
        devopsSecretE.setName("sec" + code)
        devopsSecretE.setEnvId(devopsEnvironmentE.getId())
        Map<String, String> keys = new HashMap<>()
        keys.put("test", "test")
        devopsSecretE.setValue(keys)
        devopsSecretE.setDescription("test")
        devopsSecretE = devopsSecretRepository.create(devopsSecretE)

        DevopsEnvCommandVO devopsEnvCommandE4 = new DevopsEnvCommandVO()
        devopsEnvCommandE4.setObject("secret")
        devopsEnvCommandE4.setObjectId(devopsSecretE.getId())
        devopsEnvCommandE4.setCommandType("create")
        devopsEnvCommandE4.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandRepository.create(devopsEnvCommandE4)

        devopsSecretE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE4).getId())
        devopsSecretRepository.update(devopsSecretE)

        //初始化Secret文件关系
        DevopsEnvFileResourceE devopsEnvFileResourceE4 = new DevopsEnvFileResourceE()
        devopsEnvFileResourceE4.setEnvironment(devopsEnvironmentE)
        devopsEnvFileResourceE4.setResourceId(devopsSecretE.getId())
        devopsEnvFileResourceE4.setResourceType("Secret")
        devopsEnvFileResourceE4.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE4)

        //初始化ConfigMap
        DevopsConfigMapE devopsConfigMapE = new DevopsConfigMapE()
        devopsConfigMapE.setName("configMap" + code)
        devopsConfigMapE.initDevopsEnvironmentE(devopsEnvironmentE.getId())
        devopsConfigMapE.setValue(gson.toJson(keys))
        devopsConfigMapE.setDescription("test")
        devopsConfigMapE = devopsConfigMapRepository.create(devopsConfigMapE)

        DevopsEnvCommandVO devopsEnvCommandE5 = new DevopsEnvCommandVO()
        devopsEnvCommandE5.setObject("configMap")
        devopsEnvCommandE5.setObjectId(devopsConfigMapE.getId())
        devopsEnvCommandE5.setCommandType("create")
        devopsEnvCommandE5.setStatus(CommandStatus.SUCCESS.getStatus())
        devopsEnvCommandRepository.create(devopsEnvCommandE5)

        devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE5).getId())
        devopsConfigMapRepository.update(devopsConfigMapE)


        //初始化ConfigMap文件关系
        DevopsEnvFileResourceE devopsEnvFileResourceE5 = new DevopsEnvFileResourceE()
        devopsEnvFileResourceE5.setEnvironment(devopsEnvironmentE)
        devopsEnvFileResourceE5.setResourceId(devopsConfigMapE.getId())
        devopsEnvFileResourceE5.setResourceType("ConfigMap")
        devopsEnvFileResourceE5.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE5)

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
        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE()
        devopsEnvironmentE.setId(1L)
        hand("gitopsa", devopsEnvironmentE)
        hand("gitopsb", devopsEnvironmentE)

        //初始化gitops tag 比较结果
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(true)
        diffE.setRenamedFile(false)
        diffE.setNewFile(false)
        diffE.setNewPath("gitopsa.yaml")
        diffE.setOldPath("gitopsa.yaml")
        diffES.add(diffE)
        DiffE diffE1 = new DiffE()
        diffE1.setDeletedFile(false)
        diffE1.setRenamedFile(false)
        diffE1.setNewFile(false)
        diffE1.setOldPath("gitopsb.yaml")
        diffE1.setNewPath("gitopsb.yaml")
        diffES.add(diffE1)
        DiffE diffE2 = new DiffE()
        diffE2.setDeletedFile(false)
        diffE2.setRenamedFile(false)
        diffE2.setNewFile(true)
        diffE2.setNewPath("gitopsc.yaml")
        diffE2.setOldPath("gitopsc.yaml")
        diffES.add(diffE2)
        compareResultsE.setDiffs(diffES)


        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE()
        applicationInstanceE.setCode("insgitopsC")
        applicationInstanceE.initApplicationEById(1L)
        applicationInstanceE.initApplicationVersionEById(1L)
        applicationInstanceE.initDevopsEnvironmentEById(devopsEnvironmentE.getId())

        DevopsServiceE devopsServiceE = new DevopsServiceE()
        devopsServiceE.setName("svcgitopsC")
        devopsServiceE.setEnvId(devopsEnvironmentE.getId())
        devopsServiceE.setPorts(new ArrayList<PortMapE>())

        DevopsIngressE devopsIngressE = new DevopsIngressE()
        devopsIngressE.setName("inggitopsC")
        devopsIngressE.setEnvId(devopsEnvironmentE.getId())

        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO()
        devopsIngressPathDO.setPath("/")
        devopsIngressPathDO.setIngressId(2L)
        devopsIngressPathMapper.insert(devopsIngressPathDO)

        ResponseEntity<CompareResultsE> responseEntity2 = new ResponseEntity<>(compareResultsE, HttpStatus.OK)
        Mockito.doReturn(responseEntity2).when(gitlabServiceClient).getCompareResults(1, "devops-sync", "123456")


        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        envUtil.checkEnvConnection(_ as Long, _ as EnvListener) >> null
        List<DevopsEnvFileResourceE> devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndPath(1, "gitopsc.yaml")
        devopsEnvFileResourceE.size() == 6
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
//        List<TagDO> tagDOS = new ArrayList<>()
//        TagDO tagDO = new TagDO()
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
//        List<TagDO> tagDOS = new ArrayList<>()
//        TagDO tagDO = new TagDO()
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
//        List<TagDO> tagDOS = new ArrayList<>()
//        TagDO tagDO = new TagDO()
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
//        List<TagDO> tagDOS = new ArrayList<>()
//        TagDO tagDO = new TagDO()
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
//        List<TagDO> tagDOS = new ArrayList<>()
//        TagDO tagDO = new TagDO()
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
//        List<TagDO> tagDOS = new ArrayList<>()
//        TagDO tagDO = new TagDO()
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
//        List<TagDO> tagDOS = new ArrayList<>()
//        TagDO tagDO = new TagDO()
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
//        List<TagDO> tagDOS = new ArrayList<>()
//        TagDO tagDO = new TagDO()
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
//        List<TagDO> tagDOS = new ArrayList<>()
//        TagDO tagDO = new TagDO()
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
        List<ApplicationInstanceDO> list = applicationInstanceMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (ApplicationInstanceDO e : list) {
                applicationInstanceMapper.delete(e)
            }
        }


        // 删除appVersion
        List<ApplicationVersionDO> list4 = applicationVersionMapper.selectAll()
        if (list4 != null && !list4.isEmpty()) {
            for (ApplicationVersionDO e : list4) {
                applicationVersionMapper.delete(e)
            }
        }
        // 删除appVersionValue
        List<ApplicationVersionValueDO> list5 = applicationVersionValueMapper.selectAll()
        if (list5 != null && !list5.isEmpty()) {
            for (ApplicationVersionValueDO e : list5) {
                applicationVersionValueMapper.delete(e)
            }
        }
        // 删除app
        List<ApplicationDTO> list6 = applicationMapper.selectAll()
        if (list6 != null && !list6.isEmpty()) {
            for (ApplicationDTO e : list6) {
                applicationMapper.delete(e)
            }
        }
        // 删除env
        List<DevopsEnvironmentDO> list7 = devopsEnvironmentMapper.selectAll()
        if (list7 != null && !list7.isEmpty()) {
            for (DevopsEnvironmentDO e : list7) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除envCommand
        List<DevopsEnvCommandDO> list8 = devopsEnvCommandMapper.selectAll()
        if (list8 != null && !list8.isEmpty()) {
            for (DevopsEnvCommandDO e : list8) {
                devopsEnvCommandMapper.delete(e)
            }
        }
        // 删除envCommandValue
        List<DevopsEnvCommandValueDO> list9 = devopsEnvCommandValueMapper.selectAll()
        if (list9 != null && !list9.isEmpty()) {
            for (DevopsEnvCommandValueDO e : list9) {
                devopsEnvCommandValueMapper.delete(e)
            }
        }
        // 删除envFile
        List<DevopsEnvFileDO> list10 = devopsEnvFileMapper.selectAll()
        if (list10 != null && !list10.isEmpty()) {
            for (DevopsEnvFileDO e : list10) {
                devopsEnvFileMapper.delete(e)
            }
        }
        // 删除envFileResource
        List<DevopsEnvFileResourceDO> list11 = devopsEnvFileResourceMapper.selectAll()
        if (list11 != null && !list11.isEmpty()) {
            for (DevopsEnvFileResourceDO e : list11) {
                devopsEnvFileResourceMapper.delete(e)
            }
        }

        // 删除ingress
        List<DevopsIngressDO> list12 = devopsIngressMapper.selectAll()
        if (list12 != null && !list12.isEmpty()) {
            for (DevopsIngressDO e : list12) {
                devopsIngressMapper.delete(e)
            }
        }

        // 删除service
        List<DevopsServiceDO> list13 = devopsServiceMapper.selectAll()
        if (list13 != null && !list13.isEmpty()) {
            for (DevopsServiceDO e : list13) {
                devopsServiceMapper.delete(e)
            }
        }

        // 删除cert
        List<CertificationDO> list14 = devopsCertificationMapper.selectAll()
        if (list14 != null && !list14.isEmpty()) {
            for (CertificationDO e : list14) {
                devopsCertificationMapper.delete(e)
            }
        }

        // 删除secret
        List<DevopsSecretDO> list15 = devopsSecretMapper.selectAll()
        if (list15 != null && !list15.isEmpty()) {
            for (DevopsSecretDO e : list15) {
                devopsSecretMapper.delete(e)
            }
        }

        // 删除secret
        List<DevopsConfigMapDO> list16 = devopsConfigMapMapper.selectAll()
        if (list16 != null && !list16.isEmpty()) {
            for (DevopsConfigMapDO e : list16) {
                devopsConfigMapMapper.delete(e)
            }
        }

        FileUtil.deleteDirectory(new File("gitops"))
    }
}
