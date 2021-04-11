package com.pkmnapps.activitydo

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flipboard.bottomsheet.BottomSheetLayout
import com.flipboard.bottomsheet.commons.ImagePickerSheetView
import com.flipboard.bottomsheet.commons.MenuSheetView
import com.google.firebase.analytics.FirebaseAnalytics
import com.pkmnapps.activitydo.TaskActivity
import com.pkmnapps.activitydo.adapters.ActivityContentAdapter
import com.pkmnapps.activitydo.custominterfaces.TaskActivityInterface
import com.pkmnapps.activitydo.databasehelpers.DBHelperImage
import com.pkmnapps.activitydo.databasehelpers.DBHelperList
import com.pkmnapps.activitydo.databasehelpers.DBHelperText
import com.pkmnapps.activitydo.databasehelpers.DBHelperWidgets
import com.pkmnapps.activitydo.dataclasses.*
import org.junit.runner.RunWith
import java.io.File
import java.util.*

class TaskActivity : AppCompatActivity(), TaskActivityInterface {
    var activityData: ActivityData? = null
    var bottomSheetLayout: BottomSheetLayout? = null
    var currentSheetView: View? = null
    var recyclerView: RecyclerView? = null
    var activityContentAdapter: ActivityContentAdapter? = null
    var widgets: MutableList<Widget?>? = null
    var cameraImageUri: Uri? = null
    var tempUid: String? = null
    var dbHelperWidgets: DBHelperWidgets? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.getBundleExtra("activityData")
        activityData = ActivityData(bundle.getString("id"), bundle.getString("name"), bundle.getString("color"), bundle.getBoolean("pinned"), 0)
        setTheme(colorTheme(activityData.getColor()))
        setContentView(R.layout.activity_task)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        toolbar.title = activityData.getName()
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar).setDisplayHomeAsUpEnabled(true)
        dbHelperWidgets = DBHelperWidgets(this@TaskActivity)
        bottomSheetLayout = findViewById(R.id.bottomsheet)
        setUpJumpControls()
        setUpRecyclerView()
        initialiseRecyclerViewData()
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_task_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                // app icon in action bar clicked; goto parent activity.
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (bottomSheetLayout.isSheetShowing()) {
            bottomSheetLayout.dismissSheet()
        } else super.onBackPressed()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.w("n", requestCode.toString())
        Log.w("n", resultCode.toString())
        if (resultCode == RESULT_OK) {
            val selectedImage: Uri?
            if (requestCode == MConstants.REQUEST_LOAD_IMAGE && data != null) { //image from gallery
                selectedImage = data.data
                if (selectedImage != null) {
                    addImageWidget(selectedImage)
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
                val dbHelperText = DBHelperText(this@TaskActivity)

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
                val dbHelperList = DBHelperList(this@TaskActivity)
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
        val takeNote = findViewById<TextView?>(R.id.note_tvbutton)
        takeNote.setOnClickListener { createNote() }
        val more = findViewById<ImageButton?>(R.id.more_button)
        more.setOnClickListener { displayWidgetChoser() }
        val list = findViewById<ImageButton?>(R.id.list_button)
        list.setOnClickListener { createList() }
        val image = findViewById<ImageButton?>(R.id.image_button)
        image.setOnClickListener {
            if (checkNeedsPermission()) {
                requestStoragePermission()
            } else {
                createImageSheet()
            }
        }
    }

    fun displayWidgetChoser() {
        //show bottomsheet
        val menuSheetView = MenuSheetView(this@TaskActivity, MenuSheetView.MenuType.LIST, "Chose widget", MenuSheetView.OnMenuItemClickListener { item ->
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

    fun initialiseRecyclerViewData() {
        val dbHelperWidgets = DBHelperWidgets(this@TaskActivity)

        //add all simpleTextWidgets
        val dbHelperText = DBHelperText(this@TaskActivity)
        for (o in dbHelperText.getAllTextsAsList(activityData.getId())) {
            if (o != null) {
                widgets.add(Widget(MConstants.textW, o, o.uid, dbHelperWidgets.getSortValue(o.uid)))
            }
        }
        //add all lists
        val dbHelperList = DBHelperList(this@TaskActivity)
        for (o in dbHelperList.getAllListAsList(activityData.getId())) {
            if (o != null) {
                widgets.add(Widget(MConstants.listW, o, o.uid, dbHelperWidgets.getSortValue(o.uid)))
            }
        }

        //add all image widgets
        val dbHelperImage = DBHelperImage(this@TaskActivity)
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
                }
            }
            if (!swap) break
        }
    }

    fun setUpRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this@TaskActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView.setLayoutManager(mLayoutManager)
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setNestedScrollingEnabled(false)
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
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
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
        activityContentAdapter = ActivityContentAdapter(widgets, this@TaskActivity)
        recyclerView.setAdapter(activityContentAdapter)
    }

    fun colorTheme(colorName: String?): Int {
        return when (colorName) {
            "#3f79b4" -> R.style.AppTheme_NoActionBar_blue
            "#50933c" -> R.style.AppTheme_NoActionBar_green
            "#afaf41" -> R.style.AppTheme_NoActionBar_yellow
            "#af4541" -> R.style.AppTheme_NoActionBar_red
            "#673a9e" -> R.style.AppTheme_NoActionBar_purple
            else -> R.style.AppTheme_NoActionBar
        }
    }

    fun createNote() {
        val intent = Intent(this@TaskActivity, NoteActivity::class.java)
        val uid = System.currentTimeMillis().toString()
        intent.putExtra("uid", uid)
        DBHelperText(this@TaskActivity).insertText(uid, activityData.getId(), "", "")
        startActivityForResult(intent, MConstants.REQUEST_NEW_NOTE)
    }

    fun createList() {
        val intent = Intent(this@TaskActivity, ListActivity::class.java)
        val lid = System.currentTimeMillis().toString()
        intent.putExtra("lid", lid)
        intent.putExtra("aid", activityData.getId())
        //create a list in database
        DBHelperList(this@TaskActivity).insertList(lid, activityData.getId(), "")
        startActivityForResult(intent, MConstants.REQUEST_NEW_LIST)
    }

    fun createImageSheet() {
        val sheetView = ImagePickerSheetView.Builder(this)
                .setMaxItems(30)
                .setShowCameraOption(createCameraIntent() != null)
                .setShowPickerOption(createPickIntent() != null)
                .setImageProvider { imageView, imageUri, size -> //feeds image to sheet views
                    Glide.with(this@TaskActivity)
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
                        if (selectedTile.imageUri != null) addImageWidget(selectedTile.imageUri)
                    } else {
                        genericError()
                    }
                }
                .setTitle("Choose an image...")
                .create()
        bottomSheetLayout.showWithSheetView(sheetView)
    }

    private fun checkNeedsPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this@TaskActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, arrayOf<String?>(Manifest.permission.WRITE_EXTERNAL_STORAGE), MConstants.REQUEST_STORAGE)
        } else {
            // Eh, prompt anyway
            ActivityCompat.requestPermissions(this, arrayOf<String?>(Manifest.permission.WRITE_EXTERNAL_STORAGE), MConstants.REQUEST_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == MConstants.REQUEST_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createImageSheet()
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied :/", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun addImageWidget(selectedImageUri: Uri?) {
        //save image uri to image database
        val imageWidget = ImageWidget(tempUid, activityData.getId(), selectedImageUri.toString())
        val dbHelperImage = DBHelperImage(this@TaskActivity)
        dbHelperImage.insertImage(imageWidget)

        //show in widgets
        widgets.add(0, Widget(MConstants.imageW, imageWidget, tempUid, 0))
        //update UI
        dbHelperWidgets.updateAllWidgetSortOrders(widgets)
        activityContentAdapter.notifyDataSetChanged()
    }

    private fun createPickIntent(): Intent? {
        val picImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        return if (picImageIntent.resolveActivity(packageManager) != null) {
            picImageIntent
        } else {
            null
        }
    }

    private fun createCameraIntent(): Intent? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return if (takePictureIntent.resolveActivity(packageManager) != null) {
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
                cameraImageUri = FileProvider.getUriForFile(this@TaskActivity, "com.pkmnapps.activitydo.fileprovider", imageFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                startActivityForResult(takePictureIntent, MConstants.REQUEST_IMAGE_CAPTURE)
            } catch (e: Exception) {
                // Error occurred while creating the File
                genericError("Could not create imageFile for camera")
            }
        }
    }

    private fun genericError(message: String? = null) {
        Toast.makeText(this, message ?: "Something went wrong.", Toast.LENGTH_SHORT).show()
    }

    private fun createImageFile(): File? {
        // Create an image file name
        val imageFileName = "$tempUid.jpg"
        return File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun createAudio() {
        currentSheetView = LayoutInflater.from(this@TaskActivity).inflate(R.layout.audio_record_view, bottomSheetLayout, false)
        bottomSheetLayout.showWithSheetView(currentSheetView)
        val imageButton = currentSheetView.findViewById<ImageButton?>(R.id.recButton)
        val textView = currentSheetView.findViewById<TextView?>(R.id.recText)
    }

    override fun editWidget(widget: Widget?) {
        when (widget.getType()) {
            MConstants.textW -> {
                val simpleTextWidget = widget.getObject() as SimpleTextWidget
                val intent = Intent(this@TaskActivity, NoteActivity::class.java)
                intent.putExtra("uid", simpleTextWidget.uid)
                intent.putExtra("head", simpleTextWidget.head)
                intent.putExtra("body", simpleTextWidget.body)
                intent.putExtra("index", widgets.indexOf(widget))
                startActivityForResult(intent, MConstants.REQUEST_NEW_NOTE)
            }
            MConstants.listW -> {
                val listWidget = widget.getObject() as ListWidget
                val intent2 = Intent(this@TaskActivity, ListActivity::class.java)
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
        val builder = AlertDialog.Builder(this@TaskActivity)
        builder.setTitle("Delete ?")
        builder.setPositiveButton("Yes") { dialog, which -> //delete from list
            val x = widgets.indexOf(widget)
            widgets.remove(widget)
            activityContentAdapter.notifyItemRemoved(x)
            dbHelperWidgets.updateAllWidgetSortOrders(widgets)
            when (widget.getType()) {
                MConstants.textW -> DBHelperText(this@TaskActivity).deleteText((widget.getObject() as SimpleTextWidget).uid)
                MConstants.listW -> DBHelperList(this@TaskActivity).deleteList((widget.getObject() as ListWidget).uid)
                MConstants.imageW -> DBHelperImage(this@TaskActivity).deleteImage((widget.getObject() as ImageWidget).uid)
                MConstants.audioW -> {
                }
            }
        }
        builder.setOnCancelListener { activityContentAdapter.notifyItemChanged(widgets.indexOf(widget)) }
        builder.show()
    }

    override fun changeActivtyOfWidget(type: Int, uid: String?, pos: Int) {
        val intent = Intent(this, ActivityChoser::class.java)
        intent.putExtra("action", MConstants.ACTION_MOVE_WIDGET)
        intent.putExtra("type", type)
        intent.putExtra("uid", uid)
        intent.putExtra("pos", pos)
        intent.putExtra("aid", activityData.getId())
        startActivityForResult(intent, MConstants.REQUEST_WIDGET_ACTIVITY_CHANGE)
    }
}