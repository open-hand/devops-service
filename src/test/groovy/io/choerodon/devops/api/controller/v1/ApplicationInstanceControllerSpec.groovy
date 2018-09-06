package io.choerodon.devops.api.controller.v1
//package io.choerodon.devops.api.controller.v1
//
//import io.choerodon.devops.IntegrationTestConfiguration
//import io.choerodon.devops.api.dto.ApplicationDeployDTO
//import io.choerodon.devops.app.service.ApplicationInstanceService
//import io.choerodon.devops.domain.application.entity.ApplicationE
//import io.choerodon.devops.domain.application.entity.ApplicationInstanceE
//import io.choerodon.devops.domain.application.entity.ApplicationVersionE
//import io.choerodon.devops.domain.application.entity.ApplicationVersionValueE
//import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE
//import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE
//import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE
//import io.choerodon.devops.domain.application.entity.ProjectE
//import io.choerodon.devops.domain.application.entity.UserAttrE
//import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE
//import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository
//import io.choerodon.devops.domain.application.repository.ApplicationRepository
//import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository
//import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository
//import io.choerodon.devops.domain.application.repository.DevopsEnvCommandValueRepository
//import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository
//import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository
//import io.choerodon.devops.domain.application.repository.DevopsProjectRepository
//import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository
//import io.choerodon.devops.domain.application.repository.GitlabRepository
//import io.choerodon.devops.domain.application.repository.IamRepository
//import io.choerodon.devops.domain.application.repository.UserAttrRepository
//import io.choerodon.devops.domain.application.valueobject.ApplicationVersionReadmeV
//import io.choerodon.devops.domain.application.valueobject.Organization
//import io.choerodon.devops.infra.common.util.EnvUtil
//import io.choerodon.devops.infra.common.util.FileUtil
//import io.choerodon.devops.infra.common.util.GitUtil
//import io.choerodon.devops.infra.common.util.enums.AccessLevel
//import io.choerodon.devops.infra.common.util.enums.CommandType
//import io.choerodon.devops.infra.common.util.enums.InstanceStatus
//import io.choerodon.devops.infra.common.util.enums.ObjectType
//import io.choerodon.devops.infra.dataobject.DevopsProjectDO
//import io.choerodon.websocket.helper.EnvListener
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Qualifier
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.web.client.TestRestTemplate
//import org.springframework.context.annotation.Import
//import spock.lang.Specification
//
//import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
//
//@SpringBootTest(webEnvironment = RANDOM_PORT)
//@Import(IntegrationTestConfiguration)
//class ApplicationInstanceControllerSpec extends Specification {
//
//    private static int initLabel = 0;
//
//    @Autowired
//    TestRestTemplate testRestTemplate;
//    @Autowired
//    private ApplicationVersionRepository applicationVersionRepository;
//    @Autowired
//    private DevopsEnvCommandValueRepository devopsEnvCommandValueRepository;
//    @Autowired
//    private DevopsEnvCommandRepository devopsEnvCommandRepository
//    @Autowired
//    private ApplicationInstanceRepository applicationInstanceRepositoryImpl;
//    @Autowired
//    private DevopsEnvironmentRepository devopsEnvironmentRepository;
//    @Autowired
//    private UserAttrRepository userAttrRepository;
//    @Autowired
//    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository
//    @Autowired
//    private DevopsProjectRepository devopsProjectRepository;
//    @Autowired
//    private ApplicationRepository applicationRepository;
//    @Autowired
//    private ApplicationInstanceRepository applicationInstanceRepository
//    @Autowired
//    private ApplicationInstanceService  applicationInstanceService
//    @Autowired
//    @Qualifier("mockGitlabGroupMemberRepository")
//    GitlabGroupMemberRepository gitlabGroupMemberRepository
//    @Autowired
//    @Qualifier("mockGitlabRepository")
//    private GitlabRepository gitlabRepository;
//    @Autowired
//    @Qualifier("mockIamRepository")
//    private IamRepository iamRepository;
//    @Autowired
//    @Qualifier("mockEnvListener")
//    private EnvListener envListener;
//    @Autowired
//    @Qualifier("mockEnvUtil")
//    private EnvUtil envUtil;
//    @Autowired
//    @Qualifier("mockGitUtil")
//    private GitUtil gitUtil;
//
//    def setup() {
//        FileUtil.copyFile("test/gitops/test1.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test2.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test3.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test4.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test5.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test6.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test7.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test8.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test9.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test10.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test11.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test12.yaml", "gitops/test/test/test")
//        FileUtil.copyFile("test/gitops/test2.yaml", "gitops/test/test/test2")
//        if (initLabel == 0) {
//            DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE(1L);
//            ProjectE projectE = new ProjectE(1L);
//            projectE.setCode("test");
//            Organization organization = new Organization(1);
//            organization.setCode("test")
//            projectE.setOrganization(organization);
//            devopsEnvironmentE.setProjectE(projectE);
//            devopsEnvironmentE.setGitlabEnvProjectId(1);
//            devopsEnvironmentE.setCode("test");
//            devopsEnvironmentE.setName("test");
//            devopsEnvironmentRepository.create(devopsEnvironmentE);
//
//            DevopsEnvironmentE devopsEnvironmentE2 = new DevopsEnvironmentE(2);
//            ProjectE projectE2 = new ProjectE(2L);
//            projectE2.setCode("test2");
//            Organization organization2 = new Organization(2);
//            organization2.setCode("test2")
//            projectE2.setOrganization(organization2);
//            devopsEnvironmentE2.setProjectE(projectE2);
//            devopsEnvironmentE2.setGitlabEnvProjectId(2);
//            devopsEnvironmentE2.setCode("test2");
//            devopsEnvironmentE2.setName("test2");
//            devopsEnvironmentRepository.create(devopsEnvironmentE2);
//
//
//            DevopsProjectDO devopsProjectDO = new DevopsProjectDO(1)
//            devopsProjectDO.setEnvGroupId(1)
//            devopsProjectDO.setGitlabGroupId(1)
//            devopsProjectRepository.createProject(devopsProjectDO);
//
//            DevopsProjectDO devopsProjectDO2 = new DevopsProjectDO(2)
//            devopsProjectDO2.setEnvGroupId(2)
//            devopsProjectDO2.setGitlabGroupId(2)
//            devopsProjectRepository.createProject(devopsProjectDO2);
//
//            UserAttrE userAttrE = new UserAttrE(1, 1);
//            userAttrRepository.insert(userAttrE);
//
//            GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
//            groupMemberE.setId(1);
//            groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
//
//            ApplicationE applicationE = new ApplicationE();
//            applicationE.setId(1L);
//            applicationE.setName("test")
//            applicationE.setProjectE(projectE);
//            applicationE.setCode("test")
//            applicationRepository.create(applicationE);
//
//            ApplicationVersionE applicationVersionE = new ApplicationVersionE(1L);
//            applicationVersionE.setVersion("0.8.0")
//            applicationVersionE.setApplicationE(applicationE);
//            ApplicationVersionReadmeV applicationVersionReadmeV = new ApplicationVersionReadmeV(1);
//            applicationVersionE.setApplicationVersionReadmeV(applicationVersionReadmeV);
//            applicationVersionE.setApplicationVersionValueE(new ApplicationVersionValueE(1));
//            applicationVersionRepository.create(applicationVersionE);
//
//            ApplicationVersionE applicationVersionE2 = new ApplicationVersionE(2L);
//            applicationVersionE2.setVersion("0.9.0")
//            applicationVersionE2.setApplicationE(applicationE);
//            ApplicationVersionReadmeV applicationVersionReadmeV2 = new ApplicationVersionReadmeV(2);
//            applicationVersionE2.setApplicationVersionReadmeV(applicationVersionReadmeV2);
//            applicationVersionE2.setApplicationVersionValueE(new ApplicationVersionValueE(2));
//            applicationVersionRepository.create(applicationVersionE2);
//
//            ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE(1L);
//            applicationInstanceE.setCode("test");
//            applicationInstanceE.setApplicationE(applicationE);
//            applicationInstanceE.setApplicationVersionE(applicationVersionE)
//            applicationInstanceE.setDevopsEnvironmentE(devopsEnvironmentE);
//            applicationInstanceRepository.create(applicationInstanceE);
//
//            DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
//            devopsEnvFileResourceE.setId(1)
//            devopsEnvFileResourceE.setEnvironment(devopsEnvironmentE)
//            devopsEnvFileResourceE.setResourceId(1)
//            devopsEnvFileResourceE.setFilePath("/test1.yaml")
//            devopsEnvFileResourceE.setResourceType("C7NHelmRelease")
//
//            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
//
//            DevopsEnvFileResourceE devopsEnvFileResourceE2 = new DevopsEnvFileResourceE();
//            devopsEnvFileResourceE2.setId(2)
//            devopsEnvFileResourceE2.setEnvironment(devopsEnvironmentE2)
//            devopsEnvFileResourceE2.setResourceId(2)
//            devopsEnvFileResourceE2.setFilePath("/test2.yaml")
//            devopsEnvFileResourceE2.setResourceType("C7NHelmRelease")
//            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE2);
//
//            DevopsEnvFileResourceE devopsEnvFileResourceE3 = new DevopsEnvFileResourceE();
//            devopsEnvFileResourceE3.setId(3)
//            devopsEnvFileResourceE3.setEnvironment(devopsEnvironmentE2)
//            devopsEnvFileResourceE3.setResourceId(3)
//            devopsEnvFileResourceE3.setFilePath("/test2.yaml")
//            devopsEnvFileResourceE3.setResourceType("C7NHelmRelease")
//            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE3);
//            initLabel = 1
//        }
//
//    }
//
//    def "create"() {
//        given:
//        ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO();
//        applicationDeployDTO.setEnvironmentId(2L);
//        applicationDeployDTO.setAppId(1L);
//        applicationDeployDTO.setAppInstanceId(2L);
//        applicationDeployDTO.setAppVerisonId(1L);
//        applicationDeployDTO.setValues("test2");
//        applicationDeployDTO.setType("create");
//        applicationDeployDTO.setInstanceName("test2");
//
//        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE(1L);
//        ProjectE projectE = new ProjectE(1L);
//        projectE.setCode("test");
//
//        Organization organization = new Organization(1);
//        organization.setCode("test")
//
//        projectE.setOrganization(organization);
//        devopsEnvironmentE.setProjectE(projectE);
//        devopsEnvironmentE.setGitlabEnvProjectId(1);
//        devopsEnvironmentE.setCode("test");
//        devopsEnvironmentE.setName("test");
//
//        DevopsProjectDO devopsProjectDO = new DevopsProjectDO(1)
//        devopsProjectDO.setEnvGroupId(1)
//        devopsProjectDO.setGitlabGroupId(1)
//
//        UserAttrE userAttrE = new UserAttrE(1, 1);
//
//        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
//        groupMemberE.setId(1);
//        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
//
//        ApplicationE applicationE = new ApplicationE();
//        applicationE.setId(1L);
//        applicationE.setName("test")
//        applicationE.setProjectE(projectE);
//        applicationE.setCode("test")
//
//        ApplicationVersionE applicationVersionE = new ApplicationVersionE(1L);
//        applicationVersionE.setVersion("0.8.0")
//        applicationVersionE.setApplicationE(applicationE);
//        ApplicationVersionReadmeV applicationVersionReadmeV = new ApplicationVersionReadmeV(1);
//        applicationVersionE.setApplicationVersionReadmeV(applicationVersionReadmeV);
//        applicationVersionE.setApplicationVersionValueE(new ApplicationVersionValueE(1));
//
//        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
//        devopsEnvFileResourceE.setId(1)
//        devopsEnvFileResourceE.setEnvironment(devopsEnvironmentE)
//        devopsEnvFileResourceE.setResourceId(1)
//        devopsEnvFileResourceE.setFilePath("/Test.yml")
//        devopsEnvFileResourceE.setResourceType("C7NHelmRelease")
//
//        when:
//        def e = testRestTemplate.postForEntity("/v1/projects/1/app_instances", applicationDeployDTO, ApplicationDeployDTO);
//
//        then:
//        userAttrRepository.queryById(_) >> userAttrE
//        gitlabGroupMemberRepository.getUserMemberByUserId(_, _) >> groupMemberE
//        applicationVersionRepository.query(_) >> applicationVersionE
//        applicationVersionRepository.queryValue(_) >> null
//        iamRepository.queryIamProject(_) >> projectE
//        iamRepository.queryOrganizationById(_) >> organization
//        ApplicationInstanceE result = applicationInstanceRepository.selectById(2L);
//        result.getApplicationE().getId() == 1;
//        result.getDevopsEnvironmentE().id == 2;
//        result.getApplicationVersionE().id == 1;
//        e.getStatusCode().value() == 200
//    }
//
//    def "update"() {
//        given:
//        ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO();
//        applicationDeployDTO.setEnvironmentId(1L);
//        applicationDeployDTO.setAppId(1L);
//        applicationDeployDTO.setAppInstanceId(1L);
//        applicationDeployDTO.setAppVerisonId(2L);
//        applicationDeployDTO.setValues("test");
//        applicationDeployDTO.setType("update");
//        applicationDeployDTO.setInstanceName("test");
//
//        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE(1L);
//        ProjectE projectE = new ProjectE(1L);
//        projectE.setCode("test");
//        Organization organization = new Organization(1);
//        organization.setCode("test")
//        projectE.setOrganization(organization);
//        devopsEnvironmentE.setProjectE(projectE);
//        devopsEnvironmentE.setGitlabEnvProjectId(1);
//        devopsEnvironmentE.setCode("test");
//        devopsEnvironmentE.setName("test");
//
//        DevopsProjectDO devopsProjectDO = new DevopsProjectDO(1)
//        devopsProjectDO.setEnvGroupId(1)
//        devopsProjectDO.setGitlabGroupId(1)
//
//        UserAttrE userAttrE = new UserAttrE(1, 1);
//
//        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
//        groupMemberE.setId(1);
//        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
//
//        ApplicationE applicationE = new ApplicationE();
//        applicationE.setId(1L);
//        applicationE.setName("test")
//        applicationE.setProjectE(projectE);
//        applicationE.setCode("test")
//
//        ApplicationVersionE applicationVersionE = new ApplicationVersionE(1L);
//        applicationVersionE.setVersion("0.8.0")
//        applicationVersionE.setApplicationE(applicationE);
//        ApplicationVersionReadmeV applicationVersionReadmeV = new ApplicationVersionReadmeV(1);
//        applicationVersionE.setApplicationVersionReadmeV(applicationVersionReadmeV);
//        applicationVersionE.setApplicationVersionValueE(new ApplicationVersionValueE(1));
//
//        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
//        devopsEnvFileResourceE.setId(1)
//        devopsEnvFileResourceE.setEnvironment(devopsEnvironmentE)
//        devopsEnvFileResourceE.setResourceId(1)
//        devopsEnvFileResourceE.setFilePath("/Test.yml")
//        devopsEnvFileResourceE.setResourceType("C7NHelmRelease")
//
//        when:
//        def e = testRestTemplate.postForEntity("/v1/projects/1/app_instances", applicationDeployDTO, ApplicationDeployDTO);
//
//        then:
//        userAttrRepository.queryById(_) >> userAttrE
//        gitlabGroupMemberRepository.getUserMemberByUserId(_, _) >> groupMemberE
//        applicationVersionRepository.query(_) >> applicationVersionE
//        applicationVersionRepository.queryValue(_) >> null
//        iamRepository.queryIamProject(_) >> projectE
//        iamRepository.queryOrganizationById(_) >> organization
//        applicationInstanceRepository.selectById(1L).applicationVersionE.id==2
//        e.statusCodeValue == 200
//    }
//    /*
//    * 删除情况一:配置文件单kind情况
//    * **/
//    def "delete1"() {
//        given:
//        ProjectE projectE = new ProjectE(1L);
//        projectE.setCode("test");
//        Organization organization = new Organization(1);
//        organization.setCode("test")
//        projectE.setOrganization(organization);
//
//        ApplicationE applicationE = new ApplicationE();
//        applicationE.setId(1L);
//        applicationE.setName("test")
//        applicationE.setProjectE(projectE);
//        applicationE.setCode("test")
//
//        DevopsProjectDO devopsProjectDO = new DevopsProjectDO(1)
//        devopsProjectDO.setEnvGroupId(1)
//        devopsProjectDO.setGitlabGroupId(1)
//
//        UserAttrE userAttrE = new UserAttrE(1, 1);
//
//        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
//        groupMemberE.setId(1);
//        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
//
//        ApplicationVersionE applicationVersionE = new ApplicationVersionE(1L);
//        applicationVersionE.setVersion("0.8.0")
//        applicationVersionE.setApplicationE(applicationE);
//        ApplicationVersionReadmeV applicationVersionReadmeV = new ApplicationVersionReadmeV(1);
//        applicationVersionE.setApplicationVersionReadmeV(applicationVersionReadmeV);
//        applicationVersionE.setApplicationVersionValueE(new ApplicationVersionValueE(1));
//
//
//        when:
//        testRestTemplate.delete("/v1/projects/1/app_instances/1/delete");
//
//        then:
//        userAttrRepository.queryById(_) >> userAttrE
//        gitlabGroupMemberRepository.getUserMemberByUserId(_, _) >> groupMemberE
//        applicationVersionRepository.query(_) >> applicationVersionE
//        applicationVersionRepository.queryValue(_) >> null
//        iamRepository.queryIamProject(_) >> projectE
//        iamRepository.queryOrganizationById(_) >> organization
//        1 * gitlabRepository.deleteFile(_,_,_,_);
//        applicationInstanceRepository.selectById(1).status.equals(InstanceStatus.OPERATIING.getStatus());
//    }
//    /*
//    * 删除情况二:配置文件多kind情况
//    * **/
//    def "delete2"() {
//        given:
//        DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE(1L);
//        ProjectE projectE = new ProjectE(1L);
//        projectE.setCode("test");
//        Organization organization = new Organization(1);
//        organization.setCode("test")
//        projectE.setOrganization(organization);
//        devopsEnvironmentE.setProjectE(projectE);
//        devopsEnvironmentE.setGitlabEnvProjectId(1);
//        devopsEnvironmentE.setCode("test");
//        devopsEnvironmentE.setName("test");
//
//        DevopsProjectDO devopsProjectDO = new DevopsProjectDO(1)
//        devopsProjectDO.setEnvGroupId(1)
//        devopsProjectDO.setGitlabGroupId(1)
//
//        UserAttrE userAttrE = new UserAttrE(1, 1);
//
//        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
//        groupMemberE.setId(1);
//        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
//
//        ApplicationE applicationE = new ApplicationE();
//        applicationE.setId(1L);
//        applicationE.setName("test")
//        applicationE.setProjectE(projectE);
//        applicationE.setCode("test")
//
//
//        when:
//        testRestTemplate.delete("/v1/projects/1/app_instances/2/delete");
//
//        then:
//        userAttrRepository.queryById(_) >> userAttrE;
//        gitlabGroupMemberRepository.getUserMemberByUserId(_, _) >> groupMemberE;
//        applicationVersionRepository.queryValue(_) >> null;
//        iamRepository.queryIamProject(_) >> projectE;
//        iamRepository.queryOrganizationById(_) >> organization;
//        devopsEnvFileResourceRepository.queryByEnvIdAndPath(2,"/test2.yaml").size()>1;
//        applicationInstanceRepository.selectById(2).status.equals(InstanceStatus.OPERATIING.getStatus());
//    }
//}