package spotify;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

	@RequestMapping("/callback/")
	public ApiBuilder tokenValue(@RequestParam(value = "code", defaultValue = "Spotify access code") String value) {
		return new ApiBuilder(value);
	}
}
