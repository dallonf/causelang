import * as util from 'util';
import * as fs from 'fs';
import { Command, flags } from '@oclif/command';
import compileToJs from 'causelang-proto-compiler/src/compileToJs';
import * as runtime from 'causelang-proto-compiler/src/runtime';
import library, { ExitCodeSymbol, ExitCodeType } from '../library';

export default class Run extends Command {
  static description = 'Runs a .cau file as a command line application';

  static flags = {
    help: flags.help({ char: 'h' }),
  };

  static args = [{ name: 'file', required: true }];

  async run() {
    const { args } = this.parse(Run);

    const source = await util.promisify(fs.readFile)(args.file, 'utf-8');

    const jsSource = compileToJs(source, library);

    const runner = new runtime.CauseRuntime(jsSource, args.file, {
      library,
    });

    const result = await runner.invokeFn('main', []);
    if (result.type === ExitCodeSymbol) {
      const code = (result as ExitCodeType).value;
      // eslint-disable-next-line no-process-exit, unicorn/no-process-exit
      process.exit(code);
    } else {
      throw new Error(
        `I was expecting the main function to return an ExitCode; instead it returned this: ${util.inspect(
          result
        )}`
      );
    }
  }
}
