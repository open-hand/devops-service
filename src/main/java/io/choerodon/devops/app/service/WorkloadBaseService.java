package io.choerodon.devops.app.service;

public interface WorkloadBaseService<T> {
    T baseQueryByEnvIdAndName(Long envId, String name);

    T selectByPrimaryKey(Long id);

    void checkExist(Long envId, String name);

    Long baseCreate(T t);

    void baseUpdate(T t);

    void baseDelete(Long id);
}
