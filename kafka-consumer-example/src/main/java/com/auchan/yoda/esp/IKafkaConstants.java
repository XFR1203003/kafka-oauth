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

public interface IKafkaConstants {
	String KAFKA_BROKERS = "host.docker.internal:9092";

	String CLIENT_ID = "test-consumer";

	String TOPIC_NAME = "yoda-test-topic";

	String GROUP_ID_CONFIG = "test-consumer";

	Integer MAX_NO_MESSAGE_FOUND_COUNT = 10;

	String OFFSET_RESET_LATEST = "latest";

	String OFFSET_RESET_EARLIEST = "earliest";

	Integer MAX_POLL_RECORDS = 1000;
}
