package com.pkmnapps.activitydo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.ImagePickerSheetView;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.pkmnapps.activitydo.adapters.ActivityContentAdapter;
import com.pkmnapps.activitydo.custominterfaces.TaskActivityInterface;
import com.pkmnapps.activitydo.databasehelpers.DBHelper;
import com.pkmnapps.activitydo.databasehelpers.DBHelperImage;
import com.pkmnapps.activitydo.databasehelpers.DBHelperList;
import com.pkmnapps.activitydo.databasehelpers.DBHelperListItems;
import com.pkmnapps.activitydo.databasehelpers.DBHelperText;
import com.pkmnapps.activitydo.databasehelpers.DBHelperWidgets;
import com.pkmnapps.activitydo.dataclasses.ActivityData;
import com.pkmnapps.activitydo.dataclasses.ImageWidget;
import com.pkmnapps.activitydo.dataclasses.ListWidget;
import com.pkmnapps.activitydo.dataclasses.SimpleTextWidget;
import com.pkmnapps.activitydo.dataclasses.Widget;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pkmnapps.activitydo.MConstants.REQUEST_LOAD_IMAGE;
import static com.pkmnapps.activitydo.MConstants.REQUEST_STORAGE;

public class TaskActivity extends AppCompatActivity implements TaskActivityInterface{
    public ActivityData activityData;
    BottomSheetLayout bottomSheetLayout;
    View currentSheetView;
    RecyclerView recyclerView;
    ActivityContentAdapter activityContentAdapter;
    List<Widget> widgets;
    Uri cameraImageUri = null;
    ImageView selectedImage;
    String tempUid;
    DBHelperWidgets dbHelperWidgets;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getBundleExtra("activityData");
        activityData = new ActivityData(bundle.getString("id"),bundle.getString("name"),bundle.getString("color"),bundle.getBoolean("pinned"),0);

        setTheme(colorTheme(activityData.getColor()));

        setContentView(R.layout.activity_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(activityData.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelperWidgets = new DBHelperWidgets(TaskActivity.this);

        bottomSheetLayout = (BottomSheetLayout)findViewById(R.id.bottomsheet);
        selectedImage =(ImageView)findViewById(R.id.imageView);
        setUpRecyclerView();
        initialiseRecyclerViewData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.action_add_widget:
                displayWidgetChoser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(bottomSheetLayout.isSheetShowing()){
            bottomSheetLayout.dismissSheet();
        }
        else
            super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("n",String.valueOf(requestCode));
        Log.w("n",String.valueOf(resultCode));
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = null;
            if (requestCode == REQUEST_LOAD_IMAGE && data != null) { //image from gallery
                selectedImage = data.getData();
                if (selectedImage != null) {
                    addImageWidget(selectedImage);
                } else {
                    genericError();
                }
            } else if (requestCode == MConstants.REQUEST_IMAGE_CAPTURE) {//image from camera
                // Do something with imagePath
                selectedImage = cameraImageUri;
                if (selectedImage != null) {
                    addImageWidget(selectedImage);
                } else {
                    genericError();
                }
            } else if(requestCode == MConstants.REQUEST_NEW_NOTE && data != null){

                //save note
                String uid = data.getStringExtra("uid");
                int index = data.getIntExtra("index",-1000);
                DBHelperText dbHelperText = new DBHelperText(TaskActivity.this);

                //update note
                    SimpleTextWidget s;
                    if(index==-1000) {//for new note
                        s = dbHelperText.getTextWidget(uid);
                        if(s!=null)
                            widgets.add(0,new Widget(MConstants.textW,s,uid,0));
                    }
                    else{//for edited note
                        s = dbHelperText.getTextWidget(uid);
                        if(s!=null) {
                            widgets.set(index, new Widget(MConstants.textW, s,uid,index));//replacing widget
                        }else {
                            widgets.remove(index);//delete as it doesnt exist
                        }
                    }
                    //update UI
                dbHelperWidgets.updateAllWidgetSortOrders(widgets);
                activityContentAdapter.notifyDataSetChanged();

            } else if(requestCode == MConstants.REQUEST_NEW_LIST && data != null){
                DBHelperList dbHelperList = new DBHelperList(TaskActivity.this);
                String lid = data.getStringExtra("lid");
                ListWidget listWidget = dbHelperList.getListWidget(lid);

                int index = data.getIntExtra("index",-1000);
                if(index==-1000) {//new list
                    widgets.add(0,new Widget(MConstants.listW, listWidget,lid,0));
                }
                else {//edit list
                   widgets.set(index, new Widget(MConstants.listW, listWidget,lid,index));
                }
                //update ui
                dbHelperWidgets.updateAllWidgetSortOrders(widgets);
                activityContentAdapter.notifyDataSetChanged();
            }
        }
    }

    public void displayWidgetChoser(){
        //show bottomsheet
        MenuSheetView menuSheetView =
                new MenuSheetView(TaskActivity.this, MenuSheetView.MenuType.LIST, "Chose widget", new MenuSheetView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (bottomSheetLayout.isSheetShowing()) {
                            bottomSheetLayout.dismissSheet();
                        }
                        switch (item.getItemId()){
                            case R.id.item_note:
                                createNote();
                                return true;
                            case R.id.item_list:
                                createList();
                                return true;
                            case R.id.item_image:
                                if (checkNeedsPermission()) {
                                    requestStoragePermission();
                                } else {
                                    createImageSheet();
                                }
                                return true;
                        }

                        return true;
                    }
                });
        menuSheetView.inflateMenu(R.menu.widget_menu);
        bottomSheetLayout.showWithSheetView(menuSheetView);
    }

    public void initialiseRecyclerViewData(){

        DBHelperWidgets dbHelperWidgets = new DBHelperWidgets(TaskActivity.this);

        //add all simpleTextWidgets
        DBHelperText dbHelperText = new DBHelperText(TaskActivity.this);
        for (SimpleTextWidget o :dbHelperText.getAllTextsAsList(activityData.getId())) {
            if(o!=null) {
                widgets.add(new Widget(MConstants.textW,o,o.getUid(),dbHelperWidgets.getSortValue(o.getUid())));
            }
        }
        //add all lists
        DBHelperList dbHelperList = new DBHelperList(TaskActivity.this);
        for(ListWidget o:dbHelperList.getAllListAsList(activityData.getId())){
            if(o!=null) {
                widgets.add(new Widget(MConstants.listW, o,o.getUid(),dbHelperWidgets.getSortValue(o.getUid())));
            }
        }

        //add all image widgets
        DBHelperImage dbHelperImage = new DBHelperImage(TaskActivity.this);
        for(ImageWidget o:dbHelperImage.getAllImagesAsList(activityData.getId())){
            if(o!=null)
                widgets.add(new Widget(MConstants.imageW,o,o.getUid(),dbHelperWidgets.getSortValue(o.getUid())));
        }
        sortWidgetList();
        //sort the list
        activityContentAdapter.notifyDataSetChanged();
    }
    public void sortWidgetList(){
        Boolean swap;
        for(int i = 0;i<widgets.size()-1;i++){
            swap = false;
            for(int j =0;j<widgets.size()-1;j++){
                if(widgets.get(j).getSortOrder()>widgets.get(j+1).getSortOrder()){
                    Widget temp = widgets.get(j);
                    widgets.set(j,widgets.get(j+1));
                    widgets.set(j+1,temp);
                    swap = true;
                }
            }
            if(!swap)
                break;
        }
    }
    public void setUpRecyclerView(){
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(TaskActivity.this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        ItemTouchHelper.Callback itcallback = new ItemTouchHelper.Callback() {//for sorting
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them
                Collections.swap(widgets, viewHolder.getAdapterPosition(), target.getAdapterPosition());
//                widgets.get(viewHolder.getAdapterPosition()).setSortOrder(target.getAdapterPosition());
//                widgets.get(target.getAdapterPosition()).setSortOrder(viewHolder.getAdapterPosition());
                //s
                // and notify the adapter that its dataset has changed
                activityContentAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                dbHelperWidgets.updateAllWidgetSortOrders(widgets);
            }
        };
        ItemTouchHelper.Callback itcallbackswipe = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);

            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                deleteWidget(widgets.get(viewHolder.getAdapterPosition()));
            }
        };
        ItemTouchHelper itemTouchHelperSwipe = new ItemTouchHelper(itcallbackswipe);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itcallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        itemTouchHelperSwipe.attachToRecyclerView(recyclerView);

        widgets = new ArrayList<>();
        activityContentAdapter = new ActivityContentAdapter(widgets,TaskActivity.this);

        recyclerView.setAdapter(activityContentAdapter);
    }

    public int colorTheme(String colorName){
        switch (colorName){
            case "#3f79b4":
                return R.style.AppTheme_NoActionBar_blue;
            case "#50933c":
                return R.style.AppTheme_NoActionBar_green;
            case "#afaf41":
                return R.style.AppTheme_NoActionBar_yellow;
            case "#af4541":
                return R.style.AppTheme_NoActionBar_red;
            case "#673a9e":
                return R.style.AppTheme_NoActionBar_purple;

            default:
            return R.style.AppTheme_NoActionBar;

        }
    }

    public void createNote() {
          Intent intent = new Intent(TaskActivity.this,NoteActivity.class);
          String uid = String.valueOf(System.currentTimeMillis());
          intent.putExtra("uid",uid);
          new DBHelperText(TaskActivity.this).insertText(uid,activityData.getId(),"","");
          startActivityForResult(intent,MConstants.REQUEST_NEW_NOTE);
    }
    public void createList() {
        Intent intent = new Intent(TaskActivity.this,ListActivity.class);
        String lid = String.valueOf(System.currentTimeMillis());
        intent.putExtra("lid",lid);
        intent.putExtra("aid",activityData.getId());
        //create a list in database
        new DBHelperList(TaskActivity.this).insertList(lid,activityData.getId(),"");
        startActivityForResult(intent,MConstants.REQUEST_NEW_LIST);
    }
    public void createImageSheet() {
        ImagePickerSheetView sheetView = new ImagePickerSheetView.Builder(this)
                .setMaxItems(30)
                .setShowCameraOption(createCameraIntent() != null)
                .setShowPickerOption(createPickIntent() != null)
                .setImageProvider(new ImagePickerSheetView.ImageProvider() {
                    @Override
                    public void onProvideImage(ImageView imageView, Uri imageUri, int size) {//feeds image to sheet views
                        Glide.with(TaskActivity.this)
                                .load(imageUri)
                                .into(imageView);
                    }
                })
                .setOnTileSelectedListener(new ImagePickerSheetView.OnTileSelectedListener() {
                    @Override
                    public void onTileSelected(ImagePickerSheetView.ImagePickerTile selectedTile) {
                        bottomSheetLayout.dismissSheet();
                        tempUid = String.valueOf(System.currentTimeMillis());//do not remove this
                        if (selectedTile.isCameraTile()) {
                            dispatchTakePictureIntent();
                        } else if (selectedTile.isPickerTile()) {
                            startActivityForResult(createPickIntent(), REQUEST_LOAD_IMAGE);
                        } else if (selectedTile.isImageTile()) {
                            if(selectedTile.getImageUri()!=null)
                                addImageWidget(selectedTile.getImageUri());
                        } else {
                            genericError();
                        }
                    }
                })
                .setTitle("Choose an image...")
                .create();

        bottomSheetLayout.showWithSheetView(sheetView);
    }

    private boolean checkNeedsPermission() {
        return ActivityCompat.checkSelfPermission(TaskActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        } else {
            // Eh, prompt anyway
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        }
    }
    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createImageSheet();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied :/", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void addImageWidget(Uri selectedImageUri) {
        //save image uri to image database
        ImageWidget imageWidget = new ImageWidget(tempUid,activityData.getId(),selectedImageUri.toString());
        DBHelperImage dbHelperImage = new DBHelperImage(TaskActivity.this);
        dbHelperImage.insertImage(imageWidget);

        //show in widgets
        widgets.add(0,new Widget(MConstants.imageW,imageWidget,tempUid,0));
        //update UI
        dbHelperWidgets.updateAllWidgetSortOrders(widgets);
        activityContentAdapter.notifyDataSetChanged();
    }

    @Nullable
    private Intent createPickIntent() {
        Intent picImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (picImageIntent.resolveActivity(getPackageManager()) != null) {
            return picImageIntent;
        } else {
            return null;
        }
    }
    @Nullable
    private Intent createCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            return takePictureIntent;
        } else {
            return null;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = createCameraIntent();
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent != null) {
            try {
                File imageFile = createImageFile();
                // Save a file: path for use with ACTION_VIEW intents
                cameraImageUri = FileProvider.getUriForFile(TaskActivity.this,"com.pkmnapps.activitydo.fileprovider",imageFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(takePictureIntent, MConstants.REQUEST_IMAGE_CAPTURE);
            } catch (Exception e) {
                // Error occurred while creating the File
                genericError("Could not create imageFile for camera");
            }
        }
    }

    private void genericError() {
        genericError(null);
    }

    private void genericError(String message) {
        Toast.makeText(this, message == null ? "Something went wrong." : message, Toast.LENGTH_SHORT).show();
    }

    private File createImageFile() {
        // Create an image file name
        String imageFileName = tempUid + ".jpg";
        return new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),imageFileName);
    }


    @SuppressLint("ClickableViewAccessibility")
    public void createAudio() {
        currentSheetView = LayoutInflater.from(TaskActivity.this).inflate(R.layout.audio_record_view,bottomSheetLayout,false);
        bottomSheetLayout.showWithSheetView(currentSheetView);
        final ImageButton imageButton = (ImageButton)currentSheetView.findViewById(R.id.recButton);
        final TextView textView = (TextView)currentSheetView.findViewById(R.id.recText);

    }


    @Override
    public void editWidget(final Widget widget) {

        switch (widget.getType()) {
            case MConstants.textW:
                final SimpleTextWidget simpleTextWidget = (SimpleTextWidget)widget.getObject();
                Intent intent = new Intent(TaskActivity.this,NoteActivity.class);
                intent.putExtra("uid",simpleTextWidget.getUid());
                intent.putExtra("head",simpleTextWidget.getHead());
                intent.putExtra("body",simpleTextWidget.getBody());
                intent.putExtra("index",widgets.indexOf(widget));
                startActivityForResult(intent,MConstants.REQUEST_NEW_NOTE);
                break;
            case MConstants.listW:
                final ListWidget listWidget = (ListWidget) widget.getObject();
                Intent intent2 = new Intent(TaskActivity.this,ListActivity.class);
                intent2.putExtra("lid",listWidget.getUid());
                intent2.putExtra("head",listWidget.getHead());
                intent2.putExtra("index",widgets.indexOf(widget));
                intent2.putExtra("edit","1");
                startActivityForResult(intent2,MConstants.REQUEST_NEW_LIST);
                break;

        }


    }

    public void deleteWidget(final Widget widget){
        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
        builder.setTitle("Delete ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //delete from list
                widgets.remove(widget);
                activityContentAdapter.notifyDataSetChanged();
                dbHelperWidgets.updateAllWidgetSortOrders(widgets);
                //delete rom database
                switch (widget.getType()){
                    case MConstants.textW:
                        new DBHelperText(TaskActivity.this).deleteText(((SimpleTextWidget)widget.getObject()).getUid());
                        break;
                    case MConstants.listW:
                        new DBHelperList(TaskActivity.this).deleteList(((ListWidget)widget.getObject()).getUid());
                        break;
                    case MConstants.imageW:
                        new DBHelperImage(TaskActivity.this).deleteImage(((ImageWidget)widget.getObject()).getUid());
                        break;
                    case MConstants.audioW:
                        break;
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                activityContentAdapter.notifyDataSetChanged();
            }
        });
        builder.show();
    }

}