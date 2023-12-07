package com.mobileprogramming.tadainu.notiFeat

import android.app.Dialog
import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.mobileprogramming.tadainu.notiFeat.UploadFeedActivity.Companion.sharedImageList
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.DialogCameraGalleryBinding
import com.mobileprogramming.tadainu.databinding.FragmentNotiBinding
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotiFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentNotiBinding
    lateinit var dialogBinding: DialogCameraGalleryBinding
    private var selectedImageUris: ArrayList<Uri> = ArrayList()
    private var croppedImageUris: ArrayList<Uri> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Noti로 나오면 sharedImageList 초기화 시킴
        sharedImageList.clear()
        binding = FragmentNotiBinding.inflate(inflater, container, false)
        dialogBinding = DialogCameraGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCreateFeed.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_camera_gallery)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Assuming your dialog layout contains galleryButton and cameraButton
            val galleryButton = dialog.findViewById<Button>(R.id.galleryButton)
            galleryButton.setOnClickListener {
                openGallery()
                dialog.dismiss()
            }

            val cameraButton = dialog.findViewById<Button>(R.id.cameraButton)
            cameraButton.setOnClickListener {
                val intent = Intent(requireContext(), CameraPreviewActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    // 갤러리 열기
    private fun openGallery() {
        // 갤러리를 여는 Intent
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        // 여러개 선택 가능 하도록
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        // 결과를 처리하기 위해 resultLauncher를 사용하여 갤러리 액티비티 시작
        resultLauncher.launch(galleryIntent)
    }

    private val resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // RESULT_OK는 -1임
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data

                // 이미지 여러 개일 때 갤러리에서 선택한 이미지들을 처리
                if (data?.clipData != null) {
                    val clipData = data.clipData

                    // 선택한 이미지들을 담을 리스트 초기화
                    selectedImageUris = ArrayList()

                    // 모든 선택된 이미지에 대해 반복
                    for (i in 0 until clipData!!.itemCount) {
                        val selectedImageUri = clipData!!.getItemAt(i).uri

                        // 리스트에 선택한 이미지 추가
                        selectedImageUris.add(selectedImageUri)
                    }

                    // 선택된 모든 이미지들을 처리하는 함수 호출
                    processSelectedImages(selectedImageUris)
                } else if (data?.data != null) {
                    // 단일 이미지 일때
                    // 선택한 이미지의 URI를 가져옴
                    val selectedImageUri = data.data

                    // 리스트를 초기화하고 선택한 이미지를 추가
                    if (selectedImageUri != null) {
                        selectedImageUris = arrayListOf(selectedImageUri)
                        processSelectedImages(selectedImageUris)
                    }
                }
            }
        }
    private var processedImageCount = 0
    private fun processSelectedImages(selectedImageUris: ArrayList<Uri>) {
        // Reset processedImageCount and croppedImageUris
        processedImageCount = 0
        croppedImageUris.clear()

        // Start processing the images
        startCropActivity(selectedImageUris, processedImageCount)
    }

    private fun startCropActivity(sourceUris: ArrayList<Uri>, index: Int) {
        if (index < sourceUris.size) {
            val sourceUri = sourceUris[index]
            val destinationUri =
                Uri.fromFile(File(requireContext().cacheDir, "cropped_image_${UUID.randomUUID()}.jpg"))
            val uCrop = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)

            val options = UCrop.Options()
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
            options.setCompressionQuality(80)
            uCrop.withOptions(options)

            val uCropIntent = uCrop.getIntent(requireContext())
            cropActivityResultLauncher.launch(uCropIntent)
        } else {
            // All images have been processed, start the UploadFeedActivity
            val intent = Intent(requireContext(), UploadFeedActivity::class.java)
            requireActivity().overridePendingTransition(0, 0)
            startActivity(intent)
        }
    }

    private val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val resultUri: Uri? = UCrop.getOutput(data)
                    if (resultUri != null) {
                        // CROP한 사진 ArrayList에 추가
                        sharedImageList.add(resultUri)
                    }
                    processedImageCount++

                    startCropActivity(selectedImageUris, processedImageCount)
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val error: Throwable? = UCrop.getError(result.data!!)
                Log.e(TAG, "Error cropping image: ${error?.message}", error)
            }
        }

    companion object {
        const val TAG = "NotiFragment"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}