package com.mapzen.erasermap.view

import android.view.View
import android.view.ViewGroup
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.R
import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.valhalla.Instruction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.ArrayList

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
public class InstructionAdapterTest {
    private var adapter: InstructionAdapter? = null
    private var instructions = ArrayList<Instruction>()

    @Before fun setUp() {
        instructions.add(TestHelper.getTestInstruction())
        instructions.add(TestHelper.getTestInstruction())
        instructions.add(TestHelper.getTestInstruction())
        adapter = InstructionAdapter(RuntimeEnvironment.application, instructions)
    }

    @Test fun shouldNotBeNull() {
        assertThat(adapter).isNotNull()
    }

    @Test fun instantiateItem_shouldSetDistanceViewVisibility() {
        assertThat(getView(0).findViewById(R.id.distance).visibility).isEqualTo(View.VISIBLE)
        assertThat(getView(1).findViewById(R.id.distance).visibility).isEqualTo(View.VISIBLE)
        assertThat(getView(2).findViewById(R.id.distance).visibility).isEqualTo(View.GONE)
    }

    private fun getView(position: Int): View {
        return adapter?.instantiateItem(TestViewGroup(), position) as View
    }

    class TestViewGroup : ViewGroup(RuntimeEnvironment.application) {
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            throw UnsupportedOperationException()
        }
    }
}
