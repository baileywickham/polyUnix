package com.jovial.util

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL

class HttpResult (
        val input : Reader,
        val connection : HttpURLConnection
) {

    fun readJsonValue() : Any {
        val result = JsonIO.readJSON(input)
        input.close()
        return result
    }

}

fun httpGet(server: URL, headers: Map<String, String>) : HttpResult {
    val conn = server.openConnection() as HttpURLConnection
    for ((key, value) in headers) {
        conn.setRequestProperty(key, value)
    }
    return HttpResult(getConnectionReader(conn), conn)
}

fun getConnectionReader(conn : HttpURLConnection) : Reader {
    val enc = conn.contentType
    val charset =
            if (enc == null) {
                "UTF-8"
            } else {
                val pos = enc.indexOf("charset=", ignoreCase = true)
                if (pos < 0) {
                    "UTF-8"
                } else {
                    val s = enc.drop(pos + 8)
                    val p = s.indexOf(";")
                    if (p < 0) {
                        s.toUpperCase()
                    } else {
                        s.substring(0, p).toUpperCase()
                    }
                }
            }
    try {
        val input = BufferedReader(InputStreamReader(conn.inputStream, charset))
        return input
    } catch (ex : IOException) {
        try {
            val err = BufferedReader(InputStreamReader(conn.errorStream, charset))
            println("Error from server:")
            while (true) {
                val c = err.read()
                if (c == -1) {
                    break;
                }
                print(c.toChar())
            }
            println()
            println()
        } catch (ex: Exception) {
            println("Error trying to read error message:  $ex")
        }
        throw ex
    }
}

