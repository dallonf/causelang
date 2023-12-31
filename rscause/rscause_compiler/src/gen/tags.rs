#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub enum NodeTag {
    ReferencesFile(ReferencesFileNodeTag),
    BadFileReference(BadFileReferenceNodeTag),
    TopLevelDeclaration(TopLevelDeclarationNodeTag),
    ValueGoesTo(ValueGoesToNodeTag),
    ValueComesFrom(ValueComesFromNodeTag),
    FunctionCanReturnTypeOf(FunctionCanReturnTypeOfNodeTag),
    ReturnsFromFunction(ReturnsFromFunctionNodeTag),
    FunctionCanReturnAction(FunctionCanReturnActionNodeTag),
    ActionReturn(ActionReturnNodeTag),
    DeclarationForScope(DeclarationForScopeNodeTag),
    ScopeContainsDeclaration(ScopeContainsDeclarationNodeTag),
}
impl NodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> Option<NodeTag> {
    match self {
        NodeTag::ReferencesFile(_) => None,
        NodeTag::BadFileReference(_) => None,
        NodeTag::TopLevelDeclaration(_) => None,
        NodeTag::ValueGoesTo(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ValueComesFrom(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::FunctionCanReturnTypeOf(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ReturnsFromFunction(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::FunctionCanReturnAction(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ActionReturn(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::DeclarationForScope(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ScopeContainsDeclaration(tag) => Some(tag.inverse(breadcrumbs).into()),
    }
  }
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ReferencesFileNodeTag {
    pub path: Arc<String>,
    pub export_name: Option<Arc<String>>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct BadFileReferenceNodeTag {
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct TopLevelDeclarationNodeTag {
    pub name: Arc<String>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
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
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
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
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
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
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
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
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
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
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
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
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct DeclarationForScopeNodeTag {
    pub scope: Breadcrumbs,
}
impl DeclarationForScopeNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> ScopeContainsDeclarationNodeTag {
    ScopeContainsDeclarationNodeTag {
      declaration: breadcrumbs.clone(),
    }
  }
}
impl From<DeclarationForScopeNodeTag> for NodeTag {
  fn from(tag: DeclarationForScopeNodeTag) -> Self {
    NodeTag::DeclarationForScope(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ScopeContainsDeclarationNodeTag {
    pub declaration: Breadcrumbs,
}
impl ScopeContainsDeclarationNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> DeclarationForScopeNodeTag {
    DeclarationForScopeNodeTag {
      scope: breadcrumbs.clone(),
    }
  }
}
impl From<ScopeContainsDeclarationNodeTag> for NodeTag {
  fn from(tag: ScopeContainsDeclarationNodeTag) -> Self {
    NodeTag::ScopeContainsDeclaration(tag)
  }
}
