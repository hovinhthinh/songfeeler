package detector;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Logger;
import util.EngineConfiguration;
import util.Pair;
import util.song.Song;
import util.song.SongFingerprint;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by thinhhv on 02/08/2014.
 */
public class DetectorTable {
	private static final Logger LOG = Logger.getLogger(DetectorTable.class);
	private static final String MATCH_THRESHOLD = "detector.match-threshold";

	protected ReadWriteLock lock;

	protected ArrayList<Pair<Integer, Pair<int[], short[]>>> hashTable;
	private TIntObjectMap<Song> trackSet;

	public DetectorTable() {
		LOG.info("initializing detector hash table");
		hashTable = new ArrayList<Pair<Integer, Pair<int[], short[]>>>();
		trackSet = new TIntObjectHashMap<Song>();
		lock = new ReentrantReadWriteLock();
	}

	/* return null if unable to acquire read lock */
	protected TIntList listSong() {
		LOG.info("listing songs");
		boolean flag = lock.readLock().tryLock();
		if (!flag) {
			LOG.info("fail to acquire lock");
			return null;
		}
		try {
			TIntList list = new TIntLinkedList();
			for (TIntObjectIterator<Song> i = trackSet.iterator(); i.hasNext(); ) {
				i.advance();
				list.add(i.key());
			}
			return list;
		} finally {
			lock.readLock().unlock();
		}
	}

	protected boolean addSong(Song song) {
		LOG.info("adding song: trackId = " + song.getSongId() + " ; trackTitle = " + song.getSongTitle());
		boolean flag;
		try {
			flag = lock.writeLock().tryLock(500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			flag = false;
		}
		if (!flag) {
			LOG.info("fail to acquire lock");
			return false;
		}
		try {
			if (song.getSongId() <= 0) {
				LOG.info("key must be positive");
				return false;
			}
			if (trackSet.containsKey(song.getSongId())) return false;
			int[] hash = song.getFingerprint().getHash(0);
			short[] offset = song.getFingerprint().getOffset(0);
			hashTable.add(new Pair<Integer, Pair<int[], short[]>>(song.getSongId(), new Pair<int[], short[]>(hash, offset)));
			trackSet.put(song.getSongId(), song);
			return true;
		} finally {
			lock.writeLock().unlock();
		}
	}

	protected boolean removeSong(Song song) {
		LOG.info("removing song: trackId = " + song.getSongId() + " ; trackTitle = " + song.getSongTitle());
		boolean flag = lock.writeLock().tryLock();
		if (!flag) {
			LOG.info("fail to acquire lock");
			return false;
		}
		try {
			if (!trackSet.containsKey(song.getSongId())) return false;
			for (int i = 0; i < hashTable.size(); ++i)
				if (hashTable.get(i).first == song.getSongId()) {
					hashTable.remove(i);
					break;
				}
			trackSet.remove(song.getSongId());
			return true;
		} finally {
			lock.writeLock().unlock();
		}
	}

	protected boolean removeSong(int key) {
		LOG.info("removing song: trackId = " + key);
		boolean flag = lock.writeLock().tryLock();
		if (!flag) {
			LOG.info("fail to acquire lock");
			return false;
		}
		try {
			if (!trackSet.containsKey(key)) return false;
			for (int i = 0; i < hashTable.size(); ++i)
				if (hashTable.get(i).first == key) {
					hashTable.remove(i);
					break;
				}
			trackSet.remove(key);
			return true;
		} finally {
			lock.writeLock().unlock();
		}
	}

	protected boolean containsSong(Song song) {
		LOG.info("containing song: trackId = " + song.getSongId() + " ; trackTitle = " + song.getSongTitle());
		boolean flag = lock.readLock().tryLock();
		if (!flag) {
			LOG.info("fail to acquire lock");
			return false;
		}
		try {
			return trackSet.containsKey(song.getSongId());
		} finally {
			lock.readLock().unlock();
		}
	}

	protected boolean containsSong(int key) {
		LOG.info("containing song: trackId = " + key);
		boolean flag = lock.readLock().tryLock();
		if (!flag) {
			LOG.info("fail to acquire lock");
			return false;
		}
		try {
			return trackSet.containsKey(key);
		} finally {
			lock.readLock().unlock();
		}
	}

	protected Song getSong(int id) {
		LOG.info("getting song: trackId = " + id);
		boolean flag = lock.readLock().tryLock();
		if (!flag) {
			LOG.info("fail to acquire lock");
			return null;
		}

		try {
			return trackSet.get(id);
		} finally {
			lock.readLock().unlock();
		}
	}

	protected final int binarySearch(int[] hash, int key, int start) { /* the first index which value >= key */
		int l = start - 1, r = hash.length;
		while (l + 1 < r) {
			int mid = (l + r) >> 1;
			if (hash[mid] >= key) r = mid;
			else l = mid;
		}
		return r;
	}

	protected TIntObjectMap<TIntList> match(SongFingerprint pattern, int matchThreshold) {
		lock.readLock().lock();
		try {
			LOG.info("matching");
			TIntObjectMap<TIntList> result = new TIntObjectHashMap<TIntList>();

			for (int t = 0; t < pattern.getDescriptor().getTryMatchTime(); ++t) {
				int[] hash = pattern.getHash(t);
				short[] offset = pattern.getOffset(t);
				SongRecordTryMatcher matcher = new SongRecordTryMatcher();
				for (Pair<Integer, Pair<int[], short[]>> pair : hashTable) {
					int[] songHash = pair.second.first;
					short[] songOffset = pair.second.second;
					int startIndex = 0;
					for (int i = 0; i < hash.length; ++i) {
						int matchIndex = binarySearch(songHash, hash[i], startIndex);
						startIndex = matchIndex;
						while (matchIndex < songHash.length && songHash[matchIndex] == hash[i]) {
							if (songOffset[matchIndex] - offset[i] >= 0) {
								matcher.add(pair.first, songOffset[matchIndex] - offset[i]);
							}
							++matchIndex;
						}
					}
				}
				TIntObjectMap<TIntIntMap> tryResult = matcher.get();
				for (TIntObjectIterator<TIntIntMap> it1 = tryResult.iterator(); it1.hasNext(); ) {
					it1.advance();
					int trackId = it1.key();
					int trackScore = 0;
					for (TIntIntIterator it2 = it1.value().iterator(); it2.hasNext(); ) {
						it2.advance();
						trackScore = Math.max(trackScore, it2.value());
					}
					TIntList trackResultList = result.get(trackId);
					if (trackResultList == null) {
						trackResultList = new TIntLinkedList();
						result.put(trackId, trackResultList);
					}
					trackResultList.add(trackScore);
				}
			}

			System.out.println("[matching result]");
			for (TIntObjectIterator<TIntList> it = result.iterator(); it.hasNext(); ) {
				it.advance();
				if (it.value().max() < matchThreshold) {
					it.remove();
					continue;
				}
				StringBuilder matchMess = null;
				int max = 0;
				for (TIntIterator it1 = it.value().iterator(); it1.hasNext(); ) {
					int v = it1.next();
					max = Math.max(max, v);
					if (matchMess == null) matchMess = new StringBuilder("[" + v);
					else matchMess.append(", " + v);
				}
				matchMess.append("] = " + max);
				System.out.println(it.key() + "\t" + getSong(it.key()).getSongTitle() + "\t" + matchMess.toString());
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	protected TIntObjectMap<TIntList> match(SongFingerprint pattern) {
		int matchThreshold = Integer.parseInt(EngineConfiguration.getInstance().get(MATCH_THRESHOLD));
		return match(pattern, matchThreshold);
	}
}
