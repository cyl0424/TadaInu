package com.mobileprogramming.tadainu.appWidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.RemoteViews
import com.mobileprogramming.tadainu.MainActivity
import com.mobileprogramming.tadainu.R

class MyWidgetProvider: AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 위젯 업데이트 로직을 구현
        appWidgetIds.forEach { appWidgetId ->
            // 액티비티를 실행할 인텐트 정의
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let {intent ->
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }

            // 앱 위젯의 레이아웃 가져옴. 버튼에 클릭 리스너 연결
            val views: RemoteViews = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setOnClickPendingIntent(R.id.mvbutton, pendingIntent)
            }

            // 앱 위젯에서 업데이트 수행하도록 AppWidgetManager 에 알림
            appWidgetManager.updateAppWidget(appWidgetId,views)

        }
    }
}