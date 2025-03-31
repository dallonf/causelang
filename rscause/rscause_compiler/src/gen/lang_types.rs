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
