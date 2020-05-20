package io.choerodon.devops.api.controller.v1

import com.alibaba.fastjson.JSONArray
import com.github.pagehelper.PageInfo
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO
import io.choerodon.devops.app.service.DevopsEnvFileService
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
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

import static org.mockito.ArgumentMatchers.eq
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
    def rootUrl = "/v1/projects/{project_id}/envs/{env_id}/error_file"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvFileErrorMapper devopsEnvFileErrorMapper

    BaseServiceClientOperator mockBaseServiceClient = PowerMockito.mock(BaseServiceClientOperator.class)

    @Autowired
    private DevopsEnvFileService devopsEnvFileService
    @Shared
    private DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO()
    @Shared
    private DevopsEnvFileErrorDTO devopsEnvFileErrorDTO1 = new DevopsEnvFileErrorDTO()
    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO()

    @Shared
    private Long projectId = 1L
    @Shared
    private Long envId = 1L
    @Shared
    boolean isToInit = true
    @Shared
    boolean isToClean = false

    def setup() {
        if (isToInit) {
            DependencyInjectUtil.setAttribute(devopsEnvFileService, "baseServiceClientOperator", mockBaseServiceClient)

            devopsEnvFileErrorDTO.setId(1L)
            devopsEnvFileErrorDTO.setEnvId(1L)
            devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDTO)

            devopsEnvFileErrorDTO.setId(2L)
            devopsEnvFileErrorDTO1.setEnvId(1L)
            devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDTO1)

            devopsEnvironmentDTO.setId(envId)
            devopsEnvironmentDTO.setCode("env")
            devopsEnvironmentDTO.setProjectId(projectId)
            devopsEnvironmentDTO.setDevopsEnvGroupId(1L)
            devopsEnvironmentMapper.insert(devopsEnvironmentDTO)

            OrganizationDTO organizationDTO = new OrganizationDTO()
            organizationDTO.setId(1L)
            organizationDTO.setCode("org")
            PowerMockito.when(mockBaseServiceClient.queryOrganizationById(eq(organizationDTO.getId()))).thenReturn(organizationDTO)

            ProjectDTO projectDTO = new ProjectDTO()
            projectDTO.setId(projectId)
            projectDTO.setCode("pro")
            projectDTO.setOrganizationId(organizationDTO.getId())
            PowerMockito.when(mockBaseServiceClient.queryIamProjectById(eq(projectId))).thenReturn(projectDTO)
        }
    }

    def cleanup() {
        if (isToClean) {
            DependencyInjectUtil.restoreDefaultDependency(devopsEnvFileService, "baseServiceClientOperator")

            devopsEnvFileErrorMapper.delete(null)
            devopsEnvironmentMapper.delete(null)
        }
    }

    def "List"() {
        given: '准备'
        isToInit = false
        def url = rootUrl + "/list_by_env"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("env_id", envId)

        when: '项目下查询环境文件错误列表'
        def list = JSONArray.parseArray(restTemplate.getForObject(url, String.class, params), DevopsEnvFileErrorVO)

        then: '校验返回结果'
        list != null
        list.size() == 2
    }

    def "Page"() {
        given: '准备'
        isToClean = true
        def url = rootUrl + "/page_by_env?page={page}&size={size}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("env_id", envId)
        params.put("page", 1)
        params.put("size", 10)

        when: '项目下分页查询环境文件错误'
        def page = restTemplate.getForObject(url, PageInfo.class, params)

        then: '校验返回结果'
        page != null
        page.getTotal() == 2
        page.getList().size() == 2
    }
}
