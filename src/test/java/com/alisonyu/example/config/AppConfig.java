package com.alisonyu.example.config;

import com.alisonyu.airforce.configuration.anno.Configuration;
import com.alisonyu.airforce.configuration.anno.Value;

import java.util.Arrays;
import java.util.List;

/**
 * @author yuzhiyi
 * @date 2018/9/17 22:06
 */
@Configuration(prefix = "app")
public class AppConfig {
	@Value("name")
	private String name;
	@Value("number")
	private List<Integer> number;
	@Value("number")
	private Integer[] numbers;
	@Value("database")
	private DataBaseConfig dataBaseConfig;
	@Value("databases")
	private List<DataBaseConfig> dataBaseConfigs;
	@Value("databases")
	private DataBaseConfig[] dataBaseConfigArray;


	public String getName() {
		return name;
	}

	public List<Integer> getNumber() {
		return number;
	}

	public Integer[] getNumbers() {
		return numbers;
	}

	public void setNumbers(Integer[] numbers) {
		this.numbers = numbers;
	}

	@Override
	public String toString() {
		return "AppConfig{" +
				"name='" + name + '\'' +
				", number=" + number +
				", numbers=" + Arrays.toString(numbers) +
				", dataBaseConfig=" + dataBaseConfig +
				", dataBaseConfigs=" + dataBaseConfigs +
				", dataBaseConfigArray=" + Arrays.toString(dataBaseConfigArray) +
				'}';
	}
}
