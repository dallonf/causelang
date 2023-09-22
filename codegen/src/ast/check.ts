import { categories, nodes } from "./nodes.ts";
import { NodeDeclaration, NodeFieldType } from "./types.ts";

export function checkData(): boolean {
  const errors: string[] = [];
  const allCategories = new Set<string>();
  for (const category of categories) {
    if (allCategories.has(category.name)) {
      errors.push(`${category.name} category is defined more than once`);
    }
    allCategories.add(category.name);
  }
  const allNodes = new Map<string, NodeDeclaration>();
  for (const node of nodes) {
    if (allNodes.has(node.name)) {
      errors.push(`${node.name} node is defined more than once`);
    }
    allNodes.set(node.name, node);
  }

  for (const node of allNodes.values()) {
    if (node.category && !allCategories.has(node.category)) {
      errors.push(`${node.name} node has unknown category ${node.category}`);
    }

    for (const [fieldName, field] of Object.entries(node.fields)) {
      const unwrappedType = unwrapFieldReference(field);
      if (unwrappedType != null) {
        if (!allNodes.has(unwrappedType) && !allCategories.has(unwrappedType)) {
          errors.push(
            `${node.name}.${fieldName} references unknown type: ${unwrappedType}`
          );
        }
      }
    }
  }

  if (errors.length) {
    console.error("Errors:");
    for (const error of errors) {
      console.error("  ", error);
    }
    return false;
  }

  return true;
}

function unwrapFieldReference(fieldType: NodeFieldType): string | null {
  if (typeof fieldType === "string") {
    return fieldType;
  }
  if (fieldType.kind === "list") {
    return unwrapFieldReference(fieldType.type);
  }
  if (fieldType.kind === "primitive") {
    return null;
  }
  return fieldType satisfies never;
}
