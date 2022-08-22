# Cause

Cause is a proof-of-concept programming language. It's very much WIP right now, but the pitch is that it's a completely determinstic pure language that accomplishes I/O and other impure or non-deterministic actions by sending "Signals" up the stack, possibly all the way up to the runtime. It's inspired by [this blog post by Dan Abramov on algebraic effects](https://overreacted.io/algebraic-effects-for-the-rest-of-us/).

## Why would I want something like this?

A completely deterministic scripting language has a lot of really cool implications. It'd be pretty friendly to concurrency. You can write automated tests for anything; just handle I/O effects with mock responses before they make it back to the runtime. And say goodbye to flaky tests! You could save the state of a function mid-execution, persist it, and load it back later, just by recording and replaying caused signals and their responses. Maybe you do this to provide a 100% reproduction of a test that's flaky because you actually do want it to use actual I/O. Time-travel debugging is a pretty good bet. Hot reloading, depending on the use case, could be easy.

Effects themselves also make some interesting patterns possible. Imagine a turn-based game where other players can interrupt someone else's turn. Or how you might write a conversational bot!

## Is it a functional programming language?

I'm actually not sure. I'm not familiar enough with the academic principles of functional programming to say that it actually qualifies. I do, however, like a lot of the features of functional programming languages, and so I'll definitely have those features:

* Immutable-by-default data structures (in fact I'm unsure if I'll have truly mutable data structures)
* Pure functions
* Function pipeline calls / extension methods
* Static typing with type inference
* Something like [Rust's enums](https://doc.rust-lang.org/book/ch06-01-defining-an-enum.html), which I believe are called "algebraic data types" in the FP world
* Pattern matching
* No classical or prototypical inheritance
* Complete determinism; that is, given the same input and the same responses to effects, every function will produce the same output and the same effects in the same order.
* And of course, something like algebraic effects

## What can it do so far?

Not much. Here's a sample program that works in the current interpreter, though:

```
import test/io (Prompt)

fn main() {
  cause Debug("What is your name?")
  let name = cause Prompt()
  cause Debug(append("Hello, ", name))
}
```

## What's up next?

Building up the language enough that I can use it for [Advent of Code](https://adventofcode.com/) this year. AoC isn't a great fit for what I imagine Cause being used for, especially this early unoptimized version of it, but it's a good stress test to see what patterns might need more work.

You can look at the `future-examples` directory for some ideas about where I think the language could go. It's mostly just sketches at this point, but I look to these - especially the Cheat game - as goals for the language to support in the future.

## Can I play with it?

There's not much to play with yet, but you can check out the current Kotlin interpreter's tests in `ktcause/src/test/kotlin` to see some examples of how the language actually works so far.

To get the repo running, you'll need to load up the `ktcause` folder in IntelliJ, or in Gradle. You'll also need to use ANTLR 4 to generate a parser from `ktcause/src/main/resources/Cause.g4` in `ktcause/src/main/gen`.

## This is, without a doubt, the worst intepreter code I have ever seen.

![I have no idea what I'm doing](https://media.giphy.com/media/xDQ3Oql1BN54c/giphy.gif)

You're probably right! I've never done anything like this before, and basically the only research I did what Robert Nystrom's _Crafting Interpreters_ (http://craftinginterpreters.com/). Of course, if you're willing to help, I'll happily accept advice or PRs :)