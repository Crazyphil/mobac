package mobac.mapsources.impl;

import java.awt.Color;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MultiLayerMapSource;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


public class OsmMapSources {

	protected static final String MAP_MAPNIK = "http://tile.openstreetmap.org";
	protected static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles/tile";
	public static final String MAP_HIKING_TRAILS = "http://topo.geofabrik.de/trails/";
	public static final String MAP_HIKING_RELIEF = "http://topo.gvf.ve.it/cont/";
	protected static final String MAP_PISTE = "http://openpistemap.org/tiles/contours/";

	protected static abstract class AbstractOsmTileSource extends AbstractMapSource {

		public AbstractOsmTileSource(String name) {
			super(name, 0, 18, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		public String getTileType() {
			return "png";
		}

		public boolean allowFileStore() {
			return true;
		}
	}

	public static class Mapnik extends AbstractOsmTileSource {

		public Mapnik() {
			super("Mapnik");
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_MAPNIK + super.getTileUrl(zoom, tilex, tiley);
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfNoneMatch;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Mapnik";
		}

	}

	public static class CycleMap extends AbstractOsmTileSource {

		private static final String PATTERN = "http://%s.andy.sandbox.cloudmade.com/tiles/cycle/%d/%d/%d.png";

		private static final String[] SERVER = { "a", "b", "c" };

		private int SERVER_NUM = 0;

		public CycleMap() {
			super("OSM Cycle Map");
			this.maxZoom = 17;
			this.tileUpdate = TileUpdate.ETag;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			String url = String.format(PATTERN, new Object[] { SERVER[SERVER_NUM], zoom, tilex,
					tiley });
			SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
			return url;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Cyclemap";
		}

	}

	public static class OsmPublicTransport extends AbstractOsmTileSource {

		private static final String PATTERN = "http://tile.xn--pnvkarte-m4a.de/tilegen/%d/%d/%d.png";

		public OsmPublicTransport() {
			super("OSMPublicTransport");
			this.maxZoom = 16;
			this.minZoom = 2;
			this.tileUpdate = TileUpdate.ETag;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			String url = String.format(PATTERN, new Object[] { zoom, tilex, tiley });
			return url;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Public Transport";
		}

	}

	public static class TilesAtHome extends AbstractOsmTileSource {

		public TilesAtHome() {
			super("TilesAtHome");
			this.maxZoom = 17;
			this.tileUpdate = TileUpdate.IfModifiedSince;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_OSMA + super.getTileUrl(zoom, tilex, tiley);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Osmarenderer";
		}

	}

	public static class OsmHikingMap extends AbstractMapSource {

		public OsmHikingMap() {
			super("OSM Hiking", 4, 15, "png", TileUpdate.LastModified);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_TRAILS + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class OsmHikingRelief extends AbstractMapSource {

		public OsmHikingRelief() {
			super("OSM Hiking Relief", 4, 15, "png", TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking Relief only (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_RELIEF + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class OsmHikingMapWithRelief extends OsmHikingMap implements MultiLayerMapSource {

		private MapSource background = new OsmHikingRelief();

		@Override
		public String toString() {
			return "OpenStreetMap Hiking with Relief";
		}

		@Override
		public String getName() {
			return "OSM Hiking with Relief";
		}

		public MapSource getBackgroundMapSource() {
			return background;
		}
	}

	public static class OpenPisteMap extends AbstractMapSource {

		public OpenPisteMap() {
			super("OpenPisteMap", 0, 17, "png", TileUpdate.LastModified);
		}

		@Override
		public String toString() {
			return "Open Piste Map";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_PISTE + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	/**
	 * Uses 512x512 tiles - not fully supported at the moment!
	 */
	public static class Turaterkep extends AbstractMapSource {

		private MapSpace space = new MercatorPower2MapSpace(512);

		public Turaterkep() {
			super("Turaterkep", 7, 16, "png", TileUpdate.IfNoneMatch);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://turaterkep.hostcity.hu/tiles/" + zoom + "/" + tilex + "/" + tiley
					+ ".png";
		}

		@Override
		public MapSpace getMapSpace() {
			return space;
		}

		@Override
		public String toString() {
			return "Turaterkep (Hungary)";
		}

		@Override
		public Color getBackgroundColor() {
			return Color.WHITE;
		}

	}

}
