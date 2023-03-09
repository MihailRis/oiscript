package mihailris.oiscript.parsing;

import mihailris.oiscript.*;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.runtime.Function;
import mihailris.oiscript.runtime.Procedure;

import java.util.*;

import static mihailris.oiscript.Keywords.*;

public class Parser {
    private Source source;
    private final Position position;
    private char[] chars;
    private boolean verbose;
    public Parser() {
        position = new Position();
    }

    public void setSource(Source source) {
        this.source = source;
        chars = source.getSource().toCharArray();
        position.reset();
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private boolean expectIndent(int indent) throws ParsingException {
        int start = position.pos;
        while (position.pos < chars.length) {
            char chr = chars[position.pos];
            if (chr == ' ' || chr == '\t') {
                indent--;
                if (indent < 0)
                    throw new ParsingException(source, position, "wrong indent");
            } else if (indent != 0) {
                position.pos = start;
                return false;
            } else {
                break;
            }
            position.pos++;
        }
        return true;
    }

    private void skipWhitespace() {
        for (; position.pos < chars.length; position.pos++) {
            if (!Character.isWhitespace(chars[position.pos]) || chars[position.pos] == '\n')
                break;
        }
    }

    private void skipWhitespaceAny() {
        for (; position.pos < chars.length; position.pos++) {
            if (chars[position.pos] == '\n') {
                position.newline();
            }
            if (!Character.isWhitespace(chars[position.pos]))
                break;
        }
    }

    private void skipEmptyLines() {
        for (int pos = position.pos; pos < chars.length; pos++) {
            char chr = chars[pos];
            if (Character.isWhitespace(chr) && chr != '\n')
                continue;
            if (chr == '\n') {
                position.pos = pos + 1;
                position.newline();
                skipEmptyLines();
            } else if (chr == '#') {
                skipOnelineComment();
                skipEmptyLines();
            }
            break;
        }
    }

    private void skipOnelineComment() {
        while (position.pos < chars.length && chars[position.pos] != '\n') {
            position.pos++;
        }
        position.pos++;
        position.newline();
    }

    public Script perform(Source source) throws ParsingException {
        this.source = source;
        this.position.reset();
        chars = source.getSource().toCharArray();

        Map<String, Function> functions = new HashMap<>();
        Map<String, Procedure> procedures = new HashMap<>();
        List<String> includes = new ArrayList<>();
        Script script = new Script(source.getFilename(), functions, procedures, includes);
        while (position.pos < chars.length) {
            Position cmdpos = position.cpy();
            skipEmptyLines();
            expectIndent(0);
            if (position.pos >= chars.length)
                break;
            String keyword = expectToken();
            if (keyword == null)
                continue;
            switch (keyword) {
                case PROC: {
                    Procedure procedure = parseProcedure();
                    procedures.put(procedure.getName(), procedure);
                    break;
                }
                case FUNC: {
                    Function function = parseFunction();
                    if (function.getName().equals(INIT) && functions.containsKey(INIT)) {
                        Function initFunction = functions.get(INIT);
                        initFunction.getCommands().addAll(function.getCommands());
                        break;
                    }
                    functions.put(function.getName(), function);
                    break;
                }
                default:
                    position.set(cmdpos);
                    if (chars[position.pos] == '\n') {
                        skipEmptyLines();
                        break;
                    }
                    Command command = parseCommand(false, false, 0);
                    Function initFunction = functions.get(INIT);
                    if (initFunction == null) {
                        initFunction = new Function(INIT, new ArrayList<>(), new ArrayList<>());
                        functions.put(INIT, initFunction);
                    }
                    if (command != null) {
                        initFunction.getCommands().add(command);
                    }
                    seekNewLine(false);
                    break;
            }
        }
        return script;
    }

    private Function parseFunction() throws ParsingException {
        String name = expectName();
        List<String> arguments = parseArgsBlock();
        List<Command> commands = requireBlock(false, false, 1);
        if (verbose) {
            printDebug(commands);
        }
        return new Function(name, arguments, commands);
    }

    private Procedure parseProcedure() throws ParsingException {
        String name = expectName();
        List<String> arguments = parseArgsBlock();
        List<Command> commands = requireBlock(true, false, 1);
        if (verbose) {
            printDebug(commands);
        }
        return new Procedure(name, arguments, commands);
    }

    private void printDebug(List<Command> commands) {
        if (source.getSource().length() > position.pos) {
            System.out.println("REST:");
            System.out.println(source.getSource().substring(position.pos));
        } else {
            System.out.println("END");
        }
        printTree(commands, 0);
    }

    private void printTree(ScriptComponent component) {
        if (component instanceof Function) {
            Function function = (Function) component;
            String argsStr = function.getArgs().toString();
            System.out.println("<func "+function.getName()+"("+argsStr.substring(1, argsStr.length()-1)+")>");
            printTree(function.getCommands(), 0);
        }
        if (component instanceof Procedure) {
            Procedure procedure = (Procedure) component;
            String argsStr = procedure.getArgs().toString();
            System.out.println("<proc "+procedure.getName()+"("+argsStr.substring(1, argsStr.length()-1)+")>");
            printTree(procedure.getCommands(), 0);
        }
    }

    private void printTree(List<Command> commands, int indent) {
        for (Command command : commands) {
            printTree(command, indent);
        }
    }

    private void printTree(Command command, int indent) {
        System.out.print((command.getPosition().line+1)+"| ");
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
        System.out.println(command);
        List<Command> subcommands = command.getCommands();
        if (subcommands != null) {
            printTree(subcommands, indent + 1);
        }
        if (command instanceof If) {
            If branch = (If) command;
            for (Elif elif : branch.getElifs()) {
                printTree(elif, indent);
            }
            Else elseblock = branch.getElseBlock();
            if (elseblock != null) {
                printTree(elseblock, indent);
            }
        }
    }

    private List<Command> _requireBlock(int leastIndent, boolean procedure, boolean loop) throws ParsingException {
        skipEmptyLines();
        int indent = calcIndent();
        if (indent < leastIndent) {
            throw new ParsingException(source, position, "wrong indent (expected more)");
        }
        List<Command> commands = new ArrayList<>();
        do {
            Command command = parseCommand(procedure, loop, indent);
            if (command != null) {
                commands.add(command);
            }
            if (position.getLocalPos() > 0) {
                seekNewLine(false);
                skipEmptyLines();
            }
        } while (position.pos < chars.length && expectIndent(indent));
        Command previous = null;
        for (int i = 0; i < commands.size(); i++) {
            Command command = commands.get(i);
            if ((command instanceof Elif || command instanceof Else)) {
                if (!(previous instanceof If || previous instanceof Elif)) {
                    if (command instanceof Else && previous instanceof While) {
                        While whileNode = (While) previous;
                        whileNode.add((Else)command);
                        commands.remove(command);
                        i--;
                        previous = command;
                        continue;
                    }
                    throw new ParsingException(source, command.position, "invalid use of elif/else ('if' missing)");
                }
            }
            previous = command;
        }
        return commands;
    }

    private void requireProcedure(String operator, boolean procedure, Position position) throws ParsingException {
        if (!procedure)
            throw new ParsingException(source, position, "'"+operator+"' is procedure-only operator");
    }

    private void requireLoop(String operator, boolean loop, Position position) throws ParsingException {
        if (!loop)
            throw new ParsingException(source, position, "'"+operator+"' outside of loop");
    }

    private Command parseCFor(Position cmdpos, boolean procedure, boolean loop, int indent) throws ParsingException {
        position.set(cmdpos);
        expectKeyword();
        skipWhitespace();
        Command initCommand = null;
        if (chars[position.pos] != ';')
            initCommand = parseCommand(procedure, loop, indent);
        position.pos++;
        skipWhitespace();
        Value condition = null;
        if (chars[position.pos] != ';')
            condition = parseValue(indent);
        position.pos++;
        skipWhitespace();
        Command repeatCommand = null;
        if (chars[position.pos] != ':')
            repeatCommand = parseCommand(procedure, loop, indent);
        skipWhitespace();
        List<Command> commands = requireBlock(procedure, true, indent+1);
        return new CForLoop(cmdpos, initCommand, condition, repeatCommand, commands);
    }

    private Command parseCommand(boolean procedure, boolean loop, int indent) throws ParsingException {
        Position cmdpos = position.cpy();
        skipWhitespace();
        if (isNextChar('#')) {
            skipOnelineComment();
            skipEmptyLines();
            if (position.pos >= chars.length)
                return null;
        }
        String token = expectToken();
        skipWhitespace();
        switch (token) {
            case PASS:
                return new Pass(cmdpos);
            case INCLUDE: {
                Value value = parseValue(0);
                return new Include(cmdpos, value);
            }
            case WAIT: {
                requireProcedure(WAIT, procedure, cmdpos);
                Value value = parseValue(indent);
                return new Wait(cmdpos, value);
            }
            case SKIP: {
                requireProcedure(SKIP, procedure, cmdpos);
                skipWhitespace();
                Value value = null;
                if (isToken()) {
                    value = parseValue(indent);
                }
                return new Skip(cmdpos, value);
            }
            case PRINT: {
                Value value = parseValue(indent);
                return new Print(cmdpos, value);
            }
            case IF: {
                Value condition = parseValue(indent);
                List<Command> commands = requireBlock(procedure, loop, indent+1);
                If branch = new If(cmdpos, condition, commands);
                Position initpos = position.cpy();
                skipEmptyLines();
                while (!isEnd()) {
                    skipWhitespace();
                    if (position.pos - position.linepos < indent) {
                        position.set(initpos);
                        break;
                    }
                    if (position.pos - position.linepos > indent)
                        throw new IllegalStateException(cmdpos.toString()+" "+ position);
                    String keyword = expectToken();
                    switch (keyword) {
                        case ELIF:
                            condition = parseValue(indent);
                            commands = requireBlock(procedure, loop, indent+1);
                            branch.add(new Elif(initpos.cpy(), condition, commands));
                            break;
                        case ELSE:
                            commands = requireBlock(procedure, loop, indent+1);
                            branch.add(new Else(initpos.cpy(), commands));
                            return branch;
                        default:
                            position.set(initpos);
                            return branch;
                    }
                }
                return branch;
            }
            case ELSE: {
                throw new ParsingException(source, cmdpos, "'else' out of 'if' context");
            }
            case FOR: {
                if (position.pos < chars.length && chars[position.pos] == ';') {
                    return parseCFor(cmdpos, procedure, loop, indent);
                }
                String name = expectName();
                checkName(name);
                skipWhitespace();
                if (position.pos >= chars.length || chars[position.pos] != ':') {
                    return parseCFor(cmdpos, procedure, loop, indent);
                }
                position.pos++;
                skipWhitespace();
                Value value = parseValue(indent);
                List<Command> commands = requireBlock(procedure, true, indent+1);
                return new ForLoop(cmdpos, name, value, commands);
            }
            case WHILE: {
                Value condition = parseValue(indent);
                List<Command> commands = requireBlock(procedure, true, indent+1);
                While whileloop = new While(cmdpos, condition, commands);
                Position initpos = position.cpy();
                if (!isEnd()) {
                    initpos.set(position);
                    String keyword = expectToken();
                    if (ELSE.equals(keyword)) {
                        commands = requireBlock(procedure, loop, indent + 1);
                        whileloop.add(new Else(initpos, commands));
                    } else {
                        position.set(initpos);
                    }
                }
                return whileloop;
            }
            case TRY:
                List<Command> commands = requireBlock(procedure, true, indent+1);
                String exceptKeyword = expectKeyword();
                if (!EXCEPT.equals(exceptKeyword)) {
                    throw new ParsingException(source, position, "'"+EXCEPT+"' required");
                }
                String name = expectName();
                List<Command> exceptCommands = requireBlock(procedure, true, indent+1);
                return new TryExcept(position, commands, name, exceptCommands);
            case RETURN: {
                Value value = null;
                if (!isNewLineOrEnd())
                    value = parseValue(indent);
                return new Return(cmdpos, value);
            }
            case BREAK: {
                requireLoop(BREAK, loop, cmdpos);
                return new Break(cmdpos);
            }
            case CONTINUE: {
                requireLoop(CONTINUE, loop, cmdpos);
                return new Continue(cmdpos);
            }
            default: {
                String nextToken = expectToken();
                skipWhitespace();
                switch (nextToken) {
                    case "=":
                    case "+=":
                    case "-=":
                    case "*=":
                    case "/=":
                    case "%=":
                        checkName(token, cmdpos);
                        return parseAssign(indent, token, nextToken);
                    case ".":
                        return parseAttributeCommand(indent, tokenToValue(token));
                    case "[":
                        return parseItemCommand(indent, tokenToValue(token));
                }
                position.set(cmdpos);
                if (token.equals(";")) {
                    position.pos++;
                    return null;
                }
                Value value = parseValue(indent);
                return new ValueWrapper(cmdpos.cpy(), value);
            }
        }
    }

    private void checkName(String name) throws ParsingException {
        checkName(name, position);
    }

    private void checkName(String name, Position position) throws ParsingException {
        if (Keywords.isReservedName(name)) {
            throw new ParsingException(source, position, "name '"+name+"' is reserved");
        }
    }

    private boolean isToken() {
        if (position.pos >= chars.length)
            return false;
        char chr = chars[position.pos];
        switch (chr) {
            case '\n':
            case ';':
            case '#':
            case '/':
                return false;
            default:
                return true;
        }
    }

    private Command parseAttributeCommand(int indent, Value leftValue) throws ParsingException {
        String attributeName = expectName();
        skipWhitespace();
        Position startPos = position.cpy();
        String nextToken = expectToken();
        skipWhitespace();
        switch (nextToken) {
            case "=":
            case "+=":
            case "-=":
            case "*=":
            case "/=":
            case "%=":
                return parseAttributeAssign(indent, leftValue, attributeName, nextToken);
            case "[":
                return parseItemCommand(indent, new AttributeValue(leftValue, attributeName));
            case ".":
                return parseAttributeCommand(indent, new AttributeValue(leftValue, attributeName));
            default: {
                position.set(startPos);
                AttributeValue attrib = new AttributeValue(leftValue, attributeName);
                Value value = parseValue(indent, attrib);
                if (value instanceof Call) {
                    Call call = (Call) value;
                    return new ValueWrapper(position.cpy(), new CallMethod(leftValue, attributeName, call.getValues()));
                }
                return new ValueWrapper(position.cpy(), value);
            }
        }
    }

    private Command parseItemCommand(int indent, Value leftValue) throws ParsingException {
        Value indexValue = parseValue(indent);
        position.pos++;
        skipWhitespace();
        String nextToken = expectToken();
        switch (nextToken) {
            case "=":
            case "+=":
            case "-=":
            case "*=":
            case "/=":
            case "%=":
                return parseItemAssign(indent, leftValue, indexValue, nextToken);
            case "[":
                return parseItemCommand(indent, new ItemValue(leftValue, indexValue));
            case ".":
                return parseAttributeCommand(indent, new ItemValue(leftValue, indexValue));
        }
        throw new IllegalStateException();
    }

    private Command parseItemAssign(int indent,
                                    Value leftValue, Value indexValue,
                                    String operator) throws ParsingException {
        Value value = parseValue(indent);
        value = value.optimize();
        return new ItemAssignment(position.cpy(), leftValue, indexValue, operator, value);
    }

    private Command parseAttributeAssign(int indent,
                                         Value source, String name,
                                         String operator) throws ParsingException {
        Value value = parseValue(indent);
        value = value.optimize();
        return new AttributeAssignment(position.cpy(), source, name, operator, value);
    }

    private Command parseAssign(int indent, String name, String operator) throws ParsingException {
        Value value = parseValue(indent);
        value = value.optimize();
        return new Assignment(position.cpy(), name, operator, value);
    }

    private Value parseList(int indent) throws ParsingException {
        skipWhitespaceAny();
        List<Value> values = new ArrayList<>();
        if (chars[position.pos] == ']') {
            position.pos++;
            return new ListValue(values);
        }
        while (!isEnd()){
            values.add(parseValue(indent));
            skipWhitespaceAny();
            if (position.pos < chars.length && chars[position.pos] == ']') {
                position.pos++;
                return new ListValue(values);
            }
            requireCommaOrEnd();
            position.pos++;
            skipWhitespaceAny();
            if (position.pos < chars.length && chars[position.pos] == ']') {
                position.pos++;
                return new ListValue(values);
            }
        }
        throw new ParsingException(source, position, "']' expected");
    }

    private Value parseMap(int indent) throws ParsingException {
        skipWhitespaceAny();
        Map<Value, Value> values = new HashMap<>();
        if (chars[position.pos] == '}') {
            position.pos++;
            return new MapValue(values);
        }
        while (!isEnd()) {
            Value key = parseValue(indent);
            skipWhitespaceAny();
            if (position.pos >= chars.length || chars[position.pos] != ':') {
                throw new ParsingException(source, position, "':' expected");
            }
            position.pos++;
            skipWhitespaceAny();
            values.put(key, parseValue(indent));
            if (position.pos < chars.length && chars[position.pos] == '}') {
                position.pos++;
                return new MapValue(values);
            }
            requireCommaOrEnd();
            position.pos++;
            skipWhitespaceAny();
            if (position.pos < chars.length && chars[position.pos] == '}') {
                position.pos++;
                return new MapValue(values);
            }
        }
        throw new ParsingException(source, position, "'}' expected");
    }

    public Value parseValue(int indent) throws ParsingException {
        return parseValue(indent, false);
    }

    public Value parseValue(int indent, boolean breakOnOp) throws ParsingException {
        skipWhitespace();
        Position startPos = position.cpy();
        String token = expectToken();
        if (token.equals(")")) {
            position.set(startPos);
            return new Void();
        }
        // anonymous function
        if (token.equals(FUNC)) {
            skipWhitespace();

            List<String> arguments = parseArgsBlock();
            skipWhitespace();

            List<Command> commands = requireBlock(false, true, indent+1);
            return new Function(null, arguments, commands);
        }
        if (token.equals("(")) {
            ValueBlock valueBlock = new ValueBlock(parseValue(indent));
            position.pos++;
            skipWhitespace();
            return parseValue(indent, valueBlock);
        }
        if (Operators.isUnaryOperator(token)) {
            skipWhitespace();
            switch (token) {
                case "-":
                    return parseValue(indent, new Negative(parseValue(indent, true)), breakOnOp);
                case "~":
                    return parseValue(indent, new BitInverse(parseValue(indent, true)), breakOnOp);
                case "*":
                    return parseValue(indent, new RestHolder(parseValue(indent, true)), breakOnOp);
                case "+":
                    return parseValue(indent, true);
                case "!":
                case "not":
                    return new Not(parseValue(indent, true));
                case "new": {
                    String className = expectName();
                    skipWhitespace();
                    if (position.pos >= chars.length || chars[position.pos] != '(')
                        throw new ParsingException(source, position, "'(' expected");
                    position.pos++;
                    List<Value> values = new ArrayList<>();
                    values.add(new NamedValue(className));
                    while (!isNewLineOrEnd()){
                        values.add(parseValue(indent));
                        requireCommaOrEnd();
                        if (chars[position.pos] != ')') {
                            position.pos++;
                            skipWhitespace();
                        }
                    }
                    position.pos++;
                    Call callStdNew = new Call(new NamedValue("$new"), values);
                    return parseValue(indent, callStdNew, breakOnOp);
                }
                default:
                    throw new ParsingException(source, position, "not implemented for unary '"+token+"'");
            }
        }
        if (token.equals("[")) {
            return parseValue(indent, parseList(indent), breakOnOp);
        }
        if (token.equals("{")) {
            return parseValue(indent, parseMap(indent), breakOnOp);
        }
        return parseValue(indent, tokenToValue(token), breakOnOp);
    }

    private Value parseValue(int indent, Value leftOperand) throws ParsingException {
        return parseValue(indent, leftOperand, false);
    }

    private Value parseValue(int indent, Value leftOperand, boolean breakOnOp) throws ParsingException {
        skipWhitespace();

        if (!isNewLineOrEnd()) {
            Position initpos = position.cpy();
            String next = expectToken();
            if (next.equals("?")) {
                skipWhitespace();
                Value valueA = parseValue(indent);
                skipWhitespace();
                if (position.pos >= chars.length || chars[position.pos] != ':')
                    throw new ParsingException(source, position, "':' expected");
                position.pos++;
                Value valueB = parseValue(indent);
                return new Ternary(leftOperand, valueA, valueB);
            }
            if (Operators.isOperator(next)) {
                if (breakOnOp) {
                    position.set(initpos);
                    return leftOperand;
                }
                skipWhitespace();
                Value rightOperand = parseValue(indent);
                BinaryOperator binaryOperator = new BinaryOperator(leftOperand, next, rightOperand);
                binaryOperator = binaryOperator.correction();
                skipWhitespace();
                return binaryOperator;
            } else if (next.equals(".")) {
                skipWhitespace();
                if (leftOperand instanceof NumberValue) {
                    String valueRight = expectToken();
                    return parseValue(indent, NumberValue.choose(
                            ((NumberValue) leftOperand).value + Double.parseDouble("0."+valueRight)));
                }
                String attribute = expectName();
                Value value = new AttributeValue(leftOperand, attribute);
                skipWhitespace();
                Value parsedValue = parseValue(indent, value);
                if (parsedValue instanceof Call) {
                    Call call = (Call) parsedValue;
                    return new CallMethod(leftOperand, attribute, call.getValues());
                }
                return parsedValue;
            } else if (next.equals("[")) {
                skipWhitespace();
                if (chars[position.pos] == ':'){
                    return parseSlice(indent, leftOperand, null);
                }
                Value indexValue = parseValue(indent);
                skipWhitespace();
                if (chars[position.pos] == ':') {
                    return parseSlice(indent, leftOperand, indexValue);
                }
                position.pos++;
                Value value = new ItemValue(leftOperand, indexValue);
                skipWhitespace();
                return parseValue(indent, value);
            } else if (next.equals("(")) {
                skipWhitespace();
                List<Value> values = new ArrayList<>();
                while (!isNewLineOrEnd()){
                    values.add(parseValue(indent));
                    requireCommaOrEnd();
                    if (chars[position.pos] != ')') {
                        position.pos++;
                        skipWhitespace();
                    }
                    //System.out.println("Parser.parseValue "+chars[position.pos]);
                }
                position.pos++;
                return parseValue(indent, new Call(leftOperand, values));
            } else {
                throw new ParsingException(source, position, "unexpected token '"+next+"'");
            }
        }
        return leftOperand;
    }

    private Value parseSlice(int indent, Value leftValue, Value left) throws ParsingException {
        position.pos++;
        skipWhitespace();
        Value right;
        Value step = null;
        if (chars[position.pos] == ':') {
            right = null;
        } else if (chars[position.pos] == ']') {
            position.pos++;
            return new Slice(leftValue, left, null, null);
        } else {
            right = parseValue(indent);
        }
        skipWhitespace();
        if (chars[position.pos] == ':') {
            position.pos++;
            skipWhitespace();
            if (chars[position.pos] != ']') {
                step = parseValue(indent);
                skipWhitespace();
            }
        }
        if (chars[position.pos] != ']')
            throw new ParsingException(source, position, "']' expected");
        position.pos++;
        return new Slice(leftValue, left, right, step);
    }

    private void requireCommaOrEnd() throws ParsingException {
        skipWhitespace();
        if (isNewLineOrEnd())
            return;
        if (chars[position.pos] == ',')
            return;
        throw new ParsingException(source, position, "',' expected");
    }

    private Value tokenToValue(String token) throws ParsingException {
        if (Character.isDigit(token.charAt(0))) {
            if (token.startsWith("0x")) {
                return NumberValue.choose(Long.parseLong(token.substring(2), 16));
            } else if (token.startsWith("0o")) {
                return NumberValue.choose(Long.parseLong(token.substring(2), 8));
            } else if (token.startsWith("0b")) {
                return NumberValue.choose(Long.parseLong(token.substring(2), 2));
            }
            return NumberValue.choose(Double.parseDouble(token));
        } else if (token.equals(TRUE) || token.equals(FALSE)) {
            return new BooleanValue(token.equals(TRUE));
        } else if (token.equals(NONE)) {
            return OiNone.NONE;
        } else if (token.equals(SCRIPT)) {
            return ScriptRef.INSTANCE;
        } else if (Character.isJavaIdentifierStart(token.charAt(0))) {
            checkName(token);
            return new NamedValue(token);
        } else if (token.startsWith("\"")){
            return new StringValue(token.substring(1));
        } else if (token.startsWith("'")) {
            return new CharValue(token.charAt(1));
        } else if (token.startsWith("`")) {
            return new StringValue(token.substring(1));
        } else {
            throw new IllegalStateException(token);
        }
    }

    private boolean isNewLineOrEnd() {
        if (position.pos >= chars.length)
            return true;
        char c = chars[position.pos];
        return c == '\n' || c == ')' || c == ']' || c == ':' || c == ',' || c == '}' || c == ';' || c == '#';
    }

    private boolean isEnd() {
        if (position.pos >= chars.length)
            return true;
        char c = chars[position.pos];
        return c == ')' || c == ']' || c == ':' || c == ',' || c == '}' || c == ';';
    }

    private int calcIndent() {
        int indent = 0;
        for (int pos = position.pos; pos < chars.length; pos++) {
            if (Character.isWhitespace(chars[pos])) {
                indent++;
            } else {
                break;
            }
        }
        return Math.max(indent, position.pos-position.linepos);
    }

    private List<Command> requireBlock(boolean procedure, boolean loop, int leastIndent) throws ParsingException {
        if (position.pos >= chars.length || chars[position.pos] != ':')
            throw new ParsingException(source, position, "':' expected");
        position.pos++;
        seekNewLine(true);
        return _requireBlock(leastIndent, procedure, loop);
    }

    private void seekNewLine(boolean nextRequired) throws ParsingException {
        skipWhitespace();
        if (position.pos >= chars.length) {
            if (nextRequired)
                throw new ParsingException(source, position, "unexpected end");
            else
                return;
        }
        if (chars[position.pos] == '\n') {
            position.pos++;
            position.newline();
            return;
        }
        if (chars[position.pos] == '#') {
            while (position.pos < chars.length && chars[position.pos] != '\n') {
                position.pos++;
            }
            position.pos++;
            return;
        }
        if (isEnd() && nextRequired)
            throw new ParsingException(source, position, "unexpected end");
    }

    private List<String> parseArgsBlock() throws ParsingException {
        if (position.pos >= chars.length || chars[position.pos] != '(')
            throw new ParsingException(source, position, "'(' expected");
        position.pos++;
        boolean ended = false;
        List<String> arguments = new ArrayList<>();
        while (position.pos < chars.length) {
            skipWhitespace();
            char chr = chars[position.pos];
            if (chr == ')') {
                ended = true;
                position.pos++;
                break;
            }
            if (isNextChar('*')){
                position.pos++;
                String name = expectName();
                skipWhitespace();
                if (!isNextChar(')')){
                    throw new ParsingException(source, position, "')' expected after *"+name);
                }
                arguments.add("*"+name);
                continue;
            }
            String name = expectName();
            arguments.add(name);
            skipWhitespace();
            chr = chars[position.pos];
            if (chr == ',') {
                position.pos++;
                continue;
            }
            if (chr != ')') {
                throw new ParsingException(source, position, "',' or ')' expected");
            }
        }
        if (!ended) {
            throw new ParsingException(source, position, "')' expected");
        }
        return arguments;
    }

    private boolean isNextChar(char c) {
        if (position.pos >= chars.length)
            return false;
        return chars[position.pos] == c;
    }

    private String expectKeyword() throws ParsingException {
        for (int pos = position.pos; pos < chars.length; pos++) {
            char chr = chars[pos];
            if (Character.isWhitespace(chr)) {
                String keyword = source.getSource().substring(position.pos, pos);
                position.pos = pos;
                skipWhitespace();
                return keyword;
            } else if (!Character.isJavaIdentifierStart(chr)) {
                throw new ParsingException(source, position, "keyword expected");
            }
        }
        return null;
    }

    private String expectName() throws ParsingException {
        int pos = position.pos;
        if (pos >= chars.length || !Character.isJavaIdentifierStart(chars[pos]))
            throw new ParsingException(source, position, "name expected");
        pos++;
        for (; pos < chars.length; pos++) {
            char chr = chars[pos];
            if (Character.isWhitespace(chr) ||
                    chr == '(' || chr == ',' || chr == '.' || chr == ')' ||
                    Operators.isOperatorChar(chr) || chr == '[' || chr == ':' || chr == ']') {
                String name = source.getSource().substring(position.pos, pos);
                position.pos = pos;
                skipWhitespace();
                return name;
            } else if (!Character.isJavaIdentifierPart(chr)) {
                throw new ParsingException(source, position, "name expected, got '"+chr+"'");
            }
        }
        String name = source.getSource().substring(position.pos, pos);
        position.pos = pos;
        return name;
    }

    private boolean isInteger(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String expectToken() throws ParsingException {
        if (position.pos >= chars.length)
            throw new ParsingException(source, position, "token expected");
        char chr = chars[position.pos];
        if (chr == '\n')
            throw new ParsingException(source, position, "token expected");
        if (chr == ';')
            return String.valueOf(chr);
        if (chr == '.' || chr == '(' || chr == ')' || chr == '[' || chr == ']' || chr == ',' || chr == '{' || chr == '}') {
            position.pos++;
            if (chr == '.' && position.pos < chars.length && Character.isDigit(chars[position.pos])) {
                return "0."+expectToken();
            }
            return String.valueOf(chr);
        }
        if (chr == '\'') {
            String literal = parseString();
            if (literal.length() != 2) {
                throw new ParsingException(source, position, "invalid char literal");
            }
            return literal;
        }
        if (chr == '"'){
            return parseString();
        }
        if (chr == '`') {
            for (int i = position.pos+1; i < chars.length; i++) {
                if (chars[i] == '`') {
                    String literal = source.getSource().substring(position.pos, i);
                    position.pos = i+1;
                    return literal;
                }
            }
            throw new ParsingException(source, position, "non-closed '`' literal");
        }
        if (Operators.isOperatorChar(chr)) {
            return readOperator();
        }
        int pos = position.pos;
        for (; pos < chars.length; pos++) {
            chr = chars[pos];
            if (Syntax.isBreakingChar(chr)) {
                String keyword = source.getSource().substring(position.pos, pos);
                position.pos = pos;
                skipWhitespace();
                if (isInteger(keyword) && chr == '.') {
                    position.pos++;
                    skipWhitespace();
                    String other = expectToken();
                    return keyword+"."+other;
                }
                return keyword;
            } else if (chr == '\n') {
                throw new ParsingException(source, position, "token expected");
            }
        }
        String token = source.getSource().substring(position.pos, pos);
        position.pos = pos;
        return token;
    }

    /**
     * Parse escaped string
     */
    private String parseString() throws ParsingException {
        char openChar = chars[position.pos++];
        StringBuilder builder = new StringBuilder(String.valueOf(openChar));
        boolean escaped = false;
        for (; position.pos < chars.length; position.pos++) {
            char chr = chars[position.pos];
            if (chr == '\\') {
                if (escaped) {
                    escaped = false;
                } else {
                    escaped = true;
                    continue;
                }
            }
            if (!escaped && chr == '\n') {
                throw new ParsingException(source, position, "'"+openChar+"' expected");
            }
            if (chr == openChar) {
                if (!escaped) {
                    position.pos++;
                    return builder.toString();
                }
                escaped = false;
            } else if (escaped){
                switch (chr) {
                    case 'n': builder.append('\n'); escaped = false; break;
                    case 'r': builder.append('\r'); escaped = false; break;
                    case '\'': builder.append('\''); escaped = false; break;
                    case '"': builder.append('"'); escaped = false; break;
                    case '0': builder.append('\0'); escaped = false; break;
                    case 'b': builder.append('\b'); escaped = false; break;
                    case 't': builder.append('\t'); escaped = false; break;
                    case 'f': builder.append('\f'); escaped = false; break;
                    default:
                        throw new ParsingException(source, position, "unknown escaped character '\\"+chr+"'");
                }
                continue;
            }
            builder.append(chr);
        }
        throw new ParsingException(source, position, "'"+openChar+"' expected");
    }

    private String readOperator() throws ParsingException {
        int pos = position.pos+1;
        for (; pos < chars.length; pos++) {
            char chr = chars[pos];
            if (Character.isWhitespace(chr) || Operators.isBreakingOperatorChar(chars[pos-1], chr) || !Operators.isOperatorChar(chr)) {
                String operator = source.getSource().substring(position.pos, pos);
                position.pos = pos;
                skipWhitespace();
                return operator;
            } else if (chr == '\n') {
                throw new ParsingException(source, position, "token expected");
            }
        }
        String operator = source.getSource().substring(position.pos, pos);
        position.pos = pos;
        return operator;
    }
}
