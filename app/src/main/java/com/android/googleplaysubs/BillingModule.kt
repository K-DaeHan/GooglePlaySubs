package com.android.googleplaysubs

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillingModule(val activity: Activity) {

    private val purchasesUpdatedListener = object : PurchasesUpdatedListener {
        override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
            // 구매 작업 결과는 여기서 처리.
            if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
                // 사용자가 구매 흐름을 취소하여 발생한 오류를 처리합니다.
            } else {
                // 기타 오류 코드 처리.
            }
        } // onPurchasesUpdated
    }

    // BillingClient 초기화
    private val billingClient: BillingClient = BillingClient.newBuilder(activity)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        Log.d("test", "BillingModule init")
        // Google Play 연결
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    Log.d("test", "onBillingSetupFinished: Google Play 연결 성공.")
                    // BillingClient가 준비되었습니다. 여기에서 구매를 쿼리할 수 있습니다.
                    querySkuDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("test", "onBillingSetupFinished: Google Play 연결 실패.")
                // 다음에 대한 다음 요청에서 연결을 다시 시작하십시오.
                // startConnection() 메서드를 호출하여 Google Play를 실행합니다.
            }
        })
    } // init

    private fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("1_month_season")
        skuList.add("1_year_season")
        val params: SkuDetailsParams.Builder = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS) // 인앱(SkuType.INAPP) or 구독(SkuType.SUBS) 타입 설정

        /*val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }
        skuDetailsResult.skuDetailsList?.get(0)*/

        // Google Play에 인앱 상품 세부정보를 쿼리하려면 querySkuDetailsAsync()를 호출. 비동기로 처리.
        billingClient.querySkuDetailsAsync(params.build(), object : SkuDetailsResponseListener {
            // List에(skuDetailsList) 쿼리 결과를 저장합니다.
            override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: MutableList<SkuDetails>?) {
                // Process the result.
                val isItemChecked = skuDetailsList?.let {
                    it.size != 0
                } ?: false

                if (isItemChecked) {
                    Log.d("test", "skuDetailsList.size: ${skuDetailsList!!.size}")
                    skuDetailsList.forEach { skuDetail ->
                        Log.d("test", skuDetail.sku)
                    }
                    val skuDetails = skuDetailsList[0]

                    val flowParams: BillingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()

                    // launchBillingFlow() 호출에 성공하면 시스템에서 Google Play 구매 화면을 표시합니다
                    val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
                    when (responseCode) {
                        BillingResponseCode.OK -> { Log.d("test", "결제 아이템 보여주기 성공") }
                        else -> { Log.d("test", "결제 아이템 보여주기 실패") }
                    }
                } else {
                    // 인앱 상품 쿼리 실패.
                    return
                }
            } // onSkuDetailsResponse
        })
    } // querySkuDetails

    private fun handlePurchase(purchase: Purchase) {
        // BillingClient#queryPurchasesAsync 또는 PurchasesUpdatedListener에서 검색된 구매.
        // val purchase : Purchase = ...;
        Log.d("test", "handlePurchase")
        Log.d("test", "token: ${purchase.purchaseToken}")
        Log.d("test", "orderId: ${purchase.orderId}")
        Log.d("test", "skus: ${purchase.skus}")
        Log.d("test", "purchaseTime: ${purchase.purchaseTime}")
        // 구매를 확인합니다.
        // 이 구매 토큰에 대한 권한이 아직 부여되지 않았는지 확인합니다.
        // 사용자에게 권한을 부여합니다.

        val consumeParams: ConsumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

        val listener: ConsumeResponseListener = object : ConsumeResponseListener {
            override fun onConsumeResponse(billingResult: BillingResult, purchaseToken: String) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    // 소비 작업의 성공을 처리합니다.

                }
            } // onConsumeResponse
        }

        billingClient.consumeAsync(consumeParams, listener)
    } // handlePurchase

}
