package io.github.jmif.entities.melt.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class MapAdapter extends XmlAdapter<MapWrapper, List<Map<String, String>>> {
	public MapWrapper marshal(List<Map<String, String>> data) throws Exception {
		MapWrapper wrapper = new MapWrapper();
		wrapper.row = new ArrayList<MapList>();

		for (Map<String, String> row : data) {

			MapList mapList = new MapList();
			mapList.col = new ArrayList<MapEntry>();

			for (Map.Entry<String, String> col : row.entrySet()) {
				MapEntry entry = new MapEntry();
				entry.key = col.getKey();
				entry.value = col.getValue();
				mapList.col.add(entry);
			}
			wrapper.row.add(mapList);
		}
		return wrapper;
	}

	public List<Map<String, String>> unmarshal(MapWrapper wrapper) throws Exception {
		List<Map<String, String>> r = new ArrayList<Map<String, String>>();
		for (MapList row : wrapper.row) {
			Map<String, String> mapList = new HashMap<String, String>();

			for (MapEntry col : row.col) {
				mapList.put(col.key, col.value);
			}
			r.add(mapList);
		}
		return r;
	}
}
