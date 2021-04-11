package com.pkmnapps.activitydo

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pkmnapps.activitydo.HomeFragment
import com.pkmnapps.activitydo.adapters.ActivityAdapter
import com.pkmnapps.activitydo.adapters.ColorThemeAdapter
import com.pkmnapps.activitydo.custominterfaces.HomeFragInterace
import com.pkmnapps.activitydo.databasehelpers.DBHelper
import com.pkmnapps.activitydo.dataclasses.ActivityData
import org.junit.runner.RunWith
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HomeFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), HomeFragInterace {
    var view: View? = null
    var activityDataList: MutableList<ActivityData?>? = null
    var activityAdapter: ActivityAdapter? = null
    var recyclerView: RecyclerView? = null
    var dbHelper: DBHelper? = null
    var dialogView: View? = null
    var colorTheme = MConstants.colors[0]
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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false)
        Objects.requireNonNull(activity).setTitle("Home")
        activityDataList = ArrayList()
        activityAdapter = ActivityAdapter(activityDataList, this@HomeFragment)
        recyclerView = view.findViewById(R.id.recycler_view)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.getContext())
        recyclerView.setLayoutManager(mLayoutManager)
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setNestedScrollingEnabled(false)
        dbHelper = DBHelper(context)
        val itcallback: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {
            //for sorting
            override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN or ItemTouchHelper.UP)
            }

            override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
                // get the viewHolder's and target's positions in your adapter data, swap them
                Collections.swap(activityDataList, viewHolder.getAdapterPosition(), target.getAdapterPosition())
                //                activityDataList.get(viewHolder.getAdapterPosition()).setSortOrder(target.getAdapterPosition());
//                activityDataList.get(target.getAdapterPosition()).setSortOrder(viewHolder.getAdapterPosition());
                //s
                // and notify the adapter that its dataset has changed
                activityAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition())
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {}
            override fun onMoved(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, fromPos: Int, target: RecyclerView.ViewHolder?, toPos: Int, x: Int, y: Int) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
                dbHelper.updateAllSortOrders(activityDataList)
            }
        }
        val itcallbackswipe: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT)
            }

            override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                displayDeleteDialog(activityDataList.get(viewHolder.getAdapterPosition()))
            }
        }
        val itemTouchHelperSwipe = ItemTouchHelper(itcallbackswipe)
        val itemTouchHelper = ItemTouchHelper(itcallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        itemTouchHelperSwipe.attachToRecyclerView(recyclerView)
        val addNewButton = view.findViewById<Button?>(R.id.addNewButton)
        addNewButton.setOnClickListener { //open a popup for adding new
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            dialogView = inflater.inflate(R.layout.new_activity_alertbox, null)
            builder.setView(dialogView)
            changeColorTheme(Random().nextInt(MConstants.colors.size)) //gives random number from 0 to max(excluding)
            val recyclerView: RecyclerView = dialogView.findViewById(R.id.recycler_view)
            val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = mLayoutManager
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.adapter = ColorThemeAdapter(this@HomeFragment)
            builder.setPositiveButton("Add") { dialog, which ->
                val id: String
                val name: String
                val color: String?
                //add to list
                val nameEdiText = dialogView.findViewById<EditText?>(R.id.nameEditText)
                id = System.currentTimeMillis().toString()
                name = nameEdiText.text.toString()
                color = colorTheme
                if (name != "") {
                    val a = ActivityData(id, name, color, false, 0)
                    activityDataList.add(0, a)
                    //add to database
                    dbHelper.insertActivity(a)
                    //update UI
                    activityAdapter.notifyDataSetChanged()
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        //clear-up previous data if any...
        activityDataList.clear()
        //add from database here
        activityDataList.addAll(dbHelper.getAllActivitiesAsList())
        //sort the list here...
        activityAdapter.notifyDataSetChanged()
        recyclerView.setAdapter(activityAdapter)
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

    override fun displayEditDialog(activityData: ActivityData?) {
        //open a popup for adding new
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        dialogView = inflater.inflate(R.layout.new_activity_alertbox, null)
        builder.setView(dialogView)
        changeColorTheme(activityData.getColor())
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recycler_view)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = ColorThemeAdapter(this@HomeFragment)
        val nameEdiText = dialogView.findViewById<EditText?>(R.id.nameEditText)
        nameEdiText.setText(activityData.getName())
        builder.setPositiveButton("Edit") { dialog, which ->
            val name: String
            val color: String?
            name = nameEdiText.text.toString()
            color = colorTheme
            if (name != "") {
                activityData.setName(name)
                activityData.setColor(color)
                //add to database
                dbHelper.updateActivity(activityData)
                //update UI
                activityAdapter.notifyItemChanged(activityDataList.indexOf(activityData))
            }
        }
        builder.setNeutralButton("Delete") { dialog, which -> displayDeleteDialog(activityData) }
        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

    override fun displayDeleteDialog(activityData: ActivityData?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete '" + activityData.getName() + "' ?")
        builder.setPositiveButton("Delete") { dialog, which -> //delete from database
            DBHelper(context).deleteActivity(activityData.getId())
            //delete from list
            val x = activityDataList.indexOf(activityData)
            activityDataList.remove(activityData)
            activityAdapter.notifyItemRemoved(x)
            //also remove from pinned menu if it exists
            updatePinnedMenu()
        }
        builder.setOnCancelListener { activityAdapter.notifyItemChanged(activityDataList.indexOf(activityData)) }
        builder.show()
    }

    override fun changeColorTheme(pos: Int) {
        //save to variable
        colorTheme = MConstants.colors[pos]
        //change indicator in view
        dialogView.setBackgroundColor(Color.parseColor(colorTheme))
    }

    private fun changeColorTheme(colorTheme: String?) {
        dialogView.setBackgroundColor(Color.parseColor(colorTheme))
    }

    override fun updatePinnedMenu() {
        (Objects.requireNonNull(activity) as MainActivity?).menuAddPinned()
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
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): HomeFragment? {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}