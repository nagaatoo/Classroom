package ru.numbdev.classroom;

import org.springframework.boot.SpringApplication;

public class TestClassroomApplication {

	public static void main(String[] args) {
		SpringApplication.from(ClassroomApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
