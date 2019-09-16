package io.choerodon.devops.api.controller.v1

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.*
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsEnvCommitDTO
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO
import io.choerodon.devops.infra.dto.gitlab.CommitDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.*

/**
 *
 * @author zmf
 *
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(GitlabWebHookController)
@Stepwise
class GitlabWebHookControllerSpec extends Specification {
    private static final String BASE_URL = "/webhook"
    private static final String token = "b5eff928-e0cd-4279-abcf-7908e0922dc6"

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper
    @Autowired
    private DevopsBranchMapper devopsBranchMapper
    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvCommitMapper devopsEnvCommitMapper
    @Autowired
    private IamService iamRepository
    @Autowired
    private DevopsGitService devopsGitRepository
    @Autowired
    private AgentCommandService deployService
    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler mockEnvUtil
    @Autowired
    DevopsEnvCommitService devopsEnvCommitService
    @Autowired
    GitlabWebHookController gitlabWebHookController

    @Qualifier("mockBaseServiceClientOperator")
    @Autowired
    private BaseServiceClientOperator mockBaseServiceClientOperator

    @Qualifier("mockGitlabServiceClientOperator")
    @Autowired
    private GitlabServiceClientOperator mockGitlabServiceClientOperator

    @Shared
    private AppServiceDTO applicationDO = new AppServiceDTO()
    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private GitlabWebHookService gitlabWebHookService


    def setup() {
        if (isToInit) {
            DependencyInjectUtil.setAttribute(gitlabWebHookService,"devopsGitService",devopsGitService)
            // mock baseList user
            IamUserDTO userDO = new IamUserDTO()
            userDO.setId(1L)
            Mockito.when(mockBaseServiceClientOperator.queryByEmail(anyLong(), anyString())).thenReturn(userDO)

            // mock get commit
            CommitDTO commit = new CommitDTO()
            commit.setMessage("message")
            commit.setAuthorName("zzz")
            commit.setId("asdfsdfasdfsdfsdfadf")
            commit.setAuthorEmail("1332efas@163.mail.com")
            commit.setUrl("http://www.baidu.com")
            commit.setCommittedDate(new Date())

            Mockito.doReturn(commit).when(mockGitlabServiceClientOperator).queryCommit(anyInt(), anyString(), anyInt())

            // mock env util
            Mockito.when(mockEnvUtil.getConnectedEnvList()).thenReturn(Arrays.asList(1L))

            IamUserDTO iamUserDTO=new IamUserDTO()
            iamUserDTO.setLoginName("aaa")
            iamUserDTO.setOrganizationId(1L)
            Mockito.doReturn(iamUserDTO).when(mockBaseServiceClientOperator).queryUserByUserId(null)

        }
    }

    def cleanup() {
        if (isToClean) {
            DependencyInjectUtil.restoreDefaultDependency(gitlabWebHookService,"devopsGitService")

            applicationMapper.delete(applicationDO)
            devopsEnvironmentMapper.delete(devopsEnvironmentDO)
            devopsGitlabPipelineMapper.selectAll().forEach { devopsGitlabPipelineMapper.delete(it) }
            devopsGitlabCommitMapper.selectAll().forEach { devopsGitlabCommitMapper.delete(it) }
            devopsBranchMapper.selectAll().forEach { devopsBranchMapper.delete(it) }
            devopsMergeRequestMapper.selectAll().forEach { devopsMergeRequestMapper.delete(it) }
            devopsEnvCommitMapper.selectAll().forEach { devopsEnvCommitMapper.delete(it) }
        }
    }


    // webhook转发
    def "ForwardGitlabWebHook Pipeline"() {
        given: "准备PileLine数据"
        String body = "{\"object_kind\":\"pipeline\",\"object_attributes\":{\"id\":54192,\"ref\":\"master\",\"tag\":false,\"sha\":\"b77fceb9aab97e2a8dbacb8ef69686368fe8f7a1\",\"before_sha\":\"bae263786ff7009c1bfabe65baf5e575f68041cd\",\"status\":\"failed\",\"detailed_status\":\"failed\",\"stages\":[\"mvn-package\",\"docker-build\",\"external\"],\"created_at\":\"2018-12-25 10:55:26 UTC\",\"finished_at\":\"2018-12-25 11:04:56 UTC\",\"duration\":317},\"user\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"},\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null},\"commit\":{\"id\":\"b77fceb9aab97e2a8dbacb8ef69686368fe8f7a1\",\"message\":\"Merge branch 'feature-C7NCD-1756' into 'master'\\n\\n[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\\n\\nSee merge request choerodon-c7ncd/devops-service!302\",\"timestamp\":\"2018-12-25T02:55:23Z\",\"url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service/commit/b77fceb9aab97e2a8dbacb8ef69686368fe8f7a1\",\"author\":{\"name\":\"zmf\",\"email\":\"mofang.zheng@test.com\"}},\"builds\":[{\"id\":101344,\"stage\":\"docker-build\",\"name\":\"docker-build\",\"status\":\"success\",\"created_at\":\"2018-12-25 10:55:26 UTC\",\"started_at\":\"2018-12-25 11:04:35 UTC\",\"finished_at\":\"2018-12-25 11:04:56 UTC\",\"when\":\"on_success\",\"manual\":false,\"user\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"},\"runner\":{\"id\":1,\"description\":\"choerodon-runner\",\"active\":true,\"is_shared\":true},\"artifacts_file\":{\"filename\":null,\"size\":0}},{\"id\":101343,\"stage\":\"mvn-package\",\"name\":\"maven-test-build\",\"status\":\"success\",\"created_at\":\"2018-12-25 10:55:26 UTC\",\"started_at\":\"2018-12-25 10:59:30 UTC\",\"finished_at\":\"2018-12-25 11:04:26 UTC\",\"when\":\"on_success\",\"manual\":false,\"user\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"},\"runner\":{\"id\":1,\"description\":\"choerodon-runner\",\"active\":true,\"is_shared\":true},\"artifacts_file\":{\"filename\":null,\"size\":1671812}}]}"

        applicationDO.setToken(token)
        applicationDO.setProjectId(1L)
        applicationDO.setGitlabProjectId(1)
        applicationMapper.insert(applicationDO)

        devopsEnvironmentDO.setToken(token)
        devopsEnvironmentDO.setName("env-test")
        devopsEnvironmentDO.setCode("env-test-webhook")
        devopsEnvironmentMapper.insert(devopsEnvironmentDO)

        def requestEntity = createEntity(body)

        when: "调用方法"
        def entity = restTemplate.postForEntity(BASE_URL, requestEntity, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
    }

    def "forward gitops webhook"() {
        given: "准备数据"
        String body = "{\"object_kind\":\"push\",\"event_name\":\"push\",\"before\":\"0000000000000000000000000000000000000000\",\"after\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"ref\":\"refs/heads/feature-C7NCD-1756\",\"checkout_sha\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":null,\"user_id\":10256,\"user_name\":\"郑膜坊\",\"user_username\":\"20610\",\"user_email\":\"mofang.zheng@test.com\",\"user_avatar\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\",\"project_id\":237,\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"commits\":[{\"id\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":\"[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\\n\",\"timestamp\":\"2018-12-25T10:33:28+08:00\",\"url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service/commit/c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"author\":{\"name\":\"zmf\",\"email\":\"1984654893@qq.com\"},\"added\":[],\"modified\":[\"src/main/java/io/choerodon/devops/app/service/impl/DevopsEnvPodServiceImpl.java\",\"src/test/groovy/io/choerodon/devops/api/controller/v1/DevopsEnvPodControllerSpec.groovy\"],\"removed\":[]}],\"total_commits_count\":1,\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"visibility_level\":0}  }"
        def url = BASE_URL + "/git_ops"

        def request = createEntity(body)
        def validation = new DevopsEnvCommitDTO()
        validation.setCommitSha("c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b")

        when: "调用方法"
        def entity = restTemplate.postForEntity(url, request, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        devopsEnvCommitMapper.selectOne(validation) != null
    }



    // 查询自动化测试应用实例状态
    def "get test status"() {
        given: "准备数据"
        def url = BASE_URL + "/get_test_status"
        Map<Long, List<String>> request = new HashMap<>()
        request.put(1L, Arrays.asList("name", "test"))

        when: "调用方法"
        def entity = restTemplate.postForEntity(url, request, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
    }

    // 清理现场
    def "clean stage"() {
        given: "设置标志位"
        isToClean = true
    }

    private static HttpEntity<Object> createEntity(Object body) {
        HttpHeaders headers = new HttpHeaders()
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json")
        headers.add("X-Gitlab-Token", token)

        return new HttpEntity<Object>(body, headers)
    }
}
