export type NodeTag = SingleNodeTag | TwoWayNodeTag;

export interface SingleNodeTag {
  kind: "single";
  name: string;
  params: Record<string, NodeTagParam>;
}

export interface TwoWayNodeTag {
  kind: "two-way";
  interface: {
    breadcrumb1: string;
    forwardName: string;
    breadcrumb2: string;
    inverseName: string;
  };
  extraParams: Record<string, NodeTagParam>;
}

export interface NodeTagParam {
  type: "breadcrumbs" | "string" | "uint";
  nullable?: boolean;
}

export function singleNodeTag(
  name: string,
  params: Record<string, NodeTagParam>
): SingleNodeTag {
  return { kind: "single", name, params };
}

export function twoWayNodeTag(
  [breadcrumb1, forwardName, breadcrumb2]: [string, string, string],
  {
    inverseName,
  }: {
    inverseName: string;
  },
  extraParams: Record<string, NodeTagParam> = {}
): TwoWayNodeTag {
  return {
    kind: "two-way",
    interface: { breadcrumb1, forwardName, breadcrumb2, inverseName },
    extraParams,
  };
}
