package illusion.songfeeler.fragment;

import illusion.songfeeler.R;
import illusion.songfeeler.adapter.HistoryAdapter;
import illusion.songfeeler.db.IllusionSQLiteOpenHelper;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryFragment extends Fragment {
	private IllusionSQLiteOpenHelper dbHelper;
	private ListView lvHistory;
	private TextView tvNoHistory;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater
				.inflate(illusion.songfeeler.R.layout.fragment_history,
						container, false);
		lvHistory = (ListView) rootView
				.findViewById(illusion.songfeeler.R.id.lvHistory);
		tvNoHistory = (TextView) rootView
				.findViewById(illusion.songfeeler.R.id.tvNoHistory);
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		dbHelper = new IllusionSQLiteOpenHelper(activity);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		/* Get list song */
		Cursor cursor = dbHelper.getListSongs();
		if (cursor.getCount() == 0) {
			tvNoHistory.setText(R.string.no_history_en);
		} else {
			tvNoHistory.setText("");
		}
		HistoryAdapter historyAdapter = new HistoryAdapter(getActivity(),
				dbHelper.getListSongs(), false);
		lvHistory.setAdapter(historyAdapter);
	}
}
