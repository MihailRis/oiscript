package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.Logics;

import java.util.ArrayList;
import java.util.List;

public class If extends Command {
    private final Value condition;
    private final List<Command> commands;
    private final List<Elif> elifs;
    private Else elseBlock;
    public If(Position position, Value condition, List<Command> commands) {
        super(position);
        this.condition = condition;
        this.commands = commands;
        this.elifs = new ArrayList<>();
    }

    public void add(Elif elif){
        elifs.add(elif);
    }

    public void add(Else elseBlock) {
        this.elseBlock = elseBlock;
    }

    public List<Elif> getElifs() {
        return elifs;
    }

    public Else getElseBlock() {
        return elseBlock;
    }

    @Override
    public void execute(Context context) {
        Object conditionValue = condition.eval(context);
        if (Logics.isTrue(conditionValue)) {
            for (Command command : commands) {
                command.execute(context);
                if (context.returned)
                    return;
            }
            return;
        }
        for (Elif elif : elifs) {
            conditionValue = elif.getCondition().eval(context);
            if (Logics.isTrue(conditionValue)) {
                for (Command command : elif.getCommands()) {
                    command.execute(context);
                    if (context.returned)
                        return;
                }
                return;
            }
        }
        if (elseBlock != null) {
            for (Command command : elseBlock.getCommands()) {
                command.execute(context);
                if (context.returned)
                    return;
            }
        }
    }

    @Override
    public String toString() {
        return super.toString()+"if "+condition;
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }
}
