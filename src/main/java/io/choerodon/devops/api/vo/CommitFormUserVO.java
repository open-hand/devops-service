package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 17:15
 * Description:
 */
public class CommitFormUserVO {
    private Long id;
    private String name;
    private String imgUrl;
    private List<Date> commitDates;

    public CommitFormUserVO() {
    }

    public CommitFormUserVO(Long id, String name, String imgUrl, List<Date> commitDates) {
        this.id = id;
        this.name = name;
        this.imgUrl = imgUrl;
        this.commitDates = commitDates;
    }

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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public List<Date> getCommitDates() {
        return commitDates;
    }

    public void setCommitDates(List<Date> commitDates) {
        this.commitDates = commitDates;
    }
}
