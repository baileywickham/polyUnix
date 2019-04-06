import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Deals with a git repo, once we've cloned it from github
 */

class GitRepo (
        val dir : File,
        val uri : String
){
    fun checkoutRevisionBefore(date: String) {
        val rev = runCommand(dir, "git", "rev-list", "-n", "1", "--before=$date", "origin").trim()
        if (rev == "") {
            throw TestFailed("No git repo version on or before $date")
        }
        runCommand(dir, "git", "checkout", rev, "--quiet")
    }

    fun checkoutMaster() {
        runCommand(dir, "git", "checkout", "master")
    }

    fun add(file: File, force: Boolean = false) {
        if (force) {
            runCommand(dir, "git", "add", "-f", file.relativeTo(dir).path)
        } else {
            runCommand(dir, "git", "add", file.relativeTo(dir).path)
        }
    }

    fun commitAndPush() {
        runCommand(dir, "git", "commit", "--allow-empty", "-m", "checkgit run")
        runCommand(dir, "git", "push")
    }
}

object Github  {

    var useSSHURIs = true

    fun getRepo(orgName: String, name: String, dir: File) : GitRepo {
        val gitURI = if (useSSHURIs) {
            "git@github.com:$orgName/$name.git"
        } else {
            "https://github.com/$orgName/$name"
        }
        runCommand(dir, "git", "clone", gitURI, name, mode=Run.INTERACTIVE)
        return GitRepo(File(dir, name), gitURI)
    }
}


enum class Deadline (
        val daysLate : Int,
        val triggerFile : String?
) {
    ON_TIME(0, null),
    FIVE_OFF(1, "late_5_percent_off.txt"),
    FIFTEEN_OFF(2, "late_15_percent_off.txt"),
    IGNORE_DEADLINE(365 * 10, "ignore_deadline.txt"),
    SECRET_PROFESSOR(1000, "secret_professor.txt");

    companion object {
        fun checkFor(f: File, repo: GitRepo, silent: Boolean) : Boolean {
            val found = f.exists()
            if (silent) {
                // say nothing
            } else {
                val message = if (found) "found" else "not found"
                println("${f.relativeTo(repo.dir)} $message")
            }
            return found
        }
        fun get(dir : File, repo: GitRepo): Deadline {
            val result = if (checkFor(File(dir, SECRET_PROFESSOR.triggerFile), repo, true)) {
                SECRET_PROFESSOR
            } else if (checkFor(File(dir, IGNORE_DEADLINE.triggerFile), repo, false)) {
               IGNORE_DEADLINE
            } else if (checkFor(File(dir, FIFTEEN_OFF.triggerFile), repo, false)) {
                FIFTEEN_OFF
            } else if (checkFor(File(dir, FIVE_OFF.triggerFile), repo, false)) {
                FIVE_OFF
            } else {
                ON_TIME
            }
            return result
        }

        private val GITHUB_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        public fun addDays(dateTimeString: String, days: Int) : String {
            val formatter = GITHUB_DATE_FORMATTER
            val orig = LocalDateTime.parse(dateTimeString, formatter)
            return formatter.format(orig.plusDays(days.toLong()))

        }

        public fun addMinutes(dateTimeString: String, minutes: Int) : String {
            val formatter = GITHUB_DATE_FORMATTER
            val orig = LocalDateTime.parse(dateTimeString, formatter)
            return formatter.format(orig.plusMinutes(minutes.toLong()))
        }
    }



    fun getCutoff(due : String) : String {
        return addDays(due, daysLate)
    }
}
