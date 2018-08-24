package io.choerodon.devops.api.controller.v1

import com.alibaba.fastjson.JSONObject
import com.google.gson.JsonObject
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.ApplicationDeployDTO
import io.choerodon.devops.api.dto.DevopsServiceReqDTO
import io.choerodon.devops.domain.application.entity.ApplicationE
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE
import io.choerodon.devops.domain.application.entity.DevopsServiceE
import io.choerodon.devops.domain.application.entity.PortMapE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository
import io.choerodon.devops.domain.application.repository.ApplicationRepository
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository
import io.choerodon.devops.domain.application.repository.DevopsIngressRepository
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository
import io.choerodon.devops.domain.application.repository.DevopsServiceInstanceRepository
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository
import io.choerodon.devops.domain.application.repository.GitlabRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.repository.UserAttrRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.GitUtil
import io.choerodon.devops.infra.common.util.enums.AccessLevel
import io.choerodon.devops.infra.dataobject.DevopsProjectDO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class DevopsServiceControllerSpec extends Specification {

    private static int setupLabel = 0;
    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    @Qualifier("mockGitlabGroupMemberRepository")
    private GitlabGroupMemberRepository gitlabGroupMemberRepository;

    @Autowired
    private GitlabRepository gitlabRepository;

    @Autowired
    @Qualifier("mockEnvUtil")
    private EnvUtil envUtil;

    private MockMvc mockMvc;

    private UserAttrE userAttrE = new UserAttrE(1,1);


    def setup()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        if (setupLabel == 0)
        {
            List<PortMapE> ports = new ArrayList<>();
            PortMapE portMapE = new PortMapE();
            portMapE.setName("test");
            portMapE.setNodePort(8080);
            portMapE.setPort(8080);
            portMapE.setProtocol("TCP");
            portMapE.setTargetPort("8080");
            ports.add(portMapE);

            ProjectE projectE = new ProjectE(1L);
            projectE.setCode("test");
            Organization organization = new Organization(1);
            organization.setCode("test");
            projectE.setOrganization(organization);

            ProjectE projectE1 = new ProjectE(2L);
            projectE1.setCode("test2")
            projectE1.setOrganization(organization);

            DevopsEnvironmentE devopsEnvironmentE = new DevopsEnvironmentE();
            devopsEnvironmentE.setId(1L);
            devopsEnvironmentE.setCode("test");
            devopsEnvironmentE.setProjectE(projectE);
            devopsEnvironmentE.setGitlabEnvProjectId(1L);
            devopsEnvironmentRepository.create(devopsEnvironmentE);

            DevopsEnvironmentE devopsEnvironmentE2 = new DevopsEnvironmentE();
            devopsEnvironmentE2.setId(2L);
            devopsEnvironmentE2.setCode("test2");
            devopsEnvironmentE2.setProjectE(projectE1);
            devopsEnvironmentE2.setGitlabEnvProjectId(1L);
            devopsEnvironmentRepository.create(devopsEnvironmentE2);

            DevopsServiceE devopsServiceE = new DevopsServiceE();
            devopsServiceE.setId(1L);
            devopsServiceE.setName("test");
            devopsServiceE.setAppId(1);
            devopsServiceE.setEnvId(1);
            devopsServiceE.setPorts(ports);
            devopsServiceRepository.insert(devopsServiceE);

            DevopsServiceE devopsServiceE2 = new DevopsServiceE();
            devopsServiceE.setId(2L);
            devopsServiceE.setName("test");
            devopsServiceE.setAppId(1);
            devopsServiceE.setEnvId(2);
            devopsServiceE.setPorts(ports);
            devopsServiceRepository.insert(devopsServiceE);

            ApplicationE applicationE = new ApplicationE(1);
            applicationE.setName("test");
            applicationE.initProjectE(1);
            applicationE.initGitlabProjectE(1);
            applicationE.setCode("test");
            applicationRepository.create(applicationE);

            ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE();
            applicationInstanceE.setId(1L);
            applicationInstanceE.setCode("test");
            applicationInstanceE.initApplicationE(1L,"test");
            applicationInstanceE.initApplicationVersionE(1L,"1");
            applicationInstanceE.initDevopsEnvironmentE(1L,"test","test");
            applicationInstanceRepository.create(applicationInstanceE);

            DevopsProjectDO devopsProjectDO = new DevopsProjectDO(1);
            devopsProjectDO.setEnvGroupId(1);
            devopsProjectDO.setGitlabGroupId(1);
            devopsProjectRepository.createProject(devopsProjectDO);

            DevopsProjectDO devopsProjectDO2 = new DevopsProjectDO(2);
            devopsProjectDO2.setEnvGroupId(2);
            devopsProjectDO2.setGitlabGroupId(2);
            devopsProjectRepository.createProject(devopsProjectDO2);

            DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE.setId(1)
            devopsEnvFileResourceE.setEnvironment(devopsEnvironmentE)
            devopsEnvFileResourceE.setResourceId(1)
            devopsEnvFileResourceE.setFilePath("/Test.yml")
            devopsEnvFileResourceE.setResourceType("Service")

            DevopsEnvFileResourceE devopsEnvFileResourceE2 = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE2.setId(2)
            devopsEnvFileResourceE2.setEnvironment(devopsEnvironmentE2)
            devopsEnvFileResourceE2.setResourceId(2)
            devopsEnvFileResourceE2.setFilePath("/Test2.yml")
            devopsEnvFileResourceE2.setResourceType("Service")

            DevopsEnvFileResourceE devopsEnvFileResourceE3 = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE3.setId(3)
            devopsEnvFileResourceE3.setEnvironment(devopsEnvironmentE2)
            devopsEnvFileResourceE3.setResourceId(3)
            devopsEnvFileResourceE3.setFilePath("/Test2.yml")
            devopsEnvFileResourceE3.setResourceType("Service")

            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE2);
            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE3);
            userAttrRepository.insert(userAttrE);
            setupLabel = 1
        }
    }

    def "Create"() {
        given:
        ProjectE projectE = new ProjectE(1L);
        projectE.setCode("test");
        Organization organization = new Organization(1);
        organization.setCode("test");
        projectE.setOrganization(organization);
        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
        Map<String,String> labels = new HashMap<>();
        labels.put("test","test");

        List<PortMapE> ports = new ArrayList<>();
        PortMapE portMapE = new PortMapE();
        portMapE.setName("test");
        portMapE.setNodePort(8080);
        portMapE.setPort(8080);
        portMapE.setProtocol("TCP");
        portMapE.setTargetPort("8080");
        ports.add(portMapE);

        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO();
        devopsServiceReqDTO.setEnvId(1L);
        devopsServiceReqDTO.setAppId(1L);
        devopsServiceReqDTO.setName("test");
        devopsServiceReqDTO.setExternalIp("127.0.0.1");
        devopsServiceReqDTO.setType("ClusterIP");
        devopsServiceReqDTO.setPorts(ports);
        devopsServiceReqDTO.setLabel(labels);

        DevopsProjectDO devopsProjectDO = new DevopsProjectDO(1);
        devopsProjectDO.setEnvGroupId(1);
        devopsProjectDO.setGitlabGroupId(1);
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
//        devopsProjectRepository.createProject(devopsProjectDO);


        when:
        def e = testRestTemplate.postForEntity("/v1/projects/1/service",devopsServiceReqDTO,Object)
        then:
        userAttrRepository.queryById(_) >> userAttrE
        gitlabGroupMemberRepository.getUserMemberByUserId(_,_) >> groupMemberE;
        iamRepository.queryIamProject(_) >> projectE
        iamRepository.queryOrganizationById(_) >> organization
        e.statusCodeValue == 201


    }
    /**
     *update情况一： 不更改service名
     * */
    def "Update1"() {
        given:
        ProjectE projectE = new ProjectE(1L);
        projectE.setCode("test");
        Organization organization = new Organization(1);
        organization.setCode("test");
        projectE.setOrganization(organization);
        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
        Map<String,String> labels = new HashMap<>();
        labels.put("test","test");

        List<PortMapE> ports = new ArrayList<>();
        PortMapE portMapE = new PortMapE();
        portMapE.setName("test");
        portMapE.setNodePort(8080);
        portMapE.setPort(8080);
        portMapE.setProtocol("TCP");
        portMapE.setTargetPort("8080");
        ports.add(portMapE);

        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO();
        devopsServiceReqDTO.setEnvId(1L);
        devopsServiceReqDTO.setAppId(1L);
        devopsServiceReqDTO.setName("test");
        devopsServiceReqDTO.setExternalIp("127.0.0.2");
        devopsServiceReqDTO.setType("ClusterIP");
        devopsServiceReqDTO.setPorts(ports);
        devopsServiceReqDTO.setLabel(labels);

        DevopsProjectDO devopsProjectDO = new DevopsProjectDO(1);
        devopsProjectDO.setEnvGroupId(1);
        devopsProjectDO.setGitlabGroupId(1);
        when:
        testRestTemplate.put("/v1/projects/1/service/1",devopsServiceReqDTO);
        then:
        userAttrRepository.queryById(_) >> userAttrE
        gitlabGroupMemberRepository.getUserMemberByUserId(_,_) >> groupMemberE;
        iamRepository.queryIamProject(_) >> projectE
        iamRepository.queryOrganizationById(_) >> organization
        devopsServiceRepository.query(1).externalIp.equals("127.0.0.2")
        notThrown(Exception)
    }

    /**
     *update情况二： service名与数据库不对应
     * */
    def "Update2"() {
        given:
        ProjectE projectE = new ProjectE(1L);
        projectE.setCode("test");
        Organization organization = new Organization(1);
        organization.setCode("test");
        projectE.setOrganization(organization);
        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
        Map<String,String> labels = new HashMap<>();
        labels.put("test","test");

        List<PortMapE> ports = new ArrayList<>();
        PortMapE portMapE = new PortMapE();
        portMapE.setName("test");
        portMapE.setNodePort(8081);
        portMapE.setPort(8081);
        portMapE.setProtocol("TCP");
        portMapE.setTargetPort("8081");
        ports.add(portMapE);

        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO();
        devopsServiceReqDTO.setEnvId(1L);
        devopsServiceReqDTO.setAppId(1L);
        devopsServiceReqDTO.setName("test2");
        devopsServiceReqDTO.setExternalIp("127.0.0.1");
        devopsServiceReqDTO.setType("ClusterIP");
        devopsServiceReqDTO.setPorts(ports);
        devopsServiceReqDTO.setLabel(labels);

        when:
        String requestJson = JSONObject.toJSONString(devopsServiceReqDTO);
        testRestTemplate.put("/v1/projects/1/service/1",devopsServiceReqDTO);
        then:
        userAttrRepository.queryById(_) >> userAttrE
        gitlabGroupMemberRepository.getUserMemberByUserId(_,_) >> groupMemberE;
        iamRepository.queryIamProject(_) >> projectE
        iamRepository.queryOrganizationById(_) >> organization
        notThrown(Exception)
    }
    /**
     * delete情况一：单个kind配置
     * */
    def "Delete1"() {
        given:
        ProjectE projectE = new ProjectE(1L);
        projectE.setCode("test");
        Organization organization = new Organization(1);
        organization.setCode("test");
        projectE.setOrganization(organization);
        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
        when:
        testRestTemplate.delete("/v1/projects/1/service/1");
        then:
        iamRepository.queryIamProject(_) >> projectE
        iamRepository.queryOrganizationById(_) >> organization
        userAttrRepository.queryById(_) >> userAttrE
        gitlabGroupMemberRepository.getUserMemberByUserId(_,_) >> groupMemberE;
        1 * gitlabRepository.deleteFile(_,_,_,_)
        notThrown(Exception)
    }
    /**
     * delete情况二：多个kind配置+
     *
     * */
    def "Delete2"()
    {
        given:
        ProjectE projectE = new ProjectE(1L);
        projectE.setCode("test");
        Organization organization = new Organization(1);
        organization.setCode("test");
        projectE.setOrganization(organization);
        GitlabGroupMemberE groupMemberE = new GitlabGroupMemberE();
        groupMemberE.setAccessLevel(AccessLevel.OWNER.toValue());
        when:
        testRestTemplate.delete("/v1/projects/1/service/2");
        then:
        iamRepository.queryIamProject(_) >> projectE
        iamRepository.queryOrganizationById(_) >> organization
        userAttrRepository.queryById(_) >> userAttrE
        gitlabGroupMemberRepository.getUserMemberByUserId(_,_) >> groupMemberE;
        1 * gitlabRepository.updateFile(_,_,_,_,_)
        notThrown(Exception)


    }
}
