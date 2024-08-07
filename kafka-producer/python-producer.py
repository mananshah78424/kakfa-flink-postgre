import datetime
import random
import time
from json import dumps

import schedule
from faker import Faker
from kafka import KafkaProducer

kafka_nodes="kafka:9092"
topic="weather"


def gen_data():
    faker=Faker()
    prod=KafkaProducer(bootstrap_servers=kafka_nodes,value_serializer=lambda x:dumps(x).encode('utf-8'))
    my_data = {'city': faker.city(), 'temp': random.uniform(10.0, 110.0)}
    print(my_data)
    prod.send(topic=topic,value=my_data)
    prod.flush()

if __name__ == "__main__":
  gen_data()
  schedule.every(10).seconds.do(gen_data)

  while True:
    schedule.run_pending()
    time.sleep(0.5)