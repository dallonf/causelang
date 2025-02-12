#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, EnumTryAs)]
pub enum NodeTag {
    ReferencesFile(ReferencesFileNodeTag),
    CanonicalIdInfo(CanonicalIdInfoNodeTag),
    BadFileReference(BadFileReferenceNodeTag),
    TopLevelDeclaration(TopLevelDeclarationNodeTag),
    ValueGoesTo(ValueGoesToNodeTag),
    ValueComesFrom(ValueComesFromNodeTag),
    SetsVariable(SetsVariableNodeTag),
    VariableSetBy(VariableSetByNodeTag),
    FunctionCanReturnTypeOf(FunctionCanReturnTypeOfNodeTag),
    ReturnsFromFunction(ReturnsFromFunctionNodeTag),
    FunctionCanReturnAction(FunctionCanReturnActionNodeTag),
    ActionReturn(ActionReturnNodeTag),
    DeclarationForScope(DeclarationForScopeNodeTag),
    ScopeContainsDeclaration(ScopeContainsDeclarationNodeTag),
    UsesCapturedValue(UsesCapturedValueNodeTag),
    ValueCapturedByFunction(ValueCapturedByFunctionNodeTag),
    FunctionCapturesValue(FunctionCapturesValueNodeTag),
    BreaksLoop(BreaksLoopNodeTag),
    LoopBreaksAt(LoopBreaksAtNodeTag),
}
impl NodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> Option<NodeTag> {
    match self {
        NodeTag::ReferencesFile(_) => None,
        NodeTag::CanonicalIdInfo(_) => None,
        NodeTag::BadFileReference(_) => None,
        NodeTag::TopLevelDeclaration(_) => None,
        NodeTag::ValueGoesTo(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ValueComesFrom(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::SetsVariable(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::VariableSetBy(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::FunctionCanReturnTypeOf(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ReturnsFromFunction(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::FunctionCanReturnAction(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ActionReturn(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::DeclarationForScope(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::ScopeContainsDeclaration(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::UsesCapturedValue(_) => None,
        NodeTag::ValueCapturedByFunction(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::FunctionCapturesValue(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::BreaksLoop(tag) => Some(tag.inverse(breadcrumbs).into()),
        NodeTag::LoopBreaksAt(tag) => Some(tag.inverse(breadcrumbs).into()),
    }
  }
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ReferencesFileNodeTag {
    pub path: Arc<String>,
    pub export_name: Option<Arc<String>>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct CanonicalIdInfoNodeTag {
    pub parent_name: Option<Arc<String>>,
    pub index: u32,
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
pub struct SetsVariableNodeTag {
    pub variable: Breadcrumbs,
}
impl SetsVariableNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> VariableSetByNodeTag {
    VariableSetByNodeTag {
      statement: breadcrumbs.clone(),
    }
  }
}
impl From<SetsVariableNodeTag> for NodeTag {
  fn from(tag: SetsVariableNodeTag) -> Self {
    NodeTag::SetsVariable(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct VariableSetByNodeTag {
    pub statement: Breadcrumbs,
}
impl VariableSetByNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> SetsVariableNodeTag {
    SetsVariableNodeTag {
      variable: breadcrumbs.clone(),
    }
  }
}
impl From<VariableSetByNodeTag> for NodeTag {
  fn from(tag: VariableSetByNodeTag) -> Self {
    NodeTag::VariableSetBy(tag)
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
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct UsesCapturedValueNodeTag {
    pub parent_function: Breadcrumbs,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ValueCapturedByFunctionNodeTag {
    pub function: Breadcrumbs,
}
impl ValueCapturedByFunctionNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> FunctionCapturesValueNodeTag {
    FunctionCapturesValueNodeTag {
      value: breadcrumbs.clone(),
    }
  }
}
impl From<ValueCapturedByFunctionNodeTag> for NodeTag {
  fn from(tag: ValueCapturedByFunctionNodeTag) -> Self {
    NodeTag::ValueCapturedByFunction(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct FunctionCapturesValueNodeTag {
    pub value: Breadcrumbs,
}
impl FunctionCapturesValueNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> ValueCapturedByFunctionNodeTag {
    ValueCapturedByFunctionNodeTag {
      function: breadcrumbs.clone(),
    }
  }
}
impl From<FunctionCapturesValueNodeTag> for NodeTag {
  fn from(tag: FunctionCapturesValueNodeTag) -> Self {
    NodeTag::FunctionCapturesValue(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct BreaksLoopNodeTag {
    pub r#loop: Breadcrumbs,
}
impl BreaksLoopNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> LoopBreaksAtNodeTag {
    LoopBreaksAtNodeTag {
      break_expression: breadcrumbs.clone(),
    }
  }
}
impl From<BreaksLoopNodeTag> for NodeTag {
  fn from(tag: BreaksLoopNodeTag) -> Self {
    NodeTag::BreaksLoop(tag)
  }
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct LoopBreaksAtNodeTag {
    pub break_expression: Breadcrumbs,
}
impl LoopBreaksAtNodeTag {
  pub fn inverse(&self, breadcrumbs: &Breadcrumbs) -> BreaksLoopNodeTag {
    BreaksLoopNodeTag {
      r#loop: breadcrumbs.clone(),
    }
  }
}
impl From<LoopBreaksAtNodeTag> for NodeTag {
  fn from(tag: LoopBreaksAtNodeTag) -> Self {
    NodeTag::LoopBreaksAt(tag)
  }
}
