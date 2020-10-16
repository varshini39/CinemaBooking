import com.cinema.model.Hall;
import com.cinema.model.Movie;
import com.cinema.model.Screen;
import com.cinema.model.Shows;
import com.cinema.model.Users;
import com.cinema.util.RedisUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Main {

	public static void main(String args[]) {
		/*Movie movieDetails = new Movie("Interstellar", 2.17f, "Sci-fi");
		movieDetails.addMovieDetails();
		Long movieId = movieDetails.getMovieId();
		System.out.println("MOVIEID::: "+movieId);*/
		/*Movie movieDetails = new Movie(8L);
		movieDetails.populateMovieDetails();*/

		/*Screen screenDetails = new Screen("Morning", "9:30am");
		screenDetails.addScreenDetails();
		Long screenId = screenDetails.getScreenId();
		System.out.println("SCREENID:: "+screenId);*/
		/*Screen screenDetails = new Screen(3L);
		screenDetails.populateScreenDetails();*/

		/*Hall hallDetails = new Hall("INOX", 20, 200);
		hallDetails.addHallDetails();
		Long hallId = hallDetails.getHallId();
		System.out.println("HALLID::: "+hallId);*/
		/*Hall hallDetails = new Hall(5L);
		hallDetails.populateHallDetails();*/

		/*Shows showDetails = new Shows(movieDetails, screenDetails, hallDetails);
		showDetails.addShowDetails();
		Long showId = showDetails.getShowId();
		System.out.println("SHOWID::: "+showId);
		
		Users userDetails = new Users("Sheldon Cooper", 32444224444L, "1,3,4", showDetails);
		userDetails.addUserDetails();
		Long userId = userDetails.getUserId();
		System.out.println("USERID::: "+userId);*/

		try {
			JSONArray jarr = new JSONArray();
			JSONObject jobj = new JSONObject();
			jobj.put("test", "hello");
			jobj.put("hello", "how are you");
			JSONObject jobjFinal = new JSONObject();
			jobjFinal.put("test", jobj);
			jobj = new JSONObject();
			jobj.put("yello", "blue");
			jobj.put("haall", "show");
			jobjFinal.put("test1", jobj);
			jarr.put(jobjFinal);
			RedisUtil.storeValueToRedis("jarr redis", jarr);
			JSONArray jarr1 = new JSONArray((String)RedisUtil.getValueFromRedis("jarr redis"));
			System.out.println("JARR::: "+jarr1);
			
		} catch(Exception e) {
			System.out.println("EXCEPTION e::: " + e);
			e.printStackTrace();
		}
	}
}
