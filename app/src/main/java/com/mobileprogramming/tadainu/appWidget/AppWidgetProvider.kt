package com.mobileprogramming.tadainu.appWidget

//https://velog.io/@soyoung-dev/AndroidKotlin-%EC%95%B1-%EC%9C%84%EC%A0%AF%EC%9D%84-%EB%A7%8C%EB%93%A4%EC%96%B4%EB%B3%B4%EC%9E%90
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.RemoteViews
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.mobileprogramming.tadainu.MainActivity
import com.mobileprogramming.tadainu.R
import com.mobileprogramming.tadainu.databinding.FragmentMyPetBinding
import com.mobileprogramming.tadainu.databinding.WidgetLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.log

class MyWidgetProvider : AppWidgetProvider() {

    // 앱 위젯은 여러개가 등록 될 수 있는데, 최초의 앱 위젯이 등록 될 때 호출 됩니다. (각 앱 위젯 인스턴스가 등록 될때마다 호출 되는 것이 아님)
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    // onEnabled() 와는 반대로 마지막의 최종 앱 위젯 인스턴스가 삭제 될 때 호출 됩니다
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    // android 4.1 에 추가 된 메소드 이며, 앱 위젯이 등록 될 때와 앱 위젯의 크기가 변경 될 때 호출 됩니다.
    // 이때, Bundle 에 위젯 너비/높이의 상한값/하한값 정보를 넘겨주며 이를 통해 컨텐츠를 표시하거나 숨기는 등의 동작을 구현 합니다
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        val sharedPreferences = context.getSharedPreferences("MyWidgetPreferences", Context.MODE_PRIVATE)

        val beautyLeftValue = sharedPreferences.getString("widget_mypet_beauty_left", "")
        val healthLeftValue = sharedPreferences.getString("widget_mypet_health_left", "")
        val beautyDateValue = sharedPreferences.getString("widget_mypet_beauty_date", "")
        val healthDateValue = sharedPreferences.getString("widget_mypet_health_date", "")
        Log.d("ITMwidget", "beautyLeftValue: $beautyLeftValue")
        Log.d("ITMwidget", "healthLeftValue: $healthLeftValue")
        Log.d("ITMwidget", "beautyDateValue: $beautyDateValue")
        Log.d("ITMwidget", "healthDateValue: $healthDateValue")

        val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
            .let { intent ->
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }

        val views: RemoteViews =
            RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setOnClickPendingIntent(R.id.widget_mypet_beauty_background, pendingIntent)
                setOnClickPendingIntent(R.id.widget_mypet_health_background, pendingIntent)

                setTextViewText(R.id.widget_mypet_beauty_left, beautyLeftValue)
                setTextViewText(R.id.widget_mypet_health_left, healthLeftValue)
                setTextViewText(R.id.widget_mypet_beauty_date, beautyDateValue)
                setTextViewText(R.id.widget_mypet_health_date, healthDateValue)
            }

        appWidgetManager.updateAppWidget(appWidgetId, views)

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    // 위젯 메타 데이터를 구성 할 때 updatePeriodMillis 라는 업데이트 주기 값을 설정하게 되며, 이 주기에 따라 호출 됩니다.
    // 또한 앱 위젯이 추가 될 떄에도 호출 되므로 Service 와의 상호작용 등의 초기 설정이 필요 할 경우에도 이 메소드를 통해 구현합니다
    @SuppressLint("RemoteViewLayout")
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 위젯 업데이트 로직을 구현
        appWidgetIds.forEach { appWidgetId ->
            // 액티비티를 실행할 인텐트 정의

            val sharedPreferences = context.getSharedPreferences("MyWidgetPreferences", Context.MODE_PRIVATE)

            val beautyLeftValue = sharedPreferences.getString("widget_mypet_beauty_left", "")
            val healthLeftValue = sharedPreferences.getString("widget_mypet_health_left", "")
            val beautyDateValue = sharedPreferences.getString("widget_mypet_beauty_date", "")
            val healthDateValue = sharedPreferences.getString("widget_mypet_health_date", "")
            Log.d("ITMwidget", "beautyLeftValue: $beautyLeftValue")

            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }

            // 앱 위젯의 레이아웃 가져옴. 레이아웃에 클릭 리스너 연결
            val views: RemoteViews =
                RemoteViews(context.packageName, R.layout.widget_layout).apply {
                    setOnClickPendingIntent(R.id.widget_mypet_beauty_background, pendingIntent)
                    setOnClickPendingIntent(R.id.widget_mypet_health_background, pendingIntent)

                    setTextViewText(R.id.widget_mypet_beauty_left, beautyLeftValue)
                    setTextViewText(R.id.widget_mypet_health_left, healthLeftValue)
                    setTextViewText(R.id.widget_mypet_beauty_date, beautyDateValue)
                    setTextViewText(R.id.widget_mypet_health_date, healthDateValue)
                }

            // 앱 위젯에서 업데이트 수행하도록 AppWidgetManager 에 알림
            appWidgetManager.updateAppWidget(appWidgetId, views)

//            super.onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

        // 이 메소드는 앱 데이터가 구글 시스템에 백업 된 이후 복원 될 때 만약 위젯 데이터가 있다면 데이터가 복구 된 이후 호출 됩니다.
        // 일반적으로 사용 될 경우는 흔치 않습니다.
        // 위젯 ID 는 UID 별로 관리 되는데 이때 복원 시점에서 ID 가 변경 될 수 있으므로 백업 시점의 oldID 와 복원 후의 newID 를 전달합니다
        override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {



            super.onRestored(context, oldWidgetIds, newWidgetIds)



        }

    // 해당 앱 위젯이 삭제 될 때 호출 됩니다
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
    }

    // 앱의 브로드캐스트를 수신하며 해당 메서드를 통해 각 브로드캐스트에 맞게 메서드를 호출한다.
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

}