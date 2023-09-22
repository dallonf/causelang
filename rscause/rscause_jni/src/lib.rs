use jni::objects::JClass;
use jni::sys::jstring;
use jni::JNIEnv;
use util::jprintln;

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
