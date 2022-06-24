
package com.wind.base.gradle.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager


abstract class BaseTransformation extends Transform{

    @Override
    String getName() {
        return this.getClass().getSimpleName()
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        final boolean isIncremental = transformInvocation.isIncremental() && this.isIncremental()
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        //修改过的输入jar 输出jar文件
        Map<File, File> jarInputMap = new HashMap<>()
        //保存修改过的输入文件 输出文件
        Map<File, File> srcInputMap = new HashMap<>()

        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { dirInput ->
                def outputDir = outputProvider.getContentLocation(
                        dirInput.name,
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY
                )
                collectAndIdentifyDir(srcInputMap, dirInput, outputDir, isIncremental)
            }

            input.jarInputs.each { jarInput ->
                if (jarInput.getStatus() != Status.REMOVED) {

                    def outputJarLocation = outputProvider.getContentLocation(
                            Util.getUniqueJarName(jarInput.file),
                            jarInput.contentTypes,
                            jarInput.scopes,
                            Format.JAR
                    )
                    collectAndIdentifyJar(jarInputMap, jarInput, outputJarLocation, isIncremental)
                }
            }

            getHandler().handle(srcInputMap,jarInputMap)

        }

    }

    /**
     *
     * @param jarMap
     * @param srcMap
     */
    abstract <T extends BaseHandler> T getHandler()

    private void collectAndIdentifyDir(Map<File, File> dirInputMap, DirectoryInput input, File dirOutput, boolean isIncremental) {
        final File dirInput = input.file
        if (!dirOutput.exists()) {
            dirOutput.mkdirs()
        }
        if (isIncremental) {
            if (!dirInput.exists()) {
                dirOutput.deleteDir()
            } else {

                final String rootInputFullPath = dirInput.getAbsolutePath()
                final String rootOutputFullPath = dirOutput.getAbsolutePath()
                input.changedFiles.each { entry ->
                    final File changedFileInput = entry.getKey()
                    final String changedFileInputFullPath = changedFileInput.getAbsolutePath()
                    final File changedFileOutput = new File(changedFileInputFullPath.replace(rootInputFullPath, rootOutputFullPath))
                    final Status status = entry.getValue()
                    switch (status) {
                        case Status.NOTCHANGED:
                            break
                        case Status.ADDED:
                        case Status.CHANGED:
                            dirInputMap.put(changedFileInput,changedFileOutput)
                            break
                        case Status.REMOVED:
                            changedFileOutput.delete()
                            break
                    }
                }

            }
        } else {
            dirInputMap.put(dirInput, dirOutput)
        }

    }

    private void collectAndIdentifyJar(Map<File, File> jarInputMap, JarInput input, File jarOutput, boolean isIncremental) {
        final jarFile = input.file
        if (Util.isRealZipOrJar(jarFile)) {
            switch (input.status) {
                case Status.NOTCHANGED:
                    if (isIncremental) {
                        break
                    }
                case Status.ADDED:
                case Status.CHANGED:
                    jarInputMap.put(jarFile, jarOutput)
                    break
                case Status.REMOVED:
                    break
            }
        }

    }



}