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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.04 at 03:45:03 PM MESZ 
//

package mobac.data.gpx.gpx11;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * 
 * A geographic point with optional elevation and time. Available for use by
 * other schemas.
 * 
 * 
 * <p>
 * Java class for ptType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;ptType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;ele&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;time&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}dateTime&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;lat&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/1}latitudeType&quot; /&gt;
 *       &lt;attribute name=&quot;lon&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/1}longitudeType&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ptType", propOrder = { "ele", "time" })
public class PtType {

	protected BigDecimal ele;
	protected XMLGregorianCalendar time;
	@XmlAttribute(required = true)
	protected BigDecimal lat;
	@XmlAttribute(required = true)
	protected BigDecimal lon;

	/**
	 * Gets the value of the ele property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getEle() {
		return ele;
	}

	/**
	 * Sets the value of the ele property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setEle(BigDecimal value) {
		this.ele = value;
	}

	/**
	 * Gets the value of the time property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getTime() {
		return time;
	}

	/**
	 * Sets the value of the time property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setTime(XMLGregorianCalendar value) {
		this.time = value;
	}

	/**
	 * Gets the value of the lat property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getLat() {
		return lat;
	}

	/**
	 * Sets the value of the lat property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setLat(BigDecimal value) {
		this.lat = value;
	}

	/**
	 * Gets the value of the lon property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getLon() {
		return lon;
	}

	/**
	 * Sets the value of the lon property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setLon(BigDecimal value) {
		this.lon = value;
	}

}
