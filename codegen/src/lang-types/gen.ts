import { path } from "../../deps.ts";
import { doit } from "../utils/doit.ts";
import { compileTemplate } from "../utils/templates.ts";
import { langTypes } from "./langTypes.ts";
import { FieldType, getTypeHasSubtypes, LangTypeDeclaration } from "./types.ts";

const dirname = path.dirname(path.fromFileUrl(import.meta.url));
const projectRoot = path.resolve(dirname, "../../../");

export async function generateLangTypes() {
  await Promise.all([generateRustLangTypes()]);
}

async function generateRustLangTypes() {
  const structsTemplate = await compileTemplate(
    "_lang_type_structs.rs.handlebars",
    import.meta.url
  );
  const oldResolvingTemplate = await compileTemplate(
    "old_resolving_lang_types.rs.handlebars",
    import.meta.url
  );
  const generalTemplate = await compileTemplate(
    "lang_types.rs.handlebars",
    import.meta.url
  );

  type LangTypesTemplateGenerationContext = {
    prefix: string;
    fallibleType: string;
  };

  type LangTypesTemplate = {
    prefix: string;
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
        inferenceGetRecursiveInferredTypesExpression: string;
        inferenceFillVariableExpression: string;
        conversionExpression: string;
      }
  );

  type TemplateComplexLangType = {
    optionName: string;
    structName: string;
    fields: TemplateComplexLangTypeField[];
    customHashImplementation?: boolean;
  };

  type TemplateComplexLangTypeField = {
    name: string;
    type: string;
    inferenceFillVariableExpression: string;
    conversionExpression: string;
  } & (
    | { hasSubtypes: false }
    | {
        hasSubtypes: true;
        inferenceGetRecursiveInferredTypesExpression: string;
      }
  );

  type TemplateObjectType = {
    name: string;
    fields: TemplateObjectLangTypeField[];
  };

  type TemplateObjectLangTypeField = {
    name: string;
    type: string;
    conversionExpression: string;
  };

  function rustTypeExpression(
    fieldType: FieldType,
    ctx: LangTypesTemplateGenerationContext
  ): string {
    switch (fieldType.kind) {
      case "string":
        return "Arc<String>";
      case "langType":
        return ctx.fallibleType;
      case "canonicalTypeId":
        return "Arc<CanonicalLangTypeId>";
      case "optional":
        return `Option<${rustTypeExpression(fieldType.type, ctx)}>`;
      case "list":
        return `Vec<${rustTypeExpression(fieldType.type, ctx)}>`;
      case "object":
        return `${ctx.prefix}${fieldType.name}`;
      case "primitiveEnum":
        return "PrimitiveLangType";
      default:
        return fieldType satisfies never;
    }
  }

  function getRecursiveInferredTypesExpression(
    fieldRef: string,
    fieldType: FieldType
  ): string {
    switch (fieldType.kind) {
      case "canonicalTypeId":
      case "string":
      case "primitiveEnum":
        return "vec![]";

      case "langType":
        return `${fieldRef}.recursive_inferred_types()`;
      case "list":
        return `${fieldRef}.iter().flat_map(|it| ${getRecursiveInferredTypesExpression(
          "it",
          fieldType.type
        )}).collect()`;
      case "object": {
        const appendStatements = Object.entries(fieldType.fields).map(
          ([subfieldName, subfieldType]) => {
            return `inner_result.append(&mut ${getRecursiveInferredTypesExpression(
              `${fieldRef}.${subfieldName}`,
              subfieldType
            )}); `;
          }
        );
        return `{ let mut inner_result = vec![]; ${appendStatements.join(
          ""
        )} inner_result }`;
      }
      case "optional":
        return `${fieldRef}.as_ref().map(|it| ${getRecursiveInferredTypesExpression(
          "it",
          fieldType.type
        )}).unwrap_or(vec![])`;
      default:
        return fieldType satisfies never;
    }
  }

  function getInferenceFillVariableExpression(
    fieldRef: string,
    fieldType: FieldType,
    ctx: LangTypesTemplateGenerationContext
  ): string {
    switch (fieldType.kind) {
      case "string":
      case "canonicalTypeId":
      case "primitiveEnum":
        return `${fieldRef}.clone()`;
      case "langType":
        return `${fieldRef}.fill_variable(id, value.clone())`;
      case "optional":
        return `${fieldRef}.as_ref().map(|it| ${getInferenceFillVariableExpression(
          "it",
          fieldType.type,
          ctx
        )})`;
      case "list":
        return `${fieldRef}.iter().map(|it| ${getInferenceFillVariableExpression(
          "it",
          fieldType.type,
          ctx
        )}).collect()`;
      case "object": {
        const fields = Object.entries(fieldType.fields).map(
          ([objectFieldName, objectFieldType]) =>
            `${objectFieldName}: ${getInferenceFillVariableExpression(
              `${fieldRef}.${objectFieldName}`,
              objectFieldType,
              ctx
            )}`
        );
        return `${ctx.prefix}${fieldType.name} { ${fields.join(",")} }`;
      }
      default:
        return fieldType satisfies never;
    }
  }

  function getConversionExpression(
    fieldRef: string,
    fieldType: FieldType,
    ctx: LangTypesTemplateGenerationContext
  ): string {
    switch (fieldType.kind) {
      case "canonicalTypeId":
      case "string":
      case "primitiveEnum":
        return fieldRef;
      case "langType":
      case "object":
        return `${fieldRef}.into()`;
      case "list":
        return `${fieldRef}.into_iter().map(|it| ${getConversionExpression(
          "it",
          fieldType.type,
          ctx
        )}).collect()`;
      case "optional":
        return `${fieldRef}.map(|it| ${getConversionExpression(
          "it",
          fieldType.type,
          ctx
        )})`;
      default:
        return fieldType satisfies never;
    }
  }

  type ObjectFieldType = FieldType & { kind: "object" };
  function discoverObjects(fieldType: FieldType): ObjectFieldType[] {
    switch (fieldType.kind) {
      case "object":
        return [
          fieldType,
          ...Object.values(fieldType.fields).flatMap(discoverObjects),
        ];

      case "list":
      case "optional":
        return discoverObjects(fieldType.type);

      case "canonicalTypeId":
      case "langType":
      case "primitiveEnum":
      case "string":
        return [];

      default:
        return fieldType satisfies never;
    }
  }

  function getTemplateParams(
    langTypes: LangTypeDeclaration[],
    ctx: LangTypesTemplateGenerationContext
  ): LangTypesTemplate {
    const templateLangTypes: TemplateLangType[] = langTypes.map((langType) => {
      if (langType.kind === undefined) {
        return { name: langType.name, hasParam: false };
      }

      const paramType = doit((): string => {
        switch (langType.kind) {
          case "simple":
            return rustTypeExpression(langType.type, ctx);
          case "complex":
            return langType.name + ctx.prefix + "LangType";
          default:
            return langType satisfies never;
        }
      });

      const inferenceGetRecursiveInferredTypesExpression = doit((): string => {
        switch (langType.kind) {
          case "simple":
            return getRecursiveInferredTypesExpression("it", langType.type);
          case "complex":
            return "it.recursive_inferred_types()";
          default:
            return langType satisfies never;
        }
      });

      const inferenceFillVariableExpression = doit((): string => {
        switch (langType.kind) {
          case "simple":
            return getInferenceFillVariableExpression("it", langType.type, ctx);
          case "complex":
            return "it.fill_variable(id, value)";
          default:
            return langType satisfies never;
        }
      });

      const conversionExpression = doit((): string => {
        switch (langType.kind) {
          case "simple":
            return getConversionExpression("it", langType.type, ctx);
          case "complex":
            return "it.into()";
          default:
            return langType satisfies never;
        }
      });

      return {
        name: langType.name,
        hasParam: true,
        paramType,
        inferenceGetRecursiveInferredTypesExpression,
        inferenceFillVariableExpression,
        conversionExpression,
      };
    });

    const complexLangTypes: TemplateComplexLangType[] = langTypes
      .filter((it) => it.kind === "complex")
      .map((complexLangType): TemplateComplexLangType => {
        return {
          structName: `${complexLangType.name}${ctx.prefix}LangType`,
          optionName: complexLangType.name,
          customHashImplementation: complexLangType.customHashImplementation,
          fields: Object.entries(complexLangType.fields).map(
            ([fieldName, fieldType]): TemplateComplexLangTypeField => {
              const hasSubtypes = getTypeHasSubtypes(fieldType);

              const subtypesSpread = hasSubtypes
                ? {
                    hasSubtypes: true as const,
                    inferenceGetRecursiveInferredTypesExpression:
                      getRecursiveInferredTypesExpression(
                        `self.${fieldName}`,
                        fieldType
                      ),
                  }
                : { hasSubtypes: false as const };

              return {
                name: fieldName,
                type: rustTypeExpression(fieldType, ctx),
                conversionExpression: getConversionExpression(
                  `value.${fieldName}`,
                  fieldType,
                  ctx
                ),
                inferenceFillVariableExpression:
                  getInferenceFillVariableExpression(
                    `self.${fieldName}`,
                    fieldType,
                    ctx
                  ),
                ...subtypesSpread,
              };
            }
          ),
        };
      });

    const objects: TemplateObjectType[] = langTypes
      .flatMap((langType): ObjectFieldType[] => {
        switch (langType.kind) {
          case undefined:
            return [];
          case "simple":
            return discoverObjects(langType.type);
          case "complex":
            return Object.values(langType.fields).flatMap(discoverObjects);
          default:
            return langType satisfies never;
        }
      })
      .map(
        (objectType): TemplateObjectType => ({
          name: objectType.name,
          fields: Object.entries(objectType.fields).map(
            ([objectFieldName, objectFieldType]) => ({
              name: objectFieldName,
              type: rustTypeExpression(objectFieldType, ctx),
              conversionExpression: getConversionExpression(
                `value.${objectFieldName}`,
                objectFieldType,
                ctx
              ),
            })
          ),
        })
      );

    return {
      prefix: ctx.prefix,
      langTypes: templateLangTypes,
      complexLangTypes,
      objects,
    };
  }

  const oldResolvingParams = getTemplateParams(langTypes, {
    prefix: "OldResolving",
    fallibleType: "AnyOldResolvingLangType",
  });
  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "rscause/rscause_compiler/src/gen/old_resolving_lang_types.rs"
    ),
    oldResolvingTemplate({
      ...oldResolvingParams,
      structs: structsTemplate(oldResolvingParams),
    })
  );
  await Deno.writeTextFile(
    path.join(projectRoot, "rscause/rscause_compiler/src/gen/lang_types.rs"),
    generalTemplate({
      structs: structsTemplate(
        getTemplateParams(langTypes, {
          prefix: "",
          fallibleType: "FallibleLangType",
        })
      ),
    })
  );
}
