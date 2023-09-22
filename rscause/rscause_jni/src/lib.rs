use jni::objects::{JClass, JObject, JValue};
use jni::sys::{jstring, jvalue};
use jni::JNIEnv;
use util::{jprintln, jtry};

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
    ast: JObject<'local>,
) -> jvalue {
    jtry(&mut env, move |mut env| {
        let declarations = env
            .get_field(&ast, "declarations", "Lkotlin/collections/List;")?
            .l()?;
        // let declarations = env
        //     .call_method(ast, "getDeclarations", "()Lkotlin/collections/List;", &[])?
        //     .l()?;
        let count = env.call_method(declarations, "size", "()I", &[])?.i()?;

        jprintln(&mut env, format!("Found {} declarations", count).as_str())?;
        Ok(JValue::Object(&JObject::null()).as_jni())
    })
}
