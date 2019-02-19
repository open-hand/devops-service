package io.choerodon.devops.infra.config;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import io.choerodon.devops.app.service.impl.ApplicationServiceImpl;
import io.choerodon.devops.infra.feign.HarborClient;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@EnableConfigurationProperties(HarborConfigurationProperties.class)
public class RetrofitConfig {

    public static final Logger LOGGER = LoggerFactory.getLogger(RetrofitConfig.class);

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

        OkHttpClient okHttpClient = getOkHttpClient(harborConfigurationProperties.getInsecureSkipTlsVerify(), token);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(harborConfigurationProperties.getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        return retrofit.create(HarborClient.class);
    }

    private OkHttpClient getOkHttpClient(Boolean insecureSkipTlsVerify, String token) {
        if (insecureSkipTlsVerify) {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error(e.getMessage());
            } catch (KeyManagementException e) {
                LOGGER.error(e.getMessage());
            }

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.interceptors().add((Interceptor.Chain chain) -> {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", token);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
            okHttpClientBuilder.sslSocketFactory(sslSocketFactory);
            okHttpClientBuilder.hostnameVerifier((hostname, session) -> true);
            return okHttpClientBuilder.build();
        } else {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.interceptors().add((Interceptor.Chain chain) -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", token);
                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
            return okHttpClientBuilder.build();
        }
    }
}
