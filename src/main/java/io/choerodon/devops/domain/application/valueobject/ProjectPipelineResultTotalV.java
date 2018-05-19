package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

/**
 * Created by Zenger on 2018/4/9.
 */
public class ProjectPipelineResultTotalV {

    private Integer totalPages;
    private Integer totalElements;
    private Integer numberOfElements;
    private Integer number;
    private Integer size;
    private List<PipelineResultV> content;

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<PipelineResultV> getContent() {
        return content;
    }

    public void setContent(List<PipelineResultV> content) {
        this.content = content;
    }
}
