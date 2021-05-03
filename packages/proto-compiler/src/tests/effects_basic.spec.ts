import {
  coreOperationsLibrary,
  ACTION_ID,
  STRING_ID,
  INTEGER_ID,
} from '../coreLibrary';
import makeLibrary, { idFromLibrary } from '../runtimeLibrary';
import { runMainSync } from './testRunner';

it('Can intercept an effect', () => {
  const script = `
    fn main() {
      {
        cause Print("Howdy world!")
      } handle => {
        cause Print("Intercepted a Print effect")
      }
    }
  `;

  const { result, output } = runMainSync(script);
  expect(result).toBe(undefined);
  expect(output).toEqual(['Intercepted a Print effect']);
});

it('Can identify the type of effect', () => {
  const library = makeLibrary('test', {
    type: 'effect',
    name: 'InterceptThis',
    params: {},
    returnType: {
      kind: 'valueTypeReference',
      id: ACTION_ID,
    },
    handler: () => {
      throw new Error('This effect should have been intercepted!');
    },
  });

  const script = `
    fn main() {
      {
        cause InterceptThis()
        cause Print("Howdy world!")
      } handle InterceptThis() => {
        cause Print("Intercepted an InterceptThis effect")
      }
    }
  `;

  const { result, output } = runMainSync(script, {
    libraries: [library],
  });

  expect(result).toBe(undefined);
  expect(output).toEqual([
    'Intercepted an InterceptThis effect',
    'Howdy world!',
  ]);
});

it('Provides access to the captured effects and its values', () => {
  const library = makeLibrary('test', {
    type: 'effect',
    name: 'Greet',
    params: {
      name: {
        kind: 'valueTypeReference',
        id: STRING_ID,
      },
    },
    returnType: {
      kind: 'valueTypeReference',
      id: ACTION_ID,
    },
    handler: () => {
      throw new Error('This effect should have been intercepted!');
    },
  });

  const script = `
    fn main() {
      {
        cause Print("Don't handle this one")
        cause Greet("partner")
      } handle let e: Greet => {
        cause Print(append("Howdy, ", e.value))
      }
    }
  `;

  const { result, output } = runMainSync(script, {
    libraries: [library],
  });

  expect(result).toBe(undefined);
  expect(output).toEqual(["Don't handle this one", 'Howdy, partner']);
});

it('Provides access to the captured effect without a type', () => {
  const Print = idFromLibrary('Print', coreOperationsLibrary);

  const extract = jest.fn();
  const library = makeLibrary('test', {
    type: 'effect',
    name: 'Extract',
    params: {
      value: {
        kind: 'valueTypeReference',
        id: Print,
      },
    },
    returnType: {
      kind: 'valueTypeReference',
      id: ACTION_ID,
    },
    handler: (e) => {
      extract(e);
    },
  });

  const script = `
    fn main() {
      {
        cause Print("this should be extracted and not printed")
      } handle let e => {
        cause Extract(e)
      }
    }
  `;

  const { result, output } = runMainSync(script, {
    libraries: [library],
  });

  expect(result).toBe(undefined);
  expect(output).toEqual([]);
  expect(extract).toHaveBeenCalledTimes(1);
  expect(extract).toHaveBeenCalledWith({
    type: idFromLibrary('Extract', library),
    value: {
      type: Print,
      value: 'this should be extracted and not printed',
    },
  });
});

it('Can use effects to mutate from a closure', () => {
  const library = makeLibrary('test', {
    type: 'effect',
    name: 'SetX',
    params: {
      value: {
        kind: 'valueTypeReference',
        id: INTEGER_ID,
      },
    },
    returnType: {
      kind: 'valueTypeReference',
      id: ACTION_ID,
    },
    handler: () => {
      throw new Error('Should be handled');
    },
  });

  const script = `
    fn main() {
      let var x = 1
      
      let update = fn() {
        cause SetX(2)
      }

      {
        update()
      } handle let e: SetX => {
        x = e.value
      }

      x
    }
  `;

  const { result, output } = runMainSync(script, {
    libraries: [library],
  });
  expect(output).toEqual([]);
  expect(result).toEqual(2);
});

it('Can define your own effects', () => {
  const script = `
    effect Greet(value: String): String

    fn main() {
      {
        let greeting = cause Greet("partner")
        cause Print(greeting)
      } handle let e: Greet => {
        append("Howdy, ", e.value)
      }
    }
  `;

  const { result, output } = runMainSync(script);

  expect(result).toBe(undefined);
  expect(output).toEqual(['Howdy, partner']);
});
