package de.refactoringbot.scheduling;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class performs scheduled operations.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class ScheduledOperations {

	@Value("${server.port}")
	Integer port;

	/**
	 * This method opens the Swagger-UI in the browser on startup of the
	 * application.
	 */
	@PostConstruct
	public void startSwaggerUI() {
		// Start runtime
		Runtime runtime = Runtime.getRuntime();
		// Create URL
		String url = "http://localhost:" + port + "/swagger-ui.html#";
		// Check OS-System
		String os = System.getProperty("os.name").toLowerCase();
		try {
			// Windows
			if (os.indexOf("win") >= 0) {
				runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
			}
			// MacOS
			if (os.indexOf("mac") >= 0) {
				runtime.exec("open " + url);
			}
			// Linux
			if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
				runtime.exec("xdg-open " + url);
			}
		} catch (IOException e) {
			System.err.println("Could not start Swagger-UI in the browser!");
		}
	}

}
