//package com.mobileprogramming.tadainu.homeFeat
//
//import android.content.Context
//import android.graphics.Bitmap
//import java.io.FileInputStream
//import java.nio.channels.FileChannel
//
//// DogClassifier.kt: TFLite 모델을 사용하여 강아지 종 분류
//class DogClassifier(private val context: Context, private val modelPath: String) {
//    private val interpreter: Interpreter
//
//    init {
//        val modelFileDescriptor = context.assets.openFd(modelPath)
//        val modelInputStream = FileInputStream(modelFileDescriptor.fileDescriptor)
//        val modelByteBuffer = modelInputStream.channel.map(
//            FileChannel.MapMode.READ_ONLY,
//            modelFileDescriptor.startOffset,
//            modelFileDescriptor.declaredLength
//        )
//        interpreter = Interpreter(modelByteBuffer)
//    }
//
//    fun classify(imageBitmap: Bitmap): String {
//        // 이미지를 모델에 전달하고 분류 결과를 얻음
//        // (이 부분은 실제 모델과 입력/출력 형식에 따라 수정이 필요함)
//        val inputTensor = // 모델 입력 텐서 설정
//        val outputTensor = // 모델 출력 텐서 설정
//            interpreter.run(inputTensor, outputTensor)
//
//        // 분류 결과를 반환
//        val result = // 결과 처리 로직 (분류된 강아지 종 등)
//            return result
//    }
//}
//
//// DogMatchingUtil.kt: 갤러리 사진과 Firebase 프로필 사진을 매칭하고 일치하는 것만 리스트에 담는 유틸리티 클래스
//object DogMatchingUtil {
//    fun matchAndFilterImages(
//        galleryImages: List<GalleryImage>,
//        firebaseProfileImages: List<FirebaseProfileImage>,
//        classifier: DogClassifier
//    ): List<MatchedDogImage> {
//        val matchedImages = mutableListOf<MatchedDogImage>()
//
//        for (galleryImage in galleryImages) {
//            // 갤러리 이미지를 분류하여 강아지 종 확인
//            val predictedBreed = classifier.classify(galleryImage.bitmap)
//
//            // Firebase 프로필 이미지 중 일치하는 종 찾기
//            val matchedProfileImage = firebaseProfileImages.find {
//                it.breed == predictedBreed
//            }
//
//            // 일치하는 프로필 이미지가 있으면 리스트에 추가
//            matchedProfileImage?.let {
//                matchedImages.add(MatchedDogImage(galleryImage.date, galleryImage.localPath))
//            }
//        }
//
//        return matchedImages
//    }
//}
//
//// MatchedDogImage.kt: 일치하는 강아지 이미지를 나타내는 데이터 클래스
//data class MatchedDogImage(
//    val date: String, // 날짜 정보
//    val localPath: String // 로컬 경로 정보
//)
//
//// 갤러리 이미지 및 Firebase 프로필 이미지에 대한 데이터 클래스 정의
//data class GalleryImage(
//    val date: String,
//    val localPath: String,
//    val bitmap: Bitmap // 이미지 비트맵 정보
//)
//
//data class FirebaseProfileImage(
//    val breed: String, // 강아지 종 정보
//    val imageUrl: String // Firebase에 업로드된 이미지 URL 정보
//)
