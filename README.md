# AndroidPython
Running python on Android

# Introduction
Base on Common Language Extension  [CLE](http://www.srplab.com)

Support python2.7 script execution

The execution result is recorded by `log.d` at present. Check it by **logcat**. 
# How to use your code

## Execute from file
- Put your target `.py` file to `assets` directory
- use code like:
```
Service._DoFile("python", appFile.getPath() + "/<FILENAME>.py", "");
python._Call("<FUNC_NAME>");`
```

## Execute from string 
use `Service._RunScript("python", script , "", "");` to execute `script` string with python.

# TODO
- Third Party Library Adaptation
- Python3.4+ support

# Licenses
[LICENSE](https://github.com/pcy190/AndroidPython/blob/master/LICENSE)
