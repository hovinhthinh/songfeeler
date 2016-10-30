package illusion.songfeeler.adapter;

import illusion.songfeeler.entity.Song;
import illusion.songfeeler.fragment.SongDetailFragment;
import illusion.songfeeler.fragment.SongLyricsFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class SongDetailLyricsFragmentAdapter extends FragmentPagerAdapter {

	private Song song;
	private ViewPager viewPager;

	public SongDetailLyricsFragmentAdapter(FragmentManager fm,
			ViewPager viewPager, Song song) {
		super(fm);
		this.song = song;
		this.viewPager = viewPager;
	}

	@Override
	public Fragment getItem(int fragmentId) {
		// TODO Auto-generated method stub
		if (fragmentId == 0) {
			SongDetailFragment songDetailFragment = new SongDetailFragment();
			songDetailFragment.setSong(song);
			songDetailFragment.setViewPager(viewPager);
			return songDetailFragment;
		} else if (fragmentId == 1) {
			SongLyricsFragment songLyricsFragment = new SongLyricsFragment();
			songLyricsFragment.setSong(song);
			return songLyricsFragment;

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
