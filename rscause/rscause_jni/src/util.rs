use std::panic::{self, AssertUnwindSafe};

use anyhow::Result;
use jni::objects::{JObject, JValue};
use jni::sys::jvalue;
use jni::JNIEnv;

pub fn jprintln(env: &mut JNIEnv, str: &str) -> Result<()> {
    let java_string = env.new_string(str)?;
    let value = JValue::Object(&java_string);
    let system_class = env.find_class("java/lang/System")?;
    let out_field = env.get_static_field(system_class, "out", "Ljava/io/PrintStream;")?;
    let print_stream = out_field.l()?;
    let args = [value];
    env.call_method(print_stream, "println", "(Ljava/lang/String;)V", &args)?;
    Ok(())
}

pub fn jtry<Callback: (FnOnce(&mut JNIEnv) -> Result<jvalue>)>(
    env: &mut JNIEnv,
    callback: Callback,
) -> jvalue {
    let panicked = panic::catch_unwind(AssertUnwindSafe(|| match callback(env) {
        Ok(result) => result,
        Err(err) => {
            let _ = env.throw_new("java/lang/RuntimeException", err.to_string());
            JValue::Object(&JObject::null()).as_jni()
        }
    }));
    match panicked {
        Ok(value) => value,
        Err(err) => {
            if err.is::<String>() {
                let _ = env.throw_new(
                    "java/lang/RuntimeException",
                    err.downcast_ref::<String>().unwrap(),
                );
            } else if err.is::<&str>() {
                let _ = env.throw_new(
                    "java/lang/RuntimeException",
                    err.downcast_ref::<&str>().unwrap(),
                );
            } else {
                let _ = env.throw_new("java/lang/RuntimeException", "Unknown panic");
            }
            JValue::Object(&JObject::null()).as_jni()
        }
    }
}