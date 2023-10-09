pub static J_BREADCRUMB_NAMES: &[&str] = &[
    "text",
    "identifier",
    "name",
    "typeReference",
    "value",
    "declarations",
    "path",
    "mappings",
    "path",
    "sourceName",
    "rename",
    "name",
    "params",
    "body",
    "returnType",
    "statements",
    "expression",
    "signal",
    "callee",
    "parameters",
    "identifier",
    "text",
];

impl FromJni for ast::TypeReferenceNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "category TypeReferenceNode");
      let class = env.get_object_class(value)?;
      let class_name: String = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .jni_into(env)?;
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
      noisy_log(env, "category DeclarationNode");
      let class = env.get_object_class(value)?;
      let class_name: String = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .jni_into(env)?;
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
      noisy_log(env, "category BodyNode");
      let class = env.get_object_class(value)?;
      let class_name: String = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .jni_into(env)?;
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
      noisy_log(env, "category StatementNode");
      let class = env.get_object_class(value)?;
      let class_name: String = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .jni_into(env)?;
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
      noisy_log(env, "category ExpressionNode");
      let class = env.get_object_class(value)?;
      let class_name: String = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .jni_into(env)?;
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
      noisy_log(env, "node IdentifierNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let text: Arc<String> = {
        let jni_node = env
          .call_method(value, "getText", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IdentifierNode {
          info: jni_info,
          text,
      })
    }
}
impl FromJni for ast::IdentifierTypeReferenceNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node IdentifierTypeReferenceNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let identifier: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getIdentifier", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IdentifierTypeReferenceNode {
          info: jni_info,
          identifier,
      })
    }
}
impl FromJni for ast::FunctionSignatureParameterNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node FunctionSignatureParameterNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let name: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
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
          info: jni_info,
          name,
          type_reference,
      })
    }
}
impl FromJni for ast::FunctionCallParameterNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node FunctionCallParameterNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let value: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getValue", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FunctionCallParameterNode {
          info: jni_info,
          value,
      })
    }
}
impl FromJni for ast::FileNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node FileNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let declarations: Vec<ast::DeclarationNode> = {
        let jni_node = env
          .call_method(value, "getDeclarations", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FileNode {
          info: jni_info,
          declarations,
      })
    }
}
impl FromJni for ast::ImportNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node ImportNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
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
          info: jni_info,
          path,
          mappings,
      })
    }
}
impl FromJni for ast::ImportPathNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node ImportPathNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let path: Arc<String> = {
        let jni_node = env
          .call_method(value, "getPath", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ImportPathNode {
          info: jni_info,
          path,
      })
    }
}
impl FromJni for ast::ImportMappingNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node ImportMappingNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
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
          info: jni_info,
          source_name,
          rename,
      })
    }
}
impl FromJni for ast::FunctionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node FunctionNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
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
          info: jni_info,
          name,
          params,
          body,
          return_type,
      })
    }
}
impl FromJni for ast::BlockBodyNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node BlockBodyNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let statements: Vec<ast::StatementNode> = {
        let jni_node = env
          .call_method(value, "getStatements", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::BlockBodyNode {
          info: jni_info,
          statements,
      })
    }
}
impl FromJni for ast::ExpressionStatementNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node ExpressionStatementNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let expression: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getExpression", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ExpressionStatementNode {
          info: jni_info,
          expression,
      })
    }
}
impl FromJni for ast::CauseExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node CauseExpressionNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let signal: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getSignal", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::CauseExpressionNode {
          info: jni_info,
          signal,
      })
    }
}
impl FromJni for ast::CallExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node CallExpressionNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
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
          info: jni_info,
          callee,
          parameters,
      })
    }
}
impl FromJni for ast::IdentifierExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node IdentifierExpressionNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let identifier: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getIdentifier", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IdentifierExpressionNode {
          info: jni_info,
          identifier,
      })
    }
}
impl FromJni for ast::StringLiteralExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node StringLiteralExpressionNode");
      let jni_info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let text: Arc<String> = {
        let jni_node = env
          .call_method(value, "getText", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::StringLiteralExpressionNode {
          info: jni_info,
          text,
      })
    }
}
