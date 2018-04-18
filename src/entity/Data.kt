package entity

import java.io.Serializable

class Data : Serializable {
    var data: MutableList<Any?> = ArrayList()

    companion object {
        private const val serialVersionUID = 1L
    }
}