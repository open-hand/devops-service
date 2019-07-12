package io.choerodon.devops.infra.dto.gitlab;

import java.util.List;

/**
 * Created by Zenger on 2018/4/8.
 */
public class TagsDO {

    private List<TagDTO> tagList;

    private int totalPages;

    private int totalElements;

    public TagsDO(List<TagDTO> tagList, int totalPages) {
        this.tagList = tagList;
        this.totalPages = totalPages;
    }

    public TagsDO() {
    }

    /**
     * 构造方法
     */
    public TagsDO(List<TagDTO> tagList, int totalPages, int totalElements) {
        this.tagList = tagList;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public List<TagDTO> getTagList() {
        return tagList;
    }

    public void setTagList(List<TagDTO> tagList) {
        this.tagList = tagList;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }
}
