package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
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
    private IamService iamRepository

    BaseServiceClient iamServiceClient = PowerMockito.mock(BaseServiceClient.class)

    @Shared
    Long project_id = 1L

    def setup() {
        DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", iamServiceClient)
        ProjectDTO projectDTO = new ProjectDTO()
        projectDTO.setId(1L)
        projectDTO.setCode("pro")
        projectDTO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDTO, HttpStatus.OK)
        PowerMockito.when(iamServiceClient.queryIamProject(1L)).thenReturn(responseEntity)

        OrganizationDTO organizationDTO = new OrganizationDTO()
        organizationDTO.setId(1L)
        organizationDTO.setCode("org")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)
    }

    def "List"() {
        given: '插入数据'
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO()
        devopsEnvFileErrorDTO.setId(1L)
        devopsEnvFileErrorDTO.setEnvId(1L)
        devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDTO)
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO1 = new DevopsEnvFileErrorDTO()
        devopsEnvFileErrorDTO.setId(2L)
        devopsEnvFileErrorDTO1.setEnvId(1L)
        devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDTO1)

        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()
        devopsEnvironmentDTO.setId(1L)
        devopsEnvironmentDTO.setCode("env")
        devopsEnvironmentDTO.setProjectId(1)
        devopsEnvironmentDTO.setDevopsEnvGroupId(1L)
        devopsEnvironmentMapper.insert(devopsEnvironmentDTO)

        when: '项目下查询环境文件错误列表'
        def list = restTemplate.getForObject("/v1/projects/1/envs/1/error_file/baseList", List.class)

        then: '校验返回结果'
        list.size() == 2
    }

    def "Page"() {
        when: '项目下查询环境文件错误列表'
        def page = restTemplate.getForObject("/v1/projects/1/envs/1/error_file/list_by_page", PageInfo.class)

        then: '校验返回结果'
        page.getTotal() == 2

        // 删除env
        List<DevopsEnvironmentDTO> list = devopsEnvironmentMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (DevopsEnvironmentDTO e : list) {
                devopsEnvironmentMapper.delete(e)
            }
        }
        // 删除envFileError
        List<DevopsEnvFileErrorDTO> list1 = devopsEnvFileErrorMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsEnvFileErrorDTO e : list1) {
                devopsEnvFileErrorMapper.delete(e)
            }
        }
    }
}
