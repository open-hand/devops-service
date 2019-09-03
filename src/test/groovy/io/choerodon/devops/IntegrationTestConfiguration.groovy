package io.choerodon.devops

import com.fasterxml.jackson.databind.ObjectMapper
import io.choerodon.asgard.saga.producer.TransactionalProducer
import io.choerodon.core.exception.CommonException
import io.choerodon.core.oauth.CustomUserDetails
import io.choerodon.devops.app.service.AgentPodService
import io.choerodon.devops.app.service.GitlabGroupMemberService
import io.choerodon.devops.app.service.ProjectConfigHarborService
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.util.GitUtil
import io.choerodon.liquibase.LiquibaseConfig
import io.choerodon.liquibase.LiquibaseExecutor
import org.powermock.api.mockito.PowerMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.jwt.JwtHelper
import org.springframework.security.jwt.crypto.sign.MacSigner
import org.springframework.security.jwt.crypto.sign.Signer
import org.springframework.test.context.TestPropertySource
import spock.mock.DetachedMockFactory

import javax.annotation.PostConstruct
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

/**
 * Created by hailuoliu@choerodon.io on 2018/7/13.
 */
@TestConfiguration
@Import(LiquibaseConfig)
@Order(2)
@TestPropertySource("classpath:application-test.yml")
class IntegrationTestConfiguration extends WebSecurityConfigurerAdapter {

    private final detachedMockFactory = new DetachedMockFactory()

    @Value('${choerodon.oauth.jwt.key:choerodon}')
    String key

    @Value('${spring.datasource.url}')
    String dataBaseUrl

    @Value('${spring.datasource.username}')
    String dataBaseUsername

    @Value('${spring.datasource.password}')
    String dataBasePassword

    @Value('${liquibase.init:true}')
    boolean isToExecuteLiquibase

    @Autowired
    TestRestTemplate testRestTemplate

    @Autowired
    LiquibaseExecutor liquibaseExecutor

    final ObjectMapper objectMapper = new ObjectMapper()

    @Primary
    @Bean("mockAgentPodInfoService")
    AgentPodService agentPodService() {
        PowerMockito.mock(AgentPodService)
    }

    @Primary
    @Bean("mockBaseServiceClientOperator")
    BaseServiceClientOperator baseServiceClientOperator() {
        PowerMockito.mock(BaseServiceClientOperator)
    }

    @Primary
    @Bean("mockGitlabServiceClientOperator")
    GitlabServiceClientOperator gitlabServiceClientOperator() {
        PowerMockito.mock(GitlabServiceClientOperator)
    }

    @Primary
    @Bean("mockGitlabGroupMemberService")
    GitlabGroupMemberService gitlabGroupMemberService() {
        PowerMockito.mock(GitlabGroupMemberService)
    }

    @Primary
    @Bean("mockClusterConnectionHandler")
    ClusterConnectionHandler clusterConnectionHandler() {
        PowerMockito.mock(ClusterConnectionHandler)
    }

    @Bean("mockGitUtil")
    @Primary
    GitUtil gitUtil() {
        detachedMockFactory.Mock(GitUtil)
    }

    @Bean("mockTransactionalProducer")
    @Primary
    TransactionalProducer transactionalProducer() {
        detachedMockFactory.Mock(TransactionalProducer)
    }

    @Bean("mockProjectConfigHarborService")
    @Primary
    ProjectConfigHarborService ProjectConfigHarborService() {
        detachedMockFactory.Mock(ProjectConfigHarborService)
    }

    @PostConstruct
    void init() {
        if (isToExecuteLiquibase) {
            liquibaseExecutor.execute()
        }
        initSqlFunction()
        setTestRestTemplateJWT()
    }

    void initSqlFunction() {
        if (dataBaseUrl.contains("jdbc:h2")) {
            //连接H2数据库
            Class.forName("org.h2.Driver")
//        Class.forName("com.mysql.jdbc.Driver")
            Connection conn = DriverManager.
                    getConnection(dataBaseUrl, dataBaseUsername, dataBasePassword)
            Statement stat = conn.createStatement()
            //创建 SQL的IF函数，用JAVA的方法代替函数
            stat.execute("CREATE ALIAS IF NOT EXISTS BINARY FOR \"io.choerodon.devops.infra.util.MybatisFunctionTestUtil.binaryFunction\"")
            stat.close()
            conn.close()
        }
    }

    private void setTestRestTemplateJWT() {
        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory())
        testRestTemplate.getRestTemplate().setInterceptors([new ClientHttpRequestInterceptor() {
            @Override
            ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
                httpRequest.getHeaders()
                        .add('Authorization', createJWT(key, objectMapper))
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
        try {
            return 'Bearer ' + JwtHelper.encode(objectMapper.writeValueAsString(defaultUserDetails), signer).getEncoded()
        } catch (IOException e) {
            throw new CommonException(e)
        }
    }

    /**
     * 解决访问h2-console跨域问题
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers("/h2-console/**")
                .and()
                .headers().frameOptions().disable()
        http.csrf().disable()
    }
}

