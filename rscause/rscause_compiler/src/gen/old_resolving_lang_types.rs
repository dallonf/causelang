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

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum OldResolvingCanonicalLangType {
    Object(ObjectOldResolvingCanonicalLangType),
    Signal(SignalOldResolvingCanonicalLangType),
}
impl OldResolvingCanonicalLangType {
    pub fn type_id(&self) -> CanonicalLangTypeId {
        match self {
            Self::Object(object) => object.type_id.clone(),
            Self::Signal(signal) => signal.type_id.clone(),
        }
    }
    pub fn fields(&self) -> Vec<OldResolvingCanonicalTypeField> {
        match self {
            Self::Object(object) => object.fields.clone(),
            Self::Signal(signal) => signal.fields.clone(),
        }
    }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct OldResolvingCanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: AnyOldResolvingLangType,
}

fn assert_uniqueness_matches(
    struct_name: &str,
    type_id: &CanonicalLangTypeId,
    fields: &[OldResolvingCanonicalTypeField],
) {
    let is_unique = fields.is_empty();
    if type_id.is_unique != is_unique {
        panic!(
            "Tried to create {struct_name} with type_id.is_unique={} but fields are {}",
            is_unique,
            if fields.is_empty() {
                "empty"
            } else {
                "not empty"
            }
        );
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct ObjectOldResolvingCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<OldResolvingCanonicalTypeField>,
}
impl ObjectOldResolvingCanonicalLangType {
    pub fn new(type_id: CanonicalLangTypeId, fields: Vec<OldResolvingCanonicalTypeField>) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Object {
            panic!("ObjectOldResolvingCanonicalLangType::new called with non-object type_id");
        }
        assert_uniqueness_matches("ObjectOldResolvingCanonicalLangType", &type_id, &fields);
        Self { type_id, fields }
    }
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }
    pub fn fields(&self) -> &[OldResolvingCanonicalTypeField] {
        &self.fields
    }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct SignalOldResolvingCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<OldResolvingCanonicalTypeField>,
    pub result: AnyOldResolvingLangType,
}

impl SignalOldResolvingCanonicalLangType {
    pub fn new(
        type_id: CanonicalLangTypeId,
        fields: Vec<OldResolvingCanonicalTypeField>,
        result: AnyOldResolvingLangType,
    ) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Signal {
            panic!("SignalOldResolvingCanonicalLangType::new called with non-signal type_id");
        }
        assert_uniqueness_matches("SignalOldResolvingCanonicalLangType", &type_id, &fields);
        Self {
            type_id,
            fields,
            result,
        }
    }
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }
    pub fn fields(&self) -> &[OldResolvingCanonicalTypeField] {
        &self.fields
    }
    pub fn result(&self) -> &AnyOldResolvingLangType {
        &self.result
    }
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
impl From<OldResolvingLangType> for lang_types::LangType {
  fn from(value: OldResolvingLangType) -> Self {
    match value {
        OldResolvingLangType::TypeReference(it) => lang_types::LangType::TypeReference(it.into()),
        OldResolvingLangType::Instance(it) => lang_types::LangType::Instance(it.into()),
        OldResolvingLangType::Function(it) => lang_types::LangType::Function(it.into()),
        OldResolvingLangType::Primitive(it) => lang_types::LangType::Primitive(it),
        OldResolvingLangType::StopgapDictionary => lang_types::LangType::StopgapDictionary,
        OldResolvingLangType::StopgapList => lang_types::LangType::StopgapList,
        OldResolvingLangType::Action => lang_types::LangType::Action,
        OldResolvingLangType::Anything => lang_types::LangType::Anything,
        OldResolvingLangType::AnySignal => lang_types::LangType::AnySignal,
        OldResolvingLangType::NeverContinues => lang_types::LangType::NeverContinues,
        OldResolvingLangType::OneOf(it) => lang_types::LangType::OneOf(it.into()),
        OldResolvingLangType::BadValue => lang_types::LangType::BadValue,
    }
  }
}
impl From<lang_types::LangType> for OldResolvingLangType {
  fn from(value: lang_types::LangType) -> Self {
    match value {
        lang_types::LangType::TypeReference(it) => OldResolvingLangType::TypeReference(it.into()),
        lang_types::LangType::Instance(it) => OldResolvingLangType::Instance(it.into()),
        lang_types::LangType::Function(it) => OldResolvingLangType::Function(it.into()),
        lang_types::LangType::Primitive(it) => OldResolvingLangType::Primitive(it),
        lang_types::LangType::StopgapDictionary => OldResolvingLangType::StopgapDictionary,
        lang_types::LangType::StopgapList => OldResolvingLangType::StopgapList,
        lang_types::LangType::Action => OldResolvingLangType::Action,
        lang_types::LangType::Anything => OldResolvingLangType::Anything,
        lang_types::LangType::AnySignal => OldResolvingLangType::AnySignal,
        lang_types::LangType::NeverContinues => OldResolvingLangType::NeverContinues,
        lang_types::LangType::OneOf(it) => OldResolvingLangType::OneOf(it.into()),
        lang_types::LangType::BadValue => OldResolvingLangType::BadValue,
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
impl From<InstanceOldResolvingLangType> for lang_types::InstanceLangType {
  fn from(value: InstanceOldResolvingLangType) -> Self {
    lang_types::InstanceLangType {
      type_id: value.type_id,
    }
  }
}
impl From<lang_types::InstanceLangType> for InstanceOldResolvingLangType {
  fn from(value: lang_types::InstanceLangType) -> Self {
    InstanceOldResolvingLangType {
      type_id: value.type_id,
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
impl From<FunctionOldResolvingLangType> for lang_types::FunctionLangType {
  fn from(value: FunctionOldResolvingLangType) -> Self {
    lang_types::FunctionLangType {
      name: value.name.map(|it| it),
      params: value.params.into_iter().map(|it| it.into()).collect(),
      return_type: value.return_type.into(),
    }
  }
}
impl From<lang_types::FunctionLangType> for FunctionOldResolvingLangType {
  fn from(value: lang_types::FunctionLangType) -> Self {
    FunctionOldResolvingLangType {
      name: value.name.map(|it| it),
      params: value.params.into_iter().map(|it| it.into()).collect(),
      return_type: value.return_type.into(),
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
impl From<OneOfOldResolvingLangType> for lang_types::OneOfLangType {
  fn from(value: OneOfOldResolvingLangType) -> Self {
    lang_types::OneOfLangType {
      options: value.options.into_iter().map(|it| it.into()).collect(),
    }
  }
}
impl From<lang_types::OneOfLangType> for OneOfOldResolvingLangType {
  fn from(value: lang_types::OneOfLangType) -> Self {
    OneOfOldResolvingLangType {
      options: value.options.into_iter().map(|it| it.into()).collect(),
    }
  }
}

impl From<OldResolvingLangParameter> for lang_types::LangParameter {
  fn from(value: OldResolvingLangParameter) -> Self {
    lang_types::LangParameter {
      name: value.name,
      value_type: value.value_type.into(),
    }
  }
}
impl From<lang_types::LangParameter> for OldResolvingLangParameter {
  fn from(value: lang_types::LangParameter) -> Self {
    OldResolvingLangParameter {
      name: value.name,
      value_type: value.value_type.into(),
    }
  }
}
