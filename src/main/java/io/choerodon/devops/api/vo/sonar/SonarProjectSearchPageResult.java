package io.choerodon.devops.api.vo.sonar;

import java.util.List;

public class SonarProjectSearchPageResult {

    private Page paging;

    private List<Component> components;

    public Page getPaging() {
        return paging;
    }

    public void setPaging(Page paging) {
        this.paging = paging;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public static class Page {
        private String pageIndex;
        private String pageSize;
        private String total;

        public String getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(String pageIndex) {
            this.pageIndex = pageIndex;
        }

        public String getPageSize() {
            return pageSize;
        }

        public void setPageSize(String pageSize) {
            this.pageSize = pageSize;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }
    }
}
