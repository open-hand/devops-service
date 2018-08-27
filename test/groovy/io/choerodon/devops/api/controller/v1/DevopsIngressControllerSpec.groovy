package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsIngressDTO
import io.choerodon.devops.api.dto.DevopsIngressPathDTO
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE
import io.choerodon.devops.domain.application.entity.DevopsIngressE
import io.choerodon.devops.domain.application.entity.DevopsServiceE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository
import io.choerodon.devops.domain.application.repository.DevopsIngressRepository
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository
import io.choerodon.devops.domain.application.repository.GitlabRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.repository.UserAttrRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.GitUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.common.util.enums.IngressStatus
import io.choerodon.devops.infra.dataobject.DevopsIngressPathDO
import io.choerodon.devops.infra.dataobject.DevopsProjectDO
import io.choerodon.devops.infra.mapper.DevopsIngressPathMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class DevopsIngressControllerSpec extends Specification {

    private static int setupLabel = 0;

    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper;
    @Autowired
    @Qualifier("mockGitUtil")
    private GitUtil gitUtil;
    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository;
    @Autowired
    @Qualifier("mockGitlabGroupMemberRepository")
    private GitlabGroupMemberRepository gitlabGroupMemberRepository;
    @Autowired
    @Qualifier("mockUserAttrRepository")
    private UserAttrRepository userAttrRepository;
    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil;
    @Autowired
    @Qualifier("mockGitlabRepository")
    private GitlabRepository gitlabRepository;

    def setup()
    {
        if (setupLabel == 0)
        {
            ProjectE projectE = new ProjectE();
            projectE.setId(1L);
            projectE.setCode("test");
            Organization organization = new Organization();
            organization.setId(1L);
            organization.setCode("test")
            projectE.setOrganization(organization);
            DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE();
            devopsEnvironmentE.setId(1L);
            devopsEnvironmentE.setName("test");
            devopsEnvironmentE.setCode("test");
            devopsEnvironmentE.setProjectE(projectE);
            devopsEnvironmentE.setGitlabEnvProjectId(1L);
            devopsEnvironmentRepository.create(devopsEnvironmentE);

            ProjectE projectE2 = new ProjectE();
            projectE2.setId(2L);
            projectE2.setCode("test");
            Organization organization2 = new Organization();
            organization2.setId(2L);
            organization2.setCode("test")
            projectE2.setOrganization(organization2);

            DevopsEnvironmentE devopsEnvironmentE2 = new DevopsEnvironmentE();
            devopsEnvironmentE2.setId(2L);
            devopsEnvironmentE2.setName("test2");
            devopsEnvironmentE2.setCode("test2");
            devopsEnvironmentE2.setProjectE(projectE2);
            devopsEnvironmentE2.setGitlabEnvProjectId(1L);
            devopsEnvironmentRepository.create(devopsEnvironmentE2);
            DevopsServiceE devopsServiceE = new DevopsServiceE();
            devopsServiceE.setId(1L);
            devopsServiceE.setEnvId(1L);
            devopsServiceE.setName("test");
            devopsServiceRepository.insert(devopsServiceE);
            DevopsProjectDO devopsProjectDO = new DevopsProjectDO();
            devopsProjectDO.setGitlabGroupId(1)
            devopsProjectDO.setEnvGroupId(1);
            devopsProjectDO.setId(1L);
            devopsProjectRepository.createProject(devopsProjectDO);

            DevopsProjectDO devopsProjectDO2 = new DevopsProjectDO();
            devopsProjectDO2.setGitlabGroupId(2)
            devopsProjectDO2.setEnvGroupId(2);
            devopsProjectDO2.setId(2L);
            devopsProjectRepository.createProject(devopsProjectDO2);

            DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE.setId(1)
            devopsEnvFileResourceE.setEnvironment(devopsEnvironmentE)
            devopsEnvFileResourceE.setResourceId(1)
            devopsEnvFileResourceE.setFilePath("/test1.yaml")
            devopsEnvFileResourceE.setResourceType("Ingress")
            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);

            DevopsEnvFileResourceE devopsEnvFileResourceE2 = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE2.setId(2)
            devopsEnvFileResourceE2.setEnvironment(devopsEnvironmentE2)
            devopsEnvFileResourceE2.setResourceId(2)
            devopsEnvFileResourceE2.setFilePath("/test2.yaml")
            devopsEnvFileResourceE2.setResourceType("Ingress")
            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE2);


            DevopsEnvFileResourceE devopsEnvFileResourceE3 = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE3.setId(3)
            devopsEnvFileResourceE3.setEnvironment(devopsEnvironmentE2)
            devopsEnvFileResourceE3.setResourceId(3)
            devopsEnvFileResourceE3.setFilePath("/test2.yaml")
            devopsEnvFileResourceE3.setResourceType("Ingress")
            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE3);


            DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO();
            devopsIngressPathDO.setPath("/bootz");
            devopsIngressPathDO.setServiceId(1L);
            devopsIngressPathDO.setServiceName("test");
            devopsIngressPathDO.setIngressId(1);
            devopsIngressPathMapper.insert(devopsIngressPathDO);

            setupLabel = 1;
        }
    }

    def "Create"() {
        given:
        UserAttrE userAttrE = new UserAttrE(1,1);

        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        devopsIngressPathDTO.setPath("/bootz");
        devopsIngressPathDTO.setServiceId(1L);
        devopsIngressPathDTO.setServiceName("test");
        devopsIngressPathDTO.setServiceStatus("running");

        List<DevopsIngressPathDTO> pathList = new ArrayList<>();
        pathList.add(devopsIngressPathDTO);

        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setId(1L);
        devopsIngressDTO.setDomain("389a6.hand-china.com");
        devopsIngressDTO.setName("dname0822");
        devopsIngressDTO.setEnvId(1L);
        devopsIngressDTO.setEnvStatus(true);
        devopsIngressDTO.setUsable(true);
        devopsIngressDTO.setPathList(pathList);

        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());

        ProjectE projectE = new ProjectE();
        projectE.setId(1L);
        projectE.setCode("test");
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setCode("test");
        projectE.setOrganization(organization);
        when:

        def e =testRestTemplate.postForEntity("/v1/projects/1/ingress",devopsIngressDTO,Object);

        then:
        userAttrRepository.queryById(_) >> userAttrE;
        gitlabGroupMemberRepository.getUserMemberByUserId(_,_) >> groupMemberE;
        iamRepository.queryIamProject(_) >> projectE;
        iamRepository.queryOrganizationById(_) >> organization;
        e.statusCodeValue == 204
        devopsIngressRepository.getIngress(1).name.equals("dname0822");
    }

    def "Update"() {
        given:
        UserAttrE userAttrE = new UserAttrE(1,1);

        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        devopsIngressPathDTO.setPath("/bootz2");
        devopsIngressPathDTO.setServiceId(1L);
        devopsIngressPathDTO.setServiceName("test");
        devopsIngressPathDTO.setServiceStatus();
        List<DevopsIngressPathDTO> pathList = new ArrayList<>();
        pathList.add(devopsIngressPathDTO);

        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setId(1L);
        devopsIngressDTO.setDomain("1389a6.hand-china.com2");
        devopsIngressDTO.setName("dname0822");
        devopsIngressDTO.setEnvId(1L);
        devopsIngressDTO.setEnvStatus(true);
        devopsIngressDTO.setUsable(true);
        devopsIngressDTO.setPathList(pathList);

        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());

        ProjectE projectE = new ProjectE();
        projectE.setId(1L);
        projectE.setCode("test");
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setCode("test");
        projectE.setOrganization(organization);
        when:

        testRestTemplate.put("/v1/projects/1/ingress/1",devopsIngressDTO);

        then:
        userAttrRepository.queryById(_) >> userAttrE;
        gitlabGroupMemberRepository.getUserMemberByUserId(_,_) >> groupMemberE;
        iamRepository.queryIamProject(_) >> projectE;
        iamRepository.queryOrganizationById(_) >> organization;
        devopsIngressRepository.getIngress(1L).getDomain().equals("1389a6.hand-china.com2");
    }

    def "Delete"() {
        given:
        UserAttrE userAttrE = new UserAttrE(1,1);

        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        devopsIngressPathDTO.setPath("/bootX");
        devopsIngressPathDTO.setServiceId(1L);
        devopsIngressPathDTO.setServiceName("test");
        List<DevopsIngressPathDTO> pathList = new ArrayList<>();
        pathList.add(devopsIngressPathDTO);

        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setId(1L);
        devopsIngressDTO.setDomain("1389a6.hand-china.com");
        devopsIngressDTO.setName("dname0822");
        devopsIngressDTO.setEnvId(1L);
        devopsIngressDTO.setEnvStatus(true);
        devopsIngressDTO.setUsable(true);
        devopsIngressDTO.setPathList(pathList);

        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());

        ProjectE projectE = new ProjectE();
        projectE.setId(1L);
        projectE.setCode("test");
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setCode("test");
        projectE.setOrganization(organization);
        when:

        testRestTemplate.delete("/v1/projects/1/ingress/1");
        then:
        userAttrRepository.queryById(_) >> userAttrE;
        gitlabGroupMemberRepository.getUserMemberByUserId(_,_) >> groupMemberE;
        iamRepository.queryIamProject(_) >> projectE;
        iamRepository.queryOrganizationById(_) >> organization;
        1 * gitlabRepository.deleteFile(_,_,_,_);
    }

    def "Delete2"() {
        given:
        UserAttrE userAttrE = new UserAttrE(1,1);

        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        devopsIngressPathDTO.setPath("/bootz");
        devopsIngressPathDTO.setServiceId(1L);
        devopsIngressPathDTO.setServiceName("test");
        devopsIngressPathDTO.setServiceStatus();
        List<DevopsIngressPathDTO> pathList = new ArrayList<>();
        pathList.add(devopsIngressPathDTO);

        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setId(2L);
        devopsIngressDTO.setDomain("1389a6.hand-china.com");
        devopsIngressDTO.setName("dname0822");
        devopsIngressDTO.setEnvId(2L);
        devopsIngressDTO.setEnvStatus(true);
        devopsIngressDTO.setUsable(true);
        devopsIngressDTO.setPathList(pathList);

        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());

        ProjectE projectE = new ProjectE();
        projectE.setId(1L);
        projectE.setCode("test");
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setCode("test");
        projectE.setOrganization(organization);
        when:
        testRestTemplate.postForEntity("/v1/projects/1/ingress",devopsIngressDTO,Object);
        testRestTemplate.delete("/v1/projects/1/ingress/2");
        then:
        userAttrRepository.queryById(_) >> userAttrE;
        gitlabGroupMemberRepository.getUserMemberByUserId(_,_) >> groupMemberE;
        iamRepository.queryIamProject(_) >> projectE;
        iamRepository.queryOrganizationById(_) >> organization;
        devopsIngressRepository.getIngress(2L).status.equals(IngressStatus.OPERATING.getStatus())
    }
}
