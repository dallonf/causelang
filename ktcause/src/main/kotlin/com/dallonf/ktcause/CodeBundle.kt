package com.dallonf.ktcause

import com.dallonf.ktcause.ast.FileNode
import com.dallonf.ktcause.parse.parse
import com.dallonf.ktcause.types.*

data class CodeBundle(val files: Map<String, CompiledFile>, val compileErrors: List<Resolver.ResolverError>) {
    val allTypes by lazy {
        mapOf(*files.values.flatMap { file -> file.types.entries }.map { it.toPair() }.toTypedArray())
    }

    fun getFileDescriptor(filePath: String): Resolver.ExternalFileDescriptor {
        return requireFile(filePath).toFileDescriptor()
    }

    fun requireFile(filePath: String): CompiledFile {
        return requireNotNull(files[filePath]) { "I don't know about any file at $filePath." }
    }

    fun getType(filePath: String, name: String): CanonicalLangType {
        val descriptor = getFileDescriptor(filePath)

        val found = requireNotNull(descriptor.exports[name]) { "$filePath doesn't have an export called $name." }

        if (found is ConstraintValueLangType && found.valueType is InstanceValueLangType) {
            return getType(found.valueType.canonicalTypeId)
        } else {
            throw LangVm.VmError("$name isn't a canonical type.")
        }
    }

    fun getType(canonicalTypeId: CanonicalLangTypeId): CanonicalLangType =
        allTypes[canonicalTypeId] ?: throw LangVm.VmError("$canonicalTypeId isn't a known type")

    fun getTypeId(filePath: String, name: String): CanonicalLangTypeId {
        return getType(filePath, name).id
    }

    fun getBuiltinTypeId(name: String) = getTypeId(CoreFiles.builtin.path, name)
}

class CodeBundleBuilder {
    private val compiledFiles = mutableMapOf<String, CompiledFile>()

    data class PendingFile(
        val path: String, val ast: FileNode, val analyzed: AnalyzedNode, val debugContext: Debug.DebugContext
    )

    private val pendingFiles = mutableListOf<PendingFile>()

    val requiredFilePaths: List<String>
        get() = pendingFiles.flatMap { it.analyzed.filesReferenced }
            .filter { !it.startsWith("core/") && !compiledFiles.containsKey(it) && !pendingFiles.any { file -> file.path == it } }

    fun addCompiledFile(file: CompiledFile) {
        compiledFiles[file.path] = file
    }

    fun addFile(filePath: String, source: String): Debug.DebugContext {
        val astNode = parse(source)
        val analyzedFile = Analyzer.analyzeFile(filePath, astNode, Debug.DebugContext(source, astNode))
        val analyzedDebugCtx = Debug.DebugContext(source, astNode, analyzedFile)
        pendingFiles.add(
            PendingFile(
                filePath, astNode, analyzedFile, analyzedDebugCtx
            )
        )
        return analyzedDebugCtx
    }

    fun build(): CodeBundle {
        val finalCompiledFiles = compiledFiles.toMutableMap()
        val finalCompileErrors = mutableListOf<Resolver.ResolverError>()
        val workingPendingFiles = pendingFiles.toMutableList()

        // PERF: We don't need to add _all_ of the core files...
        for (coreFile in CoreFiles.all) {
            finalCompiledFiles[coreFile.path] = coreFile
        }

        fun compilePending(
            file: PendingFile,
            referencedCompiledFiles: List<CompiledFile>,
        ) {
            val otherFiles = referencedCompiledFiles.associate { it.path to it.toFileDescriptor() }

            val compiledFile = if (RustCompiler.shouldRunRustCompiler(file.ast)) {
                RustCompiler.compile(file.path, file.ast, file.analyzed.nodeTags, otherFiles)
            } else {
                val (resolvedFile, resolverErrors) = Resolver.resolveForFile(
                    file.path, file.ast, file.analyzed, otherFiles, file.debugContext
                )
                finalCompileErrors.addAll(resolverErrors)
                Compiler.compile(file.ast, file.analyzed, resolvedFile)
            }
            finalCompiledFiles[file.path] = compiledFile
        }

        while (workingPendingFiles.isNotEmpty()) {
            val filesCompiledThisIteration = mutableListOf<String>()

            for (file in workingPendingFiles) {
                val referencedCompiledFiles = file.analyzed.filesReferenced.mapNotNull { path ->
                    finalCompiledFiles[path]
                }
                if (referencedCompiledFiles.size == file.analyzed.filesReferenced.size) {
                    // if we've got all the compiled files we need...
                    compilePending(file, referencedCompiledFiles)

                    filesCompiledThisIteration.add(file.path)
                }
            }

            if (filesCompiledThisIteration.isEmpty()) {
                break
            }
            workingPendingFiles.removeAll { it.path in filesCompiledThisIteration }
        }

        // at this point, anything that remains won't have all the files they need, but let's go ahead and compile them anyway
        for (file in workingPendingFiles) {
            compilePending(file, file.analyzed.filesReferenced.mapNotNull { path -> finalCompiledFiles[path] })
        }

        return CodeBundle(finalCompiledFiles, finalCompileErrors)
    }
}