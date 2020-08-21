package io.choerodon.devops.api.vo;

import java.util.List;

public class CountVO {
    private List<String> date;
    private List<Long> count;

    public List<String> getDate() {
        return date;
    }

    public void setDate(List<String> date) {
        this.date = date;
    }

    public List<Long> getCount() {
        return count;
    }

    public void setCount(List<Long> count) {
        this.count = count;
    }
}
