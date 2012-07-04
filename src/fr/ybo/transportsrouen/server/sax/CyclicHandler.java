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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.ybo.transportsrouen.server.modele.Station;

/**
 * @author ybonnel
 *
 */
public class CyclicHandler extends DefaultHandler {

	private List<Station> stations = new ArrayList<Station>();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (qName.equals("marker")) {
			Station currentStation = new Station();
			currentStation.setName(attributes.getValue("name"));
			currentStation.setNumber(Integer.parseInt(attributes.getValue("number")));
			currentStation.setAdresse(attributes.getValue("address"));
			currentStation.setLatitude(Double.parseDouble(attributes.getValue("lat")));
			currentStation.setLongitude(Double.parseDouble(attributes.getValue("lng")));
			stations.add(currentStation);
		}
	}

    public List<Station> getStations() {
		return stations;
	}
}
