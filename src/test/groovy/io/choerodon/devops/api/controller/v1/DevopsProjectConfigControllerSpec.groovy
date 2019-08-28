package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.ConfigVO
import io.choerodon.devops.api.vo.DefaultConfigVO
import io.choerodon.devops.api.vo.DevopsConfigVO
import io.choerodon.devops.app.service.DevopsConfigService
import io.choerodon.devops.infra.dto.DevopsConfigDTO
import io.choerodon.devops.infra.dto.DevopsProjectDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.HarborClient
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.DevopsProjectMapper
import io.choerodon.devops.infra.util.ConvertUtils
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
 * Created by Sheep on 2019/4/9.
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsProjectConfigController)
@Stepwise
class DevopsProjectConfigControllerSpec extends Specification {
    HarborClient harborClient = Mockito.mock(HarborClient.class)

    private static final String MAPPING = "/v1/projects/{project_id}/project_config"

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsConfigService devopsProjectConfigRepository
    @Shared
    Long project_id = 1L

    @Shared
    List<DevopsConfigVO> list = new ArrayList<>()
    @Shared
    DevopsConfigVO chartDTO = new DevopsConfigVO()
    @Shared
    DevopsConfigVO harborDTO = new DevopsConfigVO()
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator
    @Autowired
    private DevopsProjectMapper devopsProjectMapper

    BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient)

    def setup() {
        DependencyInjectUtil.setAttribute(baseServiceClientOperator, "baseServiceClient", baseServiceClient)

        chartDTO.setName("test")
        chartDTO.setType("chart")
        chartDTO.setProjectId(project_id)
        chartDTO.setCustom(true)
        ConfigVO chartConfig = new ConfigVO()
        chartConfig.setUrl("http://ads.com")
        chartDTO.setConfig(chartConfig)


        harborDTO.setName("test1")
        harborDTO.setType("harbor")
        harborDTO.setProjectId(project_id)
        harborDTO.setCustom(true)
        ConfigVO harborConfig = new ConfigVO()
        harborConfig.setEmail("zhuang.chang@hand-china.com")
        harborConfig.setPassword("Handhand1357")
        harborConfig.setPrivate(true)
        harborConfig.setUrl("https://registry.saas.hand-china.com")
        harborConfig.setUserName("admin")
        harborDTO.setConfig(harborConfig)

        list.add(chartDTO)
        list.add(harborDTO)

        ProjectDTO projectDTO = new ProjectDTO()
        projectDTO.setId(1L)
        projectDTO.setCode("aads")
        projectDTO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> projectEntity = new ResponseEntity<>(projectDTO, HttpStatus.OK)
        Mockito.doReturn(projectEntity).when(baseServiceClient).queryIamProject(1L)

        OrganizationDTO organizationDTO = new OrganizationDTO()
        organizationDTO.setCode("organization")
        organizationDTO.setId(1L)
        ResponseEntity<OrganizationDTO> organizationEntity = new ResponseEntity<>(organizationDTO, HttpStatus.OK)
        Mockito.doReturn(organizationEntity).when(baseServiceClient).queryOrganizationById(1L)
    }
    //创建配置
    def "Create"() {
        given: '初始化数据'


        when: '创建配置'
        def entity = restTemplate.postForEntity(MAPPING, list, null, project_id)

        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "query"() {
        given:
        DevopsProjectDTO projectDTO = new DevopsProjectDTO()
        projectDTO.setAppId(1L)
        projectDTO.setDevopsAppGroupId(1L)
        projectDTO.setDevopsEnvGroupId(1L)
        projectDTO.setHarborProjectIsPrivate(true)
        projectDTO.setIamProjectId(1L)
        devopsProjectMapper.insert(projectDTO)

        when: '查询配置'
        def entity = restTemplate.getForEntity(MAPPING, List.class, project_id)
        then:
        entity.getStatusCode().is2xxSuccessful()
    }

    def "queryProjectDefaultConfig"() {
        when: '查询默认配置'
        def entity = restTemplate.getForEntity(MAPPING + "/default_config", DefaultConfigVO.class, project_id)
        then:
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
    }

}
