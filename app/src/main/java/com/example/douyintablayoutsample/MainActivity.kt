package com.example.douyintablayoutsample

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.douyintablayout.DouyinTabLayout

class MainActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager2
    lateinit var douyinTabLayout: DouyinTabLayout
    private var initialIndex = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewPager()
        initTabLayout()
    }

    private fun initTabLayout() {
        douyinTabLayout = findViewById(R.id.douyinTabLayout)
        douyinTabLayout.addTabs(TABS, initialIndex)
        douyinTabLayout.setOnTabSelectedListener {
            viewPager.setCurrentItem(it, false)
        }
    }

    private fun initViewPager() {
        viewPager = findViewById(R.id.view_pager)
        viewPager.adapter = Adapter()
        viewPager.setCurrentItem(initialIndex, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                douyinTabLayout.setSelect(position, true)
            }
        })
    }

    class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val tv = TextView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                gravity = Gravity.CENTER
                textSize = 30f
                setTextColor(Color.WHITE)
            }
            return ViewHolder(tv)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindData(TABS[position], COLORS[position])
        }

        override fun getItemCount(): Int {
            return TABS.size
        }

    }

    class ViewHolder(itemView: TextView) : RecyclerView.ViewHolder(itemView) {
        fun bindData(text: String, color: Int) {
            itemView.setBackgroundColor(color)
            (itemView as TextView).text = text
        }
    }

    companion object {
        val COLORS = listOf<Int>(
            Color.BLACK,
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.LTGRAY,
            Color.MAGENTA
        )
        val TABS = listOf<String>("BLACK", "BLUE", "RED", "GREEN", "LTGRAY", "MAGENTA")
    }
}