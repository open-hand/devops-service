package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/23 14:46
 */
public class CustomPageObject {
    private int size;
    private int page;
    private boolean hasPrevious;
    private boolean hasNext;
    private List<GitlabProjectDTO> projects;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public List<GitlabProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<GitlabProjectDTO> projects) {
        this.projects = projects;
    }
}
