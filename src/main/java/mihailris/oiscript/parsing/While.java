package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.BreakSignal;
import mihailris.oiscript.exceptions.ContinueSignal;
import mihailris.oiscript.exceptions.ParsingException;

import java.util.List;

public class While extends Command {
    private Value condition;
    private final List<Command> commands;
    private Else elseBlock;

    public While(Position position, Value condition, List<Command> commands) {
        super(position);
        this.condition = condition;
        this.commands = commands;
    }

    @Override
    public Command build(SemanticContext context) throws ParsingException {
        condition = condition.build(context);
        return super.build(context);
    }

    @Override
    public void execute(Context context) {
        try {
            while (true) {
                Object conditionValue = condition.eval(context);
                if (!(conditionValue instanceof Boolean && !(Boolean) conditionValue)) {
                    for (Command command : commands) {
                        try {
                            command.execute(context);
                            if (context.returned)
                                return;
                        } catch (ContinueSignal ignored) {
                        }
                    }
                } else {
                    if (elseBlock != null) {
                        for (Command command : elseBlock.getCommands()){
                            command.execute(context);
                            if (context.returned)
                                return;
                        }
                    }
                    break;
                }
            }
        } catch (BreakSignal ignored) {
        }
    }

    public Value getCondition() {
        return condition;
    }

    public Else getElseBlock() {
        return elseBlock;
    }

    @Override
    public String toString() {
        return super.toString()+"while "+condition;
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    public void add(Else elseNode) {
        this.elseBlock = elseNode;
    }
}
