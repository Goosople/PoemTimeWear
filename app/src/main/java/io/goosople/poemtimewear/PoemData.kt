package io.goosople.poemtimewear


import com.google.gson.annotations.SerializedName

data class PoemData(
    @SerializedName("poems")
    val poems: List<Poem> = listOf()
) {
    data class Poem(
        @SerializedName("poem")
        val poem: String = "", // 君子曰：学不可以已。
        @SerializedName("poet")
        val poet: String = "", // 荀子 〔先秦〕
        @SerializedName("title")
        val title: String = "" // 劝学(节选)
    )
    fun getPoemContent(num:Int):String{
        return poems.getOrNull(num)?.poem ?: "num out of range"
    }
    fun getPoemDetail(num:Int):String{
        val poet= poems.getOrNull(num)?.poet ?: "num out of range"
        val title= poems.getOrNull(num)?.title ?: "num out of range"
        return "《$title》$poet"
    }
}