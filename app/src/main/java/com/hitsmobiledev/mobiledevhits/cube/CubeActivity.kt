package com.hitsmobiledev.mobiledevhits.cube

import android.os.Bundle
import com.hitsmobiledev.mobiledevhits.BaseFiltersActivity

class CubeActivity : BaseFiltersActivity() {
    private lateinit var cubeView: CubeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cubeView = CubeView(this)
        setContentView(cubeView)
    }
}
