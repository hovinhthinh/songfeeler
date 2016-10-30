package illusion.songfeeler.adapter;

import illusion.songfeeler.activity.MainActivity;
import illusion.songfeeler.fragment.HistoryFragment;
import illusion.songfeeler.fragment.HomeFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class HomeHistoryFragmentAdapter extends FragmentPagerAdapter {

	private HomeFragment homeFragment;
	private HistoryFragment historyFragment;

	public HomeHistoryFragmentAdapter(FragmentManager fm,
			MainActivity mainActivity) {
		super(fm);
		homeFragment = new HomeFragment();
		historyFragment = new HistoryFragment();
		mainActivity.homeFragment = homeFragment;
		mainActivity.historyFragment = historyFragment;
	}

	@Override
	public Fragment getItem(int fragmentId) {
		// TODO Auto-generated method stub
		if (fragmentId == 0) {
			return homeFragment;
		} else if (fragmentId == 1) {
			return historyFragment;
		} else {
			return null;
		}
	}

	@Override
	public int getCount() {
		// Show 2 total pages.
		return 2;
	}
}
