package io.choerodon.devops.infra.enums;

public interface Type {
    int getMask();

    boolean isSet(int flags);
}
