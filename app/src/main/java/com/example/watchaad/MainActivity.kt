package com.example.watchaad

import android.widget.Button
import android.widget.TextView

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

// Remove the line below after defining your own ad unit ID.
private const val TOAST_TEXT = "Test ads are being shown. " +
        "To show live ads, replace the ad unit ID in res/values/strings.xml " +
        "with your own ad unit ID."
private const val START_LEVEL = 1

class MainActivity : AppCompatActivity() {

    private var currentLevel: Int = 0
    private var interstitialAd: InterstitialAd? = null
    private lateinit var nextLevelButton: Button
    private lateinit var levelTextView: TextView
    lateinit var mAdView : AdView
    private lateinit var rewardedAd: RewardedAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this, "ca-app-pub-4528420242999185~1706087891")

        // Create the next level button, which tries to show an interstitial when clicked.
        nextLevelButton = findViewById(R.id.next_level_button)
        mAdView = findViewById(R.id.adViewBanner)
        nextLevelButton.isEnabled = false
        nextLevelButton.setOnClickListener { showInterstitial() }

        levelTextView = findViewById(R.id.level)
        // Create the text view to show the level number.
        currentLevel = START_LEVEL

        // Create the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        interstitialAd = newInterstitialAd()
        loadInterstitial()

        rewardedAd = RewardedAd(this, "ca-app-pub-4528420242999185/2195180667")
        createAndLoadRewardedAd()

        findViewById<Button>(R.id.reward_button).setOnClickListener{
            rewardsCallback()
        }


        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
       // Toast.makeText(this, TOAST_TEXT, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.action_settings -> true
                else -> super.onOptionsItemSelected(item)
            }

    private fun newInterstitialAd(): InterstitialAd {
        return InterstitialAd(this).apply {
            adUnitId = getString(R.string.interstitial_ad_unit_id)  //todo live ca-app-pub-4528420242999185/7975148625   test ca-app-pub-3940256099942544/1033173712
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    nextLevelButton.isEnabled = true
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    nextLevelButton.isEnabled = true
                }

                override fun onAdClosed() {
                    // Proceed to the next level.
                    goToNextLevel()
                }
            }
        }
    }

    private fun showInterstitial() {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (interstitialAd?.isLoaded == true) {
            interstitialAd?.show()
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show()
            goToNextLevel()
        }
    }

    private fun loadInterstitial() {
        // Disable the next level button and load the ad.
        nextLevelButton.isEnabled = false
        val adRequestInterstitial = AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template")
                .build()
        interstitialAd?.loadAd(adRequestInterstitial)

        //banner ad.
        val adRequestBanner = AdRequest.Builder().build()
        mAdView.loadAd(adRequestBanner)
    }

    private fun goToNextLevel() {
        // Show the next level and reload the ad to prepare for the level after.
        levelTextView.text = "Level " + (++currentLevel)
        interstitialAd = newInterstitialAd()
        loadInterstitial()
    }

    private fun createAndLoadRewardedAd(): RewardedAd {
        val adLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad successfully loaded.
            }
            override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                // Ad failed to load.
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        return rewardedAd
    }

    private fun rewardsCallback() {
        if(rewardedAd.isLoaded) {
            val adLoadCallback = object : RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    // Ad opened.
                }

                override fun onRewardedAdClosed() {
                    Toast.makeText(this@MainActivity, "ad closed", Toast.LENGTH_LONG).show()
                    rewardedAd = createAndLoadRewardedAd()
                }

                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    Toast.makeText(this@MainActivity, ""+reward.amount, Toast.LENGTH_LONG).show()
                }

                override fun onRewardedAdFailedToShow(adError: AdError) {
                    Toast.makeText(this@MainActivity, ""+adError.message, Toast.LENGTH_LONG).show()
                }

            }
            rewardedAd.show(this, adLoadCallback)
        }else {
            Toast.makeText(this, "reward ad failed to load ", Toast.LENGTH_LONG).show()
        }
    }
}