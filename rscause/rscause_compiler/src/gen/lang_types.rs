#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum LangType {
  TypeReference(FallibleLangType),
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

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct InstanceLangType {
  pub type_id: Arc<CanonicalLangTypeId>,
}
impl From<InstanceLangType> for LangType {
  fn from(value: InstanceLangType) -> Self {
    Self::Instance(value)
  }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct FunctionLangType {
  pub name: Option<Arc<String>>,
  pub params: Vec<LangParameter>,
  pub return_type: FallibleLangType,
}
impl From<FunctionLangType> for LangType {
  fn from(value: FunctionLangType) -> Self {
    Self::Function(value)
  }
}
#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct OneOfLangType {
  pub options: Vec<FallibleLangType>,
}
impl From<OneOfLangType> for LangType {
  fn from(value: OneOfLangType) -> Self {
    Self::OneOf(value)
  }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct LangParameter {
    pub name: Arc<String>,
    pub value_type: FallibleLangType,
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum CanonicalLangType {
    Object(ObjectCanonicalLangType),
    Signal(SignalCanonicalLangType),
}
impl CanonicalLangType {
    pub fn type_id(&self) -> CanonicalLangTypeId {
        match self {
            Self::Object(object) => object.type_id.clone(),
            Self::Signal(signal) => signal.type_id.clone(),
        }
    }
    pub fn fields(&self) -> Vec<CanonicalTypeField> {
        match self {
            Self::Object(object) => object.fields.clone(),
            Self::Signal(signal) => signal.fields.clone(),
        }
    }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct CanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: FallibleLangType,
}

fn assert_uniqueness_matches(
    struct_name: &str,
    type_id: &CanonicalLangTypeId,
    fields: &[CanonicalTypeField],
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
pub struct ObjectCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<CanonicalTypeField>,
}
impl ObjectCanonicalLangType {
    pub fn new(type_id: CanonicalLangTypeId, fields: Vec<CanonicalTypeField>) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Object {
            panic!("ObjectCanonicalLangType::new called with non-object type_id");
        }
        assert_uniqueness_matches("ObjectCanonicalLangType", &type_id, &fields);
        Self { type_id, fields }
    }
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }
    pub fn fields(&self) -> &[CanonicalTypeField] {
        &self.fields
    }
}
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct SignalCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<CanonicalTypeField>,
    pub result: FallibleLangType,
}

impl SignalCanonicalLangType {
    pub fn new(
        type_id: CanonicalLangTypeId,
        fields: Vec<CanonicalTypeField>,
        result: FallibleLangType,
    ) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Signal {
            panic!("SignalCanonicalLangType::new called with non-signal type_id");
        }
        assert_uniqueness_matches("SignalCanonicalLangType", &type_id, &fields);
        Self {
            type_id,
            fields,
            result,
        }
    }
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }
    pub fn fields(&self) -> &[CanonicalTypeField] {
        &self.fields
    }
    pub fn result(&self) -> &FallibleLangType {
        &self.result
    }
}
