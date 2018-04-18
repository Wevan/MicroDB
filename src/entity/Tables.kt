package entity

import java.io.Serializable
import java.util.ArrayList

class Tables : Serializable {
    var tables: MutableList<Table> = ArrayList()

    companion object {


        private const val serialVersionUID = 1L
    }


}