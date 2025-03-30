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


