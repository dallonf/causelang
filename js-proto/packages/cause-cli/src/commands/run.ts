import * as util from 'util';
import * as fs from 'fs';
import { Command, flags } from '@oclif/command';
import compileAndInvoke from 'causelang-proto-compiler/src/compileAndInvoke';
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

    await compileAndInvoke(
      {
        source,
        filename: args.file,
      },
      'main',
      [],
      {
        libraries: [library],
      }
    );
  }
}
