#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum OldResolvingLangType {
  TypeReference(AnyOldResolvingLangType),
  Instance(InstanceOldResolvingLangType),
  Function(FunctionOldResolvingLangType),
  Primitive(PrimitiveLangType),
  StopgapDictionary,
  StopgapList,
  Action,
  Anything,
  AnySignal,
  NeverContinues,
  OneOf(OneOfOldResolvingLangType),
  BadValue,
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct InstanceOldResolvingLangType {
  pub type_id: Arc<CanonicalLangTypeId>,
}
impl From<InstanceOldResolvingLangType> for OldResolvingLangType {
  fn from(value: InstanceOldResolvingLangType) -> Self {
    Self::Instance(value)
  }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct FunctionOldResolvingLangType {
  pub name: Option<Arc<String>>,
  pub params: Vec<OldResolvingLangParameter>,
  pub return_type: AnyOldResolvingLangType,
}
impl From<FunctionOldResolvingLangType> for OldResolvingLangType {
  fn from(value: FunctionOldResolvingLangType) -> Self {
    Self::Function(value)
  }
}
#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct OneOfOldResolvingLangType {
  pub options: Vec<AnyOldResolvingLangType>,
}
impl From<OneOfOldResolvingLangType> for OldResolvingLangType {
  fn from(value: OneOfOldResolvingLangType) -> Self {
    Self::OneOf(value)
  }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct OldResolvingLangParameter {
    pub name: Arc<String>,
    pub value_type: AnyOldResolvingLangType,
}


impl HasInference for OldResolvingLangType {
  fn recursive_inferred_types(&self) -> Vec<AnyOldResolvingLangType> {
    match self {
        OldResolvingLangType::TypeReference(it) => it.recursive_inferred_types(),
        OldResolvingLangType::Instance(it) => it.recursive_inferred_types(),
        OldResolvingLangType::Function(it) => it.recursive_inferred_types(),
        OldResolvingLangType::Primitive(it) => vec![],
        OldResolvingLangType::StopgapDictionary => vec![],
        OldResolvingLangType::StopgapList => vec![],
        OldResolvingLangType::Action => vec![],
        OldResolvingLangType::Anything => vec![],
        OldResolvingLangType::AnySignal => vec![],
        OldResolvingLangType::NeverContinues => vec![],
        OldResolvingLangType::OneOf(it) => it.recursive_inferred_types(),
        OldResolvingLangType::BadValue => vec![],
    }
  }

  fn fill_variable(&self, id: u64, value: AnyOldResolvingLangType) -> Self {
    match self {
        OldResolvingLangType::TypeReference(it) => OldResolvingLangType::TypeReference(it.fill_variable(id, value.clone())),
        OldResolvingLangType::Instance(it) => OldResolvingLangType::Instance(it.fill_variable(id, value)),
        OldResolvingLangType::Function(it) => OldResolvingLangType::Function(it.fill_variable(id, value)),
        OldResolvingLangType::Primitive(it) => OldResolvingLangType::Primitive(it.clone()),
        OldResolvingLangType::StopgapDictionary => OldResolvingLangType::StopgapDictionary,
        OldResolvingLangType::StopgapList => OldResolvingLangType::StopgapList,
        OldResolvingLangType::Action => OldResolvingLangType::Action,
        OldResolvingLangType::Anything => OldResolvingLangType::Anything,
        OldResolvingLangType::AnySignal => OldResolvingLangType::AnySignal,
        OldResolvingLangType::NeverContinues => OldResolvingLangType::NeverContinues,
        OldResolvingLangType::OneOf(it) => OldResolvingLangType::OneOf(it.fill_variable(id, value)),
        OldResolvingLangType::BadValue => OldResolvingLangType::BadValue,
    }
  }
}

impl HasInference for InstanceOldResolvingLangType {
  fn recursive_inferred_types(&self) -> Vec<AnyOldResolvingLangType> {
    let mut result = vec![];
    return result;
  }
  fn fill_variable(&self, id: u64, value: AnyOldResolvingLangType) -> Self {
    return Self {
        type_id: self.type_id.clone(),
    }
  }
}
impl HasInference for FunctionOldResolvingLangType {
  fn recursive_inferred_types(&self) -> Vec<AnyOldResolvingLangType> {
    let mut result = vec![];
    result.append(&mut self.params.iter().flat_map(|it| { let mut inner_result = vec![]; inner_result.append(&mut vec![]); inner_result.append(&mut it.value_type.recursive_inferred_types());  inner_result }).collect());
    result.append(&mut self.return_type.recursive_inferred_types());
    return result;
  }
  fn fill_variable(&self, id: u64, value: AnyOldResolvingLangType) -> Self {
    return Self {
        name: self.name.as_ref().map(|it| it.clone()),
        params: self.params.iter().map(|it| OldResolvingLangParameter { name: it.name.clone(),value_type: it.value_type.fill_variable(id, value.clone()) }).collect(),
        return_type: self.return_type.fill_variable(id, value.clone()),
    }
  }
}
impl HasInference for OneOfOldResolvingLangType {
  fn recursive_inferred_types(&self) -> Vec<AnyOldResolvingLangType> {
    let mut result = vec![];
    result.append(&mut self.options.iter().flat_map(|it| it.recursive_inferred_types()).collect());
    return result;
  }
  fn fill_variable(&self, id: u64, value: AnyOldResolvingLangType) -> Self {
    return Self {
        options: self.options.iter().map(|it| it.fill_variable(id, value.clone())).collect(),
    }
  }
}
