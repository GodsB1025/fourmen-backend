package com.fourmen.meetingplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MeetingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingPlatformApplication.class, args);
	}

}
