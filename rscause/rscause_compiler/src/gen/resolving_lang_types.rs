#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum ResolvingLangType {
  TypeReference(LinkedResolvingLangType),
  Instance(InstanceResolvingLangType),
  Function(FunctionResolvingLangType),
  Primitive(PrimitiveLangType),
  StopgapDictionary,
  StopgapList,
  Action,
  Anything,
  AnySignal,
  NeverContinues,
  OneOf(OneOfResolvingLangType),
  BadValue,
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct InstanceResolvingLangType {
  pub type_id: Arc<CanonicalLangTypeId>,
}
impl From<InstanceResolvingLangType> for ResolvingLangType {
  fn from(value: InstanceResolvingLangType) -> Self {
    Self::Instance(value)
  }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct FunctionResolvingLangType {
  pub name: Option<Arc<String>>,
  pub params: Vec<ResolvingLangParameter>,
  pub return_type: LinkedResolvingLangType,
}
impl From<FunctionResolvingLangType> for ResolvingLangType {
  fn from(value: FunctionResolvingLangType) -> Self {
    Self::Function(value)
  }
}
#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct OneOfResolvingLangType {
  pub options: Vec<LinkedResolvingLangType>,
}
impl From<OneOfResolvingLangType> for ResolvingLangType {
  fn from(value: OneOfResolvingLangType) -> Self {
    Self::OneOf(value)
  }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct ResolvingLangParameter {
    pub name: Arc<String>,
    pub value_type: LinkedResolvingLangType,
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum ResolvingCanonicalLangType {
    Object(ObjectResolvingCanonicalLangType),
    Signal(SignalResolvingCanonicalLangType),
}
impl ResolvingCanonicalLangType {
    pub fn type_id(&self) -> CanonicalLangTypeId {
        match self {
            Self::Object(object) => object.type_id.clone(),
            Self::Signal(signal) => signal.type_id.clone(),
        }
    }
    pub fn fields(&self) -> Vec<ResolvingCanonicalTypeField> {
        match self {
            Self::Object(object) => object.fields.clone(),
            Self::Signal(signal) => signal.fields.clone(),
        }
    }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct ResolvingCanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: LinkedResolvingLangType,
}

fn assert_uniqueness_matches(
    struct_name: &str,
    type_id: &CanonicalLangTypeId,
    fields: &[ResolvingCanonicalTypeField],
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
pub struct ObjectResolvingCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<ResolvingCanonicalTypeField>,
}
impl ObjectResolvingCanonicalLangType {
    pub fn new(type_id: CanonicalLangTypeId, fields: Vec<ResolvingCanonicalTypeField>) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Object {
            panic!("ObjectResolvingCanonicalLangType::new called with non-object type_id");
        }
        assert_uniqueness_matches("ObjectResolvingCanonicalLangType", &type_id, &fields);
        Self { type_id, fields }
    }
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }
    pub fn fields(&self) -> &[ResolvingCanonicalTypeField] {
        &self.fields
    }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct SignalResolvingCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<ResolvingCanonicalTypeField>,
    pub result: LinkedResolvingLangType,
}

impl SignalResolvingCanonicalLangType {
    pub fn new(
        type_id: CanonicalLangTypeId,
        fields: Vec<ResolvingCanonicalTypeField>,
        result: LinkedResolvingLangType,
    ) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Signal {
            panic!("SignalResolvingCanonicalLangType::new called with non-signal type_id");
        }
        assert_uniqueness_matches("SignalResolvingCanonicalLangType", &type_id, &fields);
        Self {
            type_id,
            fields,
            result,
        }
    }
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }
    pub fn fields(&self) -> &[ResolvingCanonicalTypeField] {
        &self.fields
    }
    pub fn result(&self) -> &LinkedResolvingLangType {
        &self.result
    }
}


impl From<ResolvingLangType> for lang_types::LangType {
  fn from(value: ResolvingLangType) -> Self {
    match value {
        ResolvingLangType::TypeReference(it) => lang_types::LangType::TypeReference(it.into()),
        ResolvingLangType::Instance(it) => lang_types::LangType::Instance(it.into()),
        ResolvingLangType::Function(it) => lang_types::LangType::Function(it.into()),
        ResolvingLangType::Primitive(it) => lang_types::LangType::Primitive(it),
        ResolvingLangType::StopgapDictionary => lang_types::LangType::StopgapDictionary,
        ResolvingLangType::StopgapList => lang_types::LangType::StopgapList,
        ResolvingLangType::Action => lang_types::LangType::Action,
        ResolvingLangType::Anything => lang_types::LangType::Anything,
        ResolvingLangType::AnySignal => lang_types::LangType::AnySignal,
        ResolvingLangType::NeverContinues => lang_types::LangType::NeverContinues,
        ResolvingLangType::OneOf(it) => lang_types::LangType::OneOf(it.into()),
        ResolvingLangType::BadValue => lang_types::LangType::BadValue,
    }
  }
}
impl From<lang_types::LangType> for ResolvingLangType {
  fn from(value: lang_types::LangType) -> Self {
    match value {
        lang_types::LangType::TypeReference(it) => ResolvingLangType::TypeReference(it.into()),
        lang_types::LangType::Instance(it) => ResolvingLangType::Instance(it.into()),
        lang_types::LangType::Function(it) => ResolvingLangType::Function(it.into()),
        lang_types::LangType::Primitive(it) => ResolvingLangType::Primitive(it),
        lang_types::LangType::StopgapDictionary => ResolvingLangType::StopgapDictionary,
        lang_types::LangType::StopgapList => ResolvingLangType::StopgapList,
        lang_types::LangType::Action => ResolvingLangType::Action,
        lang_types::LangType::Anything => ResolvingLangType::Anything,
        lang_types::LangType::AnySignal => ResolvingLangType::AnySignal,
        lang_types::LangType::NeverContinues => ResolvingLangType::NeverContinues,
        lang_types::LangType::OneOf(it) => ResolvingLangType::OneOf(it.into()),
        lang_types::LangType::BadValue => ResolvingLangType::BadValue,
    }
  }
}

impl From<InstanceResolvingLangType> for lang_types::InstanceLangType {
  fn from(value: InstanceResolvingLangType) -> Self {
    lang_types::InstanceLangType {
      type_id: value.type_id,
    }
  }
}
impl From<lang_types::InstanceLangType> for InstanceResolvingLangType {
  fn from(value: lang_types::InstanceLangType) -> Self {
    InstanceResolvingLangType {
      type_id: value.type_id,
    }
  }
}
impl From<FunctionResolvingLangType> for lang_types::FunctionLangType {
  fn from(value: FunctionResolvingLangType) -> Self {
    lang_types::FunctionLangType {
      name: value.name.map(|it| it),
      params: value.params.into_iter().map(|it| it.into()).collect(),
      return_type: value.return_type.into(),
    }
  }
}
impl From<lang_types::FunctionLangType> for FunctionResolvingLangType {
  fn from(value: lang_types::FunctionLangType) -> Self {
    FunctionResolvingLangType {
      name: value.name.map(|it| it),
      params: value.params.into_iter().map(|it| it.into()).collect(),
      return_type: value.return_type.into(),
    }
  }
}
impl From<OneOfResolvingLangType> for lang_types::OneOfLangType {
  fn from(value: OneOfResolvingLangType) -> Self {
    lang_types::OneOfLangType {
      options: value.options.into_iter().map(|it| it.into()).collect(),
    }
  }
}
impl From<lang_types::OneOfLangType> for OneOfResolvingLangType {
  fn from(value: lang_types::OneOfLangType) -> Self {
    OneOfResolvingLangType {
      options: value.options.into_iter().map(|it| it.into()).collect(),
    }
  }
}

impl From<ResolvingLangParameter> for lang_types::LangParameter {
  fn from(value: ResolvingLangParameter) -> Self {
    lang_types::LangParameter {
      name: value.name,
      value_type: value.value_type.into(),
    }
  }
}
impl From<lang_types::LangParameter> for ResolvingLangParameter {
  fn from(value: lang_types::LangParameter) -> Self {
    ResolvingLangParameter {
      name: value.name,
      value_type: value.value_type.into(),
    }
  }
}
