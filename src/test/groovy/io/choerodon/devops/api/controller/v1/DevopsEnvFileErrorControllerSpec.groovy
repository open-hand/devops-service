package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ProjectVO
import io.choerodon.devops.api.vo.iam.entity.UserAttrE
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.OrganizationVO
import io.choerodon.devops.infra.dataobject.DevopsEnvFileErrorDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/7
 * Time: 14:54
 * Description: 
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsEnvFileErrorController)
@Stepwise
class DevopsEnvFileErrorControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvFileErrorMapper devopsEnvFileErrorMapper

    @Autowired
    private IamRepository iamRepository

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    @Shared
    Long init_id = 1L
    @Shared
    Long project_id = 1L
    @Shared
    ProjectVO projectE = new ProjectVO()
    @Shared
    UserAttrE userAttrE = new UserAttrE()
    @Shared
    OrganizationVO organization = new OrganizationVO()

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
        ProjectDO projectDO = new ProjectDO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)

        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("org")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)
    }

    def "List"() {
        given: '插入数据'
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO()
        devopsEnvFileErrorDO.setId(1L)
        devopsEnvFileErrorDO.setEnvId(1L)
        devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDO)
        DevopsEnvFileErrorDO devopsEnvFileErrorDO1 = new DevopsEnvFileErrorDO()
        devopsEnvFileErrorDO.setId(2L)
        devopsEnvFileErrorDO1.setEnvId(1L)
        devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDO1)

        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setCode("env")
        devopsEnvironmentDO.setProjectId(1)
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)

        when: '项目下查询环境文件错误列表'
        def list = restTemplate.getForObject("/v1/projects/1/envs/1/error_file/list", List.class)

        then: '校验返回结果'
        list.size() == 2
    }

    def "Page"() {
        when: '项目下查询环境文件错误列表'
        def page = restTemplate.getForObject("/v1/projects/1/envs/1/error_file/list_by_page", Page.class)

        then: '校验返回结果'
        page.size() == 2

        // 删除env
        List<DevopsEnvironmentDO> list = devopsEnvironmentMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (DevopsEnvironmentDO e : list) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除envFileError
        List<DevopsEnvFileErrorDO> list1 = devopsEnvFileErrorMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvFileErrorDO e : list1) {
                devopsEnvFileErrorMapper.delete(e)
            }
        }
    }
}
