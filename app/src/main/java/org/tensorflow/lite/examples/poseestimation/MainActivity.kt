/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package org.tensorflow.lite.examples.poseestimation

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.Notification
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Debug
import android.os.Process
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.poseestimation.camera.CameraSource
import org.tensorflow.lite.examples.poseestimation.data.Device
import org.tensorflow.lite.examples.poseestimation.ml.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {
    companion object {
        private const val FRAGMENT_DIALOG = "dialog"
    }

    /** A [SurfaceView] for camera preview.   */
    private lateinit var surfaceView: SurfaceView

    /** Default pose estimation model is 1 (MoveNet Thunder)
     * 0 == MoveNet Lightning model
     * 1 == MoveNet Thunder model
     * 2 == MoveNet MultiPose model
     * 3 == PoseNet model
     **/
    private var modelPos = 1

    /** Default device is CPU */
    private var device = Device.CPU

    private lateinit var button: FloatingActionButton
    private lateinit var pose: String;
    private var start: Boolean = false;
    private var timer: Int = 30 * 1000;
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var imageView: ImageView

    private lateinit var timerText: TextView

    //    private lateinit var tvScore: TextView
//    private lateinit var tvFPS: TextView
//    private lateinit var spnDevice: Spinner
//    private lateinit var spnModel: Spinner
//    private lateinit var spnTracker: Spinner
//    private lateinit var vTrackerOption: View
    private lateinit var tvClassificationValue1: TextView
    private lateinit var tvClassificationValue2: TextView
    private lateinit var tvClassificationValue3: TextView
    private lateinit var swClassification: SwitchCompat
    private lateinit var vClassificationOption: View
    private var cameraSource: CameraSource? = null
    private var isClassifyPose = false
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                openCamera()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                ErrorDialog.newInstance(getString(R.string.tfe_pe_request_permission))
                    .show(supportFragmentManager, FRAGMENT_DIALOG)
            }
        }
    private var changeModelListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            changeModel(position)
        }
    }

    private var changeDeviceListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            changeDevice(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }
    }

    private var changeTrackerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            changeTracker(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }
    }

    private var setClassificationListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            showClassificationResult(isChecked)
            isClassifyPose = isChecked
            isPoseClassifier()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // keep screen on while app is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        imageView = findViewById(R.id.imageView)
        timerText = findViewById(R.id.timer)

//        tvScore = findViewById(R.id.tvScore)
//        tvFPS = findViewById(R.id.tvFps)
//        spnModel = findViewById(R.id.spnModel)
//        spnDevice = findViewById(R.id.spnDevice)
//        spnTracker = findViewById(R.id.spnTracker)
//        vTrackerOption = findViewById(R.id.vTrackerOption)
        surfaceView = findViewById(R.id.surfaceView)
        tvClassificationValue1 = findViewById(R.id.tvClassificationValue1)
        tvClassificationValue2 = findViewById(R.id.tvClassificationValue2)
        tvClassificationValue3 = findViewById(R.id.tvClassificationValue3)
        swClassification = findViewById(R.id.swPoseClassification)
//        vClassificationOption = findViewById(R.id.vClassificationOption)
//        initSpinner()
//        spnModel.setSelection(modelPos)
        swClassification.setOnCheckedChangeListener(setClassificationListener)
        if (!isCameraPermissionGranted()) {
            requestPermission()
        }

        pose = intent.getStringExtra("pose")!!;

        if(pose == "up"){
            imageView.setImageResource(R.drawable.action1)
        }else if(pose == "down"){
            imageView.setImageResource(R.drawable.action2)
        }else if(pose == "left"){
            imageView.setImageResource(R.drawable.action3)
        }else if(pose == "right"){
            imageView.setImageResource(R.drawable.action4)
        }

        AlertDialog.Builder(this).setTitle("要開始進行?")
            .setMessage(getPoseName(pose))
            .setPositiveButton("開始") { dialog, which ->
                run {
                    dialog.cancel();
                    var count: Int = 4
                    mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.count)
                    mediaPlayer.start()
                    timer(period = 1500) {
                        if (count == 1) {
                            timerText.setText("開始")
                            mediaPlayer.stop()
                        } else if (count == 0) {
                            start = true
                            mediaPlayer = MediaPlayer.create(this@MainActivity,R.raw.sound)
                            mediaPlayer.start()
                            startTimer()
                            cancel()
                        } else {
                            timerText.setText((count - 1).toString())
                        }
                        count--
                    }
                }
            }
            .setNegativeButton("離開") { dialog, which -> finish() }
            .setCancelable(false)
            .show();
    }

    override fun onStart() {
        super.onStart()
        openCamera()
    }

    override fun onResume() {
        cameraSource?.resume()
        super.onResume()
    }

    override fun onPause() {
        cameraSource?.close()
        cameraSource = null
        mediaPlayer.release()
        super.onPause()
    }

    private fun getPoseName(pose: String): String {
        when(pose){
            "up" -> return "後仰伸展運動"
            "down" -> return "後頸肌肉伸展運動"
            "left" -> return "左上斜方肌伸展運動"
            "right" -> return "右上斜方肌伸展運動"
        }
        return "未知"
    }

    private fun startTimer() {
        timer(period = 200) {
            timerText.setText((timer / 1000).toString() + "'")
            println(timer)
            if (timer == 0) {
                mediaPlayer.stop()
                mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.success)
                mediaPlayer.start()
                timerText.setText("結束")
                this@MainActivity.runOnUiThread(java.lang.Runnable {
                    done()
                })
                cancel()
            } else if (start) {
                timer -= 200
                mediaPlayer.start()
            } else {
                mediaPlayer.pause()
            }
        }
    }

    private fun done() {
        AlertDialog.Builder(this@MainActivity).setTitle("結束")
            .setMessage("恭喜您成功達成目標！")
            .setPositiveButton("離開") { dialog, which ->
                run {
                    finish();
                }
            }
            .setCancelable(false)
            .show();
    }

    private fun openFabButton() {
        startActivity(Intent(this, MenuActivity::class.java))
    }

    // check if permission is granted or not.
    private fun isCameraPermissionGranted(): Boolean {
        return checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED
    }

    // open camera
    private fun openCamera() {
        if (isCameraPermissionGranted()) {
            if (cameraSource == null) {
                cameraSource =
                    CameraSource(surfaceView, object : CameraSource.CameraSourceListener {
                        override fun onFPSListener(fps: Int) {
//                            tvFPS.text = getString(R.string.tfe_pe_tv_fps, fps)
                        }

                        override fun onDetectedInfo(
                            personScore: Float?,
                            poseLabels: List<Pair<String, Float>>?
                        ) {
                            poseLabels?.sortedByDescending { it.second }?.let {
                                //TODO
                                if (it[0].first == pose) {
                                    start = true
                                    timerText.setBackgroundColor(Color.parseColor("#FFC8E6C9"))
                                } else {
                                    start = false
                                    timerText.setBackgroundColor(Color.parseColor("#FFFFCDD2"))
                                }
                                tvClassificationValue1.text = getString(
                                    R.string.tfe_pe_tv_classification_value,
                                    convertPoseLabels(if (it.isNotEmpty()) it[0] else null)
                                )
                                tvClassificationValue2.text = getString(
                                    R.string.tfe_pe_tv_classification_value,
                                    convertPoseLabels(if (it.size >= 2) it[1] else null)
                                )
                                tvClassificationValue3.text = getString(
                                    R.string.tfe_pe_tv_classification_value,
                                    convertPoseLabels(if (it.size >= 3) it[2] else null)
                                )
//                            tvScore.text = getString(R.string.tfe_pe_tv_score, personScore ?: 0f)
//                            poseLabels?.sortedByDescending { it.second }?.let {
//                                if(start){
//
//                                }
//                                tvClassificationValue1.text = getString(
//                                    R.string.tfe_pe_tv_classification_value,
//                                    convertPoseLabels(if (it.isNotEmpty()) it[0] else null)
//                                )
//                                tvClassificationValue2.text = getString(
//                                    R.string.tfe_pe_tv_classification_value,
//                                    convertPoseLabels(if (it.size >= 2) it[1] else null)
//                                )
//                                tvClassificationValue3.text = getString(
//                                    R.string.tfe_pe_tv_classification_value,
//                                    convertPoseLabels(if (it.size >= 3) it[2] else null)
//                                )
                            }
                        }

                    }).apply {
                        prepareCamera()
                    }
                isPoseClassifier()
                lifecycleScope.launch(Dispatchers.Main) {
                    cameraSource?.initCamera()
                }
            }
            createPoseEstimator()
        }
    }

    private fun convertPoseLabels(pair: Pair<String, Float>?): String {
        if (pair == null) return "empty"
        return "${pair.first} (${String.format("%.2f", pair.second)})"
    }

    private fun isPoseClassifier() {
        cameraSource?.setClassifier(if (isClassifyPose) PoseClassifier.create(this) else null)
    }

    // Initialize spinners to let user select model/accelerator/tracker.
//    private fun initSpinner() {
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.tfe_pe_models_array,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            // Specify the layout to use when the list of choices appears
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            // Apply the adapter to the spinner
//            spnModel.adapter = adapter
//            spnModel.onItemSelectedListener = changeModelListener
//        }
//
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.tfe_pe_device_name, android.R.layout.simple_spinner_item
//        ).also { adaper ->
//            adaper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//            spnDevice.adapter = adaper
//            spnDevice.onItemSelectedListener = changeDeviceListener
//        }
//
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.tfe_pe_tracker_array, android.R.layout.simple_spinner_item
//        ).also { adaper ->
//            adaper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//            spnTracker.adapter = adaper
//            spnTracker.onItemSelectedListener = changeTrackerListener
//        }
//    }

    // Change model when app is running
    private fun changeModel(position: Int) {
        if (modelPos == position) return
        modelPos = position
        createPoseEstimator()
    }

    // Change device (accelerator) type when app is running
    private fun changeDevice(position: Int) {
        val targetDevice = when (position) {
            0 -> Device.CPU
            1 -> Device.GPU
            else -> Device.NNAPI
        }
        if (device == targetDevice) return
        device = targetDevice
        createPoseEstimator()
    }

    // Change tracker for Movenet MultiPose model
    private fun changeTracker(position: Int) {
        cameraSource?.setTracker(
            when (position) {
                1 -> TrackerType.BOUNDING_BOX
                2 -> TrackerType.KEYPOINTS
                else -> TrackerType.OFF
            }
        )
    }

    private fun createPoseEstimator() {
        // For MoveNet MultiPose, hide score and disable pose classifier as the model returns
        // multiple Person instances.
        val poseDetector = when (modelPos) {
            0 -> {
                // MoveNet Lightning (SinglePose)
                showPoseClassifier(false)
//                showDetectionScore(true)
//                showTracker(false)
                MoveNet.create(this, device, ModelType.Lightning)
            }
            1 -> {
                // MoveNet Thunder (SinglePose)
                showPoseClassifier(true)
//                showDetectionScore(true)
//                showTracker(false)
                MoveNet.create(this, device, ModelType.Thunder)
            }
            2 -> {
                // MoveNet (Lightning) MultiPose
                showPoseClassifier(false)
//                showDetectionScore(false)
                // Movenet MultiPose Dynamic does not support GPUDelegate
                if (device == Device.GPU) {
                    showToast(getString(R.string.tfe_pe_gpu_error))
                }
//                showTracker(true)
                MoveNetMultiPose.create(
                    this,
                    device,
                    Type.Dynamic
                )
            }
            3 -> {
                // PoseNet (SinglePose)
                showPoseClassifier(false)
//                showDetectionScore(true)
//                showTracker(false)
                PoseNet.create(this, device)
            }
            else -> {
                null
            }
        }
        poseDetector?.let { detector ->
            cameraSource?.setDetector(detector)
        }
    }

    // Show/hide the pose classification option.
    private fun showPoseClassifier(isVisible: Boolean) {
        swClassification.isChecked = isVisible
//        vClassificationOption.visibility = if (isVisible) View.VISIBLE else View.GONE
//        if (!isVisible) {
//            swClassification.isChecked = false
//        }
    }

    // Show/hide the detection score.
//    private fun showDetectionScore(isVisible: Boolean) {
//        tvScore.visibility = if (isVisible) View.VISIBLE else View.GONE
//    }

    // Show/hide classification result.
    private fun showClassificationResult(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        tvClassificationValue1.visibility = visibility
        tvClassificationValue2.visibility = visibility
        tvClassificationValue3.visibility = visibility
    }

    // Show/hide the tracking options.
//    private fun showTracker(isVisible: Boolean) {
//        if (isVisible) {
//            // Show tracker options and enable Bounding Box tracker.
//            vTrackerOption.visibility = View.VISIBLE
//            spnTracker.setSelection(1)
//        } else {
//            // Set tracker type to off and hide tracker option.
//            vTrackerOption.visibility = View.GONE
//            spnTracker.setSelection(0)
//        }
//    }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                // You can use the API that requires the permission.
                openCamera()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // do nothing
                }
                .create()

        companion object {

            @JvmStatic
            private val ARG_MESSAGE = "message"

            @JvmStatic
            fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
                arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
            }
        }
    }
}
