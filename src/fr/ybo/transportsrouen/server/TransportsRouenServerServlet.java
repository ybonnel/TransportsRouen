package fr.ybo.transportsrouen.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.ybo.transportsrouen.server.services.TransportsRouenService;

@SuppressWarnings("serial")
public class TransportsRouenServerServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(TransportsRouenServerServlet.class.getName());

	private Gson gson;

	public Gson getGson() {
		if (gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gson = gsonBuilder.create();
		}
		return gson;
	}

	private List<Object> arguments(List<String> path) {
		return ImmutableList.builder().addAll(Iterables.skip(path, 1)).build();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String[] champs = req.getPathInfo().split("/");
		List<String> path = new ArrayList<String>();
		for (String champ : champs) {
			if (champ != null && champ.length() > 0) {
				path.add(champ);
			}
		}
		String action = Iterables.getFirst(path, "getall");

		try {
			Object object = Reflection.invokeStatic(TransportsRouenService.class, action, arguments(path));

			resp.setContentType("application/json");
			resp.getWriter().print(getGson().toJson(object));
		} catch (Exception exception) {
			logger.log(Level.SEVERE, "Problème dans TransportsRouenServerServlet", exception);
			resp.getWriter().println("L'action " + action + " n'existe pas");
			resp.setStatus(404);

		}
	}
}
