package com.pkmnapps.activitydo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import com.pkmnapps.activitydo.adapters.ActivityAdapter;
import com.pkmnapps.activitydo.adapters.ColorThemeAdapter;
import com.pkmnapps.activitydo.custominterfaces.HomeFragInterace;
import com.pkmnapps.activitydo.databasehelpers.DBHelper;
import com.pkmnapps.activitydo.dataclasses.ActivityData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class HomeFragment extends Fragment implements HomeFragInterace{
    View view;
    List<ActivityData> activityDataList;
    ActivityAdapter activityAdapter;
    RecyclerView recyclerView;
    DBHelper dbHelper;
    View dialogView;
    String colorTheme = MConstants.colors[0];

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        Objects.requireNonNull(getActivity()).setTitle("Home");

        activityDataList = new ArrayList<>();
        activityAdapter = new ActivityAdapter(activityDataList,HomeFragment.this);

        recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        dbHelper = new DBHelper(getContext());
        ItemTouchHelper.Callback itcallback = new ItemTouchHelper.Callback() {//for sorting
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them
                Collections.swap(activityDataList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
//                activityDataList.get(viewHolder.getAdapterPosition()).setSortOrder(target.getAdapterPosition());
//                activityDataList.get(target.getAdapterPosition()).setSortOrder(viewHolder.getAdapterPosition());
                //s
                // and notify the adapter that its dataset has changed
                activityAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                dbHelper.updateAllSortOrders(activityDataList);
            }
        };
        ItemTouchHelper.Callback itcallbackswipe = new ItemTouchHelper.Callback() {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,ItemTouchHelper.LEFT);

            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                displayDeleteDialog(activityDataList.get(viewHolder.getAdapterPosition()));
            }

        };
        ItemTouchHelper itemTouchHelperSwipe = new ItemTouchHelper(itcallbackswipe);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itcallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        itemTouchHelperSwipe.attachToRecyclerView(recyclerView);
        Button addNewButton = view.findViewById(R.id.addNewButton);
        addNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //open a popup for adding new
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = LayoutInflater.from(getContext());
                dialogView = inflater.inflate(R.layout.new_activity_alertbox,null);
                builder.setView(dialogView);

                changeColorTheme(new Random().nextInt(MConstants.colors.length));//gives random number from 0 to max(excluding)

                final RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setNestedScrollingEnabled(false);

                recyclerView.setAdapter(new ColorThemeAdapter(HomeFragment.this));

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id,name,color;
                        //add to list
                        EditText nameEdiText = dialogView.findViewById(R.id.nameEditText);

                        id = String.valueOf(System.currentTimeMillis());
                        name = nameEdiText.getText().toString();
                        color = colorTheme;
                        if(!name.equals("")) {
                            ActivityData a = new ActivityData(id, name, color, false,0);
                            activityDataList.add(0,a);
                            //add to database
                            dbHelper.insertActivity(a);
                            //update UI
                            activityAdapter.notifyDataSetChanged();
                        }
                    }
                });
                AlertDialog dialog =  builder.create();
                dialog.show();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //clear-up previous data if any...
        activityDataList.clear();
        //add from database here
        activityDataList.addAll(dbHelper.getAllActivitiesAsList());
        //sort the list here...
        activityAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(activityAdapter);
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
    public void displayEditDialog(final ActivityData activityData) {
        //open a popup for adding new
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        dialogView = inflater.inflate(R.layout.new_activity_alertbox,null);
        builder.setView(dialogView);

        changeColorTheme(activityData.getColor());

        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.setAdapter(new ColorThemeAdapter(HomeFragment.this));

        final EditText nameEdiText = dialogView.findViewById(R.id.nameEditText);

        nameEdiText.setText(activityData.getName());

        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name,color;

                name = nameEdiText.getText().toString();
                color = colorTheme;
                if(!name.equals("")) {
                    activityData.setName(name);
                    activityData.setColor(color);
                    //add to database
                    dbHelper.updateActivity(activityData);
                    //update UI
                    activityAdapter.notifyItemChanged(activityDataList.indexOf(activityData));
                }
            }
        });
        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayDeleteDialog(activityData);
            }
        });
        builder.setNegativeButton("Cancel",null);
        AlertDialog dialog =  builder.create();
        dialog.show();
    }

    @Override
    public void displayDeleteDialog(final ActivityData activityData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete '"+activityData.getName()+"' ?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //delete from database
                new DBHelper(getContext()).deleteActivity(activityData.getId());
                //delete from list
                int x = activityDataList.indexOf(activityData);
                activityDataList.remove(activityData);
                activityAdapter.notifyItemRemoved(x);
                //also remove from pinned menu if it exists
                updatePinnedMenu();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                activityAdapter.notifyItemChanged(activityDataList.indexOf(activityData));
            }
        });
        builder.show();
    }

    @Override
    public void changeColorTheme(int pos) {
        //save to variable
        colorTheme = MConstants.colors[pos];
        //change indicator in view
        dialogView.setBackgroundColor(Color.parseColor(colorTheme));

    }

    private void changeColorTheme(String colorTheme){
        dialogView.setBackgroundColor(Color.parseColor(colorTheme));
    }

    public void updatePinnedMenu(){
        ((MainActivity) Objects.requireNonNull(getActivity())).menuAddPinned();
    }
}
