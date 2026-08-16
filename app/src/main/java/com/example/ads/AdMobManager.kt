package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMob Configuration & Ad Manager.
 *
 * NOTE FOR PRODUCTION:
 * Replace the sample test Ad Unit IDs below with your actual Ad Unit IDs
 * obtained from your Google AdMob Console (https://admob.google.com).
 */
object AdMobConfig {
    // AdMob Application ID (from AndroidManifest.xml):
    const val APP_ID = "ca-app-pub-2767673700095238~6095372696"

    // Production Banner Ad Unit ID:
    const val BANNER_AD_UNIT_ID = "ca-app-pub-2767673700095238/2067809417"

    // Rewarded Ad Unit ID (Sample/Test ID until you add your production ID):
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Replace this with your production Interstitial Ad Unit ID:
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2767673700095238/3182268893"

    // Replace this with your production App Open Ad Unit ID:
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
}

object AdMobManager {
    private const val TAG = "AdMobManager"
    private var isInitialized = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var hasShownInitialAppOpen = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // Set global test device request configuration for reliable preview & test runs
            val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context) { initializationStatus ->
                isInitialized = true
                Log.d(TAG, "AdMob SDK Initialized: $initializationStatus")
                // Preload rewarded ad safely on main thread
                Handler(Looper.getMainLooper()).post {
                    loadRewardedAd(context.applicationContext)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob init exception", e)
        }
    }

    // ==========================================
    // REWARDED ADS (Hints, Undo, Extra Coins)
    // ==========================================
    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AdMobConfig.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${adError.message}")
                    rewardedAd = null
                    isRewardedLoading = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    isRewardedLoading = false
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (rewardAmount: Int, rewardType: String) -> Unit,
        onAdClosedOrSkipped: () -> Unit
    ) {
        val currentAd = rewardedAd
        if (currentAd != null && !activity.isFinishing && !activity.isDestroyed) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    rewardedAd = null
                    loadRewardedAd(activity.applicationContext)
                    onAdClosedOrSkipped()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                    loadRewardedAd(activity.applicationContext)
                    onAdClosedOrSkipped()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad shown")
                }
            }

            currentAd.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onUserEarnedReward(rewardItem.amount, rewardItem.type)
            }
        } else {
            Log.d(TAG, "Rewarded ad not ready or activity finishing. Granting reward fallback.")
            loadRewardedAd(activity.applicationContext)
            onUserEarnedReward(1, "FallbackReward")
            onAdClosedOrSkipped()
        }
    }

    // ==========================================
    // INTERSTITIAL ADS (Restart, Back to Home)
    // ==========================================
    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdMobConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed: ${loadAdError.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
        val currentAd = interstitialAd
        if (currentAd != null && !activity.isFinishing && !activity.isDestroyed) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onAdClosed()
                }
            }
            currentAd.show(activity)
        } else {
            loadInterstitialAd(activity.applicationContext)
            onAdClosed()
        }
    }

    // ==========================================
    // APP OPEN AD (On First Launch / Resume)
    // ==========================================
    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || isAppOpenLoading) return
        isAppOpenLoading = true

        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AdMobConfig.APP_OPEN_AD_UNIT_ID,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App Open ad loaded")
                    appOpenAd = ad
                    isAppOpenLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "App Open ad failed: ${loadAdError.message}")
                    appOpenAd = null
                    isAppOpenLoading = false
                }
            }
        )
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onFinished: () -> Unit = {}) {
        if (hasShownInitialAppOpen) {
            onFinished()
            return
        }
        val currentAd = appOpenAd
        if (currentAd != null && !activity.isFinishing && !activity.isDestroyed) {
            hasShownInitialAppOpen = true
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    loadAppOpenAd(activity.applicationContext)
                    onFinished()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    loadAppOpenAd(activity.applicationContext)
                    onFinished()
                }
            }
            currentAd.show(activity)
        } else {
            onFinished()
        }
    }
}

/**
 * Jetpack Compose Banner Ad View with safe lifecycle management.
 * Renders an adaptive banner ad above navigation bars or in dedicated ad spaces.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.BANNER_AD_UNIT_ID
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var adViewInstance by remember { mutableStateOf<AdView?>(null) }

    DisposableEffect(lifecycleOwner, adViewInstance) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adViewInstance?.pause()
                Lifecycle.Event.ON_RESUME -> adViewInstance?.resume()
                Lifecycle.Event.ON_DESTROY -> adViewInstance?.destroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adViewInstance?.destroy()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                Log.w("BannerAdView", "Banner ad failed to load: ${adError.message}")
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                        adViewInstance = this
                    }
                },
                update = { adView ->
                    adViewInstance = adView
                }
            )
        }
    }
}

