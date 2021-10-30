import * as ast from './ast';
import { nextChar, SourceStream } from './sourceStream';
import {
  advanceLine,
  expectCursor,
  consumeSequence,
  expectWhitespace,
  readIdentifier,
  skipWhitespace,
  assertCursor,
} from './readToken';
import CompilerError from './CompilerError';

export interface Context {}

export const parseModule = (cursor: SourceStream, ctx: Context): ast.Module => {
  const root: ast.Module = {
    type: 'Module',
    body: [],
  };

  let current;
  while (
    ((cursor = skipWhitespace(cursor)), (current = nextChar(cursor)), current)
  ) {
    const declaration = parseDeclaration(cursor, ctx);
    if (declaration) {
      cursor = declaration.cursor;
      root.body.push(declaration.result);
      cursor = advanceLine(cursor);
    } else {
      throw new CompilerError(
        'I\'m looking for declarations here in the root of the module; stuff like "let", "fn", etc.',
        cursor
      );
    }
  }

  return root;
};

const parseIdentifier = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.Identifier; cursor: SourceStream } => {
  const result = readIdentifier(cursor);
  if (result) {
    return {
      cursor: result.cursor,
      result: {
        type: 'Identifier',
        name: result.identifier,
      },
    };
  } else {
    return null;
  }
};

const parseDeclaration = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.Declaration; cursor: SourceStream } => {
  const keyword = readIdentifier(cursor);
  if (keyword) {
    if (keyword.identifier === 'fn') {
      return parseFunctionDeclaration(keyword.cursor, ctx);
    } else if (keyword.identifier === 'effect') {
      return parseEffectDeclaration(keyword.cursor, ctx);
    } else if (keyword.identifier === 'type') {
      return parseTypeDeclaration(keyword.cursor, ctx);
    }
  }
  return null;
};

const parseFunctionDeclaration = (
  cursor: SourceStream,
  ctx: Context
): { result: ast.FunctionDeclaration; cursor: SourceStream } => {
  const idRead = readIdentifier(cursor);
  if (!idRead) {
    throw new CompilerError(
      'I\'m looking for a function name after the "fn" declaration, but I can\'t find one',
      cursor
    );
  }
  cursor = idRead.cursor;

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, '('),
    'The next part of a function declaration should be a "(" to list the parameters.'
  );

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, ')'),
    'The next part of a function declaration should be a ")" to close out the parameter list.'
  );

  let tmp = consumeSequence(cursor, ':');
  let returnType;
  if (tmp) {
    cursor = tmp;
    returnType = parseTypeReference(cursor, ctx);
    assertCursor(
      cursor,
      returnType?.cursor,
      'Expected a type reference after ":"'
    );
    cursor = returnType.cursor;
  }

  const body = parseExpression(cursor, ctx);
  if (!body) {
    throw new CompilerError(
      `Your function body should be an expression, like a block: {}`,
      cursor
    );
  }
  cursor = body.cursor;

  const result: ast.FunctionDeclaration = {
    type: 'FunctionDeclaration',
    id: {
      type: 'Identifier',
      name: idRead.identifier,
    },
    parameters: [],
    returnType: returnType?.result,
    body: body.result,
  };

  return { result, cursor };
};

const parseEffectDeclaration = (
  cursor: SourceStream,
  ctx: Context
): { result: ast.EffectDeclaration; cursor: SourceStream } => {
  const idRead = readIdentifier(cursor);
  if (!idRead) {
    throw new CompilerError(
      'I\'m looking for an effect name after the "effect" declaration, but I can\'t find one',
      cursor
    );
  }
  cursor = idRead.cursor;

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, '('),
    'The next part of an effect declaration should be a "(" to list the parameters.'
  );

  let parameters: ast.ParameterDescriptor[] = [];
  let parameterName;
  while (
    ((cursor = skipWhitespace(cursor)),
    (parameterName = parseIdentifier(cursor, ctx)),
    parameterName)
  ) {
    cursor = skipWhitespace(parameterName.cursor);

    cursor = expectCursor(
      cursor,
      consumeSequence(cursor, ':'),
      'I\'m looking for a ":" to set the type of this parameter'
    );

    cursor = skipWhitespace(cursor);

    const typeId = parseIdentifier(cursor, ctx);
    assertCursor(cursor, typeId?.cursor, "I'm looking for a type name");
    cursor = typeId.cursor;

    parameters.push({
      type: 'ParameterDescriptor',
      name: parameterName.result,
      valueType: typeId.result,
    });

    const comma = consumeSequence(cursor, ',');
    if (!comma) {
      break;
    } else {
      cursor = comma;
    }
  }

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, ')'),
    'The next part of an effect declaration should be a ")" to close out the parameter list.'
  );

  cursor = skipWhitespace(cursor, { stopAtNewline: true });

  let returnType;
  const returnTypeColon = consumeSequence(cursor, ':');
  if (returnTypeColon) {
    cursor = returnTypeColon;
    cursor = skipWhitespace(cursor);

    const typeId = parseIdentifier(cursor, ctx);
    assertCursor(cursor, typeId?.cursor, "I'm looking for a type name");
    cursor = typeId.cursor;

    returnType = typeId.result;
  }

  const result: ast.EffectDeclaration = {
    type: 'EffectDeclaration',
    id: {
      type: 'Identifier',
      name: idRead.identifier,
    },
    parameters,
    returnType,
  };

  return { result, cursor };
};

function parseTypeDeclaration(
  cursor: SourceStream,
  ctx: Context
): { result: ast.TypeDeclaration; cursor: SourceStream } {
  const idRead = readIdentifier(cursor);
  if (!idRead) {
    throw new CompilerError(
      'I\'m looking for a type name after the "type" declaration, but I can\'t find one',
      cursor
    );
  }
  cursor = idRead.cursor;

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, '('),
    'The next part of a type declaration should be a "(" to list the fields.'
  );

  let fields: ast.FieldDescriptor[] = [];
  let fieldName;
  while (
    ((cursor = skipWhitespace(cursor)),
    (fieldName = parseIdentifier(cursor, ctx)),
    fieldName)
  ) {
    cursor = skipWhitespace(fieldName.cursor);

    cursor = expectCursor(
      cursor,
      consumeSequence(cursor, ':'),
      'I\'m looking for a ":" to set the type of this field'
    );

    cursor = skipWhitespace(cursor);

    const typeId = parseIdentifier(cursor, ctx);
    assertCursor(cursor, typeId?.cursor, "I'm looking for a type name");
    cursor = typeId.cursor;

    fields.push({
      type: 'FieldDescriptor',
      name: fieldName.result,
      valueType: typeId.result,
    });

    const comma = consumeSequence(cursor, ',');
    if (!comma) {
      break;
    } else {
      cursor = comma;
    }
  }

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, ')'),
    'The next part of a type declaration should be a ")" to close out the field list.'
  );

  const result: ast.TypeDeclaration = {
    type: 'TypeDeclaration',
    id: {
      type: 'Identifier',
      name: idRead.identifier,
    },
    fields,
  };

  return { result, cursor };
}

const parseExpression = (
  cursor: SourceStream,
  ctx: Context,
  opts?: { allowHandlers?: boolean }
): null | { result: ast.Expression; cursor: SourceStream } => {
  cursor = skipWhitespace(cursor);
  const char = nextChar(cursor);
  if (!char) return null;

  if (char.char === '{') {
    return parseBlockExpression(cursor, ctx, {
      allowHandlers: opts?.allowHandlers,
    });
  } else {
    let initialExpression;
    let readAttempt;
    if (
      ((readAttempt = parsePrefixOperatorExpression(cursor, ctx)), readAttempt)
    ) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (
      ((readAttempt = parseFunctionExpression(cursor, ctx)), readAttempt)
    ) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (
      ((readAttempt = parseBranchExpression(cursor, ctx)), readAttempt)
    ) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (((readAttempt = parseStringLiteral(cursor, ctx)), readAttempt)) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (((readAttempt = parseIntLiteral(cursor, ctx)), readAttempt)) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (
      ((readAttempt = readIdentifier(cursor)),
      readAttempt &&
        !ast.keywordSet.has(readAttempt.identifier as ast.KeywordValue))
    ) {
      initialExpression = {
        type: 'Identifier' as const,
        name: readAttempt!.identifier,
      };
      cursor = readAttempt!.cursor;
    } else {
      return null;
    }

    return transformExpressionWithSuffixes(initialExpression, cursor, ctx);
  }
};

const transformExpressionWithSuffixes = (
  initialExpression: ast.Expression,
  cursor: SourceStream,
  ctx: Context
): { cursor: SourceStream; result: ast.Expression } => {
  let suffixStart;
  if (((suffixStart = consumeSequence(cursor, '(')), suffixStart)) {
    return parseCallExpression(initialExpression, cursor, ctx);
  } else if (((suffixStart = consumeSequence(cursor, '.')), suffixStart)) {
    return parseMemberExpression(initialExpression, cursor, ctx);
  }

  return { result: initialExpression, cursor: cursor };
};

const parseStringLiteral = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.StringLiteral; cursor: SourceStream } => {
  const openingQuote = nextChar(cursor);
  if (!openingQuote || openingQuote.char !== '"') return null;
  cursor = openingQuote.cursor;

  const stringChars = [];
  while (true) {
    const char = nextChar(cursor);
    if (!char) {
      throw new CompilerError(
        'This string literal never ended; are you missing an end quote (") somewhere?',
        cursor
      );
    }
    cursor = char.cursor;

    // TODO: escaping with \"
    if (char.char === '"') break;

    stringChars.push(char.char);
  }

  return {
    cursor,
    result: {
      type: 'StringLiteral',
      value: stringChars.join(''),
    },
  };
};

const parseIntLiteral = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.IntegerLiteral; cursor: SourceStream } => {
  // TODO: negative numbers with minus signs

  const intChars = [];
  let char;
  while (((char = nextChar(cursor)), char && char.char.match(/[0-9]/))) {
    intChars.push(char!.char);
    cursor = char!.cursor;
  }
  if (intChars.length === 0) {
    return null;
  }

  const int = parseInt(intChars.join(''), 10);
  return {
    cursor,
    result: {
      type: 'IntLiteral',
      value: int,
    },
  };
};

const prefixOperators = new Set(['cause'] as const);
type PrefixSupportedOperator = typeof prefixOperators extends Set<infer T>
  ? T
  : never;
const parsePrefixOperatorExpression = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.PrefixOperatorExpression; cursor: SourceStream } => {
  const keyword = readIdentifier(cursor);
  if (
    keyword &&
    prefixOperators.has(keyword.identifier as PrefixSupportedOperator)
  ) {
    cursor = keyword.cursor;
    let tmp;
    if (((tmp = consumeSequence(cursor, ' ')), !tmp)) {
      return null;
    }
    cursor = tmp;
    skipWhitespace(cursor);

    let expression = parseExpression(cursor, ctx);
    if (expression) {
      return {
        result: {
          type: 'PrefixOperatorExpression',
          operator: {
            type: 'Keyword',
            keyword: keyword.identifier as PrefixSupportedOperator,
          },
          expression: expression.result,
        },
        cursor: expression.cursor,
      };
    }
  }
  return null;
};

const parseCallExpression = (
  callee: ast.Expression,
  cursor: SourceStream,
  ctx: Context
): {
  result: ast.CallExpression;
  cursor: SourceStream;
} => {
  const openBrace = nextChar(cursor);
  if (!openBrace || openBrace.char !== '(') {
    throw new CompilerError(
      "I'm confused; I'm looking for a function call, but I don't even see a starting \"(\". This probably isn't your fault!",
      cursor
    );
  }
  cursor = openBrace.cursor;

  const args: ast.Expression[] = [];

  let parameter;
  while (
    ((cursor = skipWhitespace(cursor)),
    (parameter = parseExpression(cursor, ctx)),
    parameter)
  ) {
    args.push(parameter.result);
    cursor = skipWhitespace(parameter.cursor);

    const comma = nextChar(cursor);
    if (!comma || comma.char !== ',') {
      break;
    } else {
      cursor = comma.cursor;
    }
  }

  const closeBrace = nextChar(cursor);
  if (!closeBrace || closeBrace.char !== ')') {
    throw new CompilerError(
      'I\'m looking for a ")" to close the parameter list',
      cursor
    );
  }
  cursor = closeBrace.cursor;

  return {
    result: {
      type: 'CallExpression',
      callee: callee,
      parameters: args,
    },
    cursor,
  };
};

const parseMemberExpression = (
  object: ast.Expression,
  cursor: SourceStream,
  ctx: Context
): { result: ast.MemberExpression; cursor: SourceStream } => {
  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, '.'),
    "I'm confused; I'm looking for a property access, but I don't even see a \".\". This probably isn't your fault!"
  );

  const property = parseIdentifier(cursor, ctx);
  assertCursor(
    cursor,
    property?.cursor,
    "I'm looking for the name of a property of this object."
  );
  cursor = property.cursor;

  return {
    cursor: cursor,
    result: {
      type: 'MemberExpression',
      object,
      property: property.result,
    },
  };
};

const parseBlockExpression = (
  cursor: SourceStream,
  ctx: Context,
  opts?: { allowHandlers?: boolean }
): { result: ast.BlockExpression; cursor: SourceStream } => {
  cursor = skipWhitespace(cursor);
  const openBrace = nextChar(cursor);
  if (!openBrace || openBrace.char !== '{') {
    throw new CompilerError(
      "I'm confused; I'm looking for a block expression, but I don't even see a starting \"{\". This probably isn't your fault!",
      cursor
    );
  }
  cursor = openBrace.cursor;

  const body: ast.Statement[] = [];

  let current;
  while (
    ((cursor = skipWhitespace(cursor)), (current = nextChar(cursor)), current)
  ) {
    if (current.char === '}') {
      cursor = current.cursor;
      break;
    }

    const statement = parseStatement(cursor, ctx);
    if (statement) {
      body.push(statement.result);
      cursor = advanceLine(statement.cursor);
    } else {
      throw new CompilerError(
        "I'm looking for statements in this block.",
        cursor
      );
    }
  }
  if (!current) {
    throw new CompilerError(
      'I\'m looking for a "}" to close this block',
      cursor
    );
  }

  cursor = skipWhitespace(cursor, { stopAtNewline: true });

  const allowHandlers = opts?.allowHandlers ?? true;
  const handlers: ast.HandlerBlockSuffix[] = [];
  if (allowHandlers) {
    let handleSuffixKeywordCursor;
    while ((handleSuffixKeywordCursor = consumeSequence(cursor, 'handle'))) {
      cursor = handleSuffixKeywordCursor;
      cursor = skipWhitespace(cursor, { stopAtNewline: true });

      const pattern = parsePattern(cursor, ctx);
      if (pattern) {
        cursor = pattern.cursor;
      }

      cursor = skipWhitespace(cursor, { stopAtNewline: true });
      cursor = expectCursor(
        cursor,
        consumeSequence(cursor, '=>'),
        pattern
          ? 'I\'m looking for an arrow ("=>") to seperate the handler match pattern from the handler itself.'
          : 'I\'m looking for a handler match pattern or an arrow ("=>")'
      );
      cursor = skipWhitespace(cursor);

      const bodyExpression = parseExpression(cursor, ctx, {
        allowHandlers: false,
      });
      assertCursor(
        cursor,
        bodyExpression?.cursor,
        "I'm looking for an expression to handle the effect."
      );
      cursor = bodyExpression.cursor;
      handlers.push({
        type: 'HandlerBlockSuffix',
        body: bodyExpression.result,
        pattern: pattern?.result,
      });
    }
  }

  return {
    result: {
      type: 'BlockExpression',
      body,
      handlers: handlers.length ? handlers : undefined,
    },
    cursor,
  };
};

const parseStatement = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.Statement; cursor: SourceStream } => {
  const nameDeclaration = parseNameDeclarationStatement(cursor, ctx);
  if (nameDeclaration) {
    return nameDeclaration;
  }

  let parseAttempt;

  if ((parseAttempt = parseAssignmentStatement(cursor, ctx))) {
    return parseAttempt;
  }
  if (((parseAttempt = parseExpression(cursor, ctx)), parseAttempt)) {
    return {
      result: {
        type: 'ExpressionStatement',
        expression: parseAttempt.result,
      },
      cursor: parseAttempt.cursor,
    };
  }

  return null;
};

const parseNameDeclarationStatement = (
  cursor: SourceStream,
  ctx: Context
):
  | undefined
  | { result: ast.NameDeclarationStatement; cursor: SourceStream } => {
  let tmp;
  tmp = consumeSequence(cursor, 'let');
  if (!tmp) return;
  cursor = tmp;
  tmp = expectWhitespace(cursor);
  if (!tmp) return;
  cursor = tmp;

  const variable = consumeSequence(cursor, 'var');
  if (variable) {
    cursor = variable;
    cursor = expectCursor(
      cursor,
      expectWhitespace(cursor),
      'I was expecting to see whitespace after "var"'
    );
  }

  const name = readIdentifier(cursor);
  if (!name)
    throw new CompilerError('I was expected to see a name after "let"', cursor);
  cursor = name.cursor;

  cursor = skipWhitespace(cursor);

  tmp = consumeSequence(cursor, ':');
  let typeAnnotation;
  if (tmp) {
    cursor = skipWhitespace(tmp);
    typeAnnotation = parseTypeReference(cursor, ctx);
    if (!typeAnnotation) {
      throw new CompilerError(
        'I was expecting to find a type after ":"',
        cursor
      );
    }
    cursor = skipWhitespace(typeAnnotation.cursor);
  }

  tmp = consumeSequence(cursor, '=');
  if (!tmp) throw new CompilerError('I was expecting to see an "="', cursor);
  cursor = tmp;

  cursor = skipWhitespace(cursor);
  const expression = parseExpression(cursor, ctx);
  if (!expression)
    throw new CompilerError(
      `I was expecting an expression after "let ${name.identifier} ="`,
      cursor
    );
  cursor = expression.cursor;

  return {
    result: {
      type: 'NameDeclarationStatement',
      name: { type: 'Identifier', name: name.identifier },
      typeAnnotation: typeAnnotation?.result,
      value: expression.result,
      variable: Boolean(variable),
    },
    cursor,
  };
};

const parseAssignmentStatement = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.AssignmentStatement; cursor: SourceStream } => {
  const name = parseIdentifier(cursor, ctx);
  if (!name) return null;
  cursor = name.cursor;

  cursor = skipWhitespace(cursor, { stopAtNewline: true });
  const equals = consumeSequence(cursor, '=');
  if (!equals) return null;
  cursor = equals;
  cursor = skipWhitespace(cursor);

  const expression = parseExpression(cursor, ctx);
  assertCursor(
    cursor,
    expression?.cursor,
    `I'm looking for an expression to assign to ${name.result.name}`
  );
  cursor = expression.cursor;

  return {
    cursor,
    result: {
      type: 'AssignmentStatement',
      name: name.result,
      value: expression.result,
    },
  };
};

const parsePattern = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.Pattern; cursor: SourceStream } => {
  let tmp;

  tmp = parseTypePattern(cursor, ctx);
  if (tmp) return tmp;

  tmp = parseNamePattern(cursor, ctx);
  if (tmp) return tmp;

  return null;
};

const parseTypePattern = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.TypePattern; cursor: SourceStream } => {
  const typeName = readIdentifier(cursor);
  if (!typeName) return null;
  cursor = typeName.cursor;

  const openBrace = consumeSequence(cursor, '(');
  if (!openBrace) return null;
  cursor = openBrace;

  const closeBrace = consumeSequence(cursor, ')');
  if (!closeBrace) return null;
  cursor = closeBrace;

  return {
    result: {
      type: 'TypePattern',
      typeName: {
        type: 'Identifier',
        name: typeName.identifier,
      },
    },
    cursor,
  };
};

const parseNamePattern = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.NamePattern; cursor: SourceStream } => {
  let tmp;

  tmp = consumeSequence(cursor, 'let');
  if (!tmp) return null;
  cursor = tmp;

  cursor = skipWhitespace(cursor);

  const name = readIdentifier(cursor);
  if (!name) {
    throw new CompilerError("I'm looking for an identifier.", cursor);
  }
  cursor = name.cursor;

  let valueType;
  const typeColon = consumeSequence(cursor, ':');
  if (typeColon) {
    cursor = typeColon;
    cursor = skipWhitespace(cursor);
    if (
      ((valueType = parseTypePattern(cursor, ctx)) ||
        (valueType = parseIdentifier(cursor, ctx)),
      valueType)
    ) {
      cursor = valueType.cursor;
    } else {
      throw new CompilerError(
        "I'm looking for a type name or a type pattern.",
        cursor
      );
    }
  }

  return {
    cursor,
    result: {
      type: 'NamePattern',
      name: { type: 'Identifier', name: name.identifier },
      valueType: valueType?.result,
    },
  };
};

const parseFunctionExpression = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.FunctionExpression; cursor: SourceStream } => {
  let tmp;
  tmp = consumeSequence(cursor, 'fn');
  if (!tmp) return null;
  cursor = tmp;

  cursor = skipWhitespace(cursor);

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, '('),
    'The next part of a function expression should be a "(" to list the parameters.'
  );

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, ')'),
    'The next part of a function expression should be a ")" to close out the parameter list.'
  );

  const body = parseExpression(cursor, ctx);
  if (!body) {
    throw new CompilerError(
      `Your function body should be an expression, like a block: {}`,
      cursor
    );
  }
  cursor = body.cursor;

  const result: ast.FunctionExpression = {
    type: 'FunctionExpression',
    parameters: [],
    body: body.result,
  };

  return { result, cursor };
};

const parseBranchExpression = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.BranchExpression; cursor: SourceStream } => {
  let tmp;
  tmp = consumeSequence(cursor, 'branch');
  if (!tmp) return null;
  cursor = tmp;

  cursor = skipWhitespace(cursor);

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, '{'),
    'The next part of a branch expression should be a "{" to list the conditions'
  );
  cursor = skipWhitespace(cursor);

  const conditions: ast.BranchCondition[] = [];
  while (((tmp = consumeSequence(cursor, '}')), !tmp)) {
    const condition = parseBranchCondition(cursor, ctx);
    cursor = condition.cursor;
    conditions.push(condition.result);
    cursor = skipWhitespace(cursor);
  }
  cursor = tmp;

  const result: ast.BranchExpression = {
    type: 'BranchExpression',
    conditions,
  };

  return { result, cursor };
};

const parseBranchCondition = (
  cursor: SourceStream,
  ctx: Context
): { result: ast.BranchCondition; cursor: SourceStream } => {
  let tmp;
  let guard;

  if (((tmp = consumeSequence(cursor, 'if')), tmp)) {
    cursor = tmp;
    guard = parseExpression(cursor, ctx);
    if (!guard)
      throw new CompilerError('Expected an expression after "if"', cursor);
    cursor = guard.cursor;
  } else {
    cursor = expectCursor(
      cursor,
      consumeSequence(cursor, 'default'),
      'Expected a pattern, an "if" guard, or a "default" case'
    );
  }

  cursor = skipWhitespace(cursor);

  cursor = expectCursor(
    cursor,
    consumeSequence(cursor, '=>'),
    'The next part of a branch condition should be a "=>" to transition to the body'
  );
  cursor = skipWhitespace(cursor, { stopAtNewline: true });

  const body = parseExpression(cursor, ctx);
  if (!body)
    throw new CompilerError(
      'Expected an expression as condition body after "=>"',
      cursor
    );
  cursor = advanceLine(body.cursor);

  const result: ast.BranchCondition = {
    type: 'BranchCondition',
    guard: guard?.result,
    body: body.result,
  };
  return {
    result,
    cursor,
  };
};

const parseTypeReference = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.TypeReference; cursor: SourceStream } => {
  return parseIdentifier(cursor, ctx);
};
