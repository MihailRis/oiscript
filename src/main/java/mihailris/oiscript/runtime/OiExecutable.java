package mihailris.oiscript.runtime;

import mihailris.oiscript.Context;

public interface OiExecutable {
    Object execute(Context context, Object... args);
}
