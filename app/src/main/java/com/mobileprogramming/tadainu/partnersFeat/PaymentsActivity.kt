package com.mobileprogramming.tadainu.partnersFeat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mobileprogramming.tadainu.databinding.ActivityPaymentsBinding
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.model.PaymentCallback
import com.tosspayments.paymentsdk.model.PaymentWidgetStatusListener
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.view.PaymentMethod

class PaymentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.toolbarTitle.text = "결제하기"

        val paymentWidget = PaymentWidget(
            activity = this@PaymentsActivity,
            clientKey = "test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq",
            customerKey = "7CP_K-knksQZ966GZAfhm",
        )

        val paymentMethodWidgetStatusListener = object : PaymentWidgetStatusListener{
            override fun onLoad() {
                val message = "결제위젯 렌더링 완료"
                Log.d("PaymentWidgetStatusListener", message)
            }
        }

        paymentWidget.run {
            renderPaymentMethods(
                method = binding.paymentMethodWidget,
                amount = PaymentMethod.Rendering.Amount(10000),
                paymentWidgetStatusListener = paymentMethodWidgetStatusListener
            )

            renderAgreement(binding.agreementWidget)
        }

        binding.requestPaymentCta.setOnClickListener {
            paymentWidget.requestPayment(
                paymentInfo = PaymentMethod.PaymentInfo(orderId = "wBWO9RJXO0UYqJMV4er8J", orderName = "orderName"),
                paymentCallback = object : PaymentCallback {
                    override fun onPaymentSuccess(success: TossPaymentResult.Success) {
                        Log.i("success", success.paymentKey)
                        Log.i("success", success.orderId)
                        Log.i("success", success.amount.toString())
                    }

                    override fun onPaymentFailed(fail: TossPaymentResult.Fail) {
                        Log.e("fail",fail.errorMessage)
                    }
                }
            )
        }

    }
}