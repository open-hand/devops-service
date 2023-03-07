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
    private String errors;

    AutoValue_Response(String value, String errors) {
        this.value = value;
        this.errors = errors;
    }

    public String value() {
        return this.value;
    }

    public String errors() {
        return this.errors;
    }
}
