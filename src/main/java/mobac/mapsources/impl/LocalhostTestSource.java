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
package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;

public class LocalhostTestSource extends AbstractMapSource {

	private final boolean allowStore;

	private String baseUrl;

	public LocalhostTestSource(String name, String tileType, boolean allowStore) {
		this(name, 80, tileType, allowStore);
	}

	public LocalhostTestSource(String name, int port, String tileType, boolean allowStore) {
		super(name, 0, 22, tileType);
		this.allowStore = allowStore;
		baseUrl = "http://127.0.0.1:" + port + "/tile." + tileType + "?";
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		return baseUrl + "x=" + tilex + "&y=" + tiley + "&z=" + zoom;
	}

	@Override
	public boolean allowFileStore() {
		return allowStore;
	}

}