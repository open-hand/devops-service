package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsNotificationVO
import io.choerodon.devops.api.vo.ResourceCheckVO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.DevopsIngressDTO
import io.choerodon.devops.infra.dto.DevopsNotificationDTO
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import io.choerodon.devops.infra.mapper.DevopsIngressMapper
import io.choerodon.devops.infra.mapper.DevopsNotificationMapper
import io.choerodon.devops.infra.mapper.DevopsServiceMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author: 25499*
 * @date: 2019/8/28 16:48
 * @description:
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsIngressController)
@Stepwise
class DevopsNotificationControllerSpec extends Specification {
    private static final BASE_URL = "/v1/projects/{project_id}/notification"
    def projectId = 1L
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsIngressMapper devopsIngressMapper
    @Autowired
    private DevopsServiceMapper devopsServiceMapper
    @Autowired
    private DevopsNotificationMapper devopsNotificationMapper
    @Autowired
    DevopsEnvironmentMapper devopsEnvironmentMapper

    def cleanup() {

    }

    def setup() {
    }

    def "Create"() {

        given:
        DevopsNotificationVO devopsEnvAppServiceVO = new DevopsNotificationVO()
//        devopsEnvAppServiceVO.setId(1L)
        devopsEnvAppServiceVO.setEnvName("test")
        devopsEnvAppServiceVO.setEnvId(1L)
        devopsEnvAppServiceVO.setProjectId(1L)
        devopsEnvAppServiceVO.setNotifyObject("test")
        devopsEnvAppServiceVO.setNotifyTriggerEvent(Arrays.asList("test", "test2", "test3"))
        devopsEnvAppServiceVO.setNotifyType(Arrays.asList("test", "test2"))
        devopsEnvAppServiceVO.setObjectVersionNumber(1L)
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)

        when:
        def entity = restTemplate.postForEntity(BASE_URL, devopsEnvAppServiceVO, DevopsNotificationVO.class, 1L)
        then:
        devopsNotificationMapper.selectAll().size() == 1
    }

    def "Update"() {
        given:
        DevopsNotificationVO devopsEnvAppServiceVO = new DevopsNotificationVO()
        devopsEnvAppServiceVO.setId(1L)
        devopsEnvAppServiceVO.setEnvName("test")
        devopsEnvAppServiceVO.setEnvId(1L)
        devopsEnvAppServiceVO.setProjectId(1L)
        devopsEnvAppServiceVO.setNotifyObject("test")
        devopsEnvAppServiceVO.setNotifyTriggerEvent(Arrays.asList("hello", "hello2", "hello2"))
        devopsEnvAppServiceVO.setNotifyType(Arrays.asList("test", "test2"))
        devopsEnvAppServiceVO.setObjectVersionNumber(1L)
        Map<String, Object> urlParams = new HashMap<>()
        urlParams.put("project_id", projectId)
        when:
        def entity = restTemplate.postForEntity(BASE_URL, devopsEnvAppServiceVO, DevopsNotificationVO.class, 1L)
        then:
        entity.body.getObjectVersionNumber() == 2L
    }

    def "Delete"() {
        given:
        def url = BASE_URL + "/{notification_id}"
        when:
        restTemplate.delete(url, 1L, 1L)
        then:
        devopsNotificationMapper.selectAll().size() == 0
    }

    def "QueryById"() {
        given:
        def url = BASE_URL + "/{notification_id}"
        DevopsNotificationDTO devopsEnvAppServiceVO = new DevopsNotificationDTO()
        devopsEnvAppServiceVO.setId(1L)
        devopsEnvAppServiceVO.setEnvName("devNotify")
        devopsEnvAppServiceVO.setEnvId(1L)
        devopsEnvAppServiceVO.setProjectId(1L)
        devopsEnvAppServiceVO.setNotifyObject("test")
        devopsEnvAppServiceVO.setNotifyTriggerEvent("hello2")
        devopsEnvAppServiceVO.setNotifyType("test")
        devopsEnvAppServiceVO.setObjectVersionNumber(1L)
        devopsNotificationMapper.insertSelective(devopsEnvAppServiceVO)
        when:
        def entity = restTemplate.getForEntity(url, DevopsNotificationVO.class, 1L, 1L)
        then:
        entity.body.getId() == 1L
    }

    def "PageByOptions"() {
        given:
        DevopsNotificationDTO devopsEnvAppServiceVO = new DevopsNotificationDTO()
        devopsEnvAppServiceVO.setId(2L)
        devopsEnvAppServiceVO.setEnvName("devNotify")
        devopsEnvAppServiceVO.setEnvId(2L)
        devopsEnvAppServiceVO.setProjectId(2L)
        devopsEnvAppServiceVO.setNotifyObject("test")
        devopsEnvAppServiceVO.setNotifyTriggerEvent("hello2")
        devopsEnvAppServiceVO.setNotifyType("test")
        devopsEnvAppServiceVO.setObjectVersionNumber(2L)
        devopsNotificationMapper.insertSelective(devopsEnvAppServiceVO)
        def url = BASE_URL + "/page_by_options?page={page}&size={size}&env_id={env_id}"
        Map<String, Object> map = new HashMap<>()
        map.put("page", 1)
        map.put("size", 10)
        map.put("project_id", 1L)
        map.put("env_id", 2L)

        String params = "{\"searchParam\": {},\"params\": []}"
        when:
        def page = restTemplate.postForObject(url, params, PageInfo.class, map)
        then:
        page != null
        page.getList().size() == 0
    }

    def "Check"() {
        given:
        Map<String, Object> map = new HashMap<>()
        map.put("env_id", 1L)
        def url = BASE_URL + "/check?env_id={env_id}"
        when:
        def entity = restTemplate.getForEntity(url, Set.class, 1L, 1L)
        then:
        entity.body.size() > 0
    }

    def "CheckDeleteResource"() {
        given:
        DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()
        devopsEnvironmentDO.setId(1L)
        devopsEnvironmentDO.setActive(true)
        devopsEnvironmentDO.setClusterId(1L)
        devopsEnvironmentDO.setProjectId(1L)
        devopsEnvironmentDO.setFailed(false)
        devopsEnvironmentDO.setToken("testToken")
        devopsEnvironmentDO.setName("testNameEnv")
        devopsEnvironmentDO.setCode("testCodeEnv")
        devopsEnvironmentDO.setDevopsEnvGroupId(1L)
        devopsEnvironmentDO.setGitlabEnvProjectId(1L)
        devopsEnvironmentDO.setSkipCheckPermission(Boolean.FALSE)
        devopsEnvironmentDO.setAgentSyncCommit(1L)
        devopsEnvironmentDO.setDevopsSyncCommit(1L)
        devopsEnvironmentDO.setSagaSyncCommit(1L)
        devopsEnvironmentMapper.insertSelective(devopsEnvironmentDO)
        Map<String, Object> map = new HashMap<>()
        map.put("env_id", 1L)
        def url = BASE_URL + "/check_delete_resource?env_id={env_id}&object_type={object_type}"
        when:
        def entity = restTemplate.getForEntity(url, ResourceCheckVO.class, 1L, 1L, "email")
        then:
        entity.getStatusCode().'2xxSuccessful'
    }

    def "SendMessage"() {
        given:
        DevopsIngressDTO devopsIngressDO = new DevopsIngressDTO()
        devopsIngressDO.setId(1L)
        devopsIngressDO.setEnvId(1L)
        devopsIngressDO.setCertId(1L)
        devopsIngressDO.setUsable(true)
        devopsIngressDO.setName("ingdo")
        devopsIngressDO.setCommandId(1L)
        devopsIngressDO.setProjectId(1L)
        devopsIngressDO.setStatus("running")
        devopsIngressDO.setCommandType("create")
        devopsIngressDO.setObjectVersionNumber(1L)
        devopsIngressDO.setCommandStatus("success")
        devopsIngressDO.setDomain("test.test.com")
        devopsIngressMapper.insert(devopsIngressDO)
        Map<String, Object> map = new HashMap<>()
        map.put("env_id", 1L)
        map.put("object_id", 1L)
        map.put("project_id", 1L)
        map.put("notification_id", 1L)
        map.put("object_type", "ingress")
        def url = BASE_URL + "/send_message?env_id={env_id}&object_type={object_type}&notification_id={notification_id}&object_id={object_id}"
        when:
        def entity = restTemplate.getForEntity(url, null, map)
        then:
        entity.getStatusCode().'2xxSuccessful'
    }

    def "ValidateCaptcha"() {
        given:
        Map<String, Object> map = new HashMap<>()
        map.put("env_id", 1L)
        map.put("object_type", "ingress")
        map.put("captcha", "ingress")
        map.put("object_id", 1L)
        map.put("project_id", 1L)
        def url = BASE_URL + "/validate_captcha?env_id={env_id}&object_type={object_type}&object_id={object_id}&captcha={captcha}"
        when:
        def entity = restTemplate.getForEntity(url, null, map)
        then:
        entity.getStatusCode().'2xxSuccessful'
    }
}
