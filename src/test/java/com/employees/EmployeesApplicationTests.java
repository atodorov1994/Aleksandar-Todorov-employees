package com.employees;

import com.employees.model.ErrorResponse;
import com.employees.model.Response;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc

class EmployeesApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void findEmployeesWorkingTogether_straightCase() throws Exception {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", new FileInputStream("src/test/resources/csvfiles/straight_case.csv"));

		var resultJson = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/employees")
						.file(mockMultipartFile))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();


		var expectedResult = objectMapper.readValue(Files.readString(Path.of("src/test/resources/response/sucess.json")), Response.class);
		var result = objectMapper.readValue(resultJson, Response.class);
		assertEquals(expectedResult, result);
	}

	@Test
	void findEmployeesWorkingTogether_failedNotCsv() throws Exception {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", new FileInputStream("src/test/resources/csvfiles/failed_not_csv.txt"));

		var resultJson = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/employees")
						.file(mockMultipartFile))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

		var expectedResult = new Response(2, new ErrorResponse("Unsupported csv format"));
		var result = objectMapper.readValue(resultJson, new TypeReference<Response<ErrorResponse>>() {});
		assertEquals(expectedResult, result);
	}
}
