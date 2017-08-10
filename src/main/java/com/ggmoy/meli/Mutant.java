package com.ggmoy.meli;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Path("/mutant")
public class Mutant {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkMutant(String json) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		// Create a JSON Object from String
		JsonReader reader = Json.createReader(new StringReader(json));
		JsonObject json_obj = reader.readObject();
		reader.close();

		// Create a JsonArray object with the DNA Strings.  Then, we'll use
		// the hash code of this object as the DNA Entity's key
		JsonArray json_arr = json_obj.getJsonArray("dna");
		Key key = KeyFactory.createKey("DNAs", json_arr.hashCode());

		// We have to check if the DNA alredy exists in the datastore.  If it doesn't
		// exist, we have to create a DNA Entity and add it to the datastore and update
		// statistics
		Entity dna_entity = null;
		boolean found = true;
		try {
			dna_entity = datastore.get(key);
		} catch (EntityNotFoundException expected) {
			found = false;
		}

		if (found == false) {
			// Create a DNA String array from JSON
			String[] dna = new String[json_arr.size()];
			for (int i = 0; i < dna.length; i++) {
				dna[i] = json_arr.getString(i);
			}

			// Create a new DNA Entity and add it to the datastore
			dna_entity = new Entity("DNAs", key.getId());
			dna_entity.setProperty("dna", json_arr.toString());
			dna_entity.setProperty("is_mutant", isMutant(dna));
			datastore.put(dna_entity);

			// Update statistics
			Entity stat_entity = null;
			key = KeyFactory.createKey("STATs", "dna-statistics");
			try {
				stat_entity = datastore.get(key);
			} catch (EntityNotFoundException expected1) {
				stat_entity = new Entity(key);
				stat_entity.setProperty("count_mutant_dna", 0l);
				stat_entity.setProperty("count_human_dna", 0l);
			}

			if ((Boolean) dna_entity.getProperty("is_mutant")) {
				stat_entity.setProperty("count_mutant_dna", ((Long) stat_entity.getProperty("count_mutant_dna")) + 1);
			} else {
				stat_entity.setProperty("count_human_dna", ((Long) stat_entity.getProperty("count_human_dna")) + 1);
			}

			datastore.put(stat_entity);
		}

		if ((Boolean) dna_entity.getProperty("is_mutant")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity("").build();
		}
	}

	private boolean isMutant(String[] dna) {
		// Convert from String array to char matrix
		char matrix[][] = new char[dna.length][];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = dna[i].toCharArray();
		}

		return isMutant(matrix);
	}

	private boolean isMutant(char[][] dna) {
		int counter = 0;

		counter += searchOnRows(dna);
		if (counter >= 2) {
			return true;
		}

		counter += searchOnCols(dna);
		if (counter >= 2) {
			return true;
		}

		counter += searchOnMajorDiags(dna);
		if (counter >= 2) {
			return true;
		}

		counter += searchOnMinorDiags(dna);
		if (counter >= 2) {
			return true;
		}
		
		return false;
	}

	private int searchOnRows(char matrix[][]) {
		int found = 0;

		for (int row = 0; row < matrix.length; row++) {
			int count = 1;
			for (int col = 1; col < matrix.length; col++) {
				if (count == 0 || matrix[row][col] != matrix[row][col - 1]) {
					count = 1;
				} else {
					count += 1;
					if (count == 4) {
						found += 1;
						if (found == 2) { return found; }
						count = 0;
					}
				}
			}
		}

		return found;
	}

	private int searchOnCols(char matrix[][]) {
		int found = 0;

		for (int col = 0; col < matrix.length; col++) {
			int count = 1;
			for (int row = 1; row < matrix.length; row++) {
				if (count == 0 || matrix[row][col] != matrix[row - 1][col]) {
					count = 1;
				} else {
					count += 1;
					if (count == 4) {
						found += 1;
						if (found == 2) { return found; }
						count = 0;
					}
				}
			}
		}

		return found;
	}

	private int searchOnMinorDiags(char matrix[][]) {
		int found = 0;

		// Search on principal minor diagonal AND the ones above it
		for (int row = 3; row < matrix.length; row++) {
			int count = 1;
			for (int col = 1; col <= row; col++) {
				int curr_row = row - col;
				if (count == 0 || matrix[curr_row][col] != matrix[curr_row + 1][col - 1]) {
					count = 1;
				} else {
					count += 1;
					if (count == 4) {
						found += 1;
						if (found == 2) { return found; }
						count = 0;
					}
				}
			}
		}

		// Search on diagonals below principal minor diagonal
		for (int col1 = 1; col1 < matrix.length - 3; col1++) {
			int count = 1;
			for (int col2 = col1 + 1; col2 < matrix.length; col2++) {
				int curr_row = (matrix.length - 1) - (col2 - col1);
				if (count == 0 || matrix[curr_row][col2] != matrix[curr_row + 1][col2 - 1]) {
					count = 1;
				} else {
					count += 1;
					if (count == 4) {
						found += 1;
						if (found == 2) { return found; }
						count = 0;
					}
				}
			}
		}

		return found;
	}

	private int searchOnMajorDiags(char matrix[][]) {
		int found = 0;

		// Search on principal major diagonal AND the ones below it
		for (int row = 0; row < matrix.length - 3; row++) {
			int count = 1;
			for (int col = 1; col < matrix.length - row; col++) {
				int curr_row = row + col;
				if (count == 0 || matrix[curr_row][col] != matrix[curr_row - 1][col - 1]) {
					count = 1;
				} else {
					count += 1;
					if (count == 4) {
						found += 1;
						if (found == 2) { return found; }
						count = 0;
					}
				}
			}
		}

		// Search on diagonals above principal major diagonal
		for (int col1 = 1; col1 < matrix.length - 3; col1++) {
			int count = 1;
			for (int col2 = col1 + 1; col2 < matrix.length; col2++) {
				int curr_row = col2 - col1;
				if (count == 0 || matrix[curr_row][col2] != matrix[curr_row - 1][col2 - 1]) {
					count = 1;
				} else {
					count += 1;
					if (count == 4) {
						found += 1;
						if (found == 2) { return found; }
						count = 0;
					}
				}
			}
		}

		return found;
	}

}
