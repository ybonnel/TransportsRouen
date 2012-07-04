/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     ybonnel - initial API and implementation
 */
package fr.ybo.transportsrouen.server.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.ybo.transportsrouen.server.modele.Station;

/**
 * @author ybonnel
 *
 */
public class StationHandler extends DefaultHandler {

	public StationHandler(Station station) {
		this.station = station;
	}

	private Station station;

	/**
	 * StringBuilder servant au parsing xml.
	 */
	private StringBuilder contenu;

	@Override
	public void characters(char[] cars, int start, int length) throws SAXException {
		super.characters(cars, start, length);
		contenu.append(cars, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		if (qName.equals("available")) {
			station.setAvailable(Integer.parseInt(contenu.toString()));
		} else if (qName.equals("free")) {
			station.setFree(Integer.parseInt(contenu.toString()));
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		contenu = new StringBuilder();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		contenu.setLength(0);
	}
}
