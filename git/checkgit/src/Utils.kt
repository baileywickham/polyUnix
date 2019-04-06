import java.io.File
import java.util.*

import java.io.PrintWriter
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


private val random = Random()

enum class Run {
    NORMAL,             // Delivers a string as output
    NORMAL_SUPPRESS_OUTPUT,
    BG,
    INTERACTIVE,
    FAILURE_IS_AN_OPTION
}

class RunError(val command: List<String>, val output: String, message: String) : Exception(message) {

    override fun toString() : String {
        val msg = "${super.toString()}\n    $command\n"
        if (output == "") {
            return msg
        } else {
            return "$msg    output:  $output\n"
        }
    }
}

//
// Returns true on success
//
fun runCommand(dir : File, vararg command : String, mode : Run = Run.NORMAL) : String {
    return runCommand(dir, command.toList(), mode)
}

fun runCommand(dir : File, command : List<String>, mode : Run = Run.NORMAL) : String {
    val result = StringBuilder()
    print("    Running:  ")
    for (s in command) {
        print(s)
        print(' ')
    }
    println()
    val outRedirect =
            if (mode == Run.BG || mode == Run.INTERACTIVE) {
                ProcessBuilder.Redirect.INHERIT
            } else {
                ProcessBuilder.Redirect.PIPE
            }

    var pb = ProcessBuilder(command).directory(dir).redirectOutput(outRedirect)
    if (mode == Run.BG || mode == Run.INTERACTIVE) {
        pb = pb.redirectError(ProcessBuilder.Redirect.INHERIT)
    } else {
        pb = pb.redirectErrorStream(true)
    }
    if (mode == Run.INTERACTIVE) {
        pb = pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        pb = pb.redirectInput(ProcessBuilder.Redirect.INHERIT)
    }
    val p = pb.start()
    if (mode == Run.BG) {
        return ""
    } else if (mode != Run.INTERACTIVE) {
        p.inputStream.bufferedReader().use {
            while (true) {
                val c = it.read()
                if (c == -1) {
                    break;
                }
                result.append(c.toChar());
            }
        }
    }
    val returnCode = p.waitFor();
    if (mode == Run.NORMAL_SUPPRESS_OUTPUT) {
        result.setLength(0)
    } else if (CHATTER) {
        var s = result.toString()
        if (s.endsWith('\n')) {
            print(s)
        } else if (s != "") {
            println(s)
        }
    }
    if (returnCode != 0 && mode != Run.FAILURE_IS_AN_OPTION) {
        if (!CHATTER) {
            println(result.toString())
        }
        throw RunError(command, result.toString(), "return code $returnCode")
    }
    return result.toString()
}

/**
 * Returns a list of all elements randomized
 */
fun <T> List<T>.randomized(): List<T> {
    if (this.size > 1) {
        val result = ArrayList<T>(this.size);
        for (i in 0..(this.size - 1)) {
            result.add(this[i])
        }
        for (i in 0..(this.size - 1)) {
            val j = random.nextInt(this.size - 1)
            val tmp = result[i]
            result[i] = result[j]
            result[j] = tmp
        }
        return result
    } else {
        return this
    }
}


/**
 * Do a recursive descent of a directory hierarchy looking for a given file ending
 */
fun File.recursiveEndingWith(end : String) : List<File> {
    val result = mutableListOf<File>()
    for (f : File in listFiles()) {
        if (f.isDirectory) {
            for (g in f.recursiveEndingWith(end)) {
                result.add(g)
            }
        } else if (f.name.endsWith(end)) {
            result.add(f)
        }
    }
    return result
}

