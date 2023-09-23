use jni::{
    errors::Result,
    objects::{JObject, JValue},
    JNIEnv,
};
use rscause_compiler::ast_nodes::{self as ast, DeclarationNode};

pub trait JniToAstNode<T> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<T>;
}

impl<'local> JniToAstNode<ast::FileNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::FileNode> {
        let jni_declarations = env
            .call_method(&self, "getDeclarations", "()Ljava/util/List;", &[])?
            .l()?;
        let count = env
            .call_method(&jni_declarations, "size", "()I", &[])?
            .i()?;

        let mut declarations = vec![];

        for i in 0..count {
            let jni_declaration = env
                .call_method(
                    &jni_declarations,
                    "get",
                    "(I)Ljava/lang/Object;",
                    &[JValue::Int(i)],
                )?
                .l()?;

            let declaration: DeclarationNode = jni_declaration.to_ast_node(env)?;
            declarations.push(declaration);
        }

        Ok(ast::FileNode { declarations })
    }
}

impl<'local> JniToAstNode<ast::DeclarationNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::DeclarationNode> {
        todo!()
    }
}
