package com.smartautoclicker.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var wordInput: EditText
    private lateinit var status: TextView
    private lateinit var speedValue: TextView
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }
    private val bg = Color.rgb(5, 14, 31)
    private val card = Color.rgb(12, 31, 59)
    private val card2 = Color.rgb(10, 24, 46)
    private val accent = Color.rgb(37, 139, 255)
    private val muted = Color.rgb(153, 181, 220)
    private val white = Color.rgb(245, 249, 255)
    private val green = Color.rgb(0, 155, 111)
    private val red = Color.rgb(203, 42, 73)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun rounded(color: Int, radius: Int = 20, strokeColor: Int? = null): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat(); if (strokeColor != null) setStroke(dp(1), strokeColor) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        if (android.os.Build.VERSION.SDK_INT >= 23) window.decorView.systemUiVisibility = 0

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(26))
            setBackgroundColor(bg)
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(4), 0, dp(18)) }
        header.addView(TextView(this).apply {
            text = "➤"; textSize = 30f; gravity = Gravity.CENTER; setTextColor(Color.WHITE)
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(accent, Color.rgb(42, 70, 235))).apply { shape = GradientDrawable.OVAL }
        }, LinearLayout.LayoutParams(dp(60), dp(60)))
        val titleCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(13), 0, 0, 0) }
        titleCol.addView(TextView(this).apply { text = "Smart Auto Clicker"; textSize = 21f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD })
        titleCol.addView(TextView(this).apply { text = "Auto Click • Fast • Easy"; textSize = 13f; setTextColor(muted); setPadding(0, dp(4), 0, 0) })
        header.addView(titleCol, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(TextView(this).apply { text = "⚙"; textSize = 24f; setTextColor(white); gravity = Gravity.CENTER; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }, LinearLayout.LayoutParams(dp(38), dp(44)))
        page.addView(header)

        val serviceCard = horizontalCard()
        serviceCard.addView(TextView(this).apply { text = "●"; textSize = 18f; setTextColor(if (isServiceEnabled()) Color.rgb(40, 220, 139) else Color.rgb(255, 190, 70)) })
        val serviceTexts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), 0, 0, 0) }
        serviceTexts.addView(TextView(this).apply { text = "خدمة إمكانية الوصول"; textSize = 15f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD })
        serviceTexts.addView(TextView(this).apply { text = if (isServiceEnabled()) "مفعّلة وجاهزة للاستخدام" else "فعّلها لبدء استخدام التطبيق"; textSize = 12f; setTextColor(muted); setPadding(0, dp(3), 0, 0) })
        serviceCard.addView(serviceTexts, LinearLayout.LayoutParams(0, -2, 1f))
        serviceCard.addView(TextView(this).apply { text = "›"; textSize = 28f; setTextColor(muted); gravity = Gravity.CENTER; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }, LinearLayout.LayoutParams(dp(28), dp(42)))
        page.addView(serviceCard, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })

        val wordCard = verticalCard()
        wordCard.addView(sectionTitle("⌕", "كلمة البحث أو الوصف"))
        wordInput = EditText(this).apply {
            hint = "اكتب الكلمة التي تريد النقر عليها"; setHintTextColor(Color.rgb(117, 146, 183)); setTextColor(white); textSize = 15f
            maxLines = 1; setPadding(dp(13), dp(12), dp(13), dp(12))
            background = rounded(Color.rgb(7, 22, 43), 14, Color.rgb(31, 84, 145)); setText(prefs.getString("word", ""))
        }
        wordCard.addView(wordInput, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        val saveWord = actionButton("حفظ الكلمة", "✓", accent) { saveWord() }
        wordCard.addView(saveWord, LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(10) })
        page.addView(wordCard, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })

        val actionRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actionRow.addView(actionButton("تشغيل", "▶", green) {
            if (!saveWord()) return@actionButton
            if (isServiceEnabled()) { prefs.edit().putBoolean("running", true).apply(); showStatus("الحالة: يعمل", true) }
            else { showStatus("فعّل خدمة إمكانية الوصول أولًا", false); startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }, LinearLayout.LayoutParams(0, dp(70), 1f).apply { marginEnd = dp(6) })
        actionRow.addView(actionButton("إيقاف", "■", red) {
            prefs.edit().putBoolean("running", false).apply(); showStatus("الحالة: متوقف", false)
        }, LinearLayout.LayoutParams(0, dp(70), 1f).apply { marginStart = dp(6) })
        page.addView(actionRow, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })

        val speedCard = verticalCard()
        val speedHeader = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        speedHeader.addView(TextView(this).apply { text = "◴"; textSize = 23f; setTextColor(Color.rgb(83, 190, 255)) })
        speedHeader.addView(TextView(this).apply { text = "سرعة النقر"; textSize = 15f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD; setPadding(dp(9), 0, 0, 0) }, LinearLayout.LayoutParams(0, -2, 1f))
        speedValue = TextView(this).apply { textSize = 13f; setTextColor(white); gravity = Gravity.CENTER; setPadding(dp(10), dp(6), dp(10), dp(6)); background = rounded(Color.rgb(19, 67, 130), 12) }
        speedHeader.addView(speedValue)
        speedCard.addView(speedHeader)
        val delay = prefs.getInt("click_delay_ms", 1000).coerceIn(1, 5000)
        speedValue.text = "$delay ms"
        speedCard.addView(SeekBar(this).apply {
            max = 4999; progress = delay - 1
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(Color.rgb(75, 190, 255))
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val ms = progress + 1; speedValue.text = "$ms ms"; prefs.edit().putInt("click_delay_ms", ms).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        })
        val range = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        range.addView(TextView(this).apply { text = "1 ms"; textSize = 11f; setTextColor(muted) }, LinearLayout.LayoutParams(0, -2, 1f))
        range.addView(TextView(this).apply { text = "5000 ms"; textSize = 11f; setTextColor(muted); gravity = Gravity.END }, LinearLayout.LayoutParams(0, -2, 1f))
        speedCard.addView(range)
        page.addView(speedCard, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })

        val infoRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        infoRow.addView(statCard("◉", "طريقة العمل", "كلمة / وصف"), LinearLayout.LayoutParams(0, dp(86), 1f).apply { marginEnd = dp(5) })
        infoRow.addView(statCard("◷", "فاصل النقر", "${delay} ms"), LinearLayout.LayoutParams(0, dp(86), 1f).apply { marginStart = dp(5); marginEnd = dp(5) })
        infoRow.addView(statCard("●", "الحالة", if (prefs.getBoolean("running", false)) "يعمل" else "متوقف"), LinearLayout.LayoutParams(0, dp(86), 1f).apply { marginStart = dp(5) })
        page.addView(infoRow, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })

        val settingsCard = verticalCard()
        settingsCard.addView(menuRow("⚙", "إعدادات الخدمة", "تفعيل إمكانية الوصول") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
        settingsCard.addView(separator())
        settingsCard.addView(menuRow("▣", "إظهار النافذة العائمة", "إعادة فتحها بعد إغلاقها بزر ×") {
            if (isServiceEnabled()) {
                sendBroadcast(Intent(AutoClickService.ACTION_SHOW_FLOATING_WINDOW).setPackage(packageName))
                Toast.makeText(this, "تم طلب إظهار النافذة العائمة", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "فعّل خدمة إمكانية الوصول أولًا", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })
        settingsCard.addView(separator())
        settingsCard.addView(menuRow("ⓘ", "حول التطبيق", "Smart Auto Clicker • الإصدار 2.2") { Toast.makeText(this, "Smart Auto Clicker 2.2", Toast.LENGTH_SHORT).show() })
        page.addView(settingsCard)

        status = TextView(this).apply {
            text = if (prefs.getBoolean("running", false)) "● الحالة: يعمل" else "● الحالة: متوقف"
            textSize = 12f; setTextColor(if (prefs.getBoolean("running", false)) Color.rgb(75, 226, 157) else muted)
            gravity = Gravity.CENTER; setPadding(dp(8), dp(17), dp(8), 0)
        }
        page.addView(status)

        setContentView(ScrollView(this).apply { isFillViewport = true; setBackgroundColor(bg); addView(page) })
    }

    private fun horizontalCard() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; background = rounded(card, 20, Color.rgb(27, 63, 105)); setPadding(dp(15), dp(13), dp(13), dp(13)) }
    private fun verticalCard() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = rounded(card, 20, Color.rgb(27, 63, 105)); setPadding(dp(15), dp(14), dp(15), dp(14)) }
    private fun sectionTitle(icon: String, title: String) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        addView(TextView(this@MainActivity).apply { text = icon; textSize = 22f; setTextColor(Color.rgb(83, 190, 255)) })
        addView(TextView(this@MainActivity).apply { text = title; textSize = 15f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD; setPadding(dp(8), 0, 0, 0) })
    }
    private fun actionButton(label: String, symbol: String, color: Int, params: LinearLayout.LayoutParams? = null, action: () -> Unit): LinearLayout {
        val button = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; background = rounded(color, 17); isClickable = true; isFocusable = true; setOnClickListener { action() } }
        button.addView(TextView(this).apply { text = symbol; textSize = 19f; setTextColor(Color.WHITE); setPadding(0, 0, dp(8), 0) })
        button.addView(TextView(this).apply { text = label; textSize = 15f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD })
        if (params != null) button.layoutParams = params
        return button
    }
    private fun statCard(icon: String, label: String, value: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; background = rounded(card2, 16, Color.rgb(27, 63, 105)); setPadding(dp(4), dp(7), dp(4), dp(7))
        addView(TextView(this@MainActivity).apply { text = icon; textSize = 18f; setTextColor(Color.rgb(83, 190, 255)) })
        addView(TextView(this@MainActivity).apply { text = label; textSize = 10f; setTextColor(muted); gravity = Gravity.CENTER; setPadding(0, dp(2), 0, 0) })
        addView(TextView(this@MainActivity).apply { text = value; textSize = 11f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, dp(3), 0, 0) })
    }
    private fun menuRow(icon: String, title: String, subtitle: String, action: () -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(9), 0, dp(9)); isClickable = true; setOnClickListener { action() }
        addView(TextView(this@MainActivity).apply { text = icon; textSize = 21f; gravity = Gravity.CENTER; setTextColor(Color.WHITE); background = rounded(Color.rgb(20, 68, 130), 14) }, LinearLayout.LayoutParams(dp(42), dp(42)))
        val col = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), 0, 0, 0) }
        col.addView(TextView(this@MainActivity).apply { text = title; textSize = 13f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD })
        col.addView(TextView(this@MainActivity).apply { text = subtitle; textSize = 10f; setTextColor(muted); setPadding(0, dp(3), 0, 0) })
        addView(col, LinearLayout.LayoutParams(0, -2, 1f))
        addView(TextView(this@MainActivity).apply { text = "›"; textSize = 25f; setTextColor(muted) })
    }
    private fun separator() = View(this).apply { setBackgroundColor(Color.rgb(27, 63, 105)); layoutParams = LinearLayout.LayoutParams(-1, dp(1)) }
    private fun showStatus(message: String, running: Boolean) { status.text = "● $message"; status.setTextColor(if (running) Color.rgb(75, 226, 157) else muted) }
    private fun saveWord(): Boolean {
        val word = wordInput.text.toString().trim()
        if (word.isEmpty()) { wordInput.error = "اكتب الكلمة أولًا"; return false }
        prefs.edit().putString("word", word).apply(); showStatus("تم حفظ الكلمة", false); return true
    }
    private fun isServiceEnabled(): Boolean {
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        return manager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
            it.resolveInfo.serviceInfo.packageName == packageName && it.resolveInfo.serviceInfo.name.endsWith("AutoClickService")
        }
    }
}