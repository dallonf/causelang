# Cause

Cause is a proof-of-concept programming language. It's very much WIP right now, but the pitch is that it's a completely determinstic strongly-typed scripting language that accomplishes I/O and other effects or non-deterministic actions by sending "Signals" up the stack, possibly all the way up to the runtime. It's inspired by [this blog post by Dan Abramov on algebraic effects](https://overreacted.io/algebraic-effects-for-the-rest-of-us/).

## Why would I want something like this?

A completely deterministic scripting language has a lot of really cool implications. It'd be pretty friendly to concurrency. You can write automated tests for anything; just handle I/O effects with mock responses before they make it back to the runtime. And say goodbye to flaky tests! You could save the state of a function mid-execution, persist it, and load it back later, just by recording and replaying caused signals and their responses. Maybe you do this to provide a 100% reproduction of a test that's flaky because you actually do want it to use actual I/O. Time-travel debugging is a pretty good bet. Hot reloading, depending on the use case, could be easy.

Effects themselves also make some interesting patterns possible. Imagine a turn-based game where other players can interrupt someone else's turn. Or how you might write a conversational bot!

## Is it a functional programming language?

Not really. I call it a "deterministic procedural" language. It does share a lot of values with functional programming languages, namely its emphasis on immutable data and pushing effects away from core logic. However, unlike a functional programming language, it doesn't steer you towards function composition as the solution to every problem, and so I hope it will avoid the mental gymnastics required to understand highly abstract functional code.

## What does it look like?

Here's a code example of how you might solve [Advent of Code 2019's Day 2](https://adventofcode.com/2019/day/2) Part One, which involves a simple bytecode ("Intcode") interpreter:

```
import core/math (add, multiply)
import core/stopgap/collections (List, append, with_item_at_index, at_index)
import aoc/input (NeedInput, Split, ParseNumber)
import project/common/collections (for_each)
import project/common/cast (as_number, as_text)

function part_one() {
    let program = parse_program(cause NeedInput("day02/puzzleinput.txt"))
    let program = with_item_at_index(program, 1, 12)
    let program = with_item_at_index(program, 2, 2)
    let after_memory = run_vm(program)
    at_index(after_memory, 0)
}

function parse_program(text: Text): List {
  let items = cause Split(text, ",")
  let variable program = List()
  signal AddItem(value: Number): Action
  effect for AddItem as add_item {
    set program = program>>append(add_item.value)
  }
  for_each(items, fn(it: Anything) {
    let number = cause ParseNumber(as_text(it))
    cause AddItem(number)
  })
  program
}

signal ReadMemory(index: Number): Number
signal WriteMemory(index: Number, value: Number): Action
signal Terminate: NeverContinues

function run_vm(program: List): List {
  let variable memory = program
  let variable instruction_pointer = 0

  effect for Terminate {
    return memory
  }
  
  effect for ReadMemory as it {
    as_number(at_index(memory, it.index))
  }

  effect for WriteMemory as write_memory {
    set memory = with_item_at_index(memory, write_memory.index, write_memory.value)
  }

  loop {
    set instruction_pointer = execute(instruction_pointer)
  }
}

function execute(instruction_pointer: Number): Number {
  let instruction = cause ReadMemory(instruction_pointer)
  branch {
    if equals(instruction, 1) {
      let position_1 = cause ReadMemory(add(instruction_pointer, 1))
      let position_2 = cause ReadMemory(add(instruction_pointer, 2))
      let position_3 = cause ReadMemory(add(instruction_pointer, 3))

      let value_1 = cause ReadMemory(position_1)
      let value_2 = cause ReadMemory(position_2)
      let result = add(value_1, value_2)

      cause WriteMemory(position_3, result)
      add(instruction_pointer, 4)
    }
    if equals(instruction, 2) {
      let position_1 = cause ReadMemory(add(instruction_pointer, 1))
      let position_2 = cause ReadMemory(add(instruction_pointer, 2))
      let position_3 = cause ReadMemory(add(instruction_pointer, 3))

      let value_1 = cause ReadMemory(position_1)
      let value_2 = cause ReadMemory(position_2)
      let result = multiply(value_1, value_2)
      
      cause WriteMemory(position_3, result)
      add(instruction_pointer, 4)
    }
    if equals(instruction, 99) => cause Terminate
    else => cause AssumptionBroken("unexpected instruction")
  }
}
```

## Can I play with it?

I don't yet have an easy-to-use environment for it, but you can check out a little bit of example code I wrote for a couple of puzzles in Advent of Code 2019 (in 2022, long story): https://github.com/dallonf/advent-of-code-2019-cau

You can also check out the current Kotlin interpreter's tests in `ktcause/src/test/kotlin`.

To get the repo running, you'll need to load up the `ktcause` folder in IntelliJ, or in Gradle. You'll also need to use ANTLR 4 to generate a parser from `ktcause/src/main/resources/Cause.g4` in `ktcause/src/main/gen`.

## What's up next?

Building some dev tools, namely a VS Code plugin and language server. Statically typed languages don't really shine when you're editing them as plain text!

As far as language features, next up are generics in the type system as well as Ruby-style blocks for many functions that currently take a function parameter, because this language in particular benefits from distinguishing between a callback that might be called later and a code parameter that will be called as part of the function itself.

You can look at the `future-examples` directory for some ideas about where I think the language could go. It's mostly just sketches at this point, but I look to these - especially the Cheat game - as goals for the language to support in the future.

## This is, without a doubt, the worst intepreter code I have ever seen.

![I have no idea what I'm doing](https://media.giphy.com/media/xDQ3Oql1BN54c/giphy.gif)

You're probably right! I've never done anything like this before, and basically the only research I did what Robert Nystrom's _Crafting Interpreters_ (http://craftinginterpreters.com/). Of course, if you're willing to help, I'll happily accept advice or PRs :)