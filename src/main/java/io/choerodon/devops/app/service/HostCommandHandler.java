package io.choerodon.devops.app.service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/19 16:57
 */
@FunctionalInterface
public interface HostCommandHandler<T, K> {

    void accept(T t, K k);

}
