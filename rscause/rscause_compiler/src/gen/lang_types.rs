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

#[derive(Debug, Clone, Eq, PartialEq, PartialOrd, Ord, Hash)]
pub struct CanonicalLangTypeId {
    pub path: Arc<String>,
    pub parent_name: Option<Arc<String>>,
    pub name: Option<Arc<String>>,
    pub number: u32,
    pub category: CanonicalLangTypeCategory,
    pub is_unique: bool,
}
impl ToString for CanonicalLangTypeId {
    fn to_string(&self) -> String {
        let name_with_fallback = self
            .name
            .as_ref()
            .map(|it| it.to_string())
            .unwrap_or_else(|| "$?".to_string());
        let full_name = match &self.parent_name {
            Some(parent_name) => {
                format! {"{}.{}", parent_name, name_with_fallback}
            }
            None => name_with_fallback,
        };

        let number_if_applicable = if self.number == 0 {
            "".into()
        } else {
            format!("_{}", self.number)
        };

        let category = match self.category {
            CanonicalLangTypeCategory::Object => "O",
            CanonicalLangTypeCategory::Signal => "S",
        };

        format!(
            "{}:{}:{}{}{}",
            &self.path,
            category,
            full_name,
            number_if_applicable,
            if self.is_unique { "!" } else { "" }
        )
    }
}
impl FromStr for CanonicalLangTypeId {
    type Err = anyhow::Error;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let original = s;
        let (path, s) = s
            .split_once(':')
            .ok_or(anyhow!("No : in type id: {}", original))?;
        let (category, s) = s
            .split_once(':')
            .ok_or(anyhow!("No : after category in type id: {}", original))?;

        let unique = s.ends_with('!');
        let s = if unique { s.trim_end_matches('!') } else { s };

        let (full_name, number_str) = s
            .split_once('_')
            .map(|(a, b)| (a, Some(b)))
            .unwrap_or((s, None));

        let number = number_str
            .map(|it| it.parse::<u32>())
            .transpose()
            .map_err(|err| anyhow!("Failed to parse number in type id: {}", err))?
            .unwrap_or(0);

        let (parent_name, name_with_fallback) = full_name
            .split_once('.')
            .map(|(a, b)| (Some(a), b))
            .unwrap_or((None, full_name));

        let name = if name_with_fallback == "$?" {
            None
        } else {
            Some(name_with_fallback)
        };

        let category = match category {
            "O" => CanonicalLangTypeCategory::Object,
            "S" => CanonicalLangTypeCategory::Signal,
            _ => {
                return Err(anyhow!(
                    "Invalid category in type id: {} (expected O or S)",
                    category
                ))
            }
        };

        Ok(CanonicalLangTypeId {
            path: path.to_string().into(),
            parent_name: parent_name.map(|it| it.to_string().into()),
            name: name.map(|it| it.to_string().into()),
            number,
            category,
            is_unique: unique,
        })
    }
}
impl Serialize for CanonicalLangTypeId {
    fn serialize<S: serde::Serializer>(&self, serializer: S) -> Result<S::Ok, S::Error> {
        serializer.serialize_str(&self.to_string())
    }
}
impl<'de> Deserialize<'de> for CanonicalLangTypeId {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: serde::Deserializer<'de>,
    {
        let string = String::deserialize(deserializer)?;
        string
            .parse::<CanonicalLangTypeId>()
            .map_err(de::Error::custom)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, PartialOrd, Ord, Serialize, Deserialize)]
pub enum CanonicalLangTypeCategory {
    Object,
    Signal,
}
