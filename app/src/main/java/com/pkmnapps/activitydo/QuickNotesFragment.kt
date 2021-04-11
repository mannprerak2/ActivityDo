package com.pkmnapps.activitydo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flipboard.bottomsheet.BottomSheetLayout
import com.flipboard.bottomsheet.commons.ImagePickerSheetView
import com.flipboard.bottomsheet.commons.MenuSheetView
import com.pkmnapps.activitydo.QuickNotesFragment
import com.pkmnapps.activitydo.adapters.ActivityContentAdapter
import com.pkmnapps.activitydo.custominterfaces.TaskActivityInterface
import com.pkmnapps.activitydo.databasehelpers.DBHelperImage
import com.pkmnapps.activitydo.databasehelpers.DBHelperList
import com.pkmnapps.activitydo.databasehelpers.DBHelperText
import com.pkmnapps.activitydo.databasehelpers.DBHelperWidgets
import com.pkmnapps.activitydo.dataclasses.*
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [QuickNotesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [QuickNotesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuickNotesFragment : Fragment(), TaskActivityInterface {
    var activityData: ActivityData? = null
    var bottomSheetLayout: BottomSheetLayout? = null
    var currentSheetView: View? = null
    var recyclerView: RecyclerView? = null
    var activityContentAdapter: ActivityContentAdapter? = null
    var widgets: MutableList<Widget?>? = null
    var cameraImageUri: Uri? = null
    var tempUid: String? = null
    var dbHelperWidgets: DBHelperWidgets? = null
    var view: View? = null
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
        view = inflater.inflate(R.layout.fragment_quick_notes, container, false) //same layout for both
        activityData = ActivityData("0", "Quick Notes", "#ffffff", false, 0) //only id is useful here
        dbHelperWidgets = DBHelperWidgets(context)
        bottomSheetLayout = view.findViewById(R.id.bottomsheet)
        setUpJumpControls()
        setUpRecyclerView()
        initialiseRecyclerViewData()


        // Inflate the layout for this fragment
        return view
    }

    override fun onStart() {
        if (activityMovedHere) { //used when item is moved to quicknotes from any activity
            widgets.clear()
            initialiseRecyclerViewData()
            activityMovedHere = false
        }
        super.onStart()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.w("n", requestCode.toString())
        Log.w("n", resultCode.toString())
        if (resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri?
            if (requestCode == MConstants.REQUEST_LOAD_IMAGE && data != null) { //image from gallery
                selectedImage = data.data
                if (selectedImage != null) {
                    saveImageAddWidget(selectedImage)
                } else {
                    genericError()
                }
            } else if (requestCode == MConstants.REQUEST_IMAGE_CAPTURE) { //image from camera
                // Do something with imagePath
                selectedImage = cameraImageUri
                if (selectedImage != null) {
                    addImageWidget(selectedImage)
                } else {
                    genericError()
                }
            } else if (requestCode == MConstants.REQUEST_NEW_NOTE && data != null) {

                //save note
                val uid = data.getStringExtra("uid")
                val index = data.getIntExtra("index", -1000)
                val dbHelperText = DBHelperText(context)

                //update note
                val s: SimpleTextWidget?
                if (index == -1000) { //for new note
                    s = dbHelperText.getTextWidget(uid)
                    if (s != null) widgets.add(0, Widget(MConstants.textW, s, uid, 0))
                } else { //for edited note
                    s = dbHelperText.getTextWidget(uid)
                    if (s != null) {
                        widgets.set(index, Widget(MConstants.textW, s, uid, index)) //replacing widget
                    } else {
                        widgets.removeAt(index) //delete as it doesnt exist
                    }
                }
                //update UI
                dbHelperWidgets.updateAllWidgetSortOrders(widgets)
                activityContentAdapter.notifyDataSetChanged()
            } else if (requestCode == MConstants.REQUEST_NEW_LIST && data != null) {
                val dbHelperList = DBHelperList(context)
                val lid = data.getStringExtra("lid")
                val listWidget = dbHelperList.getListWidget(lid)
                val index = data.getIntExtra("index", -1000)
                if (index == -1000) { //new list
                    if (listWidget != null) widgets.add(0, Widget(MConstants.listW, listWidget, lid, 0))
                } else { //more list
                    if (listWidget != null) widgets.set(index, Widget(MConstants.listW, listWidget, lid, index)) else {
                        widgets.removeAt(index)
                    }
                }
                //update ui
                dbHelperWidgets.updateAllWidgetSortOrders(widgets)
                activityContentAdapter.notifyDataSetChanged()
            } else if (requestCode == MConstants.REQUEST_WIDGET_ACTIVITY_CHANGE && data != null) {
                val pos = data.getIntExtra("pos", -1)
                if (pos != -1) {
                    widgets.removeAt(pos)
                    activityContentAdapter.notifyItemRemoved(pos)
                }
            }
        }
    }

    private fun setUpJumpControls() {
        val takeNote = view.findViewById<TextView?>(R.id.note_tvbutton)
        takeNote.setOnClickListener { createNote() }
        val more = view.findViewById<ImageButton?>(R.id.more_button)
        more.setOnClickListener { displayWidgetChoser() }
        val list = view.findViewById<ImageButton?>(R.id.list_button)
        list.setOnClickListener { createList() }
        val image = view.findViewById<ImageButton?>(R.id.image_button)
        image.setOnClickListener {
            if (checkNeedsPermission()) {
                requestStoragePermission()
            } else {
                createImageSheet()
            }
        }
    }

    fun setUpRecyclerView() {
        recyclerView = view.findViewById(R.id.recycler_view)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.setLayoutManager(mLayoutManager)
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setNestedScrollingEnabled(true)
        val itcallback: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {
            //for sorting
            override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN or ItemTouchHelper.UP)
            }

            override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
                // get the viewHolder's and target's positions in your adapter data, swap them
                Collections.swap(widgets, viewHolder.getAdapterPosition(), target.getAdapterPosition())
                //                widgets.get(viewHolder.getAdapterPosition()).setSortOrder(target.getAdapterPosition());
//                widgets.get(target.getAdapterPosition()).setSortOrder(viewHolder.getAdapterPosition());
                //s
                // and notify the adapter that its dataset has changed
                activityContentAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition())
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {}
            override fun onMoved(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, fromPos: Int, target: RecyclerView.ViewHolder?, toPos: Int, x: Int, y: Int) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
                dbHelperWidgets.updateAllWidgetSortOrders(widgets)
            }
        }
        val itcallbackswipe: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.RIGHT)
            }

            override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                deleteWidget(widgets.get(viewHolder.getAdapterPosition()))
            }
        }
        val itemTouchHelperSwipe = ItemTouchHelper(itcallbackswipe)
        val itemTouchHelper = ItemTouchHelper(itcallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        itemTouchHelperSwipe.attachToRecyclerView(recyclerView)
        widgets = ArrayList()
        activityContentAdapter = ActivityContentAdapter(widgets, this@QuickNotesFragment)
        recyclerView.setAdapter(activityContentAdapter)
    }

    fun initialiseRecyclerViewData() {
        val dbHelperWidgets = DBHelperWidgets(context)

        //add all simpleTextWidgets
        val dbHelperText = DBHelperText(context)
        for (o in dbHelperText.getAllTextsAsList(activityData.getId())) {
            if (o != null) {
                widgets.add(Widget(MConstants.textW, o, o.uid, dbHelperWidgets.getSortValue(o.uid)))
            }
        }
        //add all lists
        val dbHelperList = DBHelperList(context)
        for (o in dbHelperList.getAllListAsList(activityData.getId())) {
            if (o != null) {
                widgets.add(Widget(MConstants.listW, o, o.uid, dbHelperWidgets.getSortValue(o.uid)))
            }
        }

        //add all image widgets
        val dbHelperImage = DBHelperImage(context)
        for (o in dbHelperImage.getAllImagesAsList(activityData.getId())) {
            if (o != null) widgets.add(Widget(MConstants.imageW, o, o.uid, dbHelperWidgets.getSortValue(o.uid)))
        }
        sortWidgetList()
        //sort the list
        activityContentAdapter.notifyDataSetChanged()
    }

    fun sortWidgetList() {
        var swap: Boolean
        for (i in 0 until widgets.size - 1) {
            swap = false
            for (j in 0 until widgets.size - 1) {
                if (widgets.get(j).getSortOrder() > widgets.get(j + 1).getSortOrder()) {
                    val temp = widgets.get(j)
                    widgets.set(j, widgets.get(j + 1))
                    widgets.set(j + 1, temp)
                    swap = true
                } else if (widgets.get(j).getSortOrder() == widgets.get(j + 1).getSortOrder()) {
                    if (widgets.get(j).getUid().toLong() < widgets.get(j + 1).getUid().toLong()) {
                        val temp = widgets.get(j)
                        widgets.set(j, widgets.get(j + 1))
                        widgets.set(j + 1, temp)
                        swap = true
                    }
                }
            }
            if (!swap) break
        }
    }

    fun displayWidgetChoser() {
        //show bottomsheet
        val menuSheetView = MenuSheetView(Objects.requireNonNull(context), MenuSheetView.MenuType.LIST, "Chose widget", MenuSheetView.OnMenuItemClickListener { item ->
            if (bottomSheetLayout.isSheetShowing()) {
                bottomSheetLayout.dismissSheet()
            }
            when (item.itemId) {
                R.id.item_note -> {
                    createNote()
                    return@OnMenuItemClickListener true
                }
                R.id.item_list -> {
                    createList()
                    return@OnMenuItemClickListener true
                }
                R.id.item_image -> {
                    if (checkNeedsPermission()) {
                        requestStoragePermission()
                    } else {
                        createImageSheet()
                    }
                    return@OnMenuItemClickListener true
                }
            }
            true
        })
        menuSheetView.inflateMenu(R.menu.widget_menu)
        bottomSheetLayout.setShouldDimContentView(false)
        bottomSheetLayout.showWithSheetView(menuSheetView)
    }

    fun createNote() {
        val intent = Intent(context, NoteActivity::class.java)
        val uid = System.currentTimeMillis().toString()
        intent.putExtra("uid", uid)
        DBHelperText(context).insertText(uid, activityData.getId(), "", "")
        startActivityForResult(intent, MConstants.REQUEST_NEW_NOTE)
    }

    fun createList() {
        val intent = Intent(context, ListActivity::class.java)
        val lid = System.currentTimeMillis().toString()
        intent.putExtra("lid", lid)
        intent.putExtra("aid", activityData.getId())
        //create a list in database
        DBHelperList(context).insertList(lid, activityData.getId(), "")
        startActivityForResult(intent, MConstants.REQUEST_NEW_LIST)
    }

    fun createImageSheet() {
        val sheetView = ImagePickerSheetView.Builder(Objects.requireNonNull(context))
                .setMaxItems(30)
                .setShowCameraOption(createCameraIntent() != null)
                .setShowPickerOption(createPickIntent() != null)
                .setImageProvider { imageView, imageUri, size -> //feeds image to sheet views
                    Glide.with(Objects.requireNonNull(context))
                            .load(imageUri)
                            .into(imageView)
                }
                .setOnTileSelectedListener { selectedTile ->
                    bottomSheetLayout.dismissSheet()
                    tempUid = System.currentTimeMillis().toString() //do not remove this
                    if (selectedTile.isCameraTile) {
                        dispatchTakePictureIntent()
                    } else if (selectedTile.isPickerTile) {
                        startActivityForResult(createPickIntent(), MConstants.REQUEST_LOAD_IMAGE)
                    } else if (selectedTile.isImageTile) {
                        if (selectedTile.imageUri != null) saveImageAddWidget(selectedTile.imageUri)
                    } else {
                        genericError()
                    }
                }
                .setTitle("Choose an image...")
                .create()
        bottomSheetLayout.showWithSheetView(sheetView)
    }

    private fun checkNeedsPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(Objects.requireNonNull(activity), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(activity), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(activity, arrayOf<String?>(Manifest.permission.WRITE_EXTERNAL_STORAGE), MConstants.REQUEST_STORAGE)
        } else {
            // Eh, prompt anyway
            ActivityCompat.requestPermissions(activity, arrayOf<String?>(Manifest.permission.WRITE_EXTERNAL_STORAGE), MConstants.REQUEST_STORAGE)
        }
    }

    private fun addImageWidget(selectedImageUri: Uri?) {
        //save image uri to image database
        val imageWidget = ImageWidget(tempUid, activityData.getId(), selectedImageUri.toString())
        val dbHelperImage = DBHelperImage(context)
        dbHelperImage.insertImage(imageWidget)

        //show in widgets
        widgets.add(0, Widget(MConstants.imageW, imageWidget, tempUid, 0))
        //update UI
        dbHelperWidgets.updateAllWidgetSortOrders(widgets)
        activityContentAdapter.notifyDataSetChanged()
    }

    private fun saveImageAddWidget(selectedImageUri: Uri?) {
        if (selectedImageUri != null) {
            try {
                val file = createImageFile()
                val input = Objects.requireNonNull(context).contentResolver.openInputStream(selectedImageUri)
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
                //save image uri to image database
                val imageWidget = ImageWidget(tempUid, activityData.getId(), Uri.fromFile(file).toString())
                val dbHelperImage = DBHelperImage(context)
                dbHelperImage.insertImage(imageWidget)

                //show in widgets
                widgets.add(0, Widget(MConstants.imageW, imageWidget, tempUid, 0))
                //update UI
                dbHelperWidgets.updateAllWidgetSortOrders(widgets)
                activityContentAdapter.notifyDataSetChanged()
            } catch (ignored: Exception) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createImageFile(tempUid: String?): File? {
        // Create an image file name
        val imageFileName = "$tempUid.jpg"
        return File(Objects.requireNonNull(context).getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName)
    }

    private fun createPickIntent(): Intent? {
        val picImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        return if (picImageIntent.resolveActivity(Objects.requireNonNull(activity).getPackageManager()) != null) {
            picImageIntent
        } else {
            null
        }
    }

    private fun createCameraIntent(): Intent? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return if (takePictureIntent.resolveActivity(Objects.requireNonNull(activity).getPackageManager()) != null) {
            takePictureIntent
        } else {
            null
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = createCameraIntent()
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent != null) {
            try {
                val imageFile = createImageFile()
                // Save a file: path for use with ACTION_VIEW intents
                cameraImageUri = FileProvider.getUriForFile(Objects.requireNonNull(context), "com.pkmnapps.activitydo.fileprovider", imageFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                startActivityForResult(takePictureIntent, MConstants.REQUEST_IMAGE_CAPTURE)
            } catch (e: Exception) {
                // Error occurred while creating the File
                genericError("Could not create imageFile for camera")
            }
        }
    }

    private fun genericError(message: String? = null) {
        Toast.makeText(context, message ?: "Something went wrong.", Toast.LENGTH_SHORT).show()
    }

    private fun createImageFile(): File? {
        // Create an image file name
        val imageFileName = "$tempUid.jpg"
        return File(Objects.requireNonNull(context).getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName)
    }

    override fun editWidget(widget: Widget?) {
        when (widget.getType()) {
            MConstants.textW -> {
                val simpleTextWidget = widget.getObject() as SimpleTextWidget
                val intent = Intent(context, NoteActivity::class.java)
                intent.putExtra("uid", simpleTextWidget.uid)
                intent.putExtra("head", simpleTextWidget.head)
                intent.putExtra("body", simpleTextWidget.body)
                intent.putExtra("index", widgets.indexOf(widget))
                startActivityForResult(intent, MConstants.REQUEST_NEW_NOTE)
            }
            MConstants.listW -> {
                val listWidget = widget.getObject() as ListWidget
                val intent2 = Intent(context, ListActivity::class.java)
                intent2.putExtra("lid", listWidget.uid)
                intent2.putExtra("head", listWidget.head)
                intent2.putExtra("index", widgets.indexOf(widget))
                intent2.putExtra("more", "1")
                startActivityForResult(intent2, MConstants.REQUEST_NEW_LIST)
            }
        }
    }

    override fun deleteWidget(widget: Widget?) {
        //show dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete ?")
        builder.setPositiveButton("Yes") { dialog, which -> //delete from list
            val x = widgets.indexOf(widget)
            widgets.remove(widget)
            activityContentAdapter.notifyItemRemoved(x)
            dbHelperWidgets.updateAllWidgetSortOrders(widgets)
            when (widget.getType()) {
                MConstants.textW -> DBHelperText(context).deleteText((widget.getObject() as SimpleTextWidget).uid)
                MConstants.listW -> DBHelperList(context).deleteList((widget.getObject() as ListWidget).uid)
                MConstants.imageW -> DBHelperImage(context).deleteImage((widget.getObject() as ImageWidget).uid)
                MConstants.audioW -> {
                }
            }
        }
        builder.setOnCancelListener { activityContentAdapter.notifyItemChanged(widgets.indexOf(widget)) }
        builder.show()
    }

    override fun changeActivtyOfWidget(type: Int, uid: String?, pos: Int) {
        val intent = Intent(context, ActivityChoser::class.java)
        intent.putExtra("action", MConstants.ACTION_MOVE_WIDGET)
        intent.putExtra("type", type)
        intent.putExtra("uid", uid)
        intent.putExtra("pos", pos)
        intent.putExtra("aid", activityData.getId())
        startActivityForResult(intent, MConstants.REQUEST_WIDGET_ACTIVITY_CHANGE)
    }

    companion object {
        var activityMovedHere = false

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
         * @return A new instance of fragment QuickNotesFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): QuickNotesFragment? {
            val fragment = QuickNotesFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}