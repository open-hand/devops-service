package io.choerodon.devops.api.controller.v1

import io.choerodon.asgard.saga.producer.TransactionalProducer
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsPvReqVo
import io.choerodon.devops.app.service.DevopsClusterService
import io.choerodon.devops.app.service.DevopsEnvCommandService
import io.choerodon.devops.app.service.DevopsEnvFileResourceService
import io.choerodon.devops.app.service.DevopsEnvironmentService
import io.choerodon.devops.app.service.DevopsPvProPermissionService
import io.choerodon.devops.app.service.DevopsPvServcie
import io.choerodon.devops.app.service.UserAttrService
import io.choerodon.devops.infra.dto.DevopsClusterDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.DevopsProjectDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.DevopsPvMapper
import io.choerodon.devops.infra.mapper.DevopsPvcMapper
import javafx.beans.binding.When
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsPvController)
@Stepwise
class DevopsPvControllerSpec extends Specification {

    private static final String MAPPING = "/v1/projects/{project_id}/pv"

    @Autowired
    DevopsPvMapper devopsPvMapper;
    @Autowired
    DevopsPvcMapper devopsPvcMapper
    @Autowired
    BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    DevopsPvProPermissionService devopsPvProPermissionService;
    @Autowired
    ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    DevopsClusterService devopsClusterService;
    @Autowired
    UserAttrService userAttrService;
    @Autowired
    DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    TestRestTemplate restTemplate;

    @Shared
    Long project_id = 1L;
    @Shared
    Long cluster_id = 1L;

    TransactionalProducer producer = Mockito.mock(TransactionalProducer.class)
    BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient.class)
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)


    def setup(){
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setId(1L);
        devopsClusterDTO.setSystemEnvId(1L);
        Mockito.doReturn(devopsClusterDTO).when()


        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setId(1L)

        ProjectDTO projectDTO = new ProjectDTO()
        projectDTO.setId(1L)
        projectDTO.setName("pro")
        projectDTO.setOrganizationId(1L)
        Mockito.doReturn(new ResponseEntity(projectDTO, HttpStatus.OK)).when(baseServiceClient).queryIamProject(1L)


        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(1L)

    }

    def cleanup() {
        // 删除pv
        devopsPvMapper.selectAll().forEach { devopsPvMapper.delete(it) }
        // 删除pvc
        devopsPvcMapper.select().forEach { devopsPvcMapper.delete(it)}
        //
    }


    def "create"(){
        given: "创建devopsPvReqVO"
        DevopsPvReqVo devopsPvReqVo = new DevopsPvReqVo();
        devopsPvReqVo.setType("NFS");
        devopsPvReqVo.setRequestResource("1Gi")
        devopsPvReqVo.setAccessModes("ReadWriteMany")
        devopsPvReqVo.setClusterId(1L);
        devopsPvReqVo.setName("pv-101");
        devopsPvReqVo.setDescription("description");
        devopsPvReqVo.setSkipCheckProjectPermission(true);

        when: '创建PV'
        def entity = restTemplate.postForEntity(MAPPING,devopsPvReqVo, ResponseEntity.class, project_id);

        then: '校验结果'
        entity.getStatusCode().is2xxSuccessful()
    }

    def "check_name"(){
        given:
        Long clusterId = 1L;
        String pvName = "pv-101";

        when:
        def entity = restTemplate.getForEntity(MAPPING  + "/check_name?clusterId={clusterId}&pvName={pvName}",ResponseEntity.class,
                clusterId, pvName);

        then:
        entity.statusCode.is2xxSuccessful()

    }

    def "delete" (){

        when: '删除pv'
        def entity = restTemplate.delete(MAPPING+"/{pv_id}", project_id,1L );

        then: '校验结果'
        devopsPvMapper.selectAll().size() == 0
    }



}
