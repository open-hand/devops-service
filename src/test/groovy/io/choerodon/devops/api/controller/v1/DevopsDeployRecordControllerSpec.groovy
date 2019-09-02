package io.choerodon.devops.api.controller.v1

import static org.mockito.ArgumentMatchers.any

import com.github.pagehelper.PageInfo
import org.powermock.api.mockito.PowerMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsDeployRecordVO
import io.choerodon.devops.app.service.DevopsDeployRecordService
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.DevopsDeployRecordMapper
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper

/**
 *
 * @author zmf
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Subject(DevopsDeployRecordController)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsDeployRecordControllerSpec extends Specification {
    def rootUrl = "/v1/projects/{project_id}/deploy_record"
    @Shared
    private Long projectId = 1L
    @Shared
    private Long envId = 1L
    @Shared
    private Long userId = 1L

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private DevopsDeployRecordService deployRecordService

    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsDeployRecordMapper deployRecordMapper
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper

    private BaseServiceClientOperator mockBaseServiceClientOperator = PowerMockito.mock(BaseServiceClientOperator)

    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDTO
    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDTO2
    @Shared
    private DevopsEnvCommandDTO devopsEnvCommandDTO
    @Shared
    private DevopsDeployRecordDTO deployRecordDTO

    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false

    void setup() {
        if (!isToInit) {
            return
        }
        DependencyInjectUtil.setAttribute(deployRecordService, "baseServiceClientOperator", mockBaseServiceClientOperator)

        devopsEnvironmentDTO = new DevopsEnvironmentDTO()
        devopsEnvironmentDTO.setId(envId)
        devopsEnvironmentDTO.setName("env-name1")
        devopsEnvironmentDTO.setCode("env-code1")
        devopsEnvironmentMapper.insert(devopsEnvironmentDTO)

        devopsEnvironmentDTO2 = new DevopsEnvironmentDTO()
        devopsEnvironmentDTO2.setId(envId + 1)
        devopsEnvironmentDTO2.setName("env-name2")
        devopsEnvironmentDTO2.setCode("env-code2")
        devopsEnvironmentMapper.insert(devopsEnvironmentDTO2)

        devopsEnvCommandDTO = new DevopsEnvCommandDTO()
        devopsEnvCommandDTO.setId(1L)
        devopsEnvCommandDTO.setStatus("FINISHED")
        devopsEnvCommandDTO.setCreatedBy(userId)
        devopsEnvCommandMapper.insert(devopsEnvCommandDTO)

        deployRecordDTO = new DevopsDeployRecordDTO()
        deployRecordDTO.setId(1L)
        deployRecordDTO.setProjectId(projectId)
        deployRecordDTO.setEnv(devopsEnvironmentDTO.getId() + "," + devopsEnvironmentDTO2.getId())
        deployRecordDTO.setDeployType("manual")
        deployRecordMapper.insert(deployRecordDTO)
    }

    void cleanup() {
        if (!isToClean) {
            return
        }
        DependencyInjectUtil.restoreDefaultDependency(deployRecordService, "baseServiceClientOperator")

        devopsEnvironmentMapper.delete(null)
        devopsEnvCommandMapper.delete(null)
        deployRecordMapper.delete(null)
    }

    void mock() {
        IamUserDTO responseUser = new IamUserDTO()
        responseUser.setRealName("user1")
        responseUser.setLoginName("user1")
        PowerMockito.when(mockBaseServiceClientOperator.listUsersByIds(any(List))).thenReturn(Collections.singletonList(responseUser))
    }

    def "PageByOptions"() {
        given: "准备数据"
        isToInit = false
        isToClean = true
        def url = rootUrl + "/page_by_options?page={page}&size={size}"
        Map<String, Object> params = new HashMap<>()
        params.put("project_id", projectId)
        params.put("page", 1)
        params.put("size", 1)

        when: "调用方法"
        def resp = restTemplate.postForObject(url, null, PageInfo, params)

        then: "校验结果"
        resp != null
        resp.getSize() == 1
        (resp.getList().get(0) as DevopsDeployRecordVO).getEnv() == devopsEnvironmentDTO.getName() + "," + devopsEnvironmentDTO2.getName()
    }
}
