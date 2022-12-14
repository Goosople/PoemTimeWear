package io.goosople.poemtimewear

import android.annotation.SuppressLint
import android.app.Activity
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import io.goosople.poemtimewear.PoemTimeUtils.Companion.getPoemData
import io.goosople.poemtimewear.PoemTimeUtils.Companion.poemTotalNumber
import io.goosople.poemtimewear.databinding.ActivityMainBinding
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding

    private var isPlaying = false
    private var poemNum = 0
    private var vClick = 0
    private var vClickTime = System.currentTimeMillis()

    // poem data
    private lateinit var poemData: PoemData

    private var tTaskHandler = Handler {
        val sP = PreferenceManager.getDefaultSharedPreferences(this)
        when (it.what) {
            0 -> {
                poemNum = sP.getInt("poemNum", 0)
                if (poemNum >= poemTotalNumber) {
                    isPlaying = false
                    sP.edit().putInt("poemNum", 0).commit()
                } else {
                    tts(poemData.getPoemContent(poemNum))
                }
                return@Handler true
            }
            1 -> {
                mTextToSpeech.stop()
                isPlaying = false
                return@Handler true
            }
            2 -> {
                poemNum++
                poemDetailNum(poemNum)
                return@Handler true
            }
            else -> return@Handler false
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        poemData = getPoemData(resources)

        // === TTS ===
        mTextToSpeech = TextToSpeech(this, this)
        // === Shared Preference ===
        val sP = PreferenceManager.getDefaultSharedPreferences(this)

        // === view initialize ===
        poemNum = sP.getInt("poemNum", 0)
        sP.edit().putInt("poemNum", poemNum).commit()
        poemTitle(poemNum)
        poemDetailNum(poemNum)

        // === edittext initialize ===
        binding.poemNum.doAfterTextChanged { text ->
            if (text.isNullOrBlank()) {
                return@doAfterTextChanged
            }
            val num = text.toString().toInt() - 1
            if (num in 0..poemTotalNumber) {
                poemNum = num
                poemTitle(poemNum)
                sP.edit().putInt("poemNum", poemNum).commit()
            } else {
                poemDetailNum(poemNum)
                Toast.makeText(this, "Number out of range", Toast.LENGTH_SHORT).show()
            }
        }

        // === button initialize ===
        binding.closeView.setOnClickListener {
            val deltaT = System.currentTimeMillis() - vClickTime
            if (deltaT < 1000) {
                Toast.makeText(this, "${3 - vClick} to exit", Toast.LENGTH_SHORT).show()
            } else if (deltaT >= 1000) vClick = 0
            if (vClick >= 3) finish()
            vClick++
            vClickTime = System.currentTimeMillis()
        }

        binding.play.setOnClickListener {
            isPlaying = !isPlaying
            onPlayPress(it as ImageButton, isPlaying)
        }

        binding.previous.setOnClickListener {
            if (poemNum > 0) {
                poemNum--
                poemTitle(poemNum)
                poemDetailNum(poemNum)
                sP.edit().putInt("poemNum", poemNum).commit()
            } else {
                Toast.makeText(this, "No previous poem", Toast.LENGTH_SHORT).show()
            }
        }

        binding.next.setOnClickListener {
            if (poemNum < poemTotalNumber) {
                poemNum++
                poemTitle(poemNum)
                poemDetailNum(poemNum)
                sP.edit().putInt("poemNum", poemNum).commit()
            } else {
                Toast.makeText(this, "No next poem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // === view event handlers ===

    private fun onPlayPress(btn: ImageButton, isPlay: Boolean) {
        isPlaying = isPlay
        btn.setImageResource(if (isPlay) R.drawable.ic_round_pause_circle_filled_24 else R.drawable.ic_round_play_circle_filled_24)
        if (isPlay) playPoem()
        else stopPoem()
    }

    // === poem services ===

    private fun stopPoem() {
        isPlaying = false
        tTaskHandler.sendEmptyMessage(1)
    }

    private fun playPoem() {
        Thread {
            while (isPlaying) {
                tTaskHandler.sendEmptyMessage(0)
                Thread.sleep(2000)
                while (mTextToSpeech.isSpeaking) Thread.sleep(1000)
                tTaskHandler.sendEmptyMessage(2)
            }
            tTaskHandler.sendEmptyMessage(1)
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun poemTitle(num: Int) {
        binding.poemTitle.text = poemData.getPoemDetail(num)
    }

    private fun poemDetailNum(num: Int) {
        val poemNum = num + 1
        binding.poemNum.setText(poemNum.toString())
    }

    // === text to speech ===
    private var isSupport = true
    private lateinit var mTextToSpeech: TextToSpeech
    private fun tts(text: String) {
        if (!isSupport) {
            Toast.makeText(
                this,
                "Your watch don't support TTS.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        //??????????????????????????????????????????????????????
        val myHashAlarm = hashMapOf<String, String>()
        myHashAlarm[TextToSpeech.Engine.KEY_PARAM_STREAM] = AudioManager.STREAM_MUSIC.toString()
        /* ????????????
         QUEUE_ADD?????????????????????????????????????????????????????????
         QUEUE_FLUSH????????????????????????????????????????????????????????? */
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, myHashAlarm)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = mTextToSpeech.setLanguage(Locale.SIMPLIFIED_CHINESE)
            // ???????????????????????????????????????????????????????????????????????????,1.0?????????
            mTextToSpeech.setPitch(1.0f) //(??????????????????,???????????????????????????????????????????????????)
            mTextToSpeech.setSpeechRate(1.0f)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                //???????????????????????????
                isSupport = false
            }
            Log.d("TTS", isSupport.toString())
        }
    }

    override fun onDestroy() {
        mTextToSpeech.shutdown()
        super.onDestroy()
    }
}