package db

import check.Checker
import comm.Tools
import entity.Tables
import exception.MyException
import java.io.IOException

object DB {

    fun readComm(): String {
        var s = ""
        try {
            var r: Char = 0.toChar()
            do {
                r = System.`in`.read().toChar()
                if (r == ';') break
                s += r
            } while (r != ';')
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return s
    }

    @Throws(MyException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        println("====================MicroDB======================")
        while (true) {
            val h = readComm().trim { it <= ' ' }
            if (h == "exit") break
            val ch = Checker(h)

            try {

                if (ch.comm!![0] == 'c' || ch.comm!![0] == 'C') {
                    val tb = ch.checkCreate()
                    val t = Tools.readTables()
                    if (t == null) {
                        if (tb != null) {
                            val tbs = Tables()
                            tbs.tables.add(tb)
                            Tools.writeTables(tbs)
                            println(tb.name + ":表创建成功！~")
                        } else {

                        }
                    } else {
                        if (t.tables.contains(tb)) {
                            throw MyException(tb!!.name + ":表已经存在！创建失败~")
                        } else {
                            t.tables.add(tb!!)
                            Tools.writeTables(t)

                            println(tb.name + ":表创建成功！")
                        }

                    }
                } else if (ch.comm!![0] == 's' || ch.comm!![0] == 'S') {
                    ch.checkSelect()
                } else if (ch.comm!![0] == 'i' || ch.comm!![0] == 'I') {
                    ch.checkInsert()
                } else if (ch.comm!![0] == 'd' || ch.comm!![0] == 'D') {
                    ch.checkDel()
                } else if (ch.comm!![0] == 'u' || ch.comm!![0] == 'U') {
                    ch.checkUpdate()
                } else {
                    throw MyException("解析错误！")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        println("谢谢使用！")
    }

}
