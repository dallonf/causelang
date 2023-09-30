use std::sync::Arc;

use super::{JniInto, FromJni};
use anyhow::Result;
use jni::{
    objects::{JObject, JString},
    JNIEnv,
};
use rscause_compiler::ast;

impl FromJni for ast::TypeReferenceNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let class = env.get_object_class(value)?;
      let class_name: JString = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .into();
      let class_name: String = class_name.jni_into(env)?;
      Ok(match class_name.as_str() {
            "IdentifierTypeReferenceNode" => {
                ast::TypeReferenceNode::Identifier(value.jni_into(env)?)
            },
          _ => panic!("Unknown class name for TypeReferenceNode: {}", class_name)
      })
    }
}
impl FromJni for ast::DeclarationNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let class = env.get_object_class(value)?;
      let class_name: JString = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .into();
      let class_name: String = class_name.jni_into(env)?;
      Ok(match class_name.as_str() {
            "ImportNode" => {
                ast::DeclarationNode::Import(value.jni_into(env)?)
            },
            "FunctionNode" => {
                ast::DeclarationNode::Function(value.jni_into(env)?)
            },
          _ => panic!("Unknown class name for DeclarationNode: {}", class_name)
      })
    }
}
impl FromJni for ast::BodyNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let class = env.get_object_class(value)?;
      let class_name: JString = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .into();
      let class_name: String = class_name.jni_into(env)?;
      Ok(match class_name.as_str() {
            "BlockBodyNode" => {
                ast::BodyNode::Block(value.jni_into(env)?)
            },
          _ => panic!("Unknown class name for BodyNode: {}", class_name)
      })
    }
}
impl FromJni for ast::StatementNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let class = env.get_object_class(value)?;
      let class_name: JString = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .into();
      let class_name: String = class_name.jni_into(env)?;
      Ok(match class_name.as_str() {
            "ExpressionStatementNode" => {
                ast::StatementNode::Expression(value.jni_into(env)?)
            },
          _ => panic!("Unknown class name for StatementNode: {}", class_name)
      })
    }
}
impl FromJni for ast::ExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let class = env.get_object_class(value)?;
      let class_name: JString = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .into();
      let class_name: String = class_name.jni_into(env)?;
      Ok(match class_name.as_str() {
            "CauseExpressionNode" => {
                ast::ExpressionNode::Cause(value.jni_into(env)?)
            },
            "CallExpressionNode" => {
                ast::ExpressionNode::Call(value.jni_into(env)?)
            },
            "IdentifierExpressionNode" => {
                ast::ExpressionNode::Identifier(value.jni_into(env)?)
            },
            "StringLiteralExpressionNode" => {
                ast::ExpressionNode::StringLiteral(value.jni_into(env)?)
            },
          _ => panic!("Unknown class name for ExpressionNode: {}", class_name)
      })
    }
}

impl FromJni for ast::IdentifierNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let text: Arc<String> = {
        let jni_node = env
          .call_method(value, "getText", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IdentifierNode {
          breadcrumbs,
          text,
      })
    }
}
impl FromJni for ast::IdentifierTypeReferenceNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let identifier: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getIdentifier", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IdentifierTypeReferenceNode {
          breadcrumbs,
          identifier,
      })
    }
}
impl FromJni for ast::FunctionSignatureParameterNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let name: Arc<String> = {
        let jni_node = env
          .call_method(value, "getName", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let type_reference: Option<ast::TypeReferenceNode> = {
        let jni_node = env
          .call_method(value, "getTypeReference", "()Lcom/dallonf/ktcause/ast/TypeReferenceNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FunctionSignatureParameterNode {
          breadcrumbs,
          name,
          type_reference,
      })
    }
}
impl FromJni for ast::FunctionCallParameterNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let value: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getValue", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FunctionCallParameterNode {
          breadcrumbs,
          value,
      })
    }
}
impl FromJni for ast::FileNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let declarations: Vec<ast::DeclarationNode> = {
        let jni_node = env
          .call_method(value, "getDeclarations", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FileNode {
          breadcrumbs,
          declarations,
      })
    }
}
impl FromJni for ast::ImportNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let path: Arc<ast::ImportPathNode> = {
        let jni_node = env
          .call_method(value, "getPath", "()Lcom/dallonf/ktcause/ast/ImportPathNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let mappings: Vec<Arc<ast::ImportMappingNode>> = {
        let jni_node = env
          .call_method(value, "getMappings", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ImportNode {
          breadcrumbs,
          path,
          mappings,
      })
    }
}
impl FromJni for ast::ImportPathNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let path: Arc<String> = {
        let jni_node = env
          .call_method(value, "getPath", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ImportPathNode {
          breadcrumbs,
          path,
      })
    }
}
impl FromJni for ast::ImportMappingNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let source_name: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getSourceName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let rename: Option<Arc<ast::IdentifierNode>> = {
        let jni_node = env
          .call_method(value, "getRename", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ImportMappingNode {
          breadcrumbs,
          source_name,
          rename,
      })
    }
}
impl FromJni for ast::FunctionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let name: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let params: Vec<Arc<ast::FunctionSignatureParameterNode>> = {
        let jni_node = env
          .call_method(value, "getParams", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let body: ast::BodyNode = {
        let jni_node = env
          .call_method(value, "getBody", "()Lcom/dallonf/ktcause/ast/BodyNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let return_type: Option<ast::TypeReferenceNode> = {
        let jni_node = env
          .call_method(value, "getReturnType", "()Lcom/dallonf/ktcause/ast/TypeReferenceNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FunctionNode {
          breadcrumbs,
          name,
          params,
          body,
          return_type,
      })
    }
}
impl FromJni for ast::BlockBodyNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let statements: Vec<ast::StatementNode> = {
        let jni_node = env
          .call_method(value, "getStatements", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::BlockBodyNode {
          breadcrumbs,
          statements,
      })
    }
}
impl FromJni for ast::ExpressionStatementNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let expression: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getExpression", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ExpressionStatementNode {
          breadcrumbs,
          expression,
      })
    }
}
impl FromJni for ast::CauseExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let signal: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getSignal", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::CauseExpressionNode {
          breadcrumbs,
          signal,
      })
    }
}
impl FromJni for ast::CallExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let callee: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getCallee", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let parameters: Vec<Arc<ast::FunctionCallParameterNode>> = {
        let jni_node = env
          .call_method(value, "getParameters", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::CallExpressionNode {
          breadcrumbs,
          callee,
          parameters,
      })
    }
}
impl FromJni for ast::IdentifierExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let identifier: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getIdentifier", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IdentifierExpressionNode {
          breadcrumbs,
          identifier,
      })
    }
}
impl FromJni for ast::StringLiteralExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?;
      let breadcrumbs = env
        .call_method(info, "getBreadcrumbs", "()Lcom/dallonf/ktcause/ast/Breadcrumbs;", &[])?
        .l()?
        .into();
      let text: Arc<String> = {
        let jni_node = env
          .call_method(value, "getText", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::StringLiteralExpressionNode {
          breadcrumbs,
          text,
      })
    }
}
