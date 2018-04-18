package comm


import entity.Data
import entity.Tables

import java.io.*

object Tools {
    private val name = "talbes"

    fun delSpaceEnter(str: String?): String? {
        var str: String? = str ?: return null
        str = str!!.replace("[\\s]+".toRegex(), " ")
        str = str.replace("\n".toRegex(), " ")
        return str.trim { it <= ' ' }
    }

    fun readTables(): Tables? {
        try {
            val fis = FileInputStream(name)
            val ois = ObjectInputStream(fis)
            val tb = ois.readObject() as Tables
            ois.close()
            fis.close()
            return tb
        } catch (e: FileNotFoundException) {

        } catch (e: ClassNotFoundException) {

        } catch (e: IOException) {

        }

        return null
    }

    fun writeTables(tb: Tables) {
        try {
            val fos = FileOutputStream(name)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(tb)
            oos.flush()
            oos.close()
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    fun writeData(tb: Data, tname: String) {
        try {
            val fos = FileOutputStream(tname)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(tb)
            oos.flush()
            oos.close()
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun readData(tname: String): Data? {
        try {
            val fis = FileInputStream(tname)
            val ois = ObjectInputStream(fis)
            val data = ois.readObject() as Data
            ois.close()
            fis.close()
            return data
        } catch (e: FileNotFoundException) {

        } catch (e: ClassNotFoundException) {

        } catch (e: IOException) {

        }

        return null
    }

}
