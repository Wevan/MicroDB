package exception

class MyException(s: String) : Exception() {
    var em: String? = null

    init {
        this.em = s
        println(s)
    }

    companion object {
        /**
         *
         */
        private val serialVersionUID = 1L
    }

}
