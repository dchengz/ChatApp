package com.cti.chatapp.frame;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.cti.chatapp.R;
import com.cti.chatapp.object.GCMHelper;
import com.cti.chatapp.object.Obj_GCM;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessagesFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	private OnFragmentInteractionListener mListener;
	public static MyArrayAdapter adapter;
	public static ArrayList<Obj_GCM> objarr;
	private Calendar calendar;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = new Bundle();
        args.putString("ID", mListener.getProfileEmail());
        getLoaderManager().initLoader(0, args, this);
        this.getListView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        this.getListView().setStackFromBottom(true);
        calendar = Calendar.getInstance();
		GCMHelper gcm_helper = GCMHelper.getHelper(getActivity());
		objarr = gcm_helper.consMsgs(mListener.getProfileEmail());
		adapter = new MyArrayAdapter(getActivity(), objarr);	
		setListAdapter(adapter);
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
		adapter=null;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return null;
	}
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {		
	}
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
	public interface OnFragmentInteractionListener {
        public String getProfileEmail();
    }
	public class MyArrayAdapter extends ArrayAdapter<Obj_GCM> {
		private Context context;
		private ArrayList<Obj_GCM> gcm_arr;
		
		public MyArrayAdapter(Context context, ArrayList<Obj_GCM> objarr) {
			super(context, R.layout.activity_chatusers, objarr);
			this.context = context;
			this.gcm_arr = objarr;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            Obj_GCM ob = gcm_arr.get(position);
			String msg = ob.getMessage();
			calendar.setTimeInMillis(Long.valueOf(ob.getDate()));
			SimpleDateFormat format = new SimpleDateFormat("d MMM, k:mm");			
			String fecha = format.format(calendar.getTime());
			
			if (convertView==null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.chat_list_item, parent, false);
			}
			TextView tv_name1 = (TextView) convertView.findViewById(R.id.text1);
			TextView tv_name2 = (TextView) convertView.findViewById(R.id.text2);
			tv_name1.setText(msg);
			tv_name2.setText(fecha);
//			rowView.setPadding(8, 12, 8, 12);
			convertView.setPadding(8, 12, 8, 12);
			ImageView iv_tick = (ImageView) convertView.findViewById(R.id.img_tick);
			LinearLayout root = (LinearLayout) convertView.findViewById(R.id.lin_lay);
            if (ob.getFlag()==0) {
            	iv_tick.setVisibility(View.VISIBLE);
            	root.getChildAt(0).setBackgroundResource(R.drawable.bubble_green);
                root.setGravity(Gravity.RIGHT);
                root.setPadding(50, 10, 10, 10);
                if (ob.getViewed()==0) {
                	iv_tick.setImageResource(R.drawable.ic_clock);
    			} else if (ob.getViewed()==1) {
    				iv_tick.setImageResource(R.drawable.ic_tick_sent);
    			} else if (ob.getViewed()==2) {
    				iv_tick.setImageResource(R.drawable.ic_tick_rec);
    			}
            } else if (ob.getFlag()==1) {
            	iv_tick.setVisibility(View.GONE);
            	root.getChildAt(0).setBackgroundResource(R.drawable.bubble_yellow);
                root.setGravity(Gravity.LEFT);
                root.setPadding(10, 10, 50, 10);
            }
            return convertView;
		}
		public void updateViewed(int idx, int viewed) {
			for (int i = 0; i < gcm_arr.size(); i++) {
				String idddx = String.valueOf(idx);
				if (gcm_arr.get(i).getId().equals(idddx)) {
					gcm_arr.get(i).setViewed(viewed);
					break;
				}
			}
		}
	}
}
