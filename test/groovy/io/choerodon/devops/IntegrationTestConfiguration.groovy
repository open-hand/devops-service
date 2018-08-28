package io.choerodon.devops

import com.fasterxml.jackson.databind.ObjectMapper
import io.choerodon.core.oauth.CustomUserDetails
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.service.DeployService
import io.choerodon.devops.infra.common.util.EnvUtil
import io.choerodon.devops.infra.common.util.GitUtil
import io.choerodon.liquibase.LiquibaseConfig
import io.choerodon.liquibase.LiquibaseExecutor
import io.choerodon.websocket.helper.EnvListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.security.jwt.JwtHelper
import org.springframework.security.jwt.crypto.sign.MacSigner
import org.springframework.security.jwt.crypto.sign.Signer
import org.springframework.test.context.TestPropertySource
import spock.mock.DetachedMockFactory

import javax.annotation.PostConstruct

/**
 * Created by hailuoliu@choerodon.io on 2018/7/13.
 */
@TestConfiguration
@Import(LiquibaseConfig)
@TestPropertySource("classpath:application-test.yml")
class IntegrationTestConfiguration {

    private final detachedMockFactory = new DetachedMockFactory()

    @Value('${choerodon.oauth.jwt.key:choerodon}')
    String key

    @Autowired
    TestRestTemplate testRestTemplate

    @Autowired
    LiquibaseExecutor liquibaseExecutor

    final ObjectMapper objectMapper = new ObjectMapper()

    @Bean("mockGitlabRepository")
    @Primary
    GitlabRepository gitlabRepository() {
        detachedMockFactory.Mock(GitlabRepository)
    }

    @Bean("mockUserAttrRepository")
    @Primary
    UserAttrRepository userAttrRepository() {
        detachedMockFactory.Mock(UserAttrRepository);
    }

    @Bean("mockGitlabGroupMemberRepository")
    @Primary
    GitlabGroupMemberRepository gitlabGroupMemberRepository() {
        detachedMockFactory.Mock(GitlabGroupMemberRepository);
    }

    @Primary
    @Bean("mockEnvUtil")
    EnvUtil envUtil() {
        detachedMockFactory.Mock(EnvUtil);
    }

    @Primary
    @Bean("mockEnvListener")
    EnvListener envListener() {
        detachedMockFactory.Mock(EnvListener);
    }

    @Bean("mockIamRepository")
    @Primary
    IamRepository iamRepository() {
        detachedMockFactory.Mock(IamRepository)
    }


    @Bean("mockGitUtil")
    @Primary
    GitUtil gitUtil() {
        detachedMockFactory.Mock(GitUtil)
    }


    @Bean("mockDevopsGitRepository")
    @Primary
    DevopsGitRepository devopsGitRepository() {
        detachedMockFactory.Mock(DevopsGitRepository)
    }


    @Bean("mockDeployService")
    @Primary
    DeployService deployService() {
        detachedMockFactory.Mock(DeployService)
    }


    @Bean("mockApplicationTemplateRepository")
    @Primary
    ApplicationTemplateRepository applicationTemplateRepository() {
        detachedMockFactory.Mock(ApplicationTemplateRepository)
    }


    @PostConstruct
    void init() {
        liquibaseExecutor.execute(new String()[])
        setTestRestTemplateJWT()
    }

    private void setTestRestTemplateJWT() {
        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory())
        testRestTemplate.getRestTemplate().setInterceptors([new ClientHttpRequestInterceptor() {
            @Override
            ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
                httpRequest.getHeaders()
                        .add('JWT_Token', createJWT(key, objectMapper))
                return clientHttpRequestExecution.execute(httpRequest, bytes)
            }
        }])
    }

    static String createJWT(final String key, final ObjectMapper objectMapper) {
        Signer signer = new MacSigner(key)
        CustomUserDetails defaultUserDetails = new CustomUserDetails('default', 'unknown', Collections.emptyList())
        defaultUserDetails.setUserId(1L)
        defaultUserDetails.setOrganizationId(0L)
        defaultUserDetails.setLanguage('zh_CN')
        defaultUserDetails.setTimeZone('CCT')
        String jwtToken = null
        try {
            jwtToken = 'Bearer ' + JwtHelper.encode(objectMapper.writeValueAsString(defaultUserDetails), signer).getEncoded()
        } catch (IOException e) {
            e.printStackTrace()
        }
        return jwtToken
    }
}

