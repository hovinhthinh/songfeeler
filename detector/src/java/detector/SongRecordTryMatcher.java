package detector;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Created by thinhhv on 15/08/2014.
 */
class SongRecordTryMatcher {
	private TIntObjectMap<TIntIntMap> matchMap;

	protected SongRecordTryMatcher() {
		matchMap = new TIntObjectHashMap<TIntIntMap>();
	}

	protected void add(int trackId, int diff) {
		TIntIntMap diffMap = matchMap.get(trackId);
		if (diffMap == null) {
			diffMap = new TIntIntHashMap();
			matchMap.put(trackId, diffMap);
		}
		if (diffMap.containsKey(diff)) {
			diffMap.put(diff, diffMap.get(diff) + 1);
		} else {
			diffMap.put(diff, 1);
		}
	}

	protected TIntObjectMap<TIntIntMap> get() {
		return matchMap;
	}
}
