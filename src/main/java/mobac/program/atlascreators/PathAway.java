/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.program.atlascreators;

import java.io.File;
import java.io.IOException;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.impl.OsmMapSources;
import mobac.mapsources.impl.Google.GoogleEarth;
import mobac.mapsources.impl.Google.GoogleMaps;
import mobac.mapsources.impl.Google.GoogleTerrain;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.utilities.tar.TarIndex;


/**
 * Creates a tile cache structure as used by <a href="http://www.pathaway.com/">PathAway</a> (for WindowsMobile,
 * Symbian, Palm)
 */
public class PathAway extends OSMTracker {

	public PathAway() {
		super();
		tileFileNamePattern = "%02X/%04X/%04X.%s";
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);

		MapSource mapSource = map.getMapSource();
		String shortMapDir = null;
		if (mapSource.getClass().equals(GoogleMaps.class))
			shortMapDir = "G1";
		else if (mapSource.getClass().equals(GoogleEarth.class))
			shortMapDir = "G2";
		else if (mapSource.getClass().equals(GoogleTerrain.class))
			shortMapDir = "G3";
		else if (mapSource.getClass().equals(OsmMapSources.Mapnik.class))
			shortMapDir = "OSM1";
		else if (mapSource.getClass().equals(OsmMapSources.CycleMap.class))
			shortMapDir = "OCM1";
		if (shortMapDir != null)
			mapDir = new File(atlasDir, shortMapDir);
	}

	public void createMap() throws MapCreationException, InterruptedException {
		// This means there should not be any resizing of the tiles.
		if (mapTileWriter == null)
			mapTileWriter = new PathAwayTileWriter();
		createTiles();
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				if (map.getZoom() > 17)
					throw new AtlasTestException("resolution too high - " + "highest possible zoom level is 17");
			}
		}
	}

	protected class PathAwayTileWriter extends OSMTileWriter {

		@Override
		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			File file = new File(mapDir, String.format(tileFileNamePattern, 17 - zoom, tilex, tiley, tileType));
			writeTile(file, tileData);
		}

	}
}
