package io.choerodon.devops.api.vo.kubernetes;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:00 2019/6/24
 * Description:
 */
public class MenuCodeDTO {
    private Long menuCodeId;
    private String menuCode;

    public Long getMenuCodeId() {
        return menuCodeId;
    }

    public void setMenuCodeId(Long menuCodeId) {
        this.menuCodeId = menuCodeId;
    }

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }
}
