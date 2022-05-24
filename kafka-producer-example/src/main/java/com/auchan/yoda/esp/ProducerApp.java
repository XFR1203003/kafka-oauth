/*
Copyright © 2020 BlackRock Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.auchan.yoda.esp;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerApp {

	private final static Logger log = LoggerFactory.getLogger(Producer.class);

	public static void main(String[] args) {
		runProducer();
	}

	static void runProducer() {

		Producer<Long, String> producer = ProducerCreator.createProducer();

		for (long index = 0; index < IKafkaConstants.MESSAGE_COUNT; index++) {
			final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(IKafkaConstants.TOPIC_NAME,
					index, "This is record " + index);
			try {
				RecordMetadata metadata = producer.send(record).get();
				log.info("===========> Record sent to partition {}, offset {} with key {} and value \"{}\"",
						metadata.partition(), metadata.offset(), record.key(), record.value());
			} catch (ExecutionException | InterruptedException e) {
				log.error("===========> Error in sending record", e);
			}
		}
	}
}
