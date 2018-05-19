package io.choerodon.devops.api.dto;

/**
 * Created by younger on 2018/4/10.
 */
public class ApplicationUpdateDTO {

    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
