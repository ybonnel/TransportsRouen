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
package fr.ybo.transportsrouen.server.modele;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Id;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.base.Throwables;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.NotSaved;

import fr.ybo.transportsrouen.server.sax.CyclicHandler;
import fr.ybo.transportsrouen.server.sax.StationHandler;

/**
 * @author ybonnel
 *
 */
public class Station implements Serializable {

	private static final Logger logger = Logger.getLogger(Station.class.getName());
	private static final long serialVersionUID = 1L;

	@Id
	private Long id;
	private String name;
	private int number;
	private String adresse;
	private double latitude;
	private double longitude;
	private int available;
	private int free;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public int getAvailable() {
		return available;
	}

	public void setAvailable(int available) {
		this.available = available;
	}

	public int getFree() {
		return free;
	}

	public void setFree(int free) {
		this.free = free;
	}

	@Override
	public String toString() {
		return "Station [name=" + name + ", number=" + number + "]";
	}

	public static List<Station> getStationsFromApi() {
		logger.info("Appel de getStationsFromApi");
		List<Station> stations = new ArrayList<Station>();

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			CyclicHandler handler = new CyclicHandler();

			InputStream inputStream = openInputStream("http://cyclic.rouen.fr/service/carto");
			try {
				parser.parse(inputStream, handler);
				stations = handler.getStations();
			} finally {
				try {
					inputStream.close();
				} catch (Exception ignore) {
				}
			}
		} catch (Exception e) {
			Throwables.propagate(e);
		}

		for (Station station : stations) {
			remplirStation(station);
		}
		logger.info("Fin getStationsFromApi");
		return stations;
	}

	private static void remplirStation(Station station) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			StationHandler handler = new StationHandler(station);

			InputStream inputStream =
					openInputStream("http://cyclic.rouen.fr/service/stationdetails/rouen/" + station.getNumber());
			try {
				parser.parse(inputStream, handler);
			} finally {
				try {
					inputStream.close();
				} catch (Exception ignore) {
				}
			}
		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	/**
	 * Timeout de connexion.
	 */
	private static final int CONNECT_TIMEOUT = 10000;
	/**
	 * Timeout de lecture.
	 */
	private static final int READ_TIMEOUT = 20000;

	private static InputStream openInputStream(String url) {
		try {
			URL myUrl = new URL(url);
			URLConnection connection = myUrl.openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT);
			return connection.getInputStream();
		} catch (Exception exception) {
			Throwables.propagate(exception);
			return null;
		}
	}

	public static void store(Station station) {
		logger.info("Store de " + station.toString());
		ObjectifyDao dao = new ObjectifyDao();
		dao.ofy().put(station);
		if (station.historique != null) {
			HistoriqueStation.store(station.historique);
		}
	}

	public static void resetCache(List<Station> stations) {
		logger.info("Reset du cache");
		MemcacheServiceFactory.getMemcacheService().delete("stations");
		MemcacheServiceFactory.getMemcacheService().put("stations", stations);
	}

	@SuppressWarnings("unchecked")
	public static List<Station> retrieve() {
		MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
		List<Station> stations = (List<Station>) cache.get("stations");
		if (stations == null) {
			logger.warning("Les stations ne sont pas en cache, appel du datastore");
			stations = new ObjectifyDao().ofy().query(Station.class).list();
			if (stations != null) {
				cache.put("stations", stations);
			}
		}
		return stations;
	}

	@NotSaved
	private transient HistoriqueStation historique = null;

	public boolean merge(Station station) {
		boolean isMerged = false;
		if (!station.getName().equals(name)) {
			isMerged = true;
			name = station.getName();
		}
		if (!station.getAdresse().equals(adresse)) {
			isMerged = true;
			adresse = station.getAdresse();
		}
		if (latitude != station.getLatitude()) {
			isMerged = true;
			latitude = station.getLatitude();
		}
		if (longitude != station.getLongitude()) {
			isMerged = true;
			longitude = station.getLongitude();
		}
		int oldAvailable = available;
		int oldFree = free;
		if (available != station.getAvailable()) {
			historique = new HistoriqueStation();
			isMerged = true;
			available = station.getAvailable();
		}
		if (free != station.getFree()) {
			isMerged = true;
			free = station.getFree();
		}
		if (isMerged) {
			historique = new HistoriqueStation();
			historique.setAvailable(oldAvailable);
			historique.setFree(oldFree);
			historique.setDateHisto(new Date());
			historique.setStation(new Key<Station>(Station.class, id));
		}
		return isMerged;
	}
}
