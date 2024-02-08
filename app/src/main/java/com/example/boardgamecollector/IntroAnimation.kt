package com.example.boardgamecollector

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class IntroAnimation: AppCompatActivity() {
    companion object {
        private const val DURATION: Long = 1600
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        val img: ImageView = findViewById(R.id.image_icon)
        val animator = ValueAnimator.ofFloat(0f, 8f, 0f)
        animator.duration = DURATION
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { valueAnimator ->
            val progress = valueAnimator.animatedValue as Float
            img.scaleX = progress
            img.scaleY = progress
        }
        val mediaPlayer = MediaPlayer.create(this, R.raw.transition)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                Handler(Looper.getMainLooper()).postDelayed({
                    mediaPlayer.start()
                }, animator.startDelay)
            }
            override fun onAnimationEnd(animation: Animator) {
                val intent = Intent(this@IntroAnimation, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })

        val rotateAnimation = ObjectAnimator.ofFloat(img, "rotation", 0f, 3600f)
        rotateAnimation.duration = DURATION
        rotateAnimation.interpolator = LinearInterpolator()

        val set = AnimatorSet()
        set.playTogether(animator, rotateAnimation)
        set.start()
    }
}