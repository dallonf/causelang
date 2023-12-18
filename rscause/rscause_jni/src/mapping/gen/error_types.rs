impl FromJni for LangError {
  fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
    let class_name = get_class_name(env, value)?;
    match class_name.as_ref() {
      "NeverResolved" => {
        Ok(LangError::NeverResolved)
      },
      "NotInScope" => {
        Ok(LangError::NotInScope)
      },
      "FileNotFound" => {
        Ok(LangError::FileNotFound)
      },
      "ImportPathInvalid" => {
        Ok(LangError::ImportPathInvalid)
      },
      "ExportNotFound" => {
        Ok(LangError::ExportNotFound)
      },
      "ProxyError" => {
        let actual_error: Arc<LangError> = {
          let jni_node = env
            .call_method(value, "getActualError", "()Lcom/dallonf/ktcause/types/ErrorLangType;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        let proxy_chain: Vec<ErrorPosition> = {
          let jni_node = env
            .call_method(value, "getProxyChain", "()Ljava/util/List;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        Ok(LangError::ProxyError(error_types::ProxyErrorError {
          actual_error,
          proxy_chain,
        }).into())
      },
      "NotCallable" => {
        Ok(LangError::NotCallable)
      },
      "NotCausable" => {
        Ok(LangError::NotCausable)
      },
      "ImplementationTodo" => {
        let description: String = {
          let jni_node = env
            .call_method(value, "getDescription", "()Ljava/lang/String;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        Ok(LangError::ImplementationTodo(error_types::ImplementationTodoError {
          description,
        }).into())
      },
      "MismatchedType" => {
        let expected: lang_types::AnyInferredLangType = {
          let jni_node = env
            .call_method(value, "getExpected", "()Lcom/dallonf/ktcause/types/ConstraintValueLangType;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        let actual: Arc<lang_types::LangType> = {
          let jni_node = env
            .call_method(value, "getActual", "()Lcom/dallonf/ktcause/types/ResolvedValueLangType;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        Ok(LangError::MismatchedType(error_types::MismatchedTypeError {
          expected,
          actual,
        }).into())
      },
      "MissingParameters" => {
        let names: Vec<String> = {
          let jni_node = env
            .call_method(value, "getNames", "()Ljava/util/List;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        Ok(LangError::MissingParameters(error_types::MissingParametersError {
          names,
        }).into())
      },
      "ExcessParameters" => {
        let expected: u32 = {
          let jni_node = env
            .call_method(value, "getExpected", "()I", &[])?
            .i()?
            .try_conv::<u32>()?;
          jni_node
        };
        Ok(LangError::ExcessParameters(error_types::ExcessParametersError {
          expected,
        }).into())
      },
      "UnknownParameter" => {
        Ok(LangError::UnknownParameter)
      },
      "MissingElseBranch" => {
        Ok(LangError::MissingElseBranch)
      },
      "UnreachableBranch" => {
        let options: Option<lang_types::OneOfLangType> = {
          let jni_node = env
            .call_method(value, "getOptions", "()Lcom/dallonf/ktcause/types/OptionValueLangType;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        Ok(LangError::UnreachableBranch(error_types::UnreachableBranchError {
          options,
        }).into())
      },
      "ActionIncompatibleWithValueTypes" => {
        let actions: Vec<SourcePosition> = {
          let jni_node = env
            .call_method(value, "getActions", "()Ljava/util/List;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        let types: Vec<lang_types::LangType> = {
          let jni_node = env
            .call_method(value, "getTypes", "()Ljava/util/List;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        Ok(LangError::ActionIncompatibleWithValueTypes(error_types::ActionIncompatibleWithValueTypesError {
          actions,
          types,
        }).into())
      },
      "ConstraintUsedAsValue" => {
        let r#type: lang_types::LangType = {
          let jni_node = env
            .call_method(value, "getType", "()Lcom/dallonf/ktcause/types/ConstraintValueLangType;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        Ok(LangError::ConstraintUsedAsValue(error_types::ConstraintUsedAsValueError {
          r#type,
        }).into())
      },
      "ValueUsedAsConstraint" => {
        let r#type: lang_types::AnyInferredLangType = {
          let jni_node = env
            .call_method(value, "getType", "()Lcom/dallonf/ktcause/types/ValueLangType;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        Ok(LangError::ValueUsedAsConstraint(error_types::ValueUsedAsConstraintError {
          r#type,
        }).into())
      },
      "DoesNotHaveAnyMembers" => {
        Ok(LangError::DoesNotHaveAnyMembers)
      },
      "DoesNotHaveMember" => {
        Ok(LangError::DoesNotHaveMember)
      },
      "NotVariable" => {
        Ok(LangError::NotVariable)
      },
      "OuterVariable" => {
        Ok(LangError::OuterVariable)
      },
      "CannotBreakHere" => {
        Ok(LangError::CannotBreakHere)
      },
      "NotSupportedInRust" => {
        Ok(LangError::NotSupportedInRust)
      },
      "CompilerBug" => {
        let description: String = {
          let jni_node = env
            .call_method(value, "getDescription", "()Ljava/lang/String;", &[])?
            .l()?;
          let jni_node = JObject::from(jni_node);
          jni_node.jni_into(env)?
        };
        Ok(LangError::CompilerBug(error_types::CompilerBugError {
          description,
        }).into())
      },
      _ => panic!("Unknown class name for LangError: {}", class_name)
    }
  }
}

impl IntoJni for LangError {
  fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
    match self {
      LangError::NeverResolved => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$NeverResolved")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::NotInScope => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$NotInScope")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::FileNotFound => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$FileNotFound")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::ImportPathInvalid => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$ImportPathInvalid")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::ExportNotFound => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$ExportNotFound")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::ProxyError(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$ProxyError")?;
        let actual_error = err.actual_error.into_jni(env)?;
        let proxy_chain = err.proxy_chain.into_jni(env)?;
        let result = env.new_object(class, "(Lcom/dallonf/ktcause/types/ErrorLangType;Ljava/util/List;)V", &[
          actual_error.borrow(),
          proxy_chain.borrow(),
        ])?;
        Ok(result.into())
      },
      LangError::NotCallable => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$NotCallable")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::NotCausable => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$NotCausable")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::ImplementationTodo(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$ImplementationTodo")?;
        let description = err.description.into_jni(env)?;
        let result = env.new_object(class, "(Ljava/lang/String;)V", &[
          description.borrow(),
        ])?;
        Ok(result.into())
      },
      LangError::MismatchedType(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$MismatchedType")?;
        let expected = err.expected.into_jni(env)?;
        let actual = err.actual.into_jni(env)?;
        let result = env.new_object(class, "(Lcom/dallonf/ktcause/types/ConstraintValueLangType;Lcom/dallonf/ktcause/types/ResolvedValueLangType;)V", &[
          expected.borrow(),
          actual.borrow(),
        ])?;
        Ok(result.into())
      },
      LangError::MissingParameters(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$MissingParameters")?;
        let names = err.names.into_jni(env)?;
        let result = env.new_object(class, "(Ljava/util/List;)V", &[
          names.borrow(),
        ])?;
        Ok(result.into())
      },
      LangError::ExcessParameters(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$ExcessParameters")?;
        let expected = err.expected.into_jni(env)?;
        let result = env.new_object(class, "(I)V", &[
          expected.borrow(),
        ])?;
        Ok(result.into())
      },
      LangError::UnknownParameter => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$UnknownParameter")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::MissingElseBranch => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$MissingElseBranch")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::UnreachableBranch(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$UnreachableBranch")?;
        let options = err.options.into_jni(env)?;
        let result = env.new_object(class, "(Lcom/dallonf/ktcause/types/OptionValueLangType;)V", &[
          options.borrow(),
        ])?;
        Ok(result.into())
      },
      LangError::ActionIncompatibleWithValueTypes(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$ActionIncompatibleWithValueTypes")?;
        let actions = err.actions.into_jni(env)?;
        let types = err.types.into_jni(env)?;
        let result = env.new_object(class, "(Ljava/util/List;Ljava/util/List;)V", &[
          actions.borrow(),
          types.borrow(),
        ])?;
        Ok(result.into())
      },
      LangError::ConstraintUsedAsValue(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$ConstraintUsedAsValue")?;
        let r#type = err.r#type.into_jni(env)?;
        let result = env.new_object(class, "(Lcom/dallonf/ktcause/types/ConstraintValueLangType;)V", &[
          r#type.borrow(),
        ])?;
        Ok(result.into())
      },
      LangError::ValueUsedAsConstraint(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$ValueUsedAsConstraint")?;
        let r#type = err.r#type.into_jni(env)?;
        let result = env.new_object(class, "(Lcom/dallonf/ktcause/types/ValueLangType;)V", &[
          r#type.borrow(),
        ])?;
        Ok(result.into())
      },
      LangError::DoesNotHaveAnyMembers => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$DoesNotHaveAnyMembers")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::DoesNotHaveMember => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$DoesNotHaveMember")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::NotVariable => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$NotVariable")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::OuterVariable => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$OuterVariable")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::CannotBreakHere => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$CannotBreakHere")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::NotSupportedInRust => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$NotSupportedInRust")?;
        let result = env.new_object(class, "()V", &[
        ])?;
        Ok(result.into())
      },
      LangError::CompilerBug(err) => {
        let class = env.find_class("com/dallonf/ktcause/types/ErrorLangType$CompilerBug")?;
        let description = err.description.into_jni(env)?;
        let result = env.new_object(class, "(Ljava/lang/String;)V", &[
          description.borrow(),
        ])?;
        Ok(result.into())
      },
    }
  }
}