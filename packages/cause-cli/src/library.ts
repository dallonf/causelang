import cli from 'cli-ux';
import makeLibrary from 'causelang-proto-compiler/src/makeLibrary';

const library = makeLibrary({
  type: 'effect',
  name: 'Prompt',
  handler: () => {
    return cli.prompt('');
  },
});
export default library;
