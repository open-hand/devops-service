package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.api.vo.MarketInstanceCreationRequestVO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.market.MarketServiceDTO;
import io.choerodon.devops.infra.dto.market.MarketServiceVersionDTO;

/**
 * Created by Sheep on 2019/7/4.
 */
public class MarketInstanceSagaPayload {

    private Long projectId;
    private Long gitlabUserId;
    private Long commandId;
    private String secretCode;
    private MarketServiceDTO marketServiceDTO;
    private MarketServiceVersionDTO marketServiceVersionDTO;
    private DevopsEnvironmentDTO devopsEnvironmentDTO;
    private MarketInstanceCreationRequestVO marketInstanceCreationRequestVO;
    private DevopsServiceReqVO devopsServiceReqVO;
    private DevopsIngressVO devopsIngressVO;


    public MarketInstanceSagaPayload() {
    }


    public MarketInstanceSagaPayload(Long projectId, Long gitlabUserId, String secretCode, Long commandId) {
        this.projectId = projectId;
        this.gitlabUserId = gitlabUserId;
        this.secretCode = secretCode;
        this.commandId = commandId;
    }


    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public DevopsEnvironmentDTO getDevopsEnvironmentDTO() {
        return devopsEnvironmentDTO;
    }

    public void setDevopsEnvironmentDTO(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        this.devopsEnvironmentDTO = devopsEnvironmentDTO;
    }

    public MarketInstanceCreationRequestVO getMarketInstanceCreationRequestVO() {
        return marketInstanceCreationRequestVO;
    }

    public void setMarketInstanceCreationRequestVO(MarketInstanceCreationRequestVO marketInstanceCreationRequestVO) {
        this.marketInstanceCreationRequestVO = marketInstanceCreationRequestVO;
    }

    public DevopsServiceReqVO getDevopsServiceReqVO() {
        return devopsServiceReqVO;
    }

    public void setDevopsServiceReqVO(DevopsServiceReqVO devopsServiceReqVO) {
        this.devopsServiceReqVO = devopsServiceReqVO;
    }

    public DevopsIngressVO getDevopsIngressVO() {
        return devopsIngressVO;
    }

    public void setDevopsIngressVO(DevopsIngressVO devopsIngressVO) {
        this.devopsIngressVO = devopsIngressVO;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public MarketServiceDTO getMarketServiceDTO() {
        return marketServiceDTO;
    }

    public void setMarketServiceDTO(MarketServiceDTO marketServiceDTO) {
        this.marketServiceDTO = marketServiceDTO;
    }

    public MarketServiceVersionDTO getMarketServiceVersionDTO() {
        return marketServiceVersionDTO;
    }

    public void setMarketServiceVersionDTO(MarketServiceVersionDTO marketServiceVersionDTO) {
        this.marketServiceVersionDTO = marketServiceVersionDTO;
    }
}
