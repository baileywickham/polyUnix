import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * checkgit:  Try out the contents of a git repository.
 *
 * This program is designed to be run by students who have submitted an assignment
 * to a git repository.  It runs some basic validation tests against the repo, to
 * give the student immediate feedback.  It's intended to address problems like
 * incorrect directory names, compilation errors, undesirable API signatures,
 * course names, and other properties that can be easily checked.  It also checks
 * to make sure no two file names only differ in case (because foo.java and Foo.java
 * are different files on most Unix filesystems, and teh same file on most Mac
 * and Windows filesystems), and it issues a warning if any source files have tab
 * characters in them.
 *
 * Usage:
 *
 *      checkgit <course> <assignment> [other options (see usage)]
 *
 * Note that the shell script that launchs us gives us the checkgit
 * base directory as args[0].
 */

val CHATTER = false
private val GRACE_MINUTES = 61;

class TestFailed : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(msg: String, ex: Exception) : super(msg, ex)
}

private fun usage() {
    println()
    println("Usage:  checkgit <git-name> <course> <assignment> [ -dir <dir> | -run ]")
    println()
    println("        <git-name>   is your GitHub login name.")
    println("        <course>     is the course this is for (203, 305, ...).")
    println("        <assignment> is the assignment name (or say \"list\" to list them).")
    println()
    println("        -ssh         will use a GitHub ssh URI instead of https.")
    println("                     (see https://bit.ly/2kdF9Md)")
    println("        -dir <dir>   will create dir, and use it for checking out your repo.")
    println("                     dir will contain your repo as of the deadline date.")
    println("        -run         will run the assignment program(s), if applicable.")
    println("        -trun        like -run, but sets up input for Spritely text mode.")
    println("        -log         Will create a checkgit log entry in your feedback repo.")
    println()
    println("Leave course or assignment blank for a list.")
    println()
    println("In your assignment directory, you can change the deadline checkgit")
    println("applies by adding a file with a blank line in it, named as follows:")
    for (v : Deadline in Deadline.values()) {
        if (v != Deadline.SECRET_PROFESSOR && v != Deadline.ON_TIME) {
            print("    ${v.triggerFile}  extends deadline by ${v.daysLate} day(s).")
            if (v == (Deadline.IGNORE_DEADLINE)) {
                println("  Ignored by grader.")
            } else {
                println()
            }
        }
    }
    println()
    System.exit(1)
}

/**
 * Find the directory where the materials named "name" are.  If not found,
 * generate a TestFailed exception that lists the allowable values.
 */
fun findMaterials(description: String, base: File, name: String) : File {
    val result = File(base, name)
    if (result.exists() && result.isDirectory) {
        return result
    }
    val message = StringBuffer()
    message.append("I was unable to find the $description named $name.\n")
    message.append("\n")
    val dirs = base.listFiles().filter { it.isDirectory() }
    if (dirs.size == 0) {
        message.append("I don't seem to have any entries configured for $description.")
        message.append("\n")
    } else {
        message.append("Here are the names I know for \"$description\":\n")
        message.append("\n")
        for (f in base.listFiles().sorted()) {
            if (f.name.startsWith(".")) {
                // ignore
            } else if (f.isDirectory()) {
                var details = ""
                val config = File(f, "config.json")
                if (config.exists()) {
                    val json : Map<String, Any> = with (config.bufferedReader()) {
                        @Suppress("UNCHECKED_CAST")
                        com.jovial.util.JsonIO.readJSON(this) as Map<String, Any>
                    }
                    val dueDate = json["due"]
                    if (dueDate is String) {
                        details = " -- due ${Deadline.addMinutes(dueDate, -GRACE_MINUTES)}"
                    }
                }
                message.append("    ${f.name}${details}\n")
            }
        }
        if (GRACE_MINUTES != 0) {
            message.append("\n    Note:  A grace period of $GRACE_MINUTES minutes is added to the due date.\n" )
        }
    }
    throw TestFailed(message.toString())
}

//
// Get an argument, or "<not set>" if not there
//
private fun getArg(args: List<String>, index: Int) : String {
    if (index < args.size) {
        return args[index];
    } else {
        return "<not set>"
    }
}

fun main(args:  Array<String>) {
    if (args.size < 2) {
        usage()
    }
    try {
        val baseDir = File(args[0]);
        if (!baseDir.exists()) {
            throw RuntimeException("${baseDir.absolutePath} does not exist")
        }
        val otherArgs = mutableListOf<String>()
        var i = 1;
        var runMode = false
        var textMode = false
        var writeLogToFeedback = false
        var userCheckoutDir: File? = null
        while (i < args.size) {
            if (args[i] == "-ssh") {
                Github.useSSHURIs = true
            } else if (args[i] == "-log") {
                writeLogToFeedback = true
            } else if (args[i] == "-run") {
                runMode = true
            } else if (args[i] == "-trun") {
                // With -trun, the running shell script is expected to have
                // done "stty -echo cbreak"
                runMode = true
                textMode = true
            } else if (args[i] == "-dir" && (i+1) < args.size) {
                userCheckoutDir = File(args[++i])
            } else {
                otherArgs.add(args[i])
            }
            i++
        }
        if (otherArgs.size > 3) {
            println("Unrecognized argument:  ${otherArgs[3]}")
            usage()
        }
        val githubLogin = getArg(otherArgs, 0)
        val courseName = getArg(otherArgs, 1)
        val assignmentName = getArg(otherArgs, 2)
        println()
        println("Github login:  $githubLogin")
        println("Course:  $courseName")
        println("Assignment:  $assignmentName")
        println()
        val course: File = findMaterials("course", baseDir, courseName)
        val assignment: File = findMaterials("assignment", course , assignmentName)
        val checker = GitChecker(baseDir, githubLogin, CourseConfig(course, courseName), AssignmentConfig(assignment),
                                 userCheckoutDir, runMode, textMode, writeLogToFeedback)
        checker.check()
    } catch (ex: TestFailed) {
        ex.printStackTrace(System.out)
        println()
        println("Something went wrong checking your repository.")
        println()
        if (ex.cause != null) {
            ex.cause.printStackTrace()
            println()
        }
        println("Error message:  $ex")
        println()
        System.exit(1)
    } catch (ex: Exception) {
        ex.printStackTrace()
        println()
        System.exit(1)
    }
    System.exit(0)
}
