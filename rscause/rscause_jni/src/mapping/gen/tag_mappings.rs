impl FromJni for tags::NodeTag {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let class_name = get_class_name(env, value)?;
        Ok(match class_name.as_str() {
            "ReferencesFile" => tags::NodeTag::ReferencesFile(value.jni_into(env)?),
            "BadFileReference" => tags::NodeTag::BadFileReference(value.jni_into(env)?),
            "ValueGoesTo" => tags::NodeTag::ValueGoesTo(value.jni_into(env)?),
            "ValueComesFrom" => tags::NodeTag::ValueComesFrom(value.jni_into(env)?),
            "FunctionCanReturnTypeOf" => {
                tags::NodeTag::FunctionCanReturnTypeOf(value.jni_into(env)?)
            }
            "ReturnsFromFunction" => tags::NodeTag::ReturnsFromFunction(value.jni_into(env)?),
            "FunctionCanReturnAction" => {
                tags::NodeTag::FunctionCanReturnAction(value.jni_into(env)?)
            }
            "ActionReturn" => tags::NodeTag::ActionReturn(value.jni_into(env)?),
            _ => panic!("Unknown class name for NodeTag: {}", class_name),
        })
    }
}

impl FromJni for tags::ReferencesFileNodeTag {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "tag ReferencesFile");
        let path: Arc<String> = {
            let jni_node = env
                .call_method(value, "getPath", "()Ljava/lang/String;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        let export_name: Option<Arc<String>> = {
            let jni_node = env
                .call_method(value, "getExportName", "()Ljava/lang/String;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        Ok(tags::ReferencesFileNodeTag { path, export_name })
    }
}
impl FromJni for tags::BadFileReferenceNodeTag {
    fn from_jni<'local>(env: &mut JNIEnv, _value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "tag BadFileReference");
        Ok(tags::BadFileReferenceNodeTag {})
    }
}
impl FromJni for tags::ValueGoesToNodeTag {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "tag ValueGoesTo");
        let destination: Breadcrumbs = {
            let jni_node = env
                .call_method(
                    value,
                    "getDestination",
                    "()Lcom/dallonf/ktcause/ast/Breadcrumbs;",
                    &[],
                )?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        Ok(tags::ValueGoesToNodeTag { destination })
    }
}
impl FromJni for tags::ValueComesFromNodeTag {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "tag ValueComesFrom");
        let source: Breadcrumbs = {
            let jni_node = env
                .call_method(
                    value,
                    "getSource",
                    "()Lcom/dallonf/ktcause/ast/Breadcrumbs;",
                    &[],
                )?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        Ok(tags::ValueComesFromNodeTag { source })
    }
}
impl FromJni for tags::FunctionCanReturnTypeOfNodeTag {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "tag FunctionCanReturnTypeOf");
        let return_expression_value: Breadcrumbs = {
            let jni_node = env
                .call_method(
                    value,
                    "getReturnExpressionValue",
                    "()Lcom/dallonf/ktcause/ast/Breadcrumbs;",
                    &[],
                )?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        Ok(tags::FunctionCanReturnTypeOfNodeTag {
            return_expression_value,
        })
    }
}
impl FromJni for tags::ReturnsFromFunctionNodeTag {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "tag ReturnsFromFunction");
        let function: Breadcrumbs = {
            let jni_node = env
                .call_method(
                    value,
                    "getFunction",
                    "()Lcom/dallonf/ktcause/ast/Breadcrumbs;",
                    &[],
                )?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        Ok(tags::ReturnsFromFunctionNodeTag { function })
    }
}
impl FromJni for tags::FunctionCanReturnActionNodeTag {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "tag FunctionCanReturnAction");
        let return_expression: Breadcrumbs = {
            let jni_node = env
                .call_method(
                    value,
                    "getReturnExpression",
                    "()Lcom/dallonf/ktcause/ast/Breadcrumbs;",
                    &[],
                )?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        Ok(tags::FunctionCanReturnActionNodeTag { return_expression })
    }
}
impl FromJni for tags::ActionReturnNodeTag {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "tag ActionReturn");
        let function: Breadcrumbs = {
            let jni_node = env
                .call_method(
                    value,
                    "getFunction",
                    "()Lcom/dallonf/ktcause/ast/Breadcrumbs;",
                    &[],
                )?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        Ok(tags::ActionReturnNodeTag { function })
    }
}
