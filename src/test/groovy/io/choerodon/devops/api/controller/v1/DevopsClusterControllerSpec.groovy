package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsClusterRepDTO
import io.choerodon.devops.api.dto.DevopsClusterReqDTO
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.DevopsClusterMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.Matchers.anyLong
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/11/13
 * Time: 14:03
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsClusterController)
@Stepwise
class DevopsClusterControllerSpec extends Specification {

    private static final String mapping = "/v1/organizations/{organization_id}/clusters"

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private IamRepository iamRepository
    @Autowired
    private DevopsClusterMapper devopsClusterMapper
//    @Autowired
//    private GitlabRepository gitlabRepository
//    @Autowired
//    private GitlabGroupMemberRepository gitlabGroupMemberRepository

    //  SagaClient sagaClient = Mockito.mock(SagaClient.class)
    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)
    //  GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    def setup() {
        iamRepository.initMockIamService(iamServiceClient)
//        gitlabRepository.initMockService(gitlabServiceClient)
//        gitlabGroupMemberRepository.initMockService(gitlabServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("proName")
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(anyLong())
        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(anyLong())
    }

    def "Create"() {
        given: '初始化DTO'
        DevopsClusterReqDTO devopsClusterReqDTO = new DevopsClusterReqDTO()
        List<Long> projectIds = new ArrayList<>()
        projectIds.add(1L)
        devopsClusterReqDTO.setCode("cluster")
        devopsClusterReqDTO.setProjects(projectIds)
        devopsClusterReqDTO.setSkipCheckProjectPermission(false)

        when: '组织下创建集群'
        def str = restTemplate.postForObject(mapping, devopsClusterReqDTO, String.class, 1L)

        then: '校验返回值'
        str != null
    }

    def "Update"() {
        given: '初始化DTO'
        DevopsClusterReqDTO devopsClusterReqDTO = new DevopsClusterReqDTO()
        List<Long> projectIds = new ArrayList<>()
        projectIds.add(2L)
        devopsClusterReqDTO.setCode("cluster")
        devopsClusterReqDTO.setProjects(projectIds)
        devopsClusterReqDTO.setName("updateCluster")
        devopsClusterReqDTO.setSkipCheckProjectPermission(false)

        when: '更新集群下的项目'
        restTemplate.put(mapping + "?clusterId=1", devopsClusterReqDTO, 2L)

        then: '校验是否更新'
        devopsClusterMapper.selectAll().get(0)["name"] == "updateCluster"
    }

    def "Query"() {
        when: '查询单个集群信息'
        def dto = restTemplate.getForObject(mapping + "/1", DevopsClusterRepDTO.class, 1L)

        then: '校验返回值'
        dto["name"] == "updateCluster"
    }

    def "CheckName"() {
    }

    def "CheckCode"() {
    }

    def "PageProjects"() {
    }

    def "ListClusterProjects"() {
    }

    def "QueryShell"() {
    }

    def "ListCluster"() {
    }

    def "DeleteCluster"() {
    }
}
