package de.refactoringbot.scheduling;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * This Opens the Swagger-UI on startup of the application.
 * 
 * @author Stefan Basaric
 *
 */
@Component
class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

	@Value("${server.port}")
	private Integer port;

	private static final Logger logger = LoggerFactory.getLogger(ApplicationStartup.class);

	/**
	 * This method opens the Swagger-UI in the browser on startup of the
	 * application.
	 */
	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		Runtime runtime = Runtime.getRuntime();
		String url = "http://localhost:" + port + "/swagger-ui.html#";
		// Check OS-System
		String os = System.getProperty("os.name").toLowerCase();
		try {
			// Windows
			if (os.contains("win")) {
				runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
			}
			// MacOS
			if (os.contains("mac")) {
				runtime.exec("open " + url);
			}
			// Linux
			if (os.contains("nix") || os.contains("nux")) {
				runtime.exec("xdg-open " + url);
			}
		} catch (IOException e) {
			logger.error("Could not open Swagger-UI in the browser!");
		}
	}
}
