package com.pkmnapps.activitydo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.pkmnapps.activitydo.databasehelpers.DBHelperImage;
import com.pkmnapps.activitydo.databasehelpers.DBHelperList;
import com.pkmnapps.activitydo.databasehelpers.DBHelperText;
import com.pkmnapps.activitydo.databasehelpers.DBHelperWidgets;
import com.pkmnapps.activitydo.dataclasses.ActivityData;
import com.pkmnapps.activitydo.dataclasses.ImageWidget;
import com.pkmnapps.activitydo.dataclasses.ListWidget;
import com.pkmnapps.activitydo.dataclasses.SimpleTextWidget;
import com.pkmnapps.activitydo.dataclasses.Widget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pkmnapps.activitydo.MConstants.REQUEST_LOAD_IMAGE;
import static com.pkmnapps.activitydo.MConstants.REQUEST_STORAGE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link QuickNotesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link QuickNotesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuickNotesFragment extends Fragment implements TaskActivityInterface{
    static boolean activityMovedHere = false;
    public ActivityData activityData;
    BottomSheetLayout bottomSheetLayout;
    View currentSheetView;
    RecyclerView recyclerView;
    ActivityContentAdapter activityContentAdapter;
    List<Widget> widgets;
    Uri cameraImageUri = null;
    String tempUid;
    DBHelperWidgets dbHelperWidgets;

    View view;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public QuickNotesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QuickNotesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QuickNotesFragment newInstance(String param1, String param2) {
        QuickNotesFragment fragment = new QuickNotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_quick_notes, container, false);//same layout for both

        activityData = new ActivityData("0","Quick Notes","#ffffff",false,0);//only id is useful here


        dbHelperWidgets = new DBHelperWidgets(getContext());

        bottomSheetLayout = (BottomSheetLayout)view.findViewById(R.id.bottomsheet);
        setUpJumpControls();
        setUpRecyclerView();
        initialiseRecyclerViewData();


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        if(activityMovedHere){//used when item is moved to quicknotes from any activity
            widgets.clear();
            initialiseRecyclerViewData();
            activityMovedHere = false;
        }
        super.onStart();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
                    saveImageAddWidget(selectedImage);
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
                DBHelperText dbHelperText = new DBHelperText(getContext());

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
                DBHelperList dbHelperList = new DBHelperList(getContext());
                String lid = data.getStringExtra("lid");
                ListWidget listWidget = dbHelperList.getListWidget(lid);

                int index = data.getIntExtra("index",-1000);
                if(index==-1000) {//new list
                    if(listWidget!=null)
                        widgets.add(0,new Widget(MConstants.listW, listWidget,lid,0));
                }
                else {//more list
                    if(listWidget!=null)
                        widgets.set(index, new Widget(MConstants.listW, listWidget,lid,index));
                    else {
                        widgets.remove(index);
                    }
                }
                //update ui
                dbHelperWidgets.updateAllWidgetSortOrders(widgets);
                activityContentAdapter.notifyDataSetChanged();
            }
            else if(requestCode == MConstants.REQUEST_WIDGET_ACTIVITY_CHANGE && data != null){
                int pos = data.getIntExtra("pos",-1);
                if(pos!=-1) {
                    widgets.remove(pos);
                    activityContentAdapter.notifyItemRemoved(pos);
                }
            }
        }
    }


    private void setUpJumpControls(){
        TextView takeNote = (TextView)view.findViewById(R.id.note_tvbutton);
        takeNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNote();
            }
        });
        ImageButton more = (ImageButton)view.findViewById(R.id.more_button);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayWidgetChoser();
            }
        });
        ImageButton list = (ImageButton)view.findViewById(R.id.list_button);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createList();
            }
        });
        ImageButton image = (ImageButton)view.findViewById(R.id.image_button);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNeedsPermission()) {
                    requestStoragePermission();
                } else {
                    createImageSheet();
                }
            }
        });
    }
    public void setUpRecyclerView(){
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(true);
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
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,ItemTouchHelper.RIGHT);

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
        activityContentAdapter = new ActivityContentAdapter(widgets,QuickNotesFragment.this);

        recyclerView.setAdapter(activityContentAdapter);
    }

    public void initialiseRecyclerViewData(){

        DBHelperWidgets dbHelperWidgets = new DBHelperWidgets(getContext());

        //add all simpleTextWidgets
        DBHelperText dbHelperText = new DBHelperText(getContext());
        for (SimpleTextWidget o :dbHelperText.getAllTextsAsList(activityData.getId())) {
            if(o!=null) {
                widgets.add(new Widget(MConstants.textW,o,o.getUid(),dbHelperWidgets.getSortValue(o.getUid())));
            }
        }
        //add all lists
        DBHelperList dbHelperList = new DBHelperList(getContext());
        for(ListWidget o:dbHelperList.getAllListAsList(activityData.getId())){
            if(o!=null) {
                widgets.add(new Widget(MConstants.listW, o,o.getUid(),dbHelperWidgets.getSortValue(o.getUid())));
            }
        }

        //add all image widgets
        DBHelperImage dbHelperImage = new DBHelperImage(getContext());
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
                }else if(widgets.get(j).getSortOrder()==widgets.get(j+1).getSortOrder()){
                    if(Long.parseLong(widgets.get(j).getUid())< Long.parseLong(widgets.get(j+1).getUid())) {
                        Widget temp = widgets.get(j);
                        widgets.set(j, widgets.get(j + 1));
                        widgets.set(j + 1, temp);
                        swap = true;
                    }
                }
            }
            if(!swap)
                break;
        }
    }
    public void displayWidgetChoser(){
        //show bottomsheet
        MenuSheetView menuSheetView =
                new MenuSheetView(getContext(), MenuSheetView.MenuType.LIST, "Chose widget", new MenuSheetView.OnMenuItemClickListener() {
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
        bottomSheetLayout.setShouldDimContentView(false);
        bottomSheetLayout.showWithSheetView(menuSheetView);
    }
    public void createNote() {
        Intent intent = new Intent(getContext(),NoteActivity.class);
        String uid = String.valueOf(System.currentTimeMillis());
        intent.putExtra("uid",uid);
        new DBHelperText(getContext()).insertText(uid,activityData.getId(),"","");
        startActivityForResult(intent,MConstants.REQUEST_NEW_NOTE);
    }
    public void createList() {
        Intent intent = new Intent(getContext(),ListActivity.class);
        String lid = String.valueOf(System.currentTimeMillis());
        intent.putExtra("lid",lid);
        intent.putExtra("aid",activityData.getId());
        //create a list in database
        new DBHelperList(getContext()).insertList(lid,activityData.getId(),"");
        startActivityForResult(intent,MConstants.REQUEST_NEW_LIST);
    }
    public void createImageSheet() {
        ImagePickerSheetView sheetView = new ImagePickerSheetView.Builder(getContext())
                .setMaxItems(30)
                .setShowCameraOption(createCameraIntent() != null)
                .setShowPickerOption(createPickIntent() != null)
                .setImageProvider(new ImagePickerSheetView.ImageProvider() {
                    @Override
                    public void onProvideImage(ImageView imageView, Uri imageUri, int size) {//feeds image to sheet views
                        Glide.with(getContext())
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
                                saveImageAddWidget(selectedTile.getImageUri());
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
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        } else {
            // Eh, prompt anyway
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        }
    }

    private void addImageWidget(Uri selectedImageUri) {
        //save image uri to image database
        ImageWidget imageWidget = new ImageWidget(tempUid,activityData.getId(),selectedImageUri.toString());
        DBHelperImage dbHelperImage = new DBHelperImage(getContext());
        dbHelperImage.insertImage(imageWidget);

        //show in widgets
        widgets.add(0,new Widget(MConstants.imageW,imageWidget,tempUid,0));
        //update UI
        dbHelperWidgets.updateAllWidgetSortOrders(widgets);
        activityContentAdapter.notifyDataSetChanged();
    }

    private void saveImageAddWidget(Uri selectedImageUri){
        if (selectedImageUri != null) {
            try {
                File file = createImageFile();
                InputStream input = getContext().getContentResolver().openInputStream(selectedImageUri);
                try (OutputStream output = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                }
                //save image uri to image database
                ImageWidget imageWidget = new ImageWidget(tempUid,activityData.getId(),Uri.fromFile(file).toString());
                DBHelperImage dbHelperImage = new DBHelperImage(getContext());
                dbHelperImage.insertImage(imageWidget);

                //show in widgets
                widgets.add(0,new Widget(MConstants.imageW,imageWidget,tempUid,0));
                //update UI
                dbHelperWidgets.updateAllWidgetSortOrders(widgets);
                activityContentAdapter.notifyDataSetChanged();
            }catch (Exception ignored){
                Toast.makeText(getContext(),"Error",Toast.LENGTH_SHORT).show();
            }
        }

    }
    private File createImageFile(String tempUid) {
        // Create an image file name
        String imageFileName = tempUid + ".jpg";
        return new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),imageFileName);
    }
    @Nullable
    private Intent createPickIntent() {
        Intent picImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (picImageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            return picImageIntent;
        } else {
            return null;
        }
    }
    @Nullable
    private Intent createCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
                cameraImageUri = FileProvider.getUriForFile(getContext(),"com.pkmnapps.activitydo.fileprovider",imageFile);

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
        Toast.makeText(getContext(), message == null ? "Something went wrong." : message, Toast.LENGTH_SHORT).show();
    }

    private File createImageFile() {
        // Create an image file name
        String imageFileName = tempUid + ".jpg";
        return new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),imageFileName);
    }

    @Override
    public void editWidget(final Widget widget) {

        switch (widget.getType()) {
            case MConstants.textW:
                final SimpleTextWidget simpleTextWidget = (SimpleTextWidget)widget.getObject();
                Intent intent = new Intent(getContext(),NoteActivity.class);
                intent.putExtra("uid",simpleTextWidget.getUid());
                intent.putExtra("head",simpleTextWidget.getHead());
                intent.putExtra("body",simpleTextWidget.getBody());
                intent.putExtra("index",widgets.indexOf(widget));
                startActivityForResult(intent,MConstants.REQUEST_NEW_NOTE);
                break;
            case MConstants.listW:
                final ListWidget listWidget = (ListWidget) widget.getObject();
                Intent intent2 = new Intent(getContext(),ListActivity.class);
                intent2.putExtra("lid",listWidget.getUid());
                intent2.putExtra("head",listWidget.getHead());
                intent2.putExtra("index",widgets.indexOf(widget));
                intent2.putExtra("more","1");
                startActivityForResult(intent2,MConstants.REQUEST_NEW_LIST);
                break;

        }
    }

    public void deleteWidget(final Widget widget){
        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //delete from list
                int x = widgets.indexOf(widget);
                widgets.remove(widget);
                activityContentAdapter.notifyItemRemoved(x);
                dbHelperWidgets.updateAllWidgetSortOrders(widgets);
                //delete rom database
                switch (widget.getType()){
                    case MConstants.textW:
                        new DBHelperText(getContext()).deleteText(((SimpleTextWidget)widget.getObject()).getUid());
                        break;
                    case MConstants.listW:
                        new DBHelperList(getContext()).deleteList(((ListWidget)widget.getObject()).getUid());
                        break;
                    case MConstants.imageW:
                        new DBHelperImage(getContext()).deleteImage(((ImageWidget)widget.getObject()).getUid());
                        break;
                    case MConstants.audioW:
                        break;
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                activityContentAdapter.notifyItemChanged(widgets.indexOf(widget));
            }
        });
        builder.show();
    }

    @Override
    public void changeActivtyOfWidget(int type, String uid, int pos) {
        Intent intent = new Intent(getContext(),ActivityChoser.class);
        intent.putExtra("action",MConstants.ACTION_MOVE_WIDGET);
        intent.putExtra("type",type);
        intent.putExtra("uid",uid);
        intent.putExtra("pos",pos);
        intent.putExtra("aid",activityData.getId());
        startActivityForResult(intent,MConstants.REQUEST_WIDGET_ACTIVITY_CHANGE);
    }
}
