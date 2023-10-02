#[derive(Debug, Clone, PartialEq, Eq)]
pub enum NodeTag {
    ReferencesFile(ReferencesFileNodeTag),
    BadFileReference(BadFileReferenceNodeTag),
    ValueGoesTo(ValueGoesToNodeTag),
    ValueComesFrom(ValueComesFromNodeTag),
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
        NodeTag::ValueGoesTo(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ValueComesFrom(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::FunctionCanReturnTypeOf(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ReturnsFromFunction(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::FunctionCanReturnAction(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ActionReturn(tag) => Some(tag.inverse(breadcrumbs).into()),
    }
  }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ReferencesFileNodeTag {
    pub path: Arc<String>,
    pub export_name: Option<Arc<String>>,
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BadFileReferenceNodeTag {
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ValueGoesToNodeTag {
    pub destination: Breadcrumbs,
}
impl ValueGoesToNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> ValueComesFromNodeTag {
    ValueComesFromNodeTag {
      source: breadcrumbs.clone(),
    }
  }
}
impl From<ValueGoesToNodeTag> for NodeTag {
  fn from(tag: ValueGoesToNodeTag) -> Self {
    NodeTag::ValueGoesTo(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ValueComesFromNodeTag {
    pub source: Breadcrumbs,
}
impl ValueComesFromNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> ValueGoesToNodeTag {
    ValueGoesToNodeTag {
      destination: breadcrumbs.clone(),
    }
  }
}
impl From<ValueComesFromNodeTag> for NodeTag {
  fn from(tag: ValueComesFromNodeTag) -> Self {
    NodeTag::ValueComesFrom(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FunctionCanReturnTypeOfNodeTag {
    pub return_expression_value: Breadcrumbs,
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
      return_expression_value: breadcrumbs.clone(),
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
