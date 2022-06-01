package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 17:15
 * Description:
 */
public class CommitFormUserVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("用户")
    private String name;
    @ApiModelProperty("头像")
    private String imgUrl;
    @ApiModelProperty("提交日期")
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
