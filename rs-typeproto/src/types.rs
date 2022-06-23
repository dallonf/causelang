// #[derive(Debug, Clone, PartialEq, Eq)]
// struct LangFunctionType {
//     name: String,
//     // TODO: params,
//     return_type
// }

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum LangPrimitiveType {
    String,
    Integer,
    Float,
    Action,
}
