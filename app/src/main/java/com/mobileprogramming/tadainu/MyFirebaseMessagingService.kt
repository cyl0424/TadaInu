package com.mobileprogramming.tadainu

import com.mobileprogramming.tadainu.GlobalApplication.Companion.prefs
import com.mobileprogramming.tadainu.partnersFeat.ChatRoomActivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService: FirebaseMessagingService()
{
    val firestore = FirebaseFirestore.getInstance()
    private lateinit var uid : String
    private lateinit var auth: FirebaseAuth

    private val TAG: String = this.javaClass.simpleName


    // 메세지가 수신되면 호출
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // 서버에서 직접 보냈을 때
        if(remoteMessage.notification != null){
            sendNotification(remoteMessage.notification?.title,
                remoteMessage.notification?.body!!)
        }

        // 다른 기기에서 서버로 보냈을 때
        else if(remoteMessage.data.isNotEmpty()){
            val title = remoteMessage.data["title"]!!
            val userId = remoteMessage.data["userId"]!!
            val message = remoteMessage.data["message"]!!
            val postId = remoteMessage.data["postId"]!!
            val destinationUid = remoteMessage.data["destinationUid"]!!
            val type = remoteMessage.data["type"]!!

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                sendMessageNotification(title,userId,message, postId, destinationUid, type)
            }
            else{
                sendNotification(remoteMessage.notification?.title,
                    remoteMessage.notification?.body!!)
            }
        }
    }


    // 토큰이 바뀌었을 때 업데이트 사항
    override fun onNewToken(token: String)
    {
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        if(uid!=null)
            sendRegistrationToServer(token)
        Log.d(TAG, "Refreshed token : $token")
        super.onNewToken(token)
    }
    // Firebase Cloud Messaging Server 가 대기중인 메세지를 삭제 시 호출
    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    // 메세지가 서버로 전송 성공 했을때 호출
    override fun onMessageSent(p0: String) {
        super.onMessageSent(p0)
    }

    // 메세지가 서버로 전송 실패 했을때 호출
    override fun onSendError(p0: String, p1: Exception) {
        super.onSendError(p0, p1)
    }



    // 서버에서 직접 보냈을 때
    private fun sendNotification(title: String?, body: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 액티비티 중복 생성 방지
        val pendingIntent = PendingIntent.getActivity(this, 0 , intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE) // 일회성

        val channelId = R.string.default_notification_channel_id.toString() // 채널 아이디
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 소리
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title) // 제목
            .setContentText(body) // 내용
            .setSmallIcon(R.mipmap.ic_launcher_round) // 아이콘
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("versioncheck", "oreo version over1")
        // 오레오 버전 예외처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Finder",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
            Log.d("versioncheck", "oreo version over2")
        }

        notificationManager.notify(0 , notificationBuilder.build()) // 알림 생성
    }



    // 다른 기기에서 서버로 보냈을 때
    @RequiresApi(Build.VERSION_CODES.P)
    private fun sendMessageNotification(title: String, userId: String, body: String, postId: String, destinationUid:String, type:String){
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra("destinationUid", destinationUid )
        intent.putExtra("postId", postId)
        intent.putExtra("type", type)
        prefs.setString("alarm", "true")


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 액티비티 중복 생성 방지
        Log.d("lastcheck", "destinationUid: "+destinationUid)
        Log.d("lastcheck", "postId: " + postId)

        Log.d("checkmessage", title + userId)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE ) // 일회성
        }else {
            PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_UPDATE_CURRENT ) // 일회성
        }


        // messageStyle 로
        val user: Person = Person.Builder()
            .setName(userId)
            .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher_round))
            .build()

        val message = NotificationCompat.MessagingStyle.Message(
            body,
            System.currentTimeMillis(),
            user
        )
        val messageStyle = NotificationCompat.MessagingStyle(user)
            .addMessage(message)


        val channelId = "channel" // 채널 아이디
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 소리
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title) // 제목
            .setContentText(body) // 내용
            .setStyle(messageStyle)
            .setSmallIcon(R.mipmap.ic_launcher_round) // 아이콘
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("versioncheck", "oreo version over3")
        // 오레오 버전 예외처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Finder",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            Log.d("versioncheck", "oreo version over4")
        }

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        val appCollection = firestore.collection("users")
        uid?.let { appCollection.document(it) }?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                val blockusers = (document["block"] as ArrayList<String>?)!!

                Log.d("noticheck", ChatRoomActivity.In_Room.toString() + ChatRoomActivity.In_Room_postId)

                if (!blockusers.contains(destinationUid) && !(ChatRoomActivity.In_Room == true
                            && postId == ChatRoomActivity.In_Room_postId) && prefs.Notification == true){
                    // 차단한 유저가 보낸 메시지가 아니며, 알림이 온 채팅방 안에 있을 때
                    notificationManager.notify(0 , notificationBuilder.build()) // 알림 생성
                    Log.d("noticheck!!!", "unblock")
                }
                else {
                    Log.d("noticheck!!!", "block")

                }
            }

        }


    }

    // 받은 토큰을 서버로 전송
    private fun sendRegistrationToServer(token: String){

        firestore.collection("users").document(uid).update("fcmToken", token)
    }
}