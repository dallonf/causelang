#[derive(Debug, Clone, PartialEq, Eq)]
pub enum NodeTag {
    ReferencesFile(ReferencesFileNodeTag),
    BadFileReference(BadFileReferenceNodeTag),
    ValuesGoesTo(ValuesGoesToNodeTag),
    ValuesComesFrom(ValuesComesFromNodeTag),
    FunctionCanReturnTypeOf(FunctionCanReturnTypeOfNodeTag),
    ReturnsFromFunction(ReturnsFromFunctionNodeTag),
    FunctionCanReturnAction(FunctionCanReturnActionNodeTag),
    ActionReturn(ActionReturnNodeTag),
}
impl NodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> Option<NodeTag> {
    match self {
        NodeTag::ReferencesFile(_) => None,
        NodeTag::BadFileReference(_) => None,
        NodeTag::ValuesGoesTo(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ValuesComesFrom(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::FunctionCanReturnTypeOf(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ReturnsFromFunction(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::FunctionCanReturnAction(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ActionReturn(tag) => Some(tag.inverse(breadcrumbs).into()),
    }
  }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ReferencesFileNodeTag {
    pub path: String,
    pub export_name: Option<String>,
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BadFileReferenceNodeTag {
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ValuesGoesToNodeTag {
    pub destination: Breadcrumbs,
}
impl ValuesGoesToNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> ValuesComesFromNodeTag {
    ValuesComesFromNodeTag {
      source: breadcrumbs.clone(),
    }
  }
}
impl From<ValuesGoesToNodeTag> for NodeTag {
  fn from(tag: ValuesGoesToNodeTag) -> Self {
    NodeTag::ValuesGoesTo(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ValuesComesFromNodeTag {
    pub source: Breadcrumbs,
}
impl ValuesComesFromNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> ValuesGoesToNodeTag {
    ValuesGoesToNodeTag {
      destination: breadcrumbs.clone(),
    }
  }
}
impl From<ValuesComesFromNodeTag> for NodeTag {
  fn from(tag: ValuesComesFromNodeTag) -> Self {
    NodeTag::ValuesComesFrom(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FunctionCanReturnTypeOfNodeTag {
    pub return_expression: Breadcrumbs,
}
impl FunctionCanReturnTypeOfNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> ReturnsFromFunctionNodeTag {
    ReturnsFromFunctionNodeTag {
      function: breadcrumbs.clone(),
    }
  }
}
impl From<FunctionCanReturnTypeOfNodeTag> for NodeTag {
  fn from(tag: FunctionCanReturnTypeOfNodeTag) -> Self {
    NodeTag::FunctionCanReturnTypeOf(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ReturnsFromFunctionNodeTag {
    pub function: Breadcrumbs,
}
impl ReturnsFromFunctionNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> FunctionCanReturnTypeOfNodeTag {
    FunctionCanReturnTypeOfNodeTag {
      return_expression: breadcrumbs.clone(),
    }
  }
}
impl From<ReturnsFromFunctionNodeTag> for NodeTag {
  fn from(tag: ReturnsFromFunctionNodeTag) -> Self {
    NodeTag::ReturnsFromFunction(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FunctionCanReturnActionNodeTag {
    pub return_expression: Breadcrumbs,
}
impl FunctionCanReturnActionNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> ActionReturnNodeTag {
    ActionReturnNodeTag {
      function: breadcrumbs.clone(),
    }
  }
}
impl From<FunctionCanReturnActionNodeTag> for NodeTag {
  fn from(tag: FunctionCanReturnActionNodeTag) -> Self {
    NodeTag::FunctionCanReturnAction(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ActionReturnNodeTag {
    pub function: Breadcrumbs,
}
impl ActionReturnNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> FunctionCanReturnActionNodeTag {
    FunctionCanReturnActionNodeTag {
      return_expression: breadcrumbs.clone(),
    }
  }
}
impl From<ActionReturnNodeTag> for NodeTag {
  fn from(tag: ActionReturnNodeTag) -> Self {
    NodeTag::ActionReturn(tag)
  }
}
