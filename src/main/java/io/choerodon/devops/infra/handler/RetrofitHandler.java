package io.choerodon.devops.infra.handler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.feign.SonarClient;

public class RetrofitHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger(RetrofitHandler.class);

    private RetrofitHandler() {
    }

    /**
     * Retrofit 设置
     *
     * @param configurationProperties 插件配置信息
     * @return retrofit
     */
    public static Retrofit initRetrofit(ConfigurationProperties configurationProperties) {

        String credentials = configurationProperties.getUsername() + ":"
                + configurationProperties.getPassword();
        String token = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        // 跳过tls校验
        OkHttpClient okHttpClient = getOkHttpClient(true, configurationProperties.getType(), token);

        return new Retrofit.Builder()
                .baseUrl(configurationProperties.getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit initRetrofit(ConfigurationProperties configurationProperties, Converter.Factory factory) {
        String credentials = configurationProperties.getUsername() + ":"
                + configurationProperties.getPassword();
        String token = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        // 默认跳过证书校验
        OkHttpClient okHttpClient = getOkHttpClient(true, configurationProperties.getType(), token);

        return new Retrofit.Builder()
                .baseUrl(configurationProperties.getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(Objects.requireNonNull(factory))
                .build();
    }


    private static OkHttpClient getOkHttpClient(Boolean insecureSkipTlsVerify, String type, String token) {
        if (!type.equals("chart")) {
            if (insecureSkipTlsVerify) {
                X509TrustManager x509TrustManager = new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        //此处是不检验安全证书，避免有些harbor仓库没使用https安全证书导致harbor api失败，如果不抛出异常会出现sonar的问题
                        if (type == null) {
                            try {
                                throw new CertificateException("the type is null");
                            } catch (CertificateException e) {
                                throw new CommonException(e);
                            }
                        }
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        //此处是不检验安全证书，避免有些harbor仓库没使用https安全证书导致harbor api失败，如果不抛出异常会出现sonar的问题
                        if (type == null) {
                            try {
                                throw new CertificateException("the type is null");
                            } catch (CertificateException e) {
                                throw new CommonException(e);
                            }
                        }
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                };
                final TrustManager[] trustAllCerts = new TrustManager[]{x509TrustManager};

                // Install the all-trusting trust manager
                SSLContext sslContext = null;
                try {
                    sslContext = SSLContext.getInstance("TLSv1.2");
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new CommonException(e);
                }

                // Create an ssl socket factory with our all-trusting manager
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
                okHttpClientBuilder.interceptors().add((Interceptor.Chain chain) -> {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", token);

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                });
                okHttpClientBuilder.sslSocketFactory(sslSocketFactory, x509TrustManager);
                okHttpClientBuilder.hostnameVerifier((requestedHost, remoteServerSession) ->
                        requestedHost.equalsIgnoreCase(remoteServerSession.getPeerHost()) // Compliant
                );
                okHttpClientBuilder.followRedirects(true);
                return okHttpClientBuilder.build();
            } else {
                return RetrofitHandler.buildWithToken(token);
            }
        } else {
            return RetrofitHandler.buildWithToken(token);
        }
    }

    /**
     * basic的token来创建client
     *
     * @param token basic认证的token
     * @return client
     */
    public static OkHttpClient buildWithToken(String token) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.interceptors().add((Interceptor.Chain chain) -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", token);
            Request request = requestBuilder.build();
            return chain.proceed(request);
        });
        okHttpClientBuilder.followRedirects(true);
        return okHttpClientBuilder.build();
    }

    public static SonarClient getSonarClient(String sonarqubeUrl, String sonar, String userName, String password) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(sonarqubeUrl);
        configurationProperties.setType(sonar);
        configurationProperties.setUsername(userName);
        configurationProperties.setPassword(password);
        configurationProperties.setInsecureSkipTlsVerify(true);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        return retrofit.create(SonarClient.class);
    }

    public static class StringConverter extends Converter.Factory {
        @Nullable
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return new StringResponseBodyConverter();
        }

        @Nullable
        @Override
        public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
            return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
        }

        @Nullable
        @Override
        public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return super.stringConverter(type, annotations, retrofit);
        }
    }

    public static final class StringResponseBodyConverter implements Converter<ResponseBody, String> {
        StringResponseBodyConverter() {
        }

        @Override
        public String convert(ResponseBody value) throws IOException {
            return value.string();
        }
    }

}
