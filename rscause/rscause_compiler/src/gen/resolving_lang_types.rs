#[derive(Debug, Clone, EnumTryAs)]
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

#[derive(Debug, Clone)]
pub struct InstanceResolvingLangType {
  pub type_id: Arc<CanonicalLangTypeId>,
}
impl From<InstanceResolvingLangType> for ResolvingLangType {
  fn from(value: InstanceResolvingLangType) -> Self {
    Self::Instance(value)
  }
}
#[derive(Debug, Clone)]
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
#[derive(Debug, Clone)]
pub struct OneOfResolvingLangType {
  pub options: Vec<LinkedResolvingLangType>,
}
impl From<OneOfResolvingLangType> for ResolvingLangType {
  fn from(value: OneOfResolvingLangType) -> Self {
    Self::OneOf(value)
  }
}

#[derive(Debug, Clone)]
pub struct ResolvingLangParameter {
    pub name: Arc<String>,
    pub value_type: LinkedResolvingLangType,
}

#[derive(Debug, Clone, EnumTryAs)]
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
#[derive(Debug, Clone)]
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

#[derive(Debug, Clone)]
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
#[derive(Debug, Clone)]
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

impl ResolvingLangType {
  pub fn import_type(
    ctx: &mut ResolvingLangTypesContext,
    value: lang_types::LangType,
  ) -> anyhow::Result<Self> {
    match value {
      lang_types::LangType::TypeReference(it) => ResolvingLangType::TypeReference(LinkedResolvingLangType::import_type(ctx, it)?),
      lang_types::LangType::Instance(it) => ResolvingLangType::Instance(InstanceResolvingLangType::import_type(ctx, it)?),
      lang_types::LangType::Function(it) => ResolvingLangType::Function(FunctionResolvingLangType::import_type(ctx, it)?),
      lang_types::LangType::Primitive(it) => ResolvingLangType::Primitive(it),
      lang_types::LangType::StopgapDictionary => ResolvingLangType::StopgapDictionary,
      lang_types::LangType::StopgapList => ResolvingLangType::StopgapList,
      lang_types::LangType::Action => ResolvingLangType::Action,
      lang_types::LangType::Anything => ResolvingLangType::Anything,
      lang_types::LangType::AnySignal => ResolvingLangType::AnySignal,
      lang_types::LangType::NeverContinues => ResolvingLangType::NeverContinues,
      lang_types::LangType::OneOf(it) => ResolvingLangType::OneOf(OneOfResolvingLangType::import_type(ctx, it)?),
      lang_types::LangType::BadValue => ResolvingLangType::BadValue,
    }.pipe(Ok)
  }
}
impl TryFrom<ResolvingLangType> for lang_types::LangType {
  type Error = anyhow::Error;
  fn try_from(value: ResolvingLangType) -> Result<Self, Self::Error> {
    match value {
        ResolvingLangType::TypeReference(it) => lang_types::LangType::TypeReference(it.try_into()?),
        ResolvingLangType::Instance(it) => lang_types::LangType::Instance(it.try_into()?),
        ResolvingLangType::Function(it) => lang_types::LangType::Function(it.try_into()?),
        ResolvingLangType::Primitive(it) => lang_types::LangType::Primitive(it),
        ResolvingLangType::StopgapDictionary => lang_types::LangType::StopgapDictionary,
        ResolvingLangType::StopgapList => lang_types::LangType::StopgapList,
        ResolvingLangType::Action => lang_types::LangType::Action,
        ResolvingLangType::Anything => lang_types::LangType::Anything,
        ResolvingLangType::AnySignal => lang_types::LangType::AnySignal,
        ResolvingLangType::NeverContinues => lang_types::LangType::NeverContinues,
        ResolvingLangType::OneOf(it) => lang_types::LangType::OneOf(it.try_into()?),
        ResolvingLangType::BadValue => lang_types::LangType::BadValue,
    }.pipe(Ok)
  }
}

impl InstanceResolvingLangType {
  pub fn import_type(
    ctx: &mut ResolvingLangTypesContext,
    value: lang_types::InstanceLangType,
  ) -> anyhow::Result<Self> {
    Ok(InstanceResolvingLangType {
      type_id: value.type_id,
    })
  }
}
impl TryFrom<InstanceResolvingLangType> for lang_types::InstanceLangType {
  type Error = anyhow::Error;
  fn try_from(value: InstanceResolvingLangType) -> Result<Self, Self::Error> {
    Ok(lang_types::InstanceLangType {
      type_id: value.type_id,
    })
  }
}
impl FunctionResolvingLangType {
  pub fn import_type(
    ctx: &mut ResolvingLangTypesContext,
    value: lang_types::FunctionLangType,
  ) -> anyhow::Result<Self> {
    Ok(FunctionResolvingLangType {
      name: value.name.map(|it| -> Result<_, anyhow::Error> { Ok(it) }).transpose()?,
      params: value.params.into_iter().map(|it| Ok(ResolvingLangParameter::import_type(ctx, it)?)).collect::<Result<Vec<_>, anyhow::Error>>()?,
      return_type: LinkedResolvingLangType::import_type(ctx, value.return_type)?,
    })
  }
}
impl TryFrom<FunctionResolvingLangType> for lang_types::FunctionLangType {
  type Error = anyhow::Error;
  fn try_from(value: FunctionResolvingLangType) -> Result<Self, Self::Error> {
    Ok(lang_types::FunctionLangType {
      name: value.name.map(|it| it),
      params: value.params.into_iter().map(|it| Ok(it.try_into()?)).collect::<Result<Vec<lang_types::LangParameter>, anyhow::Error>>()?,
      return_type: value.return_type.try_into()?,
    })
  }
}
impl OneOfResolvingLangType {
  pub fn import_type(
    ctx: &mut ResolvingLangTypesContext,
    value: lang_types::OneOfLangType,
  ) -> anyhow::Result<Self> {
    Ok(OneOfResolvingLangType {
      options: value.options.into_iter().map(|it| Ok(LinkedResolvingLangType::import_type(ctx, it)?)).collect::<Result<Vec<_>, anyhow::Error>>()?,
    })
  }
}
impl TryFrom<OneOfResolvingLangType> for lang_types::OneOfLangType {
  type Error = anyhow::Error;
  fn try_from(value: OneOfResolvingLangType) -> Result<Self, Self::Error> {
    Ok(lang_types::OneOfLangType {
      options: value.options.into_iter().map(|it| Ok(it.try_into()?)).collect::<Result<Vec<lang_types::FallibleLangType>, anyhow::Error>>()?,
    })
  }
}

impl ResolvingLangParameter {
  pub fn import_type(
    ctx: &mut ResolvingLangTypesContext,
    value: lang_types::LangParameter,
  ) -> anyhow::Result<Self> {
    Ok(ResolvingLangParameter {
      name: value.name,
      value_type: LinkedResolvingLangType::import_type(ctx, value.value_type)?,
    })
  }
}
impl TryFrom<ResolvingLangParameter> for lang_types::LangParameter {
  type Error = anyhow::Error;
  fn try_from(value: ResolvingLangParameter) -> Result<Self, Self::Error> {
    Ok(lang_types::LangParameter {
      name: value.name,
      value_type: value.value_type.try_into()?,
    })
  }
}
