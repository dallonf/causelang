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
      let class_name = env.get_string(&class_name)?;
      let class_name = class_name.to_str()?;

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
      let class_name = env.get_string(&class_name)?;
      let class_name = class_name.to_str()?;

      Ok(match class_name {
            "ImportNode" => {
                ast::DeclarationNode::Import(self.to_ast_node(env)?)
            },
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
      let class_name = env.get_string(&class_name)?;
      let class_name = class_name.to_str()?;

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
      let class_name = env.get_string(&class_name)?;
      let class_name = class_name.to_str()?;

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
      let class_name = env.get_string(&class_name)?;
      let class_name = class_name.to_str()?;

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
      let text = {
            let jni_string = env
                .call_method(&self, "getText", "()Ljava/lang/String;", &[])?
                .l()?;
            let jni_string = JString::from(jni_string);
            let jni_string = env.get_string(&jni_string)?;
            let value = jni_string.to_str()?.to_owned();
            Arc::new(value)
              };

      Ok(ast::IdentifierNode {
          text,
      })
    }
}
impl<'local> JniToAstNode<ast::IdentifierTypeReferenceNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::IdentifierTypeReferenceNode> {
      let identifier = {
            let jni_node = env
                .call_method(&self, "getIdentifier", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::IdentifierNode = jni_node.to_ast_node(env)?;
            node
      };

      Ok(ast::IdentifierTypeReferenceNode {
          identifier,
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
      let type_reference = {
            let jni_node = env
                .call_method(&self, "getTypeReference", "()Lcom/dallonf/ktcause/ast/TypeReferenceNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            if env.is_same_object(&jni_node, JObject::null())? {
                None
            } else {
                let node: ast::TypeReferenceNode = jni_node.to_ast_node(env)?;
                Some(Box::new(node))
            }
      };

      Ok(ast::FunctionSignatureParameterNode {
          name,
          type_reference,
      })
    }
}
impl<'local> JniToAstNode<ast::FunctionCallParameterNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::FunctionCallParameterNode> {
      let value = {
            let jni_node = env
                .call_method(&self, "getValue", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::ExpressionNode = jni_node.to_ast_node(env)?;
            Box::new(node)
      };

      Ok(ast::FunctionCallParameterNode {
          value,
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
      let path = {
            let jni_node = env
                .call_method(&self, "getPath", "()Lcom/dallonf/ktcause/ast/ImportPathNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::ImportPathNode = jni_node.to_ast_node(env)?;
            node
      };
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
          path,
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
      let source_name = {
            let jni_node = env
                .call_method(&self, "getSourceName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::IdentifierNode = jni_node.to_ast_node(env)?;
            node
      };
      let rename = {
            let jni_node = env
                .call_method(&self, "getRename", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            if env.is_same_object(&jni_node, JObject::null())? {
                None
            } else {
                let node: ast::IdentifierNode = jni_node.to_ast_node(env)?;
                Some(node)
            }
      };

      Ok(ast::ImportMappingNode {
          source_name,
          rename,
      })
    }
}
impl<'local> JniToAstNode<ast::FunctionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::FunctionNode> {
      let name = {
            let jni_node = env
                .call_method(&self, "getName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::IdentifierNode = jni_node.to_ast_node(env)?;
            node
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
      let body = {
            let jni_node = env
                .call_method(&self, "getBody", "()Lcom/dallonf/ktcause/ast/BodyNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::BodyNode = jni_node.to_ast_node(env)?;
            Box::new(node)
      };
      let return_type = {
            let jni_node = env
                .call_method(&self, "getReturnType", "()Lcom/dallonf/ktcause/ast/TypeReferenceNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            if env.is_same_object(&jni_node, JObject::null())? {
                None
            } else {
                let node: ast::TypeReferenceNode = jni_node.to_ast_node(env)?;
                Some(Box::new(node))
            }
      };

      Ok(ast::FunctionNode {
          name,
          params,
          body,
          return_type,
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
      let expression = {
            let jni_node = env
                .call_method(&self, "getExpression", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::ExpressionNode = jni_node.to_ast_node(env)?;
            Box::new(node)
      };

      Ok(ast::ExpressionStatementNode {
          expression,
      })
    }
}
impl<'local> JniToAstNode<ast::CauseExpressionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::CauseExpressionNode> {
      let signal = {
            let jni_node = env
                .call_method(&self, "getSignal", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::ExpressionNode = jni_node.to_ast_node(env)?;
            Box::new(node)
      };

      Ok(ast::CauseExpressionNode {
          signal,
      })
    }
}
impl<'local> JniToAstNode<ast::CallExpressionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::CallExpressionNode> {
      let callee = {
            let jni_node = env
                .call_method(&self, "getCallee", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::ExpressionNode = jni_node.to_ast_node(env)?;
            Box::new(node)
      };
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
          callee,
          parameters,
      })
    }
}
impl<'local> JniToAstNode<ast::IdentifierExpressionNode> for JObject<'local> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<ast::IdentifierExpressionNode> {
      let identifier = {
            let jni_node = env
                .call_method(&self, "getIdentifier", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            let node: ast::IdentifierNode = jni_node.to_ast_node(env)?;
            node
      };

      Ok(ast::IdentifierExpressionNode {
          identifier,
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
