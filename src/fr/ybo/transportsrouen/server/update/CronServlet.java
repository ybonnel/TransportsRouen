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
package fr.ybo.transportsrouen.server.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.ybo.transportsrouen.server.modele.Station;

/**
 * @author ybonnel
 * 
 */
public class CronServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(CronServlet.class.getName());

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.info("Début de cronServlet");
		try {
			Map<Integer, Station> stationsFromDb = new HashMap<Integer, Station>();
			for (Station stationFromDb : Station.retrieve()) {
				stationsFromDb.put(stationFromDb.getNumber(), stationFromDb);
			}
			Station stationFromDb;
			boolean cacheHaveToBeUpdated = false;
			for (Station station : Station.getStationsFromApi()) {
				stationFromDb = stationsFromDb.get(station.getNumber());
				if (stationFromDb == null) {
					stationFromDb = station;
					Station.store(stationFromDb);
					cacheHaveToBeUpdated = true;
					stationsFromDb.put(stationFromDb.getNumber(), stationFromDb);
				} else {
					if (stationFromDb.merge(station)) {
						Station.store(stationFromDb);
						cacheHaveToBeUpdated = true;
					}
				}
			}
			if (cacheHaveToBeUpdated) {
				Station.resetCache(new ArrayList<Station>(stationsFromDb.values()));
			}
		} catch (Exception exception) {
			logger.log(Level.SEVERE, "Erreur durant CronServlet", exception);
		}
		logger.info("Fin de cronServlet");
	}

}
