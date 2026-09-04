# Athena UI test session

- Plan: `C:\Users\jiang\Duke\ip\test\ui-test-plan.md`
- Started: 2026-09-04T20:41:35+08:00
- Status: **FAILED**

## Build

- Command: `gradlew.bat --console=plain shadowJar`
- Exit code: `0`

### Console output

````text
> Task :compileJava
> Task :processResources UP-TO-DATE
> Task :classes
> Task :shadowJar

BUILD SUCCESSFUL in 8s
3 actionable tasks: 2 executed, 1 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.6.1/userguide/configuration_cache_enabling.html

````

### Result: PASS

## Test case 1: Exit cleanly

- Aim: Verify that Athena starts with an empty isolated store and prints its farewell message for `bye`.
- Command: `java -jar "C:/Users/jiang/Duke/ip/build/libs/athena.jar"`
- Working directory: isolated temporary directory
- Exit code: `0` (expected `0`)

### Console input

````text
bye
````

### Actual console output

````text
Sept 04, 2026 8:41:44 PM com.sun.javafx.application.PlatformImpl startup
WARNING: Unsupported JavaFX configuration: classes were loaded from 'unnamed module @6d5e123c'
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by com.sun.glass.utils.NativeLibLoader in an unnamed module (file:/C:/Users/jiang/Duke/ip/build/libs/athena.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled

WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::allocateMemory has been called by com.sun.marlin.OffHeapArray (file:/C:/Users/jiang/Duke/ip/build/libs/athena.jar)
WARNING: Please consider reporting this to the maintainers of class com.sun.marlin.OffHeapArray
WARNING: sun.misc.Unsafe::allocateMemory will be removed in a future release

````

### Expected console output

````text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
____________________________________________________________
Farewell, Your Majesty. I hope to serve you again soon!
____________________________________________________________
````

### Result: FAIL

Failure reason: console output mismatch
