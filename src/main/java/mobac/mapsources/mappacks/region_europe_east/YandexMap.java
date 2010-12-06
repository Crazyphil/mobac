package mobac.mapsources.mappacks.region_europe_east;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.UpdatableMapSource;
import mobac.mapsources.mapspace.MercatorPower2MapSpaceEllipsoidal;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;

/**
 * Yandex Maps
 */
public class YandexMap extends AbstractHttpMapSource implements UpdatableMapSource {
	// YandexMap.url=http://vec0{$servernum}.maps.yandex.ru/tiles?l=map&v=2.10.2&x={$x}&y={$y}&z={$z}

	int SERVER_NUM = 1;

	String urlPattern;

	public YandexMap() {
		super("YandexMap", 1, 17, TileImageType.PNG, HttpMapSource.TileUpdate.IfModifiedSince);
		update();
	}

	@Override
	public MapSpace getMapSpace() {
		return MercatorPower2MapSpaceEllipsoidal.INSTANCE_256;
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		SERVER_NUM = (SERVER_NUM % 3) + 3;
		String tmp = urlPattern;
		tmp = tmp.replace("{$servernum}", Integer.toString(SERVER_NUM));
		tmp = tmp.replace("{$x}", Integer.toString(tilex));
		tmp = tmp.replace("{$y}", Integer.toString(tiley));
		tmp = tmp.replace("{$z}", Integer.toString(zoom));
		return tmp;
	}

	@Override
	public String toString() {
		return "Yandex Map (Russia)";
	}

	public void update() {
		urlPattern = MapSourceTools.loadMapUrl(this, "url");
	}

}