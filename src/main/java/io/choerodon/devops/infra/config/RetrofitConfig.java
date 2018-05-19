package io.choerodon.devops.infra.config;

import java.util.Base64;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import io.choerodon.devops.infra.feign.HarborClient;

@Configuration
@EnableConfigurationProperties(HarborConfigurationProperties.class)
public class RetrofitConfig {

    /**
     * Retrofit 设置
     *
     * @param harborConfigurationProperties Harbor 信息
     * @return Harbor 平台连接
     */
    @Bean
    @ConditionalOnProperty(name = "services.harbor.enabled", matchIfMissing = true)
    public HarborClient harborService(HarborConfigurationProperties harborConfigurationProperties) {
        String credentials = harborConfigurationProperties.getUsername() + ":"
                + harborConfigurationProperties.getPassword();
        String token = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.interceptors().add((Interceptor.Chain chain) -> {
            Request original = chain.request();

            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", token);

            Request request = requestBuilder.build();
            return chain.proceed(request);
        });
        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(harborConfigurationProperties.getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        return retrofit.create(HarborClient.class);
    }
}
