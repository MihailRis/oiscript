# OiScript
## Table of Contents:
- [About](#about)
- [Syntax](#syntax)
- [Oi Types](#oi-types)
  - [number](#number)
  - [string](#string)
  - [OiObject](#oiobject)
- [Built-in modules](#built-in-modules):
  - [std](#std)
- [API](#api)
  - [Single expressions](#single-expressions)
  - [Scripting](#scripting)
  - [Custom library](#custom-library)
## About:
Simple dynamic-typed scripting engine and language with Python-like syntax 
for use in Java applications.

- Status: in-development
- Version: 0.9.4

Engine works with java interfaces:
+ Collection
+ Iterable

## Syntax:
Hello-world:
```oi
print("Hello, World!")
```

Functions:
```oi
func sum(values):
    result = 0
    for value : values:
        result += value
    return result
```

<details>
    <summary>Language keywords list</summary>

syntax:

```
and break continue elif else for func in is not or proc 
return to while pass do skip wait and or
```

types:
```
int float str bool vector map
```

values:
```
none true false nan
```
</details>

## Oi types:

### number:

Java Integer/Long and Float/Double types combined.

### string:
Java String

**string.join(iterable):**

```oi
print(", ".join(1 to 5))

Output:
1, 2, 3, 4, 5
```

### OiObject:
Map<Object, Object> wrapper.

```oi
obj = {"fieldA": 174, "fieldB": 42}
print(obj.fieldA)
print(obj["fieldB"])

Output:
174
42
```

Using prototypes as classes:

```oi
func User_init(self, name, age):
    self.name = name
    self.age = age
    
func User_tostr(self):
    return "User("+self.name+" age: "+self.age+")")
    
User = {
    "_init": User_init,
    "tostr": User_tostr,
}

print(new User("unknown", 45).tostr())
```

`new User(name, age)` is equialent to `std._included["$new"](User, name, age)`

Where `User` is prototype for `user`.

$new - is a function that may be implemented (without constructor args) in OiScript as:
```oi
func oi_new(prototype):
  obj = {"_proto": prototype}
  obj._init()
  return obj
```

## Built-in modules:

### **std** 
(included automatically like `__builtins__` in Python):

---
`_version` -> str

OiScript version string in format `major.minor.patch` (example: `0.9.2`).
May be accessed only directly as `std._version` because it's hidden field.

---
`endl` -> str

System line separator.

---
`print(*values)` -> none

Print all args separated with space and **endl** included.

---
`bool(value)` -> bool

Convert value to boolean.

---
`int(value)` -> int

Convert value to integer.

---
`chr(integer)` -> char

Cast integer to char.

---
`keys(object)` -> vector

Get list of OiObject attributes names.

---
`rand()` -> float

Generate pseudo-random number in range [0.0, 1.0].

---
`vector(iterable)` -> vector

Create vector from Iterable object.

---
`cat(object)` -> str

Create string with shown escaped characters.
Example: `some "text""` -> `"some \"text\"""`.
Wraps strings into `""`.

---
`nanotime()` -> float

Get System.nanoTime() but in seconds.

---
`$new(prototype, *args)` -> OiObject

Create new instance using prototype. 

```oi
new SomeProto(arg1, arg2, arg3)
```
will be converted to 
```
std._included["$new"](SomeProto, arg1, arg2, arg3)
```

## API
### Single expressions
OiScript.eval(expression_string) calculates an expression result.
```java
OI.eval("20 - 5 * 3.2") -> 4.0
OI.eval("2 * 3 == 6") -> true
OI.eval("[]") -> mihailris.oiscript.OiVector extends ArrayList<Object>
OI.eval("sqrt(81)") -> 9.0
```
### Scripting
#### Initializing:
```java
// create globals namespace object
OiObject globals = new OiObject();
// add std-modules to globals
globals.set("std", OI.moduleStd);
globals.set("math", OI.moduleMath);
// create scripts map object
OiObject scripts = new OiObject();
```

#### Loading a script:
```java
String sourceName = ...;
String sourceCode = ...;
Script script = OI.load(sourceName, sourceCode, globals, scripts);
// execute module
script.init();
```

#### Execute script function:

Source:
```oi
func factorial(num):
    if num == 1:
        return 1
    return num*factorial(num-1)
```

Usage:
```java
long num = script.execute("factorial", 10);
System.out.println(num);
```

#### Start process:
```java
OiRunHandle handle = script.start(proc_name);
```

#### Continue process:
```java
while (!runHandle.isFinished()) {
    System.out.println("interruption (skip or wait)");
    runHandle.continueProc();
}
```
or
```java
while (!runHandle.isFinished()) {
    System.out.println("interruption (skip or wait)");
    long delta_time_ms = ...;
    runHandle.continueProc(delta_time_ms);
}
```

### Custom library:

#### Create module:
```java
OiModule module = new OiModule();
```

#### Set values:
```java
module.set("gravity", 9.8);
```

#### Custom functions:
```java
module.set("shutdown", OiUtils.customFunc("shutdown", (context, args) -> {
    Number exitCode = (Number)args[0];
    SomeImportantClassIdk.shutdown(exitCode.intValue());
    return OiNone.NONE;
}, 1))
```
