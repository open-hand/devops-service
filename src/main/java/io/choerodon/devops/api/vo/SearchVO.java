package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

/**
 * Created by wangxiang on 2020/12/1
 */
public class SearchVO {
    private List<Object> params;
    private Map<String, Object> searchParam;

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public Map<String, Object> getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(Map<String, Object> searchParam) {
        this.searchParam = searchParam;
    }
}
