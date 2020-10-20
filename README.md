# Cause

Cause is a proof-of-concept programming language. It's very much WIP right now, but the pitch is that it's a completely determinstic pure language that accomplishes I/O and other impure or non-deterministic actions by sending "Effect" messages up the stack, possibly all the way up to the runtime. It's inspired by [this blog post by Dan Abramov on algebraic effects](https://overreacted.io/algebraic-effects-for-the-rest-of-us/).

## Why would I want something like this?

A completely deterministic scripting language has a lot of really cool implications. It'd be pretty friendly to concurrency. You can write automated tests for anything; just handle I/O effects with mock responses before they make it back to the runtime. And say goodbye to flaky tests! You could save the state of a function mid-execution, persist it, and load it back later, just by recording and replaying effects and their responses. Maybe you do this to provide a 100% reproduction of a test that's flaky because you actually do want it to use actual I/O. Time-travel debugging is a pretty good bet. Hot reloading, depending on the use case, could be easy.

Effects themselves also make some interesting patterns possible. Imagine a turn-based game where other players can interrupt someone else's turn. Or how you might write a conversational bot!

## Is it a functional programming language?

I'm actually not sure. I'm not familiar enough with the academic principles of functional programming to say that it actually qualifies. I do, however, like a lot of the features of functional programming languages, and so I'll definitely have those features:

* Immutable-by-default data structures (in fact I don't have mutable data structures yet)
* Pure functions
* Function pipeline calls / extension methods
* Static typing with type inference
* Something like [Rust's enums](https://doc.rust-lang.org/book/ch06-01-defining-an-enum.html), which I believe are called "algebraic data types" in the FP world
* Pattern matching
* No classical or prototypical inheritance
* Complete determinism; that is, given the same input and the same responses to effects, every function will produce the same output and the same effects in the same order.
* And of course, something like algebraic effects

## What can it do so far?

Not much. Here's a sample program, though:

```
fn main() {
  cause Print("What is your name?")
  let name = cause Prompt()
  cause Print(append("Hello, ", name))
}
```

## What's up next?

Adding more to the language so it can do obvious things that every programming language should be able to do. Flow control, custom types, stuff like that. Unfortunately it's hard to explore Effects and their implications without those!

You can look at the `future-examples` directory for some ideas about where I think the language could go. It's mostly just sketches at this point, but I look to these - especially the Cheat game - as goals for the language to support in the future.

## Can I play with it?

There's not much to play with yet, but you can check out the tests in `packages/proto-compiler/tests` to see some examples of how the language actually works so far.

You can also run the CLI: `packages/cause-cli/bin/run run [filename]` (note the double "run" - if the CLI was installed locally, it would be `cause run`). Check out some of the examples in `packages/cause-cli/examples` that can be executed with that CLI.

To install dependencies, just run `npm install` and `npx lerna init` in the root directory.

## It compiles to JavaScript?

At the moment, it does. I consider that an implementation detail, and I'm strongly considering moving to a lighter runtime for the purpose of a language that can be embedded in many other runtimes!

## This is, without a doubt, the worst compiler code I have ever seen.

![I have no idea what I'm doing](https://media.giphy.com/media/xDQ3Oql1BN54c/giphy.gif)

You're probably right! I've never done anything like this before, and definitely didn't study up very much on how others have done it. That'll be a job for after I'm convinced the language is a good idea! Of course, if you're willing to help, I'll happily accept advice or PRs :)