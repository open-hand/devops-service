package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.infra.dto.DevopsProjectDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.DevopsProjectMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author zhaotianxin* @since 2019/10/8
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsProjectController)
@Stepwise
class DevopsProjectControllerSpec extends Specification {
    private static final String MAPPING = "/v1/projects/{project_id}"
    @Autowired
    private TestRestTemplate restTemplate
    @Qualifier("mockBaseServiceClientOperator")
    @Autowired
    private BaseServiceClientOperator mockBaseServiceClientOperator
    @Autowired
    private DevopsProjectMapper devopsProjectMapper
    @Shared
    private DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO()

    def setup() {
        ProjectDTO projectDTO = new ProjectDTO()
        projectDTO.setId(1L)
        projectDTO.setOrganizationId(1L)
        Mockito.doReturn(projectDTO).when(mockBaseServiceClientOperator).queryIamProjectById(1L)
        PageInfo<ProjectDTO> pageInfo = new PageInfo<>()
        List<ProjectDTO> projectDTOList = new ArrayList<>()

        projectDTOList.add(projectDTO)
        pageInfo.setList(projectDTOList)
        Mockito.doReturn(pageInfo).when(mockBaseServiceClientOperator).pageProjectByOrgId(1L, 1, 20, null, null, null)
    }

    def "QueryProjectGroupReady"() {
        given: "初始化参数"
        devopsProjectDTO.setIamProjectId(1L)
        devopsProjectDTO.setDevopsAppGroupId(1L)
        devopsProjectDTO.setDevopsEnvGroupId(1L)
        devopsProjectDTO.setHarborProjectUserEmail("123@qq.com")
        devopsProjectDTO.setHarborProjectUserName("大佬")
        devopsProjectDTO.setHarborProjectUserPassword("niuniu")
        devopsProjectDTO.setHarborProjectIsPrivate(true)
        devopsProjectMapper.insertSelective(devopsProjectDTO)
        when:
        def entity = restTemplate.getForEntity(MAPPING + "/check_gitlab_group", Object.class, 1L)
        then:
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
    }

    def "PageProjects"() {
        given: "初始化参数"
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        String params = "{\"searchParam\":{},\"params\":[]}"
        HttpEntity<String> httpEntity = new HttpEntity(params, headers)
        when: "执行测试"
        def entity = restTemplate.postForEntity(MAPPING + "/page_projects", httpEntity, PageInfo.class, 1L)
        then: "结果校验"
        entity.getStatusCode().is2xxSuccessful()

    }

    def "GetAllUsers"() {

        when: "执行测试"
        def entity = restTemplate.getForEntity(MAPPING + " /users/list_users", List.class, 1L)
        then: "结果校验"
        entity.getStatusCode().is2xxSuccessful()
    }
}
