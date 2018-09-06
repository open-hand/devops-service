package io.choerodon.devops.app.service

import groovy.sql.Sql
import io.choerodon.asgard.saga.dto.SagaInstanceDTO
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.core.convertor.ConvertHelper
import io.choerodon.core.exception.CommonException
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationInstanceDTO
import io.choerodon.devops.api.dto.PushWebHookDTO
import io.choerodon.devops.app.service.impl.DevopsGitServiceImpl
import io.choerodon.devops.domain.application.entity.*
import io.choerodon.devops.domain.application.entity.gitlab.CompareResultsE
import io.choerodon.devops.domain.application.entity.gitlab.DiffE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.domain.service.DeployService
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.common.util.enums.CommandType
import io.choerodon.devops.infra.common.util.enums.ObjectType
import io.choerodon.devops.infra.dataobject.DevopsIngressPathDO
import io.choerodon.devops.infra.dataobject.gitlab.TagDO
import io.choerodon.devops.infra.mapper.DevopsIngressPathMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import spock.lang.Shared
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class GitlabWebHookServiceSpec extends Specification {

    private static int level = 0

    @Autowired
    GitlabWebHookService gitlabWebHookService

    @Autowired
    private DevopsGitService devopsGitService

    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository

    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    @Autowired
    @Qualifier("mockDevopsGitRepository")
    private DevopsGitRepository devopsGitRepository

    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository

    @Autowired
    private DevopsEnvFileRepository devopsEnvFileRepository

    @Autowired
    private DevopsEnvFileErrorRepository devopsEnvFileErrorRepository

    @Autowired
    @Qualifier("mockDeployService")
    private DeployService deployService

//    @Autowired
//    @Qualifier("mockApplicationInstanceService")
//    private ApplicationInstanceService applicationInstanceService
//
//    @Autowired
//    @Qualifier("mockDevopsServiceService")
//    private DevopsServiceService devopsServiceService
//
//    @Autowired
//    @Qualifier("mockDevopsIngressService")
//    private DevopsIngressService devopsIngressService


    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository

    @Autowired
    private DevopsServiceRepository devopsServiceRepository

    @Autowired
    private DevopsIngressRepository devopsIngressRepository

    @Autowired
    private ApplicationRepository applicationRepository

    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;

    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository

    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper
    @Autowired
    private ApplicationVersionValueRepository applicationVersionValueRepository

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil;

    SagaClient sagaClient = Mockito.mock(SagaClient.class)



    def setup() {
        FileUtil.copyFile("src/test/gitops/test1.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test2.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test3.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test4.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test5.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test6.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test7.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test8.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test9.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test10.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test11.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test12.yaml", "gitops/test/test/test")
        FileUtil.copyFile("src/test/gitops/test2.yaml", "gitops/test/test/test2")
        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE()
        devopsEnvironmentE.setId(1L)
        devopsEnvironmentE.setToken("123456")
        devopsEnvironmentE.setCode("test")
        devopsEnvironmentE.setGitCommit(1L)
        devopsEnvironmentE.initProjectE(1L)
        devopsGitService.initMockService(sagaClient)
        ApplicationE applicationE = new ApplicationE()
        applicationE.setCode("testapp")
        applicationE.initProjectE(1L)
        ApplicationVersionValueE applicationVersionValueE = new ApplicationVersionValueE();
        applicationVersionValueE.setValue("resources:\n" +
                "  requests:\n" +
                "    memory: 1.6Gi\n" +
                "  limits:\n" +
                "    memory: 2.5Gi\n" +
                "env:\n" +
                "  open:\n" +
                "    SPRING_REDIS_HOST: redis.tools.svc\n" +
                "    SERVICES_GITLAB_URL: http://git.staging.saas.hand-china.com\n" +
                "    SERVICES_GATEWAY_URL: http://api.staging.saas.hand-china.com\n" +
                "    AGENT_SERVICEURL: ws://devops-service-front.staging.saas.hand-china.com/agent/\n" +
                "    SERVICES_SONARQUBE_URL: http://sonarqube.staging.saas.hand-china.com\n" +
                "    AGENT_REPOURL: http://chart.choerodon.com.cn/choerodon/c7ncd/\n" +
                "    SECURITY_IGNORED: /ci,/webhook,/v2/api-docs,/agent/**,/ws/**,/webhook/**\n" +
                "    SPRING_CLOUD_CONFIG_URI: http://config-server.choerodon-framework-staging:8010\n" +
                "    SERVICES_HARBOR_PASSWORD: Handhand123\n" +
                "    SERVICES_HELM_URL: http://helm-charts.staging.saas.hand-china.com\n" +
                "    SERVICES_HARBOR_BASEURL: https://registry.saas.hand-china.com\n" +
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
        ApplicationVersionE applicationVersionE = new ApplicationVersionE()
        applicationVersionE.initApplicationEById(applicationRepository.create(applicationE).getId())
        applicationVersionE.setVersion("2018.8.21-091848-release-0-9-0")
        applicationVersionE.initApplicationVersionReadmeV("")
        applicationVersionE.initApplicationVersionValueE(applicationVersionValueRepository.create(applicationVersionValueE).getId())
        DevopsEnvCommitE devopsEnvCommitE = new DevopsEnvCommitE()
        devopsEnvCommitE.setId(1L)
        devopsEnvCommitE.setEnvId(1L)
        devopsEnvCommitE.setCommitSha("123456")
        if (level == 0) {
            devopsEnvironmentRepository.create(devopsEnvironmentE)
            applicationRepository.create(applicationE)
            applicationVersionRepository.create(applicationVersionE)
            devopsEnvCommitRepository.create(devopsEnvCommitE)
            level = 1
        }
    }


    def hand(String code, DevopsEnvironmentE devopsEnvironmentE) {
        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE()
        applicationInstanceE.setCode("ins" + code)
        applicationInstanceE.initApplicationEById(1L)
        applicationInstanceE.initApplicationVersionEById(1L)
        applicationInstanceE.initDevopsEnvironmentEById(devopsEnvironmentE.getId())
        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE()
        devopsEnvFileResourceE.setEnvironment(devopsEnvironmentE)
        devopsEnvFileResourceE.setResourceId(applicationInstanceRepository.create(applicationInstanceE).getId())
        devopsEnvFileResourceE.setResourceType("C7NHelmRelease")
        devopsEnvFileResourceE.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE)
        DevopsServiceE devopsServiceE = new DevopsServiceE()
        devopsServiceE.setName("svc" + code)
        devopsServiceE.setNamespace("test")
        devopsServiceE.setEnvId(devopsEnvironmentE.getId())
        devopsServiceE.setAppId(1L)
        devopsServiceE.setPorts(new ArrayList<PortMapE>())
        DevopsEnvFileResourceE devopsEnvFileResourceE1 = new DevopsEnvFileResourceE()
        devopsEnvFileResourceE1.setEnvironment(devopsEnvironmentE)
        devopsEnvFileResourceE1.setResourceId(devopsServiceRepository.insert(devopsServiceE).getId())
        devopsEnvFileResourceE1.setResourceType("Service")
        devopsEnvFileResourceE1.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE1)
        DevopsIngressE devopsIngressE = new DevopsIngressE()
        devopsIngressE.setName("ing" + code)
        devopsIngressE.setEnvId(devopsEnvironmentE.getId())
        devopsIngressE.setDomain("devops-service2-front.staging.saas.hand-china.com")
        DevopsEnvFileResourceE devopsEnvFileResourceE2 = new DevopsEnvFileResourceE()
        devopsEnvFileResourceE2.setEnvironment(devopsEnvironmentE)
        devopsEnvFileResourceE2.setResourceId(devopsIngressRepository.insertIngress(devopsIngressE).getId())
        devopsEnvFileResourceE2.setResourceType("Ingress")
        devopsEnvFileResourceE2.setFilePath(code + ".yaml")
        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE2)
    }


    def "GitOpsWebHook"() {

        given:
        String webhook = "{\n" +
                "  \"object_kind\": \"push\",\n" +
                "  \"checkout_sha\": \"123456\",\n" +
                "  \"commits\": [\n" +
                "    {\n" +
                "      \"id\": \"123456\",\n" +
                "      \"timestamp\": \"2018-08-21T13:07:28+08:00\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        String token = "123456"
        Mockito.doReturn(new SagaInstanceDTO()).when(sagaClient).startSaga(null, null)

        when:
        gitlabWebHookService.gitOpsWebHook(webhook, token)

        then:
        token == "123456"

    }


    def "create a instance,service,ingress ,delete a instance,service,ingress,update a instance,service,ingress"() {
        given:
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE()
        devopsEnvironmentE.setId(1L)
        hand("test1", devopsEnvironmentE)
        hand("test2", devopsEnvironmentE)
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(true)
        diffE.setRenamedFile(false)
        diffE.setNewFile(false)
        diffE.setNewPath("test1.yaml")
        diffE.setOldPath("test1.yaml")
        diffES.add(diffE)
        DiffE diffE1 = new DiffE()
        diffE1.setDeletedFile(false)
        diffE1.setRenamedFile(false)
        diffE1.setNewFile(false)
        diffE1.setOldPath("test2.yaml")
        diffE1.setNewPath("test2.yaml")
        diffES.add(diffE1)
        DiffE diffE2 = new DiffE()
        diffE2.setDeletedFile(false)
        diffE2.setRenamedFile(false)
        diffE2.setNewFile(true)
        diffE2.setNewPath("test3.yaml")
        diffE2.setOldPath("test3.yaml")
        diffES.add(diffE2)
        compareResultsE.setDiffs(diffES)
        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE()
        applicationInstanceE.setCode("instest3")
        applicationInstanceE.initApplicationEById(1L)
        applicationInstanceE.initApplicationVersionEById(1L)
        applicationInstanceE.initDevopsEnvironmentEById(devopsEnvironmentE.getId())
        DevopsServiceE devopsServiceE = new DevopsServiceE()
        devopsServiceE.setName("svctest3")
        devopsServiceE.setNamespace("test")
        devopsServiceE.setEnvId(devopsEnvironmentE.getId())
        devopsServiceE.setPorts(new ArrayList<PortMapE>())
        DevopsIngressE devopsIngressE = new DevopsIngressE()
        devopsIngressE.setName("ingtest3")
        devopsIngressE.setEnvId(devopsEnvironmentE.getId())
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO()
        applicationInstanceDTO.setId(3L)
        ApplicationInstanceDTO applicationInstanceDTO1 = new ApplicationInstanceDTO();
        applicationInstanceDTO1.setId(2L)
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE()
        devopsEnvCommandE.setCommandType(CommandType.CREATE.getType())
        devopsEnvCommandE.setObjectId(2L)
        devopsEnvCommandE.setObject(ObjectType.INSTANCE.getType())
        devopsEnvCommandRepository.create(devopsEnvCommandE)
        DevopsEnvCommandE devopsEnvCommandE1 = new DevopsEnvCommandE()
        devopsEnvCommandE1.setCommandType(CommandType.CREATE.getType())
        devopsEnvCommandE1.setObjectId(1L)
        devopsEnvCommandE1.setObject(ObjectType.INSTANCE.getType())
        devopsEnvCommandRepository.create(devopsEnvCommandE1)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)
        DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO()
        devopsIngressPathDO.setPath("/")
        devopsIngressPathDO.setIngressId(2L)
        devopsIngressPathMapper.insert(devopsIngressPathDO)


        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        9 * envUtil.checkEnvConnection(_,_)
        3 * iamRepository.queryIamProject(_) >> projectE
        3 * iamRepository.queryOrganizationById(_) >> organization
        2 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        1 * devopsGitRepository.deleteTag(_, _, _) >> {tagDOS.clear()}
        1 * devopsGitRepository.createTag(_, _, _, _,_,_)
        1 * deployService.sendCommand(_)
        List<DevopsEnvFileResourceE> devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndPath(1, "test3.yaml")
        devopsEnvFileResourceE.size() == 3
    }


    def "move a instance,service,ingress file to other file"() {
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(true)
        diffE.setRenamedFile(false)
        diffE.setNewFile(false)
        diffE.setNewPath("test2.yaml")
        diffE.setOldPath("test2.yaml")
        diffES.add(diffE)
        DiffE diffE1 = new DiffE()
        diffE1.setDeletedFile(false)
        diffE1.setRenamedFile(false)
        diffE1.setNewFile(true)
        diffE1.setOldPath("test4.yaml")
        diffE1.setNewPath("test4.yaml")
        diffES.add(diffE1)
        compareResultsE.setDiffs(diffES)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE()
        devopsEnvCommandE.setId(4L)
        devopsEnvCommandE.setCommandType(CommandType.CREATE.getType())
        devopsEnvCommandE.setObjectId(2L)
        devopsEnvCommandE.setObject(ObjectType.INSTANCE.getType())
        devopsEnvCommandRepository.update(devopsEnvCommandE)


        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        3 * envUtil.checkEnvConnection(_,_)
        2 * iamRepository.queryIamProject(_) >> projectE
        2 * iamRepository.queryOrganizationById(_) >> organization
        2 * devopsGitRepository.getGitLabTags(_,_) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        1 * devopsGitRepository.deleteTag(_, _, _) >> {tagDOS.clear()}
        1 * devopsGitRepository.createTag(_, _, _, _,_,_)
        1 * deployService.sendCommand(_)
        List<DevopsEnvFileResourceE> devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndPath(1, "test2.yaml")
        devopsEnvFileResourceE.size() == 0
        List<DevopsEnvFileResourceE> newdevopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndPath(1, "test4.yaml")
        newdevopsEnvFileResourceE.size() == 3
    }

    def "create a same release object"() {

        given:
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(false)
        diffE.setRenamedFile(false)
        diffE.setNewFile(true)
        diffE.setOldPath("test5.yaml")
        diffE.setNewPath("test5.yaml")
        diffES.add(diffE)
        compareResultsE.setDiffs(diffES)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        1 * iamRepository.queryIamProject(_) >> projectE
        1 * iamRepository.queryOrganizationById(_) >> organization
        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.listByEnvId(1L)
        devopsEnvFileErrorE.get(0).getError() == "the another file already has the same object: instest2";
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
    }


    def "create a same service object"() {
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(false)
        diffE.setRenamedFile(false)
        diffE.setNewFile(true)
        diffE.setOldPath("test6.yaml")
        diffE.setNewPath("test6.yaml")
        diffES.add(diffE)
        compareResultsE.setDiffs(diffES)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        1 * iamRepository.queryIamProject(_) >> projectE
        1 * iamRepository.queryOrganizationById(_) >> organization
        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.listByEnvId(1L)
        devopsEnvFileErrorE.get(0).getError() == "the another file already has the same object: svctest2"
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))

    }


    def "create a same ingress object"() {
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(false)
        diffE.setRenamedFile(false)
        diffE.setNewFile(true)
        diffE.setOldPath("test7.yaml")
        diffE.setNewPath("test7.yaml")
        diffES.add(diffE)
        compareResultsE.setDiffs(diffES)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        1 * iamRepository.queryIamProject(_) >> projectE
        1 * iamRepository.queryOrganizationById(_) >> organization
        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.listByEnvId(1L)
        devopsEnvFileErrorE.get(0).getError() == "the another file already has the same object: ingtest2"
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))

    }

    def "create a release object when has no app"() {
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(false)
        diffE.setRenamedFile(false)
        diffE.setNewFile(true)
        diffE.setOldPath("test8.yaml")
        diffE.setNewPath("test8.yaml")
        diffES.add(diffE)
        compareResultsE.setDiffs(diffES)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        2 * iamRepository.queryIamProject(_) >> projectE
        2 * iamRepository.queryOrganizationById(_) >> organization
        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.listByEnvId(1L)
        devopsEnvFileErrorE.get(0).getError() == "the App: testappasdasdasdnot exist in the devops-service"
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))

    }


    def "create a service when has no instance"() {
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(false)
        diffE.setRenamedFile(false)
        diffE.setNewFile(true)
        diffE.setOldPath("test9.yaml")
        diffE.setNewPath("test9.yaml")
        diffES.add(diffE)
        compareResultsE.setDiffs(diffES)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        1 * iamRepository.queryIamProject(_) >> projectE
        1 * iamRepository.queryOrganizationById(_) >> organization
        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.listByEnvId(1L)
        devopsEnvFileErrorE.get(0).getError() == "The related instance of the service not found: instest212"
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))

    }



    def "create a ingress when has no service"() {
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(false)
        diffE.setRenamedFile(false)
        diffE.setNewFile(true)
        diffE.setOldPath("test10.yaml")
        diffE.setNewPath("test10.yaml")
        diffES.add(diffE)
        compareResultsE.setDiffs(diffES)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        1 * iamRepository.queryIamProject(_) >> projectE
        1 * iamRepository.queryOrganizationById(_) >> organization
        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.listByEnvId(1L)
        devopsEnvFileErrorE.get(0).getError() == "the related service of the ingress not exist:svctest2asd"
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
    }




    def "create a ingress when has same domain and path"() {
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(false)
        diffE.setRenamedFile(false)
        diffE.setNewFile(true)
        diffE.setOldPath("test11.yaml")
        diffE.setNewPath("test11.yaml")
        diffES.add(diffE)
        compareResultsE.setDiffs(diffES)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        1 * iamRepository.queryIamProject(_) >> projectE
        1 * iamRepository.queryOrganizationById(_) >> organization
        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.listByEnvId(1L)
        devopsEnvFileErrorE.get(0).getError() == "the ingress domain and path is already exist!"
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
    }


    def "create a object when has no name"() {
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO()
        pushWebHookDTO.setToken("123456")
        pushWebHookDTO.setProjectId(1)
        pushWebHookDTO.setUserId(1)
        Organization organization = new Organization()
        organization.setCode("test")
        ProjectE projectE = new ProjectE()
        projectE.setOrganization(organization)
        projectE.setCode("test")
        CompareResultsE compareResultsE = new CompareResultsE()
        List<DiffE> diffES = new ArrayList<>()
        DiffE diffE = new DiffE()
        diffE.setDeletedFile(false)
        diffE.setRenamedFile(false)
        diffE.setNewFile(true)
        diffE.setOldPath("test12.yaml")
        diffE.setNewPath("test12.yaml")
        diffES.add(diffE)
        compareResultsE.setDiffs(diffES)
        List<TagDO> tagDOS = new ArrayList<>()
        TagDO tagDO = new TagDO()
        tagDO.setName("devops-sync")
        tagDOS.add(tagDO)

        when:
        devopsGitService.fileResourceSync(pushWebHookDTO)

        then:
        1 * iamRepository.queryIamProject(_) >> projectE
        1 * iamRepository.queryOrganizationById(_) >> organization
        1 * devopsGitRepository.getGitLabTags(_, _) >> tagDOS
        1 * devopsGitRepository.getCompareResults(_, _, _) >> compareResultsE
        List<DevopsEnvFileErrorE> devopsEnvFileErrorE = devopsEnvFileErrorRepository.listByEnvId(1L)
        devopsEnvFileErrorE.get(0).getError() == "The C7nHelmRelease does not define name properties"
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE.get(0))
    }


    def cleanupSpec() {
        FileUtil.deleteDirectory(new File("gitops"))
    }
}
