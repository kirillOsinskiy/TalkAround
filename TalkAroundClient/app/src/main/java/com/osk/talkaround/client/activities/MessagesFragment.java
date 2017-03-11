package com.osk.talkaround.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.osk.talkaroundclient.R;
import com.osk.talkaround.client.adapters.TalkListArrayAdapter;
import com.osk.talkaround.model.Talk;

import static com.osk.talkaround.client.activities.MainActivity.TALK_ID_PARAM;


public class MessagesFragment extends UpdatableFragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private SwipeRefreshLayout swipeLayout;
    private ListView talksListView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private boolean isViewCreated;
    private Talk[] talks;
    //private OnFragmentInteractionListener mListener;

    public MessagesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessagesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessagesFragment newInstance(String param1, String param2) {
        MessagesFragment fragment = new MessagesFragment();
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
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        talksListView = (ListView) view.findViewById(R.id.talkList);
        // ListView Item Click Listener
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        talksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Intent intent = new Intent(view.getContext(), DisplayTalkActivity.class);
                Talk itemValue = (Talk) talksListView.getItemAtPosition(position);
                intent.putExtra(TALK_ID_PARAM, String.valueOf(itemValue.getId()));
                startActivity(intent);
            }

        });
        isViewCreated = true;
        if (talks != null)
            updateTalks(talks);
    }

    @Override
    public void updateTalks(Talk[] talks) {
        if (swipeLayout != null && swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(false);
        }
        if (isViewCreated)
            fillTalksList(talks);
        else
            this.talks = talks;
    }

    public void fillTalksList(Talk[] talks) {
        TalkListArrayAdapter adapter = new TalkListArrayAdapter(getContext(), talks);
        talksListView.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
        ((MainActivity) getActivity()).refreshData();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (swipeLayout != null && swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                    Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_LONG).show();
                }
            }
        }, 5000);
    }




    /* // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

  /*  @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
*/
/*    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    *//**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     *//*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
