package com.moodcatch;

import org.springframework.boot.SpringApplication;

public class TestMoodcatchCharacterApplication {

    public static void main(String[] args) {
        SpringApplication.from(MoodcatchCharacterApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
