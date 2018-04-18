package check

import comm.Tools
import entity.Data
import entity.Table
import exception.MyException
import java.io.File
import java.util.*

class Checker @Throws(MyException::class)
constructor(s: String) {
    var comm: String? = null

    init {
        this.comm = Tools.delSpaceEnter(s)!!.trim { it <= ' ' }
        checkMatch()
    }

    @Throws(MyException::class)
    private fun checkMatch() {
        val len = comm!!.length
        val stack = IntArray(comm!!.length)
        var top = -1
        for (i in 0 until len) {
            if (comm!![i] == '(') {
                stack[++top] = '('.toInt()
            } else if (comm!![i] == ')') {
                if (top >= 0) {
                    top--
                } else {
                    throw MyException("括号不匹配！！")
                }
            }
        }

    }

    @Throws(MyException::class)
    fun checkCreate(): Table? {
        val words = comm!!.split("[\\s]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val table = Table()

        if (words[1].toUpperCase() != "TABLE") {
            throw MyException("无法解析 创建表语句！")
        }
        var na = ""
        for (i in 13 until comm!!.length) {
            if (comm!![i] == '(') break
            na += comm!![i]
        }

        if (!na.trim { it <= ' ' }.matches("[a-zA-Z_]+".toRegex())) {

            throw MyException("$na 表名创建无效，只能为大小写字母下划线！")
        }
        //设置表名
        table.name = na.trim { it <= ' ' }
        var st = ""
        for (i in 0 until comm!!.length) {
            if (comm!![i] == '(') {
                st = comm!!.substring(i + 1, comm!!.length - 1)
                break
            }
        }
        if (st == "") {
            throw MyException("括号不匹配！")
        }
        val w = st.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in w.indices) {
            val name = Tools.delSpaceEnter(w[i])!!.trim { it <= ' ' }
            val c = name.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (c.size != 2) {
                throw MyException(w[i] + ":属性解析错误！")
            } else {
                if (c[0].trim().matches("[a-zA-Z0-9_]+".toRegex()) == false || c[0][0] >= '0' && c[0][0] <= '9') {
                    throw MyException(c[0] + ":属性名定义不合法")
                } else {
                    when {
                        c[1].trim().matches("int".toRegex()) -> table.mp[c[0].trim { it <= ' ' }] = "int"
                        c[1].trim().matches("char\\([0-9]+\\)".toRegex()) -> table.mp.put(c[0].trim(), c[1].trim { it <= ' ' })
                        else -> throw MyException(c[1] + ":属性类型不合法！")
                    }
                }
            }
        }
        return table
    }

    @Throws(MyException::class)
    fun checkInsert(): Boolean {
        val words = this.comm!!.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (words[0].toLowerCase() == "insert" && words[1].toLowerCase() == "into") {
            var name = ""
            var index = 0
            for (i in 12 until comm!!.length) {
                if (comm!![i] == '(') {
                    name = name.trim { it <= ' ' }
                    index = i + 1
                    break
                }
                name += comm!![i]
            }
            val t = Table()
            t.name = name
            if (!Tools.readTables()!!.tables.contains(t)) {
                throw MyException("$name:表名不存在，请检查！")
            }

            val tbs = Tools.readTables()
            var table: Table? = null
            for (tb in tbs!!.tables) {
                if (tb.name == name) {
                    table = tb
                    break
                }
            }
            var colm = ""
            for (i in index until comm!!.length) {
                if (comm!![i] == ')') {
                    index = i + 1
                    break
                }
                colm += comm!![i]
            }
            val cs = colm.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var `val` = ""
            for (i in index until comm!!.length) {
                if (comm!![i] == '(') {
                    index = i + 1
                    break
                }
                `val` += comm!![i]
            }
            `val` = `val`.trim { it <= ' ' }
            if (`val`.toLowerCase() == "values" == false) {
                throw MyException("解析错误！")
            }
            var num = ""
            for (i in index until comm!!.length) {
                if (comm!![i] == ')') {
                    break
                }
                num += comm!![i]
            }
            val vals = num.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val va = ArrayList<Any>()
            if (cs.size != vals.size) {
                throw MyException("插入的值不对应请检查！")
            }
            for (i in cs.indices) {
                val k = table!!.mp[cs[i].trim { it <= ' ' }]
                if (null == k) {
                    throw MyException(cs[i] + ":属性不存在，请检查！")
                } else {
                    if (k == "int") {
                        if (vals[i].trim { it <= ' ' }.matches("[0-9]+".toRegex()) == false) {
                            throw MyException(vals[i].trim { it <= ' ' } + ":插入的值与表定义的类型不一致！插入失败")
                        } else {
                            va.add(Integer.valueOf(vals[i]))
                        }
                    } else {
                        vals[i] = vals[i].trim { it <= ' ' }
                        if (vals[i][0] != '\'' || vals[i][vals[i].length - 1] != '\'') {
                            throw MyException(vals[i].trim { it <= ' ' } + ":插入的值与表定义的类型不一致！插入失败")
                        }
                        va.add(vals[i])
                    }

                }
            }
            var dt = Tools.readData("$name.tb")
            if (dt == null) {
                dt = Data()
            }
            val tbs2 = Tools.readTables()!!.tables
            var tbn: Table? = null
            for (tb2 in tbs2) {
                if (tb2.name == name) {
                    tbn = tb2
                    break
                }
            }

            val s = tbn!!.mp.keys
            val i = s.iterator()

            while (i.hasNext()) {

                val k = i.next()
                for (j in cs.indices) {
                    if (cs[j].trim { it <= ' ' } == k) {
                        //						System.out.println("#");
                        if (va[j] is String) {
                            var oi = va[j] as String
                            oi = oi.replace("'", "")
                            dt.data.add(oi)
                        } else
                            dt.data.add(va[j])
                        break
                    }
                }
            }

            //			for (Object object : va) {
            //				dt.getData().add(object);
            //			}
            Tools.writeData(dt, "$name.tb")
            println("数据插入成功！")
        } else {
            throw MyException("无法解析插入命令！")
        }
        return true
    }

    @Throws(MyException::class)
    fun checkDel(): Boolean {
        //delete from name where cnamm=value;
        var num = 0
        val w = comm!!.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (w[0].trim().toLowerCase() != "delete") {
            throw MyException("解析语句错误!删除失败！")
        } else if (w[1].trim { it <= ' ' }.toLowerCase() == "from" == false) {
            throw MyException("from缺失！删除失败！")
        }
        val tbs = Tools.readTables()
        if (w.size < 3) {
            throw MyException("参数不足！删除失败！")
        } else if (w.size == 3) {
            if (tbs!!.tables.contains(Table(w[2].trim { it <= ' ' })) == false) {
                throw MyException("表名错误！请检查！~")
            } else {
                val file = File(w[2].trim { it <= ' ' } + ".tb")
                if (file.exists()) {
                    file.delete()
                    println("删除成功！")
                }
            }
        } else {
            if (tbs!!.tables.contains(Table(w[2].trim { it <= ' ' })) == false) {
                throw MyException("表名错误！请检查！~")
            }
            var t: Table? = null
            for (tb in tbs.tables) {
                if (tb.name == w[2].trim { it <= ' ' }) {
                    t = tb
                    break
                }
            }

            if (w[3].trim { it <= ' ' } == "where" == false) {
                throw MyException("解析错误！")
            }
            val last = this.comm!!.split("where".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }
            val condition = last.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val a = IntArray(t!!.mp.size + 2)
            val ans = ArrayList<Any>()
            for (i in condition.indices) {
                val kv = condition[i].trim { it <= ' ' }.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (kv.size != 2) {
                    throw MyException(condition[i] + "where条件解析错误，请检查！")
                }
                kv[0] = kv[0].trim { it <= ' ' }
                kv[1] = kv[1].trim { it <= ' ' }
                if (t.mp[kv[0]] == null) {
                    throw MyException(kv[0] + ":列名不存在！")
                }
                if (t.mp[kv[0]] == "int") {
                    if (kv[1].matches("[0-9]+".toRegex()) == false) {
                        throw MyException(kv[1] + ":与定义列的属性不符，请检查！")
                    } else {
                        ///code
                        val s = t.mp.keys
                        val it = s.iterator()
                        var de = 0

                        while (it.hasNext()) {
                            val k = it.next()
                            if (k == kv[0]) {
                                a[de] = 1
                                ans.add(kv[1])
                                break
                            }
                            de++
                        }

                    }
                } else {
                    if (!(kv[1][0] == '\'' && kv[1][kv[1].length - 1] == '\'')) {
                        throw MyException(kv[1] + ":与定义列的属性不符，请检查！")
                    } else {
                        kv[1] = kv[1].replace("'", "")
                        //code
                        val s = t.mp.keys
                        val it = s.iterator()
                        var de = 0

                        while (it.hasNext()) {
                            val k = it.next()
                            if (k == kv[0]) {
                                if (a[de] == 1) {
                                    throw MyException("解析错误！")
                                }
                                a[de] = 1
                                ans.add(kv[1])
                                break
                            }
                            de++
                        }
                    }

                }
            }
            val dt = Tools.readData(t.name!! + ".tb")
            val stack = IntArray(333)
            var top = -1
            run {
                var i = 0
                while (i < dt!!.data.size) {
                    var ind = 0
                    var ret = 0
                    for (j in i until i + t.mp.size) {
                        if (a[j - i] == 1) {
                            if (dt.data[j] is Int) {

                                if (dt.data[j] as Int == Integer.valueOf(ans[ind] as String)) {
                                    ret++

                                }
                            }
                            if (dt.data[j] is String) {
                                if (dt.data[j] as String == ans[ind] as String) {
                                    ret++
                                }
                            }
                            ind++
                        }
                    }
                    if (ret == ans.size) {
                        stack[++top] = i / t.mp.size
                    }
                    i += t.mp.size
                }
            }
            var yu = 0
            num = top + 1
            var i = 0
            while (i < dt!!.data.size) {
                if (yu <= top && stack[yu] == i / t.mp.size) {
                    for (j in i until i + t.mp.size) {
                        dt.data[j] = null
                    }
                    yu++
                }
                i += t.mp.size
            }
            val nullArr = ArrayList<Int?>()
            nullArr.add(null)
            dt.data.removeAll(nullArr)
            Tools.writeData(dt, t.name + ".tb")

        }

        println(num.toString() + " 条记录删除成功！")
        return true
    }

    @Throws(MyException::class)
    fun checkSelect(): Boolean {
        val w = comm!!.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (w[0].trim { it <= ' ' }.toLowerCase() == "select" == false) {
            throw MyException("解析错误，请检查！")
        }
        if (w[1].trim { it <= ' ' } == "*") {
            if (w[2].trim { it <= ' ' } == "from" == false) {
                throw MyException("解析错误，请检查！")
            }
            if (Tools.readTables()!!.tables.contains(Table(w[3].trim { it <= ' ' })) == false) {
                throw MyException(w[3] + ":表不存在，请检查！")
            }

            var tb: Table? = null
            for (t in Tools.readTables()!!.tables) {
                if (t.name == w[3].trim { it <= ' ' }) {
                    tb = t
                    break
                }
            }

            /*
			   create table kl(name char(3),age int);
			   insert into kl(name,age) values('d',3);
			   insert into kl(name,age) values('d',3);
			   insert into kl(name,age) values('d',3);
			   insert into kl(name,age) values('d',3);
			   insert into kl(name,age) values('d',322);
			   insert into kl(name,age) values('d',23);
			   select * from kl;
			 */
            val s = tb!!.mp.keys
            val ii = s.iterator()
            println(w[3] + " : ")
            while (ii.hasNext()) {
                val k = ii.next()
                print("$k     ")
            }
            println()
            val dt = Tools.readData(w[3].trim { it <= ' ' } + ".tb")
            if (dt != null)
                println("选择出了：" + dt.data.size / tb.mp.size + " 条记录！")
            else {
                println("选择出了：" + 0 + " 条记录！")
                return true
            }
            val size = dt.data.size
            for (i in 0 until size) {
                print(dt.data[i].toString() + "   ")
                if ((i + 1) % tb.mp.size == 0 && i != 0) {
                    println()
                }
            }

        } else {
            val cls = comm!!.split("from".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (cls.size < 2) {
                throw MyException("解析错误，缺失from！")
            }

            val tnm = cls[1].trim { it <= ' ' }.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
            val ts = Tools.readTables()
            if (ts!!.tables.contains(Table(tnm)) == false) {
                throw MyException("$tnm:表不存在！")
            }
            var t = Table()
            cls[0] = cls[0].substring(7).trim { it <= ' ' }
            val p = cls[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in 0 until ts.tables.size) {
                if (ts.tables[i].name == tnm) {
                    t = ts.tables[i]
                    break
                }
            }
            for (i in p.indices) {
                if (t.mp[p[i].trim { it <= ' ' }] == null) {
                    print(p[i].trim { it <= ' ' })
                    throw MyException(":列名不存在，请检查！")
                }
            }
            val s = t.mp.keys
            val ii = s.iterator()
            println("$tnm : ")
            val a = IntArray(22)
            var ui = 0
            while (ii.hasNext()) {
                val k = ii.next()
                for (i in p.indices) {
                    if (p[i].trim { it <= ' ' } == k) {
                        print(p[i].trim { it <= ' ' } + "       ")
                        a[ui] = 1
                        break
                    }
                }
                ui++
            }
            println("")
            val dt = Tools.readData(tnm.trim { it <= ' ' } + ".tb")
            println("选择出了：" + dt!!.data.size / t.mp.size + " 条记录！")
            val size = dt.data.size
            for (i in 0 until size) {
                if (a[i % t.mp.size] == 1) {
                    print(dt.data[i].toString() + "   ")
                }
                if ((i + 1) % t.mp.size == 0 && i != 0) {
                    println()
                }
            }


        }
        return false
    }

    @Throws(MyException::class)
    fun checkUpdate(): Boolean {
        var num = 0
        val w = comm!!.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (w[0].trim { it <= ' ' }.toLowerCase() == "update" == false) {
            throw MyException("解析语句错误!更新失败！")
        }
        val tbs = Tools.readTables()
        if (w.size < 3) {
            throw MyException("参数不足！更新失败！")
        } else {
            if (tbs!!.tables.contains(Table(w[1].trim { it <= ' ' })) == false) {
                throw MyException("表名错误！请检查！~")
            }
            var t: Table? = null
            for (tb in tbs.tables) {
                if (tb.name == w[1].trim { it <= ' ' }) {
                    t = tb
                    break
                }
            }

            if (w[4].trim { it <= ' ' } == "where" == false) {
                throw MyException("解析错误！")
            }
            val last = this.comm!!.split("where".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }
            val setkey = this.comm!!.split("set".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }
            val skey = setkey.split("where".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
            val condition = last.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val setkeys = skey.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val a = IntArray(t!!.mp.size + 2)
            val ans = ArrayList<Any>()
            for (i in condition.indices) {
                val kv = condition[i].trim { it <= ' ' }.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                //String [] resets = setkeys[i].trim().split("=");
                if (kv.size != 2) {
                    throw MyException(condition[i] + "where条件解析错误，请检查！")
                }
                kv[0] = kv[0].trim { it <= ' ' }
                kv[1] = kv[1].trim { it <= ' ' }
                if (t.mp[kv[0]] == null) {
                    throw MyException(kv[0] + ":列名不存在！")
                }
                if (t.mp[kv[0]] == "int") {
                    if (kv[1].matches("[0-9]+".toRegex()) == false) {
                        throw MyException(kv[1] + ":与定义列的属性不符，请检查！")
                    } else {
                        ///code
                        val s = t.mp.keys
                        val it = s.iterator()
                        var de = 0

                        while (it.hasNext()) {
                            val k = it.next()
                            if (k == kv[0]) {
                                a[de] = 1
                                ans.add(kv[1])
                                break
                            }
                            de++
                        }

                    }
                } else {
                    if (!(kv[1][0] == '\'' && kv[1][kv[1].length - 1] == '\'')) {
                        throw MyException(kv[1] + ":与定义列的属性不符，请检查！")
                    } else {
                        kv[1] = kv[1].replace("'", "")
                        //code
                        val s = t.mp.keys
                        val it = s.iterator()
                        var de = 0

                        while (it.hasNext()) {
                            val k = it.next()
                            if (k == kv[0]) {
                                if (a[de] == 1) {
                                    throw MyException("解析错误！")
                                }
                                a[de] = 1
                                ans.add(kv[1])
                                break
                            }
                            de++
                        }
                    }

                }
            }
            val dt = Tools.readData(t.name!! + ".tb")
            val stack = IntArray(333)
            var top = -1
            run {
                var i = 0
                while (i < dt!!.data.size) {
                    var ind = 0
                    var ret = 0
                    for (j in i until i + t.mp.size) {
                        if (a[j - i] == 1) {
                            if (dt.data[j] is Int) {

                                if (dt.data[j] as Int == Integer.valueOf(ans[ind] as String)) {
                                    ret++
                                }
                            }
                            if (dt.data[j] is String) {
                                if (dt.data[j] as String == ans[ind] as String) {
                                    ret++
                                }
                            }
                            ind++
                        }
                    }
                    if (ret == ans.size) {
                        stack[++top] = i / t.mp.size
                    }
                    i += t.mp.size
                }
            }
            var yu = 0
            num = top + 1
            var i = 0
            while (i < dt!!.data.size) {
                val resets = setkeys[0].trim { it <= ' ' }.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val reee = resets[1]
                if (yu <= top && stack[yu] == i / t.mp.size) {
                    for (j in i until i + t.mp.size) {
                        if (j % 2 == 0) {
                            dt.data.set(j, reee)
                        }
                    }
                    yu++
                }
                i += t.mp.size
            }
            Tools.writeData(dt, t.name!! + ".tb")
        }

        println(num.toString() + " 条记录更新成功！")
        return true
    }

    companion object {
        @Throws(MyException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val ch = Checker(" insert into wocha(name,age) values('df',8)")

            ch.checkInsert()
        }
    }
}

/*
#########欢迎使用简易数据库#########
create table ui(name char(23),age int) values('oij',44);
age int) values('oij':属性解析错误！
com.ht.exception.MyException
	at com.utitl.check.Checker.checkCreate(Checker.java:75)
	at com.ht.db.DB.main(DB.java:38)
create table ui(name char(23),age int)  ;
ui:表创建成功！~
insert into ui(name ,age) values('bd',44);

数据插入成功！
insert into ui(name ,age) values('bd1',442);
数据插入成功！
insert into ui(name ,age) values('bd2',42);
数据插入成功！
insert into ui(name ,age) values('bd3',14);
数据插入成功！
insert into ui(name ,age) values('bd2',44);
数据插入成功！
insert into ui(name ,age) values('b2d',44);
数据插入成功！
select * from ui;
ui : 
name     age     
选择出了：6 条记录！
bd   44   
bd1   442   
bd2   42   
bd3   14   
bd2   44   
b2d   44   
delete from ui where age=44;
删除成功！
select * from ui;
ui : 
name     age     
选择出了：5 条记录！
null   bd1   
442   bd2   
42   bd3   
14   bd2   
44   b2d   
44   
\
 */