package com.example.testaudio

import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testaudio.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import android.Manifest
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import android.os.Handler


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()
    private val updateSeekBar = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val currentPosition = it.currentPosition
                binding.seekBar.progress = currentPosition
                handler.postDelayed(this, 1000)
            }
        }
    }

    companion object {
        const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.record.setOnClickListener {
            startRecording()
        }
        binding.stoprecord.setOnClickListener {
            stopRecording()
        }
        binding.play.setOnClickListener {
            playRecording()
        }
    }

    fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
            Toast.makeText(this@MainActivity,"quyen",Toast.LENGTH_LONG).show()
        } else {
            try {
                mediaRecorder = MediaRecorder()
                audioFile = File(this.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio_file.3gp")

                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(audioFile?.absolutePath)
                    prepare()
                    start()
                    Toast.makeText(this@MainActivity,"success",Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                // Handle exception
            }
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }

            // Di chuyển hoặc sao chép file âm thanh vào thư mục trong bộ nhớ
            audioFile?.let { sourceFile ->
                val storageDirectory = File(this.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "MyAudioFiles")

                if (!storageDirectory.exists()) {
                    storageDirectory.mkdirs()
                }

                val destinationFile = File(storageDirectory, "audio_file1.3gp")

                if (sourceFile.exists()) {
                    sourceFile.renameTo(destinationFile)
                }
                Toast.makeText(this@MainActivity, "Success recording", Toast.LENGTH_LONG).show()
                val actualPath = File(storageDirectory, "audio_file1.3gp").absolutePath
                Toast.makeText(this@MainActivity, "File saved at: $actualPath", Toast.LENGTH_LONG).show()
                Log.d("ttt", actualPath)
            }
        } catch (e: Exception) {
            // Xử lý ngoại lệ
            Toast.makeText(this@MainActivity, "Error during recording: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun playRecording() {
        try {
            mediaPlayer = MediaPlayer()
            val storageDirectory = File(this.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "MyAudioFiles")
            val audioFile = File(storageDirectory, "audio_file1.3gp")

            mediaPlayer?.apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                start()

                // Set max progress for SeekBar
                binding.seekBar.max = duration

                // Start updating SeekBar
                handler.postDelayed(updateSeekBar, 1000)
            }
        } catch (e: IOException) {
            // Handle exception when playback fails
            Toast.makeText(this@MainActivity, "Error during playback: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun stopPlayback() {
        mediaPlayer?.let {
            it.stop()
            it.release()
            handler.removeCallbacks(updateSeekBar)
        }
    }

    // Xử lý kết quả yêu cầu quyền
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording()
                }
            }
        }
    }
}