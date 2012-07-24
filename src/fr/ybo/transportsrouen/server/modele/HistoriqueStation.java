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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Id;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.base.Objects;
import com.googlecode.objectify.Key;

/**
 * @author ybonnel
 *
 */
public class HistoriqueStation implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(HistoriqueStation.class.getName());

	@Id
	private Long id;
	private Key<Station> station;
	private Date dateHisto;
	private int available;
	private int free;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Key<Station> getStation() {
		return station;
	}

	public void setStation(Key<Station> station) {
		this.station = station;
	}

	public Date getDateHisto() {
		return dateHisto;
	}

	public void setDateHisto(Date dateHisto) {
		this.dateHisto = dateHisto;
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

	@SuppressWarnings("unchecked")
	public static List<HistoriqueStation> retreive(Station station) {
		MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
		CleHistorique cleHistorique = new CleHistorique(station.getId());
		List<HistoriqueStation> historiques = (List<HistoriqueStation>) cache.get(cleHistorique);
		if (historiques == null) {
			logger.warning("Les historiques de " + station.getName() + " ne sont pas en cache, appel du datastore");
			historiques = new ObjectifyDao().ofy().query(HistoriqueStation.class).filter("station", station).list();
			if (historiques != null) {
				cache.put(cleHistorique, historiques);
			}
		}
		return historiques;
	}

	public static void store(HistoriqueStation historiqueStation) {
		ObjectifyDao dao = new ObjectifyDao();
		Station station = dao.ofy().get(historiqueStation.getStation());
		addHistoriqueStationInCache(station, historiqueStation);
		new ObjectifyDao().ofy().put(historiqueStation);
	}

	private static void addHistoriqueStationInCache(Station station, HistoriqueStation historiqueStation) {
		CleHistorique cleHistorique = new CleHistorique(station.getId());
		List<HistoriqueStation> historiques = retreive(station);
		historiques.add(historiqueStation);
		MemcacheServiceFactory.getMemcacheService().delete(cleHistorique);
		MemcacheServiceFactory.getMemcacheService().put(cleHistorique, historiques);
	}

	public static class CleHistorique implements Serializable {
		public CleHistorique(Long idStation) {
			this.idStation = idStation;
		}

		private static final long serialVersionUID = 1L;
		private long idStation;

		@Override
		public int hashCode() {
			return Objects.hashCode(CleHistorique.class, idStation);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass())
				return false;
			return idStation == ((CleHistorique) obj).idStation;
		}

	}
}
