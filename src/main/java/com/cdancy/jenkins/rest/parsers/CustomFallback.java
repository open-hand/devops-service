package com.cdancy.jenkins.rest.parsers;

import org.jclouds.Fallback;

import io.choerodon.core.exception.CommonException;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/8 9:59
 */
public class CustomFallback implements Fallback<Void> {

    @Override
    public Void createOrPropagate(Throwable t) throws Exception {
        throw new CommonException(t);
    }
}
