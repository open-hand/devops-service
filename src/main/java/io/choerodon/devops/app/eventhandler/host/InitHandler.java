package io.choerodon.devops.app.eventhandler.host;

import static io.choerodon.devops.infra.enums.host.HostMsgEventEnum.INIT_AGENT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import io.choerodon.devops.api.vo.host.HostInfoVO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
import io.choerodon.devops.infra.util.JsonHelper;

@Component
public class InitHandler implements HostMsgHandler {
    @Autowired
    private DevopsHostMapper devopsHostMapper;


    @Override
    public void handler(String hostId, Long commandId, String payload) {
        if (!ObjectUtils.isEmpty(payload)) {
            HostInfoVO hostInfoVO = JsonHelper.unmarshalByJackson(payload, HostInfoVO.class);
            if (!ObjectUtils.isEmpty(hostInfoVO.getNetwork())) {
                DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(Long.valueOf(hostId));
                devopsHostDTO.setNetwork(hostInfoVO.getNetwork());
                devopsHostMapper.updateByPrimaryKeySelective(devopsHostDTO);
            }
        }
    }

    @Override
    public String getType() {
        return INIT_AGENT.value();
    }
}
