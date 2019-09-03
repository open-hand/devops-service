package io.choerodon.devops.api.controller.v1

import static org.mockito.ArgumentMatchers.*
import static org.powermock.api.mockito.PowerMockito.when

import com.github.pagehelper.PageInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsCustomizeResourceVO
import io.choerodon.devops.app.service.DevopsCustomizeResourceService
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.UserAttrDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.mapper.*

/**
 *
 * @author zmf
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsCustomizeResourceController)
@Stepwise
class DevopsCustomizeResourceControllerSpec extends Specification {
    def rootUrl = "/v1/projects/{project_id}/customize_resource"
    @Shared
    private Long projectId = 1L
    @Shared
    private Long envId = 1L
    @Shared
    private Long userId = 1L

    @Autowired
    private TestRestTemplate restTemplate


    @Autowired
    private DevopsCustomizeResourceService devopsCustomizeResourceService

    @Autowired
    private DevopsCustomizeResourceMapper devopsCustomizeResourceMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsDeployRecordMapper deployRecordMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper
    @Autowired
    private UserAttrMapper userAttrMapper

    @Qualifier("mockBaseServiceClientOperator")
    @Autowired
    private BaseServiceClientOperator mockBaseServiceClientOperator

    @Qualifier("mockGitlabServiceClientOperator")
    @Autowired
    private GitlabServiceClientOperator mockGitlabServiceClientOperator

    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDTO
    @Shared
    private UserAttrDTO userAttrDTO

    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false

    void setup() {
        if (!isToInit) {
            return
        }
        mock()

        devopsEnvironmentDTO = new DevopsEnvironmentDTO()
        devopsEnvironmentDTO.setId(envId)
        devopsEnvironmentDTO.setName("env-name1")
        devopsEnvironmentDTO.setCode("env-code1")
        devopsEnvironmentDTO.setGitlabEnvProjectId(100L)
        devopsEnvironmentMapper.insert(devopsEnvironmentDTO)

        userAttrDTO = new UserAttrDTO()
        userAttrDTO.setIamUserId(2L)
        userAttrDTO.setGitlabUserId(2L)
        userAttrMapper.insert(userAttrDTO)
    }

    void cleanup() {
        if (!isToClean) {
            return
        }

        devopsEnvironmentMapper.delete(null)
        userAttrMapper.deleteByPrimaryKey(userAttrDTO.getIamUserId())
    }

    void mock() {
        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setId(1L)
        projectDO.setCode("pro")
        projectDO.setOrganizationId(1L)
        when(mockBaseServiceClientOperator.queryIamProjectById(anyLong())).thenReturn(projectDO)

        when(mockBaseServiceClientOperator.isProjectOwner(anyLong(), any(ProjectDTO))).thenReturn(true)

        IamUserDTO user = new IamUserDTO()
        user.setLoginName("loginName")
        user.setRealName("real-name")
        when(mockBaseServiceClientOperator.queryUserByUserId(anyLong())).thenReturn(user)

        when(mockGitlabServiceClientOperator.getFile(anyInt(), anyString(), anyString())).thenReturn(false)
    }

    def "CreateResource"() {
        given: "准备数据"
        isToInit = false
        def url = rootUrl
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<String, Object>()
        requestBody.add("envId", envId)
        requestBody.add("type", "create")
        requestBody.add("content", "apiVersion: v1\n" +
                "kind: PersistentVolume\n" +
                "metadata:\n" +
                "  name: pv0006\n" +
                "  labels:\n" +
                "    choerodon.io/resource: custom\n" +
                "spec:\n" +
                "  capacity:\n" +
                "    storage: 5Gi")

        when: "调用方法：直接调方法因为这里的请求格式是rest不能模拟form表单提交"
        restTemplate.postForObject(url, requestBody, Object, params)

        then: "校验结果"
        devopsCustomizeResourceMapper.selectAll().size() == 1
    }

    def "GetResource"() {
        given: "准备数据"
        def url = rootUrl + "/{resource_id}"
        Map<String, Object> params = new HashMap<>()
        def resouceId = devopsCustomizeResourceMapper.selectOne(null).getId()
        params.put("project_id", projectId)
        params.put("resource_id", resouceId)

        when: "调用方法"
        def resp = restTemplate.getForObject(url, DevopsCustomizeResourceVO, params)

        then: "校验结果"
        resp != null
        resp.getId() == resouceId
        resp.getName() == "pv0006"
    }

    def "PageByEnv"() {
        given: "准备数据"
        def url = rootUrl + "/{env_id}/page_by_env?page={page}&size={size}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("env_id", envId)
        params.put("page", 1)
        params.put("size", 10)

        when: "调用方法"
        def resp = restTemplate.postForObject(url, null, PageInfo, params)

        then: "校验结果"
        resp != null
        resp.getTotal() == 1
    }

    def "DeleteResource"() {
        given: "准备数据"
        isToClean = true
        def url = rootUrl + "?resource_id={resource_id}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("resource_id", devopsCustomizeResourceMapper.selectOne(null).getId())

        when: "调用方法"
        restTemplate.delete(url, params)

        then: "校验结果"
        devopsCustomizeResourceMapper.selectAll().size() == 0
    }
}
