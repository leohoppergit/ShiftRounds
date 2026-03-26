package de.nulide.shiftcal.ui.helper

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import de.nulide.shiftcal.ui.MainActivity
import de.nulide.shiftcal.utils.Runner

abstract class SFragment : Fragment() {

    lateinit var root: View
    lateinit var ctx: Context
    lateinit var main: MainActivity

    private var hasUIData = false

    abstract val fragmentName: String

    val handler = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null

    val freeFragmentPager = Runnable {
        enableViewPagerInput()
    }

    fun setTitle(title: String) {
        Runner.run {
            main.setTitle(title)
        }
    }

    fun disableViewPagerInput() {
        main.binding.fragmentPager.isUserInputEnabled = false
        runnable?.let { handler.removeCallbacks(it) }
        handler.postDelayed(freeFragmentPager, 200)
    }

    fun enableViewPagerInput() {
        main.binding.fragmentPager.isUserInputEnabled = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reloadUIData()
    }

    fun <T : View> findViewById(id: Int): T {
        return root.findViewById(id)
    }

    open fun onCreateMenu(menu: Menu?, inflater: MenuInflater) {

    }

    open fun onTitleClicked() {

    }

    fun reloadUIData() {
        try {
            ctx = requireContext()
            main = requireActivity() as MainActivity
            hasUIData = true
        } catch (e: IllegalStateException) {
            hasUIData = false
        }
    }

    override fun onResume() {
        super.onResume()
        reloadUIData()
        if (hasUIData && !isHidden) {
            updateActivity()
        }
    }

    open fun updateActivity() {

    }

    open fun backPressed(): Boolean {
        return false
    }

}