import * as util from 'util';
import * as fs from 'fs';
import { Command, flags } from '@oclif/command';
import compileToJs from 'causelang-proto-compiler/src/compileToJs';
import * as runtime from 'causelang-proto-compiler/src/runtime';
import library from '../library';

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

    await runner.invokeFn('main', []);
  }
}
