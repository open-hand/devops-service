package com.cdancy.jenkins.rest.domain.common;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/7 16:44
 */
public class AutoValue_Response extends Response {
    private String value;

    private int statusCode;
    private String error;

    public AutoValue_Response(String value, int statusCode, String error) {
        this.value = value;
        this.statusCode = statusCode;
        this.error = error;
    }

    public String value() {
        return this.value;
    }

    @Override
    public String error() {
        return this.error;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }
}
