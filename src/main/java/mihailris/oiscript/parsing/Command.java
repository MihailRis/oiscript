package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {
    protected final Position position;

    public Command(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "";
    }

    public List<Command> getCommands(){
        return null;
    }

    public Command build(SemanticContext context) throws ParsingException {
        List<Command> commands = getCommands();
        if (commands != null) {
            List<Command> temp = new ArrayList<>(commands);
            commands.clear();
            for (Command command : temp) {
                command = command.build(context);
                if (command != null) {
                    commands.add(command);
                }
            }
        }
        return this;
    }

    public void execute(Context context) {
        throw new IllegalStateException("not implemented in "+getClass().getSimpleName());
    }
}
