pub static J_BREADCRUMB_NAMES: &[&str] = &[
    "text",
    "identifier",
    "name",
    "typeReference",
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
    "name",
    "typeReference",
    "value",
    "isVariable",
    "statements",
    "statement",
    "expression",
    "declaration",
    "withValue",
    "branches",
    "condition",
    "body",
    "pattern",
    "body",
    "body",
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
            "NamedValueNode" => {
                ast::DeclarationNode::NamedValue(value.jni_into(env)?)
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
            "SingleStatementBodyNode" => {
                ast::BodyNode::SingleStatement(value.jni_into(env)?)
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
            "DeclarationStatementNode" => {
                ast::StatementNode::Declaration(value.jni_into(env)?)
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
            "BranchExpressionNode" => {
                ast::ExpressionNode::Branch(value.jni_into(env)?)
            },
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
impl FromJni for ast::BranchOptionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "category BranchOptionNode");
      let class = env.get_object_class(value)?;
      let class_name: String = env
          .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
          .l()?
          .jni_into(env)?;
      Ok(match class_name.as_str() {
            "IfBranchOptionNode" => {
                ast::BranchOptionNode::If(value.jni_into(env)?)
            },
            "IsBranchOptionNode" => {
                ast::BranchOptionNode::Is(value.jni_into(env)?)
            },
            "ElseBranchOptionNode" => {
                ast::BranchOptionNode::Else(value.jni_into(env)?)
            },
          _ => panic!("Unknown class name for BranchOptionNode: {}", class_name)
      })
    }
}

impl FromJni for ast::IdentifierNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node IdentifierNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let text_value: Arc<String> = {
        let jni_node = env
          .call_method(value, "getText", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IdentifierNode {
          info,
          text: text_value,
      })
    }
}
impl FromJni for ast::IdentifierTypeReferenceNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node IdentifierTypeReferenceNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let identifier_value: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getIdentifier", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IdentifierTypeReferenceNode {
          info,
          identifier: identifier_value,
      })
    }
}
impl FromJni for ast::PatternNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node PatternNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let name_value: Option<Arc<ast::IdentifierNode>> = {
        let jni_node = env
          .call_method(value, "getName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let type_reference_value: ast::TypeReferenceNode = {
        let jni_node = env
          .call_method(value, "getTypeReference", "()Lcom/dallonf/ktcause/ast/TypeReferenceNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::PatternNode {
          info,
          name: name_value,
          type_reference: type_reference_value,
      })
    }
}
impl FromJni for ast::FunctionSignatureParameterNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node FunctionSignatureParameterNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let name_value: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let type_reference_value: Option<ast::TypeReferenceNode> = {
        let jni_node = env
          .call_method(value, "getTypeReference", "()Lcom/dallonf/ktcause/ast/TypeReferenceNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FunctionSignatureParameterNode {
          info,
          name: name_value,
          type_reference: type_reference_value,
      })
    }
}
impl FromJni for ast::FunctionCallParameterNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node FunctionCallParameterNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let value_value: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getValue", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FunctionCallParameterNode {
          info,
          value: value_value,
      })
    }
}
impl FromJni for ast::FileNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node FileNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let declarations_value: Vec<ast::DeclarationNode> = {
        let jni_node = env
          .call_method(value, "getDeclarations", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FileNode {
          info,
          declarations: declarations_value,
      })
    }
}
impl FromJni for ast::ImportNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node ImportNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let path_value: Arc<ast::ImportPathNode> = {
        let jni_node = env
          .call_method(value, "getPath", "()Lcom/dallonf/ktcause/ast/ImportPathNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let mappings_value: Vec<Arc<ast::ImportMappingNode>> = {
        let jni_node = env
          .call_method(value, "getMappings", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ImportNode {
          info,
          path: path_value,
          mappings: mappings_value,
      })
    }
}
impl FromJni for ast::ImportPathNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node ImportPathNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let path_value: Arc<String> = {
        let jni_node = env
          .call_method(value, "getPath", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ImportPathNode {
          info,
          path: path_value,
      })
    }
}
impl FromJni for ast::ImportMappingNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node ImportMappingNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let source_name_value: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getSourceName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let rename_value: Option<Arc<ast::IdentifierNode>> = {
        let jni_node = env
          .call_method(value, "getRename", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ImportMappingNode {
          info,
          source_name: source_name_value,
          rename: rename_value,
      })
    }
}
impl FromJni for ast::FunctionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node FunctionNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let name_value: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let params_value: Vec<Arc<ast::FunctionSignatureParameterNode>> = {
        let jni_node = env
          .call_method(value, "getParams", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let body_value: ast::BodyNode = {
        let jni_node = env
          .call_method(value, "getBody", "()Lcom/dallonf/ktcause/ast/BodyNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let return_type_value: Option<ast::TypeReferenceNode> = {
        let jni_node = env
          .call_method(value, "getReturnType", "()Lcom/dallonf/ktcause/ast/TypeReferenceNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::FunctionNode {
          info,
          name: name_value,
          params: params_value,
          body: body_value,
          return_type: return_type_value,
      })
    }
}
impl FromJni for ast::NamedValueNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node NamedValueNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let name_value: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getName", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let type_reference_value: Option<ast::TypeReferenceNode> = {
        let jni_node = env
          .call_method(value, "getTypeReference", "()Lcom/dallonf/ktcause/ast/TypeReferenceNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let value_value: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getValue", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let is_variable_value: bool = {
        let jni_node = env
          .call_method(value, "getIsVariable", "()Z", &[])?
          .z()?;
        jni_node
      };

      Ok(ast::NamedValueNode {
          info,
          name: name_value,
          type_reference: type_reference_value,
          value: value_value,
          is_variable: is_variable_value,
      })
    }
}
impl FromJni for ast::BlockBodyNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node BlockBodyNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let statements_value: Vec<ast::StatementNode> = {
        let jni_node = env
          .call_method(value, "getStatements", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::BlockBodyNode {
          info,
          statements: statements_value,
      })
    }
}
impl FromJni for ast::SingleStatementBodyNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node SingleStatementBodyNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let statement_value: ast::StatementNode = {
        let jni_node = env
          .call_method(value, "getStatement", "()Lcom/dallonf/ktcause/ast/StatementNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::SingleStatementBodyNode {
          info,
          statement: statement_value,
      })
    }
}
impl FromJni for ast::ExpressionStatementNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node ExpressionStatementNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let expression_value: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getExpression", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ExpressionStatementNode {
          info,
          expression: expression_value,
      })
    }
}
impl FromJni for ast::DeclarationStatementNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node DeclarationStatementNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let declaration_value: ast::DeclarationNode = {
        let jni_node = env
          .call_method(value, "getDeclaration", "()Lcom/dallonf/ktcause/ast/DeclarationNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::DeclarationStatementNode {
          info,
          declaration: declaration_value,
      })
    }
}
impl FromJni for ast::BranchExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node BranchExpressionNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let with_value_value: Option<ast::ExpressionNode> = {
        let jni_node = env
          .call_method(value, "getWithValue", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let branches_value: Vec<ast::BranchOptionNode> = {
        let jni_node = env
          .call_method(value, "getBranches", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::BranchExpressionNode {
          info,
          with_value: with_value_value,
          branches: branches_value,
      })
    }
}
impl FromJni for ast::IfBranchOptionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node IfBranchOptionNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let condition_value: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getCondition", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let body_value: ast::BodyNode = {
        let jni_node = env
          .call_method(value, "getBody", "()Lcom/dallonf/ktcause/ast/BodyNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IfBranchOptionNode {
          info,
          condition: condition_value,
          body: body_value,
      })
    }
}
impl FromJni for ast::IsBranchOptionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node IsBranchOptionNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let pattern_value: Arc<ast::PatternNode> = {
        let jni_node = env
          .call_method(value, "getPattern", "()Lcom/dallonf/ktcause/ast/PatternNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let body_value: ast::BodyNode = {
        let jni_node = env
          .call_method(value, "getBody", "()Lcom/dallonf/ktcause/ast/BodyNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IsBranchOptionNode {
          info,
          pattern: pattern_value,
          body: body_value,
      })
    }
}
impl FromJni for ast::ElseBranchOptionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node ElseBranchOptionNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let body_value: ast::BodyNode = {
        let jni_node = env
          .call_method(value, "getBody", "()Lcom/dallonf/ktcause/ast/BodyNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::ElseBranchOptionNode {
          info,
          body: body_value,
      })
    }
}
impl FromJni for ast::CauseExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node CauseExpressionNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let signal_value: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getSignal", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::CauseExpressionNode {
          info,
          signal: signal_value,
      })
    }
}
impl FromJni for ast::CallExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node CallExpressionNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let callee_value: ast::ExpressionNode = {
        let jni_node = env
          .call_method(value, "getCallee", "()Lcom/dallonf/ktcause/ast/ExpressionNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };
      let parameters_value: Vec<Arc<ast::FunctionCallParameterNode>> = {
        let jni_node = env
          .call_method(value, "getParameters", "()Ljava/util/List;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::CallExpressionNode {
          info,
          callee: callee_value,
          parameters: parameters_value,
      })
    }
}
impl FromJni for ast::IdentifierExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node IdentifierExpressionNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let identifier_value: Arc<ast::IdentifierNode> = {
        let jni_node = env
          .call_method(value, "getIdentifier", "()Lcom/dallonf/ktcause/ast/IdentifierNode;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::IdentifierExpressionNode {
          info,
          identifier: identifier_value,
      })
    }
}
impl FromJni for ast::StringLiteralExpressionNode {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
      noisy_log(env, "node StringLiteralExpressionNode");
      let info = env
        .call_method(value, "getInfo", "()Lcom/dallonf/ktcause/ast/NodeInfo;", &[])?
        .l()?
        .jni_into(env)?;
      let text_value: Arc<String> = {
        let jni_node = env
          .call_method(value, "getText", "()Ljava/lang/String;", &[])?
          .l()?;
        let jni_node = JObject::from(jni_node);
        jni_node.jni_into(env)?
      };

      Ok(ast::StringLiteralExpressionNode {
          info,
          text: text_value,
      })
    }
}
