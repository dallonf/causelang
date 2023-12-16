impl IntoJni for Instruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        match self {
            Instruction::NoOp(instruction) => instruction.into_jni(env),
            Instruction::Pop(instruction) => instruction.into_jni(env),
            Instruction::Swap(instruction) => instruction.into_jni(env),
            Instruction::PopScope(instruction) => instruction.into_jni(env),
            Instruction::RegisterEffect(instruction) => instruction.into_jni(env),
            Instruction::PopEffects(instruction) => instruction.into_jni(env),
            Instruction::PushAction(instruction) => instruction.into_jni(env),
            Instruction::Literal(instruction) => instruction.into_jni(env),
            Instruction::Import(instruction) => instruction.into_jni(env),
            Instruction::ImportSameFile(instruction) => instruction.into_jni(env),
            Instruction::DefineFunction(instruction) => instruction.into_jni(env),
            Instruction::ReadLocal(instruction) => instruction.into_jni(env),
            Instruction::WriteLocal(instruction) => instruction.into_jni(env),
            Instruction::ReadLocalThroughEffectScope(instruction) => instruction.into_jni(env),
            Instruction::WriteLocalThroughEffectScope(instruction) => instruction.into_jni(env),
            Instruction::Construct(instruction) => instruction.into_jni(env),
            Instruction::CallFunction(instruction) => instruction.into_jni(env),
            Instruction::GetMember(instruction) => instruction.into_jni(env),
            Instruction::NameValue(instruction) => instruction.into_jni(env),
            Instruction::IsAssignableTo(instruction) => instruction.into_jni(env),
            Instruction::Jump(instruction) => instruction.into_jni(env),
            Instruction::JumpIfFalse(instruction) => instruction.into_jni(env),
            Instruction::StartLoop(instruction) => instruction.into_jni(env),
            Instruction::ContinueLoop(instruction) => instruction.into_jni(env),
            Instruction::BreakLoop(instruction) => instruction.into_jni(env),
            Instruction::Cause(instruction) => instruction.into_jni(env),
            Instruction::RejectSignal(instruction) => instruction.into_jni(env),
            Instruction::FinishEffect(instruction) => instruction.into_jni(env),
            Instruction::Return(instruction) => instruction.into_jni(env),
        }
    }
}

impl IntoJni for instructions::NoOpInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$NoOp")?;
        let jni_instruction = env.new_object(
            class,
            "()V",
            &[
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::PopInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$Pop")?;
        let jni_number = {
            (self.number as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_number.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::SwapInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$Swap")?;
        let jni_instruction = env.new_object(
            class,
            "()V",
            &[
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::PopScopeInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$PopScope")?;
        let jni_values = {
            (self.values as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_values.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::RegisterEffectInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$RegisterEffect")?;
        let jni_procedure_index = {
            (self.procedure_index as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_procedure_index.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::PopEffectsInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$PopEffects")?;
        let jni_number = {
            (self.number as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_number.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::PushActionInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$PushAction")?;
        let jni_instruction = env.new_object(
            class,
            "()V",
            &[
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::LiteralInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$Literal")?;
        let jni_constant = {
            (self.constant as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_constant.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::ImportInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$Import")?;
        let jni_file_path_constant = {
            (self.file_path_constant as i32).into_jni(env)?
        };
        let jni_export_name_constant = {
            (self.export_name_constant as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(II)V",
            &[
                jni_file_path_constant.borrow(),
                jni_export_name_constant.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::ImportSameFileInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$ImportSameFile")?;
        let jni_export_name_constant = {
            (self.export_name_constant as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_export_name_constant.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::DefineFunctionInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$DefineFunction")?;
        let jni_procedure_index = {
            (self.procedure_index as i32).into_jni(env)?
        };
        let jni_type_constant = {
            (self.type_constant as i32).into_jni(env)?
        };
        let jni_captured_values = {
            (self.captured_values as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(III)V",
            &[
                jni_procedure_index.borrow(),
                jni_type_constant.borrow(),
                jni_captured_values.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::ReadLocalInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$ReadLocal")?;
        let jni_index = {
            (self.index as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_index.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::WriteLocalInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$WriteLocal")?;
        let jni_index = {
            (self.index as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_index.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::ReadLocalThroughEffectScopeInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$ReadLocalThroughEffectScope")?;
        let jni_effect_depth = {
            (self.effect_depth as i32).into_jni(env)?
        };
        let jni_index = {
            (self.index as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(II)V",
            &[
                jni_effect_depth.borrow(),
                jni_index.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::WriteLocalThroughEffectScopeInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$WriteLocalThroughEffectScope")?;
        let jni_effect_depth = {
            (self.effect_depth as i32).into_jni(env)?
        };
        let jni_index = {
            (self.index as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(II)V",
            &[
                jni_effect_depth.borrow(),
                jni_index.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::ConstructInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$Construct")?;
        let jni_arity = {
            (self.arity as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_arity.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::CallFunctionInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$CallFunction")?;
        let jni_arity = {
            (self.arity as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_arity.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::GetMemberInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$GetMember")?;
        let jni_index = {
            (self.index as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_index.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::NameValueInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$NameValue")?;
        let jni_name_constant = {
            (self.name_constant as i32).into_jni(env)?
        };
        let jni_variable = {
            self.variable.into_jni(env)?
                    };
        let jni_local_index = {
            into_jni_optional_int(self.local_index.map(|it| it as i32), env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(IZLjava/lang/Integer;)V",
            &[
                jni_name_constant.borrow(),
                jni_variable.borrow(),
                jni_local_index.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::IsAssignableToInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$IsAssignableTo")?;
        let jni_instruction = env.new_object(
            class,
            "()V",
            &[
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::JumpInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$Jump")?;
        let jni_instruction = {
            (self.instruction as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_instruction.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::JumpIfFalseInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$JumpIfFalse")?;
        let jni_instruction = {
            (self.instruction as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_instruction.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::StartLoopInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$StartLoop")?;
        let jni_end_instruction = {
            (self.end_instruction as i32).into_jni(env)?
        };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_end_instruction.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::ContinueLoopInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$ContinueLoop")?;
        let jni_instruction = env.new_object(
            class,
            "()V",
            &[
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::BreakLoopInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$BreakLoop")?;
        let jni_levels = {
            self.levels.into_jni(env)?
                    };
        let jni_instruction = env.new_object(
            class,
            "(I)V",
            &[
                jni_levels.borrow(),
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::CauseInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$Cause")?;
        let jni_instruction = env.new_object(
            class,
            "()V",
            &[
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::RejectSignalInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$RejectSignal")?;
        let jni_instruction = env.new_object(
            class,
            "()V",
            &[
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::FinishEffectInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$FinishEffect")?;
        let jni_instruction = env.new_object(
            class,
            "()V",
            &[
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
impl IntoJni for instructions::ReturnInstruction {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/Instruction$Return")?;
        let jni_instruction = env.new_object(
            class,
            "()V",
            &[
            ],
        )?;
        Ok(jni_instruction.into())
    }
}
