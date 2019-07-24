package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.api.vo.kubernetes.PipelineResultVO;


/**
 * Created by Zenger on 2018/4/9.
 */
public class ProjectPipelineResultTotalVO {

    private Integer totalPages;
    private Integer totalElements;
    private Integer numberOfElements;
    private Integer number;
    private Integer size;
    private List<PipelineResultVO> content;

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

    public List<PipelineResultVO> getContent() {
        return content;
    }

    public void setContent(List<PipelineResultVO> content) {
        this.content = content;
    }
}
