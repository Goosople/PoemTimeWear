package io.goosople.poemtimewear

import android.content.res.Resources
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

class PoemTimeUtils {
    companion object {
        const val poemTotalNumber = 2369

/*        private fun getKeyString(num: Int, jsonString: String, key: String): String {
            var jsonObj = JSONObject(jsonString)
            val array = jsonObj.getJSONArray("poems")
            jsonObj = array.getJSONObject(num)
            return jsonObj.getString(key)
        }

        private fun getPoemData(resources: Resources): String {
            val input = resources.openRawResource(R.raw.poemdata)
            val inputStreamReader = BufferedReader(InputStreamReader(input))
            return inputStreamReader.readText()
        }

        fun getPoemContent(num: Int, resources: Resources): String {
            return getKeyString(num, getPoemData(resources), "poem")
        }

        fun getPoemDetail(num: Int, resources: Resources): String {
            val poet = getKeyString(num, getPoemData(resources), "poet")
            val title = getKeyString(num, getPoemData(resources), "title")
            return "《$title》$poet"
        }*/

        fun getPoemData(resources: Resources): PoemData {
            val input = resources.openRawResource(R.raw.poemdata)
            val inputStreamReader = BufferedReader(InputStreamReader(input))
            return Gson().fromJson(inputStreamReader.readText(), PoemData::class.java)
        }
    }
}