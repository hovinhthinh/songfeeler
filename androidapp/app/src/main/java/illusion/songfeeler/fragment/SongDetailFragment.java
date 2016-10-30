package illusion.songfeeler.fragment;

import illusion.songfeeler.R;
import illusion.songfeeler.entity.Song;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SongDetailFragment extends Fragment {

	private Song song;
	private ViewPager viewPager;

	public void setSong(Song song) {
		this.song = song;
	}

	public void setViewPager(ViewPager viewPager) {
		this.viewPager = viewPager;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				illusion.songfeeler.R.layout.fragment_song_detail, container,
				false);
		ImageView ivAvarta = (ImageView) rootView
				.findViewById(R.id.ivDetailSongAvarta);
		TextView tvTitle = (TextView) rootView
				.findViewById(R.id.tvDetailSongTitle);
		TextView tvArtist = (TextView) rootView
				.findViewById(R.id.tvDetailSongArtist);
		TextView tvAuthor = (TextView) rootView
				.findViewById(R.id.tvDetailSongAuthor);

		Button linkBth = (Button) rootView.findViewById(R.id.btnLink);
		linkBth.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String url = song.getUrl();
				if (url.isEmpty()) {
					Toast.makeText(getActivity(), R.string.no_link_en,
							Toast.LENGTH_LONG).show();
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(song.getUrl()));
					startActivity(intent);
				}
			}
		});

		Button lyricsBtn = (Button) rootView.findViewById(R.id.btnShowLyrics);
		lyricsBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				viewPager.setCurrentItem(1);
			}
		});

		Button shareBtn = (Button) rootView.findViewById(R.id.btnShare);
		shareBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, song.getUrl());
				startActivity(Intent.createChooser(shareIntent, getResources()
						.getString(R.string.share_title_en)));
			}
		});
		if (song.getImage().length > 0) {
			ivAvarta.setImageBitmap(BitmapFactory.decodeByteArray(
					song.getImage(), 0, song.getImage().length));
		} else {
			ivAvarta.setImageBitmap(BitmapFactory.decodeResource(getActivity()
					.getResources(), R.drawable.img_no_avarta));
		}

		tvTitle.setText(song.getTitle());
		tvArtist.setText(song.getArtist());
		tvAuthor.setText("Author: " + song.getAuthor());

		return rootView;
	}
}
