use jni::objects::{JClass, JObject, JValue};
use jni::sys::{jstring, jvalue};
use jni::JNIEnv;
use mapping::ast::JniToAstNode;
use rscause_compiler::ast_nodes::FileNode;
use util::{jprintln, jtry};

mod mapping;
mod util;

#[no_mangle]
pub extern "system" fn Java_com_dallonf_ktcause_RustCompiler_hello<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jstring {
    jprintln(&mut env, "Logging from Rust v2!").unwrap();

    let output = env.new_string("Hello from Rust!").unwrap();
    output.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_dallonf_ktcause_RustCompiler_logAst<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    jni_ast: JObject<'local>,
) -> jvalue {
    jtry(&mut env, move |mut env| {
        let ast: FileNode = jni_ast.to_ast_node(&mut env)?;

        jprintln(&mut env, format!("AST: {:#?}", ast).as_str())?;

        Ok(JValue::Object(&JObject::null()).as_jni())
    })
}
