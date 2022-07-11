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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerApp {

	private final static Logger log = LoggerFactory.getLogger(ConsumerApp.class);

	public static void main(String[] args) throws FileNotFoundException, IOException {
		runConsumer();
	}

	static void runConsumer() throws FileNotFoundException, IOException {
		Consumer<Long, String> consumer = ConsumerCreator.createConsumer();

		// int noMessageToFetch = 0;
		int messagesPollMillis = 10000;

		boolean justOneTry = false;

		do {
			final ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofMillis(messagesPollMillis));
			// if (consumerRecords.count() == 0) {
			// 	noMessageToFetch++;
			// 	if (noMessageToFetch > IKafkaConstants.MAX_NO_MESSAGE_FOUND_COUNT) {
			// 		log.info("===========> still no message to fetch after {} tries. stopping now", noMessageToFetch);
			// 		break;
			// 	} else {
			// 		log.debug("===========> no message to fetch");
			// 		continue;
			// 	}
			// }

			consumerRecords.forEach(record -> {
				log.info("Record received on partition {}, offset {} with key {} and value \"{}\"",
						record.partition(), record.offset(), record.key(), record.value());
			});
			consumer.commitAsync();
		} while (!justOneTry);

		consumer.close();
	}
}
