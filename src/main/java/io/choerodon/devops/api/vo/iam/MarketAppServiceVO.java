package io.choerodon.devops.api.vo.iam;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:04 2019/8/27
 * Description: 应用市场上传修复版本Payload使用
 */
public class MarketAppServiceVO {
    private Long id;

    private String marketAppCode;

    private String name;

    private String code;

    private MarketServiceVersionDTO marketServiceVersionCreateDTO;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMarketAppCode() {
        return marketAppCode;
    }

    public void setMarketAppCode(String marketAppCode) {
        this.marketAppCode = marketAppCode;
    }

    public MarketServiceVersionDTO getMarketServiceVersionCreateDTO() {
        return marketServiceVersionCreateDTO;
    }

    public void setMarketServiceVersionCreateDTO(MarketServiceVersionDTO marketServiceVersionCreateDTO) {
        this.marketServiceVersionCreateDTO = marketServiceVersionCreateDTO;
    }
}
