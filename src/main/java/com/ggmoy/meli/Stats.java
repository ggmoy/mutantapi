package com.ggmoy.meli;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Path("/stats")
public class Stats {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getStats() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		long count_mutant_dna;
		long count_human_dna;

		Entity stat_entity = null;
		Key key = KeyFactory.createKey("STATs", "dna-statistics");
		try {
			stat_entity = datastore.get(key);
			count_mutant_dna = (Long) stat_entity.getProperty("count_mutant_dna");
			count_human_dna = (Long) stat_entity.getProperty("count_human_dna");
		} catch (EntityNotFoundException expected1) {
			count_mutant_dna = 0;
			count_human_dna = 0;
		}
		double ratio = count_human_dna == 0 ? 0.0d : Math.round((double) count_mutant_dna / count_human_dna * 100.0d) / 100.0d;

		return "{ \"count_mutant_dna\": " + count_mutant_dna + ", \"count_human_dna\": " + count_human_dna + ", \"ratio\": " + ratio + " }";
	}

}
