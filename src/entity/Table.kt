package entity

import java.io.Serializable

class Table : Serializable {
    var name: String? = null
    var mp: MutableMap<String, String> = HashMap<String, String>()

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj is Table) {
            val t = obj as Table?
            return if (name == t!!.name)
                true
            else
                false
        }
        return super.equals(obj)
    }

    constructor() {}
    constructor(s: String) {
        this.name = s
    }

    companion object {

        private const val serialVersionUID = 1L
    }
}
