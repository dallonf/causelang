cause-cli
=========

Prototype compiler and runner for the Cause language

[![oclif](https://img.shields.io/badge/cli-oclif-brightgreen.svg)](https://oclif.io)
[![Version](https://img.shields.io/npm/v/cause-cli.svg)](https://npmjs.org/package/cause-cli)
[![Downloads/week](https://img.shields.io/npm/dw/cause-cli.svg)](https://npmjs.org/package/cause-cli)
[![License](https://img.shields.io/npm/l/cause-cli.svg)](https://github.com/dallonf/causelang/blob/master/package.json)

<!-- toc -->
* [Usage](#usage)
* [Commands](#commands)
<!-- tocstop -->
# Usage
<!-- usage -->
```sh-session
$ npm install -g cause-cli
$ cause COMMAND
running command...
$ cause (-v|--version|version)
cause-cli/0.0.0 linux-x64 node-v14.5.0
$ cause --help [COMMAND]
USAGE
  $ cause COMMAND
...
```
<!-- usagestop -->
# Commands
<!-- commands -->
* [`cause hello [FILE]`](#cause-hello-file)
* [`cause help [COMMAND]`](#cause-help-command)
* [`cause run [FILE]`](#cause-run-file)

## `cause hello [FILE]`

describe the command here

```
USAGE
  $ cause hello [FILE]

OPTIONS
  -f, --force
  -h, --help       show CLI help
  -n, --name=name  name to print

EXAMPLE
  $ cause hello
  hello world from ./src/hello.ts!
```

_See code: [src/commands/hello.ts](https://github.com/dallonf/causelang/blob/v0.0.0/src/commands/hello.ts)_

## `cause help [COMMAND]`

display help for cause

```
USAGE
  $ cause help [COMMAND]

ARGUMENTS
  COMMAND  command to show help for

OPTIONS
  --all  see all commands in CLI
```

_See code: [@oclif/plugin-help](https://github.com/oclif/plugin-help/blob/v3.2.0/src/commands/help.ts)_

## `cause run [FILE]`

describe the command here

```
USAGE
  $ cause run [FILE]

OPTIONS
  -f, --force
  -h, --help       show CLI help
  -n, --name=name  name to print
```

_See code: [src/commands/run.ts](https://github.com/dallonf/causelang/blob/v0.0.0/src/commands/run.ts)_
<!-- commandsstop -->
