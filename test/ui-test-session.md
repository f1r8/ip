# Athena UI test session

- Plan: `C:\Users\jiang\Duke\ip\test\ui-test-plan.md`
- Started: 2026-08-29T09:16:26+08:00
- Status: **PASSED**

## Build

- Command: `gradlew.bat --console=plain shadowJar`
- Exit code: `0`

### Console output

````text
> Task :compileJava
> Task :processResources NO-SOURCE
> Task :classes
> Task :shadowJar UP-TO-DATE

BUILD SUCCESSFUL in 2s
2 actionable tasks: 1 executed, 1 up-to-date
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

### Result: PASS

## Test case 2: Manage a todo through its lifecycle

- Aim: Verify adding, listing, marking, unmarking, and deleting a todo in one stateful session.
- Command: `java -jar "C:/Users/jiang/Duke/ip/build/libs/athena.jar"`
- Working directory: isolated temporary directory
- Exit code: `0` (expected `0`)

### Console input

````text
todo Read the project brief
list
mark 1
unmark 1
delete 1
list
````

### Actual console output

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
As you command, Your Majesty. I've added this task:
  [T][ ] Read the project brief
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
1. [T][ ] Read the project brief
____________________________________________________________
____________________________________________________________
Excellent, Your Majesty! I've marked this task as done:
  [T][X] Read the project brief
____________________________________________________________
____________________________________________________________
Certainly, Your Majesty. I've marked this task as not done yet:
  [T][ ] Read the project brief
____________________________________________________________
____________________________________________________________
As you wish, Your Majesty. I've removed this task:
  [T][ ] Read the project brief
You now have 0 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
____________________________________________________________

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
As you command, Your Majesty. I've added this task:
  [T][ ] Read the project brief
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
1. [T][ ] Read the project brief
____________________________________________________________
____________________________________________________________
Excellent, Your Majesty! I've marked this task as done:
  [T][X] Read the project brief
____________________________________________________________
____________________________________________________________
Certainly, Your Majesty. I've marked this task as not done yet:
  [T][ ] Read the project brief
____________________________________________________________
____________________________________________________________
As you wish, Your Majesty. I've removed this task:
  [T][ ] Read the project brief
You now have 0 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
____________________________________________________________
````

### Result: PASS

## Test case 3: Add dated tasks

- Aim: Verify deadline and event commands parse dates and display their formatted times.
- Command: `java -jar "C:/Users/jiang/Duke/ip/build/libs/athena.jar"`
- Working directory: isolated temporary directory
- Exit code: `0` (expected `0`)

### Console input

````text
deadline Submit report /by 2026-12-31 2359
event Team meeting /from 2026-12-30 1400 /to 2026-12-30 1500
list
````

### Actual console output

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
As you command, Your Majesty. I've added this task:
  [D][ ] Submit report (by: Dec 31, 2026, 23:59)
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [E][ ] Team meeting (from: Dec 30, 2026, 14:00, to: Dec 30, 2026, 15:00)
You now have 2 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
1. [D][ ] Submit report (by: Dec 31, 2026, 23:59)
2. [E][ ] Team meeting (from: Dec 30, 2026, 14:00, to: Dec 30, 2026, 15:00)
____________________________________________________________

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
As you command, Your Majesty. I've added this task:
  [D][ ] Submit report (by: Dec 31, 2026, 23:59)
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [E][ ] Team meeting (from: Dec 30, 2026, 14:00, to: Dec 30, 2026, 15:00)
You now have 2 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
1. [D][ ] Submit report (by: Dec 31, 2026, 23:59)
2. [E][ ] Team meeting (from: Dec 30, 2026, 14:00, to: Dec 30, 2026, 15:00)
____________________________________________________________
````

### Result: PASS

## Test case 4: Reject incomplete commands

- Aim: Verify that missing descriptions, dates, and task indexes produce the intended guidance without terminating the session.
- Command: `java -jar "C:/Users/jiang/Duke/ip/build/libs/athena.jar"`
- Working directory: isolated temporary directory
- Exit code: `0` (expected `0`)

### Console input

````text
todo
deadline Submit report
event Team meeting /from 2026-12-30 1400
mark not-a-number
delete 1
````

### Actual console output

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
Please provide a todo description, Your Majesty.
____________________________________________________________
____________________________________________________________
Please provide a deadline and /by date, Your Majesty.
____________________________________________________________
____________________________________________________________
Please provide an event with /from and /to times, Your Majesty.
____________________________________________________________
____________________________________________________________
Which task shall I mark, Your Majesty?
____________________________________________________________
____________________________________________________________
Your Majesty, there aren't that many tasks in the list.
____________________________________________________________

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
Please provide a todo description, Your Majesty.
____________________________________________________________
____________________________________________________________
Please provide a deadline and /by date, Your Majesty.
____________________________________________________________
____________________________________________________________
Please provide an event with /from and /to times, Your Majesty.
____________________________________________________________
____________________________________________________________
Which task shall I mark, Your Majesty?
____________________________________________________________
____________________________________________________________
Your Majesty, there aren't that many tasks in the list.
____________________________________________________________
````

### Result: PASS

## Test case 5: Reject an unknown command

- Aim: Verify that an unrecognized command produces Athena's unknown-command response.
- Command: `java -jar "C:/Users/jiang/Duke/ip/build/libs/athena.jar"`
- Working directory: isolated temporary directory
- Exit code: `0` (expected `0`)

### Console input

````text
dance
````

### Actual console output

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
*Athena blinks her eyes, unsure of what you want, tilting 
her head slightly as the meaning of your words slips just 
out of reach.*
____________________________________________________________

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
*Athena blinks her eyes, unsure of what you want, tilting
her head slightly as the meaning of your words slips just
out of reach.*
____________________________________________________________
````

### Result: PASS

## Test case 6: Find matching tasks

- Aim: Verify that the find command prints only matching tasks and numbers the matches from one.
- Command: `java -jar "C:/Users/jiang/Duke/ip/build/libs/athena.jar"`
- Working directory: isolated temporary directory
- Exit code: `0` (expected `0`)

### Console input

````text
todo Read the project brief
todo Submit the final report
find report
````

### Actual console output

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
As you command, Your Majesty. I've added this task:
  [T][ ] Read the project brief
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [T][ ] Submit the final report
You now have 2 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the matching tasks in your list:
1. [T][ ] Submit the final report
____________________________________________________________

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
As you command, Your Majesty. I've added this task:
  [T][ ] Read the project brief
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [T][ ] Submit the final report
You now have 2 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the matching tasks in your list:
1. [T][ ] Submit the final report
____________________________________________________________
````

### Result: PASS
