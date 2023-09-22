import { categories, nodes } from "./nodes.ts";
import { NodeDeclaration, NodeFieldType } from "./types.ts";

export function checkData(): boolean {
  const errors: string[] = [];

  const allCategories = new Set<string>();
  const allNodes = new Map<string, NodeDeclaration>();

  // Check for duplicate categories and nodes
  {
    for (const category of categories) {
      if (allCategories.has(category.name)) {
        errors.push(`${category.name} category is defined more than once`);
      }
      allCategories.add(category.name);
    }
    for (const node of nodes) {
      if (allNodes.has(node.name)) {
        errors.push(`${node.name} node is defined more than once`);
      }
      allNodes.set(node.name, node);
    }
  }

  // Check for unknown categories and types
  {
    for (const node of allNodes.values()) {
      if (node.category && !allCategories.has(node.category)) {
        errors.push(`${node.name} node has unknown category ${node.category}`);
      }

      for (const [fieldName, field] of Object.entries(node.fields)) {
        const unwrappedType = unwrapFieldReference(field);
        if (unwrappedType != null) {
          if (
            !allNodes.has(unwrappedType) &&
            !allCategories.has(unwrappedType)
          ) {
            errors.push(
              `${node.name}.${fieldName} references unknown type: ${unwrappedType}`
            );
          }
        }
      }
    }
  }

  // Check for empty categories
  {
    const nodesForCategory = new Map<string, NodeDeclaration[]>();
    for (const node of allNodes.values()) {
      if (node.category) {
        const nodes = nodesForCategory.get(node.category) ?? [];
        nodes.push(node);
        nodesForCategory.set(node.category, nodes);
      }
    }
    for (const category of categories) {
      const numNodes = nodesForCategory.get(category.name)?.length ?? 0;
      if (numNodes === 0) {
        errors.push(`Category ${category.name} has no nodes`);
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
  if (fieldType.kind === "list" || fieldType.kind === "optional") {
    return unwrapFieldReference(fieldType.type);
  }
  if (fieldType.kind === "primitive") {
    return null;
  }
  return fieldType satisfies never;
}
