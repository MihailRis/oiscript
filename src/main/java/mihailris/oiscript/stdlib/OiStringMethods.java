package mihailris.oiscript.stdlib;

import mihailris.oiscript.OiUtils;
import mihailris.oiscript.OiVector;
import mihailris.oiscript.exceptions.NameException;

import java.util.Collection;
import java.util.Iterator;

public class OiStringMethods {
    public static Object call(String string, String name, Object[] args) {
        switch (name) {
            case "join": {
                StringBuilder builder = new StringBuilder();
                OiUtils.requreArgCount("string.join", 1, args);
                Collection<?> collection = (Collection<?>) args[0];
                Iterator<?> iterator = collection.iterator();
                for (int i = 0; i < collection.size(); i++) {
                    builder.append(iterator.next());
                    if (i+1 < collection.size()) {
                        builder.append(string);
                    }
                }
                return builder.toString();
            }
            case "split": {
                String[] substrings = string.split(String.valueOf(args[0]));
                return OiVector.from(substrings);
            }
            case "index": {
                if (args.length == 1){
                    return string.indexOf(String.valueOf(args[0]));
                } else {
                    return string.indexOf(String.valueOf(args[0]), ((Number)args[1]).intValue());
                }
            }
            case "count": {
                String substring = String.valueOf(args[0]);
                if (substring.isEmpty())
                    return 0;
                int count = 0;
                int index = string.indexOf(substring);
                while (index != -1) {
                    count++;
                    index += substring.length();
                    index = string.indexOf(substring, index);
                }
                return count;
            }
            case "lfill": {
                int length = ((Number)args[0]).intValue();
                char chr = (Character)args[1];
                if (string.length() >= length)
                    return string;
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < length-string.length(); i++) {
                    builder.append(chr);
                }
                builder.append(string);
                return builder.toString();
            }
            default:
                throw new NameException("str."+name);
        }
    }
}
