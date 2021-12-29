package com.aksh.kinesis.producer;

import com.aksh.kinesis.producer.random.gen.RandomGenerator;
import org.junit.jupiter.api.Test;

class RandomGeneratorTest {

	@Test
	void test() {
		System.out.println(RandomGenerator.generateRandome("RANDOM_FLOAT5"));
	}

}
