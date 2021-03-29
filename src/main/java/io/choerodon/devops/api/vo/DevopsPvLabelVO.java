package io.choerodon.devops.api.vo;

import java.util.Objects;

public class DevopsPvLabelVO {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DevopsPvLabelVO that = (DevopsPvLabelVO) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }
}
