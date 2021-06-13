package io.choerodon.devops.app.service;

public interface WorkloadBaseService<T, E> {
    T baseQueryByEnvIdAndName(Long envId, String name);

    T selectByPrimaryKey(Long id);

    void checkExist(Long envId, String name);

    Long baseCreate(T t);

    void baseUpdate(T t);

    void baseDelete(Long id);

    E createOrUpdateByGitOps(E t, Long userId, String content);

    void deleteByGitOps(Long id);
}
