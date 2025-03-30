#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum LangType {
  TypeReference(AnyInferredLangType),
  Instance(InstanceLangType),
  Function(FunctionLangType),
  Primitive(PrimitiveLangType),
  StopgapDictionary,
  StopgapList,
  Action,
  Anything,
  AnySignal,
  NeverContinues,
  OneOf(OneOfLangType),
  BadValue,
}
impl HasInference for LangType {
  fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
    match self {
        LangType::TypeReference(it) => it.recursive_inferred_types(),
        LangType::Instance(it) => it.recursive_inferred_types(),
        LangType::Function(it) => it.recursive_inferred_types(),
        LangType::Primitive(it) => vec![],
        LangType::StopgapDictionary => vec![],
        LangType::StopgapList => vec![],
        LangType::Action => vec![],
        LangType::Anything => vec![],
        LangType::AnySignal => vec![],
        LangType::NeverContinues => vec![],
        LangType::OneOf(it) => it.recursive_inferred_types(),
        LangType::BadValue => vec![],
    }
  }

  fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
    match self {
        LangType::TypeReference(it) => LangType::TypeReference(it.fill_variable(id, value.clone())),
        LangType::Instance(it) => LangType::Instance(it.fill_variable(id, value)),
        LangType::Function(it) => LangType::Function(it.fill_variable(id, value)),
        LangType::Primitive(it) => LangType::Primitive(it.clone()),
        LangType::StopgapDictionary => LangType::StopgapDictionary,
        LangType::StopgapList => LangType::StopgapList,
        LangType::Action => LangType::Action,
        LangType::Anything => LangType::Anything,
        LangType::AnySignal => LangType::AnySignal,
        LangType::NeverContinues => LangType::NeverContinues,
        LangType::OneOf(it) => LangType::OneOf(it.fill_variable(id, value)),
        LangType::BadValue => LangType::BadValue,
    }
  }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct InstanceLangType {
  pub type_id: Arc<CanonicalLangTypeId>,
}
impl From<InstanceLangType> for LangType {
  fn from(value: InstanceLangType) -> Self {
    Self::Instance(value)
  }
}
impl HasInference for InstanceLangType {
  fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
    let mut result = vec![];
    return result;
  }
  fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
    return Self {
        type_id: self.type_id.clone(),
    }
  }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct FunctionLangType {
  pub name: Option<Arc<String>>,
  pub params: Vec<LangParameter>,
  pub return_type: AnyInferredLangType,
}
impl From<FunctionLangType> for LangType {
  fn from(value: FunctionLangType) -> Self {
    Self::Function(value)
  }
}
impl HasInference for FunctionLangType {
  fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
    let mut result = vec![];
    result.append(&mut self.params.iter().flat_map(|it| { let mut inner_result = vec![]; inner_result.append(&mut vec![]); inner_result.append(&mut it.value_type.recursive_inferred_types());  inner_result }).collect());
    result.append(&mut self.return_type.recursive_inferred_types());
    return result;
  }
  fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
    return Self {
        name: self.name.as_ref().map(|it| it.clone()),
        params: self.params.iter().map(|it| LangParameter { name: it.name.clone(),value_type: it.value_type.fill_variable(id, value.clone()) }).collect(),
        return_type: self.return_type.fill_variable(id, value.clone()),
    }
  }
}
#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct OneOfLangType {
  pub options: Vec<AnyInferredLangType>,
}
impl From<OneOfLangType> for LangType {
  fn from(value: OneOfLangType) -> Self {
    Self::OneOf(value)
  }
}
impl HasInference for OneOfLangType {
  fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
    let mut result = vec![];
    result.append(&mut self.options.iter().flat_map(|it| it.recursive_inferred_types()).collect());
    return result;
  }
  fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
    return Self {
        options: self.options.iter().map(|it| it.fill_variable(id, value.clone())).collect(),
    }
  }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct LangParameter {
    pub name: Arc<String>,
    pub value_type: AnyInferredLangType,
}
