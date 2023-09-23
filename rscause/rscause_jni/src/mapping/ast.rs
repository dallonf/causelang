use anyhow::Result;
use jni::{
    objects::{JObject, JString},
    JNIEnv,
};
use rscause_compiler::ast_nodes::{self as ast, DeclarationNode};

pub trait JniToAstNode<T> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<T>;
}

impl<'local> JniToAstNode<ast::FileNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::FileNode> {
        let declarations = {
            let jni_declarations = env
                .call_method(&self, "getDeclarations", "()Ljava/util/List;", &[])?
                .l()?;
            let jni_declarations = env.get_list(&jni_declarations)?;

            let mut list = vec![];

            let mut iter = jni_declarations.iter(env)?;
            while let Some(jni_declaration) = iter.next(env)? {
                let declaration: DeclarationNode = jni_declaration.to_ast_node(env)?;
                list.push(declaration);
            }
            list
        };

        Ok(ast::FileNode { declarations })
    }
}

impl<'local> JniToAstNode<ast::DeclarationNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::DeclarationNode> {
        let class = env.get_object_class(&self)?;
        let class_name: JString = env
            .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
            .l()?
            .into();
        let class_name = env.get_string(&class_name)?.to_str()?;

        Ok(match class_name {
            "com/dallonf/ktcause/ast/FunctionNode" => {
                ast::DeclarationNode::Function(self.to_ast_node(env)?)
            }
        })
    }
}
