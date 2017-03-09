package spotify;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.AddTrackToPlaylistRequest;
import com.wrapper.spotify.methods.CurrentUserRequest;
import com.wrapper.spotify.methods.PlaylistTracksRequest;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.PlaylistTrack;
import com.wrapper.spotify.models.User;

public class ApiBuilder {
	
	final String clientId = "clientID";
	final String clientSecret = "clientSecret";
	final String redirectURI = "redirectURI";

	final Api api = Api.builder().clientId(clientId).clientSecret(clientSecret).redirectURI(redirectURI).build();

	private final String code;

	public ApiBuilder() {
		this("Default code");
	};

	public ApiBuilder(String code) {
		this.code = code;
	}

	public String getCode() {

		/* Application details necessary to get an access token */
		final String codeToString = code.toString();

		/*
		 * Make a token request. Asynchronous requests are made with the
		 * .getAsync method and synchronous requests are made with the .get
		 * method. This holds for all type of requests.
		 */
		final SettableFuture<AuthorizationCodeCredentials> authorizationCodeCredentialsFuture = api
				.authorizationCodeGrant(codeToString).build().getAsync();

		/* Add callbacks to handle success and failure */
		Futures.addCallback(authorizationCodeCredentialsFuture, new FutureCallback<AuthorizationCodeCredentials>() {
			@Override
			public void onSuccess(AuthorizationCodeCredentials authorizationCodeCredentials) {
				/* The tokens were retrieved successfully! */
				System.out.println(
						"Successfully retrieved an access token! " + authorizationCodeCredentials.getAccessToken());
				System.out.println(
						"The access token expires in " + authorizationCodeCredentials.getExpiresIn() + " seconds");
				System.out.println("Luckily, I can refresh it using this refresh token! "
						+ authorizationCodeCredentials.getRefreshToken());

				/*
				 * Set the access token and refresh token so that they are used
				 * whenever needed
				 */
				api.setAccessToken(authorizationCodeCredentials.getAccessToken());
				api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

			}

			@Override
			public void onFailure(Throwable throwable) {
				/*
				 * Let's say that the client id is invalid, or the code has been
				 * used more than once, the request will fail. Why it fails is
				 * written in the throwable's message.
				 */

			}
		});
		
		/* Getting current user based on authorization */
		final CurrentUserRequest requestCurrentUser = api.getMe().build();
		

		try {
			/* Getting tracks from Spotify's Discover Playlist */
			final PlaylistTracksRequest requestForDiscoverWeeklyPlaylist = api
					.getPlaylistTracks("spotify", "spotifyPlaylistID").build();
			final List<String> tracksToAdd = new ArrayList<String>();
			final Page<PlaylistTrack> page = requestForDiscoverWeeklyPlaylist.get();
			final List<PlaylistTrack> playlistTracks = page.getItems();

			/* Creating List of tracks to add */
			for (PlaylistTrack playlistTrack : playlistTracks) {
				tracksToAdd.add(playlistTrack.getTrack().getUri());
			}
			
			final User user = requestCurrentUser.get();
			
			/* Adding tracks to from Spotify's playlist to User defined playlist*/
			final AddTrackToPlaylistRequest requestAddTracks = api
					.addTracksToPlaylist(user.getId(), "userPlaylistID", tracksToAdd).build();

			requestAddTracks.get();

		} catch (Exception e) {
			System.out.println("Something went wrong: " + e.getMessage());
		}

		return "Success";
	}

	public void createConnection() throws URISyntaxException, IOException {

		/*
		 * Set the necessary scopes that the application will need from the user
		 */
		final List<String> scopes = Arrays.asList("user-read-private", "user-read-email", "playlist-read-private",
				"playlist-modify-private");

		/* Set a state. This is used to prevent cross site request forgeries. */
		final String state = "someExpectedStateString";

		String authorizeURL = api.createAuthorizeURL(scopes, state);
		URL spotifyAuthorize = new URL(authorizeURL);

		/* Opening the web browser */
		String Command = "open " + spotifyAuthorize;
		Process Child = Runtime.getRuntime().exec(Command);

	}
}
