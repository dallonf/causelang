import { path } from "../../deps.ts";
import { doit } from "../utils/doit.ts";
import { compileTemplate } from "../utils/templates.ts";
import { langTypes } from "./langTypes.ts";
import { FieldType } from "./types.ts";

const dirname = path.dirname(path.fromFileUrl(import.meta.url));
const projectRoot = path.resolve(dirname, "../../../");

export async function generateLangTypes() {
  await Promise.all([generateRustLangTypes()]);
}

type LangTypesTemplate = {
  langTypes: TemplateLangType[];
  complexLangTypes: TemplateComplexLangType[];
  objects: TemplateObjectType[];
};

type TemplateLangType = {
  name: string;
} & (
  | {
      hasParam: false;
    }
  | {
      hasParam: true;
      paramType: string;
    }
);

type TemplateComplexLangType = {
  name: string;
  fields: TemplateComplexLangTypeField[];
};

type TemplateComplexLangTypeField = {
  name: string;
  type: string;
  inferenceFillVariableExpression: string;
} & (
  | { hasSubtypes: false }
  | { hasSubtypes: true; inferenceGetRecursiveInferredTypesExpression: string }
);

type TemplateObjectType = {
  name: string;
  fields: TemplateObjectLangTypeField[];
  hasSubtypes: boolean;
};

type TemplateObjectLangTypeField = {
  name: string;
  type: string;
  inferenceFillVariableExpression: string;
} & (
  | { hasSubtypes: false }
  | { hasSubtypes: true; inferenceGetRecursiveInferredTypesExpression: string }
);

async function generateRustLangTypes() {
  const template = await compileTemplate(
    "lang_types.rs.handlebars",
    import.meta.url
  );

  const templateLangTypes: TemplateLangType[] = langTypes.map((langType) => {
    if (langType.kind === undefined) {
      return { name: langType.name, hasParam: false };
    }

    const paramType = doit((): string => {
      switch (langType.kind) {
        case "simple":
          return rustTypeExpression(langType.type);
        case "complex":
          return langType.name + "LangType";
        default:
          return langType satisfies never;
      }
    });

    return {
      name: langType.name,
      hasParam: true,
      paramType,
    };
  });

  const output = template({
    langTypes: templateLangTypes,
    // TODO
    complexLangTypes: [],
    objects: [],
  } satisfies LangTypesTemplate);

  await Deno.writeTextFile(
    path.join(projectRoot, "rscause/rscause_compiler/src/gen/lang_types.rs"),
    output
  );
}

function rustTypeExpression(fieldType: FieldType): string {
  switch (fieldType.kind) {
    case "string":
      return "Arc<String>";
    case "langType":
      return "AnyInferredLangType";
    case "canonicalTypeId":
      return "Arc<CanonicalLangTypeId>";
    case "optional":
      return `Option<${rustTypeExpression(fieldType.type)}>`;
    case "list":
      return `Vec<${rustTypeExpression(fieldType.type)}>`;
    case "object":
      return fieldType.name;
    case "primitiveEnum":
      return "PrimitiveLangType";
    default:
      return fieldType satisfies never;
  }
}
