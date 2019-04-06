import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GitChecker(
        val configBaseDir : File,
        val githubLogin : String,
        val courseConfig : CourseConfig,
        val assignmentConfig : AssignmentConfig,
        val userCheckoutDir : File?,
        val runMode : Boolean,
        val textMode : Boolean,
        val writeLogToFeedback : Boolean
) {
    private val checkoutDir : File by lazy { userCheckoutDir ?: makeTempDir() }

    private fun makeTempDir() : File {
        val result = Files.createTempDirectory("gitchecker").toFile()
        result.deleteOnExit()
        return result
    }


    /**
     * Check the given assignment.  This is the entry point to GitChecker, called from main.
     *
     * @throws TestFailed
     */
    public fun check() {
        if (userCheckoutDir?.exists() == true) {
            throw TestFailed("$userCheckoutDir exists already")
        }
        val testDir = File(checkoutDir, "test")
        testDir.mkdirs()
        if (!testDir.isDirectory) {
            checkoutDir.deleteRecursively()
            throw TestFailed("Could not create directory $testDir");
        }
        val turnin = courseConfig.turninName
        val repo = Github.getRepo(courseConfig.githubOrg, "$turnin-$githubLogin", checkoutDir)
        val feedbackRepo = if (writeLogToFeedback) {
            Github.getRepo(courseConfig.githubOrg, "feedback-$githubLogin", checkoutDir)
        } else {
            null
        }
        val origBaseDir = File(repo.dir, assignmentConfig.dir.name)
        val deadline = Deadline.get(origBaseDir, repo)
        val cutoff = deadline.getCutoff(assignmentConfig.due)
        println("$deadline:  Getting last push before $cutoff")
        repo.checkoutRevisionBefore(cutoff)
        if (!origBaseDir.exists() || !origBaseDir.isDirectory) {
            throw IOException("There is no ${origBaseDir.name} directory in this version of your repo.")
        }
        val baseDir = File(origBaseDir.parent, "checkgit-${origBaseDir.name}")
        if (!origBaseDir.renameTo(baseDir)) {
            throw IOException("Unable to rename $origBaseDir to $baseDir")
        }
        try {
            for (part in assignmentConfig.parts) {
                var repoDir = File(baseDir, part.dirName)
                val destDir = if (part.useTestDir) {
                    File(testDir, part.dirName)
                } else {
                    repoDir
                }
                part.check(this, repoDir, destDir)
            }
            logResult(repo, feedbackRepo, assignmentConfig, "passed")
            println()
            println("**  Checkgit Tests Passed **")
            println()
        } catch (ex: TestFailed) {
            logResult(repo, feedbackRepo, assignmentConfig, "failed:  ${ex.message?.split('\n')?.getOrNull(0)}")
            throw ex
        } catch (ex: Exception) {
            logResult(repo, feedbackRepo, assignmentConfig, "failed:  $ex")
            throw ex
        } finally {
            if (userCheckoutDir == null) {
                checkoutDir.deleteRecursively()
                if (checkoutDir.exists()) {
                    println("Warning:  Could not delete $checkoutDir")
                }
            }
        }
    }

    private fun logResult(repo: GitRepo, feedbackRepo: GitRepo?,
                          assignmentConfig: AssignmentConfig, message: String)
    {
        val now = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd").format(LocalDateTime.now())
        val f = File("/home/wffoote/public/logs/checkgit.log")
        val user = System.getenv("USER")
        val entry = "$now ${repo.uri} ${assignmentConfig.dir.name} by $user:  ${message.split('\n').getOrNull(0)}"
        if (!f.exists() || !f.canWrite()) {
            println(entry)
            println("Result not logged - $f not writable")
        }
        try {
            java.nio.file.Files.write(f.toPath(), listOf(entry), StandardOpenOption.APPEND)
        } catch (ignored: Exception) {
        }
        if (feedbackRepo != null) {
            val feedbackFile = File(feedbackRepo.dir, "checkgit.log")
            if (!feedbackFile.exists()) {
                feedbackFile.createNewFile()
            }
            java.nio.file.Files.write(feedbackFile.toPath(), listOf(entry), StandardOpenOption.APPEND)
            feedbackRepo.add(feedbackFile)
            feedbackRepo.commitAndPush()
        }
    }

    private fun compileProgram(dir: File, classpathIn: String) : Boolean {
        var classpath = classpathIn
        val srcDir = File(dir, "src")
        val isKotlin = lookForKotlin(srcDir);
        if (!isKotlin) {
            println("No kotlin files - Not building Kotlin")
        } else {
            val command = mutableListOf<String>("kotlinc", "-cp", classpath,
                                                "-include-runtime", "-d", "kotlin.jar")
            addFilesTo(command, srcDir, "src", { s -> s.endsWith(".java") || s.endsWith(".kt") })
            runCommand(dir, command)
            classpath = classpathIn + ":./kotlin.jar"
        }
        val command = mutableListOf<String>("javac", "-cp", classpath + ":", "-Xmaxerrs", "5",
                "-Xlint:unchecked", "-Werror", "-d", ".")
        val lengthBeforeAdd = command.size
        addFilesTo(command, srcDir, "src", { s -> s.endsWith(".java") })
        if (command.size != lengthBeforeAdd) {
            runCommand(dir, command)
        } else if (!isKotlin) {
            throw TestFailed("No .kt or .java files found in $srcDir")
        }
        return isKotlin
    }

    private fun lookForKotlin(dir: File) : Boolean {
        if (!dir.exists() || !dir.isDirectory()) {
            return false
        }
        for (f in dir.listFiles()) {
            if (f.name.endsWith(".kt")) {
                return true;
            } else if (f.name.startsWith(".")) {
            } else if (f.isDirectory() && lookForKotlin(f)) {
                return true
            }
        }
        return false
    }

    private fun addFilesTo(list: MutableList<String>, dir: File, prefix: String,
                           condition: (String) -> Boolean)
    {
        for (f in dir.listFiles()) {
            if (f.name.startsWith(".")) {
                // ignore
            } else if (f.isDirectory) {
                addFilesTo(list, f, "$prefix/${f.name}", condition)
            } else if (condition(f.name)) {
                list.add("$prefix/${f.name}")
            }
        }
    }

    private fun copyProvidedFiles(partDirName: String, dest: File) {
        val provided = File(assignmentConfig.dir, "provided/$partDirName")
        if (provided.isDirectory) {
            copyFiles(provided, dest, "checkgit internal file", { true })
        }
    }

    public fun checkSrcExists(repoDir: File) {
        val srcDir = File(repoDir, "src")
        if (!srcDir.exists() || !srcDir.isDirectory) {
            throw TestFailed("No src directory found in $repoDir")
        }
    }

    public fun runTest(part: AssignmentConfig.RunTest, repoDir: File) {
        val srcDir = File(repoDir, "src")
        srcDir.mkdirs()
        for (filter in part.filter) {
            val rmMe = File(repoDir, filter)
            rmMe.deleteRecursively()
            assert (!rmMe.exists())
        }
        copyProvidedFiles(part.dirName, srcDir)
        val libs = part.libs
        if (libs != null) {
            val libsDir = File(configBaseDir, ".libs")
            for (lib: String in libs) {
                var libFile = File(libsDir, lib)
                if (!libFile.exists()) {
                    libFile = File(libsDir, "${courseConfig.courseName}/$lib")
                }
                val dest = File(repoDir, lib)
                copyLib(libFile, dest)
            }
        }
        val isKotlin = compileProgram(repoDir, part.classpath)
        val classpath = if (isKotlin) { part.classpath + ":./kotlin.jar" } else { part.classpath }
        if (runMode) {
            val result = if (textMode) {
                runCommand(repoDir, "java", "-cp", classpath, "-Djava.awt.headless=true",
                        part.mainClass, "-run", mode = Run.INTERACTIVE)
            } else {
                runCommand(repoDir, "java", "-cp", classpath,
                        part.mainClass, "-run", mode = Run.INTERACTIVE)
            }
            println(result)
        } else {
            runCommand(repoDir, "java", "-cp", classpath, part.mainClass, mode=Run.INTERACTIVE)
        }
    }

    private fun copyLib(libFile: File, dest: File) {
        if (libFile.isDirectory()) {
            dest.mkdirs()
            for (f in libFile.listFiles()) {
                if (f.name.startsWith(".")) {
                    continue    // ignore
                }
                copyLib(f, File(dest, f.name))
            }
        } else {
            Files.copy(libFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    public fun checkUML(part: AssignmentConfig.CheckUML, srcDir: File) {
        val yedFile = File(srcDir, part.flleName)
        if (!yedFile.exists()) {
            throw TestFailed("$yedFile does not exist")
        }
        val model = YedUMLModel(yedFile)
        model.readYed()
        model.dump()
        for (c in part.classes) {
            if (model.types.getTypeStrict(c) == null) {
                throw TestFailed("${yedFile.name} does not contain a type called $c")
            }
        }
    }

    public fun checkFiles(part: AssignmentConfig.CheckFiles, srcDir: File) {
        for (arg in part.files) {
            if (arg is String) {
                val f = File(srcDir, arg)
                if (!f.exists()) {
                    throw TestFailed("Could not find the file $arg")
                }
            } else {
                @Suppress("UNCHECKED_CAST")
                arg as List<String>
                var found = false;
                for (alt: String in arg) {
                    found = found || File(srcDir, alt).exists()
                }
                if (!found) {
                    throw TestFailed("Could not find a file with any of these names:  $arg")
                }
            }
        }
    }

    //
    // Returns true if something copied
    //
    private fun copyFiles(from: File, to: File, fromName: String, condition: (name : String) -> Boolean) : Boolean {
        var result = false
        val fileNamesCopiedLC = mutableMapOf<String, File>()
        for (f in to.listFiles()) {
            val clash = fileNamesCopiedLC.putIfAbsent(f.name.toLowerCase(), f)
            if (clash != null) {
                throw TestFailed("${clash.name} and ${f.name} ($fromName) are the same in lower case.")
            }
        }
        for (f in from.listFiles()) {
            if (f.isFile && condition(f.name)) {
                val dest = File(to, f.name)
                val nameLC = f.name.toLowerCase()
                val clash = fileNamesCopiedLC.putIfAbsent(nameLC, f)
                if (clash != null) {
                    throw TestFailed("${clash.name} and ${f.name} ($fromName) are the same in lower case.")
                }
                if (dest.exists()) {
                    throw TestFailed("Cannot copy $f.name ($fromName) - file already exists!")
                } else {
                    println("    Copying $fromName ${f.name} to ${reasonableName(to.parentFile)}.")
                    Files.copy(f.toPath(), File(to, f.name).toPath())
                }
                result = true
            } else if (f.isDirectory && !(f.name.startsWith("."))) {
                val newTo = File(to, f.name);
                newTo.mkdirs()
                val r = copyFiles(File(from, f.name), newTo, from.name, condition)
                result = result || r
            }
        }
        return result
    }

    private fun reasonableName(f : File) : String  {
        var curr = f
        while (curr.name == "src" || curr.name == ".") {
            curr = curr.parentFile
        }
        return curr.name
    }
}