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
package mobac.mapsources.custom;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mobac.exceptions.TileException;
import mobac.gui.mapview.PreviewMap;
import mobac.mapsources.mapspace.MapSpaceFactory;
import mobac.program.interfaces.FileBasedMapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.jaxb.ColorAdapter;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;
import mobac.utilities.Utilities;
import mobac.utilities.jdbc.SQLiteLoader;

import org.apache.log4j.Logger;

/**
 * 
 * MBTiles input http://mbtiles.org/
 * 
 */
@XmlRootElement(name = "localTileSQLite")
public class CustomLocalTileSQliteMapSource implements FileBasedMapSource {

	private static Logger log = Logger.getLogger(CustomLocalTileSQliteMapSource.class);

	private static enum SQLiteAtlasType {
		RMaps, MBTiles, BigPlanetTracks, Galileo, NaviComputer
	};

	private MapSourceLoaderInfo loaderInfo = null;

	private boolean initialized = false;

	private TileImageType tileImageType = null;

	@XmlElement(nillable = false, defaultValue = "CustomLocalSQLite")
	private String name = "CustomLocalSQLite";

	private int minZoom = PreviewMap.MIN_ZOOM;

	private int maxZoom = PreviewMap.MAX_ZOOM;

	@XmlElement(required = true)
	private File sourceFile = null;

	@XmlElement(required = true)
	private SQLiteAtlasType atlasType = null;

	@XmlElement(defaultValue = "#000000")
	@XmlJavaTypeAdapter(ColorAdapter.class)
	private Color backgroundColor = Color.BLACK;

	private String sqlMaxZoomStatement;
	private String sqlMinZoomStatement;
	private String sqlTileStatement;

	/**
	 * SQLite connection with database file
	 */
	private Connection conn = null;

	private final MapSpace mapSpace = MapSpaceFactory.getInstance(256, true);

	public CustomLocalTileSQliteMapSource() {
		super();
	}

	protected void updateZoomLevelInfo() {
		Statement statement = null;
		try {
			statement = conn.createStatement();
			if (statement.execute(sqlMaxZoomStatement)) {
				ResultSet rs = statement.getResultSet();
				if (rs.next()) {
					maxZoom = rs.getInt(1);
				}
				rs.close();
			}
			if (statement.execute(sqlMinZoomStatement)) {
				ResultSet rs = statement.getResultSet();
				if (rs.next()) {
					minZoom = rs.getInt(1);
				}
				rs.close();
			}
			statement.close();
		} catch (SQLException e) {
			log.error("", e);
		} finally {
			Utilities.closeStatement(statement);
		}
	}

	public synchronized void initialize() {
		if (initialized)
			return;
		if (atlasType == null) {
			JOptionPane.showMessageDialog(null, "No database  specified.\nMap name:" + name + "\nFilename: "
					+ sourceFile, "Invaild source file", JOptionPane.ERROR_MESSAGE);
			initialized = true;
			return;
		}
		if (!sourceFile.isFile()) {
			JOptionPane.showMessageDialog(null, "The specified source SQLite database does not exist:\nMap name: "
					+ name + "\nFilename: " + sourceFile, "Invaild source file", JOptionPane.ERROR_MESSAGE);
			initialized = true;
			return;
		}
		if (!SQLiteLoader.loadSQLiteOrShowError()) {
			initialized = true;
			return;
		}
		log.debug("Loading SQLite database " + sourceFile);
		String url = "jdbc:sqlite:/" + this.sourceFile;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "The specified source SQLite database could not be loaded:\nMap name: "
					+ name + "\nFilename: " + sourceFile + "\nError: " + e.getMessage(), "Error loading database",
					JOptionPane.ERROR_MESSAGE);
			initialized = true;
			return;
		}
		switch (atlasType) {
		case MBTiles:
			// DISTINCT works much faster than min(zoom_level) or max(zoom_level) - uses index?
			sqlMaxZoomStatement = "SELECT DISTINCT zoom_level FROM tiles ORDER BY zoom_level DESC LIMIT 1;";
			sqlMinZoomStatement = "SELECT DISTINCT zoom_level FROM tiles ORDER BY zoom_level ASC LIMIT 1;";
			sqlTileStatement = "SELECT tile_data from tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?;";
			break;
		case RMaps:
		case BigPlanetTracks:
		case Galileo:
			sqlMaxZoomStatement = "SELECT DISTINCT z FROM tiles ORDER BY z DESC LIMIT 1;";
			sqlMinZoomStatement = "SELECT DISTINCT z FROM tiles ORDER BY z ASC LIMIT 1;";
			sqlTileStatement = "SELECT image from tiles WHERE z=? AND x=? AND y=?;";
			break;
		case NaviComputer:
			sqlMaxZoomStatement = "SELECT DISTINCT zoom FROM Tiles ORDER BY zoom DESC LIMIT 1;";
			sqlMinZoomStatement = "SELECT DISTINCT zoom FROM Tiles ORDER BY zoom ASC LIMIT 1;";
			sqlTileStatement = "SELECT Tile FROM Tiles LEFT JOIN Tilesdata ON Tiles.id=Tilesdata.id WHERE Zoom=? AND X=? AND Y=?;";
			break;
		}
		updateZoomLevelInfo();
		initialized = true;
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		if (!initialized)
			initialize();
		PreparedStatement statement = null;
		try {
			switch (atlasType) {
			case MBTiles:
				y = (1 << zoom) - y - 1;
				break;
			case RMaps:
			case BigPlanetTracks:
			case Galileo:
				zoom = 17 - zoom;
				break;
			}

			statement = conn.prepareStatement(sqlTileStatement);
			statement.setInt(1, zoom);
			statement.setInt(2, x);
			statement.setInt(3, y);
			if (log.isTraceEnabled())
				log.trace(String.format("Loading tile z=%d x=%d y=%d", zoom, x, y));
			if (statement.execute()) {
				ResultSet rs = statement.getResultSet();
				if (!rs.next()) {
					return null;
				}
				byte[] data = rs.getBytes(1);
				rs.close();
				return data;
			}
		} catch (SQLException e) {
			log.error("", e);
		} finally {
			Utilities.closeStatement(statement);
		}
		return null;
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		byte[] data = getTileData(zoom, x, y, loadMethod);
		if (data == null)
			return null;
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	public TileImageType getTileImageType() {
		return tileImageType;
	}

	public int getMaxZoom() {
		return maxZoom;
	}

	public int getMinZoom() {
		return minZoom;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public MapSpace getMapSpace() {
		return mapSpace;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	@XmlTransient
	public MapSourceLoaderInfo getLoaderInfo() {
		return loaderInfo;
	}

	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo) {
		this.loaderInfo = loaderInfo;
	}

	protected void closeConnection() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
		conn = null;
	}
}