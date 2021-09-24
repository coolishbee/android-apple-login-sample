package com.apple.login

import android.content.Intent
import com.apple.login.java.MainActivity
import com.apple.login.kotlin.SampleActivity

class ChoiceActivity : BaseChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return kotlin.collections.listOf(
                Choice(
                        "Java",
                        "Run the Apple Login in Java.",
                        Intent(this, MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Apple Login in Kotlin.",
                        Intent(this, SampleActivity::class.java))
        )
    }
}