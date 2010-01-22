//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.04 at 03:45:03 PM MESZ 
//

package mobac.data.gpx.gpx11;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import mobac.program.ProgramInfo;


/**
 * 
 * GPX documents contain a metadata header, followed by waypoints, routes, and
 * tracks. You can add your own elements to the extensions section of the GPX
 * document.
 * 
 * 
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;metadata&quot; type=&quot;{http://www.topografix.com/GPX/1/1}metadataType&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;wpt&quot; type=&quot;{http://www.topografix.com/GPX/1/1}wptType&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;rte&quot; type=&quot;{http://www.topografix.com/GPX/1/1}rteType&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;trk&quot; type=&quot;{http://www.topografix.com/GPX/1/1}trkType&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;extensions&quot; type=&quot;{http://www.topografix.com/GPX/1/1}extensionsType&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;version&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; fixed=&quot;1.1&quot; /&gt;
 *       &lt;attribute name=&quot;creator&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "metadata", "wpt", "rte", "trk", "extensions" })
@XmlRootElement(name = "gpx")
public class Gpx {

	protected MetadataType metadata;
	protected List<WptType> wpt;
	protected List<RteType> rte;
	protected List<TrkType> trk;
	protected ExtensionsType extensions;
	@XmlAttribute(required = true)
	protected String version;
	@XmlAttribute(required = true)
	protected String creator;

	public static Gpx createGpx() {
		Gpx gpx = new Gpx();
		gpx.setVersion("1.1");
		gpx.setCreator(ProgramInfo.getVersionTitle());
		return gpx;
	}

	protected Gpx() {
	}

	/**
	 * Gets the value of the metadata property.
	 * 
	 * @return possible object is {@link MetadataType }
	 * 
	 */
	public MetadataType getMetadata() {
		return metadata;
	}

	/**
	 * Sets the value of the metadata property.
	 * 
	 * @param value
	 *            allowed object is {@link MetadataType }
	 * 
	 */
	public void setMetadata(MetadataType value) {
		this.metadata = value;
	}

	/**
	 * Gets the value of the wpt property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the wpt property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getWpt().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link WptType }
	 * 
	 * 
	 */
	public List<WptType> getWpt() {
		if (wpt == null) {
			wpt = new ArrayList<WptType>();
		}
		return this.wpt;
	}

	/**
	 * Gets the value of the rte property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the rte property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getRte().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link RteType }
	 * 
	 * 
	 */
	public List<RteType> getRte() {
		if (rte == null) {
			rte = new ArrayList<RteType>();
		}
		return this.rte;
	}

	/**
	 * Gets the value of the trk property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the trk property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getTrk().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link TrkType }
	 * 
	 * 
	 */
	public List<TrkType> getTrk() {
		if (trk == null) {
			trk = new ArrayList<TrkType>();
		}
		return this.trk;
	}

	/**
	 * Gets the value of the extensions property.
	 * 
	 * @return possible object is {@link ExtensionsType }
	 * 
	 */
	public ExtensionsType getExtensions() {
		return extensions;
	}

	/**
	 * Sets the value of the extensions property.
	 * 
	 * @param value
	 *            allowed object is {@link ExtensionsType }
	 * 
	 */
	public void setExtensions(ExtensionsType value) {
		this.extensions = value;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVersion() {
		if (version == null) {
			return "1.1";
		} else {
			return version;
		}
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(String value) {
		this.version = value;
	}

	/**
	 * Gets the value of the creator property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * Sets the value of the creator property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCreator(String value) {
		this.creator = value;
	}

}
