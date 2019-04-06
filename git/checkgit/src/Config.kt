import com.jovial.util.JsonIO
import java.io.File

public class CourseConfig {
    val githubOrg : String    // Like "CalPolyCPE203-S18", the github organization for the class's repos
    val turninName : String
    val courseName : String

    constructor(configDir: File, courseName: String) {
        val json : Map<String, Any> = with (File(configDir, "config.json").bufferedReader()) {
            @Suppress("UNCHECKED_CAST")
            JsonIO.readJSON(this) as Map<String, Any>
        }
        githubOrg = json["github_org"] as String
        turninName = json["turnin_name"] as String? ?: "turnin"
        this.courseName = courseName
    }
}

public class AssignmentConfig {

    val dir : File
    val due : String
    val parts : List<Part>

    abstract class Part(
            val dirName : String,
            val notes : List<String>?,
            val useTestDir : Boolean
    ) {
        open fun check(checker: GitChecker, repoDir: File, destDir: File) {
            if (notes != null && notes.size != 0) {
                println()
                println("****************************************************")
                println("NOTE:")
                for (line in notes) {
                    println("     $line")
                }
                println("****************************************************")
                println()
            }
        }
    }

    /**
     *  Runs a Java test application in the repo.  A test application doesn't
     *  include any user code.
     */
    open class RunTest(
            dirName: String,
            val filter: List<String>,
            notes: List<String>?,
            val mainClass : String,
            val classpath: String,
            val libs : List<String>?)
        : Part(dirName, notes, useTestDir = false)
    {
        override fun check(checker: GitChecker, repoDir: File, destDir: File) {
            super.check(checker, repoDir, destDir)
            checker.runTest(this, repoDir)
        }
    }

    /**
     *  Runs a Java application in the repo.  If -run is given, pass it on as a command-line
     *  arg to the program that is run.
     */
    class RunJava(
            dirName: String,
            filter: List<String>,
            notes: List<String>?,
            mainClass : String,
            classpath: String,
            libs : List<String>?)
        : RunTest(dirName, filter, notes, mainClass, classpath, libs)
    {
        override fun check(checker: GitChecker, repoDir: File, destDir: File) {
            checker.checkSrcExists(repoDir)
            super.check(checker, repoDir, destDir)
        }
    }

    /**
     *  Checks a yEd UML file.
     */
    class CheckUML(
            dirName: String,
            notes: List<String>?,
            val flleName : String,
            val classes : List<String>)
        : Part(dirName, notes, useTestDir = false)
    {
        override fun check(checker: GitChecker, repoDir: File, destDir: File) {
            super.check(checker, repoDir, destDir)
            checker.checkUML(this, repoDir)
        }
    }

    /**
     * Checks for existences of a file.  A list can be given instead of a
     * string to list alternates.
     */
    class CheckFiles(
            dirName: String,
            notes: List<String>?,
            val files : List<Any>)
        :Part(dirName, notes, useTestDir = false)
    {
        override fun check(checker: GitChecker, repoDir: File, destDir: File) {
            super.check(checker, repoDir, destDir)
            checker.checkFiles(this, repoDir)
        }
    }

    @Suppress("UNCHECKED_CAST")
    constructor(dir: File) {
        this.dir = dir
        val json : Map<String, Any> = with (File(dir, "config.json").bufferedReader()) {
            JsonIO.readJSON(this) as Map<String, Any>
        }
        due = json["due"] as String
        parts = mutableListOf<Part>()
        for (part in (json["parts"] as List<Any>)) {
            part as Map<String, Any>
            val dirName = part["dir"] as String
            var notes : List<String>? = null
            val notesJ = part["notes"] as List<Any>?
            if (notesJ != null) {
                notes = mutableListOf<String>()
                for (o in notesJ) {
                    notes.add(o as String)
                }
            }
            if (part["run_java"] != null) {
                val runJava = part["run_java"] as String
                val classpath = part["classpath"] as String
                val libs = part["libs"] as List<String>?
                val filter: List<String> = (part["filter"] as List<String>?) ?: emptyList()
                parts.add(RunJava(dirName, filter, notes, runJava, classpath, libs))
            } else if (part["run_test"] != null) {
                val runTest = part["run_test"] as String
                val classpath = part["classpath"] as String
                val libs = part["libs"] as List<String>?
                val filter: List<String> = (part["filter"] as List<String>?) ?: emptyList()
                parts.add(RunTest(dirName, filter, notes, runTest, classpath, libs))
            } else if (part["checkUML"] != null) {
                val checkUML = part["checkUML"] as Map<String, Any>
                val file = checkUML["file"] as String
                val classes = checkUML["classes"] as List<String>
                parts.add(CheckUML(dirName, notes, file, classes))
            } else if (part["checkFiles"] != null) {
                val checkFiles = part["checkFiles"] as List<Any>
                parts.add(CheckFiles(dirName, notes, checkFiles))
            } else {
                throw RuntimeException("Internal config error in $dir")
            }
        }
    }
}

