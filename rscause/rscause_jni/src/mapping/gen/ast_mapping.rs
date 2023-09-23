use std::sync::Arc;

use super::super::ast::JniToAstNode;
use anyhow::Result;
use jni::{
    objects::{JObject, JString},
    JNIEnv,
};
use rscause_compiler::ast_nodes as ast;

impl<'local> JniToAstNode<ast::TypeReferenceNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::TypeReferenceNode> {
      let class = env.get_object_class(&self)?;
      let class_name: JString = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .into();
      let class_name = env.get_string(&class_name)?.to_str()?;

      Ok(match class_name {
            "IdentifierTypeReferenceNode" => {
                ast::TypeReferenceNode::Identifier(self.to_ast_node(env)?)
            },
          _ => panic!("Unknown class name for TypeReferenceNode: {}", class_name)
      })
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
            "FunctionNode" => {
                ast::DeclarationNode::Function(self.to_ast_node(env)?)
            },
          _ => panic!("Unknown class name for DeclarationNode: {}", class_name)
      })
    }
}
impl<'local> JniToAstNode<ast::BodyNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::BodyNode> {
      let class = env.get_object_class(&self)?;
      let class_name: JString = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .into();
      let class_name = env.get_string(&class_name)?.to_str()?;

      Ok(match class_name {
            "BlockBodyNode" => {
                ast::BodyNode::Block(self.to_ast_node(env)?)
            },
          _ => panic!("Unknown class name for BodyNode: {}", class_name)
      })
    }
}
impl<'local> JniToAstNode<ast::StatementNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::StatementNode> {
      let class = env.get_object_class(&self)?;
      let class_name: JString = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .into();
      let class_name = env.get_string(&class_name)?.to_str()?;

      Ok(match class_name {
            "ExpressionStatementNode" => {
                ast::StatementNode::Expression(self.to_ast_node(env)?)
            },
          _ => panic!("Unknown class name for StatementNode: {}", class_name)
      })
    }
}
impl<'local> JniToAstNode<ast::ExpressionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::ExpressionNode> {
      let class = env.get_object_class(&self)?;
      let class_name: JString = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .into();
      let class_name = env.get_string(&class_name)?.to_str()?;

      Ok(match class_name {
            "CauseExpressionNode" => {
                ast::ExpressionNode::Cause(self.to_ast_node(env)?)
            },
            "CallExpressionNode" => {
                ast::ExpressionNode::Call(self.to_ast_node(env)?)
            },
            "IdentifierExpressionNode" => {
                ast::ExpressionNode::Identifier(self.to_ast_node(env)?)
            },
            "StringLiteralExpressionNode" => {
                ast::ExpressionNode::StringLiteral(self.to_ast_node(env)?)
            },
          _ => panic!("Unknown class name for ExpressionNode: {}", class_name)
      })
    }
}

impl<'local> JniToAstNode<ast::IdentifierNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::IdentifierNode> {
      let value = {
            let jni_string = env
                .call_method(&self, "getValue", "()Ljava/lang/String;", &[])?
                .l()?;
            let jni_string = JString::from(jni_string);
            let jni_string = env.get_string(&jni_string)?;
            let value = jni_string.to_str()?.to_owned();
            Arc::new(value)
              };

      Ok(ast::IdentifierNode {
          value,
      })
    }
}
impl<'local> JniToAstNode<ast::IdentifierTypeReferenceNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::IdentifierTypeReferenceNode> {

      Ok(ast::IdentifierTypeReferenceNode {
      })
    }
}
impl<'local> JniToAstNode<ast::FunctionSignatureParameterNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::FunctionSignatureParameterNode> {
      let name = {
            let jni_string = env
                .call_method(&self, "getName", "()Ljava/lang/String;", &[])?
                .l()?;
            let jni_string = JString::from(jni_string);
            let jni_string = env.get_string(&jni_string)?;
            let value = jni_string.to_str()?.to_owned();
            Arc::new(value)
              };

      Ok(ast::FunctionSignatureParameterNode {
          name,
      })
    }
}
impl<'local> JniToAstNode<ast::FunctionCallParameterNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::FunctionCallParameterNode> {

      Ok(ast::FunctionCallParameterNode {
      })
    }
}
impl<'local> JniToAstNode<ast::FileNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::FileNode> {
      let declarations = {
          let jni_list = env
                .call_method(&self, "getDeclarations", "()Ljava/util/List;", &[])?
                .l()?;
          let jni_list = env.get_list(&jni_list)?;
          let mut list = vec![];
          let mut iter = jni_list.iter(env)?;
          while let Some(jni_item) = iter.next(env)? {
              let node: ast::DeclarationNode = jni_item.to_ast_node(env)?;
              list.push(node);
          }
          list
      };

      Ok(ast::FileNode {
          declarations,
      })
    }
}
impl<'local> JniToAstNode<ast::ImportNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::ImportNode> {
      let mappings = {
          let jni_list = env
                .call_method(&self, "getMappings", "()Ljava/util/List;", &[])?
                .l()?;
          let jni_list = env.get_list(&jni_list)?;
          let mut list = vec![];
          let mut iter = jni_list.iter(env)?;
          while let Some(jni_item) = iter.next(env)? {
              let node: ast::ImportMappingNode = jni_item.to_ast_node(env)?;
              list.push(node);
          }
          list
      };

      Ok(ast::ImportNode {
          mappings,
      })
    }
}
impl<'local> JniToAstNode<ast::ImportPathNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::ImportPathNode> {
      let path = {
            let jni_string = env
                .call_method(&self, "getPath", "()Ljava/lang/String;", &[])?
                .l()?;
            let jni_string = JString::from(jni_string);
            let jni_string = env.get_string(&jni_string)?;
            let value = jni_string.to_str()?.to_owned();
            Arc::new(value)
              };

      Ok(ast::ImportPathNode {
          path,
      })
    }
}
impl<'local> JniToAstNode<ast::ImportMappingNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::ImportMappingNode> {

      Ok(ast::ImportMappingNode {
      })
    }
}
impl<'local> JniToAstNode<ast::FunctionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::FunctionNode> {
      let name = {
            let jni_string = env
                .call_method(&self, "getName", "()Ljava/lang/String;", &[])?
                .l()?;
            let jni_string = JString::from(jni_string);
            let jni_string = env.get_string(&jni_string)?;
            let value = jni_string.to_str()?.to_owned();
            Arc::new(value)
              };
      let params = {
          let jni_list = env
                .call_method(&self, "getParams", "()Ljava/util/List;", &[])?
                .l()?;
          let jni_list = env.get_list(&jni_list)?;
          let mut list = vec![];
          let mut iter = jni_list.iter(env)?;
          while let Some(jni_item) = iter.next(env)? {
              let node: ast::FunctionSignatureParameterNode = jni_item.to_ast_node(env)?;
              list.push(node);
          }
          list
      };

      Ok(ast::FunctionNode {
          name,
          params,
      })
    }
}
impl<'local> JniToAstNode<ast::BlockBodyNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::BlockBodyNode> {
      let statements = {
          let jni_list = env
                .call_method(&self, "getStatements", "()Ljava/util/List;", &[])?
                .l()?;
          let jni_list = env.get_list(&jni_list)?;
          let mut list = vec![];
          let mut iter = jni_list.iter(env)?;
          while let Some(jni_item) = iter.next(env)? {
              let node: ast::StatementNode = jni_item.to_ast_node(env)?;
              list.push(node);
          }
          list
      };

      Ok(ast::BlockBodyNode {
          statements,
      })
    }
}
impl<'local> JniToAstNode<ast::ExpressionStatementNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::ExpressionStatementNode> {

      Ok(ast::ExpressionStatementNode {
      })
    }
}
impl<'local> JniToAstNode<ast::CauseExpressionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::CauseExpressionNode> {

      Ok(ast::CauseExpressionNode {
      })
    }
}
impl<'local> JniToAstNode<ast::CallExpressionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::CallExpressionNode> {
      let parameters = {
          let jni_list = env
                .call_method(&self, "getParameters", "()Ljava/util/List;", &[])?
                .l()?;
          let jni_list = env.get_list(&jni_list)?;
          let mut list = vec![];
          let mut iter = jni_list.iter(env)?;
          while let Some(jni_item) = iter.next(env)? {
              let node: ast::FunctionCallParameterNode = jni_item.to_ast_node(env)?;
              list.push(node);
          }
          list
      };

      Ok(ast::CallExpressionNode {
          parameters,
      })
    }
}
impl<'local> JniToAstNode<ast::IdentifierExpressionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::IdentifierExpressionNode> {

      Ok(ast::IdentifierExpressionNode {
      })
    }
}
impl<'local> JniToAstNode<ast::StringLiteralExpressionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::StringLiteralExpressionNode> {
      let text = {
            let jni_string = env
                .call_method(&self, "getText", "()Ljava/lang/String;", &[])?
                .l()?;
            let jni_string = JString::from(jni_string);
            let jni_string = env.get_string(&jni_string)?;
            let value = jni_string.to_str()?.to_owned();
            Arc::new(value)
              };

      Ok(ast::StringLiteralExpressionNode {
          text,
      })
    }
}
