package com.dallonf.ktcause

object RustResolver {
    init {
        System.loadLibrary("rscause_jni")
    }

    external fun hello(): String
}