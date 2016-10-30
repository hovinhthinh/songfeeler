package illusion.songfeeler.activity;

import illusion.songfeeler.R;
import illusion.songfeeler.adapter.SongDetailLyricsFragmentAdapter;
import illusion.songfeeler.db.IllusionSQLiteOpenHelper;
import illusion.songfeeler.entity.Song;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.CirclePageIndicator;

public class SongDetailActivity extends FragmentActivity {
	private IllusionSQLiteOpenHelper dbHelper;

	private FragmentPagerAdapter mPagerAdapter;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_detail);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dbHelper = new IllusionSQLiteOpenHelper(getBaseContext());
		long _id = getIntent().getExtras().getLong(
				IllusionSQLiteOpenHelper.CL_ID);

		Song song = dbHelper.getSong(_id);

		if (song == null) {
			throw new RuntimeException("_id not found on database: " + _id);
		}

		CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indSongDetail);
		mViewPager = (ViewPager) findViewById(R.id.songDetailLyricsPager);
		mPagerAdapter = new SongDetailLyricsFragmentAdapter(
				getSupportFragmentManager(), mViewPager, song);

		mViewPager.setAdapter(mPagerAdapter);

		indicator.setViewPager(mViewPager);
	}

}
