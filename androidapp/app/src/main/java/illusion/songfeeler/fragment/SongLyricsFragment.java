package illusion.songfeeler.fragment;

import illusion.songfeeler.R;
import illusion.songfeeler.entity.Song;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SongLyricsFragment extends Fragment {
	private Song song;

	public void setSong(Song song) {
		this.song = song;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				illusion.songfeeler.R.layout.fragment_song_lyrics, container,
				false);
		ImageView ivAvarta = (ImageView) rootView
				.findViewById(R.id.ivLyricsAvarta);
		TextView tvTitle = (TextView) rootView.findViewById(R.id.tvLyricsTitle);
		TextView tvArtist = (TextView) rootView
				.findViewById(R.id.tvLyricsArtist);
		TextView tvLyrics = (TextView) rootView.findViewById(R.id.tvSongLyrics);

		if (song.getImage().length > 0) {
			ivAvarta.setImageBitmap(BitmapFactory.decodeByteArray(
					song.getImage(), 0, song.getImage().length));
		} else {
			ivAvarta.setImageBitmap(BitmapFactory.decodeResource(getActivity()
					.getResources(), R.drawable.img_no_avarta));
		}

		tvTitle.setText(song.getTitle());
		tvArtist.setText(song.getArtist());
		String lyrics = song.getLyrics().replace('|', '\n').trim();
		if (lyrics.isEmpty()) {
			tvLyrics.setText("\n"
					+ getResources().getString(R.string.no_lyrics_en));
		} else {
			tvLyrics.setText("\n" + lyrics + "\n");
		}
		return rootView;
	}

}
