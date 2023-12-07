package com.mobileprogramming.tadainu.accountFeat

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.MainActivity
import com.mobileprogramming.tadainu.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private var mBinding: ActivitySignInBinding? = null
    private val binding get() = mBinding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivitySignInBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.kakaoLoginBtn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 버튼이 눌렸을 때
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(200).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 버튼이 눌리지 않았을 때
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }
            }
            false
        }

        binding.kakaoLoginBtn.setOnClickListener {
            Log.d("로그인", "클릭")
            // 카카오 로그인
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e("로그인", "카카오 로그인 실패", error)
                    Toast.makeText(this, "잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                } else if (token != null) {
                    Log.i("로그인", "카카오 로그인 성공 ${token.accessToken}")

                    // 카카오 로그인 성공 후
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.e("로그인", "사용자 정보 요청 실패", error)
                        } else if (user != null) {
                            val email = user.kakaoAccount?.email
                            Log.d("로그인", email.toString())
                            // 파이어베이스 익명 인증
                            auth.signInAnonymously()
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // 익명 인증 성공, 사용자 정보 Firestore에 확인
                                        db.collection("TB_USER")
                                            .whereEqualTo("user_email", email)
                                            .limit(1)  // 가장 처음 일치하는 문서만 가져옵니다.
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                if (documents.isEmpty) {
                                                    // 일치하는 이메일이 없으므로 MoreInfoActivity로 이동
                                                    val intent = Intent(this, MoreInfoActivity::class.java)
                                                    intent.putExtra("user_email", email)
                                                    startActivity(intent)
                                                    finish()
                                                } else {
                                                    // 일치하는 이메일이 있으므로 MainActivity로 이동
                                                    prefs.setString("currentUser", documents.first()["user_id"].toString())
                                                    val intent = Intent(this, MainActivity::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("로그인", "Error getting documents: ", e)
                                            }
                                    } else {
                                        // 익명 인증 실패
                                        Log.w("로그인", "signInAnonymously:failure", task.exception)
                                    }
                                }
                        }
                    }

                }
            }
        }

        binding.closeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }
    }
}