package com.cdancy.jenkins.rest.domain.common;

import com.google.auto.value.AutoValue;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/7 16:42
 */
@AutoValue
public abstract class Response implements Value<String> {
    @SerializedNames({"value", "errors"})
    public static Response create(@Nullable final String value,
                                  final String errors) {

        return new AutoValue_Response(value, errors);
    }
}
