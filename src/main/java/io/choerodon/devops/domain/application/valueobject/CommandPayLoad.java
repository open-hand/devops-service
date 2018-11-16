package io.choerodon.devops.domain.application.valueobject;

import  java.util.List;

public class CommandPayLoad {
    List<Command> commands;


    public CommandPayLoad(List<Command> commands) {
        this.commands = commands;
    }


    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }
}
