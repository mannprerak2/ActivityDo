package com.pkmnapps.activitydo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import org.junit.runner.RunWith
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TabFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TabFragment : Fragment() {
    private var mListener: OnFragmentInteractionListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            // TODO: Rename and change types of parameters
            val mParam1 = arguments.getString(ARG_PARAM1)
            val mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tab, container, false)
        val viewPager: ViewPager = view.findViewById(R.id.view_pager)
        setupViewPager(viewPager)
        // Set Tabs inside Toolbar
        val tabs: TabLayout = view.findViewById(R.id.tabLayout)
        tabs.setupWithViewPager(viewPager)
        // Inflate the layout for this fragment
        return view
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        val adapter = Adapter(childFragmentManager)
        adapter.addFragment(QuickNotesFragment(), "Quick Notes")
        adapter.addFragment(HomeFragment(), "Activities")
        viewPager.setAdapter(adapter)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context as OnFragmentInteractionListener?
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        open fun onFragmentInteraction(uri: Uri?)
    }

    internal class Adapter(manager: FragmentManager?) : FragmentPagerAdapter(manager) {
        private val mFragmentList: MutableList<Fragment?>? = ArrayList()
        private val mFragmentTitleList: MutableList<String?>? = ArrayList()
        override fun getItem(position: Int): Fragment? {
            return mFragmentList.get(position)
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment?, title: String?) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList.get(position)
        }
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1: String? = "param1"
        private val ARG_PARAM2: String? = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TabFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): TabFragment? {
            val fragment = TabFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}