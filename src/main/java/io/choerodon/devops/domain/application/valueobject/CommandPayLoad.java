package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

public class CommandPayLoad {
    List<CommandVO> commandVOS;


    public CommandPayLoad(List<CommandVO> commandVOS) {
        this.commandVOS = commandVOS;
    }


    public List<CommandVO> getCommandVOS() {
        return commandVOS;
    }

    public void setCommandVOS(List<CommandVO> commandVOS) {
        this.commandVOS = commandVOS;
    }
}
