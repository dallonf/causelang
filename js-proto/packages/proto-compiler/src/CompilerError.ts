import { SourceStream } from './sourceStream';

class CompilerError extends Error {
  constructor(message: string, public cursor: SourceStream) {
    super(message);
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export default CompilerError;
